package org.dromara.easytl.ast;

/**
 * null字面量节点
 * 表示 null 值
 * 
 * @author EasyTL Team
 */
public class NullLiteral extends ASTNode {
    
    /** 单例实例 */
    public static final NullLiteral INSTANCE = new NullLiteral();
    
    /**
     * 私有构造函数（单例模式）
     */
    private NullLiteral() {
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitNullLiteral(this);
    }
    
    @Override
    public String toString() {
        return "NullLiteral{}";
    }
}

