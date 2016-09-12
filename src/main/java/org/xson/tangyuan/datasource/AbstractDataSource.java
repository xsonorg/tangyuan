package org.xson.tangyuan.datasource;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractDataSource {

	protected boolean	group;

	protected boolean	supportXA;

	protected boolean	supportSavepoint;

	protected String	logicDataSourceId;

	protected String	realDataSourceId;

	public boolean isGroup() {
		return this.group;
	}

	public boolean isSupportXA() {
		return this.supportXA;
	}

	public boolean isSupportSavepoint() {
		return supportSavepoint;
	}

	public String getLogicDataSourceId() {
		return logicDataSourceId;
	}

	public String getRealDataSourceId() {
		return realDataSourceId;
	}

	abstract public Connection getConnection() throws SQLException;

	abstract public void recycleConnection(Connection connection) throws SQLException;

	abstract public void close() throws SQLException;

	// abstract public Connection getConnection(String dsGroupKey) throws SQLException;
	// abstract public void recycleConnection(String dsGroupKey, Connection connection) throws SQLException;
}
