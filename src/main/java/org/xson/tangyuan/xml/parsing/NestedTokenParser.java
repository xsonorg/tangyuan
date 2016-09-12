package org.xson.tangyuan.xml.parsing;

public class NestedTokenParser {

	private final String		openToken;
	private final String		closeToken;
	private final TokenHandler	handler;

	public NestedTokenParser(String openToken, String closeToken, TokenHandler handler) {
		this.openToken = openToken;
		this.closeToken = closeToken;
		this.handler = handler;
	}

	public String parse(String text) {
		StringBuilder builder = new StringBuilder();
		if (text != null && text.length() > 0) {
			char[] src = text.toCharArray();
			int offset = 0;
			int start = text.indexOf(openToken, offset);
			while (start > -1) {
				if (start > 0 && src[start - 1] == '\\') {
					// the variable is escaped. remove the backslash.
					builder.append(src, offset, start - 1).append(openToken);
					offset = start + openToken.length();
				} else {
					int end = text.indexOf(closeToken, start);
					if (end == -1) {
						builder.append(src, offset, src.length - offset);
						offset = src.length;
					} else {
						builder.append(src, offset, start - offset);
						offset = start + openToken.length();
						int tempOffset = parseNesting(src, offset, end);
						if (0 == tempOffset) {
							String content = new String(src, offset, end - offset);
							builder.append(handler.handleToken(content));
							offset = end + closeToken.length();
						} else {
							offset = tempOffset + closeToken.length(); //
						}
					}
				}
				start = text.indexOf(openToken, offset);
			}
			if (offset < src.length) {
				builder.append(src, offset, src.length - offset);
			}
		}
		return builder.toString();
	}

	private final char	nestedOpenToken		= '{';
	private final char	nestedCloseToken	= '}';

	/**
	 * 嵌套分析
	 */
	private int parseNesting(char[] src, int offset, int end) {
		boolean nesting = false; // 是否存在嵌套
		for (int i = offset; i < end; i++) {
			if (nestedOpenToken == src[i]) {
				nesting = true;
				break;
			}
		}
		if (nesting) {
			int start = offset;
			NestedToken nestedToken = new NestedToken();
			start = recursionParseNestedToken(src, start - 1, src.length - 1, nestedToken);
			// TODO hander NestedToken
			nestedToken.getValue(null);
			return start;
		}
		return 0;
	}

	/**
	 * {x{xxx}x} // ${user{x{xxx}x}Name{xxx}}
	 * 
	 * @param src
	 * @param start
	 * @param end
	 * @param nestedToken
	 * @return
	 */
	private int recursionParseNestedToken(char[] src, int start, int end, NestedToken nestedToken) {
		if ((end - start) < 3) { // {x}:最短的
			throw new RuntimeException("(end - start) < 3");
		}
		// 当前坐标-->'{'
		start++;
		boolean toContinue = true;
		StringBuilder sb = new StringBuilder();
		while (toContinue) {
			if (nestedOpenToken == src[start]) {
				if (sb.length() > 0) {
					nestedToken.addPart(sb.toString());
					sb = new StringBuilder();
				}
				NestedToken newNestedToken = new NestedToken();
				start = recursionParseNestedToken(src, start, end, newNestedToken);
				nestedToken.addPart(newNestedToken);
			} else if (nestedCloseToken == src[start]) {
				toContinue = false;
				if (sb.length() > 0) {
					nestedToken.addPart(sb.toString());
					sb = new StringBuilder();
				}
				break;
			} else {
				sb.append(src[start]);
			}
			if ((start + 1) > end) {
				break;
			}
			start++;
		}
		if (sb.length() > 0) {
			throw new RuntimeException("sb.length() > 0");
		}
		return start;// 当前坐标-->'}'
	}
}
