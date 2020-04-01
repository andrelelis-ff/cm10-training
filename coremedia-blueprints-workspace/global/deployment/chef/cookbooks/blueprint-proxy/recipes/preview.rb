=begin
#<
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
#>
=end
include_recipe 'blueprint-proxy::_base'
template 'preview' do
  extend  Apache2::Cookbook::Helpers
  source 'vhosts/cae.erb'
  path "#{apache_dir}/sites-available/preview.conf"
  variables(
          application_name: 'preview',
          server_name: node['blueprint']['proxy']['virtual_host']['preview']['server_name'],
          server_aliases: node['blueprint']['proxy']['virtual_host']['preview']['server_aliases'].values,
          servlet_context: node['blueprint']['proxy']['virtual_host']['preview']['context'],
          live_servlet_context: node['blueprint']['proxy']['virtual_host']['preview']['live_servlet_context'],
          cluster: {'default' => { 'host' => node['blueprint']['proxy']['virtual_host']['preview']['host'], 'port' => node['blueprint']['proxy']['virtual_host']['preview']['port'] }},
          default_site: node['blueprint']['proxy']['virtual_host']['preview']['default_site'],
          rewrite_log_level: node['blueprint']['proxy']['virtual_host']['preview']['rewrite_log_level'],
          preview: true
  )
end

apache2_site 'preview'
