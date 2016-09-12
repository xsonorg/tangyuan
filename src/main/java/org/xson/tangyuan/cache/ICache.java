package org.xson.tangyuan.cache;

import java.util.Map;

public interface ICache {

	void start(Map<String, String> properties);

	void start(String resource);

	void stop();

	String getId();

	void putObject(Object key, Object value);

	void putObject(Object key, Object value, Integer time);

	Object getObject(Object key);

	Object removeObject(Object key);

	void clear();

	int getSize();

	// ReadWriteLock getReadWriteLock();

}
