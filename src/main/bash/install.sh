#!/bin/bash

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

NEW_PATH=$(dirname $0)
NEW_PATH=$(cd $NEW_PATH && pwd )

echo
echo "Please note:"
echo "This will install the Testing Excellence Build Status Traffic light version ${project.version} in $NEW_PATH. "\
     "If you have an older version on this machine, all daemon entries in /etc/init.d will be automatically removed. "\
     "The installation directory of the old version, however, will not be deleted by this process."
echo
echo "What it will do:"
echo
echo "  * Setup the script daemon.sh as /etc/init.d/tebs-daemon"
echo "  * make an update-rc.d entry to autostart the daemon on boot"
echo
echo "Do you want to continue? (y/n)"

read continueInstallation
if [ $continueInstallation != "y" ]
then
    echo "Aborted installation"
    exit 0
fi

if [ -e "/etc/init.d/tebs-daemon" ]
then

    /etc/init.d/tebs-daemon stop

    path=$(readlink -f /etc/init.d/tebs-daemon)
    path=$(dirname $path)
    path=$(cd $path && pwd)

    echo "There is an old installation in $path. Do you want to import jobs and configuration from that installation? "\
         "(y/n)"

    read importJobs
    if [ $importJobs == "y" ]
    then

        FILES="${path}/data/*"
        DIR=$(pwd)
        mkdir ${DIR}/data/
        for f in $FILES
        do
            BASENAME=$(basename $f)
            echo "copying job file $f to ${DIR}/data/${BASENAME}..."
            cp $f ${DIR}/data/${BASENAME}
        done

        echo "Finished importing job files. You may revert to the old installation by running ${path}/install.sh
        (supported from version 1.1.0)"
    else
        echo "Continuing without importing old jobs"
    fi
fi

echo "creating logging conf in ${NEW_PATH}/conf..."

mkdir ${NEW_PATH}/log
mkdir ${NEW_PATH}/conf

LOG_FILE=${NEW_PATH}/conf/log4j.properties
touch $LOG_FILE

echo "log4j.rootLogger=INFO, file" >> $LOG_FILE
echo "log4j.appender.file=org.apache.log4j.RollingFileAppender" >> $LOG_FILE
echo "log4j.appender.file.File=${NEW_PATH}/log/tebs.log" >> $LOG_FILE
echo "log4j.appender.file.MaxFileSize=2MB" >> $LOG_FILE
echo "log4j.appender.file.MaxBackupIndex=5" >> $LOG_FILE
echo "log4j.appender.file.layout=org.apache.log4j.PatternLayout" >> $LOG_FILE
echo "log4j.appender.file.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n" >> $LOG_FILE

echo "Updating daemon link /etc/init.d/tebs-daemon to point to ${NEW_PATH}/daemon.sh"

rm /etc/init.d/tebs-daemon
ln -s ${NEW_PATH}/daemon.sh /etc/init.d/tebs-daemon
update-rc.d tebs-daemon defaults
/etc/init.d/tebs-daemon start
