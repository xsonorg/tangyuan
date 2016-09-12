package org.xson.tangyuan.logging.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xson.tangyuan.logging.Log;

public class Slf4jLoggerImpl implements Log {

	private Logger log;

	public Slf4jLoggerImpl(String clazz) {
		log = LoggerFactory.getLogger(clazz);
	}

	public Slf4jLoggerImpl(Logger logger) {
		log = logger;
	}

	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	public boolean isTraceEnabled() {
		return log.isTraceEnabled();
	}

	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}

	public void error(String s, Throwable e) {
		log.error(s, e);
	}

	public void error(String s) {
		log.error(s);
	}

	public void debug(String s) {
		log.debug(s);
	}

	public void trace(String s) {
		log.trace(s);
	}

	public void warn(String s) {
		log.warn(s);
	}

	public void info(String s) {
		log.info(s);
	}

	public void info(String s, Object[] args) {
		log.info(s);
	}

}
