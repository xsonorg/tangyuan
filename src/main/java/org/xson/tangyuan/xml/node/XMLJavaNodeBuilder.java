package org.xson.tangyuan.xml.node;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.executor.CglibProxy;
import org.xson.tangyuan.logging.Log;
import org.xson.tangyuan.logging.LogFactory;
import org.xson.tangyuan.util.ClassUtils;
import org.xson.tangyuan.util.PatternMatchUtils;
import org.xson.tangyuan.util.StringUtils;
import org.xson.tangyuan.xml.XmlNodeBuilder;
import org.xson.tangyuan.xml.XmlNodeWrapper;
import org.xson.tangyuan.xml.XmlParseContext;
import org.xson.tangyuan.xml.XmlParseException;

public class XMLJavaNodeBuilder extends XmlNodeBuilder {

	private Log				log		= LogFactory.getLog(getClass());

	private XmlNodeWrapper	root	= null;

	// private CglibProxy proxy = new CglibProxy();

	@Override
	public void parseRef() {
		// TODO Auto-generated method stub
	}

	@Override
	public void parseService() {
		List<XmlNodeWrapper> contexts = this.root.evalNodes("service");
		for (XmlNodeWrapper context : contexts) {
			String ns = StringUtils.trim(context.getStringAttribute("ns"));
			String clazz = StringUtils.trim(context.getStringAttribute("class"));

			if (null == clazz || clazz.length() == 0) {
				throw new XmlParseException("Invalid service class");
			}

			// 判断clazz重复
			if (this.parseContext.getIntegralServiceClassMap().containsKey(clazz)) {
				throw new XmlParseException("Duplicate service class: " + clazz);
			}

			if (null == ns || ns.trim().length() == 0) {
				ns = getSimpleClassName(clazz);
			}

			// 判断id重复
			if (this.parseContext.getIntegralServiceNsMap().containsKey(ns)) {
				throw new XmlParseException("Duplicate service ns: " + ns);
			}

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

			// <exclude></exclude>
			List<XmlNodeWrapper> excludeNodes = context.evalNodes("exclude");
			for (XmlNodeWrapper exclude : excludeNodes) {
				String body = StringUtils.trim(exclude.getStringBody());
				if (null != body) {
					excludeList.add(body);
				}
			}

			parseClass(ns, clazz, includeList, excludeList);

			this.parseContext.getIntegralServiceNsMap().put(ns, 0);
			this.parseContext.getIntegralServiceClassMap().put(clazz, 0);
		}
	}

	/** 类解析 */
	private void parseClass(String ns, String className, List<String> includeList, List<String> excludeList) {
		Class<?> clazz = ClassUtils.forName(className);

		// Object instance = proxy.getProxy(clazz);
		Object instance = new CglibProxy().getProxy(clazz);

		Method[] methods = clazz.getMethods();
		for (Method m : methods) {
			if (isIgnoredMethod(m.getName())) {
				continue;
			}

			boolean continueFlag = false;

			for (String pattern : excludeList) {
				if (PatternMatchUtils.simpleMatch(pattern, m.getName())) {
					continueFlag = true;
					break;
				}
			}

			if (continueFlag) {
				continue;
			}

			if (includeList.size() > 0) {
				for (String pattern : includeList) {
					if (!PatternMatchUtils.simpleMatch(pattern, m.getName())) {
						continueFlag = true;
						break;
					}
				}
			}

			if (continueFlag) {
				continue;
			}

			// Ignored static method
			int modifiers = m.getModifiers();
			if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isNative(modifiers) || Modifier.isInterface(modifiers)
					|| Modifier.isAbstract(modifiers) || Modifier.isStrict(modifiers)) {
				continue;
			}

			AbstractServiceNode service = createService(instance, m, ns);

			TangYuanContainer.getInstance().addService(service);
			log.info("add <service> node: " + service.getServiceKey());
		}
	}

	private AbstractServiceNode createService(Object instance, Method method, String ns) {
		return new JavaServiceNode(method.getName(), ns, getFullId(ns, method.getName()), instance, method);
	}

	// 取类的名称
	private String getSimpleClassName(String clazz) {
		int pos = clazz.lastIndexOf(".");
		if (pos > -1) {
			clazz = clazz.substring(pos + 1);
		}
		clazz = clazz.substring(0, 1).toLowerCase() + clazz.substring(1);
		return clazz;
	}

	/** 需要忽略的方法 */
	private String[] ignoredMethodName = { "main", "getClass", "hashCode", "equals", "toString", "notify", "notifyAll", "wait" };

	private boolean isIgnoredMethod(String name) {
		for (int i = 0; i < ignoredMethodName.length; i++) {
			if (name.equals(ignoredMethodName[i])) {
				return true;
			}
		}
		return false;
	}

	protected String getFullId(String ns, String id) {
		if (null == ns || "".equals(ns)) {
			return id;
		}
		return ns + "." + id;
	}

	@Override
	public void setContext(XmlNodeWrapper root, XmlParseContext context) {
		this.parseContext = context;
		this.root = root;
	}

	public static void main(String[] args) {
		// TODO
		Class<?> clazz = ClassUtils.forName(XMLJavaNodeBuilder.class.getName());

		Method[] methods = clazz.getMethods();

		// ignos:some method
		for (int i = 0; i < methods.length; i++) {
			System.out.println(methods[i].getName());
		}
	}
}
