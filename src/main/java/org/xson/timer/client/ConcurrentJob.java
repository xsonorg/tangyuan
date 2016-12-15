package org.xson.timer.client;

import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;

public class ConcurrentJob extends TimerJob {

	private static Log log = LogFactory.getLog(ConcurrentJob.class);

	@Override
	public Log getLogger() {
		return log;
	}
}
