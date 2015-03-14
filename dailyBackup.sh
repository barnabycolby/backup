#!/bin/bash

# Read in the variables from the config file
source /home/backup/backup/config

# The first thing to do is to discard the last backup
rm -rf ${dailiesDirectoryPath}/4

# Now we can rotate the other daily snapshots
mv ${dailiesDirectoryPath}/3 ${dailiesDirectoryPath}/4
mv ${dailiesDirectoryPath}/2 ${dailiesDirectoryPath}/3
mv ${dailiesDirectoryPath}/1 ${dailiesDirectoryPath}/2

# Finally, we need to create the new daily snapshot
# a flag preserves timestamps, owners etc. (Archive)
# l flag copies files using hard links so that they use the same underlying file data
# remove-destination removes the hard link between files if it's changed to prevent the overwrite changed the data in the previous backups
cp -al --remove-destination ${continuousDirectoryPath} ${dailiesDirectoryPath}/1
