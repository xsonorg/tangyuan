package org.xson.tangyuan.executor;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.ognl.convert.ParameterConverter;
import org.xson.tangyuan.task.AsyncTask;
import org.xson.tangyuan.xml.node.AbstractSqlNode;

public class SqlServiceActuator {

	private static Log								log					= LogFactory.getLog(SqlServiceActuator.class);

	private static ThreadLocal<SqlServiceContext>	contextThreadLocal	= new ThreadLocal<SqlServiceContext>();

	private static ParameterConverter				converter			= new ParameterConverter();

	/**
	 * 方法之前的线程调用
	 */
	public static void begin() {
		SqlServiceContext context = contextThreadLocal.get();
		if (null == context) {
			context = new SqlServiceContext();
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
		SqlServiceContext context = contextThreadLocal.get();
		if (null != context) {
			log.debug("context--. hashCode[" + context.hashCode() + "], context[" + (context.counter - 1) + "]");
			if (--context.counter < 1) {
				if (null == context.getExceptionInfo()) {
					try {
						context.commit(true);// 这里是确定的提交
					} catch (Throwable e) {
						context.rollbackAll();
						log.error("SqlService commit exception", e);
					}
				} else {
					context.rollbackAll();
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
	// public static void end() {
	// SqlServiceContext context = contextThreadLocal.get();
	// if (null != context) {
	// log.debug("context--. hashCode[" + context.hashCode() + "], context[" + (context.counter - 1) + "]");
	// if (--context.counter < 1) {
	// if (null == context.getException()) {
	// try {
	// // 这里是确定的提交
	// context.commit(true);
	// } catch (Throwable e) {
	// try {
	// context.rollback();
	// } catch (SQLException e1) {
	// log.error("SqlService rollback exception", e);
	// }
	// log.error("SqlService commit exception", e);
	// }
	// } else {
	// try {
	// context.rollback();
	// } catch (SQLException e) {
	// log.error("SqlService rollback exception", e);
	// }
	// }
	// contextThreadLocal.remove();
	// log.debug("close a context. hashCode[" + context.hashCode() + "]");
	// }
	// } else {
	// contextThreadLocal.remove();
	// }
	// }

	// public static void onException(Throwable throwable) throws Throwable {
	// // 这里只是拦截漏网的
	// // log.error("SqlService onException", throwable);
	// log.debug("SqlService onException");
	// SqlServiceContext context = contextThreadLocal.get();
	// if (null != context) {
	// if (throwable instanceof SqlServiceException) {
	// // SQL服务本身抛出的异常
	// SqlServiceException ex = (SqlServiceException) throwable;
	// if (!ex.isRollback()) {
	// context.recursionRollback();
	// }
	// } else {
	// context.recursionRollback();
	// }
	// log.debug("remove a context on exception. hashCode[" + context.hashCode() + "]");
	// }
	// contextThreadLocal.remove();
	// throw throwable;
	// }

	/**
	 * 拦截器调用
	 */
	public static void onException(Throwable throwable) throws Throwable {
		// 这里只是拦截漏网的
		log.debug("SqlService onException");
		SqlServiceContext context = contextThreadLocal.get();
		if (null != context) {
			if (null != context.getExceptionInfo()) {
				context.rollbackAll();
			}
			log.debug("remove a context on exception. hashCode[" + context.hashCode() + "]");

			// monitor
			context.stopMonitor();
		}
		contextThreadLocal.remove();
		// if (throwable instanceof SqlServiceException) {
		// throw throwable;// SQL服务本身抛出的异常
		// } else {
		// throw new SqlServiceException(throwable);
		// }
		log.error("", throwable);
		throw throwable;
	}

	public static <T> T execute(String serviceId, Object arg) throws SqlServiceException {
		log.info("actuator service: " + serviceId);
		SqlServiceContext context = contextThreadLocal.get();
		if (null == context) {
			// SqlServiceException ex = new SqlServiceException("服务不存在上下文: " + serviceId);
			// ex.setRollback(true);
			// 无context, 不涉及回滚
			throw new SqlServiceException("Service context does not exist: " + serviceId);
		} else {
			return executeContext(serviceId, context, arg);
		}
	}

	/**
	 * 上下文环境
	 */
	@SuppressWarnings("unchecked")
	public static <T> T executeContext(String serviceId, SqlServiceContext context, Object arg) {

		// monitor
		context.updateMonitor(serviceId);

		AbstractSqlNode service = TangYuanContainer.getInstance().getSqlService(serviceId);
		if (null == service) {
			// 这里可以强制回滚,生产环境不会出现
			context.onException(null, "Service does not exist: " + serviceId);
		}
		Object result = null;
		try {
			Object data = converter.parameterConvert(arg, service.getResultType());
			service.execute(context, data);
			result = service.getResult(context);// 只有类型转换时发生异常
		} catch (Throwable e) {
			e.printStackTrace();
			context.onException(e, "actuator service exception: " + serviceId);
		}
		return (T) result;
	}

	/**
	 * 单独环境
	 */
	@SuppressWarnings("unchecked")
	public static <T> T executeAlone(String serviceId, Object arg) throws SqlServiceException {
		log.info("executeAlone service: " + serviceId);
		AbstractSqlNode service = TangYuanContainer.getInstance().getSqlService(serviceId);
		if (null == service) {
			// 生产环境不会出现, 并且不涉及回滚
			// SqlServiceException ex = new SqlServiceException("不存在的服务:" + serviceId);
			// ex.setRollback(true);
			throw new SqlServiceException("Service does not exist: " + serviceId);
		}
		SqlServiceContext context = new SqlServiceContext();

		// monitor
		context.updateMonitor(serviceId);

		Object result = null;
		try {
			Object data = converter.parameterConvert(arg, service.getResultType());
			service.execute(context, data);
			context.commit(true);
			result = service.getResult(context);// 只有类型转换是发生异常
		} catch (Throwable e) {
			// try {
			// context.rollback();
			// } catch (SQLException e1) {
			// }
			// SqlServiceException ex = null;
			// if (e instanceof SqlServiceException) {
			// ex = (SqlServiceException) e;
			// } else {
			// ex = new SqlServiceException("actuator service exception: " + serviceId, e);
			// }
			// ex.setRollback(true);
			// throw ex;
			context.rollbackAll();

			e.printStackTrace();// TODO

			if (e instanceof SqlServiceException) {
				throw (SqlServiceException) e;
			} else {
				throw new SqlServiceException("actuator service exception: " + serviceId, e);
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
		final AbstractSqlNode service = TangYuanContainer.getInstance().getSqlService(serviceId);
		if (null == service) {
			log.error("Service does not exist: " + serviceId);
			return;
		}
		TangYuanContainer.getInstance().addAsyncTask(new AsyncTask() {
			@Override
			public void run() {
				SqlServiceContext context = new SqlServiceContext();

				// monitor
				context.updateMonitor(serviceId);

				try {
					Object data = converter.parameterConvert(arg, service.getResultType());
					service.execute(context, data);
					context.commit(true);
					context.setResult(null);
				} catch (Throwable e) {
					// try {
					// context.rollback();
					// } catch (SQLException e1) {
					// }
					context.rollbackAll();
					log.error("actuator service exception: " + serviceId, e);
				} finally {
					context.stopMonitor();
				}
			}
		});
	}

}
