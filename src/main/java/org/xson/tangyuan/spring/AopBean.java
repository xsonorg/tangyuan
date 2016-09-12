package org.xson.tangyuan.spring;

import org.xson.tangyuan.executor.SqlServiceActuator;

public class AopBean {

	public void before() {
		SqlServiceActuator.begin();
	}

	public void after() {
		SqlServiceActuator.end();
	}

	public void onException(Throwable e) throws Throwable {
		SqlServiceActuator.onException(e);
	}
}
