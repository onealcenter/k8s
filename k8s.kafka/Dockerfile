FROM openjdk:8-jre-alpine
MAINTAINER ly

ARG kafka_version=1.0.0
ARG scala_version=2.12

RUN apk add --no-cache --update \ 
    unzip \
    wget \
    bash \
    su-exec

ENV KAFKA_VERSION=$kafka_version \
    SCALA_VERSION=$scala_version

WORKDIR /

RUN wget http://mirrors.tuna.tsinghua.edu.cn/apache/kafka/${KAFKA_VERSION}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz; \
    tar zxvf kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz; \
    rm -rf kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz; \
    ln -s kafka_${SCALA_VERSION}-${KAFKA_VERSION} kafka; \
    mkdir kafka-logs

ENV KAFKA_HOME /kafka
ENV PATH ${PATH}:${KAFKA_HOME}/bin

EXPOSE 9092
