package org.dromara.easytl.ast;

/**
 * 源代码位置信息
 * 用于记录AST节点在源代码中的位置，便于错误报告和调试
 * 
 * @author EasyTL Team
 */
public class Position {
    
    /** 起始行号（从1开始） */
    private final int startLine;
    
    /** 起始列号（从1开始） */
    private final int startColumn;
    
    /** 结束行号（从1开始） */
    private final int endLine;
    
    /** 结束列号（从1开始） */
    private final int endColumn;
    
    /**
     * 构造位置信息
     * 
     * @param startLine 起始行号
     * @param startColumn 起始列号
     * @param endLine 结束行号
     * @param endColumn 结束列号
     */
    public Position(int startLine, int startColumn, int endLine, int endColumn) {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }
    
    /**
     * 获取起始行号
     * 
     * @return 起始行号（从1开始）
     */
    public int getStartLine() {
        return startLine;
    }
    
    /**
     * 获取起始列号
     * 
     * @return 起始列号（从1开始）
     */
    public int getStartColumn() {
        return startColumn;
    }
    
    /**
     * 获取结束行号
     * 
     * @return 结束行号（从1开始）
     */
    public int getEndLine() {
        return endLine;
    }
    
    /**
     * 获取结束列号
     * 
     * @return 结束列号（从1开始）
     */
    public int getEndColumn() {
        return endColumn;
    }
    
    /**
     * 合并两个位置信息，取最小的起始位置和最大的结束位置
     * 
     * @param other 另一个位置信息
     * @return 合并后的位置信息
     */
    public Position merge(Position other) {
        if (other == null) {
            return this;
        }
        
        int newStartLine = Math.min(this.startLine, other.startLine);
        int newStartColumn = this.startLine < other.startLine ? this.startColumn :
                            (this.startLine > other.startLine ? other.startColumn :
                            Math.min(this.startColumn, other.startColumn));
        
        int newEndLine = Math.max(this.endLine, other.endLine);
        int newEndColumn = this.endLine > other.endLine ? this.endColumn :
                          (this.endLine < other.endLine ? other.endColumn :
                          Math.max(this.endColumn, other.endColumn));
        
        return new Position(newStartLine, newStartColumn, newEndLine, newEndColumn);
    }
    
    /**
     * 创建一个空位置（位置未知）
     * 
     * @return 空位置对象
     */
    public static Position unknown() {
        return new Position(0, 0, 0, 0);
    }
    
    /**
     * 判断是否为未知位置
     * 
     * @return 如果是未知位置返回true
     */
    public boolean isUnknown() {
        return startLine == 0 && startColumn == 0 && endLine == 0 && endColumn == 0;
    }
    
    @Override
    public String toString() {
        if (isUnknown()) {
            return "Position{unknown}";
        }
        if (startLine == endLine) {
            return "Position{" + startLine + ":" + startColumn + "-" + endColumn + "}";
        }
        return "Position{" + startLine + ":" + startColumn + " - " + endLine + ":" + endColumn + "}";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return startLine == position.startLine &&
               startColumn == position.startColumn &&
               endLine == position.endLine &&
               endColumn == position.endColumn;
    }
    
    @Override
    public int hashCode() {
        int result = startLine;
        result = 31 * result + startColumn;
        result = 31 * result + endLine;
        result = 31 * result + endColumn;
        return result;
    }
}

