#!/bin/bash
#question1:kube api address
#question2:redis-cli
_namespace=`cat cfg | grep namespace | awk -F= '{print $2}'`
_services=`cat cfg | grep service | awk -F= '{print $2}'`
i=1
while((1==1)) 
do
  split=`echo $_services | cut -d " " -f$i`
  if [ "$split" != "" ]
  then 
    ((i++))
    _ips=`curl http://127.0.0.1:8001/api/v1/namespaces/$_namespace/endpoints/$split | grep '\"ip\"' | awk '{print $2}' | awk -F\" '{print $2}'`
    j=1
    while((1==1))
    do
      split2=`echo $_ips | cut -d " " -f$j`
      if [ "$split2" != "" ]
      then
        ((j++))
        ./redis-cli -p 6379 sadd kubepods $split2
      else
        break
      fi
    done
  else
    break
  fi
done
