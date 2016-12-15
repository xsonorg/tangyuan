package org.xson.tangyuan.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.cache.ICache;
import org.xson.tangyuan.cache.vo.CacheGroupVo;
import org.xson.tangyuan.cache.vo.CacheRefVo;
import org.xson.tangyuan.cache.vo.CacheVo;
import org.xson.tangyuan.cache.vo.CacheVo.CacheType;
import org.xson.tangyuan.datasource.AbstractDataSource;
import org.xson.tangyuan.datasource.DataSourceCreater;
import org.xson.tangyuan.datasource.DataSourceException;
import org.xson.tangyuan.datasource.DataSourceGroupVo;
import org.xson.tangyuan.datasource.DataSourceManager;
import org.xson.tangyuan.datasource.DataSourceVo;
import org.xson.tangyuan.datasource.DataSourceVo.ConnPoolType;
import org.xson.tangyuan.datasource.MuiltDataSourceManager;
import org.xson.tangyuan.datasource.SimpleDataSourceManager;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.transaction.XTransactionDefinition;
import org.xson.tangyuan.type.TypeHandler;
import org.xson.tangyuan.type.TypeHandlerRegistry;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.Resources;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.node.TangYuanNode;
import org.xson.tangyuan.xml.node.XMLJavaNodeBuilder;
import org.xson.tangyuan.xml.node.XMLSqlNodeBuilder;

public class XmlConfigurationBuilder {

	private Log							log						= LogFactory.getLog(getClass());
	private XPathParser					xPathParser				= null;

	private Map<String, DataSourceVo>	dataSourceVoMap			= new HashMap<String, DataSourceVo>();
	private boolean						hasDefaultDataSource	= false;

	// private XmlMapperBuilder xmlMapperBuilder = null;
	// private XmlShardingBuilder xmlShardingBuilder = null;
	// private List<XMLSqlNodeBuilder> xmlsqlNodeBuilderList = null;
	// private DefaultTransactionMatcher transactionMatcher = new DefaultTransactionMatcher();

	private CacheVo						defaultCacheVo			= null;
	private Map<String, ICache>			cacheHandlerMap			= new HashMap<String, ICache>();
	private Map<String, CacheVo>		cacheVoMap				= new HashMap<String, CacheVo>();

	// private boolean licenses = true;

	/** 解析内容上下文 */
	private XmlParseContext				context					= new XmlParseContext();

	public XmlConfigurationBuilder(String resource) throws Throwable {
		InputStream inputStream = Resources.getResourceAsStream(resource);
		this.xPathParser = new XPathParser(inputStream);
		log.info("Start parsing: " + resource);
	}

	public void parse() {
		configurationElement(xPathParser.evalNode("/configuration"));
	}

	private void configurationElement(XmlNodeWrapper context) {
		try {
			// 先解析默认设置
			List<DataSourceVo> dsList = buildDataSourceNodes(context.evalNodes("dataSource"));// 解析dataSource
			List<DataSourceVo> dsGroupList = buildDataSourceGroupNodes(context.evalNodes("dataSourceGroup"));// 解析dataSourceGroup
			addDataSource(dsList, dsGroupList);
			buildTransactionNodes(context.evalNodes("transaction"));// 解析transaction
			buildSetDefaultTransaction(context.evalNodes("setDefaultTransaction"));// 解析默认的transaction

			buildCacheClass(context.evalNodes("cacheClass")); // 解析缓存处理器
			buildCache(context.evalNodes("cache")); // 解析缓存定义
			buildCacheGroup(context.evalNodes("cacheGroup")); // 解析缓存组定义
			setDefaultCache();
			startCache();// 需要启动Cache

			this.context.setDefaultCacheVo(defaultCacheVo);
			this.context.setCacheVoMap(cacheVoMap);

			buildMapperNodes(context.evalNodes("mapper"));
			buildShardingNodes(context.evalNodes("sharding"));
			buildPluginNodes(context.evalNodes("plugin"));

			// 扩展插件
			buildMongoExtendNodes(context.evalNodes("mongo-extend"));
			buildTimerServerExtendNodes(context.evalNodes("timer-server-extend"));
			buildTimerClientExtendNodes(context.evalNodes("timer-client-extend"));

			// clean
			this.context.clean();
		} catch (Throwable e) {
			throw new XmlParseException(e);
		}
	}

