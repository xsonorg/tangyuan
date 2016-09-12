package org.xson.tangyuan.ognl.expr;

import org.xson.tangyuan.ognl.OgnlException;

public class ExprCompare {

	public static boolean numberCompareEqual(Object var1, int type1, Object var2, int type2) {
		if (ExprUnit.valueType3 == type1) {
			int x = ((Integer) var1).intValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x == y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x == y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x == y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x == y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x == y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x == y;
			}
		} else if (ExprUnit.valueType4 == type1) {
			long x = ((Long) var1).longValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x == y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x == y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x == y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x == y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x == y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x == y;
			}
		} else if (ExprUnit.valueType6 == type1) {
			double x = ((Double) var1).doubleValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x == y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x == y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x == y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x == y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x == y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x == y;
			}
		} else if (ExprUnit.valueType5 == type1) {
			float x = ((Float) var1).floatValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x == y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x == y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x == y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x == y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x == y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x == y;
			}
		} else if (ExprUnit.valueType2 == type1) {
			short x = ((Short) var1).shortValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x == y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x == y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x == y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x == y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x == y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x == y;
			}
		} else if (ExprUnit.valueType1 == type1) {
			byte x = ((Byte) var1).byteValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x == y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x == y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x == y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x == y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x == y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x == y;
			}
		}
		throw new OgnlException("numberCompareEqual");// TODO
	}

	public static boolean numberCompareNotEqual(Object var1, int type1, Object var2, int type2) {
		if (ExprUnit.valueType3 == type1) {
			int x = ((Integer) var1).intValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x != y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x != y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x != y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x != y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x != y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x != y;
			}
		} else if (ExprUnit.valueType4 == type1) {
			long x = ((Long) var1).longValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x != y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x != y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x != y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x != y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x != y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x != y;
			}
		} else if (ExprUnit.valueType6 == type1) {
			double x = ((Double) var1).doubleValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x != y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x != y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x != y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x != y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x != y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x != y;
			}
		} else if (ExprUnit.valueType5 == type1) {
			float x = ((Float) var1).floatValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x != y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x != y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x != y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x != y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x != y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x != y;
			}
		} else if (ExprUnit.valueType2 == type1) {
			short x = ((Short) var1).shortValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x != y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x != y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x != y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x != y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x != y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x != y;
			}
		} else if (ExprUnit.valueType1 == type1) {
			byte x = ((Byte) var1).byteValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x != y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x != y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x != y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x != y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x != y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x != y;
			}
		}
		throw new OgnlException("numberCompareNotEqual");// TODO
	}

	public static boolean numberCompareMoreThan(Object var1, int type1, Object var2, int type2) {
		if (ExprUnit.valueType3 == type1) {
			int x = ((Integer) var1).intValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x > y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x > y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x > y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x > y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x > y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x > y;
			}
		} else if (ExprUnit.valueType4 == type1) {
			long x = ((Long) var1).longValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x > y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x > y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x > y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x > y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x > y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x > y;
			}
		} else if (ExprUnit.valueType6 == type1) {
			double x = ((Double) var1).doubleValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x > y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x > y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x > y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x > y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x > y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x > y;
			}
		} else if (ExprUnit.valueType5 == type1) {
			float x = ((Float) var1).floatValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x > y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x > y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x > y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x > y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x > y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x > y;
			}
		} else if (ExprUnit.valueType2 == type1) {
			short x = ((Short) var1).shortValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x > y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x > y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x > y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x > y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x > y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x > y;
			}
		} else if (ExprUnit.valueType1 == type1) {
			byte x = ((Byte) var1).byteValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x > y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x > y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x > y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x > y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x > y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x > y;
			}
		}
		throw new OgnlException("numberCompareMoreThan");// TODO
	}

	public static boolean numberCompareGreaterThanOrEqual(Object var1, int type1, Object var2, int type2) {
		if (ExprUnit.valueType3 == type1) {
			int x = ((Integer) var1).intValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x >= y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x >= y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x >= y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x >= y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x >= y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x >= y;
			}
		} else if (ExprUnit.valueType4 == type1) {
			long x = ((Long) var1).longValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x >= y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x >= y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x >= y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x >= y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x >= y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x >= y;
			}
		} else if (ExprUnit.valueType6 == type1) {
			double x = ((Double) var1).doubleValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x >= y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x >= y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x >= y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x >= y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x >= y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x >= y;
			}
		} else if (ExprUnit.valueType5 == type1) {
			float x = ((Float) var1).floatValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x >= y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x >= y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x >= y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x >= y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x >= y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x >= y;
			}
		} else if (ExprUnit.valueType2 == type1) {
			short x = ((Short) var1).shortValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x >= y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x >= y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x >= y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x >= y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x >= y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x >= y;
			}
		} else if (ExprUnit.valueType1 == type1) {
			byte x = ((Byte) var1).byteValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x >= y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x >= y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x >= y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x >= y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x >= y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x >= y;
			}
		}
		throw new OgnlException("numberCompareGreaterThanOrEqual");// TODO
	}

	public static boolean numberCompareLessThan(Object var1, int type1, Object var2, int type2) {
		if (ExprUnit.valueType3 == type1) {
			int x = ((Integer) var1).intValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x < y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x < y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x < y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x < y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x < y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x < y;
			}
		} else if (ExprUnit.valueType4 == type1) {
			long x = ((Long) var1).longValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x < y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x < y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x < y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x < y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x < y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x < y;
			}
		} else if (ExprUnit.valueType6 == type1) {
			double x = ((Double) var1).doubleValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x < y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x < y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x < y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x < y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x < y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x < y;
			}
		} else if (ExprUnit.valueType5 == type1) {
			float x = ((Float) var1).floatValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x < y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x < y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x < y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x < y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x < y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x < y;
			}
		} else if (ExprUnit.valueType2 == type1) {
			short x = ((Short) var1).shortValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x < y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x < y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x < y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x < y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x < y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x < y;
			}
		} else if (ExprUnit.valueType1 == type1) {
			byte x = ((Byte) var1).byteValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x < y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x < y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x < y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x < y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x < y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x < y;
			}
		}
		// TODO
		throw new OgnlException("numberCompareLessThan");
	}

	public static boolean numberCompareLessThanOrEqual(Object var1, int type1, Object var2, int type2) {
		if (ExprUnit.valueType3 == type1) {
			int x = ((Integer) var1).intValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x <= y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x <= y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x <= y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x <= y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x <= y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x <= y;
			}
		} else if (ExprUnit.valueType4 == type1) {
			long x = ((Long) var1).longValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x <= y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x <= y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x <= y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x <= y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x <= y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x <= y;
			}
		} else if (ExprUnit.valueType6 == type1) {
			double x = ((Double) var1).doubleValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x <= y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x <= y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x <= y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x <= y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x <= y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x <= y;
			}
		} else if (ExprUnit.valueType5 == type1) {
			float x = ((Float) var1).floatValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x <= y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x <= y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x <= y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x <= y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x <= y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x <= y;
			}
		} else if (ExprUnit.valueType2 == type1) {
			short x = ((Short) var1).shortValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x <= y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x <= y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x <= y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x <= y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x <= y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x <= y;
			}
		} else if (ExprUnit.valueType1 == type1) {
			byte x = ((Byte) var1).byteValue();
			if (ExprUnit.valueType3 == type2) {
				int y = ((Integer) var2).intValue();
				return x <= y;
			} else if (ExprUnit.valueType4 == type2) {
				long y = ((Long) var2).longValue();
				return x <= y;
			} else if (ExprUnit.valueType6 == type2) {
				double y = ((Double) var2).doubleValue();
				return x <= y;
			} else if (ExprUnit.valueType5 == type2) {
				float y = ((Float) var2).floatValue();
				return x <= y;
			} else if (ExprUnit.valueType2 == type2) {
				short y = ((Short) var2).shortValue();
				return x <= y;
			} else if (ExprUnit.valueType1 == type2) {
				byte y = ((Byte) var2).byteValue();
				return x <= y;
			}
		}
		throw new OgnlException("numberCompareLessThanOrEqual");// TODO
	}
}
