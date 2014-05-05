
# Synopsis #

A Lazy man's servlet filter for caching responses in memcached.

What does that mean?  It's a Java Servlet Filter (or two) that stores the
response to a GET request in memcached.  When a subsequent request,
for the same content (i.e the same GET request) is made, the content is retrieved
and serviced from memcached; rather than from the `j2ee` application.

----

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

Currently a SNAPSHOT version is available in the sonatype repo:

`https://oss.sonatype.org/content/repositories/snapshots/org/greencheek/memcached`

----

## Configuration ##

There's a few configuration (`init-param`) available for customisation of the filter.  Those that will most frequently
be used is that of setting the memcached hosts and the cache key used.

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

## What is Cached? ##

In short, the response body and the HTTP response headers are cached.  However, there are slight restrictions to this
rather large sweeping statement.

The below specifies that the response body can only be 8k in size, or less, in order for it to be considered cacheable

    <init-param>
      <param-name>memcached-maxcacheable-bodysize</param-name>
      <param-value>8192</param-value>
    </init-param>


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