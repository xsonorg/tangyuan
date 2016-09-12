package org.xson.tangyuan.datasource;

import java.util.Map;

public class DataSourceVo {

	public enum ConnPoolType {
		JNDI, C3P0, DBCP, PROXOOL, DRUID;
	}

	private String				id;
	private Map<String, String>	properties;
	private boolean				defaultDs	= false;
	protected ConnPoolType		type;
	protected boolean			group		= false;

	public DataSourceVo(String id, ConnPoolType type, boolean defaultDs, Map<String, String> properties) {
		this.id = id;
		this.type = type;
		this.defaultDs = defaultDs;
		this.properties = properties;
	}

	public String getId() {
		return id;
	}

	protected ConnPoolType getType() {
		return type;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public boolean isGroup() {
		return group;
	}

	public boolean isDefaultDs() {
		return defaultDs;
	}
}
