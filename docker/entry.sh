#!/bin/sh
/curo/bin/curo -Dplay.http.secret.key=${PLAY_SECRET} -Dplay.evolutions.db.default.autoApply=true
