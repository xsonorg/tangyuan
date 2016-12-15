package org.xson.tangyuan.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;

public class TangYuanContextLoaderListener implements ServletContextListener {

	private Log log = LogFactory.getLog(TangYuanContextLoaderListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		try {
			String resource = context.getInitParameter("tangyuan.resource");
			TangYuanContainer.getInstance().start(resource);
			log.info("tangyuan init success!!!");
		} catch (Throwable e) {
			log.error("tangyuan failed to start!!!");
			throw new RuntimeException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			TangYuanContainer.getInstance().stop();
			log.info("tangyuan stop ...");
		} catch (Throwable e) {
			log.error("tangyuan stop error.", e);
		}
	}

}
