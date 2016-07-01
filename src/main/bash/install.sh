#!/bin/bash

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

NEW_PATH=$(dirname $0)
NEW_PATH=$(cd $NEW_PATH && pwd )

echo "This will uninstall all previous versions of the tebs-daemon and install the new version from $NEW_PATH instead."
echo "Do you want to continue? (y/n)"

read continueInstallation
if [ $continueInstallation != "y" ]
then
    echo "Aborted installation"
    exit 0
fi

/etc/init.d/tebs-daemon stop

if [ -e "/etc/init.d/tebs-daemon" ]
then

    path=$(readlink -f /etc/init.d/tebs-daemon)
    path=$(dirname $path)
    path=$(cd $path && pwd)

    echo "There is an old installation in $path"
    echo "Do you want to import jobs and configuration from that installation? (y/n)"

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

echo "Updating daemon link /etc/init.d/tebs-daemon to point to ${NEW_PATH}/daemon.sh"

rm /etc/init.d/tebs-daemon
ln -s ${NEW_PATH}/daemon.sh /etc/init.d/tebs-daemon
update-rc.d tebs-daemon defaults
/etc/init.d/tebs-daemon start
