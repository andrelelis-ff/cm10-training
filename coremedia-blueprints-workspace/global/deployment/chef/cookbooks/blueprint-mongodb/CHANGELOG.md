blueprint-mongodb Cookbook CHANGELOG
======================
This file is used to list changes made in each version of the blueprint-mongodb cookbook.

1.0.5
-----
- use upstream Cookbook and reduce our own cluecode
  explicit execution of disable-transparent-hugepages
  enhance spec tests
  
1.0.4
-----
- update to upstream Cookbook 5.3.0
  fix timing issues and rewind more orginial resources
  use disable-transparent-hugepages as seperate service and own recipe class

1.0.3
-----
- add 'mongodb-systemctl-daemon-reload'

1.0.2
-----
- fixes errors for overwrite yum_repository resources

1.0.1
-----
- fixes for mirrorlist
- change repository name

1.0.0
--------
- initial release
