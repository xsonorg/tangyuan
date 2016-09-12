package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.executor.SqlServiceContext;
import org.xson.tangyuan.ognl.Ognl;

public class SetVarNode implements SqlNode {

	// 只需要支持最简单的key
	private String	key;

	private Object	value;

	// type="Integer"只有在变量的时候才有意义
	private boolean	constant	= true;

	public SetVarNode(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public boolean execute(SqlServiceContext context, Object arg) {
		if (constant) {
			// arg.put(key, value);
			Ognl.setValue(arg, key, value);
		}
		// TODO 变量到变量咱不需要考虑
		return true;
	}
}
