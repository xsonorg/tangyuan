package org.xson.tangyuan.xml.node;

import java.util.Collection;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.SqlServiceContext;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.ognl.vars.VariableVo;

public class ForEachNode implements SqlNode {

	protected static Log	log	= LogFactory.getLog(ForEachNode.class);

	private SqlNode			sqlNode;

	/**
	 * 集合变量, 从那个集合中遍历, 可为空,但是此种情况下必须有sqlNode
	 */
	private VariableVo		collection;

	/**
	 * 集合中的索引
	 */
	private String			index;

	private String			open;

	private String			close;

	private String			separator;

	public ForEachNode(SqlNode sqlNode, VariableVo collection, String index, String open, String close, String separator) {
		this.sqlNode = sqlNode;
		this.collection = collection;
		this.index = index;
		this.open = open;
		this.close = close;
		this.separator = separator;
	}

	@Override
	public boolean execute(SqlServiceContext context, Object arg) throws Throwable {
		int count = 0;
		Object obj = collection.getValue(arg);
		append(context, open);

		if (null != obj && obj instanceof Collection) {
			count = foreachCollection(obj, context, arg);
		} else if (null != obj && obj.getClass().isArray()) {
			Class<?> clazz = obj.getClass();
			if (int[].class == clazz) {
				count = foreachIntArray(obj, context, arg);
			} else if (long[].class == clazz) {
				count = foreachLongArray(obj, context, arg);
			} else if (float[].class == clazz) {
				count = foreachFloatArray(obj, context, arg);
			} else if (double[].class == clazz) {
				count = foreachDoubleArray(obj, context, arg);
			} else if (byte[].class == clazz) {
				count = foreachByteArray(obj, context, arg);
			} else if (short[].class == clazz) {
				count = foreachShortArray(obj, context, arg);
			} else if (boolean[].class == clazz) {
				count = foreachBooleanArray(obj, context, arg);
			} else if (char[].class == clazz) {
				count = foreachCharArray(obj, context, arg);
			} else {
				count = foreachObjectArray(obj, context, arg);
			}
		} else {
			throw new TangYuanException("ForEachNode: 获取对象非集合获取集合元素为空");
		}

		if (0 == count) {
			throw new TangYuanException("ForEachNode: 获取对象非集合获取集合元素为空");
		}

		append(context, close);

		// log.info("ForEach Length:" + count);

		return true;
	}

	private void append(SqlServiceContext context, String str) {
		if (null != str && str.length() > 0) {
			context.addSql(str);
		}
	}

	private int foreachCollection(Object target, SqlServiceContext context, Object arg) throws Throwable {
		Collection<?> collection = (Collection<?>) target;
		int count = 0;
		for (Object item : collection) {
			if (null == item) {
				throw new TangYuanException("foreache中某一元素为空");// TODO
			}
			// arg.put(index, count);
			Ognl.setValue(arg, index, count);
			if (count++ > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return count;
	}

	/**
	 * @param iterate
	 *            true: 遍历模式
	 */
	private int foreachObjectArray(Object target, SqlServiceContext context, Object arg) throws Throwable {
		Object[] array = (Object[]) target;
		int count = 0;
		for (int i = 0; i < array.length; i++) {
			// arg.put(index, count);
			Ognl.setValue(arg, index, count);
			if (count++ > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return count;
	}

	private int foreachIntArray(Object target, SqlServiceContext context, Object arg) throws Throwable {
		int[] array = (int[]) target;
		for (int i = 0; i < array.length; i++) {
			// arg.put(index, i);
			Ognl.setValue(arg, index, i);
			if (i > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return array.length;
	}

	private int foreachLongArray(Object target, SqlServiceContext context, Object arg) throws Throwable {
		long[] array = (long[]) target;
		for (int i = 0; i < array.length; i++) {
			// arg.put(index, i);
			Ognl.setValue(arg, index, i);
			if (i > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return array.length;
	}

	private int foreachBooleanArray(Object target, SqlServiceContext context, Object arg) throws Throwable {
		boolean[] array = (boolean[]) target;
		for (int i = 0; i < array.length; i++) {
			// arg.put(index, i);
			Ognl.setValue(arg, index, i);
			if (i > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return array.length;
	}

	private int foreachByteArray(Object target, SqlServiceContext context, Object arg) throws Throwable {
		byte[] array = (byte[]) target;
		for (int i = 0; i < array.length; i++) {
			// arg.put(index, i);
			Ognl.setValue(arg, index, i);
			if (i > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return array.length;
	}

	private int foreachCharArray(Object target, SqlServiceContext context, Object arg) throws Throwable {
		char[] array = (char[]) target;
		for (int i = 0; i < array.length; i++) {
			// arg.put(index, i);
			Ognl.setValue(arg, index, i);
			if (i > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return array.length;
	}

	private int foreachDoubleArray(Object target, SqlServiceContext context, Object arg) throws Throwable {
		double[] array = (double[]) target;
		for (int i = 0; i < array.length; i++) {
			// arg.put(index, i);
			Ognl.setValue(arg, index, i);
			if (i > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return array.length;
	}

	private int foreachFloatArray(Object target, SqlServiceContext context, Object arg) throws Throwable {
		float[] array = (float[]) target;
		for (int i = 0; i < array.length; i++) {
			Ognl.setValue(arg, index, i);
			if (i > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return array.length;
	}

	private int foreachShortArray(Object target, SqlServiceContext context, Object arg) throws Throwable {
		short[] array = (short[]) target;
		for (int i = 0; i < array.length; i++) {
			Ognl.setValue(arg, index, i);
			if (i > 0) {
				append(context, separator);
			}
			sqlNode.execute(context, arg);
		}
		return array.length;
	}

}
