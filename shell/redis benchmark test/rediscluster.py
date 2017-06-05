#!/usr/bin/env python
#coding:utf-8
import time
from rediscluster import StrictRedisCluster
from random import Random
import threading
import sys

def random_str(randomlength=64):
    str = ''
    chars = 'AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789'
    length = len(chars) - 1
    random = Random()
    for i in range(randomlength):
        str+=chars[random.randint(0, length)]
    return str

def redis_cluster():
    redis_nodes =  [{'host':'10.1.171.242','port':6379},
                    {'host':'10.1.171.242','port':6380},
                    {'host':'10.1.171.242','port':6381}
                   ]
    try:
        redisconn = StrictRedisCluster(startup_nodes=redis_nodes)
    except Exception,e:
        sys.exit(1)
    
    count = 0
    while (count<10000):
        temp = random_str()
        redisconn.set(temp,temp)
        redisconn.get(temp)
        count = count + 1

if __name__ == '__main__':
    t1 = time.time()
    threadpool = []
    for i in xrange(10):
        th = threading.Thread(target=redis_cluster)
        threadpool.append(th)
    for th in threadpool:
        th.start()
    for th in threadpool:
        threading.Thread.join(th)
    t2 = time.time()
    print t2-t1
