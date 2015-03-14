#!/bin/bash

# Read in the variables from the config file
source /home/backup/backup/config

# The first thing to do is to discard the last backup
rm -rf ${dailiesDirectoryPath}/${numberOfDailyBackups}

# Now we can rotate the other daily snapshots
# We only need to perform the rotation if we're making more than one snapshot
if [ ${numberOfDailyBackups} -gt 1 ]
then
	for i in `seq ${numberOfDailyBackups} -1 2`
	do
		folderToMove=$(( ${i} - 1))
		folderDestination=${i}
		mv ${dailiesDirectoryPath}/${folderToMove} ${dailiesDirectoryPath}/${folderDestination}
	done
fi

# Finally, we need to create the new daily snapshot
# a flag preserves timestamps, owners etc. (Archive)
# l flag copies files using hard links so that they use the same underlying file data
# remove-destination removes the hard link between files if it's changed to prevent the overwrite changed the data in the previous backups
cp -al --remove-destination ${continuousDirectoryPath} ${dailiesDirectoryPath}/1
