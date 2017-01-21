#!/bin/bash

docker run --rm -it -e SLAPD_PASSWORD=password -e SLAPD_DOMAIN=example.org -p 127.0.0.1:389:389 dinkel/openldap
