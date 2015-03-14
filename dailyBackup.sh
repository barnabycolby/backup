#!/bin/bash

# Read in the variables from the config file
source /home/backup/backup/config

# The first thing to do is to discard the last backup
# We also remove any other backups that might exist as a result of the number of backups to create being lowered
backupFolderToRemove=${numberOfDailyBackups}
# Make sure that we don't try and remove folder 0, but also that we always remove any extra folders
if [ ${numberOfDailyBackups} -lt 1 ]
then
	backupFolderToRemove=1
fi
while [ -e ${dailiesDirectoryPath}/${backupFolderToRemove} ]
do
	rm -rf ${dailiesDirectoryPath}/${backupFolderToRemove}
	backupFolderToRemove=$(( ${backupFolderToRemove} + 1 ))
done

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

# Finally, if the number of daily backups is more than 0 then we need to create a new daily snapshot
if [ ${numberOfDailyBackups} -gt 0 ]
then
	# a flag preserves timestamps, owners etc. (Archive)
	# l flag copies files using hard links so that they use the same underlying file data
	# remove-destination removes the hard link between files if it's changed to prevent the overwrite changed the data in the previous backups
	cp -al --remove-destination ${continuousDirectoryPath} ${dailiesDirectoryPath}/1
fi
