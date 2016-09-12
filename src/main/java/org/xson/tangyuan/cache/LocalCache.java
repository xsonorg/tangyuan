package org.xson.tangyuan.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * 顶层Cache
 */
public class LocalCache extends AbstractCache {

	private String				id;

	private Map<Object, Object>	cache	= new HashMap<Object, Object>();

	public LocalCache(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public int getSize() {
		return cache.size();
	}

	@Override
	public void putObject(Object key, Object value, Integer time) {
		cache.put(key, value);
	}

	public Object getObject(Object key) {
		return cache.get(key);
	}

	public Object removeObject(Object key) {
		return cache.remove(key);
	}

	public void clear() {
		cache.clear();
	}

	public boolean equals(Object o) {
		if (getId() == null) {
			throw new CacheException("Cache instances require an ID.");
		}
		if (this == o) {
			return true;
		}

		if (!(o instanceof ICache)) {
			return false;
		}

		ICache otherCache = (ICache) o;
		return getId().equals(otherCache.getId());
	}

	public int hashCode() {
		if (getId() == null) {
			throw new CacheException("Cache instances require an ID.");
		}
		return getId().hashCode();
	}

}
