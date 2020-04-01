#!/bin/sh
set -x
confd -backend env -onetime
nginx -g "daemon off;"
