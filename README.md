
# Synopsis #

A Lazy man's servlet filter for caching responses in memcached.

What does that mean?  It's a Java Servlet Filter (or two) that stores the
response to a GET request in memcached.  When a subsequent request,
for the same content (i.e the same GET request) is made, the content is retrieved
and serviced from memcached; rather than from the `j2ee` application.


## Where can it be used ##

There is a Servlet 2.4+ and a Servlet 3 filter provided:

- Servlet3 Filter: `org.greencheek.web.filter.memcached.Servlet3PublishToMemcachedFilter`
- Servlet2 Filter: `org.greencheek.web.filter.memcached.PublishToMemcachedFilter`

The Servlet3 Filter can be used for both filtering synchronous and asynchronous servlets.
 request whereas, the Servlet2 filter can only be used for filtering traditional servlets.

Traditionally the application would be coded to make direct use of memcached as
a distributed `sharded` cache.  However, there are many applications out there,
 new and old, that aren't coded to make use of a distributed cache.  As a result it
 is sometimes hard to scale these applications to deal with a few more requests per second
that cached content would give you.

Adding the memcached filter could give you the extra caching layer on your web application
you are looking for.

If you don't have access to the webapps `web.xml`.  you can always choose to modify,
for example, the base application server's `web.xml`, i.e. tomcat's conf/web.xml,
and drop either the `shadewithslf4j` or `shadenoslf4j` in the tomcat lib/ directory.

## Simple Usage ##

The below starts the servlet filtering all requests into the application,
using the memcached hosts `127.0.0.1:11211` and `127.0.0.1:11212`

    <filter>
        <filter-name>writeToMemcached</filter-name>
        <filter-class>org.greencheek.web.filter.memcached.Servlet3PublishToMemcachedFilter</filter-class>
        <async-supported>true</async-supported>
        <init-param>
           <param-name>memcached-hosts</param-name>
           <param-value>127.0.0.1:11211,127.0.0.1:11212</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>writeToMemcached</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>