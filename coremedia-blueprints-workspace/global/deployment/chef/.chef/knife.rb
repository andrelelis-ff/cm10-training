# Knife Configuration File.
#
# This is a Ruby DSL to set configuration parameters for Knife's
# general options. The default location for this file is
# ~/.chef/knife.rb. If multiple Chef repositories are used,
# per-repository configuration files can be created. A per repository
# configuration file must be .chef/knife.rb in the base directory of
# the Chef repository. For example,
#
#     ~/Development/chef-repo/.chef/knife.rb
#
# In some cases to improve clarity default values are specified,
# uncommented. Other example values are commented out. To obtain a
# plain configuration file without these comments, use grep; e.g.:
#
#    egrep "^([a-z]|^#knife)" knife.rb
#    grep -E "^([a-z]|^#knife)" knife.rb
#
# As this file is interpreted Ruby, you can set local variables or use
# Ruby's string interpolation (`#{}`). Some examples will do this.
#
# For more information on configuring Knife, see the knife(1) man
# page, or the Chef Configuration Settings page on the Chef wiki.
#
# * http://wiki.opscode.com/display/chef/Knife
# * http://wiki.opscode.com/display/chef/Chef+Configuration+Settings
#
######################################################################
# In order to interact with a Chef Server, three configuration
# settings are *required*, and you must change them.
#
#    node_name
#    client_key
#    chef_server_url
#
# Identity to use for authenticating requests to the Chef
# Server. Corresponds to the -u or --user command-line option.
#node_name "username"
node_name ""

# Private key file to authenticate to the Chef Server. Corresponds to
# the -k or --key command-line option.
#client_key "#{File.dirname(__FILE__)}/username.pem"
client_key "/etc/chef/client.pem"

# URL of the Chef Server. Corresponses to the -s or --server-url
# command-line option.
#chef_server_url "https://api.opscode.com/organizations/ORG_SHORT_NAME"
chef_server_url "http://localhost:4000"

# End of required configuration for Chef Server interaction.
######################################################################
# Name of the API client used to validate new API clients. Used with
# knife bootstrap.
#validation_client_name "ORG_SHORT_NAME-validator"
validation_client_name "chef-validator"

# Private key file to authenticate when validating new API
# clients. Used with knife bootstrap.
#validation_key "#{File.dirname(__FILE__)}/ORG_SHORT_NAME-validator.pem"
validation_key  "/etc/chef/validation.pem"

######################################################################
# Location of the cookbooks directory. Typically in the Chef
# Repository, e.g. ${HOME}/chef-repo/cookbooks. Corresponds to
# the -o or --cookbook-path command-line option. Used with 'knife
# cookbook upload' and 'knife cookbook create'.
#
# Modification is not required but strongly recommended.
cookbook_path [ "#{File.dirname(__FILE__)}/../cookbooks" ]

######################################################################
# Knife specific configuration options. These are used by Core knife
# commands and Opscode's Knife Plugin Gems and correspond to options
# passed to the command. The default value is specified, modify and
# uncomment as required. Gem sections are ordered by name, below.
#
# Some configuration options overlap with different knife plugins. The
# option specified will be used as the default for all
# plugins. However, the command-line options will override the setting
# specified in this file.

# Core: knife ssh
#
#knife[:ssh_port] = 22

# Core: knife bootstrap
#
# Default bootstrap template by distribution name. This is
# also used in knife gems for bootstrapping cloud instances.
#knife[:distro] = "ubuntu10.04-gems"

# Default bootstrap template by full pathname to the template file.
# knife[:template_file] = ""

# Version of Chef to install during bootstrap.
#knife[:bootstrap_version] = ""

# HTTP proxy to use during bootstrap.
#knife[:bootstrap_proxy] = ""

######################################################################
# Gem: knife-ec2
#
# Authentication credentials for the AWS API. Specify the access key
# ID and secret access key.
#knife[:aws_access_key_id]     = ""
#knife[:aws_secret_access_key] = ""

# Default flavor of server (m1.small, c1.medium, etc).
#knife[:flavor] = "m1.small"

# Default AMI identifier, e.g. ami-12345678
#knife[:image] = ""

# AWS Region
#knife[:region] = "us-east-1"

# AWS Availability Zone. Must be in the same Region.
#knife[:availability_zone] = "us-east-1b"

# A file with EC2 User Data to provision the instance.
#knife[:aws_user_data] = ""

# AWS SSH Keypair.
#knife[:aws_ssh_key_id] = ""

######################################################################
# Gem: knife-windows
#
# User and password to authenticate to WinRM.
#knife[:winrm_user] = "Administrator"
#knife[:winrm_password] = ""

# WinRM port.
#knife[:winrm_port] = "5985"

# The WinRM transport type. 'ssl' or 'plaintext'.
#knife[:winrm_transport] = "plaintext"

# The Certificate Authority (CA) trust file used for 'ssl' transport.
#knife[:ca_trust_file]        = ""

# Authentication using Kerberos. Set the keytab, realm and service.
#knife[:kerberos_keytab_file] = ""
#knife[:kerberos_realm]       = ""
#knife[:kerberos_service]     = ""

# End of general configuration settings, knife core and gem
# configuration.
######################################################################
# Uncomment the following to support "conf.d" style configuration
# inclusion. Create the "conf.d" directory in the same location as
# this knife.rb, and add configuration files with the .rb file
# extension. The last filename in the glob wins, so be careful about
# overriding settings from above.
#confd = File.join(File.dirname(__FILE__), "conf.d", "*.rb")
#Dir.glob(confd).each {|file| Chef::Config.from_file(file)}