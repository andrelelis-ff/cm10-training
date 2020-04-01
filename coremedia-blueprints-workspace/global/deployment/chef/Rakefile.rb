require 'rspec/core/rake_task'
require 'rubocop/rake_task'
require 'rubocop/formatter/base_formatter'
require 'rubocop/formatter/checkstyle_formatter'
require 'cookstyle'
require 'foodcritic'

is_jenkins = ENV.key?('JENKINS_HOME')

task :default => ['style']

desc 'Run all style checks'
task :style => ['style:chef', 'style:ruby']

desc 'Regenerate README.md'
task :doc do
  Rake::FileList['cookbooks/blueprint*'].each do |cookbook|
    puts "generating knife doc for #{cookbook}"
    sh "knife cookbook doc #{cookbook}"
  end
end

rubocop_cookbook_patterns = ['cookbooks/blueprint*/recipes/*.rb',
                             'cookbooks/blueprint*/resources/*.rb',
                             'cookbooks/blueprint*/providers/*.rb',
                             'cookbooks/blueprint*/libraries/*.rb',
                             'cookbooks/blueprint*/definitions/*.rb',
                             'cookbooks/blueprint*/metadata.rb'
]

namespace :style do
  desc 'Run Ruby style checks'
  RuboCop::RakeTask.new(:ruby) do |task|
    task.patterns = rubocop_cookbook_patterns
    # don't abort rake on failure
    task.fail_on_error = false
    task.formatters = ['progress', 'RuboCop::Formatter::CheckstyleFormatter']
    task.options = ['--display-cop-names', '--out', 'build/checkstyle.xml']
  end

  desc 'Run Chef style checks'
  FoodCritic::Rake::LintTask.new(:chef) do |t|
    t.options = {
            # we do not want to convert our definitions into LWRP FC015
            # FC019 also does not make sense, as we sometimes do want to set attributes using force_default
            :tags => %w(~FC019 ~FC015 ~FC064 ~FC065),
            :fail_tags => [is_jenkins ? 'none' : 'any'],
            :context => !is_jenkins,
            :progress => true,
            # foodcritic does not support json roles or environments
            :role_paths => Rake::FileList['roles/*.rb'],
            :environment_paths => Rake::FileList['environments/*.rb'],
            :cookbook_paths => Rake::FileList['cookbooks/blueprint*']
    }
  end
end

desc 'Repair Rubocop warnings'
RuboCop::RakeTask.new(:rubocop_repair) do |task|
  task.patterns = rubocop_cookbook_patterns
  # don't abort rake on failure
  task.fail_on_error = false
  task.options = ['-a']
end
