package org.xson.tangyuan.xml;

public interface XmlExtendBuilder {

	// public void parse(XmlConfigurationBuilder xmlConfigurationBuilder, String resource) throws Throwable;

	public XmlExtendCloseHook parse(XmlConfigurationBuilder xmlConfigurationBuilder, String resource) throws Throwable;

}
