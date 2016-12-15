package org.xson.rpc;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xson.common.object.XCO;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.timer.server.TimerContainer;
import org.xson.timer.server.TimerRunnable;

public class RpcServlet extends HttpServlet {

	private final static long	serialVersionUID	= 1L;

	private static Logger		logger				= LoggerFactory.getLogger(RpcServlet.class);

	private String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

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

	private RequestVo parseRequest(HttpServletRequest req) {

		logger.info("Rpc call from [" + req.getRequestURL() + "], client ip[" + getIpAddress(req) + "]");

		// http://org.xson/x/y/v/z
		// tcp://org.xson/x/y/v/z
		// x:service
		// y:method
		// v:版本
		// z:标识,扩展，表示服务从哪里获取

		String uri = req.getRequestURI();
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
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		XCO result = null;
		try {

			RequestVo rVo = parseRequest(req);
			Object value = doRequest(req, rVo);

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

		if (null != result) {
			resp.setHeader("Content-type", "text/html;charset=UTF-8");
			resp.setCharacterEncoding("UTF-8");
			Writer write = resp.getWriter();
			write.write(result.toXMLString());
			write.close();
		}
	}

	private XCO getXCOArg(HttpServletRequest request) throws IOException {
		byte[] buffer = IOUtils.toByteArray(request.getInputStream());
		return XCO.fromXML(new String(buffer, "UTF-8"));
	}

	public Object doRequest(HttpServletRequest request, RequestVo rVo) throws Throwable {
		try {
			logger.info("Rpc call [" + rVo + "] start.");
			final XCO arg = getXCOArg(request);
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
