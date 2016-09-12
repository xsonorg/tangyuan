package org.xson.tangyuan.ognl.vars;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.type.Null;
import org.xson.tangyuan.util.NumberUtils;

/**
 * 属性表达式采集
 * 
 * <pre>
 * 	1:	a.b
 * 	2:	a["b"]
 * 	3:	a.b[0]
 * 	4:	a.b.c
 * </pre>
 * 
 */
public class VariableParser {

	/**
	 * 判断字符串是否是纯数字
	 * 
	 * @param builder
	 * @return
	 */
	// private static boolean isNumeric(StringBuilder builder) {
	// int length = builder.length();
	// for (int i = 0; i < length; i++) {
	// int chr = builder.charAt(i);
	// if (chr < 48 || chr > 57) {
	// return false;
	// }
	// }
	// return true;
	// }

	/**
	 * 判断字符串是否是静态属性'xxx' | "xxxx"
	 */
	private static boolean isStaticProperty(String property) {
		if (property.length() > 2 && ((property.startsWith("'") && property.endsWith("'")) || (property.startsWith("\"") && property.endsWith("\"")))) {
			return true;
		}
		return false;
	}

	/**
	 * 获取一个属性表达式对象
	 * 
	 * @param builder
	 * @param isInternalProperty
	 *            是否是方括号内部属性
	 * @return
	 */
	private static VariableUnitVo getVariableUnitVo(StringBuilder builder, boolean isInternalProperty) {
		String property = builder.toString().trim();
		if (!isInternalProperty) {
			return new VariableUnitVo(property, false);
		} else {
			// boolean isNum = isNumeric(builder);
			boolean isNum = NumberUtils.isInteger(property);
			if (isNum) {
				return new VariableUnitVo(Integer.parseInt(property));
			} else {
				if (isStaticProperty(property)) {
					property = property.substring(1, property.length() - 1);
					return new VariableUnitVo(property, false);
				}
				// 动态的
				return new VariableUnitVo(property, true);
			}
		}
	}

	/**
	 * 解析属性表达式结构
	 * 
	 * <pre>
	 * a["b"]==>a.b(p)
	 * a[n]==>a.0(n)
	 * </pre>
	 * 
	 * @param propertyExpr
	 * @return
	 */
	private static List<VariableUnitVo> parseComplexProperty(String propertyExpr) {
		List<VariableUnitVo> list = new ArrayList<VariableUnitVo>();
		char[] src = propertyExpr.toCharArray();
		int srcLength = src.length;
		StringBuilder builder = new StringBuilder();
		boolean isInternalProperty = false; // 是否进入内部属性采集
		for (int i = 0; i < srcLength; i++) {
			char key = src[i];
			switch (key) {
			case '.': // 前面采集告一段落
				if (builder.length() > 0) {
					list.add(getVariableUnitVo(builder, isInternalProperty));
					builder = new StringBuilder();
				}
				break;
			case '[': // 进入括弧模式
				if (builder.length() > 0) {
					list.add(getVariableUnitVo(builder, isInternalProperty));
					builder = new StringBuilder();
				}
				isInternalProperty = true;
				break;
			case ']':
				if (builder.length() > 0) {
					list.add(getVariableUnitVo(builder, isInternalProperty));
					builder = new StringBuilder();
				}
				isInternalProperty = false;
				break; // 退出括弧模式
			case '\'': // 属性采集
			case '"':
				// break;
			default:
				builder.append(key);
			}
		}
		if (builder.length() > 0) {
			list.add(getVariableUnitVo(builder, isInternalProperty));
		}
		return list;
	}

	/**
	 * 是否是一个简单的属性表达式
	 * 
	 * @param propertyExpr
	 * @return
	 */
	private static boolean isSimplePropertyExpr(String propertyExpr) {
		int dotIndex = propertyExpr.indexOf(".");
		int squareBracketsIndex = propertyExpr.indexOf("[");
		if (dotIndex < 0 && squareBracketsIndex < 0) {
			return true;
		}
		return false;
	}

	/**
	 * 检测是否存在默认
	 */
	private static boolean existDefaultValue(String property) {
		if (property.indexOf("|") > -1) {
			return true;
		}
		return false;
	}

	/**
	 * 获取刨除默认值的属性值
	 */
	private static String getPropertyWithOutDefault(String property) {
		return property.substring(0, property.indexOf("|"));
	}

