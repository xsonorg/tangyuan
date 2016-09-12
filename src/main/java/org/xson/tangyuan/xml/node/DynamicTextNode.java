package org.xson.tangyuan.xml.node;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.executor.SqlServiceContext;
import org.xson.tangyuan.ognl.vars.VariableParser;
import org.xson.tangyuan.ognl.vars.VariableVo;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.parsing.GenericTokenParser;
import org.xson.tangyuan.xml.parsing.TokenHandler;

public class DynamicTextNode extends TextNode {

	// 这里存放的是$变量的内容
	protected List<VariableVo> dynamicVarList = null;

	public DynamicTextNode(String text) {
		this.originalText = text;
	}

	public boolean isDynamic() {
		DynamicCheckerTokenParser checker = new DynamicCheckerTokenParser();
		new GenericTokenParser("${", "}", checker).parse(originalText);
		boolean dynamic = checker.isDynamic();
		if (dynamic) {
			pretreatment();
		}
		return dynamic;
	}

	private class DynamicCheckerTokenParser implements TokenHandler {

		private boolean isDynamic;

		public boolean isDynamic() {
			return isDynamic;
		}

		public String handleToken(String content) {
			if (null == dynamicVarList) {
				dynamicVarList = new ArrayList<VariableVo>();
			}
			String var = StringUtils.trim(content);
			if (null == var || 0 == var.length()) {
				dynamicVarList.add(null);
			} else {
				dynamicVarList.add(VariableParser.parse(var, true));
			}
			this.isDynamic = true;
			return null;
		}
	}

	private class DynamicTokenParser implements TokenHandler {

		// private Map<String, Object> arg;
		private Object	arg;

		private int		index	= 0;

		// protected DynamicTokenParser(Map<String, Object> arg) {
		protected DynamicTokenParser(Object arg) {
			this.arg = arg;
		}

		public String handleToken(String content) {
			Object value = dynamicVarList.get(index++).getValue(arg);
			return (value == null ? "" : String.valueOf(value)); // issue #274 return "" instead of "null"
		}
	}

	@Override
	public boolean execute(SqlServiceContext context, Object arg) {
		// 1. 替换动态变量
		String sql = new GenericTokenParser("${", "}", new DynamicTokenParser(arg)).parse(this.parsedText);
		// 1. 替换分库分表
		if (null != this.shardingArgList) {
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
