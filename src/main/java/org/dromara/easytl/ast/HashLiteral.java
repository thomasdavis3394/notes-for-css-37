package org.dromara.easytl.ast;

import org.dromara.easytl.parser.Token;

import java.util.List;
import java.util.AbstractMap;
import java.util.Map;

/**
 * 哈希表字面量节点
 * 表示花括号包裹的键值对集合，例如：{name: "张三", age: 25}
 * 支持标识符键、字符串键、数字键，支持嵌套哈希表
 * 
 * @author EasyTL Team
 */
public class HashLiteral extends ASTNode {
    
    /** 键值对列表 */
    private final List<Map.Entry<ASTNode, ASTNode>> entries;
    
    /**
     * 构造哈希表字面量节点
     * 
     * @param entries 键值对列表
     */
    public HashLiteral(List<Map.Entry<ASTNode, ASTNode>> entries) {
        this.entries = entries;
    }
    
    /**
     * 构造哈希表字面量节点（带位置信息）
     *
     * @param entries 键值对列表
     * @param position 位置信息
     */
    public HashLiteral(List<Map.Entry<ASTNode, ASTNode>> entries, Position position) {
        super(position);
        this.entries = entries;
    }

    /**
     * 构造哈希表字面量节点（从 Token 范围获取位置信息）
     *
     * @param entries 键值对列表
     * @param startToken 起始 Token
     * @param endToken 结束 Token
     */
    public HashLiteral(List<Map.Entry<ASTNode, ASTNode>> entries, Token startToken, Token endToken) {
        super(positionFromTokens(startToken, endToken));
        this.entries = entries;
    }
    
    /**
     * 获取键值对列表
     * 
     * @return 键值对列表
     */
    public List<Map.Entry<ASTNode, ASTNode>> getEntries() {
        return entries;
    }
    
    /**
     * 判断是否为空哈希表
     * 
     * @return 如果为空则返回true
     */
    public boolean isEmpty() {
        return entries == null || entries.isEmpty();
    }
    
    /**
     * 创建键值对
     * 
     * @param key 键节点
     * @param value 值节点
     * @return 键值对
     */
    public static Map.Entry<ASTNode, ASTNode> entry(ASTNode key, ASTNode value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitHashLiteral(this);
    }
    
    @Override
    public String toString() {
        return "HashLiteral{" + "entries=" + entries + '}';
    }
}




