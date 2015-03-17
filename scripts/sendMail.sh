#!/bin/bash

# Read in the variables from the config files
source ~/backup/config/defaultConfig

if [ $# -ne 2 ]
then
	echo "You must provide the subject and body as arguments to this script."
	exit 2
fi

subject="$1"
body="$2"

echo "${body}" | mail -s "${subject}" ${emailAddress}
