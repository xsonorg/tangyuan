package org.xson.tangyuan.spring;

public class AopBean {

	public void before() {
		// SqlServiceActuator.begin();
		// ServiceActuator.begin();
	}

	public void after() {
		// SqlServiceActuator.end();
		// ServiceActuator.end();
	}

	public void onException(Throwable e) throws Throwable {
		// SqlServiceActuator.onException(e);
		// ServiceActuator.onException(e);
	}
}
