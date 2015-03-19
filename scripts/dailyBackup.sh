#!/bin/bash

# Get the directory containing this script
scriptDir="${BASH_SOURCE%/*}"

# Pass daily backup config file to the rotating backup script
sh ${scriptDir}/rotatingBackup.sh ${scriptDir}/../config/dailyConfig
