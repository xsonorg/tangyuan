package org.xson.tangyuan.cache.vo;

import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.task.AsyncTask;

public class CacheCleanVo extends CacheBase {

	public CacheCleanVo(CacheVo cacheVo, String key, String[] ignore, String service) {
		super(cacheVo, ignore, service);
		parseKey(service, key);
	}

	public void removeObject(final Object arg) {
		// 异步操作
		TangYuanContainer.getInstance().addAsyncTask(new AsyncTask() {
			@Override
			public void run() {
				String key = buildKey(arg);
				cacheVo.removeObject(key, ignore, service);
			}
		});
	}

}
