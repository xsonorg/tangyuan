package org.xson.rpc;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.timer.server.TimerContainer;
import org.xson.timer.server.TimerRunnable;
import org.xson.zongzi.ZongYe;

public class RpcBridge implements ZongYe {

	private Logger logger = LoggerFactory.getLogger(RpcBridge.class);

	class RequestVo {
		String	serviceName;
		String	methodName;
		String	version;
		String	mark;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(serviceName);
			if (null != methodName) {
				builder.append("/").append(methodName);
			}
			if (null != version) {
				builder.append("/").append(version);
			}
			if (null != mark) {
				builder.append("/").append(mark);
			}
			return builder.toString();
		}
	}

	private RequestVo parseRequest(String uri) {
		// http://org.xson/x/y/v/z
		// tcp://org.xson/x/y/v/z
		// x:service
		// y:method
		// v:版本
		// z:标识,扩展，表示服务从哪里获取
		if (null == uri) {
			throw new RpcException(RpcConfig.RPC_ERROR_CODE, "Invalid access path: " + uri);
		}
		uri = uri.trim();

		RequestVo rvo = new RequestVo();
		rvo.version = null;
		rvo.mark = null;

		String[] array = uri.substring(1).split("/");

		if (array.length == 1) {
			rvo.serviceName = array[0];
		} else if (array.length == 2) {
			rvo.serviceName = array[0];
			rvo.methodName = array[1];
		} else if (array.length == 3) {
			rvo.serviceName = array[0];
			rvo.methodName = array[1];
			rvo.version = array[2];
		} else if (array.length == 4) {
			rvo.serviceName = array[0];
			rvo.methodName = array[1];
			rvo.version = array[2];
			rvo.mark = array[3];
		} else {
			throw new RpcException(RpcConfig.RPC_ERROR_CODE, "Invalid access path: " + uri);
		}

		return rvo;
	}

	@Override
	public Object call(String uri, Object arg) {
		logger.info("client request to: " + uri);
		logger.info("client request args: " + arg);
		XCO result = null;
		try {

			RequestVo rVo = parseRequest(uri);
			Object value = doRequest((XCO) arg, rVo);

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
		return result;
	}

	private Object doRequest(final XCO arg, RequestVo rVo) throws Throwable {
		try {
			logger.info("Rpc call [" + rVo + "] start.");
			Object result = null;
			if ("Alone".equalsIgnoreCase(rVo.mark)) {
				result = ServiceActuator.executeAlone(getServiceId(rVo), arg);
			} else if ("Async".equalsIgnoreCase(rVo.mark)) {
				ServiceActuator.executeAsync(getServiceId(rVo), arg);
			} else if ("Timer".equalsIgnoreCase(rVo.mark)) {
				TimerContainer tc = TimerContainer.getInstance();
				final String service = getServiceId(rVo);
				if (tc.checkRunning(service)) {
					tc.execute(new TimerRunnable(service, new Runnable() {
						@Override
						public void run() {
							ServiceActuator.execute(service, arg);
						}
					}));
				}
			} else {
				result = ServiceActuator.execute(getServiceId(rVo), arg);
			}
			logger.info("Rpc call [" + rVo + "] success.");
			return result;
		} catch (Throwable e) {
			logger.error("Rpc call [" + rVo + "] error.");
			throw e;
		}
	}

	private String getServiceId(RequestVo rVo) {
		return rVo.serviceName + "." + rVo.methodName;
	}

}
