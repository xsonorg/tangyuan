package org.xson.tangyuan.bridge;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.task.AsyncTask;
import org.xson.tangyuan.xml.node.CallNode.CallMode;

public class BridgedCallSupport {

	private static Log									log			= LogFactory.getLog(BridgedCallSupport.class);

	private static Map<String, XMLBridgedCallHandler>	handlerMap	= new HashMap<String, XMLBridgedCallHandler>(8);

	public static String								separator	= "://";

	/**
	 * 注册处理器: xxx://
	 */
	public static void register(String protocol, XMLBridgedCallHandler handler) {
		handlerMap.put(protocol.toUpperCase(), handler);
	}

	private static String getProtocol(int pos, String service) {
		return service.substring(0, pos);
	}

	private static String getRealService(int pos, String service) {
		return service.substring(pos + separator.length());
	}

	public static Object call(String service, CallMode mode, final Object request) {
		int pos = service.indexOf(separator);
		String protocol = getProtocol(pos, service);
		final String realService = getRealService(pos, service);
		final XMLBridgedCallHandler handler = handlerMap.get(protocol.toUpperCase() + separator);
		if (null == handler) {
			throw new TangYuanException("不存在的调用桥接器: " + service);
		}
		if (CallMode.ASYNC != mode) {
			return handler.call(realService, request);
		} else {
			TangYuanContainer.getInstance().addAsyncTask(new AsyncTask() {
				@Override
				public void run() {
					try {
						handler.call(realService, request);
					} catch (Throwable e) {
						log.error(null, e);
					}
				}
			});
			return null;
		}
	}
}
