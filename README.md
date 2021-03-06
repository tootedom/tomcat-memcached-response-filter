
 - [Memcached Filter](#memcached-filter)
	- [Usage](#usage)
	- [Dependencies](#dependencies)
    - [Shaded Artifacts](#shaded-artifactss)
    - [Where can it be used](#where-can-it-be-used)
    - [Simple Usage](#simple-usage)
    - [Configuration](#configuration)
    - [Specifying the memcached hosts](#specifying-the-memcached-hosts)
    - [Host Checking](#host-checking)
    - [DNS Timeouts](#dns-timeouts)
    - [Memcached Protocol](#memcached-protocol)
    - [Specifying the cache key](#specifying-the-cache-key)
        - [Available Key Items](#available-key-items)
        - [Headers in Cache Key](#headers-in-cache-key)
        - [Cookies](#cookies)
        - [Cache Key Init Param](#cache-key-init-param)
        - [Cache Key Size](#cache-key-size)
        - [Cache Key](#cache-key)
    - [What is Cached?](#what-is-cached?)
    - [Caching Duration](#caching-duration)
    - [Cacheable HTTP Methods](#cacheable-http-methods)
    - [Cacheable HTTP status codes](#cacheable-http-status-codes)
    - [Cache Size](#cache-size)
    - [Caching POST or PUT](#caching-post-or-put)
        - [Request Body Size](#request-body-size)
    - [Memcached Get Timeout](#memcached-get-timeout)
    - [Disabling the Filter](#disabling-the-filter)
    - [Cache Hit Status Logging](#cache-hit-status-logging)
    - [Hystrix](#hystrix)
    - [Example Tomcat Setup](#example-tomcat-setup)


----

# Memcached Filter #

A Lazy man's servlet filter for caching responses in memcached.

What does that mean?  It's a Java Servlet Filter (or two) that stores the
response to a GET (or POST/PUT) request in memcached.  When a subsequent request,
for that same content (i.e the same GET request) is made, the content is retrieved
and serviced from memcached; rather than from the `j2ee` application.

One of the ideal use cases for this servlet filter is for a legacy Web Application, which needs to use caching,
to allow it to scale during periods of heavy load.  The Web Application could be being replaced
by a collection of small RESTful, stateless, scalable applications; and you need to add caching to the
existing application whilst the new services are brought into play.

The filter is a standard java servlet filter, and has been tested on tomcat 6 (6.0.39), with java 6u65.
It has also been tested on tomcat-7.0.53 and java 7u55

----

----

## Usage ##

The filter is available in maven central.
And you can put in the depedendency as follows:

    <dependency>
       <groupId>org.greencheek.memcached</groupId>
       <artifactId>caching-filter</artifactId>
       <version>0.0.12</version>
    </dependency>

----

## Dependencies ##

The filter uses a couple of libraries/dependencies, which are listed below.

- net.spy:spymemcached:jar:2.10.6:compile
- net.sf.trove4j:trove4j:jar:3.0.3:compile
- org.slf4j:slf4j-api:jar:1.7.7:compile
- net.jpountz.lz4:lz4:jar:1.2.0:compile

----

## Shaded Artifacts ##

There's a collection of shaded artifacts.  These artifacts package together the filter, and it's dependencies in one jar.
The shaded artifacts have been relocated to the `org.greencheek` package.  For example `gnu/trove/map/hash/TByteIntHashMap.class`
is now `org/greencheek/gnu/trove/map/hash/TByteIntHashMap.class`

The shaded artifacts are for use in legacy applications, or those applications in which you cannot modify the dependencies.
As a result you need a jar that combines all the dependencies that are required for running the filter.


- Logback

This shaded artifact compiles together all the dependencies, but also a the logging implementation `logback`

    <dependency>
       <groupId>org.greencheek.memcached</groupId>
       <artifactId>caching-filter</artifactId>
       <version>0.0.11</version>
       <classifier>shadewithlogback</classifier>
    </dependency>


- Hystrix

This shaded artifact compiles together all the dependencies, include `logback`, but also Hystrix.

    <dependency>
       <groupId>org.greencheek.memcached</groupId>
       <artifactId>caching-filter</artifactId>
       <version>0.0.11</version>
       <classifier>shadewithlogbackandhystrix</classifier>
    </dependency>


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

----

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

The library is available in maven central:

`http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22caching-filter%22`

----

## Configuration ##

There's several configuration (`init-param`)'s available for customisation of the filter.  The one filter parameter
that will most frequently be used, is that of setting the memcached hosts and the cache key used.

Other param control when caching takes place, how long an item is cached for, should the cache expiry times use
the `Cache-Control` `max-age` value; etc.

----

## Specifying the memcached hosts ##

The init parameter `memcached-hosts` is configurable with a comma separated list of the host and ports of the memcached
servers to talk to; and shard put/gets on.  An example is as follows:

    <init-param>
      <param-name>memcached-hosts</param-name>
      <param-value>127.0.0.1:11211,127.0.0.1:11212</param-value>
    </init-param>

----

## Host Checking ##

Given the comma separated list of memcached hosts, the filter can perform a preliminary check that the current server
is able to open a connection to the given memcached server.  If the server is not contactable, then that host is removed
from the list of hosts that are used as memcached nodes.

You may or may not want this functionality, therefore, by default it is disabled.  To enable:

    <init-param>
      <param-name>memcached-checkhost-connectivity</param-name>
      <param-value>true</param-value>
    </init-param>

----

## DNS Timeouts ##

The list of memcached hosts provided are resolved to DNS (`InetSocketAddress` objects) addresses.  The resolution of DNS
addresses for hostnames, have a default timeout of: 3 seconds.  To change this specify the following parameter, the
following sets the timeout to be 1 second:

    <init-param>
      <param-name>memcached-host-dnsresolutiontimeout-secs</param-name>
      <param-value>1</param-value>
    </init-param>

----

## Memcached Protocol ##

By default the filter uses the `TEXT` protocol.  It is recommended that you use the `BINARY` protocol, and that you
specify the following parameter, as the binary protocol is more efficient:

    <init-param>
      <param-name>memcached-use-binary-protocol</param-name>
      <param-value>true</param-value>
    </init-param>

----

## Specifying the cache key ##

One of the most important parts of caching, is the key against which to cache content.  If everything is cached
under the same key, then things are going to go pretty bad, and start acting quite odd on your web service/site.

The default cache key is as follows: `$scheme$request_method$uri$args?$header_accept?$header_accept-encoding_s?`
The means that the following items make up the cache key:

- The scheme, i.e: http
- The request method, i.e: GET
- The request uri (This is the path), i.e: /context/servlet/restpath
- The request query parameters, optional, i.e: "includetext=no&pretty=false"
- The "Accept" header sent by the client, optional, i.e.: */*
- The "Accept-Encoding" header as sent by the client, optional and sorted, i.e: "gzip,deflate,sdch"

----

### Available Key Items ###

The following table shows the cache key items that are available for combining into the key that a piece of content
is cached under.

| Item | Description | Can be Optional | Can be Sorted |
| ---- | ----------- | --------------- | ------------- |
| $scheme | This is `http` or `https`, i.e the addressing scheme  | No | No |
| $request_method | The HTTP verb (GET, POST, etc) | No | No |
| $uri | The request path (including context) of the url | No | No |
| $args | The query parameters | Yes | No |
| $content_type | The request `Content-Type` header | No | No |
| $request_uri | The $uri and $args in one, This can be optional; but only applies to the query params | Yes (for query parmas) | No |
| $cookie_jsessionid | The cookie named `jessionid` is used as part of the cache key | Yes | No |
| $header_accept | The header named `accept` is used as part of the cache key | Yes | Yes |
| $body | The post body.  This is only to be used for POST. (see later for more details) | No | No |
----

### Headers in Cache Key ###

Any request header can be used as part of the cache key.  For example, any header that is sent by the browser/client
can be used as part of the cache key:

- `Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8`
- `Accept-Encoding:gzip,deflate,sdch`
- `Accept-Language:en-US,en;q=0.8`
- `Cache-Control:max-age=0`
- `Connection:keep-alive`
- `Cookie:JSESSIONID=01726526F9DE0E21CC8ABB4BF448FE4B; JSESSIONID.925ef7e7=1jt9s7i77691ir5eup1m9eomo; JSESSIONID.6d5e3189=9m7m7fs6kvyrplpcq8jrjdtp; JSESSIONID.eb88b268=1u23rad575g42872hkb4sp3gi; screenResolution=2048x1280; JSESSIONID.551c1cd4=tt539v5ce2921pk3pvqf4vk1k`
- `Host:localhost:8080`
- `User-Agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36`

*Note* the cookie is a special header is chosen differently, as part of the cache key.  See later for more details.

To use the `Accept-Language` as part of the cache key, specify as part of the cache key `$header_accept-language`.
If `Accept-Language` is not represent as part of the request, then no caching will take place.  However, if you
want `Accept-Language` to be part of the cache key if it is available, and if it is not present then cache without it;
then you can specify the header to be optional, by adding `?` on the end of the header; as follows: `$header_accept-language?`

If you want the header's value to be sorted, then you can add `_s` on the header: `$header_accept-encoding_s?`
For example for the following `Accept-Encoding` header, you can use `_s` for the values to be sorted from `gzip,deflate,sdch` to
`deflate,gzip,sdch`

- `Accept-Encoding:gzip,deflate,sdch`

----

### Cookies ###

Cookies can be used as part of the cache key also.  An example use case would be the caching of content based on the
infamous `JSESSIONID` value.  In this case you could use `$cookie_jsessionid` to specific that content is to be cached
using the JSESSIONID in the http request.  In this scenario, where a new user comes to the site the following flow would
occur:

- No jsession id is in the request's cookies.  The content is generated, and sent to the user (It it not cached).
- User hits the site, and a jsessionid is present in the users request.  The content is generated by the backed (as no
 content exists in the cache yet).  The content is written to the cache using the jsession id cookie
- User hits the site, and is served cached content as the jsessionid is present in the user request's cookies, and is
present in the cache.

----

### Cache Key Init Param ###

The filter config parameter `memcached-key` is used to specify the cache key.  Below shows an example of optionally
using the jessionid cookie

    <init-param>
      <param-name>memcached-key</param-name>
      <param-value>$scheme$request_method$uri$args?$header_accept?$header_accept-encoding_s?$cookie_jsessionid?</param-value>
    </init-param>


----

### Cache Key Size ###

The cache key is made up of a number of different headers and request parameters, and can also (as will be seen later) use
the request body.  As a result a limit is put on the size of the cache key (as this is held in memory).  The byte buffer
associated with the cache key has an initial size, and can grow to a maximum.  The max size is '8192' bytes, and the
initial size is based on a calculation (for each $ cache key, 32 bytes is allowed).  Both this values are configurable
with the filter parameters

- Initial Size:  `memcached-estimated-cache-key-size` 
- Max Size:  `memcached-max-cache-key-size`

The following gives an example of configuring these:


    <init-param>
      <param-name>memcached-estimated-cache-key-size</param-name>
      <param-value>4196</param-value>
    </init-param>
    <init-param>
      <param-name>memcached-max-cache-key-size</param-name>
      <param-value>8192</param-value>
    </init-param>



----

## What is Cached? ##

In short, the response body and the HTTP response headers are cached.  However, there are slight restrictions to this
rather large sweeping statement.

The below specifies that the maximum response body can be 8k in size, or less, in order for it to be considered cacheable

    <init-param>
      <param-name>memcached-maxcacheable-bodysize</param-name>
      <param-value>8192</param-value>
    </init-param>

As the response body, from the back end system, is stored in jvm memory as a `byte[]` array; while it is sent to memcached;
there is a limit in the `max` size of the response that is considered cacheable.  The `byte[]` array used for caching
the backend response grows in size from an initial size to that of the `memcached-maxcacheable-bodysize`.  This is much
like the concept of a `StringBuilder` that has an initial `char[]`, that grows as you `append` strings and chars to it.
The initial size of the byte[] buffer can be specified with the following: `memcached-initialcacheable-bodysize`:

    <init-param>
      <param-name>memcached-initialcacheable-bodysize</param-name>
      <param-value>8192</param-value>
    </init-param>

The defaults are:

- `memcached-initialcacheable-bodysize`=4096
- `memcached-maxcacheable-bodysize`=16384


It is not only the response body that is stored in memcached; but the entire HTTP response from the backend; this includes
the HTTP headers set by the backend.  The headers that are not cached are:

- "connection"
- "keep-alive"
- "proxy-authenticate"
- "proxy-authorization"
- "te"
- "trailers"
- "transfer-encoding"
- "upgrade"
- "set-cookie"
- "date"

As the response headers take up space in the content to be stored in memcached, and also the internal intermediate `byte[]`
before it is stored in memcached.  You can specify an Initial estimated size of these combined backend http response headers,
and the maximum they should be as follows.

    <init-param>
      <param-name>memcached-response-estimated-header-size</param-name>
      <param-value>8192</param-value>
    </init-param>

    <init-param>
      <param-name>memcached-response-max-header-size</param-name>
      <param-value>8192</param-value>
    </init-param>

The defaults are:

- `memcached-response-estimated-header-size`=1024
- `memcached-response-max-header-size`=8192


----

## Caching Duration ##

By default the amount of time that an item will be cached in memcached, is that of the `max-age` parameter in the
the `Cache-Control` response header.  If no `Cache-Control` header is specified in the response, a default expiry is used of
`300` seconds (5 minutes).  This default expiry time can be specified by the init parameter `memcached-expiry`:

    <init-param>
      <param-name>memcached-expiry</param-name>
      <param-value>86400</param-value>
    </init-param>

If you do not which for a default to be applied, and only want responses to be cached when a `Cache-Control` max-age value
is specified, you can turn off the default:

    <init-param>
      <param-name>memcached-cache-nocachecontrol</param-name>
      <param-value>false</param-value>
    </init-param>

If the `Cache-Control` response header contains `no-cache`, `no-store` or `private`, then the result will not be cached.
If you don't mind the cache storing private in the shared cache, then you can allow `private` to be ignored when it is
in the `Cache-Control` header; via the following:

    <init-param>
      <param-name>memcached-cache-private</param-name>
      <param-value>true</param-value>
    </init-param>

If you `ALWAYS` want to cache content regardless of the value in the `Cache-Control` header then you can set the following
init parameter to `force` the response to be cached.

    <init-param>
      <param-name>memcached-force-cache</param-name>
      <param-value>true</param-value>
    </init-param>

The duration of the `forced` caching is: 300.  To change this specify the following parameter, and specify the number of
seconds to cache for:

    <init-param>
      <param-name>memcached-forced-expiry</param-name>
      <param-value>86400</param-value>
    </init-param>

----

## Cacheable HTTP Methods ##

By default the filters are enabled only for `GET` requests.  To allow more HTTP methods to be cacheable, for example to
allow for the caching of `GET` and `POST` requests, you can specify the following:

    <init-param>
      <param-name>memcached-cacheable-methods</param-name>
      <param-value>get,post</param-value>
    </init-param>

When specifying the caching of `POST` or `PUT`, you need to make sure 100% sure of the cache key; otherwise the POST or
PUT may not make it to your application.  See later for more details.

----

## Cacheable HTTP status codes ##

By default the following set of response codes are cacheable: `200, 203, 204, 205, 300, 301, 410`
If you wish other response codes to be cacheable then you can use the following filter config parameter.  For example to
allow only the caching of 200 and 404, the following would do it

    <init-param>
      <param-name>memcached-cacheable-methods</param-name>
      <param-value>200,404</param-value>
    </init-param>

----

## Cache Size ##

The filter will `NOT` cache everything.  It will only cache the response if the response body is below a limited size.
The reason for this is that the filter creates a temporary, in memory buffer.  This buffer is a copy of the response body
that is sent to the client.  As a result, if the filter was to cache all responses of any size, this could cause issues
for your application that may send large response bodie; such as running your jvm out of memory.

By default the cache will only cache response bodies up to `16384` bytes (16k).  The filter will not create an internal buffer of that size, but will instead use an initial size then grow in size up to the max cached size.  The default initial size is `4096`.

You can set the initial and max cacheable response bodies with the following two filter parameters:  `memcached-maxcacheable-bodysize` and `memcached-initialcacheable-bodysize`

Max response body (256k):

    <init-param>
      <param-name>memcached-maxcacheable-bodysize</param-name>
      <param-value>262144</param-value>
    </init-param>

Initial response body (16k)

    <init-param>
      <param-name>memcached-initialcacheable-bodysize</param-name>
      <param-value>16384</param-value>
    </init-param>

----

## Caching POST or PUT ##

If you need to cache `POST` or `PUT` request, for instance if an application is using `POST` data to send a large 'GET'
request to your application (i.e. an example would be sending queries to SOLR, which accepts POST requests); then you
will need to use the `POST` data as part of the key.  Otherwise the cache key is going to be same for all posts; and your
 clients will be returned incorrect data.

First you enable the caching of `POST` or `PUT`, (and `GET`), with the following:

    <init-param>
      <param-name>memcached-cacheable-methods</param-name>
      <param-value>get,post</param-value>
    </init-param>

In order to use the request body, you need to use `$body` as part of the cache key.

    <init-param>
      <param-name>memcached-key</param-name>
      <param-value>$scheme$request_method$uri$args?$header_accept?$header_accept-encoding_s?$body</param-value>
    </init-param>


With `$body` in place, for `POST` or `PUT` requests will use the `$body` as part of the key.  This will be ignored for
`GET` requests, which will use continue to use the rest of the key.

There is another piece of the puzzle to consider when using `$body`.  You need be be aware of how your application
consumes the request body.  *ONLY*, if your application consumes the request body via `getInputStream` or `getReader`
on the `HttpRequest`, should the `$body` be used.  If your application consumes the request body via `getParameter`
or `getParameterMap` then the use of `$body` will break your application's expectations.

In order for the `$body` to be useable the request **MUST** specify a `Content-Length` and not use chunking.  If
the `Content-Length` header is not available; then the PUT or POST will not be cacheable.


### Request Body Size ###

As part of the use of `$body`, the size of the request body (`Content-Length`) is important.  The filter is configured
to only cache PUT or POST requests with a limited request body size.  The reason for this is that one a `SerlvetRequest`'s
InputStream has been consumed it cannot be read again.

As an example, if we are to use the `$body` as the cache key; and that request hasn't been cached yet (i.e. a cache miss).
The request must go to the back end (i.e. your application).  However, if your application is expecting
the `InputStream` (the request body) to perform its function it is not going to be available.  It has already been read
for use a the cache key.

In order to solve this, the filter reads the `$body` and caches it in memory.  Therefore, a limit is put on the size
of the request body that is cacheable.  It order for the filter to know the size of the request body and not read the
body if it is too large, the `Content-Length` is required.

The max size of the POST or PUT body, by default is `8192` bytes (8k).  This is configurable with the filter parameter
`memcached-max-post-body-size`.  For example the following allows the max post body to be 16k:

    <init-param>
      <param-name>memcached-max-post-body-size</param-name>
      <param-value>16384</param-value>
    </init-param>

As the request `$body` is used as the cache key you need to specify/increase the size of the cache key the is allowed to
be created, via the filter parameter `memcached-max-cache-key-size`.  If you are combining the `$body` with that of other
header you will need to make the max cache key size (`memcached-max-cache-key-size`) greater than that of
the `memcached-max-post-body-size`

    <init-param>
      <param-name>memcached-estimated-cache-key-size</param-name>
      <param-value>8192</param-value>
    </init-param>
    <init-param>
      <param-name>memcached-max-cache-key-size</param-name>
      <param-value>20480</param-value>
    </init-param>


----

## Memcached Get Timeout ##

The request to fetch the content associated with a key from memcached is a blocking request.  By default the timeout in
millis for this get lookup to succeed is `1000` mills.  As 1s to obtain a result from memcached for a key can be
considered a long time.  This timeout is configurable.  The filter parameter is:  `memcached-get-timeout-millis`.

    <init-param>
      <param-name>memcached-get-timeout-millis</param-name>
      <param-value>500</param-value>
    </init-param>

----

## Disabling the Filter ##

The filter can be disabled with the following parameter.  By default the filter is enabled


    <init-param>
      <param-name>memcached-filter-enabled</param-name>
      <param-value>false</param-value>
    </init-param>


----

## Cache Hit Status Logging ##

The filter will emit cache hit status in a header named `X-Cache:` which will have either the value `HIT` or `MISS`.
The filter will also emit at `INFO` level the cached status, which will look as follows

    2014-05-25 20:31:16,586 [http-8080-17] INFO  o.greencheek.web.filter.memcached.util.CacheStatusLogger - {"cachestatus":"HIT","key":"-1163498384"}
    2014-05-25 20:31:16,794 [http-8080-17] INFO  o.greencheek.web.filter.memcached.util.CacheStatusLogger - {"cachestatus":"MISS","key":"-1163498384"}

----

## Hystrix ##


The library includes (as of version `0.0.11`), a Hystrix version of the Servlet2 filter.  The Hystrix filter is in no way
used to it's full potential.  I.e.  There is no primary and fallback options, there is no thread pooling, the hystrix filter
is mainly provided as a means to give you the ability to visualize the effectiveness of the cache in front of memcached.

The memcached GET, and the backend request to your application code, are wrapped in Hystrix Command objects.  By doing this,
you can add to the web.xml definition the Hystrix stream and obtain a visualisation of the effectiveness of the cache.

To use the Hystrix filter, you need the hystrix shaded artifact (http://search.maven.org/remotecontent?filepath=org/greencheek/memcached/caching-filter/0.0.11/caching-filter-0.0.11-shadewithlogbackandhystrix.jar):

    <dependency>
       <groupId>org.greencheek.memcached</groupId>
       <artifactId>caching-filter</artifactId>
       <version>0.0.11</version>
       <classifier>shadewithlogbackandhystrix</classifier>
    </dependency>


With the above jar in your tomcat `${catalina.home}/lib` folder, you can enable the Hystrix servlet that will provide you metrics,
as follows:

    <servlet>
        <description></description>
        <display-name>HystrixMetricsStreamServlet</display-name>
        <servlet-name>HystrixMetricsStreamServlet</servlet-name>
        <servlet-class>org.greencheek.com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet</servlet-class>
    </servlet>

    <servlet-mapping>
        <servlet-name>HystrixMetricsStreamServlet</servlet-name>
        <url-pattern>/hystrix.stream</url-pattern>
    </servlet-mapping>


And use the Hystrix filter, the Hystrix filter inherits all the properties mentioned previous, so you configure it exactly the same.

    <filter>
        <filter-name>writeToMemcached</filter-name>
        <filter-class>org.greencheek.web.filter.memcached.HystrixPublishToMemcachedFilter</filter-class>
        <init-param>
           <param-name>memcached-use-binary-protocol</param-name>
           <param-value>true</param-value>
		</init-param>
		<init-param>
		  <param-name>memcached-cacheable-methods</param-name>
		  <param-value>get,post</param-value>
		</init-param>
		<init-param>
		  <param-name>memcached-hystrix-cachelookup-batchingtime-millis</param-name>
		  <param-value>10</param-value>
		</init-param>
		<init-param>
		  <param-name>memcached-hystrix-cachelookup-batching-maxsize</param-name>
		  <param-value>100</param-value>
		</init-param>
		<init-param>
		  <param-name>memcached-key</param-name>
		  <param-value>$scheme$request_method$uri$args?$header_accept?$header_accept-encoding_s?$body</param-value>
		</init-param>
		<init-param>
		 <param-name>memcached-failure-mode</param-name>
		 <param-value>cancel</param-value>
		</init-param>
		<init-param>
		  <param-name>memcached-maxcacheable-bodysize</param-name>
		  <param-value>262144</param-value>
		</init-param>
		<!-- Hsytrix Specific properties below -->
		<init-param>
		  <param-name>memcached-hystrix-cachelookup-batching-enabled</param-name>
		  <param-value>false</param-value>
		</init-param>
    </filter>


The Hystrix filter `DOES NOT` currently support servlet 3, it has only been created for servlet 2 at the moment.

By default with the Hystrix filter in place (`org.greencheek.web.filter.memcached.HystrixPublishToMemcachedFilter`), it
will delay for 10ms cache lookups in order to batch them into a memcached MULTI_GET request.  By doing this, it will also
perform `de-duping` of requests.  So that requests for the same cache item are performed only once.  As this batching will
introduce an artifical bit of latency into your application, it can be turned off.  Also the batching, can make it difficult
to see the effectiveness of your cache (requests are batched into a single execution), rather than 1 request == 1 cache lookup.

To turn off request batching, use the following init parameter:

    <init-param>
      <param-name>memcached-hystrix-cachelookup-batching-enabled</param-name>
      <param-value>false</param-value>
    </init-param>

If you do not want to monitor the request to your application (`the back end`), you can disable that:

    <init-param>
      <param-name>memcached-hystrix-backend-tracking-enabled</param-name>
      <param-value>false</param-value>
    </init-param>

With the Hystrix filter enabled, and the Hystrix stream servlet in place, you can install a Hystrix dashboard
(https://github.com/Netflix/Hystrix/wiki/Dashboard), and monitor the filter in your webapp context. The below shows an
example sceenshot of the a dashboard.  This is taken from a system that is running SOLR as the backend; for which memcached
has been installed infront of it:

![Hystrix Dashboard](./exampledashboard.png)



----


## Example Tomcat Setup ##

The following gives an example of installing the shaded jar in a legacy web application. 

The below will provide an example of installing the filter within a tomcat 6 application, running on jdk6u45 or higher.

The installation will be such that the installation and setup of the filter is within the ${catalina.home}, as it is
assumed the legacy application cannot be modified to deploy the servlet as part of it.

The installation of the filter is a combination of the following steps:

- Configure an SLF4J property
- Deploy the shaded filter (contains slf4j and logback).
- Configure the web.xml
- Configure logback.xml

### Configure slf4j property ###

Add to `CATALINA_OPTS` the following system property: `-Dnet.spy.log.LoggerImpl=org.greencheek.net.spy.memcached.compat.log.SLF4JLogger`

This can be added to `bin/setenv.sh`:

    export CATALINA_OPTS="-Dnet.spy.log.LoggerImpl=org.greencheek.net.spy.memcached.compat.log.SLF4JLogger"

### Install the shaded filter jar ###

Download `caching-filter-0.0.6-shadewithlogback.jar` and deploy to `${catalina.home}/lib`

### Configure the web.xml ###

Configure the `web.xml` to enable the servlet:
edit `${catalina.home}/conf/web.xml`


    <filter>
        <filter-name>writeToMemcached</filter-name>
        <filter-class>org.greencheek.web.filter.memcached.PublishToMemcachedFilter</filter-class>
        <init-param>
            <param-name>memcached-use-binary-protocol</param-name>
            <param-value>true</param-value>
        </init-param>
        <init-param>
            <param-name>memcached-cacheable-methods</param-name>
            <param-value>get,post</param-value>
        </init-param>
        <init-param>
            <param-name>memcached-key</param-name>
            <param-value>$scheme$request_method$uri$args?$header_accept?$header_accept-encoding_s?$body</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>writeToMemcached</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

### Configure logback.xml ###

Create `${catalina.home}/lib/logback.xml`, and insert the following contents.  The content of this `logback.xml`
is entirely up to you.  The below gives a guide on an example configuration that:

- Uses Sync logging
- Uses day based logging (rotates daily)
- Uses size based logging so that only max 3 files exist, 100MB each.

What you will end up with is something similar to the following in your `$catalina.base/logs` directory:


logs/memcachedfilter.log
logs/memcachedfilter-2014-05-16.0.log


The configuration is as follows:

    <configuration scan="true" scanPeriod="120 seconds" >
        <contextListener class="org.greencheek.ch.qos.logback.classic.jul.LevelChangePropagator">
            <resetJUL>true</resetJUL>
        </contextListener>
        <appender name="LOGFILE" class="org.greencheek.ch.qos.logback.core.rolling.RollingFileAppender">
            <file>${catalina.base}/logs/memcachedfilter.log</file>
            <rollingPolicy class="org.greencheek.ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <!-- rollover daily -->
                <fileNamePattern>${catalina.base}/logs/memcachedfilter-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <maxHistory>2</maxHistory>
                <timeBasedFileNamingAndTriggeringPolicy
                    class="org.greencheek.ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                    <!-- or whenever the file size reaches 100MB -->
                    <maxFileSize>100MB</maxFileSize>
                </timeBasedFileNamingAndTriggeringPolicy>
            </rollingPolicy>
            <encoder>
                <pattern>%date{ISO8601} [%thread] %-5level %logger{56} - %msg%n</pattern>
            </encoder>
        </appender>
        <appender name="ASYNC" class="org.greencheek.ch.qos.logback.classic.AsyncAppender">
            <queueSize>2048</queueSize>
            <appender-ref ref="LOGFILE" />
        </appender>

        <logger name="org.greencheek.net.spy" level="WARN"/>
        <logger name="org.greencheek.web.filter.memcached" level="WARN"/>
        <logger name="org.greencheek.web.filter.memcached.util.CacheStatusLogger" level="INFO"/>

        <root level="ERROR">
            <appender-ref ref="ASYNC" />
        </root>
    </configuration>
