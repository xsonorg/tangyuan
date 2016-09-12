package org.xson.tangyuan;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.xson.tangyuan.xml.parsing.TokenParserUtil;

import com.alibaba.fastjson.JSON;

public class OtherTest {

	@Test
	public void test01() {
		String log = "这是{a}一条{b}日志";
		List<Object> unitlist = new TokenParserUtil().parseLog(log, "{", "}");
		for (Object o : unitlist) {
			System.out.println(o);
		}
		System.out.println(JSON.toJSONString(unitlist));
	}

	@Test
	public void test02() {
		Method[] ms = BasicDataSource.class.getMethods();
		for (int i = 0; i < ms.length; i++) {
			if(ms[i].getName().startsWith("set")){
				System.out.println(ms[i].getName());
			}
		}
	}
}
