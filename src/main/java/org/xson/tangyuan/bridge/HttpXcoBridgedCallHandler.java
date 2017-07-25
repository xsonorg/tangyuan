package org.xson.tangyuan.bridge;

import org.xson.common.object.XCO;
import org.xson.rpc.RpcClient;
import org.xson.rpc.RpcException;

public class HttpXcoBridgedCallHandler implements XMLBridgedCallHandler {

	@Override
	public Object call(String service, Object request) {
		XCO result = RpcClient.call(service, (XCO) request);
		// 这里要抛出异常
		if (0 != result.getCode()) {
			throw new RpcException(result.getCode(), result.getMessage());
		}
		return result;
	}

}
