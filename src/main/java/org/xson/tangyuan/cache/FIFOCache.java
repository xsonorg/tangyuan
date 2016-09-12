package org.xson.tangyuan.cache;

import java.util.LinkedList;

public class FIFOCache extends AbstractCache {

	private final ICache		delegate;
	private LinkedList<Object>	keyList;
	private int					size;

	// public void setSize(int size) {
	// this.size = size;
	// }

	public FIFOCache(ICache delegate, int size) {
		this.delegate = delegate;
		this.keyList = new LinkedList<Object>();
		this.size = size;
		// this.size = 1024;
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public int getSize() {
		return delegate.getSize();
	}

	// @Override
	// public void putObject(Object key, Object value) {
	// cycleKeyList(key);
	// delegate.putObject(key, value);
	// }

	@Override
	public void putObject(Object key, Object value, Integer time) {
		cycleKeyList(key);
		delegate.putObject(key, value);
	}

	@Override
	public Object getObject(Object key) {
		return delegate.getObject(key);
	}

	@Override
	public Object removeObject(Object key) {
		return delegate.removeObject(key);
	}

	@Override
	public void clear() {
		delegate.clear();
		keyList.clear();
	}

	private void cycleKeyList(Object key) {
		keyList.addLast(key);
		if (keyList.size() > size) {
			Object oldestKey = keyList.removeFirst();
			delegate.removeObject(oldestKey);
		}
	}
}
