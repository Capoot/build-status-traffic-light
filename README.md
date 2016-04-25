This project contains script for connecting Jenkins and traffic light.

Few parameters needs to be set in script in order to work:

    USER= username that is used for login to Jenkins
    PASSWORD= password to access to Jenkins
    JENKINS_SERVER= URL of the jenkins server
    JOB_NAME= name of the job you want to follow
    DEVICE_NO= device number of traffic light

To get device number, run the following command on machine where traffic light
software is installed:

    sudo clewarecontrol -l