#Instructions for adding a new client to the backup server
- Add a new user to the backup machine with the username "backup"
- Create a samba share of the folder you want backed up
- Give the new backup user read permissions on the samba share
- Add an entry to fstab that mounts the share in a subdirectory, with the name of the user's unique identity, of the path specified by the backupShares variable
- The fstab entry must have the user option set as well as any other required options, such as workgroup, username and password
