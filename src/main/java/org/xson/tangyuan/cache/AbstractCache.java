package org.xson.tangyuan.cache;

import java.util.Map;

import org.xson.tangyuan.TangYuanException;

public abstract class AbstractCache implements ICache {

	// @Override
	// public void start(Map<String, String> properties) {
	// throw new TangYuanException("This method subclass must implement");
	// }
	// @Override
	// public void start(String resource) {
	// throw new TangYuanException("This method subclass must implement");
	// }

	@Override
	public void start(String resource, Map<String, String> properties) {
		throw new TangYuanException("This method subclass must implement");
	}

	@Override
	public void stop() {
		throw new TangYuanException("This method subclass must implement");
	}

	@Override
	public void putObject(Object key, Object value) {
		putObject(key, value, null);
	}

	@Override
	public void clear() {
	}

	@Override
	public int getSize() {
		return -1;// return 0;
	}

	@Override
	public String getId() {
		return null;
	}

	protected String	keyEncode	= "UTF-8";

	protected String parseKey(Object key) {
		if (null == key) {
			throw new CacheException("cache key does not allow null");
		}
		if (key instanceof String) {
			return (String) key;
		}
		return key.toString();
		// throw new CacheException("Illegal cache key: " + key);
	}

	// @Override
	// public ReadWriteLock getReadWriteLock() {
	// return null;
	// }
}
