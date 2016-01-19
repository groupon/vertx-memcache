/**
 * Copyright 2014 Groupon.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.groupon.vertx.memcache;

/**
 * This contains the common keys for Memcache.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public interface MemcacheKeys {
    String MEMCACHE_CLUSTER_KEY = "memcacheClusterConfig";
    String CLUSTERS_KEY = "clusters";
    String MEMCACHE_KEY = "memcacheConfig";
    String SERVERS_KEY = "servers";
    String EVENT_BUS_ADDRESS_KEY = "eventBusAddress";
    String EVENT_BUS_ADDRESS_PREFIX_KEY = "eventBusAddressPrefix";
    String NAMESPACE_KEY = "namespace";
    String POINTS_PER_SERVER = "pointsPerServer";
    String RETRY_INTERVAL = "retryInterval";
    String ALGORITHM_KEY = "algorithm";
    String CONTINUUM_KEY = "continuum";
}
