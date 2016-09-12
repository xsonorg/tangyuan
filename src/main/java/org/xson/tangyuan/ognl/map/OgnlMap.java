package org.xson.tangyuan.ognl.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.ognl.FieldVo;
import org.xson.tangyuan.ognl.FieldVoWrapper;
import org.xson.tangyuan.ognl.OgnlException;
import org.xson.tangyuan.ognl.vars.VariableUnitVo;
import org.xson.tangyuan.ognl.vars.VariableUnitVo.VariableUnitEnum;
import org.xson.tangyuan.ognl.vars.VariableVo;
import org.xson.tangyuan.util.TypeUtils;

public class OgnlMap {

	public static Map<String, Object> beanToMap(Object bean) {
		if (null == bean) {
			return null;
		}
		Map<String, Object> beanMap = new HashMap<String, Object>();
		FieldVoWrapper fieldVoWrapper = TypeUtils.getBeanField(bean.getClass());
		List<FieldVo> fieldList = fieldVoWrapper.getFieldList();
		for (FieldVo model : fieldList) {
			try {
				Object result = model.getGetter().invoke(bean);
				if (null != result) {
					beanMap.put(model.getName(), result);
				}
			} catch (Exception e) {
				throw new OgnlException("bean to map error: " + bean.getClass(), e);
			}
		}
		return beanMap;
	}

