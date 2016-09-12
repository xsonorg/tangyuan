package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.cache.vo.CacheUseVo;
import org.xson.tangyuan.executor.SqlServiceContext;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.ognl.Ognl;

/**
 * 内部的SelectVarNode
 */
public class InternalSelectVarNode extends AbstractSqlNode {

	private static Log	log	= LogFactory.getLog(InternalSelectVarNode.class);

	// 返回结果的key
	private String		resultKey;
	private CacheUseVo	cacheUse;

	public InternalSelectVarNode(String dsKey, String resultKey, SqlNode sqlNode, CacheUseVo cacheUse) {
		this.dsKey = dsKey;
		this.resultKey = resultKey;
		this.sqlNode = sqlNode;
		this.simple = false;
		this.cacheUse = cacheUse;
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

		context.resetExecEnv();

		sqlNode.execute(context, arg); // 获取sql
		if (log.isInfoEnabled()) {
			context.parseSqlLog();
		}

		long startTime = System.currentTimeMillis();
		context.beforeExecute(this, true);// 只是打开连接

		Object result = context.executeSelectVar(this);
		if (null != this.resultKey) {
			Ognl.setValue(arg, this.resultKey, result);
		}

		context.afterExecute(this);

		if (log.isInfoEnabled()) {
			log.info("sql execution time: " + getSlowServiceLog(startTime));
		}

		if (null != cacheUse) {
			cacheUse.putObject(arg, result);
		}

		return true;
	}

}
