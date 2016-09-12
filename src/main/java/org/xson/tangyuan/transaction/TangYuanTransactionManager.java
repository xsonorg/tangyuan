package org.xson.tangyuan.transaction;

import java.sql.SQLException;
import java.util.Map;

import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;

public class TangYuanTransactionManager extends DefaultTransactionManager {

	private Log logger = LogFactory.getLog(TangYuanTransactionManager.class);

	// 这里应该有一个XA的事务管理器,来处理XA事务
	public XTransactionStatus getTransaction(String dsKey, XTransactionDefinition definition, XTransactionStatus currentStatus) throws SQLException {
		XTransactionStatus status = null;
		if (null == dsKey) {
			// logger.debug("只是处理事务");
			if (null == currentStatus) {
				status = createTransactionStatusOnly(definition);
			} else {
				status = handleExistingTransactionOnly(definition, currentStatus);
			}
		} else if (null == definition) {
			// logger.debug("只是打开连接");
			// 这里一定存在事务
			openConnectionOnly(dsKey, currentStatus);
			status = currentStatus;
		} else {
			// null != dsKey && null != definition
			status = super.getTransaction(dsKey, definition, currentStatus);
		}
		if (null == status) {
			throw new TransactionException("没有匹配的TransactionStatus");
		}
		return status;
	}

	/**
	 * 只是打开连接
	 */
	private void openConnectionOnly(String dsKey, XTransactionStatus currentStatus) throws SQLException {
		if (currentStatus.initialization) {
			// 这里所有的事务设置都是一致的, 都是最初设置的, 所以不存在事务的变化和传播
			if (currentStatus.singleDs && dsKey.equalsIgnoreCase(currentStatus.dsKey)) {
				if (currentStatus.hasSavepoint) {
					currentStatus.xConnection.setSavepoint();
				}
				return;
			}
			// 多数据源的情况
			XConnection xConn = currentStatus.getXConnectionFromMap(dsKey);
			if (null == xConn) { // 如果之前不存在事务, 则之后的Connection都是开启事务的
				xConn = new XConnection();
				xConn.begin(dsKey, dataSources, currentStatus.definition, !currentStatus.hasTransaction);
				currentStatus.addXConnection(dsKey, xConn);
			}
			if (currentStatus.hasSavepoint) {
				xConn.setSavepoint();
			}
		} else {
			// 之前事务未初始化, 在这里初始化并打开首个连接
			currentStatus.xConnection = new XConnection();
			currentStatus.xConnection.begin(dsKey, dataSources, currentStatus.definition, !currentStatus.hasTransaction);
			currentStatus.dsKey = dsKey;
			currentStatus.initialization = true;
		}
	}

	/**
	 * 创建一个新事务(ONLY), 并不打开数据库链接
	 */
	private XTransactionStatus createTransactionStatusOnly(XTransactionDefinition definition) throws SQLException {
		boolean autoCommit = false;
		if (XTransactionDefinition.PROPAGATION_SUPPORTS == definition.getBehavior() || XTransactionDefinition.PROPAGATION_NOT_SUPPORTED == definition.getBehavior()
				|| XTransactionDefinition.PROPAGATION_NEVER == definition.getBehavior()) {
			autoCommit = true;
		}
		XTransactionStatus status = new XTransactionStatus();
		// 这里一定是获取一个新的连接
		// status.xConnection = new XConnection();
		// status.xConnection.begin(dsKey, dataSources, definition, autoCommit);
		// status.dsKey = dsKey;
		status.hasTransaction = !autoCommit;
		status.name = definition.getName();
		status.definition = definition;
		status.initialization = false; // 未初始化

		if (XTransactionDefinition.PROPAGATION_REQUIRES_NEW == definition.getBehavior() || XTransactionDefinition.PROPAGATION_NOT_SUPPORTED == definition.getBehavior()) {
			status.newTransaction = true;
		}

		return status;
	}

	/**
	 * 处理存在的事务
	 */
	private XTransactionStatus handleExistingTransactionOnly(XTransactionDefinition newDefinition, XTransactionStatus currentStatus) throws SQLException {
		if (XTransactionDefinition.PROPAGATION_REQUIRED == newDefinition.getBehavior()) {
			newTransaction(currentStatus);
			return currentStatus;
		} else if (XTransactionDefinition.PROPAGATION_SUPPORTS == newDefinition.getBehavior()) {
			return currentStatus;
		} else if (XTransactionDefinition.PROPAGATION_MANDATORY == newDefinition.getBehavior()) {
			if (!currentStatus.hasTransaction) {
				throw new TransactionException("No existing transaction found for transaction marked with propagation 'mandatory'");
			}
			return currentStatus;
		} else if (XTransactionDefinition.PROPAGATION_REQUIRES_NEW == newDefinition.getBehavior()) {
			if (currentStatus.hasTransaction) {
				// 之前存在事务，挂起之前的事务,创建独立的新事物
				XTransactionStatus newStatus = createTransactionStatusOnly(newDefinition);
				newStatus.suspendedResources = currentStatus;
				return newStatus;
			} else {
				// 之前不存在事务,沿用连接并开启新事物
				newTransaction(currentStatus);
				currentStatus.newTransaction = true;
				return currentStatus;
			}
		} else if (XTransactionDefinition.PROPAGATION_NOT_SUPPORTED == newDefinition.getBehavior()) {
			if (currentStatus.hasTransaction) {
				// 之前存在事务，挂起之前的事务,创建独立的非事物连接
				XTransactionStatus newStatus = createTransactionStatusOnly(newDefinition);
				newStatus.suspendedResources = currentStatus;
				return newStatus;
			} else {
				currentStatus.newTransaction = true;
				return currentStatus;
			}
		} else if (XTransactionDefinition.PROPAGATION_NEVER == newDefinition.getBehavior()) {
			if (currentStatus.hasTransaction) {
				throw new TransactionException("Existing transaction found for transaction marked with propagation 'never'");
			}
			return currentStatus;
		} else if (XTransactionDefinition.PROPAGATION_NESTED == newDefinition.getBehavior()) {
			if (currentStatus.hasTransaction) {
				currentStatus.hasSavepoint = true;
			} else {
				// 之前不存在事务,沿用连接并开启新事物
				newTransaction(currentStatus);
			}
			return currentStatus;
		}
		return null;
	}

	/**
	 * 之前不存在事务,沿用连接并开启新事物
	 */
	private void newTransaction(XTransactionStatus currentStatus) throws SQLException {
		if (!currentStatus.hasTransaction) {
			currentStatus.hasTransaction = true;
			if (currentStatus.initialization) {
				if (currentStatus.singleDs) {
					// 单数据源的情况:不存在事务
					currentStatus.xConnection.beginTransaction(currentStatus.definition);
				} else {
					// 多数据源的情况:不存在事务
					for (Map.Entry<String, XConnection> entry : currentStatus.connMap.entrySet()) {
						entry.getValue().checkSetTransaction(currentStatus.definition);
					}
				}
			}
		}
	}

}
