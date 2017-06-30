package eopc;

import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.cluster.BrokerEndPoint;
import kafka.common.ErrorMapping;
import kafka.common.OffsetAndMetadata;
import kafka.common.OffsetMetadataAndError;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;

public class ExactlyOncePartitionConsumer {
	private static final Logger logger = LoggerFactory.getLogger(ExactlyOncePartitionConsumer.class);
	private static final int BUFFER_SIZE = 100 * 1024 * 1024; // SimpleConsumer-buffer_size-100MB
	private static final long EXCEPTION_SLEEP_TIME = 180000L; // 3 mins
	private static final int correlationId = 0;
	private List<String> replicaBrokers = new ArrayList<String>();
	private List<String> brokers;
	private int port;
	private String topic;
	private int partition;
	private long readOffset;
	private String groupid;

	private SimpleConsumer consumer;

	public ExactlyOncePartitionConsumer(List<String> brokers, int port, String topic, int partition, long readOffset,
			String groupid) {
		this.brokers = brokers;
		this.port = port;
		this.topic = topic;
		this.partition = partition;
		this.readOffset = readOffset;
		this.groupid = groupid;
	}

	public void run() {
		PartitionMetadata metadata = findLeader(brokers); // 找到当前topic和partition的leaderBroker
		// 退出条件
		if (metadata == null) {
			logger.error("can't find metadata for topic and partition");
			return;
		}
		if (metadata.leader() == null) {
			logger.error("can't find leader for topic and partition");
			return;
		}
		String leadBroker = metadata.leader().host();
		String clientName = topic + "_" + partition;

		consumer = new SimpleConsumer(leadBroker, port, 100000, BUFFER_SIZE, clientName); // 创建SimpleConsumer
		long lastReadOffset = getLastOffset(consumer, topic, partition, kafka.api.OffsetRequest.EarliestTime(),
				clientName); // 获取kafka内的最新offset
		if (readOffset > lastReadOffset) {
			lastReadOffset = readOffset; // 入参更新lastReadOffset，可能来自其它存储
		}
		int numErrors = 0; // 设置error重复次数
		while (true) { // 开始循环consume
			if (consumer == null) {
				consumer = new SimpleConsumer(leadBroker, port, 100000, BUFFER_SIZE, clientName);
			}
			FetchRequest req = new FetchRequestBuilder().clientId(clientName)
					.addFetch(topic, partition, lastReadOffset, BUFFER_SIZE).build();
			FetchResponse fetchResponse = consumer.fetch(req);

			if (fetchResponse == null) // 退出循环
				break;

			// 开始error处理
			if (fetchResponse.hasError()) {
				++numErrors;
				short code = fetchResponse.errorCode(topic, partition);
				logger.warn("error fetching data from the broker:" + leadBroker + " reason: " + code);
				if (numErrors > 5) // 如果连续5次error则直接退出循环
					break;
				if (code == ErrorMapping.OffsetOutOfRangeCode()) {
					lastReadOffset = getLastOffset(consumer, topic, partition, kafka.api.OffsetRequest.LatestTime(),
							clientName);
					continue;
				}
				consumer.close();
				consumer = null;
				try {
					leadBroker = findNewLeader(leadBroker);
				} catch (Exception e) {
					logger.error("exit with exception", e);
				}
				continue;
			}

			numErrors = 0; // 重置error次数

			// 如果本次没有message则sleep后继续循环
			if (fetchResponse.messageSet(topic, partition).sizeInBytes() == 0) {
				logger.error("no enough data to consume, please check partition {} offset {}", partition,
						lastReadOffset);
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
				}
				continue;
			}

			long numRead = 0L;
			for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(topic, partition)) {
				long currentOffset = messageAndOffset.offset();
				if (currentOffset < lastReadOffset) { // 如果是过期消息则continue
					logger.info("found an old offset: {} expecting: {}", currentOffset, lastReadOffset);
					continue;
				}

				lastReadOffset = messageAndOffset.nextOffset(); // 更新offset
				ByteBuffer payload = messageAndOffset.message().payload();

				byte[] bytes = new byte[payload.limit()];
				payload.get(bytes);

				if (!handleMessage(messageAndOffset.offset(), bytes, consumer, clientName)) {
					logger.debug("end for consume data");
					release(consumer);
					return;
				}
				numRead += 1L;
			}

