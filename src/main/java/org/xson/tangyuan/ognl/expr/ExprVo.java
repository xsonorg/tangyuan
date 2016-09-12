package org.xson.tangyuan.ognl.expr;

import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.ognl.vars.VariableParser;
import org.xson.tangyuan.ognl.vars.VariableVo;

/**
 * 表达式
 */
public class ExprVo {

	public static int	exprOperators1	= 1;	// ==
	public static int	exprOperators2	= 2;	// !=
	public static int	exprOperators3	= 3;	// >
	public static int	exprOperators4	= 4;	// >=
	public static int	exprOperators5	= 5;	// <
	public static int	exprOperators6	= 6;	// <=
	public static int	exprOperators9	= 9;	// ||

	private ExprUnit	unit1;
	private int			operators;
	private ExprUnit	unit2;

	private boolean isVar(String var) {
		if (var.length() > 2 && var.startsWith("{") && var.endsWith("}")) {
			return true;
		}
		return false;
	}

	private String getRealVar(String var) {
		return var.substring(1, var.length() - 1);
	}

	private boolean isNumber(String var) {
		return var.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	}

	private ExprUnit getNumberUnit(String var) {
		ExprUnit unit = null;
		if (var.indexOf(".") == -1) {
			try {
				unit = new ExprUnit(Integer.parseInt(var), ExprUnit.exprUnitConstants, ExprUnit.valueType3);
			} catch (NumberFormatException e) {
				unit = new ExprUnit(Long.parseLong(var), ExprUnit.exprUnitConstants, ExprUnit.valueType4);
			}
		} else {
			try {
				unit = new ExprUnit(Float.parseFloat(var), ExprUnit.exprUnitConstants, ExprUnit.valueType5);
			} catch (NumberFormatException e) {
				unit = new ExprUnit(Double.parseDouble(var), ExprUnit.exprUnitConstants, ExprUnit.valueType6);
			}
		}
		return unit;
	}

	public void addUnit(String var, boolean isStringConstant) {
		ExprUnit unit = null;
		if (isStringConstant) {
			// 常量字符串,''
			unit = new ExprUnit(var, ExprUnit.exprUnitConstants, ExprUnit.valueType9);
		} else {
			// null, true, false, and or
			if ("null".equalsIgnoreCase(var)) {
				unit = new ExprUnit(null, ExprUnit.exprUnitConstants, ExprUnit.valueTypeNull);
			} else if ("and".equalsIgnoreCase(var) || "or".equalsIgnoreCase(var)) {
				addOperators(var);
			} else if ("true".equalsIgnoreCase(var) || "false".equalsIgnoreCase(var)) {
				unit = new ExprUnit(Boolean.parseBoolean(var), ExprUnit.exprUnitConstants, ExprUnit.valueType8);
			} else {
				if (isVar(var)) { // 是否是变量:{}
					// unit = new ExprUnit(var, ExprUnit.exprUnitVariables);
					unit = new ExprUnit(VariableParser.parse(getRealVar(var), false), ExprUnit.exprUnitVariables);
				} else if (isNumber(var)) { // 是否是数字常量
					unit = getNumberUnit(var);
				} else {
					throw new OgnlException("addUnit 不合法的内容:" + var);
				}
			}
		}
		addUnit(unit);
	}

	private void addUnit(ExprUnit unit) {
		if (0 == this.operators && null == this.unit1) {
			this.unit1 = unit;
		} else if (this.operators > 0 && null == this.unit2) {
			this.unit2 = unit;
		} else {
			throw new RuntimeException("addUnit error");
		}
	}

	public void addOperators(String var) {
		if (null == this.unit1 || this.operators > 0) {
			throw new OgnlException("不合理的表达式操作符:" + var);
		}
		if ("==".equals(var)) {
			addOperators(exprOperators1);
		} else if ("!=".equals(var)) {
			addOperators(exprOperators2);
		} else if (">".equals(var) || "&gt;".equalsIgnoreCase(var)) {
			addOperators(exprOperators3);
		} else if (">=".equals(var) || "&gt;=".equalsIgnoreCase(var)) {
			addOperators(exprOperators4);
		} else if ("<".equals(var) || "&lt;".equalsIgnoreCase(var)) {
			addOperators(exprOperators5);
		} else if ("<=".equals(var) || "&lt;=".equalsIgnoreCase(var)) {
			addOperators(exprOperators6);
		} else if ("and".equalsIgnoreCase(var)) {
			addOperators(exprOperators1);
		} else if ("or".equalsIgnoreCase(var)) {
			addOperators(exprOperators9);
		} else {
			throw new OgnlException("不合理的表达式操作符:" + var);
		}
	}

	private void addOperators(int operators) {
		this.operators = operators;
	}

