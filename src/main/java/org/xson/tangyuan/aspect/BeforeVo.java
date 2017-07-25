package org.xson.tangyuan.aspect;

import java.util.List;

import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.xml.node.CallNode.CallMode;

public class BeforeVo extends AspectVo {

	protected static Log log = LogFactory.getLog(BeforeVo.class);

	public BeforeVo(String exec, CallMode mode, boolean propagation, int order, boolean bridged, List<String> includeList, List<String> excludeList) {
		super(exec, mode, propagation, order, null, bridged, includeList, excludeList);
	}

	@Override
	protected Log getLog() {
		return log;
	}
	
	@Override
	public void execBefore(String service, Object arg) {
		if (bridged) {
			execBridged(service, arg);
		} else {
			execLocal(service, arg);
		}
	}

	// private void execBridged(String service, Object arg) {
	// try {
	// BridgedCallSupport.call(exec, mode, arg);
	// } catch (ServiceException e) {
	// if (propagation) {
	// throw e;
	// } else {
	// log.error("An exception occurred before the service: " + service, e);
	// }
	// }
	// }
	//
	// private void execLocal(String service, Object arg) {
	// try {
	// if (CallMode.EXTEND == mode) {
	// ServiceActuator.execute(exec, arg);
	// } else if (CallMode.ALONE == mode) {
	// ServiceActuator.executeAlone(exec, arg);
	// } else {
	// ServiceActuator.executeAsync(exec, arg);
	// }
	// } catch (ServiceException e) {
	// if (propagation) {
	// throw e;
	// } else {
	// log.error("An exception occurred before the service: " + service, e);
	// }
	// }
	// }

}
