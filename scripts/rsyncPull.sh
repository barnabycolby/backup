#!/bin/bash

# Read in the variables from the config files
source ~/backup/config/defaultConfig
source ~/backup/config/rsyncPullConfig

if [ $# -ne 1 ]
then
	errorMessage="You must only provide one argument, the name of the share."
	echo ${errorMessage}
	echo ${errorMessage} | mail -s "${emailSubject}" ${emailAddress}
	exit 1
fi

# We take the first argument as the name of the share to backup
shareToBackup="$1"

# The share to pull the files from
shareToPullFrom="${backupShares}/${shareToBackup}"

# The destination directory to pull from
destination="${continuousDirectoryPath}/${shareToBackup}"

if mount | grep ${shareToPullFrom} > /dev/null
then
	# Check that the destination is not a file
	if [ -f ${destination} ]
	then
		errorMessage="Destination ${destination} is not a directory."
		echo ${errorMessage}
		echo ${errorMessage} | mail -s "${emailSubject}" ${emailAddress}
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
	rsyncOutput="$( rsync -a --delete ${shareToPullFrom}/ ${destination} 2>&1 )"

	# Check that the rsync operation was successful
	if [ $? -ne 0 ]
	then
		echo "${rsyncOutput}"
		echo "${rsyncOutput}" | mail -s "${emailSubject}" ${emailAddress}
	fi

else
	errorMessage="Share to pull from has not been mounted: ${shareToPullFrom}"
	echo ${errorMessage}
	echo ${errorMessage} | mail -s "${emailSubject}" ${emailAddress}
fi
