#!/bin/sh
# /etc/init.d/powerControl.sh
# starts the Tank power control
### BEGIN INIT INFO
# Provides:          PowerControl
# Required-Start:
# Required-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# X-Interactive:     true
# Short-Description: Start/stop powerControl
### END INIT INFO

case "$1" in
start)
  if [ -f "/sys/class/gpio/gpio6/value" ]; then
    echo 6 > /sys/class/gpio/unexport
  fi
  echo 6 > /sys/class/gpio/export
  echo out > /sys/class/gpio/gpio6/direction
  echo 1 > /sys/class/gpio/gpio6/value
  ;;
stop)
  echo 0 > /sys/class/gpio/gpio6/value
  ;;
*)
  echo $"Usage: $0 {start|stop}"
  exit 1
  ;;
esac

