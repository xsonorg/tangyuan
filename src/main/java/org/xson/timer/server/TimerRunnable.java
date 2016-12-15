package org.xson.timer.server;

import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;

public class TimerRunnable implements Runnable {

	private static Log				log	= LogFactory.getLog(TimerRunnable.class);

	private String					service;

	private Runnable				runnable;

	private TimerControlerHandler	handler;

	public TimerRunnable(String service, Runnable runnable) {
		this.service = service;
		this.runnable = runnable;
	}

	public String getService() {
		return service;
	}

	public Runnable getRunnable() {
		return runnable;
	}

	public void setHandler(TimerControlerHandler handler) {
		if (null == this.handler) {
			this.handler = handler;
		}
	}

	@Override
	public void run() {
		try {
			runnable.run();
		} catch (Throwable e) {
			log.error("timer run error.", e);
		}
		try {
			update();
		} catch (Throwable e) {
			log.error("timer update error.", e);
		}
	}

	private void update() {
		if (null != handler) {
			handler.update(service);
		}
	}
}
