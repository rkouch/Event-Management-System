#!/bin/sh

if [ $# -ne 0 ]; then
  ./gradlew run --args=\""$@"\"
else
 ./gradlew run
fi