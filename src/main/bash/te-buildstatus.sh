#! /bin/sh
#  /etc/init.d/te-buildstatus

### BEGIN INIT INFO
# Provides:          te-buildstatus
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Short-Description: Starts the build status traffic light
# Description:       This file is used to start the daemon and should be placed in /etc/init.d
### END INIT INFO

# Author:   ${docker.maintainer}
# Url:      https://github.bus.zalan.do/testing-excellence/build-status-lamp
# Date:     ${maven.build.timestamp}

NAME="te-buildstatus"
DAEMON_LONG_NAME="Testing Excellence Build Status Traffic Light"

CLASS_PATH="$TE_LIB_DIR/*:$TE_BIN_DIR${project.build.finalName}.jar"
MAIN_CLASS="de.zalando.buildstatus.daemon.BuildStatusDaemon"

PID="/var/run/$NAME.pid"

LOG_OUT="${system.logDir}/$NAME.out"
LOG_ERR="${system.logDir}/$NAME.err"

jsvc_exec() {
    cd $FILE_PATH
    /usr/bin/jsvc -home $JAVA_HOME -cp $CLASS_PATH -user root -outfile $LOG_OUT -errfile $LOG_ERR -pidfile $PID $1 $MAIN_CLASS
}

case "$1" in
    start)
        echo "Starting $DAEMON_LONG_NAME..."
        jsvc_exec
        echo "$DAEMON_LONG_NAME has started."
    ;;
    stop)
        echo "Stopping $DAEMON_LONG_NAME..."
        jsvc_exec "-stop"
        echo "$DAEMON_LONG_NAME has stopped."
    ;;
    restart)
        if [ -f "$PID" ]; then
            echo "Restarting $DAEMON_LONG_NAME..."
            jsvc_exec "-stop"
            jsvc_exec
            echo "$DAEMON_LONG_NAME has restarted."
        else
            echo "Daemon not running, no action taken"
            exit 1
        fi
            ;;
    *)
    echo "Usage: /etc/init.d/$NAME {start|stop|restart}" >&2
    exit 3
    ;;
esac
