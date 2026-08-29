package org.dromara.easytl.compiler;

import org.dromara.easytl.Context;
import org.dromara.easytl.ast.*;
import org.dromara.easytl.exception.ArgumentIndexOutOfBoundsException;
import org.dromara.easytl.exception.AssertionException;
import org.dromara.easytl.exception.TemplateCompileException;
import org.dromara.easytl.exception.TemplateRuntimeException;
import org.dromara.easytl.loader.TemplateLoader;
import org.dromara.easytl.parser.Lexer;
import org.dromara.easytl.parser.Parser;
import org.dromara.easytl.parser.StringSource;
import org.dromara.easytl.parser.TokenStream;
import org.dromara.easytl.runtime.AstAwareTemplateFunction;
import org.dromara.easytl.runtime.ExportRegistry;
import org.dromara.easytl.runtime.ImportedTemplateObject;
import org.dromara.easytl.runtime.JsonPathNavigator;
import org.dromara.easytl.runtime.LambdaObject;
import org.dromara.easytl.runtime.NodeList;
import org.dromara.easytl.runtime.OperatorEvaluator;
import org.dromara.easytl.runtime.Range;
import org.dromara.easytl.runtime.ReflectionUtils;
import org.dromara.easytl.runtime.ReturnException;
import org.dromara.easytl.runtime.TemplateFunction;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Pattern;