	enum defaultValueEnum {
		NOW, DATE, TIME
	}

	private static Object getPropertyDefaultValue(String property) {
		int pos = property.indexOf("|");
		String defaultString = property.substring(pos + 1, property.length()).trim();
		if (0 == defaultString.length()) {
			throw new OgnlException("属性的默认值不合法:" + property);
		}
		// 属性的默认值 #{abccc|0, null, 'xxx', now(), date(), time()}, 只有在变量[#{}|${}]里边才可以存在
		if ("null".equalsIgnoreCase(defaultString)) {
			// return null;
			return Null.OBJECT; // TODO 需要测试
			// } else if (defaultString.length() > 2 && defaultString.startsWith("'") && defaultString.endsWith("'")) {
		} else if (defaultString.length() > 1 && defaultString.startsWith("'") && defaultString.endsWith("'")) {// fix bug
			return defaultString.substring(1, defaultString.length() - 1);
		} else if (NumberUtils.isNumber(defaultString)) {
			return NumberUtils.parseNumber(defaultString);
		}

		// else if ("now()".equalsIgnoreCase(defaultString)) {
		// return new java.util.Date();
		// } else if ("date()".equalsIgnoreCase(defaultString)) {
		// return new java.sql.Date(new java.util.Date().getTime());
		// } else if ("time()".equalsIgnoreCase(defaultString)) {
		// return new java.sql.Time(new java.util.Date().getTime());
		// }
		// fixBug
		else if ("now()".equalsIgnoreCase(defaultString)) {
			return defaultValueEnum.NOW;
		} else if ("date()".equalsIgnoreCase(defaultString)) {
			return defaultValueEnum.DATE;
		} else if ("time()".equalsIgnoreCase(defaultString)) {
			return defaultValueEnum.TIME;
		}

		else if ("byte.null".equalsIgnoreCase(defaultString)) {
			return Null.BYTE;
		} else if ("short.null".equalsIgnoreCase(defaultString)) {
			return Null.SHORT;
		} else if ("int.null".equalsIgnoreCase(defaultString)) {
			return Null.INTEGER;
		} else if ("long.null".equalsIgnoreCase(defaultString)) {
			return Null.LONG;
		} else if ("float.null".equalsIgnoreCase(defaultString)) {
			return Null.FLOAT;
		} else if ("bigDecimal.null".equalsIgnoreCase(defaultString)) {
			return Null.DOUBLE;
		} else if ("double.null".equalsIgnoreCase(defaultString)) {
			return Null.BIGDECIMAL;
		} else if ("string.null".equalsIgnoreCase(defaultString)) {
			return Null.STRING;
		} else if ("datetime.null".equalsIgnoreCase(defaultString)) {
			return Null.TIMESTAMP;
		} else if ("date.null".equalsIgnoreCase(defaultString)) {
			return Null.SQLDATE;
		} else if ("time.null".equalsIgnoreCase(defaultString)) {
			return Null.SQLTIME;
		} else if ("timestamp.null".equalsIgnoreCase(defaultString)) {
			return Null.SQLTIMESTAMP;
		}

		throw new OgnlException("属性的默认值不合法:" + property);
	}

	/**
	 * 解析表达式
	 * 
	 * @param property
	 * @param allowDefault
	 *            是否允许有默认值
	 * @return
	 */
	public static VariableVo parse(String property, boolean allowDefault) {
		Object defaultValue = null;
		String original = property;
		boolean hasDefault = false;
		int defaultValueType = 0;

		if (allowDefault && existDefaultValue(property)) {
			defaultValue = getPropertyDefaultValue(property);
			property = getPropertyWithOutDefault(property);
			hasDefault = true;
			if (defaultValueEnum.NOW == defaultValue) {
				defaultValueType = 1;
				defaultValue = null;
			} else if (defaultValueEnum.DATE == defaultValue) {
				defaultValueType = 2;
				defaultValue = null;
			} else if (defaultValueEnum.TIME == defaultValue) {
				defaultValueType = 3;
				defaultValue = null;
			}
		}
		if (isSimplePropertyExpr(property)) {
			return new VariableVo(original, new VariableUnitVo(property, false), hasDefault, defaultValue, defaultValueType);
		}
		return new VariableVo(original, parseComplexProperty(property), hasDefault, defaultValue, defaultValueType);
	}

}
