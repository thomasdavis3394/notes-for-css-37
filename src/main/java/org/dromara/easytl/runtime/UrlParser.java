package org.dromara.easytl.runtime;

/**
 * URL 解析器
 * 支持完整 URL、相对 URL、IPv6 地址
 *
 * @author EasyTL Team
 */
public final class UrlParser {

    private UrlParser() {
        // 工具类，禁止实例化
    }

    /**
     * 解析 URL 字符串，返回各组件
     */
    public static UrlComponents parse(String url) {
        UrlComponents components = new UrlComponents();
        if (url == null || url.isEmpty()) {
            return components;
        }

        int pos = 0;

        // 1. 解析 scheme (xxx://)
        int schemeEnd = url.indexOf("://");
        if (schemeEnd > 0) {
            components.scheme = url.substring(0, schemeEnd);
            pos = schemeEnd + 3;
        }

        // 2. 解析 authority (userInfo@host:port)
        pos = parseAuthority(url, pos, components);

        // 3. 解析 path
        pos = parsePath(url, pos, components);

        // 4. 解析 query (?xxx)
        pos = parseQuery(url, pos, components);

        // 5. 解析 ref (#xxx)
        parseRef(url, pos, components);

        return components;
    }

    private static int parseAuthority(String url, int start, UrlComponents c) {
        // 如果没有 scheme，且 URL 以 / ? # 开头，则没有 authority
        if (start == 0 && url.length() > 0) {
            char firstChar = url.charAt(0);
            if (firstChar == '/' || firstChar == '?' || firstChar == '#') {
                return 0;
            }
        }

        // 查找 authority 结束位置（path/query/ref 开始）
        int pathStart = findPathStart(url, start);
        if (pathStart == start) {
            // 没有 authority
            return start;
        }

        String authority = url.substring(start, pathStart);

        if (authority.isEmpty()) {
            return start;
        }

        // 解析 userInfo@host:port
        int atPos = authority.indexOf('@');
        if (atPos >= 0) {
            c.userInfo = authority.substring(0, atPos);
            authority = authority.substring(atPos + 1);
        }

        // 解析 host:port（支持 IPv6 [xxx]）
        if (authority.startsWith("[")) {
            // IPv6 地址
            int bracketEnd = authority.indexOf(']');
            if (bracketEnd > 0) {
                c.host = authority.substring(1, bracketEnd);
                if (bracketEnd + 1 < authority.length()
                        && authority.charAt(bracketEnd + 1) == ':') {
                    c.port = parsePort(authority.substring(bracketEnd + 2));
                }
            }
        } else {
            // 普通主机名
            int colonPos = authority.lastIndexOf(':');
            if (colonPos > 0) {
                c.host = authority.substring(0, colonPos);
                c.port = parsePort(authority.substring(colonPos + 1));
            } else {
                c.host = authority;
            }
        }

        return pathStart;
    }

    private static int findPathStart(String url, int start) {
        for (int i = start; i < url.length(); i++) {
            char ch = url.charAt(i);
            if (ch == '/' || ch == '?' || ch == '#') {
                return i;
            }
        }
        return url.length();
    }

    private static int parsePath(String url, int start, UrlComponents c) {
        if (start >= url.length()) {
            return start;
        }

        // 如果当前位置是 ? 或 #，则没有 path
        char ch = url.charAt(start);
        if (ch == '?' || ch == '#') {
            return start;
        }

        int queryStart = url.indexOf('?', start);
        int refStart = url.indexOf('#', start);
        int end = minPositive(queryStart, refStart, url.length());

        if (end > start) {
            c.path = url.substring(start, end);
        }
        return end;
    }

    private static int parseQuery(String url, int start, UrlComponents c) {
        if (start >= url.length() || url.charAt(start) != '?') {
            return start;
        }

        int refStart = url.indexOf('#', start);
        c.query = refStart > 0
                ? url.substring(start + 1, refStart)
                : url.substring(start + 1);

        return refStart > 0 ? refStart : url.length();
    }

    private static void parseRef(String url, int start, UrlComponents c) {
        if (start < url.length() && url.charAt(start) == '#') {
            c.ref = url.substring(start + 1);
        }
    }

    private static Integer parsePort(String portStr) {
        if (portStr == null || portStr.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int minPositive(int a, int b, int defaultValue) {
        int result = defaultValue;
        if (a > 0 && a < result) result = a;
        if (b > 0 && b < result) result = b;
        return result;
    }
}