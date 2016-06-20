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

## Command line interface

The build status monitor has a command line interface (CLI) to add, remove, update and list current jobs. In order to
work, CLI requires the environment variable ```TE_DATA_DIR``` set to a location where jobs are stored. This 
environment variable will also be used by the daemon to read the jobs. It may point to any directory to which the 
user has read/write access (also, make sure it's consistent with the daemon).

The command to invoke the CLI is ```tebs``` and supports the following actions:

### Add Jenkins job

- ```add-jenkins```
    * ```--host <host>``` the host should be written as URL, as in ```http://jenkins-host:8080```
    * ```--jobname <jobname>``` the name of the job, as it is referenced in Jenkins. This job name will also be used 
    to reference the job configuration for the build status monitor.
    * ```--user <username>``` name of the user which should be used for accessing the Jenkins API
    * ```--password <password/token>``` password or personal access token for the user accessing the Jenkins API; you
     can create a personal access token via ```http://<jenkins-server>/user/<username>/configure```

**We recommend using the personal access token instead of the password!** If the token ever gets into wrong hands it 
can be easily invalidated via ```http://<jenkins-server>/user/<username>/configure```.

### Remove job

- ```remove <jobname>``` removes the job identified by ```jobname```. An overview of available jobs and their names 
can be retrieved via the list command (see below)

### List jobs

- ```list``` lists an overview of all jobs currently configured

