FROM hseeberger/scala-sbt:11.0.6_1.3.8_2.13.1 AS build

RUN apt-get update && apt-get install -y \
    bash \
 && rm -rf /var/lib/apt/lists/*

RUN curl -L https://github.com/wirvomgut/curo/archive/0.1.1.tar.gz -o curo.tar.gz -s \
    && tar -xvzf curo.tar.gz -C / \
    && mv /curo* /curo \
    && rm curo.tar.gz \
    && cd /curo \
    && sbt clean stage

FROM adoptopenjdk/openjdk11:alpine

COPY --from=build /curo/target/universal/stage /curo
COPY --from=build /curo/docker/entry.sh /entry.sh

RUN apk add --update bash \
    && rm -rf /var/cache/apk/* \
    && mkdir /conf

EXPOSE 9000

ENTRYPOINT ["/entry.sh"]