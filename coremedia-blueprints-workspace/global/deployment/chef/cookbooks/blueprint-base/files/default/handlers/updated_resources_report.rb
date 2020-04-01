#
# Copyright:: 2011, Joshua Timberman <chefcode@housepub.org>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

require 'chef/handler'

module CoreMedia
  class UpdatedResources < Chef::Handler
    def report
      print_out 'Resources updated this run:'
      run_status.updated_resources.each { |r| print_out " - #{r}" }
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
