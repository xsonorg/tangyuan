package org.xson.tangyuan.xml;

import org.xson.tangyuan.util.DateUtils;
import org.xson.tangyuan.util.NumberUtils;
import org.xson.tangyuan.xml.node.CallNode.CallMode;

public abstract class XmlNodeBuilder {

	abstract public void parseRef();

	abstract public void parseService();

	abstract public void setContext(XmlNodeWrapper root, XmlParseContext context);

	protected String			ns				= "";

	protected XmlParseContext	parseContext	= null;

	protected boolean isEmpty(String data) {
		if (null == data || 0 == data.trim().length()) {
			return true;
		}
		return false;
	}

	protected String getFullId(String id) {
		if (null == ns || "".equals(ns)) {
			return id;
		}
		return ns + "." + id;
	}

	protected String getResultKey(String str) {
		if (null != str && str.length() > 2 && str.startsWith("{") && str.endsWith("}")) {
			return str.substring(1, str.length() - 1);
		}
		return null;
	}

	protected boolean checkVar(String str) {
		if (null != str && str.length() > 2 && str.startsWith("{") && str.endsWith("}")) {
			return true;
		}
		return false;
	}

	protected String getRealVal(String str) {
		return str.substring(1, str.length() - 1);
	}

	protected int getLogLevel(String str) {
		if ("ERROR".equalsIgnoreCase(str)) {
			return 5;
		} else if ("WARN".equalsIgnoreCase(str)) {
			return 4;
		} else if ("INFO".equalsIgnoreCase(str)) {
			return 3;
		} else if ("DEBUG".equalsIgnoreCase(str)) {
			return 2;
		} else {
			return 1;
		}
	}

	protected CallMode getCallMode(String str) {
		if ("EXTEND".equalsIgnoreCase(str)) {
			return CallMode.EXTEND;
		} else if ("ALONE".equalsIgnoreCase(str)) {
			return CallMode.ALONE;
		} else if ("ASYNC".equalsIgnoreCase(str)) {
			return CallMode.ASYNC;
		} else {
			return CallMode.EXTEND;
		}
	}

	protected Object getSetVarValue(String str, String type) {
		if (null == str) {
			return null;
		}
		if (null == type) {
			if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
				return Boolean.parseBoolean(str);
			} else if (NumberUtils.isNumber(str)) {
				return NumberUtils.parseNumber(str);
			} else if (DateUtils.isDateTime(str)) {
				return DateUtils.parseDate(str);
			} else if (DateUtils.isOnlyDate(str)) {
				return DateUtils.parseSqlDate(str);
			} else if (DateUtils.isOnlyTime(str)) {
				return DateUtils.parseSqlTime(str);
			}
			// TODO: 时间类型的格式化末班需要在定义一些
			return str;
		} else {
			if ("int".equalsIgnoreCase(type) || "Integer".equalsIgnoreCase(type)) {
				return Integer.parseInt(str);
			} else if ("long".equalsIgnoreCase(type)) {
				return Long.parseLong(str);
			} else if ("float".equalsIgnoreCase(type)) {
				return Float.parseFloat(str);
			} else if ("double".equalsIgnoreCase(type)) {
				return Double.parseDouble(str);
			} else if ("short".equalsIgnoreCase(type)) {
				return Short.parseShort(str);
			} else if ("boolean".equalsIgnoreCase(type)) {
				return Boolean.parseBoolean(str);
			} else if ("byte".equalsIgnoreCase(type)) {
				return Byte.parseByte(str);
			} else if ("char".equalsIgnoreCase(type)) {
				return str.charAt(0);
			} else if ("dateTime".equalsIgnoreCase(type)) {
				return DateUtils.parseDate(str);
			} else if ("date".equalsIgnoreCase(type)) {
				return DateUtils.parseSqlDate(str);
			} else if ("time".equalsIgnoreCase(type)) {
				return DateUtils.parseSqlTime(str);
			}
			return str;
		}
	}
}
