#!/bin/sh
# /etc/init.d/tomcat
# starts the Apache Tomcat service
### BEGIN INIT INFO
# Provides:          tomcat
# Required-Start:
# Required-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# X-Interactive:     true
# Short-Description: Start/stop tomcat application server
### END INIT INFO

export CATALINA_HOME="/opt/apache-tomcat-9.0.0.M15"
case "$1" in
start)
  if [ -f $CATALINA_HOME/bin/startup.sh ];
  then
    echo $"Starting Tomcat"
#    /bin/su pi $CATALINA_HOME/bin/startup.sh
    $CATALINA_HOME/bin/startup.sh
  fi
  ;;
stop)
  if [ -f $CATALINA_HOME/bin/shutdown.sh ];
  then
    echo $"Stopping Tomcat"
#    /bin/su pi $CATALINA_HOME/bin/shutdown.sh
    $CATALINA_HOME/bin/shutdown.sh
  fi
  ;;
*)
  echo $"Usage: $0 {start|stop}"
  exit 1
  ;;
esac

