package org.xson.tangyuan.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.datasource.DataSourceManager;
import org.xson.tangyuan.executor.SqlServiceContext;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.transaction.XConnection.ConnectionState;

public class DefaultTransactionManager implements XTransactionManager {

	protected DataSourceManager	dataSources	= TangYuanContainer.getInstance().getDataSourceManager();

	private Log					logger		= LogFactory.getLog(DefaultTransactionManager.class);

	// 这里应该有一个XA的事务管理器,来处理XA事务
	public XTransactionStatus getTransaction(String dsKey, XTransactionDefinition definition, XTransactionStatus currentStatus) throws SQLException {
		// logger.info("只有简单sql-service服务进入这里");
		if (null == currentStatus) {
			return createTransactionStatus(dsKey, definition);
		} else {
			return handleExistingTransaction(dsKey, definition, currentStatus);
		}
	}

	/**
	 * 创建一个新事务： 只有存在确定的数据源的情况下再调用
	 */
	private XTransactionStatus createTransactionStatus(String dsKey, XTransactionDefinition definition) throws SQLException {
		boolean autoCommit = false;
		if (XTransactionDefinition.PROPAGATION_SUPPORTS == definition.getBehavior()
				|| XTransactionDefinition.PROPAGATION_NOT_SUPPORTED == definition.getBehavior()
				|| XTransactionDefinition.PROPAGATION_NEVER == definition.getBehavior()) {
			autoCommit = true;
		}
		XTransactionStatus status = new XTransactionStatus();
		// 这里一定是获取一个新的连接
		status.xConnection = new XConnection();
		status.xConnection.begin(dsKey, dataSources, definition, autoCommit);
		status.hasTransaction = !autoCommit;
		status.dsKey = dsKey;
		status.name = definition.getName();
		status.definition = definition;

		if (XTransactionDefinition.PROPAGATION_REQUIRES_NEW == definition.getBehavior()
				|| XTransactionDefinition.PROPAGATION_NOT_SUPPORTED == definition.getBehavior()) {
			status.newTransaction = true;
		}

		return status;
	}

	/**
	 * 处理存在的事务
	 */
	private XTransactionStatus handleExistingTransaction(String dsKey, XTransactionDefinition newDefinition, XTransactionStatus currentStatus)
			throws SQLException {
		if (null == newDefinition.getTxUse()) {
			// 存在事务定义,则根据事务的传播级别处理
			if (XTransactionDefinition.PROPAGATION_REQUIRED == newDefinition.getBehavior()) {
				return handleExistingTransactionRequired(dsKey, newDefinition, currentStatus);
			} else if (XTransactionDefinition.PROPAGATION_SUPPORTS == newDefinition.getBehavior()) {
				return handleExistingTransactionSupports(dsKey, newDefinition, currentStatus);
			} else if (XTransactionDefinition.PROPAGATION_MANDATORY == newDefinition.getBehavior()) {
				if (!currentStatus.hasTransaction) {
					throw new TransactionException("No existing transaction found for transaction marked with propagation 'mandatory'");
				}
				return currentStatus;
			} else if (XTransactionDefinition.PROPAGATION_REQUIRES_NEW == newDefinition.getBehavior()) {
				return handleExistingTransactionRequiredNew(dsKey, newDefinition, currentStatus);
			} else if (XTransactionDefinition.PROPAGATION_NOT_SUPPORTED == newDefinition.getBehavior()) {
				return handleExistingTransactionNotSupport(dsKey, newDefinition, currentStatus);
			} else if (XTransactionDefinition.PROPAGATION_NEVER == newDefinition.getBehavior()) {
				if (currentStatus.hasTransaction) {
					throw new TransactionException("Existing transaction found for transaction marked with propagation 'never'");
				}
				return currentStatus;
			} else if (XTransactionDefinition.PROPAGATION_NESTED == newDefinition.getBehavior()) {
				return handleExistingTransactionNested(dsKey, newDefinition, currentStatus);
			}
			return null;
		} else {
			return handleUseExistingTransaction(dsKey, newDefinition, currentStatus);
		}
	}

	/**
	 * 处理引用一个存在的事务
	 */
	private XTransactionStatus handleUseExistingTransaction(String dsKey, XTransactionDefinition newDefinition, XTransactionStatus currentStatus)
			throws SQLException {
		// TODO 引用之前的事务,之前检测保证存在
		XTransactionStatus existingTx = currentStatus.findTxByName(newDefinition.getTxUse());

		// 单数据源的情况
		if (existingTx.singleDs && dsKey.equalsIgnoreCase(existingTx.dsKey)) {
			return existingTx;
		}
		// 多数据源的情况
		XConnection xConn = existingTx.getXConnectionFromMap(dsKey);
		if (null == xConn) {
			xConn = new XConnection();
			xConn.begin(dsKey, dataSources, existingTx.definition, !existingTx.hasTransaction);
			existingTx.addXConnection(dsKey, xConn);
		}
		return existingTx;
	}

