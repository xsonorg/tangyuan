package org.xson.tangyuan.xml.node;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.executor.SqlServiceContext;
import org.xson.tangyuan.ognl.vars.VariableParser;
import org.xson.tangyuan.ognl.vars.VariableVo;
import org.xson.tangyuan.sharding.ShardingArgVo;
import org.xson.tangyuan.sharding.ShardingArgVo.ShardingTemplate;
import org.xson.tangyuan.sharding.ShardingDefVo;
import org.xson.tangyuan.sharding.ShardingResult;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.XmlParseException;
import org.xson.tangyuan.xml.parsing.GenericTokenParser;
import org.xson.tangyuan.xml.parsing.TokenHandler;

public abstract class TextNode implements SqlNode {

	// 原始字符串
	protected String				originalText	= null;

	// 预处理后的字符串
	protected String				parsedText		= null;

	// 这里存放的是#变量的内容
	protected List<VariableVo>		staticVarList	= null;

	// 这里是放分库分表的设置的
	protected List<ShardingArgVo>	shardingArgList	= null;

	protected void pretreatment() {
		// 1. 对字符串进行预处理, 把#变量预先收集, 变成动态变量
		this.parsedText = new GenericTokenParser("#{", "}", new StaticCheckerTokenHandler()).parse(originalText);
		// 2. 分库分表预处理
		this.parsedText = new GenericTokenParser("{DT:", "}", new ShardingCheckerTokenHandler(ShardingTemplate.DT)).parse(this.parsedText);
		this.parsedText = new GenericTokenParser("{T:", "}", new ShardingCheckerTokenHandler(ShardingTemplate.T)).parse(this.parsedText);
		this.parsedText = new GenericTokenParser("{DI:", "}", new ShardingCheckerTokenHandler(ShardingTemplate.DI)).parse(this.parsedText);
		this.parsedText = new GenericTokenParser("{I:", "}", new ShardingCheckerTokenHandler(ShardingTemplate.I)).parse(this.parsedText);
		this.parsedText = new GenericTokenParser("{D:", "}", new ShardingCheckerTokenHandler(ShardingTemplate.D)).parse(this.parsedText);
	}

	protected class StaticCheckerTokenHandler implements TokenHandler {
		public String handleToken(String content) {
			if (null == staticVarList) {
				staticVarList = new ArrayList<VariableVo>();
			}
			String var = StringUtils.trim(content);
			if (null == var || 0 == var.length()) {
				// TODO var is empty: #{}, 以后将支持单个单数, 数组, list, arg...
				staticVarList.add(null);
			} else {
				staticVarList.add(VariableParser.parse(var, true));
			}
			return "?";
		}
	}

	protected class ShardingCheckerTokenHandler implements TokenHandler {

		private ShardingTemplate template;

		protected ShardingCheckerTokenHandler(ShardingTemplate template) {
			this.template = template;
		}

		@Override
		public String handleToken(String content) {
			if (null == shardingArgList) {
				shardingArgList = new ArrayList<ShardingArgVo>();
			}
			String var = StringUtils.trim(content);
			if (0 == var.length()) {
				throw new XmlParseException("sharding标签不合法");// {DT:tbUser,a,b,c}
			}

			String[] array = var.split(",");
			ShardingDefVo shardingDef = TangYuanContainer.getInstance().getShardingDef(array[0]);
			if (null == shardingDef) {
				throw new XmlParseException("不存在的sharding.table:" + array[0]);
			}

			VariableVo[] keywords = shardingDef.getKeywords();
			// VariableVo[] keywords = null;

			if (shardingDef.isRequireKeyword()) {
				if (null == keywords && 1 == array.length) {
					throw new XmlParseException("不存在的sharding.table.keywords:" + array[0]);
				} else if (array.length > 1) {
					keywords = new VariableVo[array.length - 1];
					for (int i = 0; i < keywords.length; i++) {
						keywords[i] = VariableParser.parse(array[i + 1].trim(), false);
					}
				}
			}

			ShardingArgVo shardingArg = new ShardingArgVo(array[0], template, keywords, shardingDef);
			shardingArgList.add(shardingArg);
			return "{}";
		}
	}

	protected class ShardingProcessTokenHandler implements TokenHandler {

		private SqlServiceContext	context;
		// private Map<String, Object> arg;
		private Object				arg;
		private int					index	= 0;

		protected ShardingProcessTokenHandler(SqlServiceContext context, Object arg) {
			this.context = context;
			this.arg = arg;
		}

		@Override
		public String handleToken(String content) {
			ShardingArgVo shardingArg = shardingArgList.get(index++);
			ShardingResult result = shardingArg.getShardingResult(arg);
			// 设置数据源
			context.setDsKey(result.getDataSource());
			// 返回表名, 有可能为空
			return result.getTable();
		}
	}
}
