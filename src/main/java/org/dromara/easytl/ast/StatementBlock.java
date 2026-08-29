package org.dromara.easytl.ast;

/**
 * 语句块节点基类
 * 所有语句块节点（如 if、for、switch）都继承自此类
 * 用于在解析器中识别语句块节点，以便进行特殊处理（如空白行处理）
 * 
 * @author EasyTL Team
 */
public abstract class StatementBlock extends ASTNode {
    
    /**
     * 默认构造函数，位置信息为未知
     */
    protected StatementBlock() {
        super();
    }
    
    /**
     * 带位置信息的构造函数
     * 
     * @param position 位置信息
     */
    protected StatementBlock(Position position) {
        super(position);
    }
}
