#!/bin/sh

rm /curo/RUNNING_PID
rm -R /curo/db
/curo/bin/curo -Dplay.crypto.secret=${PLAY_SECRET} -Dplay.evolutions.db.default.autoApply=true -Dconfig.file=/conf/curo.conf
