#!/bin/bash

if [ $# -ne 1 ]
then
	echo "You must only provide one argument, the name of the share."
	exit 1
fi

# We take the first argument as the name of the share to backup
shareToBackup="$1"

# Read in the variables from the config file
source ~/backup/config/defaultConfig

# The shareToPullFrom must end with a slash, otherwise rsync will create an extra directory under the destination directory
shareToPullFrom="${backupShares}/${shareToBackup}/"

# The destination directory to pull from
destination="${continuousDirectoryPath}/${shareToBackup}"

if [ -e ${shareToPullFrom} ] && [ -e ${destination} ]
then
	echo "Pulling from the share into the continuous backup directory."
	rsync -a --delete ${shareToPullFrom} ${destination}
else
	echo "Either the share to pull from or the destination directory did not exist."
fi
