#!/usr/bin/env bash

set -eu

echo "Checking port 4550"
processId_4550=`lsof -i -n -P | grep LISTEN | grep :4550 | awk '{print $2}'`

if [ ! -z "$processId_4550" ]
then
  echo "killing process with Id $processId_4550"
  kill -9 "$processId_4550"
else
  echo "There is no process running on port 4550"
fi

