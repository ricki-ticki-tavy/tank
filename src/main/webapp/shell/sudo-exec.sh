#!/usr/bin/env bash
semaphorFileName="/var/sudo-exec"
if [ -f "$semaphorFileName" ]; then
  rm "$semaphorFileName"
fi


while :
do
  if [ -f "$semaphorFileName" ]; then
     read command < "$semaphorFileName"
    rm "$semaphorFileName"
    $command
  fi
  sleep 0.3
done