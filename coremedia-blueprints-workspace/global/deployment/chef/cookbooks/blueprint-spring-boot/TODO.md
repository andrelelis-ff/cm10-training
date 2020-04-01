OPEN ISSUES
===========


CAE Live scaling
----------------
The old tomcat cookbook allowed to scale using an integer attribute. The proxy cookbook then automatically 
included the additional instances within the loadbalancer group.

In addition there was the feature to define properties in a _base_ hash which was then merged as defaults before scaling
instance specific properties overwrite them. 


LDAP / CROWD
------------

There is currently no facade in the chef setup to configure this on the content management server


Sitemap creation and proxying
-----------------------------

question: where to set sitemap enabled `blueprint/apps/cae-live` or `blueprint/spring-boot/cae-live`


Startup wait checks
-------------------

```
property :post_start_wait_url, kind_of: String, default: '', callbacks: {
        'should be a valid URL' => lambda {
                |url| url =~ URI::regexp
        }
}
#<> @attribute post_start_wait_code the HTTP return code to wait for
property :post_start_wait_code, kind_of: Integer, default: 200
#<> @attribute post_start_wait_timeout the timeout to wait for the url check to succeed
property :post_start_wait_timeout, kind_of: Integer, default: 600
```
The above should be converted into an array of hashes with three keys with defaults if 
possible. THis way it would be possible to check for health first and then for 
warmup/readiness.
