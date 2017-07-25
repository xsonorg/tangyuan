package org.xson.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceActuator;

public class RpcLocal {

	private static Logger logger = LoggerFactory.getLogger(RpcLocal.class);

	public static XCO call(String url, XCO request) {
		logger.info("client request to: " + url);
		logger.info("client request args: " + request.toXMLString());
		XCO result = null;
		try {
			// Object value = ServiceActuator.executeAlone(url, request);
			Object value = ServiceActuator.execute(url, request);
			if (value instanceof XCO) {
				result = (XCO) value;
			} else {
				result = new XCO();
				if (null != value) {
					result.setObjectValue(RpcConfig.XCO_DATA_KEY, value);
				}
			}
			if (null == result.getCode()) {
				result.setIntegerValue(RpcConfig.XCO_CODE_KEY, RpcConfig.SUCCESS_CODE_RPC);
			}
		} catch (Throwable e) {
			result = RpcUtil.getExceptionResult(e);
		}
		logger.info("client response: " + result.toXMLString());
		return result;
	}
}
