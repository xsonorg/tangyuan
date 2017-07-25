package org.xson.tangyuan.aspect;

import java.util.List;

import org.xson.common.object.XCO;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.xml.node.CallNode.CallMode;

public class AfterVo extends AspectVo {

	protected static Log			log			= LogFactory.getLog(AfterVo.class);

	private static PostCutAssembly	pcAssembly	= new DefaultPostCutAssembly();

	public AfterVo(String exec, CallMode mode, boolean propagation, int order, AspectCondition condition, boolean bridged, List<String> includeList,
			List<String> excludeList) {
		super(exec, mode, propagation, order, condition, bridged, includeList, excludeList);
	}

	@Override
	protected Log getLog() {
		return log;
	}

	@Override
	public void execAfter(String service, Object arg, Object result, Throwable ex) {

		if (AspectCondition.SUCCESS == condition && null != ex) {
			return;
		}

		if (AspectCondition.EXCEPTION == condition && null == ex) {
			return;
		}

		XCO xcoPackage = (XCO) pcAssembly.assembly(service, arg, result, ex);

		if (bridged) {
			execBridged(service, xcoPackage);
		} else {
			execLocal(service, xcoPackage);
		}
	}

}
