package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.cache.vo.CacheUseVo;
import org.xson.tangyuan.executor.SqlServiceContext;
import org.xson.tangyuan.executor.SqlServiceExceptionInfo;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.transaction.XTransactionDefinition;

public class SelectVarNode extends AbstractSqlNode {

	private static Log	log	= LogFactory.getLog(SelectVarNode.class);

	private CacheUseVo	cacheUse;

	public SelectVarNode(String id, String ns, String serviceKey, String dsKey, XTransactionDefinition txDef, SqlNode sqlNode, CacheUseVo cacheUse) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.dsKey = dsKey;
		this.sqlNode = sqlNode;
		this.txDef = txDef;

		this.cacheUse = cacheUse;

		this.simple = true;
	}

	@Override
	public boolean execute(SqlServiceContext context, Object arg) throws Throwable {
		// 1. cache使用
		if (null != cacheUse) {
			Object result = cacheUse.getObject(arg);
			if (null != result) {
				context.setResult(result);
				return true;
			}
		}

		// 2. 清理和重置执行环境
		context.resetExecEnv();

		long startTime = 0L;
		try {
			// 3. 解析SQL
			sqlNode.execute(context, arg); // 获取sql
			if (log.isInfoEnabled()) {
				context.parseSqlLog();
			}
			// 3.1 开启事务
			startTime = System.currentTimeMillis();
			context.beforeExecute(this); // 开启事务异常, 可认为是事务之前的异常
		} catch (Throwable e) {
			// SqlServiceException ex = new SqlServiceException("简单服务,事务启动之前发生异常", e);
			// ex.setExPosition(ExceptionPosition.BEFORE);
			// ex.setNewTranscation(txDef.isNewTranscation());
			// throw ex;
			context.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), false));
			throw e;
		}

		Object result = null;
		try {
			result = context.executeSelectVar(this);
			context.setResult(result);
			context.commit(false); // 这里做不确定的提交
			context.afterExecute(this);
			if (log.isInfoEnabled()) {
				log.info("sql execution time: " + getSlowServiceLog(startTime));
			}
		} catch (Throwable e) {
			// SqlServiceException ex = new SqlServiceException("简单服务,事务处理中异常", e);
			// ex.setExPosition(ExceptionPosition.AMONG);
			// ex.setNewTranscation(txDef.isNewTranscation());
			// throw ex;
			context.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), true));
			throw e;
		}

		if (null != cacheUse) {
			cacheUse.putObject(arg, result);
		}

		return true;
	}

}
