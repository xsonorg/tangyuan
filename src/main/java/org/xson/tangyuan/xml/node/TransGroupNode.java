package org.xson.tangyuan.xml.node;

import java.sql.SQLException;

import org.xson.tangyuan.executor.SqlServiceContext;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.transaction.XTransactionDefinition;

public class TransGroupNode extends AbstractSqlNode {

	private static Log log = LogFactory.getLog(TransGroupNode.class);

	public TransGroupNode(XTransactionDefinition txDef, SqlNode sqlNode) {
		this.sqlNode = sqlNode;
		this.txDef = txDef;
	}

	@Override
	public boolean execute(SqlServiceContext context, Object arg) throws SQLException {
		try {
			// 这里只是创建事务
			context.beforeExecute(this, false);
			log.info("start trans: " + this.txDef.getId());
		} catch (Throwable e) {
			// 这里要具体处理事务了, 而且这里一定是独立的新事物
			log.error("TransGroup Exception Before", e);
			return true;
		}

		try {
			sqlNode.execute(context, arg);
			// 这里做不确定的提交
			context.commit(false);
			context.afterExecute(this);
		} catch (Throwable e) {
			// 这里要具体处理事务了, 而且这里一定是独立的新事物
			context.rollback();
			log.error("TransGroup Exception Among", e);
		}

		return true;
	}
}
