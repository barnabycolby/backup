#!/bin/bash

# Get the directory containing this script
scriptDir="${BASH_SOURCE%/*}"

# Pass monthly backup config file to the rotating backup script
sh ${scriptDir}/rotatingBackup.sh ${scriptDir}/../config/monthlyConfig
