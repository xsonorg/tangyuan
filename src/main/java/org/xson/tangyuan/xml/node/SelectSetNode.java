package org.xson.tangyuan.xml.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.vo.CacheUseVo;
import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.executor.SqlServiceExceptionInfo;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.ognl.bean.OgnlBean;
import org.xson.tangyuan.transaction.XTransactionDefinition;

public class SelectSetNode extends AbstractSqlNode {

	private static Log	log			= LogFactory.getLog(SelectSetNode.class);

	private MappingVo	resultMap	= null;

	private Integer		fetchSize;

	private CacheUseVo	cacheUse;

	public SelectSetNode(String id, String ns, String serviceKey, Class<?> resultType, MappingVo resultMap, String dsKey, Integer fetchSize,
			XTransactionDefinition txDef, SqlNode sqlNode, CacheUseVo cacheUse) {

		this.id = id;
		this.ns = ns;
		this.serviceKey = serviceKey;

		this.resultType = resultType;
		this.resultMap = resultMap;

		this.dsKey = dsKey;
		this.fetchSize = fetchSize;
		this.sqlNode = sqlNode;
		this.txDef = txDef;

		this.cacheUse = cacheUse;

		this.simple = true;
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
			// 考虑此处设置当前事务的特征, 上层统一处理(无论如何)
			// SqlServiceException ex = new SqlServiceException("简单服务,事务启动之前发生异常", e);
			// ex.setExPosition(ExceptionPosition.BEFORE);
			// ex.setNewTranscation(txDef.isNewTranscation());
			// throw ex;
			context.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), false));
			throw e;
		}

		Object result = null;
		try {
			// 4. 执行SQL
			if (XCO.class == resultType) {
				result = context.executeSelectSetListXCO(this, this.resultMap, fetchSize);
			} else {
				result = context.executeSelectSetListMap(this, this.resultMap, fetchSize);
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
	public Object getResult(ServiceContext context) {
		Object value = context.getResult();
		context.setResult(null);
		if (null == value) {
			return value;
		}
		try {
			if (null == resultMap) {
				if (XCO.class == resultType || Map.class == resultType) {
					// List<XCO>, List<Map>
					return value;
				} else {
					// List<Bean>, 最基本的bean:属性名和列名一直, 从数据库以Map接受方便
					List<Object> result = new ArrayList<Object>();
					List<Map<String, Object>> dataList = (List<Map<String, Object>>) value;
					int size = dataList.size();
					for (int i = 0; i < size; i++) {
						Map<String, Object> data = dataList.get(i);
						result.add(OgnlBean.mapToBean(data, resultType));
					}
					return result;
				}
			} else {
				Class<?> beanClass = resultMap.getBeanClass();
				if (null == beanClass && XCO.class != resultType && Map.class != resultType) {
					beanClass = resultType;
				}
				// 在数据库查询的时候就需要做列的映射, 这里只做toBean(如果需要的话)的映射
				if (null != beanClass) {
					List<Object> result = new ArrayList<Object>();
					List<Map<String, Object>> dataList = (List<Map<String, Object>>) value;
					int size = dataList.size();
					for (int i = 0; i < size; i++) {
						Map<String, Object> data = dataList.get(i);
						result.add(OgnlBean.mapToBean(data, beanClass));
					}
					return result;
				}
				// List<XCO>, List<Map>
				return value;
			}
		} catch (Throwable e) {
			// SqlServiceException ex = new SqlServiceException("简单服务,事务处理后异常", e);
			// ex.setExPosition(ExceptionPosition.AFTER);
			// ex.setNewTranscation(txDef.isNewTranscation());
			// throw ex;
			// 这里认为是没有没开启, 因为只有独立事务,执行中才需要回滚当前，其他都是回滚所有
			context.setExceptionInfo(new SqlServiceExceptionInfo(txDef.isNewTranscation(), false));
			throw e;
		}
	}
}
