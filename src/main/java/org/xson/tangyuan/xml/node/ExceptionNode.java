package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.ognl.vars.vo.LogicalVariable;

public class ExceptionNode implements TangYuanNode {

	private LogicalVariable	test;

	private int				code;

	private String			message;

	// private String i18n;
	// this.i18n = i18n;
	// if (null == i18n) {
	// this.i18n = this.message;
	// }

	public ExceptionNode(LogicalVariable test, int code, String message, String i18n) {
		this.test = test;
		this.code = code;
		this.message = message;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) {
		if (test.getResult(arg)) {
			// throw new LabelDefinedException(code, (null != message) ? message : "", (null != i18n) ? i18n : "");
			throw new ServiceException(code, (null != message) ? message : "");
		}
		return true;
	}
}
