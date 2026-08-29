package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

/**
 * 成员访问节点
 * 表示对象的属性或字段访问，例如：user.name
 * 支持普通访问（.）和空安全访问（?.）
 * 
 * @author EasyTL Team
 */
public class MemberAccess extends ASTNode {
    
    /** 对象表达式 */
    private final ASTNode object;
    
    /** 成员名称 */
    private final String memberName;
    
    /** 是否为空安全访问（?.） */
    private final boolean safeNavigation;

    /** 属性名位置（仅属性名部分，不包含对象和点号） */
    private final Position memberNamePosition;

    /**
     * 构造成员访问节点
     *
     * @param object 对象表达式
     * @param memberName 成员名称
     * @param safeNavigation 是否为空安全访问
     */
    public MemberAccess(ASTNode object, String memberName, boolean safeNavigation) {
        this.object = object;
        this.memberName = memberName;
        this.safeNavigation = safeNavigation;
        this.memberNamePosition = null;
    }

    /**
     * 构造成员访问节点（从 Token 获取位置信息）
     *
     * @param object 对象表达式
     * @param memberName 成员名称
     * @param safeNavigation 是否为安全导航
     * @param token Token 对象（用于获取位置信息）
     */
    public MemberAccess(ASTNode object, String memberName, boolean safeNavigation, Token token) {
        super(positionFromToken(token));
        this.object = object;
        this.memberName = memberName;
        this.safeNavigation = safeNavigation;
        this.memberNamePosition = null;
    }

    /**
     * 构造成员访问节点（从位置信息）
     *
     * @param object 对象表达式
     * @param memberName 成员名称
     * @param safeNavigation 是否为安全导航
     * @param position 位置信息
     */
    public MemberAccess(ASTNode object, String memberName, boolean safeNavigation, Position position) {
        super(position);
        this.object = object;
        this.memberName = memberName;
        this.safeNavigation = safeNavigation;
        this.memberNamePosition = null;
    }

    /**
     * 构造成员访问节点（包含属性名位置）
     *
     * @param object 对象表达式
     * @param memberName 成员名称
     * @param safeNavigation 是否为空安全访问
     * @param position 整个表达式的位置
     * @param memberNamePosition 属性名的位置
     */
    public MemberAccess(ASTNode object, String memberName, boolean safeNavigation,
                        Position position, Position memberNamePosition) {
        super(position);
        this.object = object;
        this.memberName = memberName;
        this.safeNavigation = safeNavigation;
        this.memberNamePosition = memberNamePosition;
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
     * 获取成员名称
     * 
     * @return 成员名称
     */
    public String getMemberName() {
        return memberName;
    }
    
    /**
     * 是否为空安全访问
     * 
     * @return 如果是空安全访问返回true
     */
    public boolean isSafeNavigation() {
        return safeNavigation;
    }

    /**
     * 获取属性名的位置
     *
     * @return 属性名位置，可能为 null 或 unknown
     */
    public Position getMemberNamePosition() {
        return memberNamePosition;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitMemberAccess(this);
    }
    
    @Override
    public String toString() {
        return "MemberAccess{" +
                "object=" + object +
                ", memberName='" + memberName + '\'' +
                ", safeNavigation=" + safeNavigation +
                ", memberNamePosition=" + memberNamePosition +
                '}';
    }
}

