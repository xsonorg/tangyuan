package org.xson.tangyuan.cache.vo;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.task.AsyncTask;

public class CacheUseVo extends CacheBase {

	private Integer time;

	public CacheUseVo(CacheVo cacheVo, String key, Integer time, String[] ignore, String service) {
		super(cacheVo, ignore, service);
		this.time = time;
		// 预处理key
		parseKey(service, key);
	}

	public void putObject(final Object arg, final Object value) {
		// 异步操作
		TangYuanContainer.getInstance().addAsyncTask(new AsyncTask() {
			@Override
			public void run() {
				String key = buildKey(arg);
				cacheVo.putObject(key, value, time, ignore, service);
			}
		});
	}

	public Object getObject(Object arg) {
		String key = buildKey(arg);
		return cacheVo.getObject(key);
	}

}
