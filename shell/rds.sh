#!/bin/bash
_anykubepod=`./redis-cli -p 6379 srandmember kubepods`
./redis-trib.rb check $_anykubepod:6379 | awk -F: '{print $1,$2}' | awk '{if ($1=="M") {cmd="./redis-cli -p 6379 hmset idipipid "$2" "$3" "$3" "$2" && ./redis-cli -p 6379 sadd rdsips "$3;system(cmd)}}'
./redis-trib.rb check $_anykubepod:6379 | awk -F: '{print $1,$2}' | awk '{if ($1=="S") {id=$2;cmd="./redis-cli -p 6379 hmset idipipid "$2" "$3" "$3" "$2" && ./redis-cli -p 6379 sadd rdsips "$3;system(cmd);getline;getline;cmd="./redis-cli -p 6379 sadd "$2"_s "id;system(cmd)}}'

