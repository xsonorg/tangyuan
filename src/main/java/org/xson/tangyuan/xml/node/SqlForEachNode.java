package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.ognl.vars.Variable;

public class SqlForEachNode extends ForEachNode {

	public SqlForEachNode(TangYuanNode sqlNode, Variable collection, String index, String open, String close, String separator) {
		this.sqlNode = sqlNode;
		this.collection = collection;
		this.index = index;
		this.open = open;
		this.close = close;
		this.separator = separator;
	}

	protected void append(ServiceContext context, String str) {
		if (null != str && str.length() > 0) {
			context.getSqlServiceContext().addSql(str);
		}
	}
}
