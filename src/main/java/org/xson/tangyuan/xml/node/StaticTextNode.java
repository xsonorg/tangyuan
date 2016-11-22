package org.xson.tangyuan.xml.node;

import org.xson.tangyuan.executor.ServiceContext;
import org.xson.tangyuan.xml.parsing.GenericTokenParser;

public class StaticTextNode extends TextNode {

	public StaticTextNode(String text) {
		this.originalText = text;
		pretreatment();
	}

	@Override
	public boolean execute(ServiceContext context, Object arg) {
		String sql = this.parsedText;
		// 先处理分库分表
		if (null != this.shardingArgList) {
			// 处理字符串和处理分库分表对象
			sql = new GenericTokenParser("{", "}", new ShardingProcessTokenHandler(context, arg)).parse(sql);
		}
		if (null != staticVarList) {
			context.addSql(sql);
			context.addStaticVarList(staticVarList, arg);
		} else {
			context.addSql(sql);
		}
		return true;
	}

}
