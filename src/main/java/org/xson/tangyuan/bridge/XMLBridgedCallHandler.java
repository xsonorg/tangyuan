package org.xson.tangyuan.bridge;

/**
 * XML 桥接调用接口
 */
public interface XMLBridgedCallHandler {

	Object call(String service, Object request);

}
