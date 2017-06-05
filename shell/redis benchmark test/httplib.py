#!/usr/bin/env python
#coding=utf-8
from random import Random
import httplib,time,threading,sys

def random_str(randomlength=64):
    str = ''
    chars = 'AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789'
    length = len(chars) - 1
    random = Random()
    for i in range(randomlength):
        str+=chars[random.randint(0, length)]
    return str

def redis_cluster():
    count = 0
    try:
        conn1 = httplib.HTTPConnection("10.1.171.170:80")
        conn2 = httplib.HTTPConnection("10.1.171.170:80")
        while (count<10000):
            temp = random_str()
            conn1.request("GET", "/set2?key="+temp+"&val="+temp, "", {"Connection":"Keep-Alive"})
            conn2.request("GET", "/get2?key="+temp, "", {"Connection":"Keep-Alive"})
            conn1.getresponse().read()
            conn2.getresponse().read()
            count = count + 1
        conn1.close()
        conn2.close()
        return "suc"
    except:
        return "error"
    
if __name__ == "__main__":
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
