package org.xson.tangyuan.cache;

import java.util.Map;

import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;

public class LoggingCache extends AbstractCache {

	protected Log	log			= LogFactory.getLog(LoggingCache.class);

	private ICache	delegate;
	protected int	requests	= 0;
	protected int	hits		= 0;

	public LoggingCache(ICache delegate) {
		this.delegate = delegate;
		// this.log = LogFactory.getLog(getId());
	}

	@Override
	public void start(Map<String, String> properties) {
		this.delegate.start(properties);
	}

	@Override
	public void stop() {
		this.delegate.stop();
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
	// public void putObject(Object key, Object object) {
	// delegate.putObject(key, object);
	// }

	@Override
	public void putObject(Object key, Object value, Integer time) {
		delegate.putObject(key, value);
	}

	@Override
	public Object getObject(Object key) {
		requests++;
		final Object value = delegate.getObject(key);
		if (value != null) {
			hits++;
		}
		if (log.isDebugEnabled()) {
			log.debug("Cache Hit Ratio [" + getId() + "]: " + getHitRatio());
		}
		return value;
	}

	@Override
	public Object removeObject(Object key) {
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

	private double getHitRatio() {
		return (double) hits / (double) requests;
	}
}
