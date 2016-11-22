package org.xson.tangyuan.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.monitor.SqlServiceContextInfo;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.ognl.vars.VariableVo;
import org.xson.tangyuan.transaction.TangYuanTransactionManager;
import org.xson.tangyuan.transaction.TransactionException;
import org.xson.tangyuan.transaction.XTransactionManager;
import org.xson.tangyuan.transaction.XTransactionStatus;
import org.xson.tangyuan.type.InsertReturn;
import org.xson.tangyuan.xml.node.AbstractSqlNode;

public class ServiceContext {

	private static Log				log					= LogFactory.getLog(ServiceContext.class);

	private StringBuilder			sqlBuilder			= null;

	private String					realSql				= null;

	/**
	 * SQL参数值列表
	 */
	private List<Object>			argList				= null;

	/**
	 * 真正的数据源dsKey
	 */
	private String					realDsKey			= null;

	private SqlActuator				sqlActuator			= null;

	private XTransactionManager		transactionManager	= null;

	private XTransactionStatus		transaction			= null;

	private SqlLog					sqlLog				= null;

	/**
	 * 提交过程发生的异常(临时的, 用完及清楚)
	 */
	private Throwable				exception			= null;
	/**
	 * 异常信息
	 */
	private SqlServiceExceptionInfo	exceptionInfo		= null;

	/**
	 * 结果返回对象:SqlService专用
	 */
	private Object					result				= null;

	// 使用计数器
	protected int					counter				= 1;

	// 监控使用
	private SqlServiceContextInfo	contextInfo			= null;

	/**
	 * 有入参决定的, 可以保证默认情况下同进同出, 用户组合SQL
	 */
	// protected Class<?> resultType = null;

	protected ServiceContext() {
		this.sqlActuator = new SqlActuator(TangYuanContainer.getInstance().getTypeHandlerRegistry());
		this.transactionManager = new TangYuanTransactionManager();
		if (log.isInfoEnabled()) {
			sqlLog = new SqlLog(TangYuanContainer.getInstance().getTypeHandlerRegistry());
		}
		if (TangYuanContainer.getInstance().isServiceMonitor()) {
			this.contextInfo = new SqlServiceContextInfo(this.hashCode());// TODO
			this.contextInfo.joinMonitor();
		}
	}

	// 更新监控信息
	public void updateMonitor(String service) {
		if (null != contextInfo) {
			contextInfo.update(service);
		}
	}

	// 停止监控信息
	public void stopMonitor() {
		if (null != contextInfo) {
			contextInfo.stop();
		}
	}

	public void setExceptionInfo(SqlServiceExceptionInfo exceptionInfo) {
		this.exceptionInfo = exceptionInfo;
	}

	public SqlServiceExceptionInfo getExceptionInfo() {
		return exceptionInfo;
	}

	// public void setResultType(Object arg) {
	// if (null == arg) {
	// this.resultType = null;
	// } else if (XCO.class == arg.getClass()) {
	// this.resultType = XCO.class;
	// } else if (Map.class.isAssignableFrom(arg.getClass())) {
	// this.resultType = Map.class;
	// }
	// this.resultType = null;
	// }
	// public Class<?> getResultType(Class<?> userResultType) {
	// if (null != userResultType) {
	// return userResultType;
	// } else if (null != resultType) {
	// return this.resultType;
	// } else {
	// return TangYuanContainer.getInstance().getDefaultResultType();
	// }
	// }

	/**
	 * 重设执行环境
	 */
	public void resetExecEnv() {
		this.realDsKey = null; // 重设dskey
		this.argList = null; // 重新设置变量
		this.sqlBuilder = new StringBuilder(); // 重新设置sqlBuilder
		this.realSql = null;
	}

	public void addSql(String sql) {
		sqlBuilder.append(sql);
	}

	private String getSql() {
		return sqlBuilder.toString();
	}

	public void setDsKey(String realDsKey) {
		if (null == realDsKey || null == this.realDsKey) {
			this.realDsKey = realDsKey;
		} else if (!this.realDsKey.equals(realDsKey)) {
			throw new TransactionException("暂不支持多dsKey:" + realDsKey);// 只有在分库分表的情况才会出现
		}
	}

	public void parseSqlLog() {
		this.realSql = sqlLog.getSqlLog(getSql(), argList);
	}

	public String getRealSql() {
		return this.realSql;
	}

	public void addStaticVarList(List<VariableVo> varList) {
		addStaticVarList(varList, null);
	}

	// TODO 以后考虑 arg...
	public void addStaticVarList(List<VariableVo> varList, Object arg) {
		if (null == varList) {
			this.argList = null;
			return;
		}

		if (null == this.argList) {
			this.argList = new ArrayList<Object>();
		}

		// for (VariableVo var : varList) {
		// argList.add(var.getValue(arg));
		// }

		for (VariableVo var : varList) {
			Object value = var.getValue(arg);
			if (null == value) {
				// log.error("Field does not exist: " + var.getOriginal());
				throw new TangYuanException("Field does not exist: " + var.getOriginal());
			}
			argList.add(value);
		}
	}

