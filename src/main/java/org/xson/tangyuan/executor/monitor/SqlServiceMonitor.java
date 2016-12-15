package org.xson.tangyuan.executor.monitor;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.util.DateUtils;

/**
 * 服务监控线程
 */
public class SqlServiceMonitor {

	private Log									log				= LogFactory.getLog(SqlServiceMonitor.class);

	private Map<Integer, SqlServiceContextInfo>	contextInfoMap	= null;

	private volatile boolean					running			= false;

	private InnerMonitorThread					thread			= null;

	private MonitorWriter						writer			= null;

	public SqlServiceMonitor(MonitorWriter writer) {
		this.writer = writer;
	}

	private class InnerMonitorThread extends Thread {

		protected InnerMonitorThread() {
			super("InnerMonitorThread");
			setDaemon(true);
		}

		@Override
		public void run() {
			while (running) {
				StringBuilder builder = new StringBuilder();
				builder.append("Monitor at: " + DateUtils.getDateTimeString(new Date()));
				builder.append("----------------------------\n");
				Set<Map.Entry<Integer, SqlServiceContextInfo>> set = contextInfoMap.entrySet();
				for (Map.Entry<Integer, SqlServiceContextInfo> entry : set) {
					SqlServiceContextInfo contextInfo = entry.getValue();
					String info = contextInfo.toString();
					if (null != info) {
						builder.append(info);
						builder.append("\n");
					}
					if (!contextInfo.isRunning()) {
						contextInfoMap.remove(entry.getKey());
					}
				}

				builder.append("\n");

				// log
				flush(builder);

				try {
					sleep(TangYuanContainer.getInstance().monitorSleepTime);
				} catch (InterruptedException e) {
					log.error(null, e);
					return;
				}
			}
		}
	}

	private void flush(StringBuilder builder) {
		this.writer.writer(builder);
	}

	public void add(SqlServiceContextInfo contextInfo) {
		contextInfoMap.put(contextInfo.hashCode(), contextInfo);
	}

	public void start() {
		if (!running) {
			running = true;
			contextInfoMap = new ConcurrentHashMap<Integer, SqlServiceContextInfo>();
			thread = new InnerMonitorThread();
			thread.start();
			log.info("InnerMonitorThread start...");
		}
	}

	public void stop() {
		if (running) {
			running = false;
			log.info("InnerMonitorThread stop...");
		}
	}

}
