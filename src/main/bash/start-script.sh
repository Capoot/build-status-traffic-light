#!/bin/sh
 
# This script is used to control a USB Traffic Light from Cleware. You can buy 
# the USB Traffic Light from this shop: http://www.cleware-shop.de
# 
# The script uses the REST API and can be used for newer versions of Jenkins that provide a REST API.
#
# Requirements:
# 
#   The Cleware USB Traffic Light comes with a control software that you can 
#   download from http://www.vanheusden.com/clewarecontrol/ 
#
#   This script can be run under Linux. You need to have "curl" installed, 
#   so the script can poll the REST API.
#
#   This script has been tested under Ubuntu and clewarecontrol 2.0
#
# @MarcelBirkner
# 
 
USER=<insert user>
PASSWORD=<insert password>
JENKINS_SERVER=<insert server>
JOB_NAME=<insert job name>
DEVICE_NO=<device number>

FLAG=
 
# Methods for controlling the device (2=blue, 1=yellow, 0=red)
lightOn() {
  sudo  clewarecontrol -c 1 -d $DEVICE_NO -as $1 1 2>&1 
}
lightOff() {
  sudo clewarecontrol -c 1 -d $DEVICE_NO -as $1 0 2>&1 
}
allOff() {
  lightOff 0;
  lightOff 1;
  lightOff 2;
}
# Change light only if color of jenkins job was changed otherwise do nothing
changeLight(){
  if [ "$FLAG" != $1 ]; then
    allOff;
    FLAG=$1;
    lightOn $2;
  fi
}
# When job is building - blinking animation is shown on traffic light
blink(){
  for i in `seq 1 3`;
  do
    lightOn $2;
    sleep 0.1;
    lightOff $2;
    sleep 0.1;
  done
  lightOn $2
  FLAG=$1
}
 
while true; do 
  color=`curl -k -silent -u $USER:$PASSWORD $JENKINS_SERVER/$JOB_NAME/api/json?pretty=true | grep color `
  state=`echo $color | sed 's/\"//g' | sed 's/,//g' | awk '{print $3}'`  
  case $state in 
    red)          echo "State: $state"; changeLight $state 0;;
    yellow)       echo "State: $state"; changeLight $state 1;;
    blue)         echo "State: $state"; changeLight $state 2;;
    red_anime)    echo "State: $state"; blink $state 0;;
    yellow_anime) echo "State: $state"; blink $state 1;;
    blue_anime)   echo "State: $state"; blink $state 2;;
    *)            echo "Nothing matched state: $state";;  
  esac;
  sleep 1;  
done;
