#!/bin/bash

docker run --rm -it -e SLAPD_PASSWORD=test -e SLAPD_DOMAIN=test.com -p 127.0.0.1:389:389 dinkel/openldap
