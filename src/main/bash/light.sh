#!/bin/bash

# Script to for easier command line control of the traffic light. The lights can
# be individually toggeled on or off. It may be required to run the script as a 
# privileged user.
#
# Example usage:
# sudo ./light.sh red on
# sudo ./light.sh red off


case $1 in
    "red" )
        COLOR=0 ;;

    "yellow" )
        COLOR=1 ;;

    "green" )
        COLOR=2 ;;

    "?" )
        echo "Toggles the traffic light colored lights on and off."
        echo "Usage: sudo $0 {red|yellow|green} {on|off}"
	echo "E.g.:"
	echo "sudo $0 red on"
	echo "sudo $0 red off"
	exit 0
        ;;
esac

case $2 in
    "on" )
        STATUS=1 ;;

    "off" )
        STATUS=0 ;;
esac

clewarecontrol -c 1 -d -1 -as $COLOR $STATUS > /dev/null 2>&1
