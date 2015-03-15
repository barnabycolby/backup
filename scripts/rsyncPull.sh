#!/bin/bash

# Read in the variables from the config file
source ~/backup/config/defaultConfig

# The shareToPullFrom must end with a slash, otherwise rsync will create an extra directory under the destination directory
shareToPullFrom='/mnt/backupShares/barney/'

destination="${continuousDirectoryPath}/barney"

if [ -e ${shareToPullFrom} ] && [ -e ${destination} ]
then
	echo "Pulling from the share into the continuous backup directory."
	rsync -a --delete ${shareToPullFrom} ${destination}
else
	echo "Either the share to pull from or the destination directory did not exist."
fi
