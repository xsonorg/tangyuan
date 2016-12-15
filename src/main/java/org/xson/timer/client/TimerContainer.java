package org.xson.timer.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.Resources;
import org.xson.tangyuan.xml.XPathParser;
import org.xson.tangyuan.xml.XmlNodeWrapper;

public class TimerContainer {

	private static Log				log					= LogFactory.getLog(TimerContainer.class);

	private static String			JOB_GROUP_NAME		= "JOB_GROUP";
	private static String			TRIGGER_GROUP_NAME	= "TRIGGER_GROUP";

	private static TimerContainer	instance			= new TimerContainer();

	private TimerContainer() {
	}

	public static TimerContainer getInstance() {
		return instance;
	}

	private SchedulerFactory	schedulerfactory	= null;
	private Scheduler			scheduler			= null;
	private List<TimerConfig>	timerList			= null;

	public void start(String resource) throws Throwable {
		parse(resource);// "config.xml"
		schedulerfactory = new StdSchedulerFactory();
		scheduler = schedulerfactory.getScheduler();
		register();
		scheduler.start();
		log.info("timer client init successful...");
	}

	private void parse(String resource) throws Throwable {
		InputStream inputStream = Resources.getResourceAsStream(resource);
		XPathParser xPathParser = new XPathParser(inputStream);
		XmlNodeWrapper root = xPathParser.evalNode("/timer-config");
		List<XmlNodeWrapper> nodeList = root.evalNodes("timer");
		timerList = new ArrayList<TimerConfig>();
		for (XmlNodeWrapper node : nodeList) {
			String scheduled = node.getStringAttribute("scheduled").trim();

			String service = node.getStringAttribute("service");

			String desc = node.getStringAttribute("desc").trim();
			boolean sync = true;

			String _sync = node.getStringAttribute("sync");
			if (null != _sync) {
				sync = Boolean.parseBoolean(_sync);
			}

			CustomJob customJob = null;
			String custom = node.getStringAttribute("custom");
			if (null != custom) {
				Class<?> clazz = ClassUtils.forName(custom.trim());
				if (!CustomJob.class.isAssignableFrom(clazz)) {
					throw new RuntimeException("User-defined JOB must implement org.xson.timer.client.CustomJob: " + custom);
				}
				customJob = (CustomJob) clazz.newInstance();
			}

			Boolean rpc = null;
			String realService = service;

			if (null == customJob) {
				rpc = isRpc(service);
				if (rpc) {
					realService = checkServiceUrl(service);
				}
			}

			TimerConfig config = new TimerConfig(scheduled, service, realService, sync, false, desc, customJob, rpc);
			timerList.add(config);
		}
	}

	private void register() throws Throwable {
		for (int i = 0; i < timerList.size(); i++) {
			TimerConfig config = timerList.get(i);
			Class<? extends Job> jobClass = config.isSync() ? NonConcurrentJob.class : ConcurrentJob.class;
			JobDetail job = JobBuilder.newJob(jobClass).withIdentity("JOB" + i, JOB_GROUP_NAME).build();
			job.getJobDataMap().put("CONFIG", config);
			Trigger trigger = TriggerBuilder.newTrigger().withIdentity("TRIGGER" + i, TRIGGER_GROUP_NAME)
					.withSchedule(CronScheduleBuilder.cronSchedule(config.getScheduled())).startNow().build();
			scheduler.scheduleJob(job, trigger);
			log.info("add timer: " + config.getService());
		}
	}

	private String checkServiceUrl(String service) {
		String url = service.toLowerCase().substring("http://".length());
		String[] array = url.split("/");
		// System.out.println(url);
		if (3 == array.length) {
			return service + "/@/Timer";
		} else if (4 == array.length) {
			return service + "/Timer";
		} else if (5 == array.length) {
			return service;
		}
		throw new RuntimeException("Illegal service format: " + service);
	}

	/** 是否是RPC调用 */
	private boolean isRpc(String service) {
		if (service.toLowerCase().startsWith("http://")) {
			return true;
		}
		return false;
	}

	public void stop() {
		try {
			if (null != scheduler) {
				scheduler.shutdown();
				log.info("timer client container stop......");
			}
		} catch (Throwable e) {
			log.error("timer client container stop error", e);
		}
	}
}
