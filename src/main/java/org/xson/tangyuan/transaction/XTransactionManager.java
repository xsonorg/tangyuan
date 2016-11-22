package org.xson.tangyuan.transaction;

import java.sql.SQLException;

import org.xson.tangyuan.executor.ServiceContext;

public interface XTransactionManager {

	XTransactionStatus getTransaction(String dsKey, XTransactionDefinition definition, XTransactionStatus status) throws SQLException;

	XTransactionStatus commit(XTransactionStatus status, boolean confirm, ServiceContext context) throws SQLException;

	XTransactionStatus rollback(XTransactionStatus status) throws SQLException;

	// void commit(XTransactionStatus status, boolean confirm) throws SQLException;
	// XTransactionStatus rollback(XTransactionStatus status, SqlServiceContext context) throws SQLException;
}
