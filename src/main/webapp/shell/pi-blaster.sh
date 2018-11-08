#!/usr/bin/env bash
# /etc/init.d/pi-blaster
# starts the Tank 4G control
### BEGIN INIT INFO
# Provides:          pi-blaster
# Required-Start:
# Required-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# X-Interactive:     true
# Short-Description: Start/stop pi-blaster
### END INIT INFO

case "$1" in
start)
  sudo /opt/pi-blaster/pi-blaster --gpio 7
  ;;
stop)
  echo 7=0.15 > /dev/pi-blaster
  sudo ps aux | grep "/pi-blaster --gpio " | grep -v grep |   tr -s ' ' | cut -d ' ' -f 2 | xargs -r kill
  echo "stopped"
  ;;
*)
  echo $"Usage: $0 {start|stop}"
  exit 1
  ;;
esac