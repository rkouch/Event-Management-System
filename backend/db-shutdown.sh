#!/usr/bin/bash

CONTAINER_NAME=tickr-db

docker stop $CONTAINER_NAME
docker rm $CONTAINER_NAME