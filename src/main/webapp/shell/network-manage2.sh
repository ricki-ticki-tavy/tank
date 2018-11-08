#!/usr/bin/env bash

# with additional WiFi device

ROUTER_4G_POWER_GPIO_PIN=13
POWER=20

function prepareGpioPin(){
  if [ ! -f "/sys/class/gpio/gpio"$ROUTER_4G_POWER_GPIO_PIN"/value" ]; then
    echo $1 > /sys/class/gpio/export
  fi
  echo out > "/sys/class/gpio/gpio"$ROUTER_4G_POWER_GPIO_PIN"/direction"
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

if [ $2 ]; then
   ROUTER_4G_POWER_GPIO_PIN=$2
fi

if [ $3 ]; then
   POWER=$3
fi

case "$1" in
adhog)
  stopAdHogServices
  prepareGpioPin
  echo 0 > "/sys/class/gpio/gpio"$ROUTER_4G_POWER_GPIO_PIN"/value"
  sleep 2
  echo 1 > "/sys/class/gpio/gpio"$ROUTER_4G_POWER_GPIO_PIN"/value"
  sleep 4

  ifconfig wlan1 down
  sleep 2
  iw reg set BO
  sleep 1
  iwconfig wlan1 txpower $POWER
  ifconfig wlan1 up
  sleep 3
  iwconfig
  echo --------------------
  ifconfig

  startAdHogServices
  ;;
stop)
  stopAdHogServices
  ifconfig wlan1 down
  echo 0 > "/sys/class/gpio/gpio"$ROUTER_4G_POWER_GPIO_PIN"/value"
  ;;
*)
  echo $"Usage: $0 {adhog|stop}"
  exit 1
  ;;
esac