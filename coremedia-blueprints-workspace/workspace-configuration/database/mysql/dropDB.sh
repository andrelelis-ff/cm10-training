#!/bin/sh -e
cd `dirname $0`
mysql --user=root -p -f < `pwd`/dropDB.sql
