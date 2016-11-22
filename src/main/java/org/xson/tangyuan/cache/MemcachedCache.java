package org.xson.tangyuan.cache;

import java.util.Date;
import java.util.Map;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.util.PropertyUtils;

import com.whalin.MemCached.MemCachedClient;
import com.whalin.MemCached.SockIOPool;

public class MemcachedCache extends AbstractCache {

	private MemCachedClient	cachedClient	= null;
	private SockIOPool		pool			= null;

	@Override
	public void start(String resource, Map<String, String> properties) {
		if (null != cachedClient || null != pool) {
			return;
		}

		// { "cache0.server.com:12345", "cache1.server.com:12345" };
		String _serverlist = properties.get("serverlist");
		if (null == _serverlist) {
			throw new TangYuanException("Memcached miss property 'serverlist'");
		}
		String[] serverlist = _serverlist.split(",");

		// { new Integer(5), new Integer(2) };
		String _weights = properties.get("weights");
		if (null == _weights) {
			throw new TangYuanException("Memcached miss property 'weights'");
		}
		String[] array = _weights.split(",");
		Integer[] weights = new Integer[array.length];
		for (int i = 0; i < array.length; i++) {
			weights[i] = Integer.parseInt(array[i]);
		}

		int initialConnections = PropertyUtils.getIntValue(properties, "initialConnections", 10);
		int minSpareConnections = PropertyUtils.getIntValue(properties, "minSpareConnections", 5);
		int maxSpareConnections = PropertyUtils.getIntValue(properties, "maxSpareConnections", 50);
		// 30 minutes
		int maxIdleTime = PropertyUtils.getIntValue(properties, "maxIdleTime", 1000 * 60 * 30);
		// 5 minutes
		long maxBusyTime = PropertyUtils.getLongValue(properties, "maxIdleTime", 1000 * 60 * 5);
		// 5 seconds
		long maintThreadSleep = PropertyUtils.getLongValue(properties, "maxIdleTime", 1000 * 5);
		// 3 seconds to block on reads
		int socketTimeOut = PropertyUtils.getIntValue(properties, "socketTimeOut", 1000 * 60 * 1000 * 3);
		// 3 seconds to block on initial connections. If 0, then will use
		// blocking connect (default)
		int socketConnectTO = PropertyUtils.getIntValue(properties, "socketConnectTO", 1000 * 60 * 1000 * 3);
		// turn off Nagle's algorithm on all sockets in pool
		boolean failover = PropertyUtils.getBooleanValue(properties, "failover", false);
		boolean failback = PropertyUtils.getBooleanValue(properties, "failback", false);
		// turn off Nagle's algorithm on all sockets in pool
		boolean nagleAlg = PropertyUtils.getBooleanValue(properties, "nagleAlg", false);
		// disable health check of socket on checkout
		boolean aliveCheck = PropertyUtils.getBooleanValue(properties, "aliveCheck", false);

		SockIOPool pool = SockIOPool.getInstance();
		pool.setServers(serverlist);
		pool.setWeights(weights);
		pool.setInitConn(initialConnections);
		pool.setMinConn(minSpareConnections);
		pool.setMaxConn(maxSpareConnections);
		pool.setMaxIdle(maxIdleTime);
		pool.setMaxBusyTime(maxBusyTime);
		pool.setMaintSleep(maintThreadSleep);
		pool.setSocketTO(socketTimeOut);
		pool.setSocketConnectTO(socketConnectTO);
		pool.setFailover(failover);
		pool.setFailback(failback);
		pool.setNagle(nagleAlg);
		pool.setHashingAlg(SockIOPool.NEW_COMPAT_HASH);
		pool.setAliveCheck(aliveCheck);
		pool.initialize();

		cachedClient = new MemCachedClient();
		if (properties.containsKey("defaultEncoding".toUpperCase())) {
			cachedClient.setDefaultEncoding(properties.get("defaultEncoding".toUpperCase()).trim());
		}
		if (properties.containsKey("primitiveAsString".toUpperCase())) {
			cachedClient.setPrimitiveAsString(Boolean.parseBoolean(properties.get("primitiveAsString".toUpperCase()).trim()));
		}
		if (properties.containsKey("sanitizeKeys".toUpperCase())) {
			cachedClient.setSanitizeKeys(Boolean.parseBoolean(properties.get("sanitizeKeys".toUpperCase()).trim()));
		}
	}

	@Override
	public void stop() {
		if (null != pool) {
			pool.shutDown();
		}
	}

	@Override
	public void putObject(Object key, Object value, Integer time) {
		Date expiry = null;
		if (null == time) {
			cachedClient.set(parseKey(key), value);
		} else {
			expiry = new Date(time.intValue() * 1000L);
			cachedClient.set(parseKey(key), value, expiry);
		}
	}

	@Override
	public Object getObject(Object key) {
		return cachedClient.get(parseKey(key));
	}

	@Override
	public Object removeObject(Object key) {
		Object result = getObject(key);
		if (null != result) {
			cachedClient.delete(parseKey(key));
		}
		return result;
	}

	@Override
	public void clear() {
		cachedClient.flushAll();
	}

	// private String parseKey(Object key) {
	// if (key instanceof String) {
	// return (String) key;
	// }
	// throw new CacheException("Illegal cache key: " + key);
	// }

}
