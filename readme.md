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

## Install Build Status Daemon

Obtain a release archive, either via download from releases in the GitHub repo:
https://github.bus.zalan.do/testing-excellence/build-status-lamp

Or build it yourself with Maven:

```
mvn -P release package
```

Copy the resulting archive to your desired location, unpack it and change into the directory

```
tar xfz te-buildstatus-1.0.0.tar.gz
cd te-buildstatus-1.0.0
```

Open ```daemon.sh``` and add the TEBS_HOME variable before the function ```startDaemon``` at line 11:

```
...
### END INIT INFO

export TEBS_HOME=/full/path/to/your/installation/te-buildstatus-1.0.0/

function startDaemon {
...
```

Save the changes to the script. As an alternative you may find your own way to supply the environment variable 
```TEBS_HOME``` to the ```daemon.sh``` script.

## Running the build status daemon

After having completed the installation as described above, you may run the script ```daemon.sh``` with the arguments
 ```start|stop|restart``` (requires root privileges!).
 
E.g.: 
```
./daemon.sh start
Starting Build Status Traffic light...
./daemon.sh stop
Stopping Build Status Traffic light...
```

## Configuring the build status daemon to run at system boot

Create a soft link to the script in ```/etc/init.d``` (requires root privileges) 

```
ln -s /full/path/to/your/installation/te-buildstatus-1.0.0/daemon.sh /etc/init.d/tebs-daemon
```

Try running it:

```
/etc/init.d/tebs-daemon start
```

Now create an update-rc entry:

```
update-rc.d tebs-daemon defaults
```
