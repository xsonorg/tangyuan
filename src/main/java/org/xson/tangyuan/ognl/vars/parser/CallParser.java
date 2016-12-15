package org.xson.tangyuan.ognl.vars.parser;

import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.vo.CallVariable;

public class CallParser extends AbstractParser {

	public boolean check(String text) {
		if (text.startsWith("@")) {
			return true;
		}
		return false;
	}

	private Object getVal(String text) {
		if ("null".equalsIgnoreCase(text)) {
			return null;
		}
		if ("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text)) {
			return Boolean.valueOf(text);
		}
		// 仅仅支持:数值类型, 字符串类型
		if (isStaticString(text)) { // 字符串
			return text.substring(1, text.length() - 1);
		} else if (isNumber(text)) { // 数值
			return getNumber(text);
		} else {// 变量
			return new NormalParser().parse(text);
		}
	}

	/**
	 * 解析调用表达式属性
	 */
	public Variable parse(String text) {
		text = text.trim();
		int left = text.indexOf("(");
		int right = text.lastIndexOf(")");

		if (left == -1 || right == -1 || left > right) {
			throw new OgnlException("不合法的调用表达式: " + text);
		}

		// TODO: 要区分Java, JS
		String method = text.substring(1, left).trim();
		String argString = text.substring(left + 1, right).trim();

		Object[] vars = null;
		if (argString.length() > 0) {
			String[] array = argString.split(",");
			vars = new Object[array.length];
			for (int i = 0; i < array.length; i++) {
				vars[i] = getVal(array[i].trim());
			}
		}
		return new CallVariable(text, method, vars);
	}

	public static void main(String[] args) {
		CallParser p = new CallParser();
		// String text = "@xxx(ccc, 's', 0L) ";
		String text = "@xxx() ";
		System.out.println(p.parse(text));
		// System.out.println("****************************");
	}

}
