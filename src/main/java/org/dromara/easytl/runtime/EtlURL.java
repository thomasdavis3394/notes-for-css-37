package org.dromara.easytl.runtime;

import org.dromara.easytl.Context;
import org.dromara.easytl.ast.ASTNode;
import org.dromara.easytl.compiler.Evaluator;
import org.dromara.easytl.loader.TemplateLoader;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * URL 运行时对象
 * 支持延迟计算和混合修改模式
 *
 * @author EasyTL Team
 */
public class EtlURL {

    // ========== 核心字段 ==========

    private final List<ASTNode> children;
    private final Context context;
    private final TemplateLoader templateLoader;
    private final String sourceText;
    private final boolean templateJavaClassImportEnabled;
    private final Map<String, Class<?>> importsMap;

    // 显式修改的属性值
    private String scheme;
    private String userInfo;
    private String host;
    private Integer port;
    private String path;
    private String query;
    private String ref;

    // 标记哪些属性被显式修改过
    private final Set<UrlComponent> modifiedComponents = EnumSet.noneOf(UrlComponent.class);

    // ========== 构造函数 ==========

    public EtlURL(List<ASTNode> children, Context context,
                  TemplateLoader templateLoader, String sourceText) {
        this(children, context, templateLoader, sourceText, true, null);
    }

    public EtlURL(List<ASTNode> children, Context context,
                  TemplateLoader templateLoader, String sourceText,
                  boolean templateJavaClassImportEnabled) {
        this(children, context, templateLoader, sourceText, templateJavaClassImportEnabled, null);
    }

    public EtlURL(List<ASTNode> children, Context context,
                  TemplateLoader templateLoader, String sourceText,
                  boolean templateJavaClassImportEnabled,
                  Map<String, Class<?>> importsMap) {
        this.children = children;
        this.context = context;
        this.templateLoader = templateLoader;
        this.sourceText = sourceText;
        this.templateJavaClassImportEnabled = templateJavaClassImportEnabled;
        this.importsMap = importsMap != null ? importsMap : Collections.emptyMap();
    }

    // ========== 属性访问 ==========

    public String getScheme() {
        if (modifiedComponents.contains(UrlComponent.SCHEME)) return scheme;
        return computeComponents().scheme;
    }

    public void setScheme(String value) {
        this.scheme = value;
        modifiedComponents.add(UrlComponent.SCHEME);
    }

    public String getUserInfo() {
        if (modifiedComponents.contains(UrlComponent.USER_INFO)) return userInfo;
        return computeComponents().userInfo;
    }

    public void setUserInfo(String value) {
        this.userInfo = value;
        modifiedComponents.add(UrlComponent.USER_INFO);
    }

    public String getHost() {
        if (modifiedComponents.contains(UrlComponent.HOST)) return host;
        return computeComponents().host;
    }

    public void setHost(String value) {
        this.host = value;
        modifiedComponents.add(UrlComponent.HOST);
    }

    public Integer getPort() {
        if (modifiedComponents.contains(UrlComponent.PORT)) return port;
        return computeComponents().port;
    }

    public void setPort(Integer value) {
        this.port = value;
        modifiedComponents.add(UrlComponent.PORT);
    }

    public String getPath() {
        if (modifiedComponents.contains(UrlComponent.PATH)) return path;
        return computeComponents().path;
    }

    public void setPath(String value) {
        this.path = value;
        modifiedComponents.add(UrlComponent.PATH);
    }

    public String getQuery() {
        if (modifiedComponents.contains(UrlComponent.QUERY)) return query;
        return computeComponents().query;
    }

    public void setQuery(String value) {
        this.query = value;
        modifiedComponents.add(UrlComponent.QUERY);
    }

    public String getRef() {
        if (modifiedComponents.contains(UrlComponent.REF)) return ref;
        return computeComponents().ref;
    }

