package org.xson.timer.server;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.util.Resources;

public class TimerContainer {

	private Log						log			= LogFactory.getLog(getClass());

	private static TimerContainer	instance	= new TimerContainer();

	private TimerContainer() {
	}

	public static TimerContainer getInstance() {
		return instance;
	}

	private TimerControlerHandler	handler		= null;

	private ThreadPoolExecutor		threadPool	= null;

	public void start(String resource) throws Throwable {

		Properties properties = new Properties();
		if (null != resource) {
			InputStream inputStream = Resources.getResourceAsStream(resource);
			properties.load(inputStream);
			inputStream.close();
		}

		// 核心线程池大小----10
		int corePoolSize = 10;
		if (properties.containsKey("corePoolSize")) {
			corePoolSize = Integer.parseInt(properties.getProperty("corePoolSize").trim());
		}
		// maximumPoolSize 最大线程池大小----30
		int maximumPoolSize = 30;
		if (properties.containsKey("maximumPoolSize")) {
			maximumPoolSize = Integer.parseInt(properties.getProperty("maximumPoolSize").trim());
		}

		// 线程池中超过corePoolSize数目的空闲线程最大存活时间
		int keepAliveTime = 3;
		if (properties.containsKey("keepAliveTime")) {
			keepAliveTime = Integer.parseInt(properties.getProperty("keepAliveTime").trim());
		}

		int queueCapacity = 20;
		if (properties.containsKey("queueCapacity")) {
			queueCapacity = Integer.parseInt(properties.getProperty("queueCapacity").trim());
		}

		// 存活单位TimeUnit
		TimeUnit timeUnit = TimeUnit.MINUTES;
		if (properties.containsKey("timeUnit")) {
			String _timeUnit = properties.getProperty("timeUnit").trim();
			if ("DAYS".equalsIgnoreCase(_timeUnit)) {
				timeUnit = TimeUnit.DAYS;
			} else if ("HOURS".equalsIgnoreCase(_timeUnit)) {
				timeUnit = TimeUnit.HOURS;
			} else if ("SECONDS".equalsIgnoreCase(_timeUnit)) {
				timeUnit = TimeUnit.SECONDS;
			}
		}

		RejectedExecutionHandler rejectedExecutionHandler = null;
		if (properties.containsKey("rejectedExecutionHandler")) {
			String _rejectedExecutionHandler = properties.getProperty("rejectedExecutionHandler").trim();
			if ("AbortPolicy".equalsIgnoreCase(_rejectedExecutionHandler)) {
				rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
			} else if ("CallerRunsPolicy".equalsIgnoreCase(_rejectedExecutionHandler)) {
				rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
			} else if ("DiscardOldestPolicy".equalsIgnoreCase(_rejectedExecutionHandler)) {
				rejectedExecutionHandler = new ThreadPoolExecutor.DiscardOldestPolicy();
			} else if ("DiscardPolicy".equalsIgnoreCase(_rejectedExecutionHandler)) {
				rejectedExecutionHandler = new ThreadPoolExecutor.DiscardPolicy();
			}
		}

		if (null == rejectedExecutionHandler) {
			rejectedExecutionHandler = new ThreadPoolExecutor.DiscardOldestPolicy();
		}

		if (properties.containsKey("timerControlerHandler")) {
			String timerControlerHandler = properties.getProperty("timerControlerHandler").trim();
			Class<?> clazz = Class.forName(timerControlerHandler);
			this.handler = (TimerControlerHandler) clazz.newInstance();
		}

		threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, timeUnit, new ArrayBlockingQueue<Runnable>(queueCapacity),
				rejectedExecutionHandler);

	}

	public void stop() {
		try {
			if (null != threadPool) {
				threadPool.shutdown();
				log.info("timer server container stop......");
			}
		} catch (Throwable e) {
			log.error("timer server container stop error", e);
		}
	}

	public boolean checkRunning(String service) {
		if (null == handler) {
			return true;
		}
		boolean running = handler.checkRunning(service);
		if (!running) {
			log.info("Before the call has not yet finished: " + service);
		}
		return running;
	}

	public void execute(TimerRunnable runnable) {
		runnable.setHandler(handler);
		threadPool.execute(runnable.getRunnable());
	}

}
