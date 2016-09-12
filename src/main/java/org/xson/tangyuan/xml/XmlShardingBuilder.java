package org.xson.tangyuan.xml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.datasource.DataSourceGroupVo;
import org.xson.tangyuan.datasource.DataSourceVo;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.ognl.vars.VariableParser;
import org.xson.tangyuan.ognl.vars.VariableVo;
import org.xson.tangyuan.sharding.HashShardingHandler;
import org.xson.tangyuan.sharding.ModShardingHandler;
import org.xson.tangyuan.sharding.RandomShardingHandler;
import org.xson.tangyuan.sharding.RangeShardingHandler;
import org.xson.tangyuan.sharding.ShardingDefVo;
import org.xson.tangyuan.sharding.ShardingDefVo.ShardingMode;
import org.xson.tangyuan.sharding.ShardingHandler;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.StringUtils;

public class XmlShardingBuilder {

	private Log								log						= LogFactory.getLog(getClass());

	private XPathParser						xPathParser				= null;

	private Map<String, ShardingHandler>	mappingClassMap			= new HashMap<String, ShardingHandler>();
	private Map<String, ShardingDefVo>		shardingDefMap			= new HashMap<String, ShardingDefVo>();
	private Map<String, DataSourceVo>		dataSourceVoMap;

	private ShardingHandler					hashShardingHandler		= new HashShardingHandler();
	private ShardingHandler					modShardingHandler		= new ModShardingHandler();
	private ShardingHandler					randomShardingHandler	= new RandomShardingHandler();
	private ShardingHandler					rangeShardingHandler	= new RangeShardingHandler();

	// private boolean licenses = true;

	public XmlShardingBuilder(InputStream inputStream, Map<String, DataSourceVo> dataSourceVoMap) {
		this.xPathParser = new XPathParser(inputStream);
		this.dataSourceVoMap = dataSourceVoMap;
	}

	public void parse() {
		configurationElement(xPathParser.evalNode("/sharding"));
	}

	private void configurationElement(XmlNodeWrapper context) {
		try {
			buildShardingClassNodes(context.evalNodes("shardingClass"));
			buildShardingTableNodes(context.evalNodes("table"));
			if (shardingDefMap.size() > 0) {
				TangYuanContainer.getInstance().setShardingDefMap(shardingDefMap);
			}
		} catch (Exception e) {
			throw new XmlParseException(e);
		}
	}

	private void buildShardingClassNodes(List<XmlNodeWrapper> contexts) throws Exception {
		// log.info("解析ShardingHandler:" + contexts.size());
		for (XmlNodeWrapper xNode : contexts) {
			String id = StringUtils.trim(xNode.getStringAttribute("id")); // xml validation 非空
			if (null != mappingClassMap.get(id)) {
				throw new XmlParseException("重复的shardingClass:" + id);
			}
			String className = StringUtils.trim(xNode.getStringAttribute("class")); // xml validation 非空
			Class<?> handlerClass = ClassUtils.forName(className);
			if (!ShardingHandler.class.isAssignableFrom(handlerClass)) {
				throw new XmlParseException("mapping class not implement the ShardingHandler interface: " + className);
			}
			Object handler = handlerClass.newInstance();
			mappingClassMap.put(id, (ShardingHandler) handler);
			log.info("add sharding handler: " + className);
		}
	}

	private void buildShardingTableNodes(List<XmlNodeWrapper> contexts) {
		// log.info("解析ShardingTable:" + contexts.size());
		int size = contexts.size();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);

			String name = StringUtils.trim(xNode.getStringAttribute("name"));
			if (null != shardingDefMap.get(name)) {
				throw new XmlParseException("存在的ShardingTable:" + name);
			}

			String dataSource = StringUtils.trim(xNode.getStringAttribute("dataSource"));
			// if (null != dataSource) {
			// if (!TangYuanContainer.getInstance().getDataSourceManager().isValidDsKey(dataSource)) {
			// throw new XmlParseException("无效的ShardingTable dataSource:" + dataSource);
			// }
			// }
			if (null == dataSource || 0 == dataSource.length()) {
				throw new XmlParseException("无效的ShardingTable dataSource:" + dataSource);
			}
			DataSourceVo dsVo = dataSourceVoMap.get(dataSource);
			if (null == dsVo) {
				throw new XmlParseException("不存在的dataSource:" + dataSource);
			}
			boolean dataSourceGroup = dsVo.isGroup();
			int dataSourceCount = 1;
			if (dataSourceGroup) {
				dataSourceCount = ((DataSourceGroupVo) dsVo).getCount();
			}

