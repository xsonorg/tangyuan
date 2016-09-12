package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.cache.vo.CacheCleanVo;
import org.xson.tangyuan.executor.SqlServiceContext;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.ognl.Ognl;

/**
 * 内部的UpdateNode
 */
public class InternalUpdateNode extends AbstractSqlNode {

	private static Log		log	= LogFactory.getLog(InternalUpdateNode.class);

	// 返回结果的key
	private String			resultKey;
	private CacheCleanVo	cacheClean;

	public InternalUpdateNode(String dsKey, String rowCount, SqlNode sqlNode, CacheCleanVo cacheClean) {
		this.dsKey = dsKey;
		this.resultKey = rowCount;
		this.sqlNode = sqlNode;
		this.simple = false;
		this.cacheClean = cacheClean;
	}

	@Override
	public boolean execute(SqlServiceContext context, Object arg) throws Throwable {
		context.resetExecEnv();

		sqlNode.execute(context, arg); // 获取sql
		if (log.isInfoEnabled()) {
			context.parseSqlLog();
		}

		long startTime = System.currentTimeMillis();
		context.beforeExecute(this, true);// 只是打开连接

		int count = context.executeUpdate(this);
		if (null != this.resultKey) {
			// arg.put(this.resultKey, count);
			Ognl.setValue(arg, this.resultKey, count);
		}

		context.afterExecute(this);

		if (log.isInfoEnabled()) {
			log.info("sql execution time: " + getSlowServiceLog(startTime));
		}
		
		if (null != cacheClean) {
			cacheClean.removeObject(arg);
		}

		
		return true;
	}

}
