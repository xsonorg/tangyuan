package org.xson.tangyuan.aspect;

import java.util.List;

import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.bridge.BridgedCallSupport;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.util.PatternMatchUtils;
import org.xson.tangyuan.xml.node.CallNode.CallMode;

public abstract class AspectVo implements Comparable<AspectVo> {

	public enum AspectCondition {
		/** 成功 */
		SUCCESS,

		/** 异常 */
		EXCEPTION,

		/** 所有 */
		ALL
	}

	protected String			exec;

	protected CallMode			mode;

	/** 是否会影响其他切面 */
	protected boolean			propagation;

	protected int				order;

	protected AspectCondition	condition;

	protected boolean			bridged;

	protected List<String>		includeList;

	protected List<String>		excludeList;

	public AspectVo(String exec, CallMode mode, boolean propagation, int order, AspectCondition condition, boolean bridged, List<String> includeList,
			List<String> excludeList) {
		this.exec = exec;
		this.mode = mode;
		this.propagation = propagation;
		this.order = order;
		this.condition = condition;
		this.bridged = bridged;
		this.includeList = includeList;
		this.excludeList = excludeList;
	}

	// public boolean match(String url) {
	// if (null != excludeList) {
	// for (String pattern : excludeList) {
	// if (PatternMatchUtils.simpleMatch(pattern, url)) {
	// return false;
	// }
	// }
	// }
	// if (null != includeList) {
	// for (String pattern : includeList) {
	// // if (!PatternMatchUtils.simpleMatch(pattern, url)) {
	// // return false;
	// // }
	// if (PatternMatchUtils.simpleMatch(pattern, url)) {
	// return true;
	// }
	// }
	// }
	// return false;
	// }

	public boolean match(String url) {

		// 排除递归
		if (exec.equalsIgnoreCase(url)) {
			return false;
		}

		if (null != excludeList) {
			for (String pattern : excludeList) {
				if (PatternMatchUtils.simpleMatch(pattern, url)) {
					return false;
				}
			}
		}
		if (null != includeList) {
			for (String pattern : includeList) {
				if (PatternMatchUtils.simpleMatch(pattern, url)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * 之前前置方法
	 * 
	 * @param service
	 *            仅作日志标识使用
	 * @param arg
	 */
	public void execBefore(String service, Object arg) {
		throw new TangYuanException("Subclasses must override this method");
	}

	public void execAfter(String service, Object arg, Object result, Throwable ex) {
		throw new TangYuanException("Subclasses must override this method");
	}

	abstract protected Log getLog();

	public int getOrder() {
		return order;
	}

	@Override
	public int compareTo(AspectVo o) {
		// 比较此对象与指定对象的顺序。如果该对象小于、等于或大于指定对象，则分别返回负整数、零或正整数。
		// AspectVo other = (AspectVo) o;
		if (this.order < o.getOrder()) {
			return -1;
		} else if (this.order > o.getOrder()) {
			return 1;
		} else {
			return 0;
		}
	}

	protected void execBridged(String service, Object arg) {
		try {
			BridgedCallSupport.call(exec, mode, arg);
		} catch (TangYuanException e) {
			if (propagation) {
				throw e;
			} else {
				getLog().error("An exception occurred before the service: " + service, e);
			}
		}
	}

	protected void execLocal(String service, Object arg) {
		try {
			if (CallMode.EXTEND == mode) {
				ServiceActuator.execute(exec, arg);
			} else if (CallMode.ALONE == mode) {
				ServiceActuator.executeAlone(exec, arg);
			} else {
				ServiceActuator.executeAsync(exec, arg);
			}
		} catch (TangYuanException e) {
			if (propagation) {
				throw e;
			} else {
				getLog().error("An exception occurred before the service: " + service, e);
			}
		}
	}

}
