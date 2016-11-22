package org.xson.tangyuan.cache;

import java.util.HashMap;
import java.util.Map;

public class ScheduledCache extends AbstractCache {

	private ICache				delegate;

	private Map<Object, Long>	keyMap;

	private int					defaultSurvivalTime;

	public ScheduledCache(ICache delegate, int defaultSurvivalTime) {
		this.delegate = delegate;
		this.defaultSurvivalTime = defaultSurvivalTime;
		keyMap = new HashMap<Object, Long>();
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public int getSize() {
		return delegate.getSize();
	}

	/**
	 * time: 超时时间, 单位秒
	 */
	@Override
	public void putObject(Object key, Object value, Integer time) {
		int survivalTime = this.defaultSurvivalTime;
		if (null != time) {
			survivalTime = time.intValue();
		}
		keyMap.put(key, System.currentTimeMillis() + survivalTime * 1000);
		delegate.putObject(key, value);
	}

	@Override
	public Object getObject(Object key) {
		if (clearWhenStale(key)) {
			return null;
		} else {
			return delegate.getObject(key);
		}
	}

	@Override
	public Object removeObject(Object key) {
		keyMap.remove(key);
		return delegate.removeObject(key);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	// true: 超时, false: 未超时
	private boolean clearWhenStale(Object key) {
		// TODO 这里有问题呢, 需要删除过期的key
		Long survivalTime = keyMap.get(key);
		if (null == survivalTime) {
			return true;
		}
		if (System.currentTimeMillis() > survivalTime.longValue()) {
			return true;
		}
		return false;
	}

}
