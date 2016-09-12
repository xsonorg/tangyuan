package org.xson.tangyuan.executor;

/**
 * SqlService执行过程中的异常辅助信息
 */
public class SqlServiceExceptionInfo {

	/**
	 * 是否是一个独立的事务
	 */
	private boolean	newTranscation;

	/**
	 * 是否已经创建事务
	 */
	private boolean	createdTranscation;

	public SqlServiceExceptionInfo() {
	}

	public SqlServiceExceptionInfo(boolean newTranscation, boolean createdTranscation) {
		this.newTranscation = newTranscation;
		this.createdTranscation = createdTranscation;
	}

	public boolean isNewTranscation() {
		return newTranscation;
	}

	public void setNewTranscation(boolean newTranscation) {
		this.newTranscation = newTranscation;
	}

	public boolean isCreatedTranscation() {
		return createdTranscation;
	}

	public void setCreatedTranscation(boolean createdTranscation) {
		this.createdTranscation = createdTranscation;
	}

}
