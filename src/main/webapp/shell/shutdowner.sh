#!/bin/sh

shutdownSemaphorFileName="/opt/shutdown"
if [ -f "$shutdownSemaphorFileName" ]; then
  rm "$shutdownSemaphorFileName"
fi


if [ -f "/sys/class/gpio/gpio4/value" ]; then
  echo 4 > /sys/class/gpio/unexport
fi
echo 4 > /sys/class/gpio/export
echo in > /sys/class/gpio/gpio4/direction

while :
do
  if [ -f "$shutdownSemaphorFileName" ]; then
    rm "$shutdownSemaphorFileName"
    shutdown now
    exit 0
  fi
  for i in {1..4}
  do
     read rslt < /sys/class/gpio/gpio4/value
     if [ $rslt = "0" ]; then
       shutdown now
       exit 0;
     fi
     sleep 0.3
  done
done