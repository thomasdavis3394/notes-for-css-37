package org.dromara.easytl.exception;

/**
 * 类型转换异常
 * <p>
 * 当 eval 方法的返回值类型转换失败时抛出此异常
 * </p>
 *
 * @author EasyTL Team
 * @since 1.0.0
 */
public class TypeConversionException extends TemplateRuntimeException {

    private static final long serialVersionUID = 1L;

    private final String sourceType;
    private final String targetType;

    /**
     * 构造函数
     *
     * @param message 异常信息
     */
    public TypeConversionException(String message) {
        super(message);
        this.sourceType = null;
        this.targetType = null;
    }

    /**
     * 构造函数，指定源类型和目标类型
     *
     * @param sourceType 源类型名称
     * @param targetType 目标类型名称
     * @param reason 失败原因
     */
    public TypeConversionException(String sourceType, String targetType, String reason) {
        super(String.format("Cannot convert %s to %s: %s", sourceType, targetType, reason));
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    /**
     * 获取源类型名称
     *
     * @return 源类型名称，可能为 null
     */
    public String getSourceType() {
        return sourceType;
    }

    /**
     * 获取目标类型名称
     *
     * @return 目标类型名称，可能为 null
     */
    public String getTargetType() {
        return targetType;
    }
}