    public void setRef(String value) {
        this.ref = value;
        modifiedComponents.add(UrlComponent.REF);
    }

    // ========== 核心计算逻辑 ==========

    private UrlComponents computeComponents() {
        String urlStr = buildUrlString();
        return UrlParser.parse(urlStr);
    }

    private String buildUrlString() {
        StringBuilder sb = new StringBuilder();
        Evaluator evaluator = new Evaluator(context, templateLoader, sourceText, templateJavaClassImportEnabled, importsMap);
        for (ASTNode child : children) {
            Object value = child.accept(evaluator);
            if (value != null) sb.append(value.toString());
        }
        return sb.toString();
    }

    // ========== Query 操作 ==========

    public String getQuery(String key) {
        String q = getQuery();
        if (q == null) return null;
        return parseQueryValue(q, key);
    }

    public List<String> getQueryValues(String key) {
        String q = getQuery();
        if (q == null) return Collections.emptyList();
        return parseQueryValues(q, key);
    }

    public void addQuery(String key, String value) {
        String q = getQuery();
        if (q == null || q.isEmpty()) {
            setQuery(key + "=" + value);
        } else {
            setQuery(q + "&" + key + "=" + value);
        }
    }

    public void setQuery(String key, String value) {
        String q = getQuery();
        if (q == null || q.isEmpty()) {
            setQuery(key + "=" + value);
            return;
        }
        setQuery(replaceQueryParam(q, key, value));
    }

    public void removeQuery(String key) {
        String q = getQuery();
        if (q == null) return;
        setQuery(removeQueryParam(q, key));
    }

    public void clearQuery() {
        setQuery((String) null);
    }

