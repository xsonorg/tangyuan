package org.xson.tangyuan.cache;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SynchronizedCache extends AbstractCache {

	private ICache			cache;
	private ReadWriteLock	lock;

	public SynchronizedCache(ICache cache) {
		this.cache = cache;
		this.lock = new ReentrantReadWriteLock();
	}

	@Override
	public void start(Map<String, String> properties) {
		this.cache.start(properties);
	}

	@Override
	public void stop() {
		this.cache.stop();
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public void putObject(Object key, Object value, Integer time) {
		this.lock.writeLock().lock();
		try {
			cache.putObject(key, value);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public Object getObject(Object key) {
		this.lock.readLock().lock();
		try {
			return cache.getObject(key);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	@Override
	public Object removeObject(Object key) {
		this.lock.writeLock().lock();
		try {
			return cache.removeObject(key);
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public void clear() {
		this.lock.writeLock().lock();
		try {
			cache.clear();
		} finally {
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public int getSize() {
		this.lock.readLock().lock();
		try {
			return cache.getSize();
		} finally {
			this.lock.readLock().unlock();
		}
	}
}
