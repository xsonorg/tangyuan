package org.xson.tangyuan.cache.vo;

import java.util.Map;

import net.sf.ehcache.CacheException;

import org.xson.tangyuan.cache.EhCacheCache;
import org.xson.tangyuan.cache.FIFOCache;
import org.xson.tangyuan.cache.ICache;
import org.xson.tangyuan.cache.LRUCache;
import org.xson.tangyuan.cache.LocalCache;
import org.xson.tangyuan.cache.LoggingCache;
import org.xson.tangyuan.cache.MemcachedCache;
import org.xson.tangyuan.cache.RedisCache;
import org.xson.tangyuan.cache.ScheduledCache;
import org.xson.tangyuan.cache.SoftCache;
import org.xson.tangyuan.cache.SynchronizedCache;
import org.xson.tangyuan.cache.WeakCache;
import org.xson.tangyuan.cache.vo.CacheVo.CacheStrategyType;
import org.xson.tangyuan.cache.vo.CacheVo.CacheType;

public class CacheCreater {

	public ICache newInstance(CacheVo cacheVo) {
		CacheType type = cacheVo.getType();
		if (CacheType.LOCAL == type) {
			return newLocalCache(cacheVo);
		} else if (CacheType.EHCACHE == type) {
			return newEhcache(cacheVo);
		} else if (CacheType.MEMCACHE == type) {
			return newMemcache(cacheVo);
		} else if (CacheType.REDIS == type) {
			return newRedisCache(cacheVo);
		}
		return null;
	}

	private ICache newLocalCache(CacheVo cacheVo) {
		Map<String, String> properties = cacheVo.getProperties();

		ICache localCache = new LocalCache(cacheVo.getId());

		CacheStrategyType strategyType = CacheStrategyType.LRU;
		String strategy = properties.get("strategy");
		if (null != strategy) {
			if ("FIFO".equalsIgnoreCase(strategy)) {
				strategyType = CacheStrategyType.FIFO;
			} else if ("SOFT".equalsIgnoreCase(strategy)) {
				strategyType = CacheStrategyType.SOFT;
			} else if ("WEAK".equalsIgnoreCase(strategy)) {
				strategyType = CacheStrategyType.WEAK;
			} else if ("TIME".equalsIgnoreCase(strategy)) {
				strategyType = CacheStrategyType.TIME;
			}
		}

		int maxSize = 1024;
		String _maxSize = properties.get("maxSize");
		if (null != _maxSize) {
			maxSize = Integer.parseInt(_maxSize);
		}

		int survivalTime = 10; // 10秒
		String _survivalTime = properties.get("survivalTime");
		if (null != _survivalTime) {
			survivalTime = Integer.parseInt(_survivalTime);
		}

		// 根据设置
		if (CacheStrategyType.LRU == strategyType) {
			localCache = new LRUCache(localCache, maxSize);
		} else if (CacheStrategyType.FIFO == strategyType) {
			localCache = new FIFOCache(localCache, maxSize);
		} else if (CacheStrategyType.SOFT == strategyType) {
			localCache = new SoftCache(localCache, maxSize);
		} else if (CacheStrategyType.WEAK == strategyType) {
			localCache = new WeakCache(localCache, maxSize);
		} else if (CacheStrategyType.TIME == strategyType) {
			localCache = new ScheduledCache(localCache, survivalTime);
		}

		// 如果是local必须
		localCache = new SynchronizedCache(localCache);

		// log可选
		boolean log = false;
		String _log = properties.get("log");
		if (null != _log) {
			log = Boolean.parseBoolean(_log);
		}
		if (log) {
			localCache = new LoggingCache(localCache);
		}

		return localCache;
	}

	private ICache newEhcache(CacheVo cacheVo) {
		EhCacheCache cache = new EhCacheCache();
		String resource = cacheVo.getResource();
		if (null == resource) {
			throw new CacheException("missing resource in ehcache type");
		}
		cache.start(resource, cacheVo.getProperties());
		return cache;
	}

	private ICache newMemcache(CacheVo cacheVo) {
		MemcachedCache cache = new MemcachedCache();
		cache.start(cacheVo.getResource(), cacheVo.getProperties());
		return cache;
	}

	private ICache newRedisCache(CacheVo cacheVo) {
		RedisCache cache = new RedisCache();
		cache.start(cacheVo.getResource(), cacheVo.getProperties());
		return cache;
	}

	// public ICache newInstance(String className) {
	// Object instance = null;
	// try {
	// Class<?> clazz = ClassUtils.forName(className);
	// if (ICache.class.isAssignableFrom(clazz)) {
	// instance = clazz.newInstance();
	// }
	// } catch (Throwable e) {
	// throw new CacheException("创建自定义缓存处理器异常", e);
	// }
	// if (null == instance) {
	// throw new CacheException("创建自定义缓存处理器失败");
	// }
	// return (ICache) instance;
	// }

}
