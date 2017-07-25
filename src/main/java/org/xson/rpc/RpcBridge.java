package org.xson.rpc;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xson.common.object.XCO;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.zongzi.JPCBridge;

public class RpcBridge implements JPCBridge {

	private Logger	logger	= LoggerFactory.getLogger(RpcBridge.class);

	private JPCBridge	hook	= null;

	@Override
	public Object call(String path, Object arg) {
		logger.info("client request to: " + path);
		logger.info("client request args: " + arg);
		XCO result = null;
		try {
			result = RpcUtil.doXcoRpcRquest(path, (XCO) arg);
		} catch (Throwable e) {
			result = RpcUtil.getExceptionResult(e);
		}
		return result;
	}

	@Override
	public URI inJvm(String url) {
		return this.hook.inJvm(url);
	}

	@Override
	public Object inJvmCall(URI uri, Object request) {
		return this.hook.inJvmCall(uri, request);
	}

	@Override
	public void setHook(Object obj) {
		// TODO 这里可能有问题, 不同的ClassLoader
		// System.out.println(obj);
		// System.out.println(obj.getClass().getClassLoader());
		// System.out.println(this.getClass().getClassLoader());
		this.hook = (JPCBridge) obj;
		RpcClient.jpcBridge = this;
	}

	@Override
	public Object getServiceInfo() {
		return TangYuanContainer.getInstance().getServicesKeySet();
	}

	// private Object doRequest(final XCO arg, RequestVo rVo) throws Throwable {
	// try {
	// logger.info("Rpc call [" + rVo + "] start.");
	// Object result = null;
	// if ("Alone".equalsIgnoreCase(rVo.mark)) {
	// result = ServiceActuator.executeAlone(getServiceId(rVo), arg);
	// } else if ("Async".equalsIgnoreCase(rVo.mark)) {
	// ServiceActuator.executeAsync(getServiceId(rVo), arg);
	// } else if ("Timer".equalsIgnoreCase(rVo.mark)) {
	// TimerContainer tc = TimerContainer.getInstance();
	// final String service = getServiceId(rVo);
	// if (tc.checkRunning(service)) {
	// tc.execute(new TimerRunnable(service, new Runnable() {
	// @Override
	// public void run() {
	// ServiceActuator.execute(service, arg);
	// }
	// }));
	// }
	// } else {
	// result = ServiceActuator.execute(getServiceId(rVo), arg);
	// }
	// logger.info("Rpc call [" + rVo + "] success.");
	// return result;
	// } catch (Throwable e) {
	// logger.error("Rpc call [" + rVo + "] error.");
	// throw e;
	// }
	// }

	// private String getServiceId(RequestVo rVo) {
	// return rVo.serviceName + "." + rVo.methodName;
	// }

	// class RequestVo {
	// String serviceName;
	// String methodName;
	// String version;
	// String mark;
	//
	// @Override
	// public String toString() {
	// StringBuilder builder = new StringBuilder();
	// builder.append(serviceName);
	// if (null != methodName) {
	// builder.append("/").append(methodName);
	// }
	// if (null != version) {
	// builder.append("/").append(version);
	// }
	// if (null != mark) {
	// builder.append("/").append(mark);
	// }
	// return builder.toString();
	// }
	// }

	// private RequestVo parseRequest(String uri) {
	// // http://org.xson/x/y/v/z
	// // tcp://org.xson/x/y/v/z
	// // x:service
	// // y:method
	// // v:版本
	// // z:标识,扩展，表示服务从哪里获取
	// if (null == uri) {
	// throw new RpcException(RpcConfig.RPC_ERROR_CODE, "Invalid access path: " + uri);
	// }
	// uri = uri.trim();
	//
	// RequestVo rvo = new RequestVo();
	// rvo.version = null;
	// rvo.mark = null;
	//
	// String[] array = uri.substring(1).split("/");
	//
	// if (array.length == 1) {
	// rvo.serviceName = array[0];
	// } else if (array.length == 2) {
	// rvo.serviceName = array[0];
	// rvo.methodName = array[1];
	// } else if (array.length == 3) {
	// rvo.serviceName = array[0];
	// rvo.methodName = array[1];
	// rvo.version = array[2];
	// } else if (array.length == 4) {
	// rvo.serviceName = array[0];
	// rvo.methodName = array[1];
	// rvo.version = array[2];
	// rvo.mark = array[3];
	// } else {
	// throw new RpcException(RpcConfig.RPC_ERROR_CODE, "Invalid access path: " + uri);
	// }
	//
	// return rvo;
	// }
}