	public boolean check() {
		// 有一个没有赋值,就返回false
		if (null == this.unit1 || null == this.unit2 || 0 == this.operators) {
			return false;
		}
		return true;
	}

	private int getClassType(Class<?> clazz) {
		if (byte.class == clazz || Byte.class == clazz) {
			return ExprUnit.valueType1;
		} else if (short.class == clazz || Short.class == clazz) {
			return ExprUnit.valueType2;
		} else if (int.class == clazz || Integer.class == clazz) {
			return ExprUnit.valueType3;
		} else if (long.class == clazz || Long.class == clazz) {
			return ExprUnit.valueType4;
		} else if (float.class == clazz || Float.class == clazz) {
			return ExprUnit.valueType5;
		} else if (double.class == clazz || Double.class == clazz) {
			return ExprUnit.valueType6;
		} else if (char.class == clazz || Character.class == clazz) {
			return ExprUnit.valueType7;
		} else if (boolean.class == clazz || Boolean.class == clazz) {
			return ExprUnit.valueType8;
		} else if (String.class == clazz) {
			return ExprUnit.valueType9;
		} else {
			return ExprUnit.valueType10;
		}
	}

	public boolean getResult(Object data) {
		if (null != unit1 && null != unit2) {
			Object var1 = null;
			int type1 = ExprUnit.valueTypeNull;
			if (ExprUnit.exprUnitConstants == this.unit1.getUnitType()) {
				var1 = this.unit1.getValue();
				type1 = this.unit1.getValueType();
			} else {
				// var1 = OgnlMap.getValue(data, (VariableVo) this.unit1.getValue());
				var1 = ((VariableVo) this.unit1.getValue()).getValue(data);
				if (null != var1) {
					type1 = getClassType(var1.getClass());
				}
			}
			Object var2 = null;
			int type2 = ExprUnit.valueTypeNull;
			if (ExprUnit.exprUnitConstants == this.unit2.getUnitType()) {
				var2 = this.unit2.getValue();
				type2 = this.unit2.getValueType();
			} else {
				// var2 = OgnlMap.getValue(data, (VariableVo) this.unit2.getValue());
				var2 = ((VariableVo) this.unit2.getValue()).getValue(data);
				if (null != var2) {
					// type2 = getClassType(var1.getClass());
					type2 = getClassType(var2.getClass());
				}
			}

			if (this.operators == exprOperators1 || this.operators == exprOperators2) { // ==|!=
				if (type1 < ExprUnit.valueType7 && type2 < ExprUnit.valueType7) {
					return numberCompare(var1, type1, var2, type2);
				} else if (type1 == ExprUnit.valueType9 && type2 == ExprUnit.valueType9) {
					if (this.operators == exprOperators1) {
						return var1.equals(var2);
					} else {
						return !var1.equals(var2);
					}
				} else if (type1 == ExprUnit.valueType8 && type2 == ExprUnit.valueType8) {
					if (this.operators == exprOperators1) {
						return ((Boolean) var1).booleanValue() == ((Boolean) var2).booleanValue();
					} else {
						return ((Boolean) var1).booleanValue() != ((Boolean) var2).booleanValue();
					}
				} else {
					if (this.operators == exprOperators1) {
						return var1 == var2;
					} else {
						return var1 != var2;
					}
				}
			} else if (this.operators == exprOperators3 || this.operators == exprOperators4 || this.operators == exprOperators5
					|| this.operators == exprOperators6) { // >|>=|<|<=
				if (type1 > ExprUnit.valueType6 || type2 > ExprUnit.valueType6) {
					throw new OgnlException("var1, var2必须是数值型");
				}
				return numberCompare(var1, type1, var2, type2);
			}
		}
		throw new OgnlException("getResult:表达式不合法.");
	}

	private boolean numberCompare(Object var1, int type1, Object var2, int type2) {
		if (this.operators == exprOperators1) {
			return ExprCompare.numberCompareEqual(var1, type1, var2, type2);
		} else if (this.operators == exprOperators2) { // >
			return ExprCompare.numberCompareNotEqual(var1, type1, var2, type2);
		} else if (this.operators == exprOperators3) { // >
			return ExprCompare.numberCompareMoreThan(var1, type1, var2, type2);
		} else if (this.operators == exprOperators4) { // >=
			return ExprCompare.numberCompareGreaterThanOrEqual(var1, type1, var2, type2);
		} else if (this.operators == exprOperators5) { // <
			return ExprCompare.numberCompareLessThan(var1, type1, var2, type2);
		} else if (this.operators == exprOperators6) { // <=
			return ExprCompare.numberCompareLessThanOrEqual(var1, type1, var2, type2);
		}
		throw new OgnlException("不支持的表达式:" + this.operators);
	}
}
