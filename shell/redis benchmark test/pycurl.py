#!/usr/bin/env python
#coding:utf-8
import time
from rediscluster import StrictRedisCluster
from random import Random
import threading
import sys
import pycurl

def random_str(randomlength=64):
    str = ''
    chars = 'AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789'
    length = len(chars) - 1
    random = Random()
    for i in range(randomlength):
        str+=chars[random.randint(0, length)]
    return str

def redis_cluster():
    c1 = pycurl.Curl()
    c2 = pycurl.Curl()
    #c1.setopt(c1.URL,'http://10.1.171.170/set2?')
    #c2.setopt(c2.URL,'http://10.1.171.170/get2?')
    count = 0
    while (count<10000):
        temp = random_str()
        c1.setopt(c1.URL,'http://10.1.171.170/set2?key='+temp+'&val='+temp)
        #print 'key='+temp+'&val='+temp
        #c1.setopt(c1.POSTFIELDS,'key='+temp+'&val='+temp)
        print c1.perform()

        #print 'key='+temp
        #c2.setopt(c2.POSTFIELDS,'key='+temp)
        c2.setopt(c2.URL,'http://10.1.171.170/get2?key='+temp)
        print c2.perform()

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
