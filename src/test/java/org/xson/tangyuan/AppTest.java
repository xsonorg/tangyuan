package org.xson.tangyuan;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceActuator;

import com.alibaba.fastjson.JSON;

public class AppTest {

	protected void call1() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("id", 1);

		data.put("array", new Object[] { new Date() });

		data.put("name", "王欢");

		data.put("ids", new Long[] { 1L, 2L, 3L });

		Object result = ServiceActuator.execute("sql-1", data);
		System.out.println(JSON.toJSONString(result));
	}

	protected void call2() {
		XCO request = new XCO();
		request.setLongValue("id", 1L);
		List<XCO> result = ServiceActuator.execute("select2", request);
		System.out.println(result.get(0).toXMLString());
	}

	protected void call3() {
		XCO request = new XCO();
		// request.setLongValue("id", 1L);
		XCO result = ServiceActuator.execute("select3", request);
		System.out.println(result.toXMLString());
	}

	protected void call4() {
		XCO request = new XCO();
		request.setStringValue("name", "高鹏");
		request.setDateTimeValue("time1", new Date());
		Integer result = ServiceActuator.execute("insert1", request);
		System.out.println(result);
	}

	protected void call5() {
		XCO request = new XCO();
		request.setStringValue("name", "高鹏");
		Object result = ServiceActuator.execute("insert2", request);
		System.out.println(result);
	}

	protected void call51() {
		XCO request = new XCO();
		String[] var = { "高鹏1", "高鹏2", "高鹏3", "高鹏4", "高鹏5" };
		request.setStringArrayValue("names", var);
		long[] result = ServiceActuator.execute("insert3", request);
		System.out.println(result.getClass().getName() + "\n" + JSON.toJSONString(result));
	}

	protected void call52() throws Exception {
		XCO request = new XCO();
		String[] var = { "高鹏1", "高鹏2", "高鹏3", "高鹏4", "高鹏5" };
		request.setStringArrayValue("names", var);
		ServiceActuator.executeAsync("insert3", request);
		// System.out.println(result.getClass().getName() + "\n" + JSON.toJSONString(result));
		System.in.read();
	}

	@Test
	public void testApp() {
		try {
			// config
			String xmlResource = "tangyuan-configuration.xml";
			TangYuanContainer.getInstance().start(xmlResource);
			System.out.println("-------------------------------------------------------------------------------");
			// for (int i = 0; i < 10; i++) {
			// call51();
			// }
			call52();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
