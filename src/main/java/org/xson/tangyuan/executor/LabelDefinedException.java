package org.xson.tangyuan.executor;

import org.xson.tangyuan.TangYuanException;

public class LabelDefinedException extends TangYuanException {

	private static final long	serialVersionUID	= 7857190361062588205L;

	private int					code;

	private String				i18n;

	public LabelDefinedException(int code, String message, String i18n) {
		super(message);
		this.code = code;
		this.i18n = i18n;
	}

	public int getCode() {
		return code;
	}

	public String getI18n() {
		return i18n;
	}

}
