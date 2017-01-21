FROM openjdk:8u92-jdk-alpine

RUN apk add --update bash git \
    && git clone https://github.com/wirvomgut/curo.git /curo-dev \
    && cd /curo-dev \ 
    && ./sbt clean stage \
    && cd / \
    && mv /curo-dev/target/universal/stage /curo \
    && mv /curo-dev/docker/entry.sh /entry.sh \
    && rm -R /curo-dev \
    && mkdir /conf \
    && apk del git \
    && rm -rf /var/cache/apk/*


EXPOSE 9000

ENTRYPOINT ["/entry.sh"]