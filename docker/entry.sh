#!/bin/bash

rm /curo/RUNNING_PID
/curo/bin/curo -Dplay.crypto.secret=${PLAY_SECRET} -Dplay.evolutions.db.default.autoApply=true
