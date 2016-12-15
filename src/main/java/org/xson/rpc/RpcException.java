package org.xson.rpc;

public class RpcException extends RuntimeException {

	private static final long	serialVersionUID	= 13299535574469587L;

	private int					code;

	public RpcException(int code, String message) {
		super(message);
		this.code = code;
	}

	public RpcException(int code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public RpcException(int code, Throwable cause) {
		super(cause);
		this.code = code;
	}

	public int getCode() {
		return code;
	}

}