/**
 * AST求值器
 * <p>
 * 实现访问者模式，遍历并执行AST节点
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public class Evaluator implements ASTVisitor<Object> {

    /**
     * 执行上下文
     */
    private final Context context;

    /**
     * 模板加载器（用于加载 extends 的外部模板）
     */
    private final TemplateLoader templateLoader;

    /**
     * 源代码文本（用于错误报告）
     */
    private final String sourceText;

    /**
     * 是否允许模板中的 Java 类 {@code {% import %}} 语句
     */
    private final boolean templateJavaClassImportEnabled;

    /**
     * 导入的类映射（别名 -> Class<?>）
     */
    private final Map<String, Class<?>> importsMap;

    /**
     * 过滤表达式中当前节点（{@code @}）的栈，仅 {@code [?(...)]} 求值期间非空
     */
    private final Deque<Object> currentNodeStack = new ArrayDeque<>();

    /**
     * 过滤谓词求值深度（>0 时处于宽松模式：成员/索引访问遇到 null 对象或属性不存在时返回 null 而非抛异常）
     */
    private int filterDepth = 0;

    /**
     * 构造函数
     *
     * @param context 执行上下文
     */
    public Evaluator(Context context) {
        this(context, null, null, true, null);
    }

    /**
     * 构造函数
     *
     * @param context 执行上下文
     * @param templateLoader 模板加载器
     */
    public Evaluator(Context context, TemplateLoader templateLoader) {
        this(context, templateLoader, null, true, null);
    }

    /**
     * 构造函数
     *
     * @param context 执行上下文
     * @param templateLoader 模板加载器
     * @param sourceText 源代码文本（用于错误报告）
     */
    public Evaluator(Context context, TemplateLoader templateLoader, String sourceText) {
        this(context, templateLoader, sourceText, true, null);
    }

    /**
     * 构造函数
     *
     * @param context 执行上下文
     * @param templateLoader 模板加载器
     * @param sourceText 源代码文本（用于错误报告）
     * @param templateJavaClassImportEnabled 是否允许模板中的 Java 类 import
     */
    public Evaluator(Context context, TemplateLoader templateLoader, String sourceText,
            boolean templateJavaClassImportEnabled) {
        this(context, templateLoader, sourceText, templateJavaClassImportEnabled, null);
    }

    /**
     * 构造函数
     *
     * @param context 执行上下文
     * @param templateLoader 模板加载器
     * @param sourceText 源代码文本（用于错误报告）
     * @param templateJavaClassImportEnabled 是否允许模板中的 Java 类 import
     * @param importsMap 导入的类映射（别名 -> Class<?>）
     */
    public Evaluator(Context context, TemplateLoader templateLoader, String sourceText,
            boolean templateJavaClassImportEnabled, Map<String, Class<?>> importsMap) {
        this.context = context;
        this.templateLoader = templateLoader;
        this.sourceText = sourceText;
        this.templateJavaClassImportEnabled = templateJavaClassImportEnabled;
        this.importsMap = importsMap != null ? importsMap : Collections.emptyMap();
    }

    /**
     * 获取模板源代码文本
     *
     * @return 源代码文本（可能为 null）
     */
    public String getSourceText() {
        return sourceText;
    }

    /**
     * 创建运行时异常，包含位置信息
     *
     * @param message 错误消息
     * @param node AST节点（用于获取位置信息）
     * @return 运行时异常
     */
    private TemplateRuntimeException runtimeError(String message, ASTNode node) {
        if (node != null && node.getPosition() != null && !node.getPosition().isUnknown()) {
            return new TemplateRuntimeException(message, node.getPosition(), new StringSource(sourceText));
        }
        return new TemplateRuntimeException(message);
    }

    /**
     * 创建运行时异常，使用指定位置（用于方法名等特定元素）
     *
     * @param message 错误消息
     * @param position 特定位置（如方法名位置）
     * @return 运行时异常
     */
    private TemplateRuntimeException runtimeError(String message, Position position) {
        if (position != null && !position.isUnknown()) {
            return new TemplateRuntimeException(message, position, new StringSource(sourceText));
        }
        return new TemplateRuntimeException(message);
    }

    // ==================== 字面量节点访问方法 ====================

    @Override
    public Object visitStringLiteral(StringLiteral node) {
        return node.getValue();
    }

    @Override
    public Object visitTemplateLiteral(TemplateLiteral node) {
        StringBuilder result = new StringBuilder();
        List<String> strings = node.getStrings();
        List<ASTNode> expressions = node.getExpressions();

        for (int i = 0; i < strings.size(); i++) {
            result.append(strings.get(i));
            if (i < expressions.size()) {
                Object value = expressions.get(i).accept(this);
                result.append(value != null ? value.toString() : "");
            }
        }

        return result.toString();
    }

    @Override
    public Object visitNumberLiteral(NumberLiteral node) {
        return node.getValue();
    }

    @Override
    public Object visitBooleanLiteral(BooleanLiteral node) {
        return node.getValue();
    }

    @Override
    public Object visitNullLiteral(NullLiteral node) {
        return null;
    }

    @Override
    public Object visitListLiteral(ListLiteral node) {
        List<Object> list = new ArrayList<>();
        for (ASTNode element : node.getElements()) {
            list.add(element.accept(this));
        }
        return list;
    }

    @Override
    public Object visitHashLiteral(HashLiteral node) {
        Map<Object, Object> map = new HashMap<>();
        for (Map.Entry<ASTNode, ASTNode> entry : node.getEntries()) {
            Object key = entry.getKey().accept(this);
            Object value = entry.getValue().accept(this);
            map.put(key, value);
        }
        return map;
    }

    @Override
    public Object visitRangeLiteral(RangeLiteral node) {
        Object start = node.getStart().accept(this);
        Object end = node.getEnd().accept(this);
        RangeLiteral.RangeType type = node.getType();

        // 创建Range对象
        return new Range(start, end, type);
    }

    @Override
    public Object visitRegexLiteral(RegexLiteral node) {
        String pattern = node.getPattern();
        String flags = node.getFlags();

        int patternFlags = 0;
        if (flags != null) {
            if (flags.contains("i")) {
                patternFlags |= Pattern.CASE_INSENSITIVE;
            }
            if (flags.contains("m")) {
                patternFlags |= Pattern.MULTILINE;
            }
            if (flags.contains("s")) {
                patternFlags |= Pattern.DOTALL;
            }
        }

        return Pattern.compile(pattern, patternFlags);
    }

    // ==================== 变量和访问节点访问方法 ====================

    @Override
    public Object visitIdentifier(Identifier node) {
        String name = node.getName();

        // 先查 importsMap
        Class<?> importedClass = importsMap.get(name);
        if (importedClass != null) {
            return importedClass;
        }

        // 默认参数名 $：作用域链存在变量 $ 时使用变量值（覆盖），否则等价 $0（默认）
        if ("$".equals(name)) {
            if (context.contains("$")) {
                return context.get("$");
            }
            try {
                return context.getArgumentVariableStrict(0);
            } catch (ArgumentIndexOutOfBoundsException e) {
                throw enrichArgumentException(e, node);
            }
        }

        // 再查 Context
        return context.get(name);
    }

    @Override
    public Object visitMemberAccess(MemberAccess node) {
        Object obj = node.getObject().accept(this);

        // 支持对 ImportedTemplateObject 的成员访问
        if (obj instanceof ImportedTemplateObject) {
            ImportedTemplateObject imported = (ImportedTemplateObject) obj;
            return imported.getMember(node.getMemberName());
        }

        // 如果是空安全访问且对象为null，直接返回null
        if (node.isSafeNavigation() && obj == null) {
            return null;
        }

        if (obj == null) {
            // 过滤谓词宽松模式：中间节点为 null 返回 null（按不匹配处理）
            if (filterDepth > 0) {
                return null;
            }
            // 使用属性名位置而非整个表达式位置
            Position errorPos = node.getMemberNamePosition();
            if (errorPos != null && !errorPos.isUnknown()) {
                throw new TemplateRuntimeException(
                    "Cannot access member '" + node.getMemberName() + "' of null",
                    errorPos, new StringSource(sourceText));
            }
            throw runtimeError("Cannot access member '" + node.getMemberName() + "' of null", node);
        }

        // NodeList 投影：逐元素取成员，元素为 null 或无该属性时跳过
        if (obj instanceof NodeList) {
            return projectMemberAccess((NodeList) obj, node.getMemberName());
        }

        String member = node.getMemberName();
        try {
            return ReflectionUtils.getProperty(obj, member);
        } catch (TemplateRuntimeException e) {
            // 过滤谓词宽松模式：属性不存在返回 null（按不匹配处理）
            if (filterDepth > 0) {
                return null;
            }
            // 使用属性名位置而非整个表达式位置
            Position errorPos = node.getMemberNamePosition();
            if (errorPos != null && !errorPos.isUnknown()) {
                throw new TemplateRuntimeException(e.getMessage(), errorPos, new StringSource(sourceText));
            }
            throw enrichRuntimeException(e, node);
        }
    }

    /**
     * NodeList 成员投影：逐元素取成员值，元素为 null 或无该属性（Map 无 key / Bean 无 getter）时跳过
     *
     * @param list NodeList 对象
     * @param member 成员名称
     * @return 投影结果（仍为 NodeList）
     */
    private NodeList projectMemberAccess(NodeList list, String member) {
        NodeList result = new NodeList();
        for (Object element : list) {
            if (element == null) {
                continue;
            }
            if (element instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) element;
                if (map.containsKey(member)) {
                    result.add(map.get(member));
                }
                continue;
            }
            try {
                result.add(ReflectionUtils.getProperty(element, member));
            } catch (TemplateRuntimeException e) {
                // 无该属性的元素跳过
            }
        }
        return result;
    }

    @Override
    public Object visitIndexAccess(IndexAccess node) {
        Object obj = node.getObject().accept(this);
        Object index = node.getIndex().accept(this);

        if (obj == null) {
            // 过滤谓词宽松模式：中间节点为 null 返回 null（按不匹配处理）
            if (filterDepth > 0) {
                return null;
            }
            // Null pointer error -> point to object (e.g., 'array' in {array[0]})
            throw runtimeError("Cannot access index of null", node.getObject());
        }

        // NodeList：数值索引按普通列表取元素；其他索引（如字符串 key）逐元素投影
        if (obj instanceof NodeList) {
            if (index instanceof Number) {
                try {
                    return ReflectionUtils.getIndexedValue(obj, index);
                } catch (TemplateRuntimeException e) {
                    throw enrichRuntimeException(e, node.getIndex());
                }
            }
            return projectIndexAccess((NodeList) obj, index);
        }

        try {
            return ReflectionUtils.getIndexedValue(obj, index);
        } catch (TemplateRuntimeException e) {
            // 过滤谓词宽松模式：索引/key 不存在返回 null（按不匹配处理）
            if (filterDepth > 0) {
                return null;
            }
            // Select position node based on error type
            ASTNode positionNode;
            String message = e.getMessage();
            if (message != null && message.contains("out of bounds")) {
                // Index out of bounds -> point to index (e.g., '0' in {array[0]})
                positionNode = node.getIndex();
            } else {
                // Other errors (type not supported) -> point to object
                positionNode = node.getObject();
            }
            throw enrichRuntimeException(e, positionNode);
        }
    }

    /**
     * NodeList 索引投影：逐元素按 key 取值，元素为 null 或无该 key 时跳过
     *
     * @param list NodeList 对象
     * @param index 索引（key）
     * @return 投影结果（仍为 NodeList）
     */
    private NodeList projectIndexAccess(NodeList list, Object index) {
        NodeList result = new NodeList();
        for (Object element : list) {
            if (element == null) {
                continue;
            }
            if (element instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) element;
                if (map.containsKey(index)) {
                    result.add(map.get(index));
                }
                continue;
            }
            try {
                result.add(ReflectionUtils.getIndexedValue(element, index));
            } catch (TemplateRuntimeException e) {
                // 无该索引/key 的元素跳过
            }
        }
        return result;
    }

    // ==================== JSONPath 访问节点访问方法 ====================

    @Override
    public Object visitCurrentNode(CurrentNode node) {
        if (currentNodeStack.isEmpty()) {
            throw runtimeError("'@' can only be used inside filter expressions [?(...)]", node);
        }
        return currentNodeStack.peek();
    }

    @Override
    public Object visitWildcardAccess(WildcardAccess node) {
        Object obj = node.getObject().accept(this);

        if (obj == null) {
            throw runtimeError("Cannot apply wildcard to null", node.getObject());
        }
        if (isScalarValue(obj)) {
            throw runtimeError("Cannot apply wildcard to " + obj.getClass().getName(), node.getObject());
        }

        // Map → 全部 value；List/数组 → 全部元素；Bean → 全部属性值
        NodeList result = new NodeList();
        result.addAll(JsonPathNavigator.childrenOf(obj));
        return result;
    }

    @Override
    public Object visitSliceAccess(SliceAccess node) {
        Object obj = node.getObject().accept(this);

        if (obj == null) {
            throw runtimeError("Cannot apply slice to null", node.getObject());
        }

        List<Object> source = toSliceSource(obj);
        if (source == null) {
            throw runtimeError("Cannot apply slice to " + obj.getClass().getName(), node.getObject());
        }

        Integer start = node.getStart() != null ? toSliceIndex(node.getStart().accept(this), node.getStart()) : null;
        Integer end = node.getEnd() != null ? toSliceIndex(node.getEnd().accept(this), node.getEnd()) : null;
        Integer step = node.getStep() != null ? toSliceIndex(node.getStep().accept(this), node.getStep()) : null;
        return sliceElements(source, start, end, step);
    }

    /**
     * 将 List/数组转换为切片源列表；其他类型返回 null
     */
    private List<Object> toSliceSource(Object obj) {
        if (obj instanceof List) {
            return new ArrayList<>((List<?>) obj);
        }
        if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            List<Object> source = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                source.add(Array.get(obj, i));
            }
            return source;
        }
        return null;
    }

    /**
     * 按 RFC 9535 语义计算切片：start 含、end 不含，均可省略；负数从末尾计数；越界自动夹取；
     * step 省略时默认为 1，负数表示反向取值（此时 start 默认为末元素、end 默认为首元素之前），
     * step 为 0 时结果为空
     *
     * @param source 源列表
     * @param start 起始下标（含），可为 null
     * @param end 结束下标（不含），可为 null
     * @param step 步长，可为 null
     * @return 切片结果
     */
    private NodeList sliceElements(List<Object> source, Integer start, Integer end, Integer step) {
        int length = source.size();
        int stepValue = step != null ? step : 1;
        NodeList result = new NodeList();
        if (stepValue == 0) {
            return result;
        }
        if (stepValue > 0) {
            int from = start != null ? normalizeSliceIndex(start, length) : 0;
            int to = end != null ? normalizeSliceIndex(end, length) : length;
            from = Math.max(0, Math.min(from, length));
            to = Math.max(0, Math.min(to, length));
            for (int i = from; i < to; i += stepValue) {
                result.add(source.get(i));
            }
        } else {
            int from = start != null ? normalizeSliceIndex(start, length) : length - 1;
            int to = end != null ? normalizeSliceIndex(end, length) : -length - 1;
            from = Math.max(-1, Math.min(from, length - 1));
            to = Math.max(-1, Math.min(to, length - 1));
            for (int i = from; i > to; i += stepValue) {
                result.add(source.get(i));
            }
        }
        return result;
    }

    /**
     * 归一化切片下标：负数从末尾计数
     */
    private int normalizeSliceIndex(int index, int length) {
        return index < 0 ? index + length : index;
    }

    @Override
    public Object visitFilterAccess(FilterAccess node) {
        Object obj = node.getObject().accept(this);

        if (obj == null) {
            throw runtimeError("Cannot apply filter to null", node.getObject());
        }

        List<Object> source = toFilterSource(obj);
        if (source == null) {
            throw runtimeError("Cannot apply filter to " + obj.getClass().getName(), node.getObject());
        }
        return filterElements(source, node.getPredicate());
    }

    /**
     * 将 List/数组/Map 转换为过滤源列表（Map 取全部 value）；其他类型返回 null
     */
    private List<Object> toFilterSource(Object obj) {
        if (obj instanceof List) {
            return new ArrayList<>((List<?>) obj);
        }
        if (obj instanceof Map) {
            return new ArrayList<>(((Map<?, ?>) obj).values());
        }
        if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            List<Object> source = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                source.add(Array.get(obj, i));
            }
            return source;
        }
        return null;
    }

    /**
     * 对源列表逐元素应用过滤谓词：{@code @} 引用当前元素；
     * 谓词求值期间处于宽松模式，元素求值失败按不匹配处理
     *
     * @param source 源列表
     * @param predicate 谓词表达式
     * @return 匹配元素组成的 NodeList
     */
    private NodeList filterElements(List<Object> source, ASTNode predicate) {
        NodeList result = new NodeList();
        for (Object element : source) {
            currentNodeStack.push(element);
            filterDepth++;
            try {
                Object predicateResult = predicate.accept(this);
                if (toBoolean(predicateResult)) {
                    result.add(element);
                }
            } catch (TemplateRuntimeException e) {
                // 谓词对该元素求值失败（如缺失属性参与比较），按不匹配处理
            } finally {
                filterDepth--;
                currentNodeStack.pop();
            }
        }
        return result;
    }

    @Override
    public Object visitUnionAccess(UnionAccess node) {
        Object obj = node.getObject().accept(this);

        if (obj == null) {
            throw runtimeError("Cannot apply union to null", node.getObject());
        }

        NodeList result = new NodeList();
        for (JsonPathSelector selector : node.getSelectors()) {
            applySelector(obj, selector, result);
        }
        return result;
    }

    @Override
    public Object visitRecursiveDescentAccess(RecursiveDescentAccess node) {
        Object obj = node.getObject().accept(this);
        if (node.getMemberName() != null) {
            return JsonPathNavigator.descendantValues(obj, node.getMemberName());
        }
        // 选择器形式（..* / ..[...]）：对后代树中的每个节点（含自身）依次应用选择器
        NodeList result = new NodeList();
        for (Object descendant : JsonPathNavigator.descendantNodes(obj)) {
            for (JsonPathSelector selector : node.getSelectors()) {
                applySelector(descendant, selector, result);
            }
        }
        return result;
    }

    /**
     * 在目标对象上应用单个 JSONPath 选择器（宽松语义：类型不适用、键不存在、
     * 下标越界等一律静默跳过，不抛异常）
     *
     * @param target 目标对象
     * @param selector 选择器
     * @param result 结果收集列表
     */
    private void applySelector(Object target, JsonPathSelector selector, NodeList result) {
        if (target == null) {
            return;
        }
        if (selector instanceof JsonPathSelector.WildcardSelector) {
            if (!isScalarValue(target)) {
                result.addAll(JsonPathNavigator.childrenOf(target));
            }
            return;
        }
        if (selector instanceof JsonPathSelector.IndexSelector) {
            Object key = ((JsonPathSelector.IndexSelector) selector).getExpression().accept(this);
            if (target instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) target;
                if (map.containsKey(key)) {
                    result.add(map.get(key));
                }
            } else if (target instanceof List || target.getClass().isArray()) {
                if (key instanceof Number) {
                    try {
                        result.add(ReflectionUtils.getIndexedValue(target, key));
                    } catch (TemplateRuntimeException e) {
                        // 下标越界，跳过
                    }
                }
            } else if (!isScalarValue(target) && key instanceof String) {
                // Bean：字符串键按属性名取值
                try {
                    result.add(ReflectionUtils.getProperty(target, (String) key));
                } catch (TemplateRuntimeException e) {
                    // 无该属性，跳过
                }
            }
            return;
        }
        if (selector instanceof JsonPathSelector.SliceSelector) {
            List<Object> source = toSliceSource(target);
            if (source == null) {
                return;
            }
            JsonPathSelector.SliceSelector slice = (JsonPathSelector.SliceSelector) selector;
            Integer start = slice.getStart() != null ? toSliceIndex(slice.getStart().accept(this), slice.getStart()) : null;
            Integer end = slice.getEnd() != null ? toSliceIndex(slice.getEnd().accept(this), slice.getEnd()) : null;
            Integer step = slice.getStep() != null ? toSliceIndex(slice.getStep().accept(this), slice.getStep()) : null;
            result.addAll(sliceElements(source, start, end, step));
            return;
        }
        if (selector instanceof JsonPathSelector.FilterSelector) {
            List<Object> source = toFilterSource(target);
            if (source == null) {
                return;
            }
            result.addAll(filterElements(source, ((JsonPathSelector.FilterSelector) selector).getPredicate()));
        }
    }

    /**
     * 判断是否为标量（JSONPath 通配不支持的叶子类型）
     */
    private boolean isScalarValue(Object obj) {
        return obj instanceof String
            || obj instanceof Number
            || obj instanceof Boolean
            || obj instanceof Character
            || obj.getClass().isEnum();
    }

    /**
     * 将切片下标表达式结果转换为 int
     *
     * @param value 下标表达式求值结果
     * @param node 下标表达式节点（用于错误报告）
     * @return int 下标
     */
    private int toSliceIndex(Object value, ASTNode node) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw runtimeError("Slice index must be a number, but got "
                + (value == null ? "null" : value.getClass().getName()), node);
    }

    @Override
    public Object visitArgumentAccess(ArgumentAccess node) {
        int index = node.getIndex();
        try {
            return context.getArgumentStrict(index);
        } catch (ArgumentIndexOutOfBoundsException e) {
            throw enrichArgumentException(e, node);
        }
    }

    @Override
    public Object visitArgumentVariable(ArgumentVariable node) {
        int index = node.getIndex();
        try {
            return context.getArgumentVariableStrict(index);
        } catch (ArgumentIndexOutOfBoundsException e) {
            throw enrichArgumentException(e, node);
        }
    }

    /**
     * 为参数异常添加位置信息
     *
     * @param e 原始异常
     * @param node AST节点
     * @return 包含位置信息的异常
     */
    private ArgumentIndexOutOfBoundsException enrichArgumentException(
            ArgumentIndexOutOfBoundsException e, ASTNode node) {
        if (node != null && node.getPosition() != null && !node.getPosition().isUnknown()) {
            return new ArgumentIndexOutOfBoundsException(
                    e.getIndex(), e.getArrayLength(), node.getPosition(), new StringSource(sourceText));
        }
        return e;
    }

    /**
     * Enrich a TemplateRuntimeException with position information from AST node
     */
    private TemplateRuntimeException enrichRuntimeException(
            TemplateRuntimeException e, ASTNode node) {
        if (node != null && node.getPosition() != null && !node.getPosition().isUnknown()) {
            return new TemplateRuntimeException(
                    e.getMessage(), node.getPosition(), new StringSource(sourceText));
        }
        return e;
    }

    /**
     * Enrich a TemplateRuntimeException with specific position (for method calls, etc.)
     */
    private TemplateRuntimeException enrichRuntimeException(
            TemplateRuntimeException e, Position position) {
        if (position != null && !position.isUnknown()) {
            return new TemplateRuntimeException(
                    e.getMessage(), position, new StringSource(sourceText));
        }
        return e;
    }

    // ==================== 调用节点访问方法 ====================

    @Override
    public Object visitMethodCall(MethodCall node) {
        // Handle the case where object is null (function-style call like fn())
        // In this case, the method name refers to a variable that might be a LambdaObject or Class<?>
        if (node.getObject() == null) {
            String varName = node.getMethodName();
            Object varValue = context.get(varName);

            // 先查 importsMap for imported classes
            Class<?> importedClass = importsMap.get(varName);

            // If imported class found or the variable is a Class<?>, invoke constructor
            Class<?> targetClass = importedClass != null ? importedClass :
                (varValue instanceof Class ? (Class<?>) varValue : null);

            if (targetClass != null) {
                List<ASTNode> argNodes = node.getArguments();
                Object[] args = new Object[argNodes.size()];
                for (int i = 0; i < argNodes.size(); i++) {
                    args[i] = argNodes.get(i).accept(this);
                }
                try {
                    return ReflectionUtils.createInstance(targetClass, args);
                } catch (TemplateRuntimeException e) {
                    throw enrichRuntimeException(e, node);
                }
            }

            if (varValue instanceof LambdaObject) {
                // Invoke the Lambda directly
                LambdaObject lambda = (LambdaObject) varValue;
                List<ASTNode> argNodes = node.getArguments();
                Object[] args = new Object[argNodes.size()];
                for (int i = 0; i < argNodes.size(); i++) {
                    args[i] = argNodes.get(i).accept(this);
                }
                return lambda.invoke(args);
            }

            // 函数注册表：Context 注册的扩展函数 + 内置函数（RFC 9535 length/count/match/search/value）
            TemplateFunction function = context.getFunction(varName);
            if (function != null) {
                List<ASTNode> argNodes = node.getArguments();
                // AST 感知函数（如 assert）：由函数自行决定参数求值方式，可识别参数源码结构
                if (function instanceof AstAwareTemplateFunction) {
                    try {
                        return ((AstAwareTemplateFunction) function).callWithAst(this, node);
                    } catch (AssertionException e) {
                        // 保留断言异常的具体类型（位置信息已在抛出点携带）
                        throw e;
                    } catch (TemplateRuntimeException e) {
                        throw enrichRuntimeException(e, node);
                    }
                }
                List<Object> args = new ArrayList<>(argNodes.size());
                for (ASTNode argNode : argNodes) {
                    args.add(argNode.accept(this));
                }
                try {
                    return function.call(args);
                } catch (TemplateRuntimeException e) {
                    throw enrichRuntimeException(e, node);
                }
            }

            // Not a LambdaObject, Class<?> or registered function, throw error
            throw runtimeError("Cannot call '" + varName + "' as a function", node);
        }

        Object obj = node.getObject().accept(this);

        // 支持对 ImportedTemplateObject 的方法调用
        if (obj instanceof ImportedTemplateObject) {
            ImportedTemplateObject imported = (ImportedTemplateObject) obj;
            String methodName = node.getMethodName();
            List<ASTNode> argNodes = node.getArguments();
            Object[] args = new Object[argNodes.size()];
            for (int i = 0; i < argNodes.size(); i++) {
                args[i] = argNodes.get(i).accept(this);
            }
            return imported.invokeMethod(methodName, args);
        }

        if (obj == null) {
            // 使用方法名位置（不包含括号）
            Position errorPos = node.getMethodNamePosition() != null
                ? node.getMethodNamePosition()
                : node.getPosition();
            throw runtimeError("Cannot call method '" + node.getMethodName() + "' on null", errorPos);
        }

        // If obj is a Class<?>, invoke static method
        if (obj instanceof Class) {
            Class<?> clazz = (Class<?>) obj;
            String methodName = node.getMethodName();
            List<ASTNode> argNodes = node.getArguments();
            Object[] args = new Object[argNodes.size()];

            for (int i = 0; i < argNodes.size(); i++) {
                args[i] = argNodes.get(i).accept(this);
            }

            try {
                return ReflectionUtils.invokeMethod(clazz, methodName, args);
            } catch (TemplateRuntimeException e) {
                throw enrichRuntimeException(e, node);
            }
        }

        // Handle LambdaObject as the callee (e.g., fns.get(0)(5))
        if (obj instanceof LambdaObject) {
            LambdaObject lambda = (LambdaObject) obj;
            List<ASTNode> argNodes = node.getArguments();
            Object[] args = new Object[argNodes.size()];
            for (int i = 0; i < argNodes.size(); i++) {
                args[i] = argNodes.get(i).accept(this);
            }
            return lambda.invoke(args);
        }

        String methodName = node.getMethodName();
        List<ASTNode> argNodes = node.getArguments();
        Object[] args = new Object[argNodes.size()];

        for (int i = 0; i < argNodes.size(); i++) {
            args[i] = argNodes.get(i).accept(this);
        }

        try {
            return ReflectionUtils.invokeMethod(obj, methodName, args);
        } catch (TemplateRuntimeException e) {
            // 使用方法名位置（不包含括号）
            Position errorPos = node.getMethodNamePosition() != null
                ? node.getMethodNamePosition()
                : node.getPosition();
            throw enrichRuntimeException(e, errorPos);
        }
    }

    // ==================== 运算符节点访问方法 ====================

    @Override
    public Object visitBinaryExpression(BinaryExpression node) {
        BinaryExpression.Operator operator = node.getOperator();

        // 逻辑运算符需要短路求值
        if (operator == BinaryExpression.Operator.AND) {
            Object left = node.getLeft().accept(this);
            if (!toBoolean(left)) {
                return false;
            }
            Object right = node.getRight().accept(this);
            return toBoolean(right);
        } else if (operator == BinaryExpression.Operator.OR) {
            Object left = node.getLeft().accept(this);
            if (toBoolean(left)) {
                return true;
            }
            Object right = node.getRight().accept(this);
            return toBoolean(right);
        }

        // 其他运算符正常求值
        Object left = node.getLeft().accept(this);
        Object right = node.getRight().accept(this);

        // 过滤谓词宽松模式：null 参与大小比较（如缺失属性 @.price < 10）时抛异常，
        // 由 visitFilterAccess 按不匹配处理。
        // 注：compareNumbers 现有语义将 null 视为最小值（null < 10 为 true），
        // 不能改动全局比较语义，因此仅在过滤谓词内做此拦截。
        if (filterDepth > 0 && (left == null || right == null)
                && (operator == BinaryExpression.Operator.GT
                || operator == BinaryExpression.Operator.LT
                || operator == BinaryExpression.Operator.GTE
                || operator == BinaryExpression.Operator.LTE)) {
            throw new TemplateRuntimeException(
                "Cannot compare null with '" + operator.getSymbol() + "' in filter predicate");
        }

        try {
            switch (operator) {
                case ADD:
                    return OperatorEvaluator.add(left, right);
                case SUBTRACT:
                    return OperatorEvaluator.subtract(left, right);
                case MULTIPLY:
                    return OperatorEvaluator.multiply(left, right);
                case DIVIDE:
                    return OperatorEvaluator.divide(left, right);
                case MODULO:
                    return OperatorEvaluator.modulo(left, right);
                case GT:
                    return OperatorEvaluator.greaterThan(left, right);
                case LT:
                    return OperatorEvaluator.lessThan(left, right);
                case GTE:
                    return OperatorEvaluator.greaterThanOrEqual(left, right);
                case LTE:
                    return OperatorEvaluator.lessThanOrEqual(left, right);
                case EQ:
                    return OperatorEvaluator.equal(left, right);
                case NEQ:
                    return OperatorEvaluator.notEqual(left, right);
                case REGEX_MATCH:
                    return OperatorEvaluator.regexMatch(left, right);
                case REGEX_NOT_MATCH:
                    return OperatorEvaluator.regexNotMatch(left, right);
                case IN:
                    return OperatorEvaluator.in(left, right);
                case NOT_IN:
                    return OperatorEvaluator.notIn(left, right);
                case SUBSETOF:
                    return OperatorEvaluator.subsetof(left, right);
                case ANYOF:
                    return OperatorEvaluator.anyof(left, right);
                case NONEOF:
                    return OperatorEvaluator.noneof(left, right);
                default:
                    throw runtimeError("Unknown binary operator: " + operator, node);
            }
        } catch (TemplateRuntimeException e) {
            throw enrichRuntimeException(e, node);
        }
    }

    @Override
    public Object visitUnaryExpression(UnaryExpression node) {
        UnaryExpression.Operator operator = node.getOperator();
        Object operand = node.getOperand().accept(this);

        try {
            switch (operator) {
                case NOT:
                    return !toBoolean(operand);
                case NEGATE:
                    return OperatorEvaluator.negate(operand);
                case PLUS:
                    return operand;
                default:
                    throw runtimeError("Unknown unary operator: " + operator, node);
            }
        } catch (TemplateRuntimeException e) {
            throw enrichRuntimeException(e, node);
        }
    }

    @Override
    public Object visitTernaryExpression(TernaryExpression node) {
        Object condition = node.getCondition().accept(this);
        if (toBoolean(condition)) {
            return node.getTrueValue().accept(this);
        } else {
            return node.getFalseValue().accept(this);
        }
    }

    @Override
    public Object visitElvisExpression(ElvisExpression node) {
        // Special handling for ArgumentVariable combined with ?? operator
        // When value is null, should return right side, but out-of-bounds error should still be thrown
        // 默认参数名 $（Identifier "$"）与 ArgumentVariable($0) 语义一致，走相同特判
        if (node.getLeft() instanceof ArgumentVariable || isDollarIdentifier(node.getLeft())) {
            try {
                Object left = node.getLeft().accept(this);
                if (left != null) {
                    return left;
                }
                return node.getRight().accept(this);
            } catch (ArgumentIndexOutOfBoundsException e) {
                // Only catch null value error, out-of-bounds error should still be thrown
                if (e.getMessage() != null && e.getMessage().contains("is null")) {
                    return node.getRight().accept(this);
                }
                throw e;
            }
        }

        // Standard Elvis behavior
        Object left = node.getLeft().accept(this);
        if (left != null) {
            return left;
        }
        return node.getRight().accept(this);
    }

    /**
     * 判断节点是否为默认参数名 $ 的标识符
     *
     * @param node AST节点
     * @return 如果是名为 $ 的 Identifier 返回 true
     */
    private boolean isDollarIdentifier(ASTNode node) {
        return node instanceof Identifier && "$".equals(((Identifier) node).getName());
    }

    @Override
    public Object visitIfExpression(IfExpression node) {
        // 求值条件
        Object condition = node.getCondition().accept(this);

        if (toBoolean(condition)) {
            // 执行then分支
            return executeStatementBlock(node.getThenStatements());
        }

        // 检查else if分支
        if (node.getElseIfClauses() != null) {
            for (IfExpression.ElseIfClause branch : node.getElseIfClauses()) {
                Object elseIfCondition = branch.getCondition().accept(this);
                if (toBoolean(elseIfCondition)) {
                    return executeStatementBlock(branch.getStatements());
                }
            }
        }

        // 执行else分支
        if (node.getElseStatements() != null) {
            return executeStatementBlock(node.getElseStatements());
        }

        return null;
    }

    @Override
    public Object visitForExpression(ForExpression node) {
        // 求值集合表达式
        Object collection = node.getIterable().accept(this);

        if (collection == null) {
            throw runtimeError("Cannot iterate over null", node);
        }

        // 转换为可迭代对象
        Iterable<?> iterable = toIterable(collection, node);

        // 创建循环作用域
        Context loopContext = context.createChildContext();

        String valueName = node.getVariableName();
        String indexName = node.getIndexName();

        int index = 0;
        for (Object item : iterable) {
            // 设置循环变量
            if (indexName != null) {
                loopContext.put(indexName, index);
            }
            loopContext.put(valueName, item);

            // 执行循环体
            Evaluator loopEvaluator = new Evaluator(loopContext, templateLoader, sourceText, templateJavaClassImportEnabled, importsMap);
            for (ASTNode statement : node.getBodyStatements()) {
                statement.accept(loopEvaluator);
            }

            index++;
        }

        // for表达式返回null
        return null;
    }

    @Override
    public Object visitCastExpression(CastExpression node) {
        String typeName = node.getTypeName();
        Object value = node.getOperand().accept(this);

        // 如果值为 null，返回 null
        if (value == null) {
            return null;
        }

        // 执行类型转换
        try {
            return castValue(typeName, value, node);
        } catch (TemplateRuntimeException e) {
            throw enrichRuntimeException(e, node);
        }
    }

    /**
     * 执行类型转换
     *
     * @param typeName 目标类型名
     * @param value    要转换的值
     * @param node     AST 节点（用于错误报告）
     * @return 转换后的值
     */
    private Object castValue(String typeName, Object value, CastExpression node) {
        switch (typeName) {
            case "int":
            case "Integer":
                return castToInt(value);
            case "long":
            case "Long":
                return castToLong(value);
            case "double":
            case "Double":
                return castToDouble(value);
            case "float":
            case "Float":
                return castToFloat(value);
            case "short":
            case "Short":
                return castToShort(value);
            case "byte":
            case "Byte":
                return castToByte(value);
            case "char":
            case "Character":
                return castToChar(value);
            case "boolean":
            case "Boolean":
                return castToBoolean(value);
            case "String":
                return value.toString();
            case "BigDecimal":
                return castToBigDecimal(value);
            case "BigInteger":
                return castToBigInteger(value);
            default:
                // 查找导入的类
                Class<?> importedClass = importsMap.get(typeName);
                if (importedClass != null) {
                    return castToCustomType(importedClass, value, node);
                }
                throw runtimeError("Cannot cast to unknown type '" + typeName + "'", node);
        }
    }

    private Integer castToInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new TemplateRuntimeException(
                "Cannot cast '" + value + "' to int: " + e.getMessage());
        }
    }

    private Long castToLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1L : 0L;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            throw new TemplateRuntimeException(
                "Cannot cast '" + value + "' to long: " + e.getMessage());
        }
    }

    private Double castToDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1.0 : 0.0;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            throw new TemplateRuntimeException(
                "Cannot cast '" + value + "' to double: " + e.getMessage());
        }
    }

    private Float castToFloat(Object value) {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1.0f : 0.0f;
        }
        try {
            return Float.parseFloat(value.toString());
        } catch (NumberFormatException e) {
            throw new TemplateRuntimeException(
                "Cannot cast '" + value + "' to float: " + e.getMessage());
        }
    }

    private Short castToShort(Object value) {
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? (short) 1 : (short) 0;
        }
        try {
            return Short.parseShort(value.toString());
        } catch (NumberFormatException e) {
            throw new TemplateRuntimeException(
                "Cannot cast '" + value + "' to short: " + e.getMessage());
        }
    }

    private Byte castToByte(Object value) {
        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? (byte) 1 : (byte) 0;
        }
        try {
            return Byte.parseByte(value.toString());
        } catch (NumberFormatException e) {
            throw new TemplateRuntimeException(
                "Cannot cast '" + value + "' to byte: " + e.getMessage());
        }
    }

    private Character castToChar(Object value) {
        if (value instanceof Character) {
            return (Character) value;
        }
        if (value instanceof Number) {
            return (char) ((Number) value).intValue();
        }
        String str = value.toString();
        if (str.length() == 1) {
            return str.charAt(0);
        }
        throw new TemplateRuntimeException(
            "Cannot cast '" + value + "' to char: string length must be 1");
    }

    private Boolean castToBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        }
        return Boolean.valueOf(value.toString());
    }

    private java.math.BigDecimal castToBigDecimal(Object value) {
        if (value instanceof java.math.BigDecimal) {
            return (java.math.BigDecimal) value;
        }
        if (value instanceof java.math.BigInteger) {
            return new java.math.BigDecimal((java.math.BigInteger) value);
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? java.math.BigDecimal.ONE : java.math.BigDecimal.ZERO;
        }
        try {
            return new java.math.BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            throw new TemplateRuntimeException(
                "Cannot cast '" + value + "' to BigDecimal: " + e.getMessage());
        }
    }

    private java.math.BigInteger castToBigInteger(Object value) {
        if (value instanceof java.math.BigInteger) {
            return (java.math.BigInteger) value;
        }
        if (value instanceof java.math.BigDecimal) {
            return ((java.math.BigDecimal) value).toBigInteger();
        }
        if (value instanceof Number) {
            return java.math.BigInteger.valueOf(((Number) value).longValue());
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? java.math.BigInteger.ONE : java.math.BigInteger.ZERO;
        }
        try {
            return new java.math.BigInteger(value.toString());
        } catch (NumberFormatException e) {
            throw new TemplateRuntimeException(
                "Cannot cast '" + value + "' to BigInteger: " + e.getMessage());
        }
    }

    private Object castToCustomType(Class<?> targetClass, Object value, CastExpression node) {
        // 如果值已经是目标类型，直接返回
        if (targetClass.isInstance(value)) {
            return value;
        }

        // 尝试通过反射构造
        try {
            return ReflectionUtils.createInstance(targetClass, new Object[]{value});
        } catch (TemplateRuntimeException e) {
            throw new TemplateRuntimeException(
                "Cannot cast '" + value + "' to " + targetClass.getName() + ": " + e.getMessage());
        }
    }

    // ==================== 模板节点访问方法 ====================

    @Override
    public Object visitTemplateNode(TemplateNode node) {
        StringBuilder result = new StringBuilder();
        List<ASTNode> children = node.getChildren();

        for (ASTNode child : children) {
            Object value = child.accept(this);
            if (value != null) {
                result.append(value.toString());
            }
        }

        return result.toString();
    }

    @Override
    public Object visitTextNode(TextNode node) {
        return node.getText();
    }

    // ==================== 表达式节点访问方法 ====================

    @Override
    public Object visitSingleBraceExpressionNode(SingleBraceExpressionNode node) {
        Object value = node.getExpression().accept(this);
        return value != null ? value.toString() : "";
    }

    @Override
    public Object visitDoubleBraceExpressionNode(DoubleBraceExpressionNode node) {
        Object value = node.getExpression().accept(this);
        return value != null ? value.toString() : "";
    }

    @Override
    public Object visitDollarBraceExpressionNode(DollarBraceExpressionNode node) {
        Object value = node.getExpression().accept(this);
        return value != null ? value.toString() : "";
    }

    // ==================== 语句块节点访问方法 ====================

    @Override
    public Object visitIfStatement(IfStatement node) {
        StringBuilder result = new StringBuilder();

        // 求值条件
        Object condition = node.getCondition().accept(this);

        if (toBoolean(condition)) {
            // 执行then分支
            for (ASTNode child : node.getThenBody()) {
                Object value = child.accept(this);
                if (value != null) {
                    result.append(value.toString());
                }
            }
        } else {
            // 检查else if分支
            boolean executed = false;
            if (node.getElseIfClauses() != null) {
                for (IfStatement.ElseIfClause branch : node.getElseIfClauses()) {
                    Object elseIfCondition = branch.getCondition().accept(this);
                    if (toBoolean(elseIfCondition)) {
                        for (ASTNode child : branch.getBody()) {
                            Object value = child.accept(this);
                            if (value != null) {
                                result.append(value.toString());
                            }
                        }
                        executed = true;
                        break;
                    }
                }
            }

            // 执行else分支
            if (!executed && node.getElseBody() != null) {
                for (ASTNode child : node.getElseBody()) {
                    Object value = child.accept(this);
                    if (value != null) {
                        result.append(value.toString());
                    }
                }
            }
        }

        return result.toString();
    }

    @Override
    public Object visitForStatement(ForStatement node) {
        StringBuilder result = new StringBuilder();

        // 求值集合表达式
        Object collection = node.getIterable().accept(this);

        if (collection == null) {
            throw runtimeError("Cannot iterate over null", node);
        }

        // 转换为可迭代对象
        Iterable<?> iterable = toIterable(collection, node);

        // 创建循环作用域
        Context loopContext = context.createChildContext();

        String valueName = node.getItemVariable();
        String indexName = node.getIndexVariable();

        int index = 0;
        for (Object item : iterable) {
            // 设置循环变量
            if (indexName != null) {
                loopContext.put(indexName, index);
            }
            loopContext.put(valueName, item);

            // 执行循环体
            Evaluator loopEvaluator = new Evaluator(loopContext, templateLoader, sourceText, templateJavaClassImportEnabled, importsMap);
            for (ASTNode child : node.getBody()) {
                Object value = child.accept(loopEvaluator);
                if (value != null) {
                    result.append(value.toString());
                }
            }

            index++;
        }

        return result.toString();
    }

    @Override
    public Object visitSwitchStatement(SwitchStatement node) {
        StringBuilder result = new StringBuilder();

        // 求值switch表达式
        Object switchValue = node.getCondition().accept(this);

        // 查找匹配的case分支
        boolean matched = false;
        for (CaseClause caseClause : node.getCases()) {
            Object caseValue = caseClause.getValue().accept(this);
            if (OperatorEvaluator.equal(switchValue, caseValue)) {
                // 执行匹配的case分支
                for (ASTNode child : caseClause.getBody()) {
                    Object value = child.accept(this);
                    if (value != null) {
                        result.append(value.toString());
                    }
                }
                matched = true;
                break;
            }
        }

        // 如果没有匹配的case，执行default分支
        if (!matched && node.getDefaultBody() != null) {
            for (ASTNode child : node.getDefaultBody()) {
                Object value = child.accept(this);
                if (value != null) {
                    result.append(value.toString());
                }
            }
        }

        return result.toString();
    }

    @Override
    public Object visitCaseClause(CaseClause node) {
        // CaseClause通常不会被直接访问
        throw runtimeError("CaseClause should not be visited directly", node);
    }

    // ==================== 嵌入式代码块节点访问方法 ====================

    @Override
    public Object visitLetStatement(LetStatement node) {
        String varName = node.getVariableName();
        Object value = node.getValue().accept(this);
        context.put(varName, value);
        return "";
    }

    @Override
    public Object visitExtendsStatement(ExtendsStatement node) {
        if (templateLoader == null) {
            throw runtimeError("TemplateLoader is not configured. Cannot load external template: " + node.getTemplatePath(), node);
        }

        try {
            // 加载外部模板文件内容
            String templateContent = templateLoader.load(node.getTemplatePath());

            // 编译外部模板
            StringSource source = new StringSource(templateContent);
            Lexer lexer = new Lexer(source);
            TokenStream tokenStream = new TokenStream(lexer);
            Parser parser = new Parser(tokenStream);
            TemplateNode externalAst = parser.parse();

            TemplateJavaImportValidator.validate(externalAst, templateJavaClassImportEnabled, templateContent);

            // 执行外部模板（使用相同的上下文）
            Object result = externalAst.accept(new Evaluator(context, templateLoader, sourceText, templateJavaClassImportEnabled, importsMap));

            // 返回渲染结果
            return result != null ? result.toString() : "";
        } catch (TemplateCompileException e) {
            TemplateRuntimeException ex = new TemplateRuntimeException("Failed to load or compile external template: " + node.getTemplatePath() + " - " + e.getMessage(), e);
            if (node.getPosition() != null && !node.getPosition().isUnknown()) {
                ex = new TemplateRuntimeException(ex.getMessage(), node.getPosition(), new StringSource(sourceText));
            }
            throw ex;
        } catch (Exception e) {
            TemplateRuntimeException ex = new TemplateRuntimeException("Failed to render external template: " + node.getTemplatePath() + " - " + e.getMessage(), e);
            if (node.getPosition() != null && !node.getPosition().isUnknown()) {
                ex = new TemplateRuntimeException(ex.getMessage(), node.getPosition(), new StringSource(sourceText));
            }
            throw ex;
        }
    }

    @Override
    public Object visitImportStatement(ImportStatement node) {
        if (!templateJavaClassImportEnabled) {
            throw runtimeError("Template Java class import is disabled; use Context.importClass(...) instead.", node);
        }
        String className = node.getClassName();
        String alias = node.getAlias();

        try {
            Class<?> clazz = Class.forName(className);

            // 如果没有指定别名，使用类的简单名称
            if (alias == null) {
                alias = clazz.getSimpleName();
            }

            // 将 Class<?> 存入 Context
            context.put(alias, clazz);

            return null;  // import 语句不返回值
        } catch (ClassNotFoundException e) {
            throw runtimeError("Cannot import class '" + className + "': class not found", node);
        }
    }

    @Override
    public Object visitImportTemplateStatement(ImportTemplateStatement node) {
        String templatePath = node.getTemplatePath();
        String alias = node.getAlias();

        if (templateLoader == null) {
            throw runtimeError("TemplateLoader is not configured. Cannot import template: " + templatePath, node);
        }

        try {
            // 加载并编译导入的模板
            String templateContent = templateLoader.load(templatePath);
            Lexer lexer = new Lexer(new StringSource(templateContent));
            TokenStream tokens = new TokenStream(lexer);
            Parser parser = new Parser(tokens);
            TemplateNode importedAst = parser.parse();

            // 创建 ImportedTemplateObject（懒加载，不立即执行）
            ImportedTemplateObject importedTemplate = new ImportedTemplateObject(
                importedAst,
                templateLoader,
                context,
                templateContent,
                templatePath
            );

            // 存入 Context
            context.put(alias, importedTemplate);

            return null;
        } catch (TemplateRuntimeException e) {
            throw enrichRuntimeException(e, node);
        } catch (Exception e) {
            throw runtimeError("Failed to import template '" + templatePath + "': " + e.getMessage(), node);
        }
    }

    @Override
    public Object visitExportStatement(ExportStatement node) {
        ExportRegistry registry = context.getExportRegistry();
        String varName = node.getVariableName();

        // 注册 export（重复会抛出异常）
        registry.register(varName);

        // 如果有 LetStatement，先执行 let
        if (node.getLetStatement() != null) {
            node.getLetStatement().accept(this);
        }

        return "";
    }

    // ==================== Lambda 表达式节点访问方法 ====================

    @Override
    public Object visitLambdaExpression(LambdaExpression node) {
        // Capture variable values from the entire scope chain (by value, like Java)
        Map<String, Object> capturedValues = captureAllVisibleVariables(context);

        return new LambdaObject(
            node.getParameters(),
            node.getBody(),
            node.getExpressionBody(),
            node.isBlockBody(),
            capturedValues,
            context,
            sourceText,
            templateJavaClassImportEnabled,
            importsMap
        );
    }

    /**
     * Capture all visible variables from the entire scope chain.
     * This implements capture-by-value semantics for Lambda closures.
     *
     * @param ctx the current context
     * @return map of variable names to their values
     */
    private Map<String, Object> captureAllVisibleVariables(Context ctx) {
        Map<String, Object> allVariables = new HashMap<>();

        // Walk up the parent chain and capture all variables
        List<Context> scopeChain = new ArrayList<>();
        Context current = ctx;
        while (current != null) {
            scopeChain.add(current);
            current = current.getParent();
        }

        // Add variables from outermost scope first, inner scopes will overwrite
        for (int i = scopeChain.size() - 1; i >= 0; i--) {
            Context scope = scopeChain.get(i);
            Map<String, Object> scopeVars = scope.getVariables();
            for (String varName : scopeVars.keySet()) {
                allVariables.put(varName, scope.get(varName));
            }
        }

        return allVariables;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement node) {
        Object value = null;
        if (node.hasValue()) {
            value = node.getValue().accept(this);
        }
        throw new ReturnException(value);
    }

    // ==================== URL 字面量节点访问方法 ====================

    @Override
    public Object visitUrlLiteral(UrlLiteral node) {
        return new org.dromara.easytl.runtime.EtlURL(
            node.getChildren(),
            context,
            templateLoader,
            sourceText,
            templateJavaClassImportEnabled,
            importsMap
        );
    }

    @Override
    public Object visitObjectConstruction(ObjectConstruction node) {
        ASTNode targetNode = node.getTargetClass();

        // 获取或创建实例
        Object target;
        if (targetNode instanceof MethodCall) {
            // 有参构造：Person("Peter") { ... }
            target = visitMethodCall((MethodCall) targetNode);
        } else {
            // 无参构造或类引用：Person { ... }
            Object classOrInstance = targetNode.accept(this);
            if (classOrInstance instanceof Class) {
                target = ReflectionUtils.createInstance((Class<?>) classOrInstance, new Object[0]);
            } else {
                target = classOrInstance;  // 可能已经是实例
            }
        }

        if (target == null) {
            throw runtimeError("Cannot create object from null", node);
        }

        // 设置属性
        HashLiteral properties = node.getPropertiesBlock();
        if (properties != null && !properties.isEmpty()) {
            for (Map.Entry<ASTNode, ASTNode> entry : properties.getEntries()) {
                String propName = getPropertyName(entry.getKey());
                Object propValue = entry.getValue().accept(this);

                // 特殊处理：如果目标是 Map，直接 put
                if (target instanceof Map) {
                    ((Map) target).put(propName, propValue);
                } else {
                    // 普通对象：调用 setter 方法
                    try {
                        ReflectionUtils.setProperty(target, propName, propValue);
                    } catch (TemplateRuntimeException e) {
                        throw enrichRuntimeException(e, node);
                    }
                }
            }
        }

        return target;
    }

    /**
     * 从 AST 节点获取属性名
     */
    private String getPropertyName(ASTNode keyNode) {
        if (keyNode instanceof Identifier) {
            return ((Identifier) keyNode).getName();
        } else if (keyNode instanceof StringLiteral) {
            return ((StringLiteral) keyNode).getValue();
        } else {
            // 对于其他类型，求值后转字符串
            Object key = keyNode.accept(this);
            return key != null ? key.toString() : null;
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 将对象转换为布尔值
     *
     * @param value 对象
     * @return 布尔值
     */
    private boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        }
        if (value instanceof String) {
            return !((String) value).isEmpty();
        }
        if (value instanceof Collection) {
            return !((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            return !((Map<?, ?>) value).isEmpty();
        }
        return true;
    }

    /**
     * 将对象转换为可迭代对象
     *
     * @param obj 对象
     * @param node AST节点（用于错误报告）
     * @return 可迭代对象
     */
    private Iterable<?> toIterable(Object obj, ASTNode node) {
        if (obj instanceof Iterable) {
            return (Iterable<?>) obj;
        }
        if (obj instanceof Object[]) {
            return Arrays.asList((Object[]) obj);
        }
        if (obj instanceof Range) {
            return ((Range) obj).toList();
        }
        throw runtimeError("Cannot iterate over " + obj.getClass().getName(), node);
    }

    /**
     * 执行语句块并返回最后一个语句的值
     *
     * @param statements 语句列表
     * @return 最后一个语句的值
     */
    private Object executeStatementBlock(List<ASTNode> statements) {
        Object result = null;
        for (ASTNode statement : statements) {
            result = statement.accept(this);
        }
        return result;
    }

    @Override
    public Object visitIncrementExpression(IncrementExpression node) {
        Object oldValue = node.getOperand().accept(this);
        Object newValue;

        if (node.isIncrement()) {
            newValue = OperatorEvaluator.add(oldValue, 1);
        } else {
            newValue = OperatorEvaluator.subtract(oldValue, 1);
        }

        writeAssignableValue(node.getOperand(), newValue);

        return node.isPrefix() ? newValue : oldValue;
    }

    @Override
    public Object visitCompoundAssignmentExpression(CompoundAssignmentExpression node) {
        Object targetValue = node.getTarget().accept(this);
        Object rightValue = node.getValue().accept(this);
        Object newValue;

        switch (node.getOperator()) {
            case PLUS_ASSIGN:
                newValue = OperatorEvaluator.add(targetValue, rightValue);
                break;
            case MINUS_ASSIGN:
                newValue = OperatorEvaluator.subtract(targetValue, rightValue);
                break;
            case MULTIPLY_ASSIGN:
                newValue = OperatorEvaluator.multiply(targetValue, rightValue);
                break;
            case DIVIDE_ASSIGN:
                newValue = OperatorEvaluator.divide(targetValue, rightValue);
                break;
            case MODULO_ASSIGN:
                newValue = OperatorEvaluator.modulo(targetValue, rightValue);
                break;
            case BIT_AND_ASSIGN:
                newValue = OperatorEvaluator.bitwiseAnd(targetValue, rightValue);
                break;
            case BIT_OR_ASSIGN:
                newValue = OperatorEvaluator.bitwiseOr(targetValue, rightValue);
                break;
            case BIT_XOR_ASSIGN:
                newValue = OperatorEvaluator.bitwiseXor(targetValue, rightValue);
                break;
            case LEFT_SHIFT_ASSIGN:
                newValue = OperatorEvaluator.leftShift(targetValue, rightValue);
                break;
            case RIGHT_SHIFT_ASSIGN:
                newValue = OperatorEvaluator.rightShift(targetValue, rightValue);
                break;
            case UNSIGNED_RIGHT_SHIFT_ASSIGN:
                newValue = OperatorEvaluator.unsignedRightShift(targetValue, rightValue);
                break;
            default:
                throw runtimeError("Unknown compound assignment operator: " + node.getOperator(), node);
        }

        writeAssignableValue(node.getTarget(), newValue);

        return newValue;
    }

    @Override
    public Object visitClassicForStatement(ClassicForStatement node) {
        StringBuilder result = new StringBuilder();

        // 创建循环作用域
        Context loopContext = context.createChildContext();

        // 执行初始化
        if (node.getInitialization() != null) {
            Evaluator initEvaluator = new Evaluator(loopContext, templateLoader, sourceText, templateJavaClassImportEnabled, importsMap);
            Object initValue = node.getInitialization().getValue().accept(initEvaluator);
            loopContext.put(node.getInitialization().getVariableName(), initValue);
        }

        while (true) {
            // 检查条件
            if (node.getCondition() != null) {
                Evaluator condEvaluator = new Evaluator(loopContext, templateLoader, sourceText, templateJavaClassImportEnabled, importsMap);
                Object condValue = node.getCondition().accept(condEvaluator);
                if (!OperatorEvaluator.toBoolean(condValue)) {
                    break;
                }
            }

            // 执行循环体
            Evaluator loopEvaluator = new Evaluator(loopContext, templateLoader, sourceText, templateJavaClassImportEnabled, importsMap);
            for (ASTNode child : node.getBody()) {
                Object value = child.accept(loopEvaluator);
                if (value != null) {
                    result.append(value.toString());
                }
            }

            // 执行更新
            if (node.getUpdate() != null) {
                Evaluator updateEvaluator = new Evaluator(loopContext, templateLoader, sourceText, templateJavaClassImportEnabled, importsMap);
                node.getUpdate().accept(updateEvaluator);
            }
        }

        return result.toString();
    }

    /**
     * 向可赋值目标写入新值
     */
    private void writeAssignableValue(ASTNode target, Object value) {
        if (target instanceof Identifier) {
            String name = ((Identifier) target).getName();
            context.put(name, value);
        } else if (target instanceof MemberAccess) {
            MemberAccess access = (MemberAccess) target;
            Object obj = access.getObject().accept(this);
            if (obj == null) {
                throw runtimeError("Cannot set property on null", target);
            }
            ReflectionUtils.setProperty(obj, access.getMemberName(), value);
        } else if (target instanceof IndexAccess) {
            IndexAccess access = (IndexAccess) target;
            Object obj = access.getObject().accept(this);
            Object index = access.getIndex().accept(this);
            if (obj == null) {
                throw runtimeError("Cannot set index on null", target);
            }
            ReflectionUtils.setIndexedValue(obj, index, value);
        } else {
            throw runtimeError("Invalid assignment target", target);
        }
    }
}
