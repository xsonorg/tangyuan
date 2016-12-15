package org.xson.tangyuan.executor;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.ognl.convert.ParameterConverter;
import org.xson.tangyuan.task.AsyncTask;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

public class ServiceActuator2 {

	private static Log							log					= LogFactory.getLog(ServiceActuator2.class);

	private static ThreadLocal<ServiceContext>	contextThreadLocal	= new ThreadLocal<ServiceContext>();

	private static ParameterConverter			converter			= new ParameterConverter();

	/**
	 * 方法之前的线程调用
	 */
	public static void begin() {
		ServiceContext context = contextThreadLocal.get();
		if (null == context) {
			context = new ServiceContext();
			log.debug("open a new context. hashCode[" + context.hashCode() + "]");
			contextThreadLocal.set(context);
		} else {
			log.debug("follow an existing context. hashCode[" + context.hashCode() + "], context[" + (context.counter + 1) + "]");
			context.counter++;
		}
	}

	/**
	 * 方法之后的线程调用
	 */
	public static void end() {
		ServiceContext context = contextThreadLocal.get();
		if (null != context) {
			log.debug("context--. hashCode[" + context.hashCode() + "], context[" + (context.counter - 1) + "]");
			if (--context.counter < 1) {
				if (null == context.getExceptionInfo()) {
					try {
						context.finish();// 这里是确定的提交
					} catch (Throwable e) {
						context.finishOnException();
						log.error("SqlService commit exception", e);
					}
				} else {
					context.finishOnException();
				}
				contextThreadLocal.remove();
				log.debug("close a context. hashCode[" + context.hashCode() + "]");
				// monitor
				context.stopMonitor();
			}
		} else {
			contextThreadLocal.remove();
		}
	}

	/**
	 * 拦截器调用
	 */
	public static void onException(Throwable throwable) throws Throwable {
		// 这里只是拦截漏网的
		log.debug("SqlService onException");
		ServiceContext context = contextThreadLocal.get();
		if (null != context) {
			if (null != context.getExceptionInfo()) {
				context.finishOnException();
			}
			log.debug("remove a context on exception. hashCode[" + context.hashCode() + "]");
			// monitor
			context.stopMonitor();
		}
		contextThreadLocal.remove();
		// TODO 是否在这里打印
		log.error("", throwable);
		throw throwable;
	}

	/**
	 * 上下文环境
	 */
	public static <T> T execute(String serviceId, Object arg) throws ServiceException {
		log.info("actuator service: " + serviceId);
		ServiceContext context = contextThreadLocal.get();
		if (null == context) {
			throw new ServiceException("Service context does not exist: " + serviceId);
		}
		return executeContext(serviceId, context, arg);
	}

	/**
	 * 上下文环境
	 */
	@SuppressWarnings("unchecked")
	public static <T> T executeContext(String serviceId, ServiceContext context, Object arg) {
		AbstractServiceNode service = TangYuanContainer.getInstance().getService(serviceId);
		if (null == service) {
			// 这里可以强制回滚,生产环境不会出现
			context.finishOnException();
			throw new ServiceException("Service does not exist: " + serviceId);
		}
		// monitor
		context.updateMonitor(serviceId);
		Object result = null;
		try {
			Object data = converter.parameterConvert(arg, service.getResultType());
			service.execute(context, data);
			result = service.getResult(context);// 只有类型转换时发生异常
		} catch (Throwable e) {
			context.onException(service.getServiceType(), e, "Execute service exception: " + serviceId);
		}
		return (T) result;
	}

	/**
	 * 单独环境
	 */
	public static <T> T executeAlone(String serviceId, Object arg) throws ServiceException {
		return executeAlone(serviceId, arg, true);
	}

	@SuppressWarnings("unchecked")
	public static <T> T executeAlone(String serviceId, Object arg, boolean throwException) throws ServiceException {
		log.info("execute alone service: " + serviceId);
		AbstractServiceNode service = TangYuanContainer.getInstance().getService(serviceId);
		if (null == service) {
			// 生产环境不会出现, 并且不涉及回滚
			throw new ServiceException("Service does not exist: " + serviceId);
		}
		ServiceContext context = new ServiceContext();
		// monitor
		context.updateMonitor(serviceId);
		Object result = null;
		try {
			Object data = converter.parameterConvert(arg, service.getResultType());
			service.execute(context, data);
			context.finish();
			result = service.getResult(context);// 只有类型转换是发生异常
		} catch (Throwable e) {
			context.finishOnException();
			if (throwException) {
				if (e instanceof ServiceException) {
					throw (ServiceException) e;
				} else {
					throw new ServiceException("Execute service exception: " + serviceId, e);
				}
			} else {
				log.error("Execute service exception: " + serviceId, e);
			}
		} finally {
			context.stopMonitor();
		}
		return (T) result;
	}

	/**
	 * 单独环境, 异步执行
	 */
	public static void executeAsync(final String serviceId, final Object arg) {
		log.info("execute async service: " + serviceId);
		final AbstractServiceNode service = TangYuanContainer.getInstance().getService(serviceId);
		if (null == service) {
			log.error("Service does not exist: " + serviceId);
			return;
		}
		TangYuanContainer.getInstance().addAsyncTask(new AsyncTask() {
			@Override
			public void run() {
				ServiceContext context = new ServiceContext();
				// monitor
				context.updateMonitor(serviceId);
				try {
					Object data = converter.parameterConvert(arg, service.getResultType());
					service.execute(context, data);
					// 这里是最终的提交
					context.finish();
					context.setResult(null);
				} catch (Throwable e) {
					// 这里是最终的回滚
					context.finishOnException();
					log.error("Execute service exception: " + serviceId, e);
				} finally {
					context.stopMonitor();
				}
			}
		});
	}

}