			ShardingMode mode = null;
			String _mode = StringUtils.trim(xNode.getStringAttribute("mode"));
			if (null != _mode) {
				mode = getShardingMode(_mode);
			}

			Integer dbCount = null;
			String _dbCount = StringUtils.trim(xNode.getStringAttribute("dbCount"));
			if (null != _dbCount) {
				dbCount = Integer.parseInt(_dbCount);
			}
			Integer tableCount = null;
			String _tableCount = StringUtils.trim(xNode.getStringAttribute("tableCount"));
			if (null != _tableCount) {
				tableCount = Integer.parseInt(_tableCount);
			}
			Integer tableCapacity = null;
			String _tableCapacity = StringUtils.trim(xNode.getStringAttribute("tableCapacity"));
			if (null != _tableCapacity) {
				tableCapacity = Integer.parseInt(_tableCapacity);
			}

			ShardingHandler handler = null;
			String impl = StringUtils.trim(xNode.getStringAttribute("impl"));
			if (null != impl) {
				handler = mappingClassMap.get(impl);
				if (null == handler) {
					throw new XmlParseException("ShardingHandler is null:" + impl);
				}
			} else {
				handler = getShardingHandler(mode);
			}

			boolean requireKeyword = true;
			String _requireKeyword = StringUtils.trim(xNode.getStringAttribute("requireKeyword"));
			if (null != _requireKeyword) {
				requireKeyword = Boolean.parseBoolean(_requireKeyword);
			}
			// random不需要关键字
			if (ShardingMode.RANDOM == mode) {
				requireKeyword = false;
			}

			VariableVo[] keywords = null;
			String keys = StringUtils.trim(xNode.getStringAttribute("keys"));
			if (null != keys) {
				String[] array = keys.split(",");
				keywords = new VariableVo[array.length];
				for (int j = 0; j < array.length; j++) {
					keywords[j] = VariableParser.parse(array[j].trim(), false);
				}
			}

			if (null == handler && (null == dbCount || null == tableCount || null == tableCapacity)) {
				throw new XmlParseException("Sharding talbe 参数不全:" + name);
			}

			boolean tableNameIndexIncrement = true;
			String _increment = StringUtils.trim(xNode.getStringAttribute("increment"));
			if (null != _increment) {
				tableNameIndexIncrement = Boolean.parseBoolean(_increment);
			}

			String defaultDataSource = null;

			ShardingDefVo shardingDefVo = new ShardingDefVo(name, dataSource, mode, dbCount, tableCount, tableCapacity, keywords,
					tableNameIndexIncrement, handler, dataSourceCount, dataSourceGroup, requireKeyword, defaultDataSource);

			shardingDefMap.put(name, shardingDefVo);
			// log.info("载入ShardingTable:" + name);
			log.info("add sharding table: " + name);
		}
	}

	private ShardingMode getShardingMode(String type) {
		if ("RANGE".equalsIgnoreCase(type)) {
			return ShardingMode.RANGE;
		} else if ("HASH".equalsIgnoreCase(type)) {
			return ShardingMode.HASH;
		} else if ("MOD".equalsIgnoreCase(type)) {
			return ShardingMode.MOD;
		} else if ("RANDOM".equalsIgnoreCase(type)) {
			return ShardingMode.RANDOM;
		}
		return null;
	}

	private ShardingHandler getShardingHandler(ShardingMode mode) {
		if (ShardingMode.RANGE == mode) {
			return rangeShardingHandler;
		} else if (ShardingMode.HASH == mode) {
			return hashShardingHandler;
		} else if (ShardingMode.MOD == mode) {
			return modShardingHandler;
		} else if (ShardingMode.RANDOM == mode) {
			return randomShardingHandler;
		}
		return null;
	}
}
