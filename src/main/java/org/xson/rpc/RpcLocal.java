package org.xson.rpc;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.executor.ServiceException;

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

			result = new XCO();

			Throwable tx = e;
			if (e instanceof InvocationTargetException) {
				tx = ((InvocationTargetException) e).getTargetException();
			}

			int errorCode = 0;
			String errorMessage = null;

			if (tx instanceof RpcException) {
				RpcException ex = (RpcException) tx;
				errorCode = ex.getCode();
				errorMessage = ex.getMessage();
			} else if (tx instanceof ServiceException) {
				ServiceException ex = (ServiceException) tx;
				errorCode = ex.getErrorCode();
				errorMessage = ex.getErrorMessage();
			} else {
				errorCode = RpcConfig.RPC_ERROR_CODE;
				errorMessage = RpcConfig.RPC_ERROR_MESSAGE;
			}

			result.setIntegerValue(RpcConfig.XCO_CODE_KEY, errorCode);
			result.setStringValue(RpcConfig.XCO_MESSAGE_KEY, errorMessage);

			logger.error(null, tx);
		}
		logger.info("client response: " + result.toXMLString());
		return result;
	}
}
