package org.xson.tangyuan;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xson.common.object.XCO;
import org.xson.tangyuan.cache.vo.CacheVo;
import org.xson.tangyuan.datasource.DataSourceManager;
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
import org.xson.tangyuan.xml.node.AbstractSqlNode;

public class TangYuanContainer {

	private Log									log						= LogFactory.getLog(getClass());

	private static TangYuanContainer			instance				= new TangYuanContainer();

	private DataSourceManager					dataSourceManager		= null;
	private final Map<String, AbstractSqlNode>	sqlServices				= new HashMap<String, AbstractSqlNode>();
	private Map<String, ShardingDefVo>			shardingDefMap;
	private Class<?>							defaultResultType		= XCO.class;
	private int									defaultFetchSize		= 100;
	private TypeHandlerRegistry					typeHandlerRegistry		= null;
	private AsyncTaskThread						asyncTaskThread			= null;
	private Map<String, CacheVo>				cacheVoMap				= null;
	// private MongoDataSource mongoDataSource = null;
	// private MongoActuator mongoActuator = null;

	private int									sqlServiceErrorCode		= -1;										// 错误信息编码
	private String								sqlServiceErrorMessage	= "服务异常";

	private boolean								licenses				= true;

	// 服务监控
	private boolean								serviceMonitor			= false;
	private SqlServiceMonitor					monitorThread			= null;
	public long									monitorSleepTime		= 2000L;
	public long									monitorIntervalTime		= 200L;
	// private long monitorSleepTime = null;

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

	public void addSqlService(AbstractSqlNode service) {
		sqlServices.put(service.getServiceKey(), service);
	}

	public AbstractSqlNode getSqlService(String serviceKey) {
		return sqlServices.get(serviceKey);
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

	// public MongoDataSource getMongoDataSource(String dsKey) {
	// return mongoDataSource;
	// }
	//
	// public MongoActuator getMongoActuator() {
	// return mongoActuator;
	// }

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

	public void start(String resource) throws Throwable {

		try {
			licenses = LicensesHelper.check();
			if (licenses) {
				log.info("tangyuan licenses verification is successful");
			}
		} catch (Exception e) {
		}

		// InputStream inputStream = Resources.getResourceAsStream(resource);
		XmlConfigurationBuilder xmlConfigurationBuilder = new XmlConfigurationBuilder(resource);
		xmlConfigurationBuilder.parse();

		this.cacheVoMap = xmlConfigurationBuilder.getCacheVoMap();

		asyncTaskThread = new AsyncTaskThread();
		asyncTaskThread.start();

		log.info("tangyuan version: " + Version.getVersionNumber());
	}

	public void startMonitor(MonitorWriter writer) {
		// if (isServiceMonitor()) {
		// this.monitorThread = new SqlServiceMonitor(writer);
		// this.monitorThread.start();
		// }
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
	}
}