	/**
	 * 处理事务传播:NESTED
	 */
	private XTransactionStatus handleExistingTransactionNested(String dsKey, XTransactionDefinition newDefinition, XTransactionStatus currentStatus)
			throws SQLException {
		if (currentStatus.hasTransaction) {
			currentStatus.hasSavepoint = true;
			// 单数据源的情况
			if (currentStatus.singleDs && dsKey.equalsIgnoreCase(currentStatus.dsKey)) {
				currentStatus.xConnection.setSavepoint();
				return currentStatus;
			}
			// 多数据源的情况
			XConnection xConn = currentStatus.getXConnectionFromMap(dsKey);
			if (null == xConn) {
				xConn = new XConnection();
				xConn.begin(dsKey, dataSources, newDefinition, !currentStatus.hasTransaction);
				currentStatus.addXConnection(dsKey, xConn);
			}
			xConn.setSavepoint();
			return currentStatus;
		} else { // 之前不存在事务,沿用连接并开启新事物
			return handleExistingTransactionRequired(dsKey, newDefinition, currentStatus);
		}
	}

	/**
	 * 处理事务传播:NOT_SUPPORTED
	 */
	private XTransactionStatus handleExistingTransactionNotSupport(String dsKey, XTransactionDefinition newDefinition,
			XTransactionStatus currentStatus) throws SQLException {
		if (currentStatus.hasTransaction) { // 之前存在事务，挂起之前的事务,创建独立的非事物连接
			XTransactionStatus newStatus = createTransactionStatus(dsKey, newDefinition);
			newStatus.suspendedResources = currentStatus;
			return newStatus;
		} else {
			currentStatus.newTransaction = true;
			return currentStatus;
		}
	}

	/**
	 * 处理事务传播:REQUIRES_NEW
	 */
	private XTransactionStatus handleExistingTransactionRequiredNew(String dsKey, XTransactionDefinition newDefinition,
			XTransactionStatus currentStatus) throws SQLException {
		if (currentStatus.hasTransaction) { // 之前存在事务，挂起之前的事务,创建独立的新事物
			XTransactionStatus newStatus = createTransactionStatus(dsKey, newDefinition);
			newStatus.suspendedResources = currentStatus;
			return newStatus;
		} else { // 之前不存在事务,沿用连接并开启新事物
			currentStatus.newTransaction = true;
			return handleExistingTransactionRequired(dsKey, newDefinition, currentStatus);
		}
	}

	/**
	 * 处理事务传播:REQUIRED
	 */
	private XTransactionStatus handleExistingTransactionRequired(String dsKey, XTransactionDefinition newDefinition, XTransactionStatus currentStatus)
			throws SQLException {
		// 单数据源的情况
		if (currentStatus.singleDs && dsKey.equalsIgnoreCase(currentStatus.dsKey)) {
			if (currentStatus.hasTransaction) { // 存在事务
				return currentStatus;
			} else { // 之前不存在事务
				currentStatus.xConnection.beginTransaction(newDefinition);
				currentStatus.hasTransaction = true;
				return currentStatus;
			}
		}
		// 多数据源的情况
		XConnection xConn = currentStatus.getXConnectionFromMap(dsKey);
		if (null == xConn) { // 如果之前不存在事务, 则之后的Connection都是开启事务的
			xConn = new XConnection();
			xConn.begin(dsKey, dataSources, newDefinition, false);
			currentStatus.addXConnection(dsKey, xConn);
		}

		if (currentStatus.hasTransaction) {
			return currentStatus;
		} else {
			for (Map.Entry<String, XConnection> entry : currentStatus.connMap.entrySet()) {
				entry.getValue().checkSetTransaction(newDefinition);
			}
			currentStatus.hasTransaction = true;
			return currentStatus;
		}
	}

	/**
	 * 处理事务传播:SUPPORTS
	 */
	private XTransactionStatus handleExistingTransactionSupports(String dsKey, XTransactionDefinition newDefinition, XTransactionStatus currentStatus)
			throws SQLException {
		// 单数据源的情况
		if (currentStatus.singleDs && dsKey.equalsIgnoreCase(currentStatus.dsKey)) {
			return currentStatus;
		}
		// 多数据源的情况
		XConnection xConn = currentStatus.getXConnectionFromMap(dsKey);
		if (null == xConn) {
			xConn = new XConnection();
			// 这里要和当前事务保持一致:!currentStatus.newTransaction,
			xConn.begin(dsKey, dataSources, currentStatus.definition, !currentStatus.hasTransaction);
			currentStatus.addXConnection(dsKey, xConn);
		}
		return currentStatus;
	}

