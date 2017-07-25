package org.xson.rpc;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.timer.server.TimerContainer;
import org.xson.timer.server.TimerRunnable;

public class RpcUtil {

	private static Logger logger = LoggerFactory.getLogger(RpcUtil.class);

	public static XCO getExceptionResult(Throwable e) {

		XCO result = new XCO();

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

		return result;
	}

	public static XCO doXcoRpcRquest(String path, XCO arg) throws Throwable {
		ServiceUriVo rVo = ServiceUriVo.parse(path);
		return doXcoRpcRquest(rVo, arg);
	}

	public static XCO doXcoRpcRquest(ServiceUriVo rVo, XCO arg) throws Throwable {
		XCO result = null;
		Object value = doXcoRpcRquest0(rVo, arg);
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
		logger.info("Rpc call [" + rVo + "] result:\n" + result);
		return result;
	}

	private static Object doXcoRpcRquest0(ServiceUriVo rVo, final XCO arg) throws Throwable {
		try {
			logger.info("Rpc call [" + rVo + "] start.");
			Object result = null;
			if ("Alone".equalsIgnoreCase(rVo.mark)) {
				result = ServiceActuator.executeAlone(rVo.getServiceId(), arg);
			} else if ("Async".equalsIgnoreCase(rVo.mark)) {
				ServiceActuator.executeAsync(rVo.getServiceId(), arg);
			} else if ("Timer".equalsIgnoreCase(rVo.mark)) {
				TimerContainer tc = TimerContainer.getInstance();
				final String service = rVo.getServiceId();
				if (tc.checkRunning(service)) {
					tc.execute(new TimerRunnable(service, new Runnable() {
						@Override
						public void run() {
							ServiceActuator.execute(service, arg);
						}
					}));
				}
			} else {
				result = ServiceActuator.execute(rVo.getServiceId(), arg);
			}
			logger.info("Rpc call [" + rVo + "] success.");
			return result;
		} catch (Throwable e) {
			logger.error("Rpc call [" + rVo + "] error.");
			throw e;
		}
	}

}
