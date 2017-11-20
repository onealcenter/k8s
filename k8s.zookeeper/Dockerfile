FROM openjdk:8-jre-alpine
MAINTAINER ly

# Install required packages
RUN apk add --no-cache \
    bash \
    su-exec

ENV ZOO_USER=zookeeper \
    ZOO_CONF_DIR=/conf \
    ZOO_DATA_DIR=/zk/data

# Add a user and make dirs
RUN set -ex; \
    adduser -D "$ZOO_USER"; \
    mkdir -p "$ZOO_DATA_DIR" "$ZOO_CONF_DIR"; \
    chown "$ZOO_USER:$ZOO_USER" "$ZOO_DATA_DIR" "$ZOO_CONF_DIR"

ARG GPG_KEY=D0BC8D8A4E90A40AFDFC43B3E22A746A68E327C1
ARG DISTRO_NAME=zookeeper-3.3.6

# Download Apache Zookeeper, verify its PGP signature, untar and clean up
RUN set -ex; \
    apk add --no-cache --virtual .build-deps \
        gnupg; \
    wget -q "http://www.apache.org/dist/zookeeper/$DISTRO_NAME/$DISTRO_NAME.tar.gz"; \
    wget -q "http://www.apache.org/dist/zookeeper/$DISTRO_NAME/$DISTRO_NAME.tar.gz.asc"; \
    export GNUPGHOME="$(mktemp -d)"; \
    gpg --keyserver ha.pool.sks-keyservers.net --recv-key "$GPG_KEY" || \
    gpg --keyserver pgp.mit.edu --recv-keys "$GPG_KEY" || \
    gpg --keyserver keyserver.pgp.com --recv-keys "$GPG_KEY"; \
    gpg --batch --verify "$DISTRO_NAME.tar.gz.asc" "$DISTRO_NAME.tar.gz"; \
    tar -xzf "$DISTRO_NAME.tar.gz"; \
    rm -rf "$GNUPGHOME" "$DISTRO_NAME.tar.gz" "$DISTRO_NAME.tar.gz.asc"; \
    apk del .build-deps; \
    touch /"$ZOO_DATA_DIR"/myid; \
    chmod 777 /"$ZOO_DATA_DIR"/myid

WORKDIR $DISTRO_NAME
VOLUME ["$ZOO_DATA_DIR", "$ZOO_CONF_DIR"]

EXPOSE 2181
EXPOSE 2888 
EXPOSE 3888

ENV PATH=$PATH:/$DISTRO_NAME/bin \
    ZOOCFGDIR=$ZOO_CONF_DIR