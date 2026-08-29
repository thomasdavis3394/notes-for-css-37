package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

import java.util.List;

/**
 * 方法调用节点
 * 表示对对象方法的调用，例如：user.getName() 或 list.size()
 * 
 * @author EasyTL Team
 */
public class MethodCall extends ASTNode {

    /** 对象表达式（可能为null，表示静态方法或全局函数） */
    private final ASTNode object;

    /** 方法名 */
    private final String methodName;

    /** 方法名位置（仅指向方法名，不包含括号） */
    private final Position methodNamePosition;

    /** 参数列表 */
    private final List<ASTNode> arguments;

    /** 是否为空安全调用（?.） */
    private final boolean safeNavigation;
    
    /**
     * 构造方法调用节点
     *
     * @param object 对象表达式
     * @param methodName 方法名
     * @param arguments 参数列表
     * @param safeNavigation 是否为空安全调用
     */
    public MethodCall(ASTNode object, String methodName, List<ASTNode> arguments, boolean safeNavigation) {
        this.object = object;
        this.methodName = methodName;
        this.methodNamePosition = null;
        this.arguments = arguments;
        this.safeNavigation = safeNavigation;
    }

    /**
     * 构造方法调用节点（从 Token 获取位置信息）
     *
     * @param object 对象表达式
     * @param methodName 方法名称
     * @param arguments 参数列表
     * @param safeNavigation 是否为安全导航
     * @param token Token 对象（用于获取位置信息）
     */
    public MethodCall(ASTNode object, String methodName, List<ASTNode> arguments, boolean safeNavigation, Token token) {
        super(positionFromToken(token));
        this.object = object;
        this.methodName = methodName;
        this.methodNamePosition = positionFromToken(token);
        this.arguments = arguments;
        this.safeNavigation = safeNavigation;
    }

    /**
     * 构造方法调用节点（从位置信息）
     *
     * @param object 对象表达式
     * @param methodName 方法名称
     * @param arguments 参数列表
     * @param safeNavigation 是否为安全导航
     * @param position 位置信息（整个方法调用位置）
     * @param methodNamePosition 方法名位置（仅方法名，不含括号）
     */
    public MethodCall(ASTNode object, String methodName, List<ASTNode> arguments, boolean safeNavigation, Position position, Position methodNamePosition) {
        super(position);
        this.object = object;
        this.methodName = methodName;
        this.methodNamePosition = methodNamePosition;
        this.arguments = arguments;
        this.safeNavigation = safeNavigation;
    }

    /**
     * 构造方法调用节点（位置信息与方法名位置相同）
     *
     * @param object 对象表达式
     * @param methodName 方法名称
     * @param arguments 参数列表
     * @param safeNavigation 是否为安全导航
     * @param position 位置信息
     */
    public MethodCall(ASTNode object, String methodName, List<ASTNode> arguments, boolean safeNavigation, Position position) {
        super(position);
        this.object = object;
        this.methodName = methodName;
        this.methodNamePosition = position;
        this.arguments = arguments;
        this.safeNavigation = safeNavigation;
    }
    
    /**
     * 获取对象表达式
     * 
     * @return 对象表达式
     */
    public ASTNode getObject() {
        return object;
    }
    
    /**
     * 获取方法名
     *
     * @return 方法名
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * 获取方法名位置（仅指向方法名，不包含括号）
     *
     * @return 方法名位置，可能为 null
     */
    public Position getMethodNamePosition() {
        return methodNamePosition;
    }

    /**
     * 获取参数列表
     * 
     * @return 参数列表
     */
    public List<ASTNode> getArguments() {
        return arguments;
    }
    
    /**
     * 是否为空安全调用
     * 
     * @return 如果是空安全调用返回true
     */
    public boolean isSafeNavigation() {
        return safeNavigation;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitMethodCall(this);
    }
    
    @Override
    public String toString() {
        return "MethodCall{" +
                "object=" + object +
                ", methodName='" + methodName + '\'' +
                ", arguments=" + arguments +
                ", safeNavigation=" + safeNavigation +
                '}';
    }
}