	// data为原始数据
	public static Object getValue(Map<String, Object> data, VariableVo varVo) {
		// 这里取值为空是否要报错, 应该严格报错, 只有最后一个为空，可以忽略
		if (null != varVo.getVarUnit()) {
			Object result = data.get(varVo.getVarUnit().getName());
			if (null != result) {
				return result;
			}
			if (varVo.isHasDefault()) {
				result = varVo.getDefaultValue();
			}
			return result;
		}
		List<VariableUnitVo> varUnitList = varVo.getVarUnitList();
		int size = varUnitList.size();
		Object returnObj = data;
		for (int i = 0; i < size; i++) {
			boolean hasNext = (i + 1) < size;
			VariableUnitVo vUnitVo = varUnitList.get(i);
			if (returnObj instanceof Map) {
				returnObj = getValueFromMap(returnObj, vUnitVo, hasNext, data);
			} else if (returnObj instanceof Collection) {
				returnObj = getValueFromCollection(returnObj, vUnitVo, hasNext, data);
			} else if (returnObj.getClass().isArray()) {
				Class<?> clazz = returnObj.getClass();
				if (int[].class == clazz) {
					returnObj = getValueFromIntArray(returnObj, vUnitVo, data);
				} else if (long[].class == clazz) {
					returnObj = getValueFromLongArray(returnObj, vUnitVo, data);
				} else if (float[].class == clazz) {
					returnObj = getValueFromFloatArray(returnObj, vUnitVo, data);
				} else if (double[].class == clazz) {
					returnObj = getValueFromDoubleArray(returnObj, vUnitVo, data);
				} else if (byte[].class == clazz) {
					returnObj = getValueFromByteArray(returnObj, vUnitVo, data);
				} else if (short[].class == clazz) {
					returnObj = getValueFromShortArray(returnObj, vUnitVo, data);
				} else if (boolean[].class == clazz) {
					returnObj = getValueFromBooleanArray(returnObj, vUnitVo, data);
				} else if (char[].class == clazz) {
					returnObj = getValueFromCharArray(returnObj, vUnitVo, data);
				} else {
					returnObj = getValueFromObjectArray(returnObj, vUnitVo, hasNext, data);
				}
			} else if (TypeUtils.isBeanType(returnObj)) {
				returnObj = getValueFromMap(OgnlMap.beanToMap(returnObj), vUnitVo, hasNext, data);
			} else {
				throw new OgnlException("get map value error: " + returnObj);// 类型错误
			}
			if (null == returnObj && hasNext) {
				throw new OgnlException("get map value error: " + varVo.getOriginal());
			}
		}

		if (null == returnObj && varVo.isHasDefault()) {
			returnObj = varVo.getDefaultValue();
		}

		return returnObj;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Object getValueFromMap(Object target, VariableUnitVo peVo, boolean hasNext, Map<String, Object> data) {

		String key = peVo.getName();
		if (VariableUnitEnum.VAR == peVo.getType()) {
			key = (String) data.get(peVo.getName());
		}

		if (VariableUnitEnum.PROPERTY == peVo.getType() || VariableUnitEnum.VAR == peVo.getType()) {
			// Map<String, Object> map = (Map<String, Object>) target;
			Map map = (Map) target;
			// String key = peVo.getName();
			Object value = map.get(key);
			if (null != value) {
				// 这里做预转换
				if (hasNext && TypeUtils.isBeanType(value)) {
					value = OgnlMap.beanToMap(value);
					map.put(key, value);
				}
				return value;
			}
			if ("size".equalsIgnoreCase(key) || "length".equalsIgnoreCase(key)) {
				return map.size();
			}
			return null;
		}
		throw new OgnlException("getValueFromMap error: " + target);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Object getValueFromCollection(Object target, VariableUnitVo peVo, boolean hasNext, Map<String, Object> data) {

		int index = peVo.getIndex();
		if (VariableUnitEnum.VAR == peVo.getType()) {
			index = (Integer) data.get(peVo.getName());
		}

		if (VariableUnitEnum.INDEX == peVo.getType() || VariableUnitEnum.VAR == peVo.getType()) {
			if (target instanceof List) {
				List list = (List) target;
				if (index < list.size()) {
					Object value = list.get(index);
					if (hasNext && TypeUtils.isBeanType(value)) {
						value = OgnlMap.beanToMap(value);
						list.set(index, value);
					}
					return value;
				}
			} else {
				// Iterator<?> iterator = ((Collection<?>) target).iterator();
				// int i = 0;
				// int targetIndex = peVo.getIndex();
				// while (iterator.hasNext()) {
				// if (i++ == targetIndex) {
				// return iterator.next();
				// }
				// }
				// 以后考虑在优化

				int i = 0;
				Collection collection = (Collection<?>) target;
				for (Object obj : collection) {
					if (i++ == index) {
						return obj;
					}
				}
			}
		} else {
			String key = peVo.getName();
			if ("size".equalsIgnoreCase(key) || "length".equalsIgnoreCase(key)) {
				Collection<?> collection = (Collection<?>) target;
				return collection.size();
			}
		}
		return null;
	}

	private static Object getValueFromObjectArray(Object target, VariableUnitVo peVo, boolean hasNext, Map<String, Object> data) {
		Object[] array = (Object[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				Object value = array[peVo.getIndex()];
				if (hasNext && TypeUtils.isBeanType(value)) {
					value = OgnlMap.beanToMap(value);
					array[peVo.getIndex()] = value;
				}
				return value;
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) data.get(peVo.getName());
			if (index < array.length) {
				Object value = array[index];
				if (hasNext && TypeUtils.isBeanType(value)) {
					value = OgnlMap.beanToMap(value);
					array[index] = value;
				}
				return value;
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromIntArray(Object target, VariableUnitVo peVo, Map<String, Object> data) {
		int[] array = (int[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) data.get(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromLongArray(Object target, VariableUnitVo peVo, Map<String, Object> data) {
		long[] array = (long[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) data.get(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromBooleanArray(Object target, VariableUnitVo peVo, Map<String, Object> data) {
		boolean[] array = (boolean[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) data.get(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromByteArray(Object target, VariableUnitVo peVo, Map<String, Object> data) {
		byte[] array = (byte[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) data.get(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromCharArray(Object target, VariableUnitVo peVo, Map<String, Object> data) {
		char[] array = (char[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) data.get(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromDoubleArray(Object target, VariableUnitVo peVo, Map<String, Object> data) {
		double[] array = (double[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) data.get(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromFloatArray(Object target, VariableUnitVo peVo, Map<String, Object> data) {
		float[] array = (float[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) data.get(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

	private static Object getValueFromShortArray(Object target, VariableUnitVo peVo, Map<String, Object> data) {
		short[] array = (short[]) target;
		if (VariableUnitEnum.INDEX == peVo.getType()) {
			if (peVo.getIndex() < array.length) {
				return array[peVo.getIndex()];
			}
		} else if (VariableUnitEnum.VAR == peVo.getType()) {
			int index = (Integer) data.get(peVo.getName());
			if (index < array.length) {
				return array[index];
			}
		} else {
			String key = peVo.getName();
			if ("length".equalsIgnoreCase(key)) {
				return array.length;
			}
		}
		return null;
	}

}
