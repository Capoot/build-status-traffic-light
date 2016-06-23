# Build Status Traffic Light

This software allows a number of CI Jobs (e.g. Jenkins) to be monitored and reports the corresponding job's status by
switching the color of a traffic light. Supports multiple jobs: will display green only if **all** of the jobs build 
successfully. If only one job fails, the traffic light will display failure.
 
 Currently supported traffic lights:
 
 - http://www.cleware-shop.de/epages/63698188.sf/en_US/?ViewObjectPath=%2FShops%2F63698188%2FProducts%2F41%2FSubProducts%2F41-1

## Signal color code

- **Red:** at least one job failed to build
- **Yellow:** at least one job's build was unstable (failing tests)
- **Green:** all of the jobs built successfully
- **Flashing (any color):** there is at least one job currently building; the color is from the last outcome before 
the current build

# Install and run

TODO: how to download and use predefined image

## Install Clewarecontrol Software

TODO: how to...

## Install Apache JSVC

TODO: how to...

## Install Build Status Daemon

TODO: note to myself:

    TE_LIB_DIR=${system.libDir}
    TE_BIN_DIR=${system.binDir}
    TE_CONF_DIR=${system.confDir}
    TE_DATA_DIR=${system.dataDir}