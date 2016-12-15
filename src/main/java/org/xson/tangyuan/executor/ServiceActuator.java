package org.xson.tangyuan.executor;

import java.util.LinkedList;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.ognl.convert.ParameterConverter;
import org.xson.tangyuan.task.AsyncTask;
import org.xson.tangyuan.xml.node.AbstractServiceNode;

public class ServiceActuator {

	private static Log									log					= LogFactory.getLog(ServiceActuator.class);

	private static ThreadLocal<ThreadServiceContext>	contextThreadLocal	= new ThreadLocal<ThreadServiceContext>();

	private static ParameterConverter					converter			= new ParameterConverter();

	/** 线程中的上下文 */
	static class ThreadServiceContext {
		/** 单个上下文 */
		private ServiceContext				context;
		/** 单个上队列 */
		private LinkedList<ServiceContext>	contextQueue;

		/**
		 * 获取或者创建新的上下文
		 * 
		 * @param isNew
		 *            是否创建新的上下文
		 */
		public ServiceContext getOrCreate(boolean isNew) {
			// 最初
			if (null == context && null == contextQueue) {
				context = new ServiceContext();
				log.debug("open a new context. hashCode[" + context.hashCode() + "]");
				return context;
			}
			ServiceContext returnContext = null;
			// 之后的情况
			if (isNew) {
				if (null != context && null == contextQueue) {
					contextQueue = new LinkedList<ServiceContext>();
					contextQueue.push(context);
					context = null;

					ServiceContext newContext = new ServiceContext();
					log.debug("open a new context. hashCode[" + context.hashCode() + "] in new");
					contextQueue.push(newContext);

					returnContext = newContext;
				} else if (null == context && null != contextQueue) {
					ServiceContext newContext = new ServiceContext();
					log.debug("open a new context. hashCode[" + context.hashCode() + "] in new");
					contextQueue.push(newContext);

					returnContext = newContext;
				} else {
					throw new ServiceException("Wrong context and contextQueue in new");
				}
			} else {
				if (null != this.context) {
					returnContext = this.context;
				} else if (null != this.contextQueue) {
					returnContext = this.contextQueue.peek();
				} else {
					throw new ServiceException("Wrong context and contextQueue");
				}

				log.debug("follow an existing context. hashCode[" + context.hashCode() + "], context[" + (context.counter + 1) + "]");
				returnContext.counter++;
			}

			return returnContext;
		}

		/** 仅仅获取上下文 */
		public ServiceContext get() {
			if (null != this.context) {
				return this.context;
			} else if (null != this.contextQueue) {
				return this.contextQueue.peek();
			} else {
				return null;
			}
		}

		/**
		 * 回收上下文
		 * 
		 * @return true: 代表线程中需要删除
		 */
		public boolean recycle() {
			if (null != this.context) {
				this.context = null;
				return true;
			} else if (null != this.contextQueue) {
				this.contextQueue.pop();
				if (this.contextQueue.isEmpty()) {
					return true;
				}
			}
			return false;
		}
	}

	/** 方法之前的线程调用 */
	private static ServiceContext begin(boolean isNew) {
		ThreadServiceContext threadContext = contextThreadLocal.get();
		if (null == threadContext) {
			threadContext = new ThreadServiceContext();
			contextThreadLocal.set(threadContext);
		}
		return threadContext.getOrCreate(isNew);
	}

