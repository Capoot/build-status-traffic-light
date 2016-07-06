#! /bin/bash
#  /etc/init.d/tebs_daemon

### BEGIN INIT INFO
# Provides:          tebs_daemon
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Short-Description: Starts the TEBS service
# Description:       Control the TEBS daemon and should be placed in /etc/init.d
### END INIT INFO

path=$(readlink -f /etc/init.d/tebs-daemon)
path=$(dirname $path)
path=$(cd $path && pwd)
export TEBS_HOME=$path

function startDaemon {
    if [ -e ${TEBS_HOME}/pid ]
    then
        echo "PID file exists, service might be running. Delete ${TEBS_HOME}/pid to override"
        exit 1
    fi
    echo "Starting Build Status Traffic light..."
    bash ${TEBS_HOME}/tebs ${TEBS_HOME}/data &
    echo $! > ${TEBS_HOME}/pid
}

function stopDaemon {
    if [ -e ${TEBS_HOME}/pid ]
    then
        echo "Stopping Build Status Traffic light..."
        kill -9 $(cat ${TEBS_HOME}/pid)
        rm -f ${TEBS_HOME}/pid
    else
        echo "Error: no PID file found. Is the daemon actually running?"
    fi
}

case "$1" in
    start)
        startDaemon
    ;;
    stop)
        stopDaemon
    ;;
    restart)
        if [ -e ${TEBS_HOME}/pid ]; then
            echo "Restarting Build Status Traffic light..."
            stopDaemon
            startDaemon
        else
            echo "Error: Build Status Traffic light is not running. Restart command ignored."
            exit 1
        fi
            ;;
    *)
    echo "Usage: /etc/init.d/tebs_daemon {start|stop|restart}" >&2
    exit 3
    ;;
esac