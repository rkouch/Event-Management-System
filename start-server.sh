#!/usr/bin/sh

cd backend
sh db-startup.sh

if [ $# -ne 0 ]; then
    gradle run --args=\""$@"\"
else
    gradle run
fi