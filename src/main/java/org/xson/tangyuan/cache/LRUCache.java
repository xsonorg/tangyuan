package org.xson.tangyuan.cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache extends AbstractCache {

	private final ICache		delegate;
	private Map<Object, Object>	keyMap;
	private Object				eldestKey;

	public LRUCache(ICache delegate, int size) {
		this.delegate = delegate;
		// setSize(1024);
		setSize(size);
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public int getSize() {
		return delegate.getSize();
	}

	private void setSize(final int size) {
		keyMap = new LinkedHashMap<Object, Object>(size, .75F, true) {
			private static final long	serialVersionUID	= 4267176411845948333L;

			protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
				boolean tooBig = size() > size;
				if (tooBig) {
					eldestKey = eldest.getKey();
				}
				return tooBig;
			}
		};
	}

	@Override
	public void putObject(Object key, Object value, Integer time) {
		delegate.putObject(key, value);
		cycleKeyList(key);
	}

	@Override
	public Object getObject(Object key) {
		keyMap.get(key); // 这里是触发缓存的使用, 最少使用的会移在队尾
		return delegate.getObject(key);
	}

	@Override
	public Object removeObject(Object key) {
		return delegate.removeObject(key);
	}

	@Override
	public void clear() {
		delegate.clear();
		keyMap.clear();
	}

	private void cycleKeyList(Object key) {
		keyMap.put(key, key);
		if (eldestKey != null) {
			delegate.removeObject(eldestKey);
			eldestKey = null;
		}
	}
}
