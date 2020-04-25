package org.edumips64.client;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONString;

/* A fluent wrapper over GWT JSONObject */
public class FluentJsonObject {
    private JSONObject json = new JSONObject();

    public FluentJsonObject put(String key, double value) {
        json.put(key, new JSONNumber(value));
        return this;
    }

    public FluentJsonObject put(String key, String value) {
        json.put(key, new JSONString(value));
        return this;
    }

    public FluentJsonObject put(String key, JSONObject value) {
        json.put(key, value);
        return this;
    }

    public FluentJsonObject put(String key, JSONArray value) {
        json.put(key, value);
        return this;
    }

    public JSONObject toJsonObject() {
        return json;
    }

    public String toString() {
        return json.toString();
    }
}
