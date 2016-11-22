package org.xson.tangyuan.executor;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;

public class ServiceException extends TangYuanException {

	private static final long	serialVersionUID	= 7857190361062588205L;

	// private ExceptionPosition exPosition;
	//
	// private boolean newTranscation;
	// /**
	// * 是否已经回滚[true:已经回滚]
	// */
	// private boolean rollback;
	// public enum ExceptionPosition {
	// BEFORE, AMONG, AFTER
	// }
	// public ExceptionPosition getExPosition() {
	// return exPosition;
	// }
	//
	// public void setExPosition(ExceptionPosition exPosition) {
	// this.exPosition = exPosition;
	// }
	//
	// public boolean isNewTranscation() {
	// return newTranscation;
	// }
	//
	// public void setNewTranscation(boolean newTranscation) {
	// this.newTranscation = newTranscation;
	// }
	//
	// public boolean isRollback() {
	// return rollback;
	// }
	//
	// public void setRollback(boolean rollback) {
	// this.rollback = rollback;
	// }

	// 错误信息
	private int					errorCode			= TangYuanContainer.getInstance().getSqlServiceErrorCode();

	private String				errorMessage		= TangYuanContainer.getInstance().getSqlServiceErrorMessage();

	public ServiceException() {
		super();
	}

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}

	public ServiceException(int errorCode, String errorMessage) {
		super(errorMessage);
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public ServiceException(int errorCode, String errorMessage, Throwable cause) {
		super(errorMessage, cause);
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
