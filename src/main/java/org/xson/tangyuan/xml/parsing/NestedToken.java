package org.xson.tangyuan.xml.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NestedToken {

	private List<Object>	parts;

	public void addPart(Object part) {
		if (null == parts) {
			parts = new ArrayList<Object>();
		}
		parts.add(part);
	}

	public String getValue(Map<String, Object> args) {
		StringBuilder sb = new StringBuilder();
		int length = parts.size();
		for (int i = 0; i < length; i++) {
			Object tmp = parts.get(i);
			if (tmp instanceof String) {
				sb.append(tmp);
			} else {
				sb.append(((NestedToken) tmp).getValue(args));
			}
		}
		// TODO 在这里取值 以后设置el表达式
		String key = sb.toString().toString().trim();
		System.out.println("key:" + key);
		// return (String) args.get(key);
		return key;
	}
}
