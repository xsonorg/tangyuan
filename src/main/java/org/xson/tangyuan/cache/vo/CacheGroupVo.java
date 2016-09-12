package org.xson.tangyuan.cache.vo;

import java.util.List;

public class CacheGroupVo extends CacheVo {

	private List<CacheRefVo>	cacheRefList;

	public CacheGroupVo(String id, boolean defaultCache, List<CacheRefVo> cacheRefList) {
		super(id, defaultCache);
		this.cacheRefList = cacheRefList;
		this.group = true;
	}

	public void putObject(String key, Object value, Integer time, String[] ignore, String service) {
		for (CacheRefVo ref : cacheRefList) {
			CacheVo cacheVo = ref.getCacheVo();

			if (null != ignore && search(ignore, cacheVo.id)) {
				continue;
			}

			String[] include = ref.getInclude();
			if (null != include && !match(include, service)) {
				continue;
			}

			String[] exclude = ref.getExclude();
			if (null != include && match(exclude, service)) {
				continue;
			}

			cacheVo.putObject(key, value, time, ignore, service);
		}
	}

	public Object getObject(String key) {
		Object result = null;
		for (CacheRefVo ref : cacheRefList) {
			result = ref.getCacheVo().getObject(key);
			if (null != result) {
				break;
			}
		}
		return result;
	}

	public Object removeObject(String key, String[] ignore, String service) {
		Object result = null;
		for (CacheRefVo ref : cacheRefList) {
			CacheVo cacheVo = ref.getCacheVo();

			if (null != ignore && search(ignore, cacheVo.id)) {
				continue;
			}

			String[] include = ref.getInclude();
			if (null != include && !match(include, service)) {
				continue;
			}

			String[] exclude = ref.getExclude();
			if (null != include && match(exclude, service)) {
				continue;
			}

			Object _result = cacheVo.removeObject(key, ignore, service);
			if (null == result && null != _result) {
				result = _result;
			}
		}
		return result;
	}

	private boolean search(String[] array, String item) {
		for (int i = 0; i < array.length; i++) {
			if (item.equals(array[i])) {
				return true;
			}
		}
		return false;
	}

	private boolean match(String[] array, String item) {
		for (int i = 0; i < array.length; i++) {
			// TODO 正则表达式
		}
		return false;
	}

	public void start() {
	}
}
