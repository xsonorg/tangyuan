package org.xson.tangyuan.xml.node;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.executor.SqlServiceContext;
import org.xson.tangyuan.ognl.expr.ExprGroupVo;
import org.xson.tangyuan.xml.XmlParseException;

public class IfNode implements SqlNode {

	private ExprGroupVo		test;

	private SqlNode			sqlNode;

	private List<IfNode>	elseIfList;

	private boolean			hasElseNode	= false;

	public IfNode(SqlNode sqlNode, ExprGroupVo test) {
		this.sqlNode = sqlNode;
		this.test = test;
	}

	public boolean isHasElseNode() {
		return hasElseNode;
	}

	public void addElseNode(IfNode node) {
		if (hasElseNode) {
			throw new XmlParseException("当前节点不能再加入else node");
		}
		addElseIfNode(node);
		hasElseNode = true;
	}

	public void addElseIfNode(IfNode node) {
		if (hasElseNode) {
			throw new XmlParseException("当前节点不能再加入else if node");
		}
		if (null == elseIfList) {
			elseIfList = new ArrayList<IfNode>();
		}
		elseIfList.add(node);
	}

	/**
	 * true: 代表执行了(表达式通过), false: 代表不能执行(表达式不通过)
	 */
	@Override
	public boolean execute(SqlServiceContext context, Object arg) throws Throwable {
		// 这里可以认识全部是IF, 表达式通过:true, 否则:false
		if (null == test || test.getResult(arg)) {
			sqlNode.execute(context, arg);
			return true;
		} else if (null != elseIfList) {
			for (IfNode ifNode : elseIfList) {
				if (ifNode.execute(context, arg)) {
					break;// TODO 是否return true也可
				}
			}
		}
		return false;
	}
}
