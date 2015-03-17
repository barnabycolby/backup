#!/bin/bash

# Store the script start time so we can write it to the log later
scriptStartTime="$(date '+%A %d %B %Y %X')"

# Check that we've been passed a config file and that it's exists
if [ $# -ne 1 ] || [ ! -f $1 ]
then
	echo "The first argument of this script must be an existing config file."
	exit 1
fi

# Read in the variables from the default config file for all backups
source ../config/defaultConfig

# Read in the variables from the config file that was passed in as an argument
source $1

# Redirect all program output to the logfile
echo ${filePipePath}
echo -n "Opening FIFO pipe....."
mkfifo ${filePipePath}
echo "Done."
echo "Piping output to logfile as well as stdout."
tee ${logFile} < ${filePipePath} &
teepid=$!
exec > ${filePipePath} 2>&1

# Clear the log
> ${logFile}

# Write the start time
echo "Script started at: $scriptStartTime"

# The first thing to do is to discard the last snapshot
# We also remove any other snapshots that might exist as a result of the number of snapshots to create being lowered
snapshotToRemove=${numberOfSnapshots}
# Make sure that we don't try and remove snapshot 0, but also that we always remove any extra snapshots
if [ ${numberOfSnapshots} -lt 1 ]
then
	snapshotToRemove=1
fi
while [ -e ${snapshotDirectoryPath}/${snapshotToRemove} ]
do
	echo "Removing snapshot ${snapshotToRemove}."
	rm -rf ${snapshotDirectoryPath}/${snapshotToRemove}
	snapshotToRemove=$(( ${snapshotToRemove} + 1 ))
done

# Now we can rotate the other snapshots
# We only need to perform the rotation if we're making more than one snapshot
if [ ${numberOfSnapshots} -gt 1 ]
then
	for i in `seq ${numberOfSnapshots} -1 2`
	do
		folderToMove=$(( ${i} - 1))
		folderDestination=${i}
		if [ -e ${snapshotDirectoryPath}/${folderToMove} ]
		then
			echo "Rotating snapshot: ${folderToMove} -> ${folderDestination}"
			mv ${snapshotDirectoryPath}/${folderToMove} ${snapshotDirectoryPath}/${folderDestination}
		fi
	done
fi

# Finally, if the number of snaphshots is more than 0 then we need to create a new snapshot
if [ ${numberOfSnapshots} -gt 0 ]
then
	# a flag preserves timestamps, owners etc. (Archive)
	# l flag copies files using hard links so that they use the same underlying file data
	# remove-destination removes the hard link between files if it's changed to prevent the overwrite changed the data in the previous backups
	echo "Creating new snapshot."
	cp -al --remove-destination ${continuousDirectoryPath} ${snapshotDirectoryPath}/1
fi

# Clean up
exec 1>&- 2>&-
wait ${teepid}
rm ${filePipePath}

# Email logs to specified address
if [ -z ${var+emailAddress} ] && [ -z ${var+emailSubject} ]
then
	cat ${logFile} | mail -s "${emailSubject}" ${emailAddress}
fi
