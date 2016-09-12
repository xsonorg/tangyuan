package org.xson.tangyuan.datasource;

import java.util.Map;

public class DataSourceGroupVo extends DataSourceVo {

	private int	start;
	private int	end;
	private int	count;

	public DataSourceGroupVo(String id, ConnPoolType type, boolean defaultDs, Map<String, String> properties, int start, int end) {
		super(id, type, defaultDs, properties);
		this.start = start;
		this.end = end;
		this.count = this.end - this.start + 1;
		this.group = true;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getCount() {
		return count;
	}

}
