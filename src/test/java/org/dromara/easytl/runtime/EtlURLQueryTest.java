package org.dromara.easytl.runtime;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EtlURLQueryTest {

    @Test
    public void testBasicCreation() {
        EtlURLQuery query = new EtlURLQuery("key", "value", "value");
        assertEquals("key", query.getKey());
        assertEquals("value", query.getValue());
        assertEquals("value", query.getRawValue());
    }

    @Test
    public void testSetValue() {
        EtlURLQuery query = new EtlURLQuery("key", "old", "old");
        query.setValue("new");
        assertEquals("new", query.getValue());
        assertEquals("old", query.getRawValue());
    }

    @Test
    public void testToString() {
        EtlURLQuery query = new EtlURLQuery("key", "value", "value");
        assertEquals("key=value", query.toString());
    }

    @Test
    public void testNullValue() {
        EtlURLQuery query = new EtlURLQuery("key", null, null);
        assertEquals("key", query.getKey());
        assertNull(query.getValue());
    }
}