package org.xson.rpc;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;

// TODO service+mode
// http://org.xson/x/y/v/z
// tcp://org.xson/x/y/v/z
// x:service
// y:method
// v:版本
// z:标识,扩展，表示服务从哪里获取

public class ServiceUriVo {

	protected String	serviceName;
	protected String	methodName;
	protected String	version;
	protected String	mark;

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

	public static ServiceUriVo parse(String path) {

		if (null == path) {
			throw new TangYuanException("Invalid service path: " + path);
		}
		path = path.trim();

		ServiceUriVo rvo = new ServiceUriVo();
		rvo.version = null;
		rvo.mark = null;

		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		String[] array = path.split("/");

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
			throw new TangYuanException("Invalid access path: " + path);
		}

		return rvo;
	}

	public String getServiceId() {
		return this.serviceName + TangYuanContainer.getInstance().getNsSeparator() + this.methodName;
	}
}
