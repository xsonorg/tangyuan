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

	private static String								separator	= "://";

	static {
		HttpXcoBridgedCallHandler hxbcHandler = new HttpXcoBridgedCallHandler();

		register("http".toLowerCase(), hxbcHandler);
		register("https".toLowerCase(), hxbcHandler);
	}

	/** 注册处理器: xxx:// */
	public static void register(String protocol, XMLBridgedCallHandler handler) {
		handlerMap.put(protocol.toUpperCase(), handler);
	}

	/** 是否需要桥接 */
	public static boolean isBridged(String service) {
		if (service.indexOf(BridgedCallSupport.separator) > 0) {
			return true;
		}
		return false;
	}

	private static String getProtocol(int pos, String service) {
		return service.substring(0, pos);
	}

	public static Object call(final String service, CallMode mode, final Object request) {
		int pos = service.indexOf(separator);
		String protocol = getProtocol(pos, service);
		final XMLBridgedCallHandler handler = handlerMap.get(protocol.toUpperCase());
		if (null == handler) {
			throw new TangYuanException("Call bridge does not exist: " + service);
		}
		if (CallMode.ASYNC != mode) {
			return handler.call(service, request);
		} else {
			TangYuanContainer.getInstance().addAsyncTask(new AsyncTask() {
				@Override
				public void run() {
					try {
						handler.call(service, request);
					} catch (Throwable e) {
						log.error(null, e);
					}
				}
			});
			return null;
		}
	}

	// private static String getRealService(int pos, String service) {
	// return service.substring(pos + separator.length());
	// }
}
