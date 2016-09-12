package org.xson.tangyuan.logging;

import org.xson.tangyuan.TangYuanException;

public class LogException extends TangYuanException {

	private static final long	serialVersionUID	= 1022924004852350942L;

	public LogException() {
		super();
	}

	public LogException(String message) {
		super(message);
	}

	public LogException(String message, Throwable cause) {
		super(message, cause);
	}

	public LogException(Throwable cause) {
		super(cause);
	}

}
