package org.xson.tangyuan.aspect;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

/**
 * AOP入口类
 */
public class AspectSupport {

	private Log									log			= LogFactory.getLog(AspectSupport.class);

	private static AspectSupport				instance	= new AspectSupport();

	private Map<String, AspectServiceMappingVo>	map			= null;

	private AspectSupport() {
	}

	public static AspectSupport getInstance() {
		return instance;
	}

	public void bind(String service, AspectServiceMappingVo asmVo) {
		if (null == map) {
			map = new HashMap<String, AspectServiceMappingVo>();
		}
		map.put(service, asmVo);
	}

	public void execBefore(AbstractServiceNode service, Object arg) {
		if (null == map) {
			return;
		}

		if (!service.hasBeforeAspect()) {
			return;
		}

		AspectServiceMappingVo asmVo = map.get(service.getServiceKey());
		if (null == asmVo) {
			return;
		}
		try {
			asmVo.execBefore(service.getServiceKey(), arg);
		} catch (Throwable e) {
			log.error("Front interception exception: " + service.getServiceKey(), e);
		}
	}

	public void execAfter(AbstractServiceNode service, Object arg, Object result, Throwable ex) {
		if (null == map) {
			return;
		}

		if (!service.hasAfterAspect()) {
			return;
		}
		AspectServiceMappingVo asmVo = map.get(service.getServiceKey());
		if (null == asmVo) {
			return;
		}
		try {
			asmVo.execAfter(service.getServiceKey(), arg, result, ex);
		} catch (Throwable e) {
			log.error("Post-intercept exception: " + service.getServiceKey(), e);
		}
	}

}
