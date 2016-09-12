package org.xson.tangyuan.datasource.impl;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.xson.tangyuan.datasource.AbstractDataSource;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;

public class DBCPDataSource extends AbstractDataSource {

	private static Log				log	= LogFactory.getLog(DBCPDataSource.class);

	private final BasicDataSource	dataSource;

	protected DBCPDataSource(BasicDataSource dataSource, String logicDataSourceId, String realDataSourceId) {
		this.dataSource = dataSource;
		this.logicDataSourceId = logicDataSourceId;
		this.realDataSourceId = realDataSourceId;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public void recycleConnection(Connection connection) throws SQLException {
		try {
			connection.close();
		} catch (Exception e) {
			log.error("recycleConnection exception", e);
		}
	}

	@Override
	public void close() throws SQLException {
		dataSource.close();
	}
}
