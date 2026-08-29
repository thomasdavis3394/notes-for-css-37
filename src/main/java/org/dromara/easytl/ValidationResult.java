package org.dromara.easytl;

/**
 * 模板验证结果
 *
 * @author gongjun
 * @since 1.0.0
 */
public class ValidationResult {

    private final boolean valid;
    private final String errorMessage;
    private final Integer errorPosition;

    private ValidationResult(boolean valid, String errorMessage, Integer errorPosition) {
        this.valid = valid;
        this.errorMessage = errorMessage;
        this.errorPosition = errorPosition;
    }

    /**
     * 创建验证通过的结果
     *
     * @return 验证通过的结果
     */
    public static ValidationResult valid() {
        return new ValidationResult(true, null, null);
    }

    /**
     * 创建验证失败的结果
     *
     * @param errorMessage 错误信息
     * @return 验证失败的结果
     */
    public static ValidationResult invalid(String errorMessage) {
        return new ValidationResult(false, errorMessage, null);
    }

    /**
     * 创建验证失败的结果（带位置信息）
     *
     * @param errorMessage 错误信息
     * @param errorPosition 错误位置
     * @return 验证失败的结果
     */
    public static ValidationResult invalid(String errorMessage, int errorPosition) {
        return new ValidationResult(false, errorMessage, errorPosition);
    }

    /**
     * 是否验证通过
     *
     * @return 如果验证通过返回 true
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * 获取错误信息
     *
     * @return 错误信息，如果验证通过则返回 null
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 获取错误位置
     *
     * @return 错误位置（字符索引），如果没有位置信息则返回 null
     */
    public Integer getErrorPosition() {
        return errorPosition;
    }
}