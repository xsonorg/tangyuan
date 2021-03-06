package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.cache.vo.CacheCleanVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.ognl.Ognl;
import org.xson.tangyuan.type.InsertReturn;

/**
 * 内部的InsertNode
 */
public class InternalInsertNode extends AbstractSqlNode {

	private static Log		log	= LogFactory.getLog(InternalInsertNode.class);

	// 返回影响行数的key
	private String			resultKey;
	// 返回的自增key, 有可能是多个
	private String			incrementKey;
	private CacheCleanVo	cacheClean;

	public InternalInsertNode(String dsKey, String rowCount, String incrementKey, SqlNode sqlNode, CacheCleanVo cacheClean) {
		this.dsKey = dsKey;
		this.resultKey = rowCount;
		this.incrementKey = incrementKey;
		this.sqlNode = sqlNode;
		this.simple = false;
		this.cacheClean = cacheClean;
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) throws Throwable {
		context.resetExecEnv();

		sqlNode.execute(context, arg); // 获取sql
		if (log.isInfoEnabled()) {
			context.parseSqlLog();
		}

		long startTime = System.currentTimeMillis();
		context.beforeExecute(this, true);// 只是打开连接

		if (null == this.incrementKey) {
			int count = context.executeInsert(this);
			if (null != this.resultKey) {
				Ognl.setValue(arg, this.resultKey, count);
			}
		} else {
			InsertReturn insertReturn = context.executeInsertReturn(this);
			if (null != this.resultKey) {
				Ognl.setValue(arg, this.resultKey, insertReturn.getRowCount());
			}
			Object columns = insertReturn.getColumns();
			Ognl.setValue(arg, this.incrementKey, columns);
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
