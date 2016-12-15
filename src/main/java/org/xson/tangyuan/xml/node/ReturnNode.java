package org.xson.tangyuan.xml.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.ognl.vars.Variable;

public class ReturnNode implements TangYuanNode {

	private Class<?>			resultType;

	/**
	 * 直接返回一个对象
	 */
	private Variable			resultValue;

	/**
	 * 返回多个对象
	 */
	private List<ReturnItem>	resultList;

	protected static class ReturnItem {

		protected ReturnItem(String name, Variable value) {
			this.name = name;
			this.value = value;
		}

		String		name;
		Variable	value;
	}

	public ReturnNode(Variable resultValue, List<ReturnItem> resultList, Class<?> resultType) {
		this.resultValue = resultValue;
		this.resultList = resultList;
		this.resultType = resultType;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) {
		if (Map.class == resultType) {
			if (null == resultValue) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (ReturnItem item : resultList) {
					map.put(item.name, item.value.getValue(arg));
				}
				context.setResult(map);
			} else {
				context.setResult(resultValue.getValue(arg));
			}
		} else if (XCO.class == resultType) {
			if (null == resultValue) {
				XCO xco = new XCO();
				for (ReturnItem item : resultList) {
					xco.setObjectValue(item.name, item.value.getValue(arg));
				}
				context.setResult(xco);
			} else {
				context.setResult(resultValue.getValue(arg));
			}
		}
		return true;
	}
}
