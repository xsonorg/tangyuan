package org.xson.tangyuan.ognl.expr;

/**
 * 表达式单元
 */
public class ExprUnit {

	private Object	value;

	/**
	 * 是否是变量
	 */
	private boolean	variable;

	protected ExprUnit(Object value, boolean variable) {
		this.value = value;
		this.variable = variable;
	}

	public Object getValue() {
		return this.value;
	}

	public boolean isVariable() {
		return variable;
	}

	// public static int exprUnitConstants = 0; // 常量值[数值, 字符串, null, true,
	// // false]
	// public static int exprUnitVariables = 2; // 变量

	// public static int valueTypeNull = 11; // NULL
	// public static int valueType1 = 1; // byte
	// public static int valueType2 = 2; // shot
	// public static int valueType3 = 3; // int
	// public static int valueType4 = 4; // long
	// public static int valueType5 = 5; // float
	// public static int valueType6 = 6; // double
	// public static int valueType7 = 7; // char
	// public static int valueType8 = 8; // boolean
	// public static int valueType9 = 9; // String
	// public static int valueType10 = 10; // Object
	// sql.data, sql.time, data, Timestamp, BigInteger, BigDecimal

	// private int unitType;
	// 如果是常量值, 则表示值的类型
	// private int valueType;

	// protected ExprUnit(Object value, int unitType) {
	// this(value, unitType, valueTypeNull);
	// }
	//
	// protected ExprUnit(Object value, int unitType, int valueType) {
	// this.value = value;
	// this.unitType = unitType;
	// this.valueType = valueType;
	// }

	// public void setValue(String value) {
	// this.value = value;
	// }

	// public int getUnitType() {
	// return unitType;
	// }
	//
	// public void setUnitType(int unitType) {
	// this.unitType = unitType;
	// }

	// public int getValueType() {
	// return valueType;
	// }
	//
	// public void setValueType(int valueType) {
	// this.valueType = valueType;
	// }

}
