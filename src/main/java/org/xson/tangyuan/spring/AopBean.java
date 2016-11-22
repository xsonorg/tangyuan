package org.xson.tangyuan.spring;

import org.xson.tangyuan.executor.ServiceActuator;

public class AopBean {

	public void before() {
		ServiceActuator.begin();
	}

	public void after() {
		ServiceActuator.end();
	}

	public void onException(Throwable e) throws Throwable {
		ServiceActuator.onException(e);
	}
}
