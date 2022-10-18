#!/usr/bin/sh

cd backend
sh db-startup.sh

./server.sh "$@"