package org.xson.tangyuan.datasource;

import java.util.Map;

import org.xson.tangyuan.datasource.DataSourceVo.ConnPoolType;
import org.xson.tangyuan.datasource.impl.DBCPDataSourceCreater;
import org.xson.tangyuan.datasource.impl.JndiDataSourceCreater;

public class DataSourceCreater {

	public AbstractDataSource create(DataSourceVo dsVo, Map<String, String> decryptProperties) throws Exception {
		if (ConnPoolType.C3P0 == dsVo.getType()) {
		} else if (ConnPoolType.DBCP == dsVo.getType()) {
			return new DBCPDataSourceCreater().create(dsVo, decryptProperties);
		} else if (ConnPoolType.PROXOOL == dsVo.getType()) {

		} else if (ConnPoolType.DRUID == dsVo.getType()) {

		} else if (ConnPoolType.JNDI == dsVo.getType()) {

		}
		return null;
	}

	public void create(DataSourceGroupVo dsGroupVo, Map<String, AbstractDataSource> realDataSourceMap, Map<String, String> decryptProperties)
			throws Exception {
		if (ConnPoolType.C3P0 == dsGroupVo.getType()) {
		} else if (ConnPoolType.DBCP == dsGroupVo.getType()) {
			new DBCPDataSourceCreater().createGroup(dsGroupVo, decryptProperties, realDataSourceMap);
		} else if (ConnPoolType.PROXOOL == dsGroupVo.getType()) {

		} else if (ConnPoolType.DRUID == dsGroupVo.getType()) {

		} else if (ConnPoolType.JNDI == dsGroupVo.getType()) {

		}
	}

	public void createDataSource(DataSourceVo dsVo, Map<String, AbstractDataSource> realDataSourceMap, Map<String, String> decryptProperties)
			throws Exception {
		if (ConnPoolType.DBCP == dsVo.getType()) {
			AbstractDataSource ads = new DBCPDataSourceCreater().create(dsVo, decryptProperties);
			realDataSourceMap.put(dsVo.getId(), ads);
		} else if (ConnPoolType.JNDI == dsVo.getType()) {
			new JndiDataSourceCreater().create(dsVo, decryptProperties, realDataSourceMap);
		}
	}

	public void createDataSourceGroup(DataSourceGroupVo dsGroupVo, Map<String, AbstractDataSource> realDataSourceMap,
			Map<String, String> decryptProperties) throws Exception {
		if (ConnPoolType.DBCP == dsGroupVo.getType()) {
			new DBCPDataSourceCreater().createGroup(dsGroupVo, decryptProperties, realDataSourceMap);
		} else if (ConnPoolType.JNDI == dsGroupVo.getType()) {
			new JndiDataSourceCreater().createGroup(dsGroupVo, decryptProperties, realDataSourceMap);
		}
	}

}
