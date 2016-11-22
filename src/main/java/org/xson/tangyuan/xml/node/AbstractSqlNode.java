package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.transaction.XTransactionDefinition;

public abstract class AbstractSqlNode implements SqlNode {

	protected String					id;

	/**
	 * 命名空间
	 */
	protected String					ns;

	/**
	 * 命名空间+ID
	 */
	protected String					serviceKey;

	/**
	 * 是否是简单的sql,还是sqlService
	 */
	protected boolean					simple;

	// 这里的是必须的
	protected String					dsKey;

	protected SqlNode					sqlNode;

	protected XTransactionDefinition	txDef;

	protected Class<?>					resultType	= null;

	public String getId() {
		return id;
	}

	public String getNs() {
		return ns;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public boolean isSimple() {
		return simple;
	}

	public String getDsKey() {
		return dsKey;
	}

	public XTransactionDefinition getTxDef() {
		return txDef;
	}

	public Class<?> getResultType() {
		return resultType;
	}

	/**
	 * 获取返回对象
	 */
	public Object getResult(ServiceContext context) {
		Object result = context.getResult();
		context.setResult(null);// 清理
		return result;
	}

	protected String getSlowServiceLog(long startTime) {
		long intervals = System.currentTimeMillis() - startTime;
		String slowLogInfo = "(";
		if (intervals >= 1000L) {
			slowLogInfo = slowLogInfo + "5level slow sql service ";
		} else if (intervals >= 500L) {
			slowLogInfo = slowLogInfo + "4level slow sql service ";
		} else if (intervals >= 300L) {
			slowLogInfo = slowLogInfo + "3level slow sql service ";
		} else if (intervals >= 200L) {
			slowLogInfo = slowLogInfo + "2level slow sql service ";
		} else if (intervals >= 100L) {
			slowLogInfo = slowLogInfo + "1level slow sql service ";
		}
		slowLogInfo = slowLogInfo + intervals + "ms)";
		return slowLogInfo;
	}

	// public Object getResult(SqlServiceContext context, Map<String, Object> arg) {
	// Object result = context.getResult();
	// context.setResult(null);// 清理
	// return result;
	// }

}
