package org.xson.tangyuan.util;

import org.xson.tangyuan.TangYuanContainer;

public class TYUtils {

	public static String getFullId(String ns, String id) {
		if (null == ns || "".equals(ns)) {
			return id;
		}
		return ns + TangYuanContainer.getInstance().getNsSeparator() + id;
	}
}
