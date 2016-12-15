package org.xson.tangyuan;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.vo.CacheVo;
import org.xson.tangyuan.datasource.DataSourceManager;
import org.xson.tangyuan.executor.ServiceContextFactory;
import org.xson.tangyuan.executor.monitor.MonitorWriter;
import org.xson.tangyuan.executor.monitor.SqlServiceMonitor;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.sharding.ShardingDefVo;
import org.xson.tangyuan.task.AsyncTask;
import org.xson.tangyuan.task.AsyncTaskThread;
import org.xson.tangyuan.type.TypeHandlerRegistry;
import org.xson.tangyuan.util.LicensesHelper;
import org.xson.tangyuan.xml.XmlConfigurationBuilder;
import org.xson.tangyuan.xml.XmlExtendBuilder;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class TangYuanContainer {

	private Log										log						= LogFactory.getLog(getClass());

	private static TangYuanContainer				instance				= new TangYuanContainer();

	private DataSourceManager						dataSourceManager		= null;
	// private final Map<String, AbstractSqlNode> sqlServices = new HashMap<String, AbstractSqlNode>();
	private final Map<String, AbstractServiceNode>	tangyuanServices		= new HashMap<String, AbstractServiceNode>();
	private Map<String, ShardingDefVo>				shardingDefMap;
	private Class<?>								defaultResultType		= XCO.class;
	private int										defaultFetchSize		= 100;
	private TypeHandlerRegistry						typeHandlerRegistry		= null;
	private AsyncTaskThread							asyncTaskThread			= null;
	private Map<String, CacheVo>					cacheVoMap				= null;

	private Map<String, ServiceContextFactory>		scFactoryMap			= new HashMap<String, ServiceContextFactory>();

	private int										sqlServiceErrorCode		= -1;											// 错误信息编码
	private String									sqlServiceErrorMessage	= "服务异常";

	private boolean									licenses				= true;

	// 服务监控
	private boolean									serviceMonitor			= false;
	private SqlServiceMonitor						monitorThread			= null;
	public long										monitorSleepTime		= 2000L;
	public long										monitorIntervalTime		= 200L;

	// public org.xson.timer.client.TimerContainer timerClientContainer = null;
	// public org.xson.timer.server.TimerContainer timerServerContainer = null;

	/**
	 * 扩展插件解析器集合
	 */
	private Map<String, XmlExtendBuilder>			builderMap				= new HashMap<String, XmlExtendBuilder>();

	private TangYuanContainer() {
	}

	public static TangYuanContainer getInstance() {
		return instance;
	}

	public DataSourceManager getDataSourceManager() {
		return dataSourceManager;
	}

	public void setDataSourceManager(DataSourceManager dataSourceManager) {
		this.dataSourceManager = dataSourceManager;
	}

	public void addService(AbstractServiceNode service) {
		tangyuanServices.put(service.getServiceKey(), service);
	}

	public ServiceContextFactory getContextFactory(TangYuanServiceType type) {
		return scFactoryMap.get(type.name());
	}

	public void registerContextFactory(TangYuanServiceType type, ServiceContextFactory factory) {
		scFactoryMap.put(type.name(), factory);
	}

	public AbstractServiceNode getService(String serviceKey) {
		return tangyuanServices.get(serviceKey);
	}

	public ShardingDefVo getShardingDef(String key) {
		return shardingDefMap.get(key);
	}

	public void setShardingDefMap(Map<String, ShardingDefVo> shardingDefMap) {
		this.shardingDefMap = shardingDefMap;
	}

	public Class<?> getDefaultResultType() {
		return defaultResultType;
	}

	public int getDefaultFetchSize() {
		return defaultFetchSize;
	}

	public TypeHandlerRegistry getTypeHandlerRegistry() {
		return typeHandlerRegistry;
	}

	public void setTypeHandlerRegistry(TypeHandlerRegistry typeHandlerRegistry) {
		this.typeHandlerRegistry = typeHandlerRegistry;
	}

	public void addAsyncTask(AsyncTask task) {
		asyncTaskThread.addTask(task);
	}

	public int getSqlServiceErrorCode() {
		return sqlServiceErrorCode;
	}

	public boolean isServiceMonitor() {
		return serviceMonitor;
	}

	public SqlServiceMonitor getMonitorThread() {
		return monitorThread;
	}

	public String getSqlServiceErrorMessage() {
		return sqlServiceErrorMessage;
	}

	public boolean hasLicenses() {
		return licenses;
	}

	public Map<String, XmlExtendBuilder> getBuilderMap() {
		return builderMap;
	}

	public void start(String resource) throws Throwable {

		try {
			licenses = LicensesHelper.check();
			if (licenses) {
				log.info("tangyuan licenses verification is successful");
			}
		} catch (Exception e) {
		}

		XmlConfigurationBuilder xmlConfigurationBuilder = new XmlConfigurationBuilder(resource);
		xmlConfigurationBuilder.parse();

		this.cacheVoMap = xmlConfigurationBuilder.getCacheVoMap();

		asyncTaskThread = new AsyncTaskThread();
		asyncTaskThread.start();

		builderMap.clear();
		builderMap = null;

		log.info("tangyuan version: " + Version.getVersion());
	}

	public void startMonitor(MonitorWriter writer) {
		if (!this.serviceMonitor) {
			this.serviceMonitor = true;
			this.monitorThread = new SqlServiceMonitor(writer);
			this.monitorThread.start();
		}
	}

	public void stop() throws Throwable {
		asyncTaskThread.stop();
		if (null != monitorThread) {
			monitorThread.stop();
		}
		if (null != this.cacheVoMap) {
			for (Entry<String, CacheVo> entry : this.cacheVoMap.entrySet()) {
				CacheVo cacheVo = entry.getValue();
				if (!cacheVo.isGroup()) {
					cacheVo.getCache().stop();
				}
			}
		}

		org.xson.timer.client.TimerContainer.getInstance().stop();
		org.xson.timer.server.TimerContainer.getInstance().stop();
	}

	// private MongoDataSource mongoDataSource = null;
	// private MongoActuator mongoActuator = null;
	// public void addSqlService(AbstractSqlNode service) {
	// sqlServices.put(service.getServiceKey(), service);
	// }
	// public AbstractSqlNode getSqlService(String serviceKey) {
	// return sqlServices.get(serviceKey);
	// }
	// public MongoDataSource getMongoDataSource(String dsKey) {
	// return mongoDataSource;
	// }
	// public MongoActuator getMongoActuator() {
	// return mongoActuator;
	// }

}