			if (numRead == 0L) { // 如果本次需要消费的message为0则sleep一段时间后继续循环
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public void release(SimpleConsumer consumer) {
		if (consumer != null)
			consumer.close();
	}

	public boolean handleMessage(long offset, byte[] bytes, SimpleConsumer consumer, String clientName) {
		logger.debug("offset: {}, message: {}", offset, new String(bytes));
		return commitOffset(consumer, groupid, topic, partition, offset, clientName);
	}

	private boolean commitOffset(SimpleConsumer consumer, String groupid, String topic, int partition, long offset,
			String clientName) {
		TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);

		Map<TopicAndPartition, OffsetAndMetadata> requestInfo = new HashMap<>();
		OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(offset, null, -1L);
		requestInfo.put(topicAndPartition, offsetAndMetadata);

		OffsetCommitRequest commitRequest = new OffsetCommitRequest(groupid, requestInfo, correlationId, clientName,
				kafka.api.OffsetRequest.CurrentVersion());

		OffsetCommitResponse response = null;

		while (true) {
			try {
				logger.debug("partition {} commit offest request", partition);
				response = consumer.commitOffsets(commitRequest);
				if (response != null)
					break;
			} catch (Exception e) {
				logger.error("Some error occur when fetch messages. Sleep 3 minutes.", e);
				try {
					Thread.sleep(EXCEPTION_SLEEP_TIME);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}

		return response.hasError();
	}

	public static long getLastOffset(SimpleConsumer consumer, String topic, int partition, long whichTime,
			String clientName) {
		TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
		Map requestInfo = new HashMap();
		requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
		kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo,
				kafka.api.OffsetRequest.CurrentVersion(), clientName);

		OffsetResponse response = null;

		while (true) {
			try {
				logger.debug("Partition {} fetch last offest request", partition);
				response = consumer.getOffsetsBefore(request);
				if (response != null)
					break;
			} catch (Exception e) {
				logger.error("Some error occur when fetch messages. Sleep 3 minutes.", e);
				try {
					Thread.sleep(EXCEPTION_SLEEP_TIME);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}

		if (response.hasError()) {
			logger.warn("Error fetching data Offset Data the Broker. Reason: {}", response.errorCode(topic, partition));
			return 0L;
		}
		long[] offsets = response.offsets(topic, partition);
		return offsets[0];
	}

	public static long fetchOffset(SimpleConsumer consumer, String groupid, String topic, int partition) {

		String clientName = topic + "_" + partition;
		TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);

		List<TopicAndPartition> requestInfo = new ArrayList<>();
		requestInfo.add(topicAndPartition);
		OffsetFetchRequest fetchRequest = new OffsetFetchRequest(groupid, requestInfo,
				kafka.api.OffsetRequest.CurrentVersion(), correlationId, clientName);

		OffsetFetchResponse response = null;

		while (true) {
			try {
				logger.debug("Partition {} fetch offest request", partition);
				response = consumer.fetchOffsets(fetchRequest);
				if (response != null)
					break;
			} catch (Exception e) {
				logger.error("Some error occur when fetch messages. Sleep 3 minutes.", e);
				try {
					Thread.sleep(EXCEPTION_SLEEP_TIME);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}

		OffsetMetadataAndError offset = response.offsets().get(topicAndPartition);
		if (offset.error() == 0)
			return offset.offset();
		else
			return 0;
	}

	private String findNewLeader(String oldLeader) throws Exception {
		for (int i = 0; i < 3; ++i) {
			boolean goToSleep = false;
			PartitionMetadata metadata = findLeader(this.replicaBrokers);
			if (metadata == null)
				goToSleep = true;
			else if (metadata.leader() == null)
				goToSleep = true;
			else if ((oldLeader.equalsIgnoreCase(metadata.leader().host())) && (i == 0))
				goToSleep = true;
			else
				return metadata.leader().host();
			if (goToSleep) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
		throw new Exception("unable to find new leader after broker failure. exiting");
	}

	private PartitionMetadata findLeader(List<String> brokers) {
		PartitionMetadata returnMetaData = null;
		loop: for (String seed : brokers) {
			SimpleConsumer consumer = null;
			try {
				consumer = new SimpleConsumer(seed, port, 100000, 64 * 1024, "leaderLookup");
				List<String> topics = Collections.singletonList(topic);
				TopicMetadataRequest req = new TopicMetadataRequest(topics);
				kafka.javaapi.TopicMetadataResponse resp = null;
				while (true) {
					try {
						logger.debug("partition {} fetch topic meta request", partition);
						resp = consumer.send(req);
						if (resp != null)
							break;
					} catch (Exception e) {
						logger.error("some error occur when fetch messages. sleep 3 minutes.", e);
						try {
							Thread.sleep(EXCEPTION_SLEEP_TIME);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}

				List<TopicMetadata> metaData = resp.topicsMetadata();
				for (TopicMetadata item : metaData) {
					for (PartitionMetadata part : item.partitionsMetadata()) {
						if (part.partitionId() == partition) {
							returnMetaData = part;
							break loop;
						}
					}
				}
			} catch (Exception e) {
				logger.warn("error communicating with broker " + "[{}] to find leader for [{}, {}] " + "reason: {}",
						seed, topic, partition, e);
			} finally {
				if (consumer != null)
					consumer.close();
			}
		}
		if (returnMetaData != null) {
			replicaBrokers.clear();
			for (BrokerEndPoint replica : returnMetaData.replicas()) {
				replicaBrokers.add(replica.host());
			}
		}
		return returnMetaData;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
