package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.cache.vo.CacheCleanVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.executor.SqlServiceExceptionInfo;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.transaction.XTransactionDefinition;

public class DeleteNode extends AbstractSqlNode {

	private static Log		log	= LogFactory.getLog(DeleteNode.class);

	private CacheCleanVo	cacheClean;

	public DeleteNode(String id, String ns, String serviceKey, String dsKey, XTransactionDefinition txDef, SqlNode sqlNode, CacheCleanVo cacheClean) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.dsKey = dsKey;
		this.sqlNode = sqlNode;
		this.txDef = txDef;
		this.simple = true;
		this.cacheClean = cacheClean;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {
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
			context.beforeExecute(this); // 开启事务异常,可认为是事务之前的异常
		} catch (Throwable e) {
			// 考虑此处设置当前事务的特征, 上层统一处理(无论如何)
			// SqlServiceException ex = new SqlServiceException("简单服务,事务启动之前发生异常", e);
			// ex.setExPosition(ExceptionPosition.BEFORE);
			// ex.setNewTranscation(txDef.isNewTranscation());
			// throw ex;
			// SqlServiceExceptionInfo exceptionInfo = new SqlServiceExceptionInfo();
			// exceptionInfo.setNewTranscation(txDef.isNewTranscation());
			// exceptionInfo.setCreatedTranscation(false);
			// context.setExceptionInfo(exceptionInfo);
			// throw e;
			context.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), false));
			throw e;
		}

		try {
			// 4. 执行SQL
			int result = context.executeDelete(this);
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
			// SqlServiceExceptionInfo exceptionInfo = new SqlServiceExceptionInfo();
			// exceptionInfo.setNewTranscation(txDef.isNewTranscation());
			// exceptionInfo.setCreatedTranscation(true);
			// context.setExceptionInfo(exceptionInfo);
			context.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), true));
			throw e;
		}

		if (null != cacheClean) {
			cacheClean.removeObject(arg);
		}

		return true;
	}

}
