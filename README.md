vert.x-memcache
===============

<a href="https://raw.githubusercontent.com/groupon/vertx-memcache/master/LICENSE">
    <img src="https://img.shields.io/hexpm/l/plug.svg"
         alt="License: Apache 2">
</a>
<a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.groupon.vertx%22%20a%3A%22vertx-memcache%22">
    <img src="https://img.shields.io/maven-central/v/com.groupon.vertx/vertx-memcache.svg"
         alt="Maven Artifact">
</a>

This is an async library for sending commands to Memcache.

Usage
-----

Configuration for Memcache Verticle:

    {
        "memcacheClusterConfig": {
            "eventBusAddressPrefix": "address_where_memcache_handler_is_registered",
            "clusters": {
                "cluster_name_here": {
                    "servers": ["hostname:11211"],
                    "namespace": "memcache-namespace"
                }
            }
        }
    }

Setting up a client and calling a simple get:

    MemcacheClusterConfig memcacheConfig = new MemcacheClusterConfig(container.config().getObject("memcacheClusterConfig"));
    MemcacheClusterClientFactory clientFactory = new MemcacheClusterClientFactory(vertx.eventBus(), memcacheConfig);
    MemcacheClient memcacheClient = clientFactory.getClient("cluster_name_here");
    Future<JsonObject> result = memcacheClient.get("some-key");

The JsonObject in the future result will be in a [Jsend](https://labs.omniti.com/labs/jsend) format.  In the case of the get call above it will be similar to:

    {
        "status": "success",
        "data": {
            "some-key": "value"
        }
    }

Results in the data block will vary based on the Memcache method being called.

Building
--------

Prerequisites:
* [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Maven 3.3.3+](http://maven.apache.org/download.cgi)
* [Memcached](http://memcached.org/)

Building:

    vertx-memcache> mvn verify

To use the local version you must first install it locally:

    vertx-memcache> mvn install

You can determine the version of the local build from the pom file.  Using the local version is intended only for testing or development.


License
-------

Published under Apache Software License 2.0, see LICENSE

&copy; Groupon Inc., 2014
