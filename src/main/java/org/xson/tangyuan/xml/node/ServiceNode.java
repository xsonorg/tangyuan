package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.cache.vo.CacheCleanVo;
import org.xson.tangyuan.cache.vo.CacheUseVo;
import org.xson.tangyuan.executor.SqlServiceContext;
import org.xson.tangyuan.executor.SqlServiceExceptionInfo;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.transaction.XTransactionDefinition;

public class ServiceNode extends AbstractSqlNode {

	private static Log		log	= LogFactory.getLog(ServiceNode.class);

	private CacheUseVo		cacheUse;

	private CacheCleanVo	cacheClean;

	public ServiceNode(String id, String ns, String serviceKey, String dsKey, XTransactionDefinition txDef, SqlNode sqlNode, CacheUseVo cacheUse,
			CacheCleanVo cacheClean, Class<?> resultType) {
		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;
		this.dsKey = dsKey;
		this.sqlNode = sqlNode;
		this.txDef = txDef;
		this.simple = false;

		this.cacheUse = cacheUse;
		this.cacheClean = cacheClean;

		this.resultType = resultType;
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

		// 设置同进同出的类型
		// context.setResultType(arg);

		long startTime = 0L;
		try {
			startTime = System.currentTimeMillis();
			// 这里只是创建事务
			context.beforeExecute(this, false);
			log.info("start trans: " + this.txDef.getId());
		} catch (Throwable e) {
			// SqlServiceException ex = new SqlServiceException("组合服务,事务启动之前发生异常", e);
			// ex.setExPosition(ExceptionPosition.BEFORE);
			// ex.setNewTranscation(txDef.isNewTranscation());
			// throw ex;
			context.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), false));
			throw e;
		}

		try {
			sqlNode.execute(context, arg);
			context.commit(false);
			context.afterExecute(this);
			if (log.isInfoEnabled()) {
				log.info("sql execution time: " + getSlowServiceLog(startTime));
			}
		} catch (Throwable e) {
			// SqlServiceException ex = new SqlServiceException("组合服务,事务处理中异常", e);
			// ex.setExPosition(ExceptionPosition.AMONG);
			// ex.setNewTranscation(txDef.isNewTranscation());
			// throw ex;

			// SqlServiceException ex = null;
			// if (e instanceof SqlServiceException) {
			// ex = (SqlServiceException) e;
			// } else {
			// ex = new SqlServiceException("组合服务,事务处理中异常", e);
			// }
			// ex.setExPosition(ExceptionPosition.AMONG);
			// ex.setNewTranscation(txDef.isNewTranscation());
			// throw ex;

			context.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), true));
			throw e;
		}

		if (null != cacheUse) {
			cacheUse.putObject(arg, context.getResult());
		}
		if (null != cacheClean) {
			cacheClean.removeObject(arg);
		}

		return true;
	}
}
