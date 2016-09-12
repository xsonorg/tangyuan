package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.executor.SqlServiceContext;
import org.xson.tangyuan.executor.SqlServiceException;
import org.xson.tangyuan.ognl.expr.ExprGroupVo;

public class ExceptionNode implements SqlNode {

	private ExprGroupVo	test;

	private int			code;

	private String		message;

	// private String i18n;

	public ExceptionNode(ExprGroupVo test, int code, String message, String i18n) {
		this.test = test;
		this.code = code;
		this.message = message;
		// this.i18n = i18n;
		// if (null == i18n) {
		// this.i18n = this.message;
		// }
	}

	@Override
	public boolean execute(SqlServiceContext context, Object arg) {
		if (test.getResult(arg)) {
			// throw new LabelDefinedException(code, (null != message) ? message : "", (null != i18n) ? i18n : "");
			// new SqlServiceException("组合服务,事务处理中异常", e);
			throw new SqlServiceException(code, (null != message) ? message : "");
		}
		return true;
	}
}
