package org.xson.tangyuan.cache.vo;

import java.util.Map;

import org.xson.tangyuan.cache.ICache;

public class CacheVo {

	public enum CacheType {
		LOCAL, EHCACHE, MEMCACHE, REDIS
	}

	public enum CacheStrategyType {
		LRU, FIFO, SOFT, WEAK
	}

	protected String			id;

	protected boolean			defaultCache;

	protected boolean			group;

	private ICache				cache;

	private CacheType			type;

	private String				resource;

	private Map<String, String>	properties;

	protected CacheVo(String id, boolean defaultCache) {
		this.id = id;
		this.defaultCache = defaultCache;
	}

	public CacheVo(String id, CacheType type, ICache cache, boolean defaultCache, String resource, Map<String, String> properties) {
		this.id = id;
		this.type = type;
		this.cache = cache;
		this.defaultCache = defaultCache;
		this.resource = resource;
		this.properties = properties;
		this.group = false;
	}

	public String getId() {
		return id;
	}

	public ICache getCache() {
		return cache;
	}

	public boolean isDefaultCache() {
		return defaultCache;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public boolean isGroup() {
		return group;
	}

	public CacheType getType() {
		return type;
	}

	public void setDefaultCache(boolean defaultCache) {
		this.defaultCache = defaultCache;
	}

	public String getResource() {
		return resource;
	}

	public void putObject(String key, Object value, Integer time, String[] ignore, String service) {
		cache.putObject(key, value, time);
	}

	public Object getObject(String key) {
		return cache.getObject(key);
	}

	public Object removeObject(String key, String[] ignore, String service) {
		return cache.removeObject(key);
	}

	public void start() {
		if (null != cache) {
			cache.start(properties);
		} else {
			cache = new CacheCreater().newInstance(this);
		}
	}
}
