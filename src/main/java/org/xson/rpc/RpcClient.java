package org.xson.rpc;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xson.common.object.XCO;

public class RpcClient {

	private static Logger		logger		= LoggerFactory.getLogger(RpcClient.class);

	protected static RpcBridge	jpcBridge	= null;

	private static String sendPostRequest(String url, byte[] buffer, String header) throws Throwable {
		// TODO: 默认值设置
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			URI uri = new URI(url);
			HttpPost httpost = new HttpPost(uri);
			ByteArrayEntity byteArrayEntity = new ByteArrayEntity(buffer, ContentType.create(header, "UTF-8"));
			httpost.setEntity(byteArrayEntity);
			CloseableHttpResponse response = httpclient.execute(httpost);
			try {
				int status = response.getStatusLine().getStatusCode();
				if (status != 200) {
					throw new Exception("Unexpected response status: " + status);
				}
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity, "UTF-8");
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}

	public static XCO call(String url) {
		return call(url, new XCO());
	}

	public static XCO call(String url, XCO request) {
		try {
			logger.info("client request to: " + url);
			String xml = request.toXMLString();
			logger.info("client request args: " + xml);
			// TODO: x-application/xco
			String result = null;
			if (null == jpcBridge) {
				result = sendPostRequest(url, xml.getBytes("UTF-8"), "x-application/xco");
			} else {
				URI uri = jpcBridge.inJvm(url);
				if (null != uri) {
					XCO xcoResult = (XCO) jpcBridge.inJvmCall(uri, request);
					logger.info("client response: " + xcoResult.toXMLString());
					return xcoResult;
				} else {
					result = sendPostRequest(url, xml.getBytes("UTF-8"), "x-application/xco");
				}
			}
			logger.info("client response: " + result);
			return XCO.fromXML(result);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

}
