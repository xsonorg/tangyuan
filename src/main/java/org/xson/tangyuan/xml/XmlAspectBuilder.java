package org.xson.tangyuan.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.aspect.AfterVo;
import org.xson.tangyuan.aspect.AspectServiceMappingVo;
import org.xson.tangyuan.aspect.AspectSupport;
import org.xson.tangyuan.aspect.AspectVo;
import org.xson.tangyuan.aspect.AspectVo.AspectCondition;
import org.xson.tangyuan.aspect.BeforeVo;
import org.xson.tangyuan.bridge.BridgedCallSupport;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.node.AbstractServiceNode;
import org.xson.tangyuan.xml.node.CallNode.CallMode;

public class XmlAspectBuilder {

	private Log				log				= LogFactory.getLog(getClass());
	private XPathParser		xPathParser		= null;

	private List<AspectVo>	beforeNodeList	= new ArrayList<AspectVo>();
	private List<AspectVo>	afterNodeList	= new ArrayList<AspectVo>();

	public XmlAspectBuilder(InputStream inputStream) {
		this.xPathParser = new XPathParser(inputStream);
	}

	public void parse() {
		configurationElement(xPathParser.evalNode("/aspect"));
	}

	private void configurationElement(XmlNodeWrapper context) {
		try {
			buildAspectNodes(context.evalNodes("before"), true);
			buildAspectNodes(context.evalNodes("after"), false);
			bind();
		} catch (Throwable e) {
			throw new XmlParseException(e);
		}
	}

	private void buildAspectNodes(List<XmlNodeWrapper> contexts, boolean before) throws Throwable {
		for (XmlNodeWrapper context : contexts) {

			String exec = StringUtils.trim(context.getStringAttribute("exec"));
			if (null == exec || 0 == exec.length()) {
				throw new XmlParseException("In the before or after node, the exec attribute can not be empty");
			}

			String _mode = StringUtils.trim(context.getStringAttribute("mode"));
			String _order = StringUtils.trim(context.getStringAttribute("order"));
			String _propagation = StringUtils.trim(context.getStringAttribute("propagation"));
			String _condition = StringUtils.trim(context.getStringAttribute("condition"));

			CallMode mode = CallMode.ALONE;
			if (null != _mode) {
				mode = getCallMode(_mode);
			}
			boolean propagation = false;
			if (null != _propagation) {
				propagation = Boolean.parseBoolean(_propagation);
			}
			int order = 10;
			if (null != _order) {
				order = Integer.parseInt(_order);
			}
			AspectCondition condition = AspectCondition.SUCCESS;
			if (null != _condition) {
				condition = getAspectCondition(_condition);
			}
			boolean bridged = BridgedCallSupport.isBridged(exec);

			List<String> includeList = new ArrayList<String>();
			List<String> excludeList = new ArrayList<String>();

			// <include></include>
			List<XmlNodeWrapper> includeNodes = context.evalNodes("include");
			for (XmlNodeWrapper include : includeNodes) {
				String body = StringUtils.trim(include.getStringBody());
				if (null != body) {
					includeList.add(body);
				}
			}
			if (includeList.size() == 0) {
				includeList = null;
			}

			// <exclude></exclude>
			List<XmlNodeWrapper> excludeNodes = context.evalNodes("exclude");
			for (XmlNodeWrapper exclude : excludeNodes) {
				String body = StringUtils.trim(exclude.getStringBody());
				if (null != body) {
					excludeList.add(body);
				}
			}
			if (excludeList.size() == 0) {
				excludeList = null;
			}

			if (null == includeList && null == excludeList) {
				throw new XmlParseException("before node missing <include|exclude>: " + exec);
			}

			if (before) {
				AspectVo aVo = new BeforeVo(exec, mode, propagation, order, bridged, includeList, excludeList);
				beforeNodeList.add(aVo);
				log.info("add before node: " + exec);
			} else {
				AspectVo aVo = new AfterVo(exec, mode, propagation, order, condition, bridged, includeList, excludeList);
				afterNodeList.add(aVo);
				log.info("add after node: " + exec);
			}
		}
	}

	private void bind() {
		if (0 == this.beforeNodeList.size() && 0 == this.afterNodeList.size()) {
			return;
		}

		Collections.sort(this.beforeNodeList);
		Collections.sort(this.afterNodeList);

		Set<String> services = TangYuanContainer.getInstance().getServicesKeySet();
		for (String service : services) {

			AbstractServiceNode serviceNode = TangYuanContainer.getInstance().getService(service);

			List<AspectVo> tempBeforeList = new ArrayList<AspectVo>();
			List<AspectVo> tempAfterList = new ArrayList<AspectVo>();

			String logstr = "";

			for (AspectVo bVo : this.beforeNodeList) {
				if (bVo.match(service)) {
					tempBeforeList.add(bVo);
				}
			}

			for (AspectVo aVo : this.afterNodeList) {
				if (aVo.match(service)) {
					tempAfterList.add(aVo);
				}
			}

			if (0 == tempBeforeList.size()) {
				tempBeforeList = null;
			} else {
				serviceNode.setBeforeAspect(true);
				logstr += "before";
			}

			if (0 == tempAfterList.size()) {
				tempAfterList = null;
			} else {
				serviceNode.setAfterAspect(true);
				logstr += ",after";
			}

			if (null == tempBeforeList && null == tempAfterList) {
				continue;
			}

			AspectServiceMappingVo asmVo = new AspectServiceMappingVo(service, tempBeforeList, tempAfterList);
			AspectSupport.getInstance().bind(service, asmVo);

			log.info("service bind aspect[" + logstr + "]: " + service);
		}
	}

	protected CallMode getCallMode(String str) {
		if (null == str) {
			return null;
		}
		if ("EXTEND".equalsIgnoreCase(str)) {
			return CallMode.EXTEND;
		} else if ("ALONE".equalsIgnoreCase(str)) {
			return CallMode.ALONE;
		} else if ("ASYNC".equalsIgnoreCase(str)) {
			return CallMode.ASYNC;
		}
		return null;
	}

	private AspectCondition getAspectCondition(String str) {
		if ("SUCCESS".equalsIgnoreCase(str)) {
			return AspectCondition.SUCCESS;
		} else if ("EXCEPTION".equalsIgnoreCase(str)) {
			return AspectCondition.EXCEPTION;
		} else if ("ALL".equalsIgnoreCase(str)) {
			return AspectCondition.ALL;
		} else {
			return AspectCondition.SUCCESS;
		}
	}

}
