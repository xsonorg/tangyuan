package org.xson.tangyuan.util;

import java.util.regex.Pattern;

public class NumberUtils {

	public static boolean isNumber(String var) {
		return var.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	}

	public static boolean isInteger(String var) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(var).matches();
	}

	public static Object parseNumber(String var) {
		Object value = null;
		if (var.indexOf(".") == -1) {
			try {
				value = Integer.parseInt(var);
			} catch (NumberFormatException e) {
				value = Long.parseLong(var);
			}
		} else {
			try {
				value = Float.parseFloat(var);
			} catch (NumberFormatException e) {
				value = Double.parseDouble(var);
			}
		}
		return value;
	}

	public static boolean randomSuccess() {
		int x = (int) (Math.random() * 100);
		return x > 10;
	}

	public static boolean randomFailure() {
		return !randomSuccess();
	}

}
