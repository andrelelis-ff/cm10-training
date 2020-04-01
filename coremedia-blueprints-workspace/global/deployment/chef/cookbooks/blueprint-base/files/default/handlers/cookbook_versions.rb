require 'rubygems'
require 'chef/handler'
require 'chef/log'

module Coremedia
  class CookbookVersions < Chef::Handler
    def report
      cookbooks = run_context.cookbook_collection
      cookbooks_array = cookbooks.keys.map { |x| cookbooks[x].name.to_s + ' ' + cookbooks[x].version }
      print_out('-----------------------------------')
      print_out('Cookbook Versions being used: ')
      cookbooks_array.sort.each do |cookbook|
        print_out(" - #{cookbook}")
      end
      print_out('-----------------------------------')
    end

    # Bad workaround for https://github.com/test-kitchen/test-kitchen/issues/529
    def print_out(message)
      if Chef::Config[:file_cache_path].include?('kitchen')
        # I'm testing
        puts message
      else
        # I'm not testing
        Chef::Log.info message
      end
    end
  end
end
