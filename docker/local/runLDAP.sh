#!/bin/bash

if [[ "$OSTYPE" == "darwin"* ]]; then
 docker-machine start
 docker-machine env
 eval $(docker-machine env)
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

docker run -it --rm --volume $DIR/ldif-files:/etc/ldap.dist/prepopulate -p 0.0.0.0:389:389 -e SLAPD_PASSWORD=password -e SLAPD_DOMAIN=example.org dinkel/openldap