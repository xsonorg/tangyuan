package org.xson.tangyuan.datasource.impl;

import java.sql.Connection;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.datasource.AbstractDataSource;
import org.xson.tangyuan.datasource.DataSourceException;
import org.xson.tangyuan.datasource.DataSourceGroupVo;
import org.xson.tangyuan.datasource.DataSourceVo;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.util.StringUtils;

public class DBCPDataSourceCreater {

	private static Log log = LogFactory.getLog(DBCPDataSourceCreater.class);

	public AbstractDataSource create(DataSourceVo dataSourceVo, Map<String, String> decryptProperties) throws Exception {
		Map<String, String> properties = dataSourceVo.getProperties();
		BasicDataSource dsPool = createDataSource(properties, decryptProperties, null);
		DBCPDataSource dbcpDataSource = new DBCPDataSource(dsPool, dataSourceVo.getId(), dataSourceVo.getId());
		return dbcpDataSource;
	}

	public void createGroup(DataSourceGroupVo dsGroupVo, Map<String, String> decryptProperties, Map<String, AbstractDataSource> dataSourceMap) throws Exception {
		Map<String, String> properties = dsGroupVo.getProperties();
		for (int i = dsGroupVo.getStart(); i <= dsGroupVo.getEnd(); i++) {
			BasicDataSource dsPool = createDataSource(properties, decryptProperties, i + "");
			if (dataSourceMap.containsKey(dsGroupVo.getId() + "." + i)) {
				throw new DataSourceException("重复的DataSourceID:" + dsGroupVo.getId() + "." + i);
			}
			dataSourceMap.put(dsGroupVo.getId() + "." + i, new DBCPDataSource(dsPool, dsGroupVo.getId(), dsGroupVo.getId() + "." + i));
			log.info("add group datasource: " + dsGroupVo.getId() + "." + i);
		}
	}

	/**
	 * 获取属性值
	 * 
	 * @param property
	 *            属性名
	 * @param properties
	 *            值容器
	 * @param decryptProperties
	 *            加密值容器
	 * @param defaultValue
	 *            默认值
	 * @param throwEx
	 *            无值是否抛异常
	 * @return
	 */
	private String getPropertyStringValue(String property, Map<String, String> properties, Map<String, String> decryptProperties, String defaultValue, boolean throwEx) {
		String value = null;
		value = properties.get(property.toUpperCase());
		if (null == value) {
			value = (String) defaultValue;
		} else {
			String strValue = (String) value;
			if (null != decryptProperties && strValue.startsWith("${") && strValue.endsWith("}")) {
				value = decryptProperties.get(property.substring(2, property.length() - 1));
			}
		}
		if (throwEx && null == value) {
			throw new TangYuanException("missing attribute initialization data source: " + property);
		}
		return value;
	}

	private int getPropertyIntegerValue(String property, Map<String, String> properties, Map<String, String> decryptProperties, Integer defaultValue, boolean throwEx) {
		Object value = null;
		value = properties.get(property.toUpperCase());
		if (null == value) {
			value = defaultValue;
		} else {
			String strValue = (String) value;
			if (null != decryptProperties && strValue.startsWith("${") && strValue.endsWith("}")) {
				value = decryptProperties.get(property.substring(2, property.length() - 1));
			}
			if (null != value) {
				value = Integer.parseInt((String) value);
			}
		}
		if (throwEx && null == value) {
			throw new TangYuanException("missing attribute initialization data source: " + property);
		}
		return (Integer) value;
	}

	private boolean getPropertyBooleanValue(String property, Map<String, String> properties, Map<String, String> decryptProperties, Boolean defaultValue, boolean throwEx) {
		Object value = null;
		value = properties.get(property.toUpperCase());
		if (null == value) {
			value = defaultValue;
		} else {
			String strValue = (String) value;
			if (null != decryptProperties && strValue.startsWith("${") && strValue.endsWith("}")) {
				value = decryptProperties.get(property.substring(2, property.length() - 1));
			}
			if (null != value) {
				value = Boolean.parseBoolean((String) value);
			}
		}
		if (throwEx && null == value) {
			throw new TangYuanException("missing attribute initialization data source: " + property);
		}
		return (Boolean) value;
	}

	public static void main(String[] args) {
		String s = "${sdfd}";
		System.out.println(s.substring(2, s.length() - 1));
	}

	private BasicDataSource createDataSource(Map<String, String> properties, Map<String, String> decryptProperties, String urlPattern) {
		BasicDataSource dsPool = new BasicDataSource();

		dsPool.setDriverClassName(getPropertyStringValue("driver", properties, null, null, true));
		dsPool.setUsername(getPropertyStringValue("username", properties, null, null, true));
		dsPool.setPassword(getPropertyStringValue("password", properties, null, null, true));

		String url = getPropertyStringValue("url", properties, null, null, true);
		if (null != urlPattern) {
			url = StringUtils.replace(url, "{}", urlPattern);
		}
		dsPool.setUrl(url);

		dsPool.setPoolPreparedStatements(getPropertyBooleanValue("poolingStatements", properties, null, true, true)); // 开启池的prepared statement 池功能
		dsPool.setRemoveAbandoned(getPropertyBooleanValue("removeAbandoned", properties, null, true, true));
		dsPool.setRemoveAbandonedTimeout(getPropertyIntegerValue("removeAbandonedTimeout", properties, null, 1000, true));
		dsPool.setLogAbandoned(getPropertyBooleanValue("logAbandoned", properties, null, true, true));

		dsPool.setInitialSize(getPropertyIntegerValue("initialSize", properties, null, 2, true));
		dsPool.setMaxActive(getPropertyIntegerValue("maxActive", properties, null, 8, true));
		dsPool.setMaxIdle(getPropertyIntegerValue("maxIdle", properties, null, 8, true));
		dsPool.setMinIdle(getPropertyIntegerValue("minIdle", properties, null, 0, true));
		dsPool.setMaxWait(getPropertyIntegerValue("maxWait", properties, null, 10000, true));

		dsPool.setTimeBetweenEvictionRunsMillis(getPropertyIntegerValue("timeBetweenEvictionRunsMillis", properties, null, -1, true));

		dsPool.setTestOnBorrow(getPropertyBooleanValue("testOnBorrow", properties, null, false, true));
		dsPool.setTestOnReturn(getPropertyBooleanValue("testOnReturn", properties, null, false, true));
		dsPool.setTestWhileIdle(getPropertyBooleanValue("testWhileIdle", properties, null, false, true));

		String validationQuery = getPropertyStringValue("validationQuery", properties, null, null, false);
		if (null != validationQuery) {
			dsPool.setValidationQuery(validationQuery);
		}
		// dsPool.setValidationQueryTimeout(timeout);

		dsPool.setNumTestsPerEvictionRun(getPropertyIntegerValue("numTestsPerEvictionRun", properties, null, 10, true));
		dsPool.setMinEvictableIdleTimeMillis(getPropertyIntegerValue("minEvictableIdleTimeMillis", properties, null, 60000, true));

		// mysql:select 1
		// oracle:select 1 from dual
		// sqlserver:select 1
		// jtds:select 1

		boolean openTest = getPropertyBooleanValue("openTest", properties, null, false, false);
		if (openTest) {
			try {
				Connection conn = dsPool.getConnection();
				conn.close();
				log.info("test open database success.");
			} catch (Exception e) {
				throw new DataSourceException("test open database error: " + e.getMessage(), e);
			}
		}
		return dsPool;
	}

}
