# Description

This is an application cookbook, it provides recipes to set up a webserver (apache httpd) for the CoreMedia Blueprint stack.
# Requirements


## Chef Client:

* chef (>= 12.5) ()

## Platform:

*No platforms defined*

## Cookbooks:

* blueprint-base
* blueprint-spring-boot
* apache2 (~> 7.1.0)

# Attributes

* `node['blueprint']['proxy']['virtual_host']['studio']['host']` -  Defaults to `localhost`.
* `node['blueprint']['proxy']['virtual_host']['studio']['port']` -  Defaults to `41080`.
* `node['blueprint']['proxy']['virtual_host']['studio']['server_name']` -  Defaults to `studio.#{node['blueprint']['hostname']}`.
* `node['blueprint']['proxy']['virtual_host']['studio']['rewrite_log_level']` -  Defaults to `trace1`.
* `node['blueprint']['proxy']['virtual_host']['studio']['server_aliases']` -  Defaults to `[ ... ]`.
* `node['blueprint']['proxy']['virtual_host']['studio']['server_cluster']` -  Defaults to `{ ... }`.
* `node['blueprint']['proxy']['virtual_host']['studio']['client_cluster']` -  Defaults to `{ ... }`.
* `node['blueprint']['proxy']['virtual_host']['preview']['host']` -  Defaults to `localhost`.
* `node['blueprint']['proxy']['virtual_host']['preview']['port']` -  Defaults to `40980`.
* `node['blueprint']['proxy']['virtual_host']['preview']['context']` -  Defaults to `blueprint`.
* `node['blueprint']['proxy']['virtual_host']['preview']['rewrite_log_level']` -  Defaults to `trace1`.
* `node['blueprint']['proxy']['virtual_host']['preview']['server_name']` -  Defaults to `preview.#{node['blueprint']['hostname']}`.
* `node['blueprint']['proxy']['virtual_host']['preview']['server_aliases']` -  Defaults to `{ ... }`.
* `node['blueprint']['proxy']['virtual_host']['preview']['live_servlet_context']` -  Defaults to `blueprint`.
* `node['blueprint']['proxy']['virtual_host']['preview']['default_site']` -  Defaults to `corporate`.
* `node['blueprint']['proxy']['virtual_host']['headless-server-live']['host']` -  Defaults to `localhost`.
* `node['blueprint']['proxy']['virtual_host']['headless-server-live']['port']` -  Defaults to `41280`.
* `node['blueprint']['proxy']['virtual_host']['headless-server-live']['server_name']` -  Defaults to `headless-server-live.#{node['blueprint']['hostname']}`.
* `node['blueprint']['proxy']['virtual_host']['headless-server-live']['rewrite_log_level']` -  Defaults to `trace1`.
* `node['blueprint']['proxy']['virtual_host']['headless-server-preview']['host']` -  Defaults to `localhost`.
* `node['blueprint']['proxy']['virtual_host']['headless-server-preview']['port']` -  Defaults to `41180`.
* `node['blueprint']['proxy']['virtual_host']['headless-server-preview']['server_name']` -  Defaults to `headless-server-preview.#{node['blueprint']['hostname']}`.
* `node['blueprint']['proxy']['virtual_host']['headless-server-preview']['rewrite_log_level']` -  Defaults to `trace1`.
* `node['blueprint']['proxy']['default_mod_config']['deflate']` -  Defaults to `true`.
* `node['blueprint']['proxy']['default_mod_config']['expires']` -  Defaults to `true`.
* `node['blueprint']['proxy']['default_mod_config']['headers']` -  Defaults to `true`.
* `node['blueprint']['proxy']['default_mod_config']['mime']` -  Defaults to `true`.
* `node['blueprint']['proxy']['default_mod_config']['rewrite']` -  Defaults to `true`.
* `node['blueprint']['proxy']['default_mod_config']['cors']` -  Defaults to `true`.
* `node['blueprint']['proxy']['overview_template']['cookbook']` - The cookbook from which to load the test system overview template. Defaults to `blueprint-proxy`.
* `node['blueprint']['proxy']['overview_template']['source']` - The source parameter of the overview template resource. Defaults to `overview/overview.html.erb`.

# Recipes

* [blueprint-proxy::default](#blueprint-proxydefault) - This recipe wraps all recipes of this cookbook for apache running in front of tomcat.
* [blueprint-proxy::delivery](#blueprint-proxydelivery) - This recipe installs virtual hosts for the CoreMedia Live CAE.
* [blueprint-proxy::headless-server-live](#blueprint-proxyheadless-server-live) - This recipe installs virtual hosts for the CoreMedia headless-server-live.
* [blueprint-proxy::headless-server-preview](#blueprint-proxyheadless-server-preview) - This recipe installs virtual hosts for the CoreMedia headless-server-preview.
* [blueprint-proxy::preview](#blueprint-proxypreview) - This recipe installs virtual hosts for the CoreMedia Preview CAE.
* [blueprint-proxy::studio](#blueprint-proxystudio) - This recipe installs virtual hosts for the CoreMedia Studio.

## blueprint-proxy::default

This recipe wraps all recipes of this cookbook for apache running in front of tomcat. To work with websphere add websphere recipe before this recipe.

## blueprint-proxy::delivery

This recipe installs virtual hosts for the CoreMedia Live CAE.

The recipe accepts an attribute hash of the following form:

```
"blueprint": {
  "proxy": {
    "virtual_host": {
      "delivery": {
        "rewrite_log_level": "trace1",
        "context": "blueprint",
        "cluster": {
          "blue-1": {
            "host": "blue.blueprint.com",
            "port": "42180"
          },
          "blue-2": {
            "host": "blue.blueprint.com",
            "port": "42280"
          },
          "green-1": {
            "host": "green.blueprint.com",
            "port": "42180"
          },
          "green-2": {
            "host": "green.blueprint.com",
            "port": "42280"
          }

        },
        "sites": {
          "corporate": {
            "server_name": "corporate.blueprint.com",
            "server_aliases": ["blueprint.com"],
            "default_site": "corporate"
          }
        }
      }
    }
  }
}
```

In the example above, a virtual host `corporate.blueprint.com` is being created backed by a cluster of 4 CAEs running on
two hosts.


## blueprint-proxy::headless-server-live

This recipe installs virtual hosts for the CoreMedia headless-server-live.

## blueprint-proxy::headless-server-preview

This recipe installs virtual hosts for the CoreMedia headless-server-preview.

## blueprint-proxy::preview

This recipe installs virtual hosts for the CoreMedia Preview CAE.

The recipe accepts an attribute hash of the following form:

```
"blueprint": {
  "proxy": {
    "virtual_host": {
      "preview": {
        "rewrite_log_level": "trace1",
        "context": "blueprint",
        "default_site": "asite"
        "host": "blue.blueprint.com",
        "port": "40980"
        "server_name": "preview.blueprint.com",
        "server_aliases": {
          "asite": "preview-asite.blueprint.com"
        }
      }
    }
  }
}
```

In the example above, the preview virtual host `preview.blueprint.com` is being created. It will have the alias
`preview-asite.blueprint.com`.

## blueprint-proxy::studio

This recipe installs virtual hosts for the CoreMedia Studio.

# Author

Author:: Your Name (<your_name@domain.com>)
