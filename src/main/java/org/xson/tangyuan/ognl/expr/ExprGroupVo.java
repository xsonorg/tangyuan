package org.xson.tangyuan.ognl.expr;

import java.util.ArrayList;
import java.util.List;

public class ExprGroupVo {

	private ExprVo			exprVo		= null;

	private List<ExprVo>	exprList	= null;

	// [0: 初始, 1:and, 2:or]
	private int				andOr		= 0;

	protected void addUnit(String var, boolean isStringConstant) {
		// first process and or
		if (!isStringConstant) {
			if ("and".equalsIgnoreCase(var)) {
				if (andOr == 2) {
					throw new RuntimeException("多条件判断的是否[and|or]只能选其一");
				}
				andOr = 1;
				return;
			}
			if ("or".equalsIgnoreCase(var)) {
				if (andOr == 1) {
					throw new RuntimeException("多条件判断的是否[and|or]只能选其一");
				}
				andOr = 2;
				return;
			}
		}

		if (null == exprVo && null == exprList) {
			exprVo = new ExprVo();
			exprVo.addUnit(var, isStringConstant);
			return;
		} else if (null != exprVo && null == exprList) {
			if (exprVo.check()) {
				if (null == exprList) {
					exprList = new ArrayList<>();
					exprList.add(exprVo);
					exprVo = null;
				}
			} else {
				exprVo.addUnit(var, isStringConstant);
				return;
			}
		}
		// process list
		ExprVo lastExprVo = exprList.get(exprList.size() - 1);
		if (lastExprVo.check()) {
			lastExprVo = new ExprVo();
			exprList.add(lastExprVo);
		}
		lastExprVo.addUnit(var, isStringConstant);
	}

	protected void addOperators(String var) {
		if (null != exprVo) {
			exprVo.addOperators(var);
		} else if (null != exprList && exprList.size() > 0) {
			exprList.get(exprList.size() - 1).addOperators(var);
		}
	}

	// public boolean getResult(Map<String, Object> data) {
	// if (null != exprVo) {
	// return exprVo.getResult(data);
	// } else {
	// if (1 == andOr) {
	// for (int i = 0; i < exprList.size(); i++) {
	// if (!exprList.get(i).getResult(data)) {
	// return false;
	// }
	// }
	// return true;
	// } else {
	// for (int i = 0; i < exprList.size(); i++) {
	// if (exprList.get(i).getResult(data)) {
	// return true;
	// }
	// }
	// return false;
	// }
	// }
	// }

	public boolean getResult(Object data) {
		if (null != exprVo) {
			return exprVo.getResult(data);
		} else {
			if (1 == andOr) {
				for (int i = 0; i < exprList.size(); i++) {
					if (!exprList.get(i).getResult(data)) {
						return false;
					}
				}
				return true;
			} else {
				for (int i = 0; i < exprList.size(); i++) {
					if (exprList.get(i).getResult(data)) {
						return true;
					}
				}
				return false;
			}
		}
	}

	protected boolean check() {
		if (null != exprVo) {
			return exprVo.check();
		} else if (null != exprList && exprList.size() > 0) {
			return exprList.get(exprList.size() - 1).check();
		}
		return false;
	}
}
