package org.xson.tangyuan.logging;

public interface Log {

	boolean isDebugEnabled();

	boolean isTraceEnabled();

	boolean isInfoEnabled();

	void error(String s, Throwable e);

	void error(String s);

	void info(String s);

	void info(String s, Object[] args);

	void debug(String s);

	void trace(String s);

	void warn(String s);

}
