
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

## Availablity ##

Currently a SNAPSHOT version is available in the sonatype repo:

`https://oss.sonatype.org/content/repositories/snapshots/org/greencheek/memcached`


## Configuration ##

There's a few configuration (`init-param`) available for customisation of the filter.  Those that will most frequently
be used is that of setting the memcached hosts and the cache key used.

Other param control when caching takes place, how long an item is cached for, should the cache expiry times use
the `Cache-Control` `max-age` value; etc.


### Specifying the memcached hosts ###

The init parameter `memcached-hosts` is configurable with a comma separated list of the host and ports of the memcached
servers to talk to; and shard put/gets on.  An example is as follows:

         <init-param>
            <param-name>memcached-hosts</param-name>
            <param-value>127.0.0.1:11211,127.0.0.1:11212</param-value>
         </init-param>


### Specifying the cache key ###

One of the most important parts of caching, is the key against which to cache content.  If everything is cached
under the same key, then things are going to go pretty bad.

The default key is: `$scheme$request_method$uri$args?$header_accept?$header_accept-encoding_s?`  The means that the
following items make up the cache key:

- The scheme, i.e: http
- The request method, i.e: GET
- The request uri (This is the path), i.e: /context/servlet/restpath
- The request query parameters, optional, i.e: "includetext=no&pretty=false"
- The "Accept" header sent by the client, optional, i.e.: */*
- The "Accept-Encoding" header as sent by the client, optional and sorted, i.e: "gzip,deflate,sdch"

### Force Caching ###

By default the