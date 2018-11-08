#!/usr/bin/env bash
# /etc/init.d/forwardPorts
# starts the port forwarder service
### BEGIN INIT INFO
# Provides:          forwardPorts
# Required-Start:
# Required-Stop:
# Default-Start:
# Default-Stop:      0 1 6
# X-Interactive:     true
# Short-Description: Start/stop forwardPorts
### END INIT INFO

function stop_channels(){
  if [ -f "/var/run/ssh-vps-http.pid" ]; then
    read pid1 < /var/run/ssh-vps-http.pid
    kill -9 $pid1
    rm "/var/run/ssh-vps-http.pid"
  fi

  if [ -f "/var/run/ssh-vps-video.pid" ]; then
    read pid2 < /var/run/ssh-vps-video.pid
    kill -9 $pid2
    rm "/var/run/ssh-vps-video.pid"
  fi

  if [ -f "/var/run/ssh-vps-video2.pid" ]; then
    read pid2 < /var/run/ssh-vps-video2.pid
    kill -9 $pid2
    rm "/var/run/ssh-vps-video2.pid"
  fi
  sleep 0.1
}

function start_channels(){
  cmd="ssh -f -N -R 8080:localhost:8080 tank@$VPS_HOST -p $VPS_PORT"
  $cmd
  sleep 0.1

  ps aux | grep "$cmd" | grep -v grep |   tr -s ' ' | cut -d ' ' -f 2 > /var/run/ssh-vps-http.pid


  cmd="ssh -f -N -R 8082:localhost:8082 tank@$VPS_HOST -p $VPS_PORT"
  $cmd
  sleep 0.1
  ps aux | grep "$cmd" | grep -v grep |   tr -s ' ' | cut -d ' ' -f 2 > /var/run/ssh-vps-video.pid

  cmd="ssh -f -N -R 8084:localhost:8084 tank@$VPS_HOST -p $VPS_PORT"
  $cmd
  sleep 0.1
  ps aux | grep "$cmd" | grep -v grep |   tr -s ' ' | cut -d ' ' -f 2 > /var/run/ssh-vps-video2.pid
}


if [ "$2" == "" ]; then
   VPS_HOST="194.87.144.98"
else
   VPS_HOST=$2
fi
if [ "$3" == ""T ]; then
   VPS_PORT="9009"
else
   VPS_PORT=$3
fi

case "$1" in
start)
  stop_channels
  start_channels
  ;;
stop)
  stop_channels
  ;;
*)
  echo $"Usage: $0 {start|stop}"
  exit 1
  ;;
esac
