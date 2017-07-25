package org.xson.tangyuan.datasource.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.datasource.AbstractDataSource;
import org.xson.tangyuan.datasource.DataSourceException;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.zongzi.JNDIDataSource;

public class TangYuanJndiDataSource extends AbstractDataSource {

	private static Log			log				= LogFactory.getLog(TangYuanJndiDataSource.class);

	private JNDIDataSource		dataSource;

	private boolean				group			= false;
	// private int start = 0;
	// private int end = 0;

	private Map<String, String>	jndiNameMapping	= null;

	protected TangYuanJndiDataSource(JNDIDataSource dataSource, String logicDataSourceId, String realDataSourceId) {
		this.dataSource = dataSource;
		this.logicDataSourceId = logicDataSourceId;
		this.realDataSourceId = realDataSourceId;
	}

	@Override
	public Connection getConnection(String dsKey) throws SQLException {
		if (group) {
			// src: life.1 --> desc jdbc/lift.1
			String jndiName = jndiNameMapping.get(dsKey);
			if (null == jndiName) {
				throw new DataSourceException("The JNDI mapping name does not exist: " + dsKey);
			}
			return dataSource.getConnection(jndiName);
		} else {
			return dataSource.getConnection(dsKey);
		}
	}

	@Override
	public void recycleConnection(Connection connection) throws SQLException {
		try {
			connection.close();
		} catch (Exception e) {
			log.error("recycleConnection exception", e);
		}
	}

	protected void setGroupInfo(int start, int end, String jndiName) {
		this.group = true;
		// this.start = start;
		// this.end = end;
		// mapping(jndiName);
		jndiNameMapping = new HashMap<String, String>();
		for (int i = start; i <= end; i++) {
			jndiNameMapping.put(logicDataSourceId + "." + i, jndiName + "." + i);
		}
	}

	// private void mapping(String jndiName) {
	// jndiNameMapping = new HashMap<String, String>();
	// for (int i = start; i <= end; i++) {
	// jndiNameMapping.put(logicDataSourceId + "." + i, jndiName + "." + i);
	// }
	// }

	@Override
	public void close() throws SQLException {
		// dataSource.close(); 什么也不做, 在ZongZi容器中回收
		dataSource = null;
	}
}
