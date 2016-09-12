package org.xson.tangyuan.ognl.vars;

import java.util.List;

import org.xson.tangyuan.ognl.Ognl;

/**
 * 变量
 */
public class VariableVo {

	private VariableUnitVo			varUnit				= null;

	private List<VariableUnitVo>	varUnitList			= null;

	// 属性的默认值 #{abccc|0, null, 'xxx', now(), date(), time()}, 只有在变量[#{}|${}]里边才可以存在
	private Object					defaultValue		= null;

	// 默认值类型: 0:普通, 1:now(), 2:date(), 3:time()
	private int						defaultValueType	= 0;

	private boolean					hasDefault			= false;

	// 原始的属性字符串, 用作日志显示
	private String					original;

	protected VariableVo(String original, VariableUnitVo varUnit, boolean hasDefault, Object defaultValue, int defaultValueType) {
		this.original = original;
		this.varUnit = varUnit;
		this.hasDefault = hasDefault;
		this.defaultValue = defaultValue;
		this.defaultValueType = defaultValueType;
	}

	protected VariableVo(String original, List<VariableUnitVo> varUnitList, boolean hasDefault, Object defaultValue, int defaultValueType) {
		this.original = original;
		this.varUnitList = varUnitList;
		this.hasDefault = hasDefault;
		this.defaultValue = defaultValue;
		this.defaultValueType = defaultValueType;
	}

	public Object getDefaultValue() {
		if (0 == defaultValueType) {
			return defaultValue;
		} else if (1 == defaultValueType) {
			return new java.util.Date();
		} else if (2 == defaultValueType) {
			return new java.sql.Date(new java.util.Date().getTime());
		} else {
			return new java.sql.Time(new java.util.Date().getTime());
		}
	}

	public VariableUnitVo getVarUnit() {
		return varUnit;
	}

	public List<VariableUnitVo> getVarUnitList() {
		return varUnitList;
	}

	public boolean isHasDefault() {
		return hasDefault;
	}

	public String getOriginal() {
		return original;
	}

	// public Object getValue(Map<String, Object> data) {
	// // TODO 默认值在这里处理
	// return OgnlMap.getValue(data, this);
	// }

	public Object getValue(Object data) {
		return Ognl.getValue(data, this);
	}

}
