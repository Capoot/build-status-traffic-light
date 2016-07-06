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

# Install and run

## Install Clewarecontrol Software

This software controls the traffic light via USB and is a precondition. For the installation it has to be downloaded 
and built via make. The following instructions work for Raspian.

1. Download from
https://www.vanheusden.com/clewarecontrol/files/clewarecontrol-4.1.tgz

2. Install hidabpi library

```
sudo apt-get install libhidapi-dev
```

3. Create file ```/usr/share/pkgconfig/hidapi.pc```

```
prefix=/usr 
exec_prefix=${prefix} 
includedir=${prefix}/include 
libdir=${exec_prefix}/lib/arm-linux-gnueabihf

Name: hidapi 
Description: The hidapi library 
Version: 4.1
Cflags: -I${includedir}/hidapi
Libs: -L${libdir} -llibhidapi-hidraw -llibhidapi-libusb
```

4. Run:
```
export PKG_CONFIG_PATH=$PKG_CONFIG_PATH:/usr/share/pkgconfig
```

5. Run:
```
ln -s /usr/lib/arm-linux-gnueabihf/libhidapi-libusb.so.0.0.0 /usr/lib/arm-linux-gnueabihf/liblibhidapi-libusb.so
ln -s /usr/lib/arm-linux-gnueabihf/libhidapi-hidraw.so.0.0.0 /usr/lib/arm-linux-gnueabihf/liblibhidapi-hidraw.so
```

6. Run:
```
ld -llibhidapi-libusb --verbose
ld -llibhidapi-hidraw --verbose
```

7. navigate to clewarecontrol-4.1 folder and run:

```
make install
```

8. Test installation, run:

```
clewarecontrol -l
```

Note: on a system different from Raspian you might have to locate the libraries first. Do it using:

```
ldconfig -p | grep libhidapi
```

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
Starting Build Status Traffic light...
```

Now create an update-rc entry:

```
update-rc.d tebs-daemon defaults
```

# Configure a Jenkins job

The Jenkins Job info will go in the ```$TEBS_HOME/data``` dir (create if not present). Create a file and name it the
same as the corresponding job in Jenkins, and attach the file ending ```.json``` (every job will be configured in 
it's own file).
 
Example:

Jenkins job name: build-status-traffic-light
File name: build-status-traffic-light.json

The file should have the following content:

```
{
	"type" : "jenkins",
	"host" : "https://besting.ci.zalan.do",
	"userName" : "johndoe",
	"password" : "ws3f6deh7z6gu6ug",
	"acceptInsecureSslCert" : "true"
}
```

As a password you can also specify Jenkins API tokens

# Configure a generic job

There is a generic job format which can send a GET request to any URL and parse the result with a regex. To create 
such a job, create a corresponding JSON file in the data dir, e.g. ```data/myjob.json```. The regex uses Java format,
see: https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html

The job will use the provided credentials to perform basic auth via HTTP. You can also opt to not specify userName 
and password in order to access an unprotected API.

```
{
  "type" : "generic-rest-api",
  "url" : "https://myhost:8081/myjob",
  "userName" : "johndoe",
  "password" : "password",
  "acceptInsecureSslCert" : "true",
  "successRegex" : ".*success.*",
  "unstableRegex" : ".*test failures.*"
}
```
