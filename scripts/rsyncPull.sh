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

# The share to pull the files from
shareToPullFrom="${backupShares}/${shareToBackup}"

# The destination directory to pull from
destination="${continuousDirectoryPath}/${shareToBackup}"

if mount | grep ${shareToPullFrom} > /dev/null
then
	# Check that the destination is not a file
	if [ -f ${destination} ]
	then
		echo "Destination ${destination} is not a directory."
		exit 1
	fi
	
	# Check that the destination directory actually exists
	if [ ! -e ${destination} ]
	then
		mkdir ${destination}
	fi

	echo "Pulling from the share into the continuous backup directory."
	# The slash on the end of the source directory is important
	# Otherwise rsync would create a copy of the folder under the destination tree, adding another unnecessary level of depth
	rsync -a --delete ${shareToPullFrom}/ ${destination}
else
	echo "Share to pull from has not been mounted: ${shareToPullFrom}"
fi
