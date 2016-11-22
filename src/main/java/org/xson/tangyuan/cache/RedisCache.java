package org.xson.tangyuan.cache;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.xson.tangyuan.util.Resources;
import org.xson.tangyuan.util.SerializeUtil;
import org.xson.thirdparty.redis.JedisClient;

public class RedisCache extends AbstractCache {

	private JedisClient	client	= null;

	@Override
	public void start(String resource, Map<String, String> propertyMap) {
		if (null != client) {
			return;
		}
		try {
			client = JedisClient.getInstance();
			Properties properties = new Properties();
			InputStream inputStream = Resources.getResourceAsStream(resource);
			properties.load(inputStream);
			client.start(properties);
		} catch (Throwable e) {
			throw new CacheException(e);
		}
	}

	@Override
	public void stop() {
		if (null != client) {
			client.stop();
		}
	}

	@Override
	public Object getObject(Object key) {
		try {
			byte[] bytes = this.client.get(parseKey(key).getBytes(keyEncode));
			if (null != bytes) {
				return SerializeUtil.unserialize(bytes);
			}
			return null;
		} catch (Throwable e) {
			throw new CacheException(e);
		}
	}

	@Override
	public void putObject(Object key, Object value, Integer time) {
		try {
			if (null == time) {
				this.client.set(parseKey(key).getBytes(keyEncode), SerializeUtil.serialize(value));
			} else {
				// EX second ：设置键的过期时间为 second 秒。 SET key value EX second 效果等同于
				// SETEX key second value 。
				// PX millisecond ：设置键的过期时间为 millisecond 毫秒。 SET key value PX
				// millisecond 效果等同于 PSETEX key millisecond value 。
				// NX ：只在键不存在时，才对键进行设置操作。 SET key value NX 效果等同于 SETNX key value
				// XX ：只在键已经存在时，才对键进行设置操作。
				String result = this.client.set(parseKey(key).getBytes(keyEncode), SerializeUtil.serialize(value), "nx".getBytes(keyEncode),
						"ex".getBytes(keyEncode), time.intValue());
				System.out.println(result);
				if (!"OK".equalsIgnoreCase(result)) {
					this.client.set(parseKey(key).getBytes(keyEncode), SerializeUtil.serialize(value), "xx".getBytes(keyEncode),
							"ex".getBytes(keyEncode), time.intValue());
				}
			}
		} catch (Throwable e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Object removeObject(Object key) {
		Object result = getObject(key);
		if (null != result) {
			this.client.del(parseKey(key));
		}
		return result;
	}

}
