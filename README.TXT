------------------------------------------------------------
Предустановка ПО
------------------------------------------------------------
1) sudo apt-get install update

2) sudo apt-get install upgrade

3) sudo apt-get install htop

4) sudo apt-get install mc

5) sudo reboot now

6) sudo systemctl ssh enable

7) sudo update-rc.d ssh defaults

8) sudo update-rc.d ssh enable

9) sudo reboot now

-------------------------------------------------------------
для зацепа в местный вай фай
-------------------------------------------------------------

1) добавить в /etc/wpa_supplicant/wpa_supplicant.conf запись
    network={
    ssid="kv87s300"
    psk="WPA_PASSWORD"
    }

2) sudo reboot now

ДОПОЛНИТЕЛЬНАЯ ДОКА для синезуба   http://www.makeuseof.com/tag/setup-wi-fi-bluetooth-raspberry-pi-3/

---------------------------------------------------------------
JAVA 8
---------------------------------------------------------------

1) sudo apt-get install oracle-java8-jdk


---------------------------------------------------------------
TOMCAT
---------------------------------------------------------------

1) wget http://apache-mirror.rbc.ru/pub/apache/tomcat/tomcat-9/v9.0.0.M18/bin/apache-tomcat-9.0.0.M18.tar.gz

2) tar -xvf apache-tomcat-9.0.0.M18.tar.gz

3) переместить под рутом apache-tomcat-9.0.0.M15 в /opt

4) добавить в конец секции "tomcat-users" в файле ~/apache-tomcat-8.0.24/conf/tomcat-users.xml

        <role rolename="manager-script"/>
        <role rolename="manager-jmx"/>
        <role rolename="manager-gui"/>
        <role rolename="manager-status"/>
        <role rolename="tankDriver"/>
        <role rolename="driver"/>
        <role rolename="camControl"/>
        <role rolename="IrControl"/>
        <role rolename="login"/>
        <role rolename="engineer"/>
        <role rolename="shutdownAllow"/>
        <user username="pi" password="raspberry" roles="manager-gui,manager-jmx,manager-status,manager-script"/>
        <user username="driver" password="driver17" roles="driver,camControl,IrControl,login"/>
        <user username="guest" password="" roles="login"/>
        <user username="jar" password="driver17" roles="driver,camControl,IrControl,login"/>
        <user username="miha" password="driver17" roles="driver,camControl,IrControl,login"/>
        <user username="dasha" password="driver17" roles="driver,camControl,IrControl,login"/>
        <user username="admin" password="GhjcnjqGfhjkm2017" roles="driver,camControl,IrControl,login,shutdownAllow,engineer"/>

5) добавить в  server.xml
    <Connector port="9009" protocol="AJP/1.3" redirectPort="9443"
           address="192.168.0.101" useIPVHosts="true" />


6) в секцию  "valve param" в файле /webapps/manager/META-INF/context.xml дописать по аналогичному правилу свой IP с
   которого будем лазить на томкат (это не обязательно)


7) создать в /etc/init.d скопировать файл tomcat



