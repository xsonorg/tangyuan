package org.xson.tangyuan.xml.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.bridge.BridgedCallSupport;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.ognl.vars.Variable;

public class CallNode implements TangYuanNode {

	private static Log					log			= LogFactory.getLog(CallNode.class);
	private String						service;
	private String						resultKey;
	private CallMode					mode;
	private List<CallNodeParameterItem>	itemList;
	// 当发生异常时候, 异常结果的描述key
	private String						exResultKey;
	// 桥接调用
	private boolean						bridgedCall	= false;

	public enum CallMode {
		// 继承之前的上下文
		EXTEND,
		// 单独的上下文
		ALONE,
		// 异步方式
		ASYNC
	}

	protected static class CallNodeParameterItem {

		protected CallNodeParameterItem(String name, Variable value) {
			this.name = name;
			this.value = value;
		}

		String		name;
		Variable	value;
	}

	public CallNode(String service, String resultKey, CallMode mode, List<CallNodeParameterItem> itemList, String exResultKey) {
		this.service = service;
		if (this.service.indexOf(BridgedCallSupport.separator) > 0) {
			bridgedCall = true;
		}
		this.resultKey = resultKey;
		this.mode = mode;
		this.itemList = itemList;
		this.exResultKey = exResultKey;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) {
		Object parameter = arg;
		if (null != itemList) {
			// 基于实际的参数来转换
			if (XCO.class == arg.getClass()) {
				XCO xco = new XCO();
				for (CallNodeParameterItem item : itemList) {
					xco.setObjectValue(item.name, item.value.getValue(arg));
				}
				parameter = xco;
			} else if (Map.class.isAssignableFrom(arg.getClass())) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (CallNodeParameterItem item : itemList) {
					map.put(item.name, item.value.getValue(arg));
				}
				parameter = map;
			} else {
				throw new TangYuanException("不支持的参数类型:" + arg.getClass());
			}
		}

		if (bridgedCall) {
			bridgedExecute(arg, parameter);
			return true;
		}

		if (CallMode.EXTEND == mode) {
			// Object result = ServiceActuator.executeContext(service, context, parameter);
			Object result = ServiceActuator.execute(service, parameter);
			if (null != this.resultKey) {
				Ognl.setValue(arg, this.resultKey, result);
			}
			// 这里的异常上抛
		} else if (CallMode.ALONE == mode) {
			try {
				Object result = ServiceActuator.executeAlone(service, parameter);
				if (null != this.resultKey) {
					Ognl.setValue(arg, this.resultKey, result);
				}
			} catch (ServiceException e) {
				if (null != exResultKey) {
					// 放置错误信息
					if (XCO.class == arg.getClass()) {
						XCO xco = new XCO();
						xco.setIntegerValue("code", e.getErrorCode());
						xco.setStringValue("message", e.getErrorMessage());
						Ognl.setValue(arg, this.exResultKey, xco);
					} else if (Map.class.isAssignableFrom(arg.getClass())) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("code", e.getErrorCode());
						map.put("message", e.getErrorMessage());
						Ognl.setValue(arg, this.exResultKey, map);
					} else {
						throw new TangYuanException("不支持的参数类型:" + arg.getClass());
					}
				}
				log.error("call service error: " + service, e);
			}
		} else {
			ServiceActuator.executeAsync(service, parameter);
		}
		return true;
	}

	private void bridgedExecute(Object arg, Object parameter) {
		if (CallMode.EXTEND == mode) {
			Object result = BridgedCallSupport.call(service, mode, parameter);
			if (null != this.resultKey) {
				Ognl.setValue(arg, this.resultKey, result);
			}
		} else if (CallMode.ALONE == mode) {
			try {
				Object result = BridgedCallSupport.call(service, mode, parameter);
				if (null != this.resultKey) {
					Ognl.setValue(arg, this.resultKey, result);
				}
			} catch (Exception e) {
				if (null != exResultKey) {
					// 放置错误信息
					if (XCO.class == arg.getClass()) {
						XCO xco = new XCO();
						xco.setIntegerValue("code", -1);
						xco.setStringValue("message", "服务调用失败: " + service);
						Ognl.setValue(arg, this.exResultKey, xco);
					} else if (Map.class.isAssignableFrom(arg.getClass())) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("code", -1);
						map.put("message", "服务调用失败: " + service);
						Ognl.setValue(arg, this.exResultKey, map);
					} else {
						throw new TangYuanException("不支持的参数类型:" + arg.getClass());
					}
				}
				log.error("call service error: " + service, e);
			}
		} else {
			BridgedCallSupport.call(service, mode, parameter);
		}
	}
}
