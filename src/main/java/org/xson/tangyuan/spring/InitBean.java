package org.xson.tangyuan.spring;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;

public class InitBean {

	private Log		log	= LogFactory.getLog(InitBean.class);

	private String	resource;

	public void setResource(String resource) {
		this.resource = resource;
	}

	public void init() throws Throwable {
		if (null != resource) {
			TangYuanContainer.getInstance().start(resource);
			log.info("tangyuan init success!!!!");
		}
	}

	public void destroy() {
		try {
			TangYuanContainer.getInstance().stop();
			log.info("tangyuan stop ...");
		} catch (Throwable e) {
			log.error("tangyuan stop failure...", e);
		}
	}
}
