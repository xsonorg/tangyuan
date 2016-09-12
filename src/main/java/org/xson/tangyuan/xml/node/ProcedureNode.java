package org.xson.tangyuan.xml.node;

import java.sql.SQLException;

import org.xson.tangyuan.executor.SqlServiceContext;

public class ProcedureNode extends AbstractSqlNode {

	public ProcedureNode() {

	}

	@Override
	public boolean execute(SqlServiceContext context, Object arg) throws SQLException {
		return true;
	}

}
