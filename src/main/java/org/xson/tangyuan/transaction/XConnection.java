package org.xson.tangyuan.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import org.xson.tangyuan.datasource.DataSourceManager;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;

public class XConnection {

	protected static enum ConnectionState {
		ROLLBACK, COMMIT
	}

	private static Log			log						= LogFactory.getLog(XConnection.class);

	private static final String	SAVEPOINT_NAME_PREFIX	= "SAVEPOINT_";

	private Connection			conn					= null;

	private Savepoint			savepoint				= null;

	private int					savepointCounter		= 0;

	// 事务进展[0: 初始化, 1:rollback, 2:commit]
	protected ConnectionState	connState				= null;

	// TODO ISO设置顺序

	public void begin(String dsKey, DataSourceManager dataSources, XTransactionDefinition definition, boolean autoCommit) throws SQLException {
		this.conn = dataSources.getConnection(dsKey);
		int isolation = this.conn.getTransactionIsolation();
		if (XTransactionDefinition.ISOLATION_DEFAULT != definition.getIsolation() && isolation != definition.getIsolation()) {
			this.conn.setTransactionIsolation(definition.getIsolation());
		}
		if (autoCommit != this.conn.getAutoCommit()) {
			this.conn.setAutoCommit(autoCommit);
		}
		// TODO
		// if (definition.isReadOnly() && !this.conn.isReadOnly()) {
		// this.conn.setReadOnly(definition.isReadOnly());
		// }
		log.info("open new connection. dsKey[" + dsKey + "], hashCode[" + this.conn.hashCode() + "]");
	}

	/**
	 * 之前不存在事务
	 */
	public void beginTransaction(XTransactionDefinition definition) throws SQLException {
		int isolation = this.conn.getTransactionIsolation();
		if (XTransactionDefinition.ISOLATION_DEFAULT != definition.getIsolation() && isolation != definition.getIsolation()) {
			this.conn.setTransactionIsolation(definition.getIsolation());
		}

		if (this.conn.getAutoCommit()) {
			this.conn.setAutoCommit(false);
		}
	}

	/**
	 * 检测之前，如果不存在事务，则设置事务
	 */
	public void checkSetTransaction(XTransactionDefinition definition) throws SQLException {
		if (this.conn.getAutoCommit()) {

			int isolation = this.conn.getTransactionIsolation();
			if (isolation != definition.getIsolation()) {
				this.conn.setTransactionIsolation(definition.getIsolation());
			}

			this.conn.setAutoCommit(false);
		}
	}

	public void setSavepoint() throws SQLException {
		// TODO
		// if (null != this.savepoint) {
		// throw new SQLException("TransactionStatus has Savepoint");
		// }
		if (!conn.getMetaData().supportsSavepoints()) {
			throw new SQLException("SavepointManager does not support Savepoint.");
		}
		this.savepoint = conn.setSavepoint(SAVEPOINT_NAME_PREFIX + this.savepointCounter++);
	}

	public Connection getConnection() {
		return conn;
	}

	public Savepoint getSavepoint() {
		return savepoint;
	}

	public int getSavepointCounter() {
		return savepointCounter;
	}

}