	/**
	 * confirm:是否确认提交 false:自动判断是否需要提交(只提交单独的事务), true:立即提交
	 */
	public XTransactionStatus commit(XTransactionStatus status, boolean confirm, SqlServiceContext context) throws SQLException {

		if (!confirm && !status.newTransaction) {
			// 不确定提交, 不是一个非独立的事务, 暂时不提交
			// logger.debug("不确定提交,不是一个非独立的事务, 暂时不提交");
			logger.debug("commit ignore.");
			return status;
		}

		if (!status.initialization) {
			if (null != status.suspendedResources) {
				return status.suspendedResources;
			}
			return null;
		}

		SQLException exception = null;
		if (status.hasTransaction) {
			// 存在事务
			if (null != status.xConnection) {
				Connection conn = status.xConnection.getConnection();
				try {
					conn.commit();
					dataSources.recycleConnection(status.dsKey, conn);// 这里不能抛出异常, 内部处理
					status.xConnection.connState = ConnectionState.COMMIT;
				} catch (SQLException e) {
					// 需要处理异常
					logger.error("commit error");
					exception = e;
					status.xConnection.connState = ConnectionState.ROLLBACK;
				}
			} else if (null != status.connMap) {
				for (Map.Entry<String, XConnection> entry : status.connMap.entrySet()) {
					Connection conn = entry.getValue().getConnection();
					if (null == exception) {
						try {
							conn.commit();
							dataSources.recycleConnection(entry.getKey(), conn);// 这里不能抛出异常, 内部处理
							entry.getValue().connState = ConnectionState.COMMIT;
						} catch (SQLException e) {
							// 需要处理异常:
							logger.error("commit error");
							exception = e;
							entry.getValue().connState = ConnectionState.ROLLBACK;
						}
					} else {
						entry.getValue().connState = ConnectionState.ROLLBACK;
					}
				}
			}
		} else {
			// 不存在事务
			if (null != status.xConnection) {
				Connection conn = status.xConnection.getConnection();
				dataSources.recycleConnection(status.dsKey, conn); // 这里不能抛出异常, 内部处理
			} else if (null != status.connMap) {
				for (Map.Entry<String, XConnection> entry : status.connMap.entrySet()) {
					Connection conn = entry.getValue().getConnection();
					dataSources.recycleConnection(entry.getKey(), conn);// 这里不能抛出异常, 内部处理
				}
			}
		}

		context.setException(exception);

		if (null != exception) {
			return status; // commit过程中发生异常, 后面将要回滚
		}

		logger.info("commit success");

		// 回复挂起的服务
		if (null != status.suspendedResources) {
			return status.suspendedResources;
		}

		return null;
	}

	public XTransactionStatus rollback(XTransactionStatus status) throws SQLException {
		if (!status.initialization) {
			if (null != status.suspendedResources) {
				return status.suspendedResources;
			}
			return null;
		}

		// SQLException exception = null;

		if (status.hasTransaction) {
			// 存在事务
			if (null != status.xConnection && status.xConnection.connState != ConnectionState.COMMIT) {
				Connection conn = status.xConnection.getConnection();
				try {
					if (null == status.xConnection.getSavepoint()) {
						conn.rollback();
					} else {
						conn.rollback(status.xConnection.getSavepoint());
						conn.commit();
					}
				} catch (SQLException e) {
					logger.error("rollback error", e);
					// exception = e;
				}
				dataSources.recycleConnection(status.dsKey, conn);// 这里不能抛出异常, 内部处理
			} else if (null != status.connMap) {
				for (Map.Entry<String, XConnection> entry : status.connMap.entrySet()) {
					if (entry.getValue().connState != ConnectionState.COMMIT) {
						Connection conn = entry.getValue().getConnection();
						try {
							if (null == entry.getValue().getSavepoint()) {
								conn.rollback();
							} else {
								conn.rollback(entry.getValue().getSavepoint());
								conn.commit();
							}
						} catch (SQLException e) {
							logger.error("rollback error", e);
							// exception = e;
						}
						dataSources.recycleConnection(entry.getKey(), conn);// 这里不能抛出异常, 内部处理
					}
				}
			}
		} else {
			// 不存在事务
			if (null != status.xConnection) {
				Connection conn = status.xConnection.getConnection();
				dataSources.recycleConnection(status.dsKey, conn);// 这里不能抛出异常, 内部处理
			} else if (null != status.connMap) {
				for (Map.Entry<String, XConnection> entry : status.connMap.entrySet()) {
					Connection conn = entry.getValue().getConnection();
					dataSources.recycleConnection(entry.getKey(), conn);// 这里不能抛出异常, 内部处理
				}
			}
		}

		logger.info("rollback");

		if (null != status.suspendedResources) {
			return status.suspendedResources;
		}
		return null;
	}
}
