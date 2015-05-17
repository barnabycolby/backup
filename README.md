# Server Prerequisites
- JDK
- JRE
- Make
- Ability to send emails via mail command (SMTP Forwarder)
- Cron
- Samba client (smbclient)
- Rsync

# Client Prerequisites
- JDK
- JRE
- Make
- Samba

# Server Installation instructions
- Clone this git repository
- Create bin directories in the client and server folders
- Create a logs directory in the root of the repository
- Set configuration settings in all files within config directory
- Add crontab entries to run the daily and monthly backup scripts at the right times
- Make sure cronie systemd service is enabled
- Make a mount point under the configured backup shares folder for each client samba share
- Add an fstab entry for mounting each clients samba share, specifying the user option with a value of backup to allow the backup user to mount the shares without root privileges

#Instructions for adding a new client to the backup server
- Add a new user to the backup machine with the username "backup"
- Create a samba share of the folder you want backed up
- Give the new backup user read permissions on the samba share
- Add an entry to fstab that mounts the share in a subdirectory, with the name of the user's unique identity, of the path specified by the backupShares variable
- The fstab entry must have the user option set as well as any other required options, such as workgroup, username and password
