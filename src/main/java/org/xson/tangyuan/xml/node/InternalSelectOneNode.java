package org.xson.tangyuan.xml.node;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.vo.CacheUseVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.ognl.Ognl;

/**
 * 内部的SelectOneNode
 */
public class InternalSelectOneNode extends AbstractSqlNode {

	private static Log	log	= LogFactory.getLog(InternalSelectOneNode.class);

	private String		resultKey;
	private CacheUseVo	cacheUse;

	public InternalSelectOneNode(String dsKey, String resultKey, SqlNode sqlNode, Class<?> resultType, CacheUseVo cacheUse) {
		this.dsKey = dsKey;
		this.resultKey = resultKey;
		this.sqlNode = sqlNode;
		this.resultType = resultType;
		this.simple = false;
		this.cacheUse = cacheUse;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {

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

		Object result = null;
		// if (XCO.class == context.getResultType(resultType)) {
		if (XCO.class == resultType) {
			result = context.executeSelectOneXCO(this, null, null);
		} else {
			result = context.executeSelectOneMap(this, null, null);
		}
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
