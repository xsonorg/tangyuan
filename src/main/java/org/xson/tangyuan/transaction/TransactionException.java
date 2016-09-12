package org.xson.tangyuan.transaction;

import org.xson.tangyuan.TangYuanException;

public class TransactionException extends TangYuanException {

	private static final long	serialVersionUID	= -8015570658967350489L;

	public TransactionException() {
		super();
	}

	public TransactionException(String message) {
		super(message);
	}

	public TransactionException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransactionException(Throwable cause) {
		super(cause);
	}

}
