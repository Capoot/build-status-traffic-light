# Build Status Traffic Light

This project contains bash scripts for connecting Jenkins jobs with the Build Status Traffic Light. Additional 
information about installing the Clewarecontrol drivers and workarounds for known problems can be found here (request
 read access if you can't open the doc):

https://docs.google.com/document/d/1UCllGJj2ZUeLe1vxnVTs1pRWzoleir_1C9he31zUW6c/view

## Configuration

In order for the script to work, open it in an editor and modify the following values:

    USER= username that is used for login to Jenkins
    PASSWORD= password to access to Jenkins
    JENKINS_SERVER= URL of the jenkins server
    JOB_NAME= name of the job you want to follow
    DEVICE_NO= device number of traffic light

To get the device number, run the following command on the machine where the traffic light software is installed:

    > sudo clewarecontrol -l
    
## Convenience Script

The script in ```bash/light.sh``` provides a convenient way of controlling the traffic light manually from command 
line. Invoke as privileged user, e.g. root or with sudo.

```
> sudo ./light.sh (red|yellow|green) (on|off)
```


Example usage:

```
> sudo ./light.sh red on
> sudo ./light.sh green off
```

