package org.xson.tangyuan.datasource.impl;

import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.xson.tangyuan.datasource.AbstractDataSource;
import org.xson.tangyuan.datasource.DataSourceException;
import org.xson.tangyuan.datasource.DataSourceGroupVo;
import org.xson.tangyuan.datasource.DataSourceVo;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.zongzi.JNDIDataSource;

public class JndiDataSourceCreater {

	private Log log = LogFactory.getLog(JndiDataSourceCreater.class);

	public void create(DataSourceVo dsVo, Map<String, String> decryptProperties, Map<String, AbstractDataSource> dataSourceMap) throws Exception {

		String jndiName = getJndiName(dsVo);
		// if (null == jndiName) {
		// throw new DataSourceException("Non-existent JNDI data source: " + jndiName);
		// }

		Context context = new InitialContext();
		JNDIDataSource dataSource = (JNDIDataSource) context.lookup(jndiName);
		if (null == dataSource) {
			throw new DataSourceException("Non-existent JNDI data source: " + jndiName);
		}

		if (dataSource.isGroup() != dsVo.isGroup()) {
			throw new DataSourceException("JNDI data source group mode does not match: " + jndiName);
		}

		TangYuanJndiDataSource jndiDs = new TangYuanJndiDataSource(dataSource, dsVo.getId(), dsVo.getId());
		dataSourceMap.put(dsVo.getId(), jndiDs);

		// if (dataSource.isGroup()) {
		// TangYuanJndiDataSource jndiDs = new TangYuanJndiDataSource(dataSource, dsVo.getId(), dsVo.getId());
		// jndiDs.setGroupInfo(dataSource.getStart(), dataSource.getEnd(), jndiName);
		// for (int i = dataSource.getStart(); i <= dataSource.getEnd(); i++) {
		// if (dataSourceMap.containsKey(dsVo.getId() + "." + i)) {
		// throw new DataSourceException("Duplicate DataSource ID: " + dsVo.getId() + "." + i);
		// }
		// dataSourceMap.put(dsVo.getId() + "." + i, jndiDs);
		// log.info("add group datasource: " + dsVo.getId() + "." + i);
		// }
		// } else {
		// TangYuanJndiDataSource jndiDs = new TangYuanJndiDataSource(dataSource, dsVo.getId(), dsVo.getId());
		// dataSourceMap.put(dsVo.getId(), jndiDs);
		// }
	}

	public void createGroup(DataSourceGroupVo dsVo, Map<String, String> decryptProperties, Map<String, AbstractDataSource> dataSourceMap)
			throws Exception {
		String jndiName = getJndiName(dsVo);
		// if (null == jndiName) {
		// throw new DataSourceException("Non-existent JNDI data source: " + jndiName);
		// }

		Context context = new InitialContext();
		JNDIDataSource dataSource = (JNDIDataSource) context.lookup(jndiName);
		if (null == dataSource) {
			throw new DataSourceException("Non-existent JNDI data source: " + jndiName);
		}

		if (dataSource.isGroup() != dsVo.isGroup()) {
			throw new DataSourceException("JNDI data source group mode does not match: " + jndiName);
		}

		TangYuanJndiDataSource jndiDs = new TangYuanJndiDataSource(dataSource, dsVo.getId(), dsVo.getId());
		jndiDs.setGroupInfo(dataSource.getStart(), dataSource.getEnd(), jndiName);
		for (int i = dataSource.getStart(); i <= dataSource.getEnd(); i++) {
			if (dataSourceMap.containsKey(dsVo.getId() + "." + i)) {
				throw new DataSourceException("Duplicate DataSource ID: " + dsVo.getId() + "." + i);
			}
			dataSourceMap.put(dsVo.getId() + "." + i, jndiDs);
			log.info("add group datasource: " + dsVo.getId() + "." + i);
		}

		// 根据JNDI的数量设置引用的
		dsVo.setGroupNum(dataSource.getStart(), dataSource.getEnd());
	}

	private String getJndiName(DataSourceVo dsVo) {
		return dsVo.getProperties().get("jndiName".toUpperCase());
	}
}
