package org.xson.tangyuan.ognl.vars.warper;

import org.xson.tangyuan.ognl.vars.ParserWarper;
import org.xson.tangyuan.ognl.vars.Variable;
import org.xson.tangyuan.ognl.vars.VariableConfig;
import org.xson.tangyuan.ognl.vars.parser.ShardingParser;
import org.xson.tangyuan.sharding.ShardingArgVo.ShardingTemplate;

/**
 * SQL分库分表变量解析包装
 */
public class SqlShardingParserWarper extends ParserWarper {

	private ShardingTemplate template;

	public SqlShardingParserWarper(ShardingTemplate template) {
		this.template = template;
	}

	@Override
	public Variable parse(String text, VariableConfig config) {
		text = text.trim();
		return new ShardingParser(this.template).parse(text);
	}
}
