#!/usr/bin/env bash
# /etc/init.d/4G
# starts the Tank 4G control
### BEGIN INIT INFO
# Provides:          4G
# Required-Start:
# Required-Stop:
# Default-Start:
# Default-Stop:      0 1 6
# X-Interactive:     true
# Short-Description: Start/stop 4G
### END INIT INFO

checkGPIO9(){
  if [ ! -f "/sys/class/gpio/gpio9/value" ]; then
    echo 9 > /sys/class/gpio/export
  fi
  echo out > /sys/class/gpio/gpio9/direction
}

SSID=Tank
WPA2KEY=90309030

case "$1" in
start)
  checkGPIO9
  # включение питания модема
  echo 1 > /sys/class/gpio/gpio9/value
  # пауза на установку соединения
  sleep 7

  ifdown wlan0

  rm /etc/wpa_supplicant/wpa_supplicant.conf

  echo 'country=GB'> /etc/wpa_supplicant/wpa_supplicant.conf
  echo 'ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev' >> /etc/wpa_supplicant/wpa_supplicant.conf
  echo 'update_config=1' >> /etc/wpa_supplicant/wpa_supplicant.conf
  echo 'network={' >> /etc/wpa_supplicant/wpa_supplicant.conf
  echo 'ssid="'$SSID'"' >> /etc/wpa_supplicant/wpa_supplicant.conf
  echo 'psk="'$WPA2KEY'"' >> /etc/wpa_supplicant/wpa_supplicant.conf
  echo '}' >> /etc/wpa_supplicant/wpa_supplicant.conf

  #Однозначное выключение возможно включенного ранее AdHog
  rm /etc/network/interfaces
  cp /etc/network/interfaces.orig /etc/network/interfaces

  ifup wlan0
  ;;
stop)
  rm /etc/wpa_supplicant/wpa_supplicant.conf
  cp /etc/wpa_supplicant/wpa_supplicant.conf.orig /etc/wpa_supplicant/wpa_supplicant.conf
  ifdown wlan0
  ifup wlan0
  checkGPIO9
  # отключение питания модема
  echo 0 > /sys/class/gpio/gpio9/value
  ;;
*)
  echo $"Usage: $0 {start|stop}"
  exit 1
  ;;
esac

