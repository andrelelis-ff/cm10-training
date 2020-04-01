# yum-pgdg Cookbook CHANGELOG
This file is used to list changes made in each version of the yum-pgdg cookbook.

## 3.0.0 (2018-02-16)

- Require Chef 12.14+ and remove compat_resource dep
- Resolve ChefSpec warnings
- Use a SPDX compliant license string

## 2.0.1 (2016-12-22)

- Depend on the latest compat_resource cookbook
- Cookstyle fixes

## 2.0.0 (2016-11-26)
- Replace yum dependency with compat_resource
- Convert to inspec for testing

## 1.0.0 (2016-09-06)
- Testing updates
- Add chef_version metadata
- Remove support for Chef 11

## v0.2.7 (2015-12-01)
- Fixing if/unless logic in recipe

## v0.2.6 (2015-11-30)
- Fixed attributes with value of false being ignored

## v0.2.5 (2015-10-28)
- Fixing Chef 13 nil property deprecation warnings

## v0.2.4 (2015-09-21)
- Update yum dependency from ~3.0 to ~3.2
- Added Chef standard Rubocop file
- Updated platforms in Kitchen CI config
- Add supported platforms to the metadata
- Added Chef standard chefignore and .gitignore files
- Updated Berksfile to 3.X format
- Updated and expanded development dependencies in the Gemfile
- Added contributing, testing, and maintainers docs
- Added platform requirements to the readme
- Added Travis and cookbook version badges to the readme
- Added source_url and issues_url to the metadata

## v0.2.2 (2014-06-11)
- Fix typos in README

## v0.2.0 (2014-02-14)
- Updating test harness

## v0.1.4
Adding CHANGELOG.md

## v0.1.0
initial release
