package org.xson.tangyuan.cache;

import java.io.InputStream;

import org.xson.tangyuan.util.Resources;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class EhCacheCache extends AbstractCache {

	private CacheManager	cacheManager	= null;
	private Cache			cache;

	@Override
	public void start(String resource) {
		if (null != cacheManager) {
			return;
		}
		try {
			InputStream inputStream = Resources.getResourceAsStream(resource);
			this.cacheManager = CacheManager.create(inputStream);
			this.cache = cacheManager.getCache(cacheManager.getCacheNames()[0]);
		} catch (Throwable e) {
			throw new CacheException(e);
		}
	}

	@Override
	public void stop() {
		if (null != cacheManager) {
			cacheManager.shutdown();
		}
	}

	@Override
	public Object getObject(Object key) {
		Element element = this.cache.get(key);
		if (null != element) {
			return element.getObjectValue();
		}
		return null;
	}

	@Override
	public void putObject(Object key, Object value, Integer time) {
		// TODO 序列化
		Element element = null;
		if (null == time) {
			element = new Element(key, value);
		} else {
			element = new Element(key, value, time, time.intValue() * 2);
		}
		this.cache.put(element);
	}

	@Override
	public Object removeObject(Object key) {
		Object result = getObject(key);
		if (null != result) {
			this.cache.remove(key);
		}
		return result;
	}

	@Override
	public void clear() {
		this.cache.removeAll();
	}

	@Override
	public int getSize() {
		return cache.getSize();
	}

	// //得到缓存对象占用内存的大小
	// cache.getMemoryStoreSize();
	// //得到缓存读取的命中次数
	// cache.getStatistics().getCacheHits()
	// //得到缓存读取的错失次数
	// cache.getStatistics().getCacheMisses()

}
