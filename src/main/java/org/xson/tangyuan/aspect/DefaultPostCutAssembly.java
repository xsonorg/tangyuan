package org.xson.tangyuan.aspect;

import org.xson.common.object.XCO;
import org.xson.rpc.RpcConfig;
import org.xson.rpc.RpcException;
import org.xson.tangyuan.executor.ServiceException;
import org.xson.tangyuan.ognl.vars.ArgSelfVo;

public class DefaultPostCutAssembly implements PostCutAssembly {

	@Override
	public Object assembly(String service, Object arg, Object result, Throwable ex) {
		XCO xco = new XCO();
		xco.setObjectValue(ArgSelfVo.AEG_SELF_MARK, arg);
		xco.setObjectValue("$$RETURN", result);

		if (null == ex) {
			xco.setIntegerValue(RpcConfig.XCO_CODE_KEY, RpcConfig.SUCCESS_CODE_RPC);
		} else {

			int errorCode = 0;
			String errorMessage = null;

			if (ex instanceof RpcException) {
				RpcException e = (RpcException) ex;
				errorCode = e.getCode();
				errorMessage = e.getMessage();
			} else if (ex instanceof ServiceException) {
				ServiceException e = (ServiceException) ex;
				errorCode = e.getErrorCode();
				errorMessage = e.getErrorMessage();
			} else {
				errorCode = RpcConfig.RPC_ERROR_CODE;
				errorMessage = ex.getMessage();
			}

			xco.setIntegerValue(RpcConfig.XCO_CODE_KEY, errorCode);
			xco.setStringValue(RpcConfig.XCO_MESSAGE_KEY, errorMessage);
		}

		return xco;
	}

}
