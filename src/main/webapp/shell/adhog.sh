#!/usr/bin/env bash
# /etc/init.d/adhog
# starts the Tank adhog control
### BEGIN INIT INFO
# Provides:          adhog
# Required-Start:
# Required-Stop:
# Default-Start:
# Default-Stop:
# X-Interactive:     true
# Short-Description: Start/stop adhog
### END INIT INFO

case "$1" in
start)
   service isc-dhcp-server stop
   service hostapd stop
   ifdown wlan0
   rm /etc/network/interfaces
   cp /etc/network/interfaces.adhog /etc/network/interfaces
   ifup wlan0
   service isc-dhcp-server start
   service hostapd start
  ;;
stop)
   service hostapd stop
   service isc-dhcp-server stop
   ifdown wlan0
   rm /etc/network/interfaces
   cp /etc/network/interfaces.orig /etc/network/interfaces
   ifup wlan0
  ;;
*)
  echo $"Usage: $0 {start|stop}"
  exit 1
  ;;
esac

