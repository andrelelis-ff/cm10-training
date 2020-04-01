require 'rubygems'
require 'chef/handler'
module Coremedia
  class DumpNode < Chef::Handler
    def report
      node_name = @run_status.node.name
      run_data = @run_status.node.to_hash
      File.open('/tmp/node.json', 'w+') do |f|
        f.puts Chef::JSONCompat.to_json_pretty(run_data)
      end
      ::FileUtils.copy_file('/tmp/node.json', "/shared/#{node_name}.json") if ::File.exist?('/shared')
    end
  end
end
