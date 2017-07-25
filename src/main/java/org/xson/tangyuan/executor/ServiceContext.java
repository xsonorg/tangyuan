package org.xson.tangyuan.executor;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.executor.monitor.SqlServiceContextInfo;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.xml.node.AbstractServiceNode.TangYuanServiceType;

public class ServiceContext {

	private static Log				log					= LogFactory.getLog(ServiceContext.class);

	private SqlServiceContext		sqlServiceContext	= null;
	private IServiceContext			mongoServiceContext	= null;
	private IServiceContext			javaServiceContext	= null;
	private IServiceContext			mqServiceContext	= null;

	// private IServiceContext hiveServiceContext = null;
	// private IServiceContext hbaseServiceContext = null;

	/**
	 * 使用计数器
	 */
	protected int					counter				= 1;
	/**
	 * 结果返回对象:组合服务专用
	 */
	private Object					result				= null;

	/**
	 * 服务执行过程中的异常辅助信息
	 */
	private IServiceExceptionInfo	exceptionInfo		= null;

	/**
	 * 监控信息
	 */
	private SqlServiceContextInfo	contextInfo			= null;

	public ServiceContext() {
		if (TangYuanContainer.getInstance().isServiceMonitor()) {
			this.contextInfo = new SqlServiceContextInfo(this.hashCode());// TODO
			this.contextInfo.joinMonitor();
		}
	}

	/**
	 * 更新监控信息
	 */
	public void updateMonitor(String service) {
		if (null != contextInfo) {
			contextInfo.update(service);
		}
	}

	/**
	 * 停止监控信息
	 */
	public void stopMonitor() {
		if (null != contextInfo) {
			contextInfo.stop();
		}
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public void setExceptionInfo(IServiceExceptionInfo exceptionInfo) {
		this.exceptionInfo = exceptionInfo;
	}

	public IServiceExceptionInfo getExceptionInfo() {
		return exceptionInfo;
	}

	public SqlServiceContext getSqlServiceContext() {
		if (null == sqlServiceContext) {
			sqlServiceContext = new SqlServiceContext();
		}
		return sqlServiceContext;
	}

	public IServiceContext getServiceContext(TangYuanServiceType type) {
		if (TangYuanServiceType.SQL == type) {
			if (null == sqlServiceContext) {
				sqlServiceContext = new SqlServiceContext();
			}
			return sqlServiceContext;
		} else if (TangYuanServiceType.MONGO == type) {
			if (null == mongoServiceContext) {
				mongoServiceContext = TangYuanContainer.getInstance().getContextFactory(type).create();
			}
			return mongoServiceContext;
		} else if (TangYuanServiceType.JAVA == type) {
			if (null == javaServiceContext) {
				javaServiceContext = new JavaServiceContext();
			}
			return javaServiceContext;
		}
		// else if (TangYuanServiceType.HIVE == type) {
		// if (null == hiveServiceContext) {
		// hiveServiceContext = TangYuanContainer.getInstance().getContextFactory(type).create();
		// }
		// return hiveServiceContext;
		// } else if (TangYuanServiceType.HBASE == type) {
		// if (null == hbaseServiceContext) {
		// hbaseServiceContext = TangYuanContainer.getInstance().getContextFactory(type).create();
		// }
		// return hbaseServiceContext;
		// }
		else if (TangYuanServiceType.MQ == type) {
			if (null == mqServiceContext) {
				mqServiceContext = TangYuanContainer.getInstance().getContextFactory(type).create();
			}
			return mqServiceContext;
		}

		return getSqlServiceContext();
	}

	public void finish() throws Throwable {
		if (null != sqlServiceContext) {
			sqlServiceContext.commit(true);// 这里是最终的提交
			sqlServiceContext = null;
		}
		if (null != mongoServiceContext) {
			mongoServiceContext.commit(true);
			mongoServiceContext = null;
		}
		if (null != javaServiceContext) {
			javaServiceContext.commit(true);
			javaServiceContext = null;
		}
		// if (null != hiveServiceContext) {
		// hiveServiceContext.commit(true);
		// hiveServiceContext = null;
		// }
		// if (null != hbaseServiceContext) {
		// hbaseServiceContext.commit(true);
		// hbaseServiceContext = null;
		// }

		if (null != mqServiceContext) {
			mqServiceContext.commit(true);
			mqServiceContext = null;
		}
	}

	public void finishOnException() {
		if (null != sqlServiceContext) {
			sqlServiceContext.rollback();
			sqlServiceContext = null;
		}

		if (null != mongoServiceContext) {
			mongoServiceContext.rollback();
			mongoServiceContext = null;
		}

		if (null != javaServiceContext) {
			javaServiceContext.rollback();
			javaServiceContext = null;
		}
		// if (null != hiveServiceContext) {
		// hiveServiceContext.rollback();
		// hiveServiceContext = null;
		// }
		// if (null != hbaseServiceContext) {
		// hbaseServiceContext.rollback();
		// hbaseServiceContext = null;
		// }

		if (null != mqServiceContext) {
			mqServiceContext.rollback();
			mqServiceContext = null;
		}

		this.exceptionInfo = null;
	}

	/**
	 * 异常发生时候入口方法
	 */
	public void onException(TangYuanServiceType type, Throwable e, String message) throws ServiceException {
		boolean canProcess = getServiceContext(type).onException(exceptionInfo);
		if (canProcess) {
			this.exceptionInfo = null;
			log.error(message, e);
		} else {
			if (e instanceof ServiceException) {
				throw (ServiceException) e;
			}
			throw new ServiceException(message, e);
		}
	}
}