8) в bin\catalina.sh после комментов вписать строку
   export JPDA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

   это для возможности запуска с удаленной отладкой. Для такого запуска выполнять
   sudo /opt/apache-tomcat-9.0.0.M15/bin/catalina.sh jpda start   или прописать это строку в /etc/rc.local

  !!!!!  Обязательно добавить в /etc/rc.local или скрипт запуска
        sudo rm /opt/apache-tomcat-9.0.0.M15/logs/* -f


9) для запуска без использования GP4J выполнить sudo update-rc.d tomcat defaults
     не объединять с предыдущим пунктом


10) sudo reboot now


------------------------------------------------------------------
Трансляция видео, выполнение фотографий
------------------------------------------------------------------

0) для камеры по шлейфу - в /etcrc.local вписать в самое начало
   sudo modprobe bcm2835-v4l2

1) sudo apt-get install libv4l-0

2) wget http://www.bobtech.ro/get?download=36:mjpg-streamer-rpi

3) Полученный wget-ом файл со страшным названием переименовываем чем-нибудь, например при помощи mc
   или
   mv get\?download\=36\:mjpg-streamer-rpi mjpg-streamer-rpi.tar.gz

4) tar -zxvf mjpg-streamer-rpi.tar.gz

5) sudo apt-get install libjpeg8

6) распакованный архив, тем же mc, переносим в /opt/

7) для запуска
   ./mjpg-streamer.sh start

8) Если камера достаточно новая, и звезды на небе находятся в правильном положении, то видео поток станет доступен по URL вида:
      http://raspberrypi:8080?action=stream
   Также вы можете встроить это виде в html страничку используя тег img: 
      <img src="http://raspberrypi:8080?action=stream"/>

   Если не взлетело

    открываем  mjpg-streamer.sh

        И видим там пять параметров:

        VIDEO_DEV="/dev/video0" - идентификатор устройства.
        FRAME_RATE="30" - частота кадров (FPS).
        RESOLUTION="640x480" - разрешение
        PORT="8080" - http порт
        YUV="false" - флаг YUV кодирования

        Три распространенные причины, по которым камера может вовсе не включиться:
            1.	Камера не поддерживает указанное разрешение - попробуйте указать 320х240;
            2.	Камера не поддерживает указанное значение FPS - попробуйте указать 15;
            3.	Камера не поддерживает кодирование MJPEG - установите флаг YUV в значение true;
        Распространенные причины по которым камера может тормозить:
            1.	Камера не поддерживает аппаратное кодирование MJPEG - выкиньте камеру
            2.	Указанные значения разрешения и FRAME_RATE не являются оптимальными - попробуйте перебрать другие значения

9) для управления видео с помощью проекта нужно сделать добавление в файл mjpg-streamer.sh

            #!/bin/bash
            export LD_LIBRARY_PATH=.

            if [ $2 ]; then
               VIDEO_DEV="/dev/"$2
            else
               VIDEO_DEV="/dev/video1"
            fi

            FRAME_RATE="15"

            if [ $3 ]; then
               RESOLUTION=$3
            else
               RESOLUTION="320x240"
            fi

            if [ $4 ]; then
               PORT=$4
            else
               PORT="8082"
            fi
            YUV="true"



----------------------------------------------------
ШИМ (PWM) через DMA
----------------------------------------------------

1) wget https://github.com/sarfata/pi-blaster/archive/master.zip

2) unzip master.zip

3) sudo apt-get install autoconf

4) в pi-blaster.c

исправить на
#define CYCLE_TIME_US 8000
#define SAMPLE_US   8

600
5

5) ./autogen.sh

6) ./configure

7) make

8) скопировать pi-blaster в /opt/pi-blaster/

9) создать файл /etc/init.d/pi-blaster с содержимым из shell/pi-blaster.sh



---------------------------------------
для настройки перехода в режим точки доступа
-------------------------------------


1) sudo apt-get install hostapd isc-dhcp-server

2) 	sudo update-rc.d hostapd disable

3) sudo update-rc.d isc-dhcp-server disable

4) /etc/dhcp/dhcpd.conf
комментим строки
   option domain-name "example.org";
   option domain-name-servers ns1.example.org, ns2.example.org;
дописываем
   subnet 192.168.1.0 netmask 255.255.255.0 {
     range 192.168.1.10 192.168.1.20;
     option subnet-mask 255.255.255.0;
     option broadcast-address 192.168.1.255;
     option routers 192.168.1.1;
     default-lease-time 600;
     max-lease-time 7200;
     option domain-name "local";
     option domain-name-servers 192.168.1.1;
   }
снимаем коммент с
   #authoritative;

5) /etc/network/interfaces копируем в /etc/networking/interfaces.orig

6) /etc/network/interfaces копируем в /etc/networking/interfaces.adhog

7) /etc/networking/interfaces.adhog правим

    allow-hotplug wlan0
    #iface wlan0 inet manual
    #    wpa-conf /etc/wpa_supplicant/wpa_supplicant.conf
    iface wlan0 inet static
    address 192.168.1.1
    netmask 255.255.255.0

8) /etc/default/isc-dhcp-server правим чтобы были строки
    DHCPD_PID=/var/run/dhcpd.pid
    DHCPD_CONF=/etc/dhcp/dhcpd.conf
    INTERFACES="wlan0"

9) touch /var/lib/misc/udhcpd.leases


10) поместить network-manage в /etc/init.d/

11) в /etc/rc.local
    sudo /etc/init.d/network-manage wifi


11-1) /etc/hostapd/hostapd.conf

interface=wlan0
driver=nl80211
ssid=Tank-02-01
hw_mode=g
channel=6
wpa=2
wpa_passphrase=90309030
macaddr_acl=0
wpa_key_mgmt=WPA-PSK
wpa_pairwise=TKIP
rsn_pairwise=CCMP
auth_algs=1

12) для перехода в режим точки доступа - sudo /etc/init.d/adhog start
    для перехода в нормальное состояние - sudo /etc/init.d/adhog stop
    после перезагрузки системы все будет в обычном состоянии, если не выполнить пункт 11. Если его не выполнить, то
    все будет совсем фигово работать так как службы точки доступа и DHCP подняты не будут, а настройки сетевой карты -
    уже не те. Однако start/stop - все всегда поправит

---------------------------------------------------------------
для управления автовыключением питания на схеме
---------------------------------------------------------------

power-control - управление блоком питания. Устанавливается в /etc/init.d и далее update-rc.d powerControl defaults.
   Собственно вся его работа сводится к удержанию высокого уровня на нужном выводе, который удерживает таймер выключения
    блока питания от запуска (23 сек)


shutdowner.sh - скрипт останавливающий систему либо при появлении файла /opt/shutdown  , либо при появлении на выводе
                GPIO4 низкого уровня. Так же выключение системы выполняется при появлении файла /opt/shutdown. Это
                сделано для выключения системы из явы.

установка: содержимое этого скрипта в файл /etc/init.d/

---------------------------------------------------------------
управление через интернет
---------------------------------------------------------------

1) Регистрируем VPS (Virtual Private Server) например на ruvds.com и ставим Debian поновее

2) заходим на сервер и делаем
     a) apt-get update
     b) apt-get upgrade
     c) apt-get install htop
     d) apt-get install mc
     e) в /etc/ssh/ssh_config дописать
        ServerAliveInterval 20
     f) adduser tank
        Пароль - как понравится
     g) /etc/ssh/sshd_config проверить, что есть параметр
          GatewayPorts yes

3) заходим по ssh на танк и выполняем следующее
    a) ssh-keygen  -t rsa -b 2048
    b) ssh-copy-id tank@VPS_IP
       вводим пароль, созданный в пункте 2-f

4) для проброса портов пишем и выполняем следующие команды
   ssh -f -N -R 8080:localhost:8080 tank@VPS_IP
   ssh -f -N -R 8082:localhost:8082 tank@VPS_IP

5) заходим на IP VPS  и управляем





---------------------------------------------------------------
4G
---------------------------------------------------------------

1) используем свисток с WiFi раздаткой (например хуайвей E8372 )
2) настраиваем через веб-морду нормальный SSID и пароль
3) Настраиваем профиль для подключения в той же веб морде в соответствии с выбранным оператором

4) пин GPIO 9 будет управлять питанием свистка (можно через копеечную релюшку 5В и IGBT прямо от лапки проца)

для режима hi-link то есть как сетевая карта модемов huawei нужно

1) в каталог /etc/udev/rules.d создать файл  70-huawei_e8372.rules с содержимым

ACTION=="add", SUBSYSTEM=="usb", ATTRS{idVendor}=="12d1", ATTRS{idProduct}=="1f01", RUN+="/usr/sbin/usb_modeswitch --default-vendor 0x12d1 --default-product 0x1f01 -J"

но проверить, что код вендра и девайса соврпадают. Для проверки используем lsusb и нажодим наш модем. Перед ним
циферки - это они.

2) sudo udevadm control --reload-rules

3) sudo reboot now

теперь он висит как eth1

---------------------------------------------------------------
Включение I2C
---------------------------------------------------------------

внести изменения в файл /boot/config.txt
1) раскомментировать
   dtparam=i2c_arm=on

2) добавить
   dtparam=i2c_baudrate=50000

3) создать файл /etc/modprobe.d/i2c.conf с содержимым
   options i2c_bcm2708 baudrate=400000

4) sudo reboot now

5) для проверки текущей скорости использовать  sudo cat /sys/module/i2c_bcm2708/parameters/baudrate

6) Для просмотра подключенных на шине устройств sudo i2cdetect -y 1


---------------------------------------------------------------
 !!!!!!!!!!!!!!!!    Клонирование, изменение размеров партиций, резервные копии
---------------------------------------------------------------

1) sudo apt-get install piclone

2) подключаем ридер с флэшкой (для этого в проекте предусмотрен режим 3, который просто подключает питание к разъему
   свистка, а ридер втыкаем в гнездо свистка)

3) Подключаемся с любой никсовой машины, набрав в терминале  ssh -X -l pi 192.168.0.100 'piclone' и введя пароль

4)


---------------------------------------------------------------
 OpenCV
---------------------------------------------------------------
1)

    sudo apt-get install build-essential cmake pkg-config libpng12-0 libpng12-dev libpng++-dev libpng3
    sudo apt-get install libpnglite-dev zlib1g-dbg zlib1g zlib1g-dev pngtools  
    sudo apt-get install libtiffxx0c2 libtiff-tools libjpeg8 libjpeg8-dev libjpeg8-dbg libjpeg-progs       (sudo apt-get install libtiffxx5 libtiff-tools libjpeg62-turbo libjpeg62-turbo-dev libjpeg62-turbo-dbg libjpeg-progs)
    sudo apt-get install libavcodec-dev libavformat-dev libgstreamer0.10-0-dbg libgstreamer0.10-0
    sudo apt-get install  libgstreamer0.10-dev   libunicap2-dev libdc1394-22-dev libdc1394-22
    sudo apt-get install  libdc1394-utils swig
    sudo apt-get install libv4l-dev libv4l-0 libunicap2 libtiff4   (libtiff5)
    sudo apt-get install ant
    sudo apt-get install libgstreamer0.10-0-dbg libgstreamer0.10-0 libgstreamer0.10-dev libgstreamer-plugins-base0.10-dev







2)  сd ~
    git clone git://git.videolan.org/x264
    cd x264
    ./configure --host=arm-unknown-linux-gnueabi --disable-static --enable-shared --disable-opencl
    make -j4
    sudo make install

3)  cd ~
    git clone https://github.com/FFmpeg/FFmpeg.git
    cd ffmpeg
    sudo ldconfig
    sudo ./configure --arch=armel --target-os=linux --enable-gpl --enable-libx264 --enable-nonfree --disable-static --enable-shared
    make -j4
    sudo make install


4)  #sudo apt-get install libxvidcore-dev
    sudo apt-get install libhdf5-dev


5)  wget https://codeload.github.com/Itseez/opencv/zip/3.2.0
    mv 3.2.0 opencv.zip
    unzip opencv.zip
    cd opencv-3.2.0/
    mkdir build
    cd build

6) cmake -J4 -D CMAKE_BUILD_TYPE=RELEASE -D WITH_OPENCL=OFF -D BUILD_PERF_TESTS=OFF -D BUILD_SHARED_LIBS=ON -D JAVA_INCLUDE_PATH=$JAVA_HOME/include -D JAVA_AWT_LIBRARY=$JAVA_HOME/jre/lib/amd64/libawt.so -D JAVA_JVM_LIBRARY=$JAVA_HOME/jre/lib/arm/server/libjvm.so -D CMAKE_INSTALL_PREFIX=/usr/local ..

7) make -j4

8) sudo make -j4 install

9) copy biild/lib/libopencv_java320.so в /usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre/lib/arm

10) edit /etc/ld.so.conf and add this path:
   /usr/local/libevent-1.4.14b/lib
   /usr/local/lib

   do
   ldconfig

   это для работы ffmpeg

-----------------------------------------------------------------------------------------------
   Мониторинг трафика
-----------------------------------------------------------------------------------------------

sudo apt-get install iptraf

-----------------------------------------------------------------------------------------------
   Увеличение мощьности WIFI
-----------------------------------------------------------------------------------------------


1) wget https://www.kernel.org/pub/software/network/wireless-regdb/wireless-regdb-2017.03.07.tar.xz
   тут свежие версии https://www.kernel.org/pub/software/network/wireless-regdb/
2) unpack
3) исправить раздел
country RU: DFS-ETSI

 увеличить значения в скобках с 15-20 до 40-60
4) make
5) copy regulatory.bin to /lib/crda
6) copy all *.pem to  /lib/crda/pubkey

7) зайти в raspi-config и выставить локаль ДЛЯ WI-FI - россия

8) sudo reboot now

-----------------------------------------------------------------------------------------------






это должно добавиться в итоге в /etc/rc.local

sudo rm /opt/apache-tomcat-9.0.0.M15/logs/* -f
sudo /etc/init.d/network-manage wifi
sudo /opt/apache-tomcat-9.0.0.M15/bin/catalina.sh jpda start
sudo /etc/init.d/shutdowner > stdoutfile 2> stderrfile &



RS362175985CN
RU037673230HK
RU038256589HK
ZA014446597HK
LM276487455CN




backup sudo dd bs=10m if=/dev/disk2 | gzip > /Users/jane/Downloads/raspberry3_SSH_MC_HTOP_Apache_mySql.gz

gunzip --stdout /Users/jane/Downloads/raspberry3_SSH_MC_HTOP_Apache_mySql.gz | sudo dd bs=10M of=/dev/disk2



Orange Pi zero

разводка http://micro-pi.ru/orange-pi-pinout-pin-definition-gpio/

0) java - качать с оракла, распаковывать и прописывать path и JAVA_HOME
1) распиновка http://micro-pi.ru/orange-pi-pinout-pin-definition-gpio/
2) wifi   nmtui-connect YOUR_ROUTER_SSID
3) X11 sudo apt-get install xbase-clients    sudo apt-get install xauth
4) piclone
       git clone https://github.com/golfromeo-fr/piclone.git
       sudo apt-get install intltool
       sudo apt-get remove gtk+-2
       cd piclone
       ./configure --sysconfdir=/etc
       make -j4
       sudo make -j4 install




ARDUINO


функции
       https://www.arduino.cc/en/Reference/HomePage