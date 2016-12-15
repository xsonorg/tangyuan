package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.transaction.XTransactionDefinition;

public abstract class AbstractSqlNode extends AbstractServiceNode {

	protected TangYuanNode				sqlNode;

	protected String					dsKey;

	protected XTransactionDefinition	txDef;

	protected boolean					simple;

	public XTransactionDefinition getTxDef() {
		return txDef;
	}

	public String getDsKey() {
		return dsKey;
	}

	public boolean isSimple() {
		return simple;
	}

	// public Object getResult(SqlServiceContext context, Map<String, Object> arg) {
	// Object result = context.getResult();
	// context.setResult(null);// 清理
	// return result;
	// }

}
