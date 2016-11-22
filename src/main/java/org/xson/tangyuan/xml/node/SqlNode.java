package org.xson.tangyuan.xml.node;

import java.sql.SQLException;

import org.xson.tangyuan.executor.ServiceContext;

public interface SqlNode {

	/**
	 * 执行当前节点
	 * 
	 * @param context
	 * @param arg
	 *            support map and xco
	 * @return
	 * @throws SQLException
	 */
	// boolean execute(SqlServiceContext context, Object arg) throws SQLException;
	boolean execute(ServiceContext context, Object arg) throws Throwable;

}
