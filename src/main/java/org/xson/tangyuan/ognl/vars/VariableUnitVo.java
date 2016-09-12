package org.xson.tangyuan.ognl.vars;

/**
 * 变量
 */
public class VariableUnitVo {
	/**
	 * 属性表达式类型
	 */
	public enum VariableUnitEnum {
		/**
		 * 属性
		 */
		PROPERTY,

		/**
		 * 索引
		 */
		INDEX,

		/**
		 * 变量,有可能是属性, 有可能是索引, 需要看所属的对象类型, 主要针对于括号中的
		 */
		VAR
	}

	/**
	 * 此属性的类型
	 */
	private VariableUnitEnum	type;

	/**
	 * 属性名称
	 */
	private String				name;

	/**
	 * 索引
	 */
	private int					index;

	protected VariableUnitVo(String name, boolean isVar) {
		if (isVar) {
			this.type = VariableUnitEnum.VAR;
		} else {
			this.type = VariableUnitEnum.PROPERTY;
		}
		this.name = name;
	}

	protected VariableUnitVo(int index) {
		this.type = VariableUnitEnum.INDEX;
		this.index = index;
	}

	public VariableUnitEnum getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

}
