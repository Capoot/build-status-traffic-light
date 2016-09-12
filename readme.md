# Build Status Traffic Light

**Build Status Traffic Light** is a Linux daemon that enables you to monitor and report multiple continuous integration jobs from Jenkins, Travis CI, or generically, simply by switching the color of a USB mini traffic light to correspond to the job's status. It comes with a Debian daemon and an automated out-of-the-box-installer for Debian-based Linux systems. 

To run Build Status Traffic Light, you must manually build and [install Clewarecontrol Software](https://github.com/zalando/build-status-traffic-light#install-clewarecontrol-software). You also need a traffic light; this project currently supports the [Cleware USB-MiniTrafficLight]
 (http://www.cleware-shop.de/epages/63698188.sf/en_US/?ViewObjectPath=%2FShops%2F63698188%2FProducts%2F41%2FSubProducts%2F41-1
 "View product information in the Cleware shop").

## Development Status
This version, released in July 2016, is early-stage and therefore might contain bugs, offer insufficient/incomplete documentation, or miss the mark in providing you with a great user experience. Please report any issues via [GitHub issues](https://github.com/zalando/build-status-traffic-light/issues), or by emailing the [maintainers](https://github.com/zalando/build-status-traffic-light/blob/master/MAINTAINERS). Development is in progress, and we will respond ASAP. 

## Jobs

The core concept of the build status monitor is a _job_, which represents an _artifact_ (job, project, etc.) built on a CI server. The daemon stores job information in JSON files in the ```data``` sub-directory of the installation directory. A job may be successful (green), unstable (yellow) or failed (red). The traffic light will display the status with the following colors:

- **Red:** build cancelled due to unrecoverable error affecting at least one job
- **Yellow:** at least one job's build was unstable; test assertion failed
- **Green:** all of the jobs built successfully

Job files are not machine-bound, so you can copy them to a backup or another installation, e.g. on a different machine.

## Jenkins Job

Jenkins Job info goes into the ```$TEBS_HOME/data``` directory (if it's not already present, you can create it). Create a file and give it the same name as the corresponding Jenkins job. Then attach the file ending with ```.json``` (every job will be configured in its own file).
 
Example:

Jenkins job name: build-status-traffic-light
File name: build-status-traffic-light.json

The file should include the following content:

```
{
	"type" : "jenkins",
	"host" : "https://besting.ci.zalan.do",
	"userName" : "johndoe",                  // optional
	"password" : "ws3f6deh7z6gu6ug",         // optional
	"acceptInsecureSslCert" : "true"         // optional, default: false
}
```

For a password, you can also specify Jenkins API tokens.

## Travis CI Jobs

With [Travis CI](https://travis-ci.org/), Build Status Traffic Light doesn't report the "unstable" status (yellow light); jobs either pass or fail. Travis builds all branches of a project separately, so you must specify which branch to query. A public Travis job does not require credentials, because the info is publicly available.  

The owner is the user or organization who owns the project. If you're not sure, check the URL of the build status. It
should look similar to [this one](https://travis-ci.org/zalando/build-status-traffic-light/pull_requests). The owner is
the first part of the path that comes after `travis-ci.org/`; in the example offered, it's "zalando".

```
{
    "type" : "travis-ci.org",
    "job" : "jobName",
    "owner" : "ownerName",
    "branch" : "master"
}
```

## Generic Jobs

Build Status Traffic Light's generic job format sends a GET request to any URL and parses the result with a regex. To create such a job, make a corresponding JSON file in the data dir, e.g. ```data/myjob.json```. The regex uses 
[Java format](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html).

The job will use the provided credentials to perform basic auth via HTTP. To access an unprotected API, simply skip specifying a userName and password.

```
{
  "type" : "generic-rest-api",
  "url" : "https://myhost:8081/myjob",
  "userName" : "johndoe",                  // optional
  "password" : "password",                 // optional
  "acceptInsecureSslCert" : "true",        // optional, default: false
  "successRegex" : ".*success.*",
  "unstableRegex" : ".*test failures.*"    // optional
}
```

When executed, the job will first try to match the response with the successful regex. If this fails, it will then try to match with the unstable regex. If none of the supplied regex matches, the job status will be "failure."

# Install and Run

## Install Clewarecontrol Software

This software controls the traffic light via USB and is required. To install, you must download and build it manually. The following instructions work for Raspian. Please note: this project is built for Clewarecontrol v.4.1 and might not support other versions.

1. Either [download the binaries](https://www.vanheusden.com/clewarecontrol/files/clewarecontrol-4.1.tgz), or clone from [this GitHub repo](https://github.com/flok99/clewarecontrol) (unfortunately, version 4.1 is not tagged)  

2. Install the hidabpi library

```
sudo apt-get install libhidapi-dev
```

3. Create the file ```/usr/share/pkgconfig/hidapi.pc```

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

7. Navigate to clewarecontrol-4.1 folder and run:

```
make install
```

8. Test your installation by running:

```
clewarecontrol -l
```

Note: on a non-Raspian system, you might have to locate the libraries first. Do it using:

```
ldconfig -p | grep libhidapi
```

## Install Build Status Daemon

Obtain a release archive either by downloading it from [our releases](https://github.com/zalando/build-status-traffic-light/releases) or by building it yourself with Maven:

```
git clone git@github.com:zalando/build-status-traffic-light.git
mvn package -P release
```

Either way, you will have a .tar.gz archive containing everything you need for installing and running the traffic light 
software. To extract the archive to your desired location, type:

```
tar xfz te-buildstatus-1.0.0.tar.gz -C /my/desired/installation/directory
cd /my/desired/installation/directory
```

Now run ```install.sh``` and follow the instructions on the screen. If you have an old installation, your machine will ask if you want to import jobs from your old installation. The installer will start the traffic light 
and set up the software to start automatically upon boot. It will create the init script ```/etc/init.d/tebs-daemon```,
which you can call with the arguments ```start```, ```stop``` or ```restart```.

# Contributing

To contribute, create a fork and pull request. We value automated tests and request that you provide at least the most essential test cases for your changes. We also emphasise code quality, so please commit only clean code that follows  [Java Coding Conventions](http://www.oracle.com/technetwork/java/codeconvtoc-136057.html).

# Contact

If you have a question about contributing, please contact someone listed in the [maintainers file](https://github.com/zalando/build-status-traffic-light/blob/master/MAINTAINERS).

## Continuous Integration

![master branch build status](https://travis-ci.org/zalando/build-status-traffic-light.svg?branch=master)

We used [Travis CI](https://travis-ci.org/zalando/build-status-traffic-light/) to build this project. All pull requests
will be built automatically and can only be merged if the build passes.

# Next steps

- Add support for Travis CI
- Add support for Go CD

# License

The MIT License (MIT) Copyright © 2016 Zalando SE, https://tech.zalando.com

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
documentation files (the “Software”), to deal in the Software without restriction, including without limitation the 
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit 
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the 
Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