	/** 无异常的结束 */
	private static void endOnSuccess() {
		ThreadServiceContext threadContext = contextThreadLocal.get();
		if (null != threadContext) {
			ServiceContext context = threadContext.get();
			if (null == context) {
				contextThreadLocal.remove();
				return;
			}
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
				log.debug("close a context. hashCode[" + context.hashCode() + "]");
				context.stopMonitor();// stop monitor
				if (threadContext.recycle()) {
					contextThreadLocal.remove();
				}
			}
		} else {
			contextThreadLocal.remove();
		}
	}

	/** 发生异常的结束 */
	protected static void endOnException(Throwable throwable, AbstractServiceNode service) {
		// 如果当前可以处理,当前处理; 如果当前不能处理,上抛,不做日志输出
		ThreadServiceContext threadContext = contextThreadLocal.get();
		ServiceContext context = threadContext.get();
		if (--context.counter < 1) {
			context.finishOnException();
			log.error("Execute service exception: " + service.getServiceKey(), throwable);
			log.debug("close a context. hashCode[" + context.hashCode() + "] on exception");
			context.stopMonitor();
			if (threadContext.recycle()) {
				contextThreadLocal.remove();
			}
			// 最后一层抛出的异常
			if (throwable instanceof ServiceException) {
				throw (ServiceException) throwable;
			}
			throw new ServiceException("Execute service exception: " + service.getServiceKey(), throwable);
		} else {
			ServiceException ex = null;
			try {
				context.onException(service.getServiceType(), throwable, "Execute service exception: " + service.getServiceKey());
			} catch (ServiceException e) {
				ex = e;
			}
			if (null != ex) {
				throw ex;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T execute(String serviceId, Object arg) throws ServiceException {
		log.info("actuator service: " + serviceId);
		AbstractServiceNode service = TangYuanContainer.getInstance().getService(serviceId);
		if (null == service) {
			throw new ServiceException("Service does not exist: " + serviceId);// 上抛异常
		}
		// A. 获取上下文
		ServiceContext context = begin(false);
		// B.执行服务
		context.updateMonitor(serviceId);
		Object result = null;
		Throwable ex = null;
		try {
			Object data = converter.parameterConvert(arg, service.getResultType());
			service.execute(context, data);
			result = service.getResult(context);// 只有类型转换时发生异常
		} catch (Throwable e) {
			ex = e;
		} finally {
			if (null != ex) {
				endOnException(ex, service);
			} else {
				endOnSuccess();
			}
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
			throw new ServiceException("Service does not exist: " + serviceId);
		}
		ServiceContext context = begin(true);
		context.updateMonitor(serviceId);// monitor
		Object result = null;
		Throwable ex = null;
		try {
			Object data = converter.parameterConvert(arg, service.getResultType());
			service.execute(context, data);
			result = service.getResult(context);// 只有类型转换是发生异常
		} catch (Throwable e) {
			ex = e;
		} finally {
			if (null != ex) {
				try {
					endOnException(ex, service);
				} catch (ServiceException e) {
					if (throwException) {
						throw e;
					}
				}
			} else {
				endOnSuccess();
			}
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
			throw new ServiceException("Service does not exist: " + serviceId);
		}
		TangYuanContainer.getInstance().addAsyncTask(new AsyncTask() {
			@Override
			public void run() {
				ServiceContext context = begin(false);
				context.updateMonitor(serviceId);// monitor
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
					contextThreadLocal.remove();
					log.debug("close a context. hashCode[" + context.hashCode() + "]");
				}
			}
		});
	}

	// public static void executeAsync(final String serviceId, final Object arg) {
	// log.info("execute async service: " + serviceId);
	// final AbstractServiceNode service = TangYuanContainer.getInstance().getService(serviceId);
	// if (null == service) {
	// log.error("Service does not exist: " + serviceId);
	// return;
	// }
	// TangYuanContainer.getInstance().addAsyncTask(new AsyncTask() {
	// @Override
	// public void run() {
	// ServiceContext context = new ServiceContext();
	// // monitor
	// context.updateMonitor(serviceId);
	// try {
	// Object data = converter.parameterConvert(arg, service.getResultType());
	// service.execute(context, data);
	// // 这里是最终的提交
	// context.finish();
	// context.setResult(null);
	// } catch (Throwable e) {
	// // 这里是最终的回滚
	// context.finishOnException();
	// log.error("Execute service exception: " + serviceId, e);
	// } finally {
	// context.stopMonitor();
	// }
	// }
	// });
	// }

	// /** 方法之前的线程调用 */
	// private static ServiceContext begin() {
	// ServiceContext context = contextThreadLocal.get();
	// if (null == context) {
	// context = new ServiceContext();
	// log.debug("open a new context. hashCode[" + context.hashCode() + "]");
	// contextThreadLocal.set(context);
	// } else {
	// log.debug("follow an existing context. hashCode[" + context.hashCode() + "], context[" + (context.counter + 1) + "]");
	// context.counter++;
	// }
	// return context;
	// }

	// /** 无异常的结束 */
	// private static void endOnSuccess() {
	// ServiceContext context = contextThreadLocal.get();
	// if (null != context) {
	// log.debug("context--. hashCode[" + context.hashCode() + "], context[" + (context.counter - 1) + "]");
	// if (--context.counter < 1) {
	// if (null == context.getExceptionInfo()) {
	// try {
	// // 这里是确定的提交
	// context.finish();
	// } catch (Throwable e) {
	// context.finishOnException();
	// log.error("SqlService commit exception", e);
	// }
	// } else {
	// context.finishOnException();
	// }
	// contextThreadLocal.remove();
	// log.debug("close a context. hashCode[" + context.hashCode() + "]");
	// // monitor
	// context.stopMonitor();
	// }
	// } else {
	// contextThreadLocal.remove();
	// }
	// }

	// /** 发生异常的结束 */
	// protected static void endOnException(Throwable throwable, AbstractServiceNode service) {
	// // 如果当前可以处理,当前处理; 如果当前不能处理,上抛,不做日志输出
	// ServiceContext context = contextThreadLocal.get();
	// if (--context.counter < 1) {
	// context.finishOnException();
	// log.error("", throwable);
	// context.stopMonitor();
	// contextThreadLocal.remove();
	// // 最后一层抛出的异常
	// if (throwable instanceof ServiceException) {
	// throw (ServiceException) throwable;
	// }
	// throw new ServiceException("Execute service exception: " + service.getServiceKey(), throwable);
	// } else {
	// ServiceException ex = null;
	// try {
	// context.onException(service.getServiceType(), throwable, "Execute service exception: " + service.getServiceKey());
	// } catch (ServiceException e) {
	// ex = e;
	// }
	// if (null != ex) {
	// throw ex;
	// }
	// }
	// }

	// @SuppressWarnings("unchecked")
	// public static <T> T executeAlone(String serviceId, Object arg, boolean throwException) throws ServiceException {
	// log.info("execute alone service: " + serviceId);
	// AbstractServiceNode service = TangYuanContainer.getInstance().getService(serviceId);
	// if (null == service) {
	// throw new ServiceException("Service does not exist: " + serviceId);
	// }
	// ServiceContext context = begin(true);
	// // monitor
	// context.updateMonitor(serviceId);
	// Object result = null;
	// try {
	// Object data = converter.parameterConvert(arg, service.getResultType());
	// service.execute(context, data);
	// context.finish();
	// result = service.getResult(context);// 只有类型转换是发生异常
	// } catch (Throwable e) {
	// context.finishOnException();
	// if (throwException) {
	// if (e instanceof ServiceException) {
	// throw (ServiceException) e;
	// } else {
	// throw new ServiceException("Execute service exception: " + serviceId, e);
	// }
	// } else {
	// log.error("Execute service exception: " + serviceId, e);
	// }
	// } finally {
	// context.stopMonitor();
	// }
	// return (T) result;
	// }
}