	protected void buildCacheClass(List<XmlNodeWrapper> contexts) throws Exception {
		int size = contexts.size();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("id"));
			if (null != cacheHandlerMap.get(id)) {
				throw new XmlParseException("重复的cacheClass: " + id);
			}
			String className = StringUtils.trim(xNode.getStringAttribute("class"));
			Class<?> handlerClass = ClassUtils.forName(className);
			if (!ICache.class.isAssignableFrom(handlerClass)) {
				throw new XmlParseException("cache class not implement the ICache interface: " + className);
			}
			ICache handler = (ICache) handlerClass.newInstance();
			cacheHandlerMap.put(id, handler);
			log.info("add cache handler: " + className);
		}
	}

	private void buildCache(List<XmlNodeWrapper> contexts) throws Exception {
		int size = contexts.size();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("id"));// xml
																			// validation
			if (cacheVoMap.containsKey(id)) {
				throw new XmlParseException("重复的cache:" + id);
			}
			String _type = StringUtils.trim(xNode.getStringAttribute("type"));
			CacheType type = null;
			if (null != _type) {
				type = getCacheType(_type);
			}

			String className = StringUtils.trim(xNode.getStringAttribute("class"));
			ICache handler = null;
			if (null != className) {
				Class<?> handlerClass = ClassUtils.forName(className);
				if (!ICache.class.isAssignableFrom(handlerClass)) {
					throw new XmlParseException("cache class not implement the ICache interface: " + className);
				}
				handler = (ICache) handlerClass.newInstance();
			}

			String _defaultCache = StringUtils.trim(xNode.getStringAttribute("default"));
			boolean defaultCache = false;
			if (null != _defaultCache) {
				defaultCache = Boolean.parseBoolean(_defaultCache);
				if (defaultCache && null != this.defaultCacheVo) {
					throw new XmlParseException("默认cache已经存在:" + id);
				}
				// if (defaultCache) {
				// this.hasDefaultCache = true;
				// }
			}

			// resource="tangyuan-mapper.xml"
			String resource = StringUtils.trim(xNode.getStringAttribute("resource"));

			Map<String, String> propertiesMap = new HashMap<String, String>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			for (XmlNodeWrapper propertyNode : properties) {
				propertiesMap.put(StringUtils.trim(propertyNode.getStringAttribute("name")),
						StringUtils.trim(propertyNode.getStringAttribute("value")));
			}

			if (null == handler && null == type) {
				throw new XmlParseException("缓存类型和缓存处理器不能都为空");
			}

			CacheVo cVo = new CacheVo(id, type, handler, defaultCache, resource, propertiesMap);
			cacheVoMap.put(id, cVo);
			log.info("add cache: " + id);

			if (defaultCache) {
				this.defaultCacheVo = cVo;
			}
		}
	}

	private void buildCacheGroup(List<XmlNodeWrapper> contexts) {
		int size = contexts.size();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("id"));// xml
																			// validation
			if (cacheVoMap.containsKey(id)) {
				throw new XmlParseException("重复的cache:" + id);
			}
			String _defaultCache = StringUtils.trim(xNode.getStringAttribute("default"));
			boolean defaultCache = false;
			if (null != _defaultCache) {
				defaultCache = Boolean.parseBoolean(_defaultCache);
				if (defaultCache && null != this.defaultCacheVo) {
					throw new XmlParseException("默认cache已经存在:" + id);
				}
			}

			List<CacheRefVo> cacheRefList = new ArrayList<CacheRefVo>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("cache");
			for (XmlNodeWrapper propertyNode : properties) {
				String _ref = StringUtils.trim(propertyNode.getStringAttribute("ref"));
				String _include = StringUtils.trim(propertyNode.getStringAttribute("include"));
				String _exclude = StringUtils.trim(propertyNode.getStringAttribute("exclude"));
				CacheVo cacheVo = cacheVoMap.get(_ref);
				if (null == cacheVo || cacheVo.isGroup()) {
					throw new XmlParseException("cache引用无效: " + _ref);
				}
				String[] include = null;
				if (null != _include && _include.trim().length() > 0) {
					include = _include.split(",");
				}
				String[] exclude = null;
				if (null != _exclude && _exclude.trim().length() > 0) {
					exclude = _exclude.split(",");
				}
				CacheRefVo refVo = new CacheRefVo(cacheVo, include, exclude);
				cacheRefList.add(refVo);
			}

			if (0 == cacheRefList.size()) {
				throw new XmlParseException("引用的缓存不能为空");
			}

			CacheVo cVo = new CacheGroupVo(id, defaultCache, cacheRefList);
			cacheVoMap.put(id, cVo);
			log.info("add cache group: " + id);

			if (defaultCache) {
				this.defaultCacheVo = cVo;
			}
		}
	}

	private void setDefaultCache() {
		if (1 == cacheVoMap.size()) {
			for (Map.Entry<String, CacheVo> entry : cacheVoMap.entrySet()) {
				entry.getValue().setDefaultCache(true);
				// 设置默认的cache defaultCacheVo
				this.defaultCacheVo = entry.getValue();
				return;
			}
		}
	}

	/** 启动Cache */
	private void startCache() {
		for (Map.Entry<String, CacheVo> entry : cacheVoMap.entrySet()) {
			entry.getValue().start();
			log.info("cache start: " + entry.getValue().getId());
		}
	}

	private void buildSetDefaultTransaction(List<XmlNodeWrapper> contexts) {
		int size = contexts.size();
		if (size > 1) {
			throw new XmlParseException("默认事务设置只能有一项");
		}
		if (size == 0) {
			return;
		}
		XmlNodeWrapper xNode = contexts.get(0);
		String type = StringUtils.trim(xNode.getStringAttribute("type"));
		List<XmlNodeWrapper> properties = xNode.evalNodes("property");
		List<String[]> ruleList = new ArrayList<String[]>();
		for (XmlNodeWrapper propertyNode : properties) {
			// TODO check value
			ruleList.add(new String[] { StringUtils.trim(propertyNode.getStringAttribute("name")),
					StringUtils.trim(propertyNode.getStringAttribute("value")) });
		}
		// transactionMatcher.setTypeAndRule(type, ruleList);
		context.getTransactionMatcher().setTypeAndRule(type, ruleList);
		log.info("add default transaction rule, type is " + type);
	}

	private List<DataSourceVo> buildDataSourceNodes(List<XmlNodeWrapper> contexts) {
		int size = contexts.size();
		List<DataSourceVo> dsList = new ArrayList<DataSourceVo>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("id"));
			if (dataSourceVoMap.containsKey(id)) {
				throw new XmlParseException("重复的数据源ID: " + id);
			}
			String tmp = StringUtils.trim(xNode.getStringAttribute("type"));
			ConnPoolType type = getConnPoolType(tmp);
			if (null == type) {
				throw new XmlParseException("无效的数据源类型");
			}
			boolean defaultDs = false;
			tmp = StringUtils.trim(xNode.getStringAttribute("isDefault"));
			if (null != tmp) {
				if ("true".equalsIgnoreCase(tmp)) {
					if (this.hasDefaultDataSource) {
						throw new XmlParseException("默认的数据源只能存在一个");
					} else {
						this.hasDefaultDataSource = true;
					}
					defaultDs = true;
				}
			}
			Map<String, String> data = new HashMap<String, String>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			for (XmlNodeWrapper propertyNode : properties) {
				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
						StringUtils.trim(propertyNode.getStringAttribute("value")));
			}
			DataSourceVo dsVo = new DataSourceVo(id, type, defaultDs, data);
			dsList.add(dsVo);
			dataSourceVoMap.put(id, dsVo);
		}
		return dsList;
	}

	private List<DataSourceVo> buildDataSourceGroupNodes(List<XmlNodeWrapper> contexts) {
		// log.info("解析数据源:" + contexts.size());
		int size = contexts.size();
		List<DataSourceVo> dsList = new ArrayList<DataSourceVo>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("groupId"));
			if (dataSourceVoMap.containsKey(id)) {
				throw new XmlParseException("重复的数据源ID:" + id);
			}
			String tmp = StringUtils.trim(xNode.getStringAttribute("type")); // xml
																				// validation
			ConnPoolType type = getConnPoolType(tmp);
			if (null == type) {
				throw new XmlParseException("无效的数据源类型");
			}
			tmp = StringUtils.trim(xNode.getStringAttribute("start")); // xml
																		// validation
			int start = 0;
			if (null != tmp) {
				start = Integer.parseInt(tmp);
			}
			tmp = StringUtils.trim(xNode.getStringAttribute("end")); // xml
																		// validation
			int end = Integer.parseInt(tmp);
			boolean defaultDs = false;
			tmp = StringUtils.trim(xNode.getStringAttribute("isDefault"));
			if (null != tmp) {
				if ("true".equalsIgnoreCase(tmp)) {
					if (this.hasDefaultDataSource) {
						throw new XmlParseException("默认的数据源只能存在一个");
					} else {
						this.hasDefaultDataSource = true;
					}
					defaultDs = true;
				}
			}

			Map<String, String> data = new HashMap<String, String>();
			List<XmlNodeWrapper> properties = xNode.evalNodes("property");
			for (XmlNodeWrapper propertyNode : properties) {
				data.put(StringUtils.trim(propertyNode.getStringAttribute("name")).toUpperCase(),
						StringUtils.trim(propertyNode.getStringAttribute("value")));
			}
			DataSourceGroupVo dsGroupVo = new DataSourceGroupVo(id, type, defaultDs, data, start, end);
			dsList.add(dsGroupVo);

			dataSourceVoMap.put(id, dsGroupVo);
		}
		return dsList;
	}

	private void addDataSource(List<DataSourceVo> dsList, List<DataSourceVo> dsGroupList) throws Exception {
		List<DataSourceVo> allList = new ArrayList<DataSourceVo>();
		if (null != dsList && dsList.size() > 0) {
			allList.addAll(dsList);
		}
		if (null != dsGroupList && dsGroupList.size() > 0) {
			allList.addAll(dsGroupList);
		}
		if (0 == allList.size()) {
			throw new XmlParseException("没有数据源");
		}

		Map<String, String> decryptProperties = null;
		DataSourceManager dataSourceManager = null;
		// 简单唯一数据源
		if (1 == allList.size() && !allList.get(0).isGroup()) {
			DataSourceVo dsVo = allList.get(0);
			AbstractDataSource dataSource = new DataSourceCreater().create(dsVo, decryptProperties);
			dataSourceManager = new SimpleDataSourceManager(dataSource, dsVo.getId());
			log.info("add datasource: " + dsVo.getId());
		} else {
			Map<String, DataSourceVo> logicDataSourceMap = new HashMap<String, DataSourceVo>();
			Map<String, AbstractDataSource> realDataSourceMap = new HashMap<String, AbstractDataSource>();
			String _defaultDsKey = null;
			for (DataSourceVo dsVo : allList) {
				if (dsVo.isGroup()) {
					new DataSourceCreater().create((DataSourceGroupVo) dsVo, realDataSourceMap, decryptProperties);
				} else {
					AbstractDataSource dataSource = new DataSourceCreater().create(dsVo, decryptProperties);
					if (realDataSourceMap.containsKey(dataSource.getRealDataSourceId())) {
						throw new DataSourceException("重复的DataSourceID:" + dataSource.getRealDataSourceId());
					}
					realDataSourceMap.put(dataSource.getRealDataSourceId(), dataSource);
				}
				if (logicDataSourceMap.containsKey(dsVo.getId())) {
					throw new DataSourceException("重复的DataSourceID:" + dsVo.getId());
				}
				logicDataSourceMap.put(dsVo.getId(), dsVo);
				if (dsVo.isDefaultDs()) {
					_defaultDsKey = dsVo.getId();
				}
				log.info("add datasource: " + dsVo.getId());
			}
			dataSourceManager = new MuiltDataSourceManager(logicDataSourceMap, realDataSourceMap, _defaultDsKey);
		}
		TangYuanContainer.getInstance().setDataSourceManager(dataSourceManager);
	}

	private void buildTransactionNodes(List<XmlNodeWrapper> contexts) {
		// log.info("解析事务定义:" + contexts.size());
		int size = contexts.size();
		if (0 == size) {
			return;
		}
		Map<String, XTransactionDefinition> transactionMap = new HashMap<String, XTransactionDefinition>();
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String id = StringUtils.trim(xNode.getStringAttribute("id"));
			if (null != transactionMap.get(id)) {
				throw new XmlParseException("重复的事务定义:" + id);
			}
			String name = StringUtils.trim(xNode.getStringAttribute("name"));

			String tmp = StringUtils.trim(xNode.getStringAttribute("behavior"));
			Integer behavior = null;
			if (null != tmp) {
				behavior = getBehaviorValue(tmp);
			}

			tmp = StringUtils.trim(xNode.getStringAttribute("isolation"));
			Integer isolation = null;
			if (null != tmp) {
				isolation = getIsolationValue(tmp);
			}

			tmp = StringUtils.trim(xNode.getStringAttribute("timeout"));
			Integer timeout = null;
			if (null != tmp) {
				timeout = Integer.parseInt(tmp);
			}

			tmp = StringUtils.trim(xNode.getStringAttribute("readOnly"));
			Boolean readOnly = null;
			if (null != tmp) {
				readOnly = Boolean.parseBoolean(tmp);
			}

			XTransactionDefinition transactionDefinition = new XTransactionDefinition(id, name, behavior, isolation, timeout, readOnly);
			transactionMap.put(id, transactionDefinition);

			// log.info("载入事务定义:" + id);
			log.info("add transaction definition: " + id);
		}

		// transactionMatcher.setTransactionMap(transactionMap);
		this.context.getTransactionMatcher().setTransactionMap(transactionMap);
	}

	/**
	 * 解析mapper
	 */
	private void buildMapperNodes(List<XmlNodeWrapper> contexts) throws Exception {
		int size = contexts.size();
		if (size == 0) {
			Map<String, TypeHandler<?>> jdbcTypeMap = new HashMap<String, TypeHandler<?>>();
			TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
			typeHandlerRegistry.init(jdbcTypeMap);
			TangYuanContainer.getInstance().setTypeHandlerRegistry(typeHandlerRegistry);
			return;
		}
		if (size > 1) {
			throw new XmlParseException("mapper只能有一项");
		}
		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource")); // xml
																					// v
		log.info("Start parsing: " + resource);
		InputStream inputStream = Resources.getResourceAsStream(resource);
		XmlMapperBuilder xmlMapperBuilder = new XmlMapperBuilder(inputStream);
		xmlMapperBuilder.parse();
		// add context
		this.context.setMappingVoMap(xmlMapperBuilder.getMappingVoMap());
	}

	/**
	 * 解析Sharding
	 */
	private void buildShardingNodes(List<XmlNodeWrapper> contexts) throws Exception {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		if (size > 1) {
			throw new XmlParseException("sharding只能有一项");
		}
		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource")); // xml
																					// v
		log.info("Start parsing: " + resource);
		InputStream inputStream = Resources.getResourceAsStream(resource);
		XmlShardingBuilder xmlShardingBuilder = new XmlShardingBuilder(inputStream, dataSourceVoMap);
		xmlShardingBuilder.parse();
	}

	private void buildPluginNodes(List<XmlNodeWrapper> contexts) throws Exception {
		int size = contexts.size();
		if (size == 0) {
			return;
		}
		List<String> resourceList = new ArrayList<String>();
		List<XmlNodeBuilder> xmlNodeBuilderList = new ArrayList<XmlNodeBuilder>();

		// 防止重复, 全局控制
		Map<String, TangYuanNode> integralRefMap = new HashMap<String, TangYuanNode>();
		Map<String, Integer> integralServiceMap = new HashMap<String, Integer>();

		context.setIntegralRefMap(integralRefMap);
		context.setIntegralServiceMap(integralServiceMap);

		// 扫描所有的<SQL>
		for (int i = 0; i < size; i++) {
			XmlNodeWrapper xNode = contexts.get(i);
			String resource = StringUtils.trim(xNode.getStringAttribute("resource")); // xml
																						// v
			log.info("Start parsing: " + resource);
			InputStream inputStream = Resources.getResourceAsStream(resource);
			// XMLSqlNodeBuilder xmlSqlNodeBuilder = new XMLSqlNodeBuilder(inputStream, this, xmlMapperBuilder, integralRefMap, integralServiceMap);

			XPathParser parser = new XPathParser(inputStream);
			XmlNodeBuilder nodeBuilder = getXmlNodeBuilder(parser);

			nodeBuilder.parseRef();
			xmlNodeBuilderList.add(nodeBuilder);
			resourceList.add(resource);
		}

		// 注册所有的服务
		for (int i = 0; i < size; i++) {
			log.info("Start parsing: " + resourceList.get(i));
			xmlNodeBuilderList.get(i).parseService();
		}
	}

	private XmlNodeBuilder getXmlNodeBuilder(XPathParser parser) {
		XmlNodeWrapper _root = null;
		if (null != (_root = parser.evalNode("/sqlservices"))) {
			XmlNodeBuilder nodeBuilder = new XMLSqlNodeBuilder();
			nodeBuilder.setContext(_root, context);
			return nodeBuilder;
		} else if (null != (_root = parser.evalNode("/javaservices"))) {
			XmlNodeBuilder nodeBuilder = new XMLJavaNodeBuilder();
			nodeBuilder.setContext(_root, context);
			return nodeBuilder;
		} else {
			throw new XmlParseException("Unsupported root node in the service plug-in");
		}
	}

	// 扫描Mongo扩展插件
	private void buildMongoExtendNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		if (contexts.size() == 0) {
			return;
		}
		if (contexts.size() > 1) {
			throw new XmlParseException("Only one mongo plugin is allowed");
		}

		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource"));

		// TODO: 调用静态代码库
		Class.forName("org.xson.tangyuan.TangYuanMongoContainer");

		XmlExtendBuilder extendBuilder = TangYuanContainer.getInstance().getBuilderMap().get("mongo");
		if (null == extendBuilder) {
			throw new XmlParseException("Missing mongo extension plugin builder");
		}

		extendBuilder.parse(this, resource);
	}

	private void buildTimerClientExtendNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		if (contexts.size() == 0) {
			return;
		}
		if (contexts.size() > 1) {
			throw new XmlParseException("Only one timer client plugin is allowed");
		}

		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource"));

		org.xson.timer.client.TimerContainer.getInstance().start(resource);
		log.info("timer client start successful...");
	}

	private void buildTimerServerExtendNodes(List<XmlNodeWrapper> contexts) throws Throwable {
		if (contexts.size() == 0) {
			return;
		}
		if (contexts.size() > 1) {
			throw new XmlParseException("Only one timer server plugin is allowed");
		}

		XmlNodeWrapper xNode = contexts.get(0);
		String resource = StringUtils.trim(xNode.getStringAttribute("resource"));

		org.xson.timer.server.TimerContainer.getInstance().start(resource);
		log.info("timer server start successful...");
	}

	private ConnPoolType getConnPoolType(String type) {
		if ("JNDI".equalsIgnoreCase(type)) {
			return ConnPoolType.JNDI;
		} else if ("C3P0".equalsIgnoreCase(type)) {
			return ConnPoolType.C3P0;
		} else if ("DBCP".equalsIgnoreCase(type)) {
			return ConnPoolType.DBCP;
		} else if ("PROXOOL".equalsIgnoreCase(type)) {
			return ConnPoolType.PROXOOL;
		} else if ("DRUID".equalsIgnoreCase(type)) {
			return ConnPoolType.DRUID;
		}
		return null;
	}

	private int getBehaviorValue(String str) {
		if ("REQUIRED".equalsIgnoreCase(str)) {
			return XTransactionDefinition.PROPAGATION_REQUIRED;
		} else if ("SUPPORTS".equalsIgnoreCase(str)) {
			return XTransactionDefinition.PROPAGATION_SUPPORTS;
		} else if ("MANDATORY".equalsIgnoreCase(str)) {
			return XTransactionDefinition.PROPAGATION_MANDATORY;
		} else if ("REQUIRES_NEW".equalsIgnoreCase(str)) {
			return XTransactionDefinition.PROPAGATION_REQUIRES_NEW;
		} else if ("NOT_SUPPORTED".equalsIgnoreCase(str)) {
			return XTransactionDefinition.PROPAGATION_NOT_SUPPORTED;
		} else if ("NEVER".equalsIgnoreCase(str)) {
			return XTransactionDefinition.PROPAGATION_NEVER;
		} else if ("NESTED".equalsIgnoreCase(str)) {
			return XTransactionDefinition.PROPAGATION_NESTED;
		} else {
			return XTransactionDefinition.PROPAGATION_REQUIRED;
		}
	}

	private int getIsolationValue(String str) {
		if ("READ_UNCOMMITTED".equalsIgnoreCase(str)) {
			return XTransactionDefinition.ISOLATION_READ_UNCOMMITTED;
		} else if ("READ_COMMITTED".equalsIgnoreCase(str)) {
			return XTransactionDefinition.ISOLATION_READ_COMMITTED;
		} else if ("REPEATABLE_READ".equalsIgnoreCase(str)) {
			return XTransactionDefinition.ISOLATION_REPEATABLE_READ;
		} else if ("SERIALIZABLE".equalsIgnoreCase(str)) {
			return XTransactionDefinition.ISOLATION_SERIALIZABLE;
		} else {
			return XTransactionDefinition.ISOLATION_DEFAULT;
		}
	}

	private CacheType getCacheType(String str) {
		if ("LOCAL".equalsIgnoreCase(str)) {
			return CacheType.LOCAL;
		} else if ("EHCACHE".equalsIgnoreCase(str)) {
			return CacheType.EHCACHE;
		} else if ("MEMCACHE".equalsIgnoreCase(str)) {
			return CacheType.MEMCACHE;
		} else if ("REDIS".equalsIgnoreCase(str)) {
			return CacheType.REDIS;
		} else {
			return null;
		}
	}

	// public DefaultTransactionMatcher getTransactionMatcher() {
	// return transactionMatcher;
	// }

	public Map<String, DataSourceVo> getDataSourceVoMap() {
		return dataSourceVoMap;
	}

	public Map<String, CacheVo> getCacheVoMap() {
		return cacheVoMap;
	}

	public CacheVo getDefaultCacheVo() {
		return defaultCacheVo;
	}

}
