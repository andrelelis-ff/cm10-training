# Description

This is the environment cookbook for your chef deployment. Within this cookbook you have one recipe for each environment, where
you set your environment specific attributes only. Do not manage any resources with it.


# Requirements


## Chef Client:

* chef (>= 12.5) ()

## Platform:

*No platforms defined*

## Cookbooks:

*No dependencies defined*

# Attributes

*No attributes defined*

# Recipes

* [blueprint::default](#blueprintdefault) - This recipe includes the recipe named like the environment.

## blueprint::default

This recipe includes the recipe named like the environment. Never include an environment recipe directly.

# Author

Author:: Your Name (<your_name@domain.com>)
