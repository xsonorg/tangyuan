package org.xson.timer.client;

import org.quartz.DisallowConcurrentExecution;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;

@DisallowConcurrentExecution
public class NonConcurrentJob extends TimerJob {

	private static Log log = LogFactory.getLog(NonConcurrentJob.class);

	@Override
	public Log getLogger() {
		return log;
	}

}
