# Copyright (c) 2012 Joe Miller
#
# Permission is hereby granted, free of charge, to any person obtaining
# a copy of this software and associated documentation files (the
# "Software"), to deal in the Software without restriction, including
# without limitation the rights to use, copy, modify, merge, publish,
# distribute, sublicense, and/or sell copies of the Software, and to
# permit persons to whom the Software is furnished to do so, subject to
# the following conditions:

# The above copyright notice and this permission notice shall be
# included in all copies or substantial portions of the Software.
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
# NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
# LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
# OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
# WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

require 'rubygems'
require 'chef/handler'
module CoreMedia
  class ElapsedTimeReport < Chef::Handler
    THRESHOLD = 5
    def report
      recipes = Hash.new(0)
      cookbooks = Hash.new(0)
      resources = Hash.new(0)
      resource_types = Hash.new(0)
      total = 0
      all_resources.each do |resource|
        cookbooks[resource.cookbook_name] += resource.elapsed_time
        recipes["#{resource.cookbook_name}::#{resource.recipe_name}"] += resource.elapsed_time
        resources["#{resource.resource_name}[#{resource.name}]"] = resource.elapsed_time
        resource_types[resource.resource_name] += resource.elapsed_time
        total += resource.elapsed_time
      end
      print_out 'Elapsed time total'
      print_out '------------  -------------'
      print_out format('%12f %s', data[:elapsed_time], 'Total run')
      print_out format('%12f %s', total, 'Total spent in resources')
      print_out format('%12f %s', data[:elapsed_time] - total, 'Total spend in retrieving/compilation')
      print_out ''
      print_out "Elapsed time by cookbook >= #{THRESHOLD} sec"
      print_out '------------ -------------'
      cookbooks.sort_by { |_k, v| -v }.each do |cookbook, run_time|
        print_out format('%12f %s', run_time, cookbook) if run_time >= THRESHOLD
      end
      print_out ''
      print_out "Elapsed time by recipe >= #{THRESHOLD} sec"
      print_out '------------ -------------'
      recipes.sort_by { |_k, v| -v }.each do |recipe, run_time|
        print_out format('%12f %s', run_time, recipe) if run_time >= THRESHOLD
      end
      print_out ''
      print_out "Elapsed time by resource >= #{THRESHOLD} sec"
      print_out '------------ -------------'
      resources.sort_by { |_k, v| -v }.each do |resource, run_time|
        print_out format('%12f %s', run_time, resource) if run_time >= THRESHOLD
      end
      print_out ''
      print_out "Elapsed time by resource type >= #{THRESHOLD} sec"
      print_out '------------ -------------'
      resource_types.sort_by { |_k, v| -v }.each do |resource_type, run_time|
        print_out format('%12f %s', run_time, resource_type) if run_time >= THRESHOLD
      end
      print_out ''
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