    private String parseQueryValue(String query, String key) {
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int eqPos = pair.indexOf('=');
            if (eqPos >= 0) {
                String k = pair.substring(0, eqPos);
                if (k.equals(key)) {
                    return decodeQueryValue(pair.substring(eqPos + 1));
                }
            } else {
                if (pair.equals(key)) {
                    return null;
                }
            }
        }
        return null;
    }

    private List<String> parseQueryValues(String query, String key) {
        List<String> values = new ArrayList<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int eqPos = pair.indexOf('=');
            if (eqPos >= 0) {
                String k = pair.substring(0, eqPos);
                if (k.equals(key)) {
                    values.add(decodeQueryValue(pair.substring(eqPos + 1)));
                }
            }
        }
        return values;
    }

    private String replaceQueryParam(String query, String key, String value) {
        String[] pairs = query.split("&");
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            int eqPos = pair.indexOf('=');
            if (eqPos >= 0 && pair.substring(0, eqPos).equals(key)) {
                if (!found) {
                    if (sb.length() > 0) sb.append("&");
                    sb.append(key).append("=").append(value);
                    found = true;
                }
            } else {
                if (sb.length() > 0) sb.append("&");
                sb.append(pair);
            }
        }
        if (!found) {
            if (sb.length() > 0) sb.append("&");
            sb.append(key).append("=").append(value);
        }
        return sb.toString();
    }

    private String removeQueryParam(String query, String key) {
        String[] pairs = query.split("&");
        StringBuilder sb = new StringBuilder();
        for (String pair : pairs) {
            int eqPos = pair.indexOf('=');
            String k = eqPos >= 0 ? pair.substring(0, eqPos) : pair;
            if (!k.equals(key)) {
                if (sb.length() > 0) sb.append("&");
                sb.append(pair);
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private String decodeQueryValue(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    // ========== 编码方法 ==========

    private String encodeScheme(String value) {
        return getUrlEncoder("_urlSchemeEncoder").encode(value);
    }

    private String encodeUserInfo(String value) {
        Object encoder = context == null ? null : context.getRaw("_urlUserInfoEncoder");
        if (encoder instanceof UrlEncoder) {
            return ((UrlEncoder) encoder).encode(value);
        }
        // Default: preserve ':' for username:password format
        return encodeUserInfoSafe(value);
    }

    /**
     * Encode user info value, preserving ':' character.
     */
    private String encodeUserInfoSafe(String value) {
        if (value == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == ':') {
                sb.append(c);
            } else if (needsEncodingForUserInfo(c)) {
                try {
                    String encoded = java.net.URLEncoder.encode(String.valueOf(c), "UTF-8");
                    sb.append(encoded);
                } catch (java.io.UnsupportedEncodingException e) {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Check if a character needs URL encoding for user info.
     */
    private boolean needsEncodingForUserInfo(char c) {
        // Keep alphanumeric, safe characters, and ':' for username:password
        return !((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                || c == '-' || c == '_' || c == '.' || c == '~' || c == ':');
    }

    private String encodeHost(String value) {
        return getUrlEncoder("_urlHostEncoder").encode(value);
    }

    private String encodePath(String value) {
        Object encoder = context == null ? null : context.getRaw("_urlPathEncoder");
        if (encoder instanceof UrlEncoder) {
            return ((UrlEncoder) encoder).encode(value);
        }
        // Default: preserve path structure, only encode special chars
        // URLEncoder encodes '/' which breaks paths, so we need custom handling
        return encodePathSafe(value);
    }

    /**
     * Encode path value, preserving '/' characters.
     */
    private String encodePathSafe(String value) {
        if (value == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '/') {
                sb.append(c);
            } else if (needsEncoding(c)) {
                try {
                    String encoded = java.net.URLEncoder.encode(String.valueOf(c), "UTF-8");
                    sb.append(encoded);
                } catch (java.io.UnsupportedEncodingException e) {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Check if a character needs URL encoding.
     */
    private boolean needsEncoding(char c) {
        // Keep alphanumeric and safe characters
        return !((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                || c == '-' || c == '_' || c == '.' || c == '~' || c == '/');
    }

    private String encodeQueryKey(String value) {
        return getUrlEncoder("_urlQueryKeyEncoder").encode(value);
    }

    private String encodeQueryValue(String value) {
        return getUrlEncoder("_urlQueryValueEncoder").encode(value);
    }

    private String encodeRef(String value) {
        return getUrlEncoder("_urlRefEncoder").encode(value);
    }

    private String encodeQueryString(String query) {
        if (query == null || query.isEmpty()) return query;
        StringBuilder sb = new StringBuilder();
        String[] pairs = query.split("&");
        for (int i = 0; i < pairs.length; i++) {
            if (i > 0) sb.append("&");
            int eqPos = pairs[i].indexOf('=');
            if (eqPos >= 0) {
                String key = pairs[i].substring(0, eqPos);
                String value = pairs[i].substring(eqPos + 1);
                sb.append(encodeQueryKey(key));
                sb.append("=");
                sb.append(encodeQueryValue(value));
            } else {
                sb.append(encodeQueryKey(pairs[i]));
            }
        }
        return sb.toString();
    }

    private UrlEncoder getUrlEncoder(String key) {
        if (context == null) return UrlEncoders.NOOP;
        Object encoder = context.getRaw(key);
        return encoder instanceof UrlEncoder ? (UrlEncoder) encoder : UrlEncoders.URL;
    }

    // ========== toString ==========

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        String s = getScheme();
        if (s != null) sb.append(s).append("://");

        String ui = getUserInfo();
        if (ui != null) sb.append(encodeUserInfo(ui)).append("@");

        String h = getHost();
        if (h != null) sb.append(encodeHost(h));

        Integer p = getPort();
        if (p != null) sb.append(":").append(p);

        String pt = getPath();
        if (pt != null) sb.append(encodePath(pt));

        String q = getQuery();
        if (q != null) sb.append("?").append(encodeQueryString(q));

        String r = getRef();
        if (r != null) sb.append("#").append(encodeRef(r));

        return sb.toString();
    }
}