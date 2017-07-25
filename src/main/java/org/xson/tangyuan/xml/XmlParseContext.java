package org.xson.tangyuan.xml;

import java.util.HashMap;
import java.util.Map;

import org.xson.tangyuan.cache.vo.CacheVo;
import org.xson.tangyuan.mapping.MappingVo;
import org.xson.tangyuan.transaction.DefaultTransactionMatcher;
import org.xson.tangyuan.xml.node.TangYuanNode;

public class XmlParseContext {

	/** 全局<sql> */
	// private Map<String, TangYuanNode> integralRefMap = null;
	// fix bug
	private Map<String, TangYuanNode>	integralRefMap			= new HashMap<String, TangYuanNode>();

	/** 全局服务 */
	// private Map<String, Integer> integralServiceMap = null;
	// fix bug
	private Map<String, Integer>		integralServiceMap		= new HashMap<String, Integer>();

	/** 缓存VO */
	private Map<String, MappingVo>		mappingVoMap			= null;

	/** 默认缓存 */
	private CacheVo						defaultCacheVo			= null;

	/** 缓存定义集合 */
	private Map<String, CacheVo>		cacheVoMap				= null;

	private Map<String, Integer>		integralServiceNsMap	= new HashMap<String, Integer>();
	private Map<String, Integer>		integralServiceClassMap	= new HashMap<String, Integer>();

	/** 事务匹配器 */
	private DefaultTransactionMatcher	transactionMatcher		= new DefaultTransactionMatcher();

	public Map<String, TangYuanNode> getIntegralRefMap() {
		return integralRefMap;
	}

	// public void setIntegralRefMap(Map<String, TangYuanNode> integralRefMap) {
	// this.integralRefMap = integralRefMap;
	// }

	public Map<String, Integer> getIntegralServiceMap() {
		return integralServiceMap;
	}

	// public void setIntegralServiceMap(Map<String, Integer> integralServiceMap) {
	// this.integralServiceMap = integralServiceMap;
	// }

	public Map<String, MappingVo> getMappingVoMap() {
		return mappingVoMap;
	}

	public void setMappingVoMap(Map<String, MappingVo> mappingVoMap) {
		this.mappingVoMap = mappingVoMap;
	}

	public DefaultTransactionMatcher getTransactionMatcher() {
		return transactionMatcher;
	}

	public CacheVo getDefaultCacheVo() {
		return defaultCacheVo;
	}

	public void setDefaultCacheVo(CacheVo defaultCacheVo) {
		this.defaultCacheVo = defaultCacheVo;
	}

	public Map<String, CacheVo> getCacheVoMap() {
		return cacheVoMap;
	}

	public void setCacheVoMap(Map<String, CacheVo> cacheVoMap) {
		this.cacheVoMap = cacheVoMap;
	}

	public Map<String, Integer> getIntegralServiceNsMap() {
		return integralServiceNsMap;
	}

	public Map<String, Integer> getIntegralServiceClassMap() {
		return integralServiceClassMap;
	}

	public void clean() {
		// fix bug
		if (null != this.integralRefMap) {
			this.integralRefMap.clear();
			this.integralRefMap = null;
		}

		if (null != this.integralServiceMap) {
			this.integralServiceMap.clear();
			this.integralServiceMap = null;
		}

		this.mappingVoMap = null;

		this.cacheVoMap = null;

		this.defaultCacheVo = null;

		if (null != this.integralServiceNsMap) {
			this.integralServiceNsMap.clear();
			this.integralServiceNsMap = null;
		}

		if (null != this.integralServiceClassMap) {
			this.integralServiceClassMap.clear();
			this.integralServiceClassMap = null;
		}

		this.transactionMatcher = null;
	}
}
