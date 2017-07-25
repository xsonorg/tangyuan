package org.xson.tangyuan.aspect;

import java.util.List;

/**
 * 切面和服务映射
 */
public class AspectServiceMappingVo {

	private String			service;

	private List<AspectVo>	beforeList;

	private List<AspectVo>	afterList;

	public AspectServiceMappingVo(String service, List<AspectVo> beforeList, List<AspectVo> afterList) {
		this.service = service;
		this.beforeList = beforeList;
		this.afterList = afterList;
	}

	public String getService() {
		return service;
	}

	public void execBefore(String service, Object arg) {
		if (null != beforeList) {
			for (AspectVo aVo : beforeList) {
				aVo.execBefore(service, arg);
			}
		}
	}

	public void execAfter(String service, Object arg, Object result, Throwable ex) {
		if (null != afterList) {
			for (AspectVo aVo : afterList) {
				aVo.execAfter(service, arg, result, ex);
			}
		}
	}
}
