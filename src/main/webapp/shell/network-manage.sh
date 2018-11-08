#!/usr/bin/env bash

ADHOG_SSID=Tank
ADHOG_WPA2KEY=90309030

ROUTER_4G_POWER_GPIO_PIN=13

function checkGPI(){
  if [ ! -f "/sys/class/gpio/gpio"$1"/value" ]; then
    echo $1 > /sys/class/gpio/export
  fi
  echo out > "/sys/class/gpio/gpio"$1"/direction"
}
#----------------------------------------------------------------------------------------------

function restoreOrigInterfaces(){
   rm /etc/network/interfaces
   cp /etc/network/interfaces.orig /etc/network/interfaces
}

#-----------  запуск режима подключения к 4G wifi роутеру
function start4G(){
  checkGPI $1
  # включение питания модема
  echo 1 > "/sys/class/gpio/gpio"$1"/value"
  # пауза на установку соединения
  sleep 20
}
#----------------------------------------------------------------------------------------------

function 4GPwerOff(){
  checkGPI $1
  # отключение питания модема
  echo 0 > "/sys/class/gpio/gpio"$1"/value"
}
#----------------------------------------------------------------------------------------------

function stop4G (){
  4GPwerOff $1
}
#----------------------------------------------------------------------------------------------

function stopAdHogServices(){
   service isc-dhcp-server stop
   service hostapd stop
}
#----------------------------------------------------------------------------------------------

function startAdHogServices(){
   service isc-dhcp-server start
   service hostapd start
}
#----------------------------------------------------------------------------------------------
function adHogPatchInterfaces(){
   rm /etc/network/interfaces
   cp /etc/network/interfaces.adhog /etc/network/interfaces
}
#----------------------------------------------------------------------------------------------

if [ $2 ]; then
   ROUTER_4G_POWER_GPIO_PIN=$2
fi

case "$1" in
4g)
  stopAdHogServices
  ifconfig wlan0 down
  ifdown wlan0
  start4G $ROUTER_4G_POWER_GPIO_PIN
  ifconfig wlan0 up
  ifup wlan0
  ;;
wifi)
  stopAdHogServices
  restoreOrigInterfaces
  ifconfig wlan0 down
  ifdown wlan0
  stop4G $ROUTER_4G_POWER_GPIO_PIN
  ifconfig wlan0 up
  ifup wlan0
  ;;
adhog)
  stopAdHogServices
  ifconfig wlan0 down
  ifdown wlan0
  stop4G $ROUTER_4G_POWER_GPIO_PIN
  adHogPatchInterfaces
  /etc/init.d/networking restart
  ifconfig wlan0 up
  ifup wlan0
  sleep 8
ifconfig
  startAdHogServices
  ;;
*)
  echo $"Usage: $0 {4g|wifi|adhog}"
  exit 1
  ;;
esac

