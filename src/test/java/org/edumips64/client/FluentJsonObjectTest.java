package org.edumips64.client;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONArray;

/**
 * GWT Unit tests for FluentJsonObject.
 * Must be run in a GWT environment (GWTTestCase).
 */
public class FluentJsonObjectTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.edumips64.client.FluentJsonObjectTest";
    }

    public void testPutString() {
        FluentJsonObject f = new FluentJsonObject();
        f.put("key", "value");
        JSONObject json = f.toJsonObject();
        assertNotNull(json);
        assertTrue(json.containsKey("key"));
        JSONString str = json.get("key").isString();
        assertNotNull(str);
        assertEquals("value", str.stringValue());
    }

    public void testPutDouble() {
        FluentJsonObject f = new FluentJsonObject();
        f.put("number", 123.45);
        JSONObject json = f.toJsonObject();
        assertTrue(json.containsKey("number"));
        JSONNumber num = json.get("number").isNumber();
        assertNotNull(num);
        assertEquals(123.45, num.doubleValue(), 0.001);
    }

    public void testPutBoolean() {
        FluentJsonObject f = new FluentJsonObject();
        f.put("flag", true);
        JSONObject json = f.toJsonObject();
        assertTrue(json.containsKey("flag"));
        JSONBoolean bool = json.get("flag").isBoolean();
        assertNotNull(bool);
        assertTrue(bool.booleanValue());
    }

    public void testPutJSONObject() {
        FluentJsonObject f = new FluentJsonObject();
        JSONObject nested = new JSONObject();
        nested.put("inner", new JSONString("innerValue"));
        f.put("obj", nested);
        
        JSONObject json = f.toJsonObject();
        assertTrue(json.containsKey("obj"));
        JSONObject retrieved = json.get("obj").isObject();
        assertNotNull(retrieved);
        assertEquals("innerValue", retrieved.get("inner").isString().stringValue());
    }

    public void testPutJSONArray() {
        FluentJsonObject f = new FluentJsonObject();
        JSONArray array = new JSONArray();
        array.set(0, new JSONString("item1"));
        f.put("arr", array);
        
        JSONObject json = f.toJsonObject();
        assertTrue(json.containsKey("arr"));
        JSONArray retrieved = json.get("arr").isArray();
        assertNotNull(retrieved);
        assertEquals(1, retrieved.size());
        assertEquals("item1", retrieved.get(0).isString().stringValue());
    }

    public void testChaining() {
        FluentJsonObject f = new FluentJsonObject()
            .put("k1", "v1")
            .put("k2", 2.0)
            .put("k3", true);
            
        JSONObject json = f.toJsonObject();
        assertEquals("v1", json.get("k1").isString().stringValue());
        assertEquals(2.0, json.get("k2").isNumber().doubleValue(), 0.001);
        assertTrue(json.get("k3").isBoolean().booleanValue());
    }
}
