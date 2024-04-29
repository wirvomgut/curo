FROM sbtscala/scala-sbt:eclipse-temurin-jammy-11.0.22_7_1.9.9_2.13.13 AS build

RUN curl -L https://github.com/wirvomgut/curo/archive/0.1.2.tar.gz -o curo.tar.gz -s \
    && tar -xvzf curo.tar.gz -C / \
    && mv /curo* /curo \
    && rm curo.tar.gz \
    && cd /curo \
    && sbt clean stage

FROM eclipse-temurin:11.0.23_9-jre-alpine

COPY --from=build /curo/target/universal/stage /curo
COPY --from=build /curo/docker/entry.sh /entry.sh

RUN apk add --update bash \
    && rm -rf /var/cache/apk/* \
    && mkdir /conf

EXPOSE 9000

ENTRYPOINT ["/entry.sh"]