#!/bin/bash

# Check that we've been passed a config file and that it's exists
if [ $# -ne 1 ] || [ ! -f $1 ]
then
	echo "The first argument of this script must be an existing config file."
	exit 1
fi

# Read in the variables from the default config file for all backups
source ~/backup/defaultConfig

# Read in the variables from the config file that was passed in as an argument
source $1

# The first thing to do is to discard the last snapshot
# We also remove any other snapshots that might exist as a result of the number of snapshots to create being lowered
snapshotFolderToRemove=${numberOfSnapshots}
# Make sure that we don't try and remove snapshot 0, but also that we always remove any extra snapshots
if [ ${numberOfSnapshots} -lt 1 ]
then
	snapshotFolderToRemove=1
fi
while [ -e ${snapshotDirectoryPath}/${snapshotFolderToRemove} ]
do
	rm -rf ${snapshotDirectoryPath}/${snapshotFolderToRemove}
	snapshotFolderToRemove=$(( ${snapshotFolderToRemove} + 1 ))
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
	cp -al --remove-destination ${continuousDirectoryPath} ${snapshotDirectoryPath}/1
fi
