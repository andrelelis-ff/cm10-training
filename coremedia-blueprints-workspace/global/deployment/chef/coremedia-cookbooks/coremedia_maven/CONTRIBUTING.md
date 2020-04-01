# How to Contribute

Pull requests are greatly appreciated and are what makes opensource great. Here's a quick guide:

* Fork it
* Create your feature branch (`git checkout -b my-new-feature`)
* Commit your changes (`git commit -am 'Add some feature'`)
* Push to the branch (`git push origin my-new-feature`)
* Create new Pull Request


# How to document

For documentation [knife-cookbook-doc](https://github.com/realityforge/knife-cookbook-doc) is being used.

# How to build

1. `bundle install`

2. `bundle exec rake <TASK>`, see `bundle exec rake -T` for available tasks

# How to release

1. edit `CHANGELOG.md` and `metadata.rb` and set version on both.

3. `bundle exec rake release`
