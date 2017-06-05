FROM centos:7
MAINTAINER ly

ADD redis_cluster.lua /
ADD nginx.conf /
ADD redis.lua /

WORKDIR /
RUN yum -y upgrade && yum install -y gcc gcc-c++ readline-devel pcre-devel openssl-devel tcl perl wget make \
    && wget http://luajit.org/download/LuaJIT-2.0.4.tar.gz \
    && wget http://openresty.org/download/ngx_openresty-1.9.7.2.tar.gz \
    && tar -zxvf LuaJIT-2.0.4.tar.gz \
    && tar -zxvf ngx_openresty-1.9.7.2.tar.gz

WORKDIR /LuaJIT-2.0.4
RUN make && make install && export LUAJIT_LIB=/usr/local/lib && export LUAJIT_INC=/usr/local/include/luajit-2.0

WORKDIR /ngx_openresty-1.9.7.2
RUN ./configure --with-luajit && make && make install

WORKDIR /
RUN cp redis_cluster.lua /usr/local/openresty/lualib
RUN rm -rf /usr/local/openresty/nginx/conf/nginx.conf
RUN cp nginx.conf /usr/local/openresty/nginx/conf
RUN rm -rf /usr/local/openresty/lualib/resty/redis.lua
RUN cp redis.lua /usr/local/openresty/lualib/resty

EXPOSE 80

ENTRYPOINT /usr/local/openresty/nginx/sbin/nginx -c /usr/local/openresty/nginx/conf/nginx.conf && /bin/bash && sleep 9999d
