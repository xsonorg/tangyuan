package org.xson.tangyuan.xml.node;

import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.vo.CacheUseVo;
import org.xson.tangyuan.executor.SqlServiceContext;
import org.xson.tangyuan.executor.SqlServiceExceptionInfo;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.ognl.bean.OgnlBean;
import org.xson.tangyuan.transaction.XTransactionDefinition;

public class SelectOneNode extends AbstractSqlNode {

	private static Log	log			= LogFactory.getLog(SelectOneNode.class);

	private MappingVo	resultMap	= null;

	private CacheUseVo	cacheUse;

	public SelectOneNode(String id, String ns, String serviceKey, Class<?> resultType, MappingVo resultMap, String dsKey,
			XTransactionDefinition txDef, SqlNode sqlNode, CacheUseVo cacheUse) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.resultType = resultType;
		this.resultMap = resultMap;

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
			if (XCO.class == resultType) {
				result = context.executeSelectOneXCO(this, this.resultMap, null);
			} else {
				result = context.executeSelectOneMap(this, this.resultMap, null);
			}
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

	@Override
	public Class<?> getResultType() {
		if (null != this.resultType) {
			return this.resultType;
		}
		return this.resultMap.getBeanClass();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object getResult(SqlServiceContext context) {
		Object value = context.getResult();
		context.setResult(null);
		if (null == value) {
			return value;
		}
		try {
			if (null == resultMap) {
				if (XCO.class == resultType || Map.class == resultType) {
					return value;// 原始的
				} else {
					return OgnlBean.mapToBean((Map<String, Object>) value, resultType);
				}
			} else {
				Class<?> beanClass = resultMap.getBeanClass();
				if (null == beanClass && XCO.class != resultType && Map.class != resultType) {
					beanClass = resultType;
				}
				if (null != beanClass) {
					return OgnlBean.mapToBean((Map<String, Object>) value, beanClass);
				}
				return value;
			}
		} catch (Throwable e) {
			// SqlServiceException ex = new SqlServiceException("简单服务,事务处理后异常", e);
			// ex.setExPosition(ExceptionPosition.AFTER);
			// ex.setNewTranscation(txDef.isNewTranscation());
			// throw ex;
			// 这里认为是没有没开启
			context.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), false));
			throw e;
		}
	}
}
