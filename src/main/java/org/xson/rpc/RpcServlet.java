package org.xson.rpc;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xson.common.object.XCO;

public class RpcServlet extends HttpServlet {

	private final static long	serialVersionUID	= 1L;

	private static Logger		logger				= LoggerFactory.getLogger(RpcServlet.class);

	private String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	private ServiceUriVo parseRequest(HttpServletRequest req) {
		logger.info("Rpc call from [" + req.getRequestURL() + "], client ip[" + getIpAddress(req) + "]");
		String uri = req.getRequestURI();
		if (null == uri) {
			throw new RpcException(RpcConfig.RPC_ERROR_CODE, "Invalid access path: " + uri);
		}
		return ServiceUriVo.parse(uri);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		XCO result = null;

		try {
			ServiceUriVo rVo = parseRequest(req);
			XCO arg = getXCOArg(req);
			result = RpcUtil.doXcoRpcRquest(rVo, arg);
		} catch (Throwable e) {
			result = RpcUtil.getExceptionResult(e);
		}

		if (null != result) {
			resp.setHeader("Content-type", "text/html;charset=UTF-8");
			resp.setCharacterEncoding("UTF-8");
			Writer write = resp.getWriter();
			write.write(result.toXMLString());
			write.close();
		}
	}

	private XCO getXCOArg(HttpServletRequest request) throws IOException {
		byte[] buffer = IOUtils.toByteArray(request.getInputStream());
		return XCO.fromXML(new String(buffer, "UTF-8"));
	}

}
