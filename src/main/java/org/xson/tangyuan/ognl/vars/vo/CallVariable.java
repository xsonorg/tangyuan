package org.xson.tangyuan.ognl.vars.vo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.ognl.vars.Variable;

/**
 * 方法调用变量
 */
public class CallVariable extends Variable {

	private String		method;
	private Object[]	vars;

	public CallVariable(String original, String method, Object[] vars) {
		this.original = original;
		this.method = method;
		this.vars = vars;
	}

	public Object getValue(Object arg) {
		// TODO: 要区分JAVA, JS, GROVE(脚本语言)
		// System.out.println(method);
		try {
			Object[] tempArgs = null;
			if (null != vars && vars.length > 0) {
				tempArgs = new Object[vars.length];
				for (int i = 0; i < vars.length; i++) {
					if (vars[i] instanceof VariableItemWraper) {
						tempArgs[i] = ((VariableItemWraper) vars[i]).getValue(arg);
					} else {
						tempArgs[i] = vars[i];
					}
				}
			}
			Method m = null;
			if (null == tempArgs) {
				return m.invoke(null);
			} else {
				return m.invoke(null, tempArgs);
			}
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				throw new OgnlException(((InvocationTargetException) e).getTargetException());
			} else {
				throw new OgnlException(e);
			}
		}
	}
}