	public List<Map<String, Object>> executeSelectSetListMap(AbstractSqlNode sqlNode, MappingVo resultMap, Integer fetchSize) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		List<Map<String, Object>> result = sqlActuator.selectAllMap(connection, getSql(), argList, resultMap, fetchSize);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public List<XCO> executeSelectSetListXCO(AbstractSqlNode sqlNode, MappingVo resultMap, Integer fetchSize) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		List<XCO> result = sqlActuator.selectAllXCO(connection, getSql(), argList, resultMap, fetchSize);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public Map<String, Object> executeSelectOneMap(AbstractSqlNode sqlNode, MappingVo resultMap, Integer fetchSize) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		Map<String, Object> result = sqlActuator.selectOneMap(connection, getSql(), argList, resultMap, fetchSize);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public XCO executeSelectOneXCO(AbstractSqlNode sqlNode, MappingVo resultMap, Integer fetchSize) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		XCO result = sqlActuator.selectOneXCO(connection, getSql(), argList, resultMap, fetchSize);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public Object executeSelectVar(AbstractSqlNode sqlNode) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		Object result = sqlActuator.selectVar(connection, getSql(), argList);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public int executeDelete(AbstractSqlNode sqlNode) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		int result = sqlActuator.delete(connection, getSql(), argList);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public int executeUpdate(AbstractSqlNode sqlNode) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		int result = sqlActuator.update(connection, getSql(), argList);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public int executeInsert(AbstractSqlNode sqlNode) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		int result = sqlActuator.insert(connection, getSql(), argList);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public InsertReturn executeInsertReturn(AbstractSqlNode sqlNode) throws SQLException {
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		Connection connection = transaction.getConnection(dsKey);
		if (null == connection) {
			throw new SQLException("Connection is null:" + dsKey);
		}
		InsertReturn result = sqlActuator.insertReturn(connection, getSql(), argList);
		if (log.isInfoEnabled()) {
			log.info(this.realSql);
		}
		return result;
	}

	public void beforeExecute(AbstractSqlNode sqlNode) throws SQLException {
		// TODO 要根据上下文获取: 需要判断两个不同的数据源, 再一个查询中
		String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
		transaction = transactionManager.getTransaction(dsKey, sqlNode.getTxDef(), transaction);
	}

	public void beforeExecute(AbstractSqlNode sqlNode, boolean openConnection) throws SQLException {
		if (openConnection) {
			// TODO 要根据上下文获取: 需要判断两个不同的数据源, 再一个查询中
			String dsKey = (null != this.realDsKey) ? this.realDsKey : sqlNode.getDsKey();
			// log.debug("子服务运行之前不需要打开事务,只需要打开连接");
			transaction = transactionManager.getTransaction(dsKey, null, transaction);
		} else {
			// log.debug("主服务运行之前只需要打开事务,不需要打开连接");
			transaction = transactionManager.getTransaction(null, sqlNode.getTxDef(), transaction);
		}
	}

	public void afterExecute(AbstractSqlNode sqlNode) throws SQLException {
		// TODO 1. count time
	}

	public void commit(boolean confirm) throws Throwable {
		if (null != transaction) {
			transaction = transactionManager.commit(transaction, confirm, this);
			if (null != this.exception) {
				Throwable ex = this.exception;
				this.exception = null;
				throw ex;
			}
		}
	}

	public void rollback() throws SQLException {
		if (null != transaction) {
			transaction = transactionManager.rollback(transaction);
		}
	}

	/**
	 * 递归回滚
	 */
	protected void rollbackAll() {
		while (null != transaction) {
			try {
				rollback();
			} catch (SQLException e) {
			}
		}
		// 清空异常信息
		this.exceptionInfo = null;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

	/**
	 * 异常处理(上下文模式中调用)
	 */
	public void onException(Throwable e, String message) {
		if (null == e) {
			rollbackAll();
			throw new ServiceException(message);
		}
		if (null != this.exceptionInfo) {
			if (this.exceptionInfo.isNewTranscation()) {
				if (this.exceptionInfo.isCreatedTranscation()) {
					try {
						rollback();// 只是回滚本层事务
					} catch (Throwable e1) {
					}
				}
				this.exceptionInfo = null; // 清理异常信息
				log.error(message, e);
				return;
			}
		}
		rollbackAll();
		if (e instanceof ServiceException) {
			throw (ServiceException) e;
		}
		throw new ServiceException(message, e);
	}

	// public Object onException(Throwable e, String message) {
	// this.setException(null);// 清除上下文中的异常
	// SqlServiceException ex = null;
	// if (null == e) {
	// ex = new SqlServiceException(message);
	// if (null != this.transaction) {
	// recursionRollback();
	// }
	// ex.setRollback(true);
	// throw ex;
	// } else if (e instanceof SqlServiceException) {
	// ex = (SqlServiceException) e;
	// ex.setRollback(true);
	// if (ex.isNewTranscation()) {
	// // 独立事务(立即处理)
	// log.error("独立事务异常(立即处理)", ex);
	// if (ExceptionPosition.AMONG == ex.getExPosition()) {
	// try {
	// rollback();// 只是回滚本层事务
	// } catch (Throwable e1) {
	// }
	// }
	// // // ExceptionPosition.BEFORE:这里未创建事务,不涉及回滚
	// // // ExceptionPosition.AFTER:这里已经提交,不能再回滚
	// } else {
	// recursionRollback();
	// throw ex;
	// }
	// } else {
	// ex = new SqlServiceException(message, e);
	// if (null != this.transaction) {
	// recursionRollback();
	// }
	// ex.setRollback(true);
	// throw ex;
	// }
	// return null;
	// }
}
