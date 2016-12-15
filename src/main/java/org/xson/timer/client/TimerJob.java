package org.xson.timer.client;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.xson.common.object.XCO;
import org.xson.rpc.RpcClient;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.logging.Log;
import org.xson.timer.server.TimerContainer;
import org.xson.timer.server.TimerRunnable;

public abstract class TimerJob implements Job {

	abstract Log getLogger();

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		TimerConfig config = (TimerConfig) dataMap.get("CONFIG");
		getLogger().info(config.getDesc() + ":" + config.getService() + " start");
		CustomJob customJob = config.getCustomJob();

		try {
			if (null == customJob) {
				final String service = config.getService();
				if (config.getRpc()) {
					XCO result = RpcClient.call(config.getRealService());
					getLogger().info(config.getDesc() + ":" + service + " end. code[" + result.getCode() + "], message[" + result.getMessage() + "]");
				} else {
					TimerContainer tc = TimerContainer.getInstance();
					if (tc.checkRunning(service)) {
						tc.execute(new TimerRunnable(service, new Runnable() {
							@Override
							public void run() {
								ServiceActuator.execute(service, new XCO());
							}
						}));
						getLogger().info(config.getDesc() + ":" + service + " end. success...");
					} else {
						getLogger().info(config.getDesc() + ":" + service + " end. ignore...");
					}
				}
			} else {
				customJob.execute(config);
			}
		} catch (Exception e) {
			getLogger().error("ERROR:" + config.getService(), e);
		}
	}

}
