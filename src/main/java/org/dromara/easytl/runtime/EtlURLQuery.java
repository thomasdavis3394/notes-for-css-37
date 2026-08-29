package org.dromara.easytl.runtime;

public class EtlURLQuery {

    private final String key;
    private String value;
    private final String rawValue;

    public EtlURLQuery(String key, String value, String rawValue) {
        this.key = key;
        this.value = value;
        this.rawValue = rawValue;
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public String getRawValue() { return rawValue; }
    public void setValue(String value) { this.value = value; }

    @Override
    public String toString() {
        if (value == null) { return key; }
        return key + "=" + value;
    }
}