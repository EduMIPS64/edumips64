/* FluentJsonObject.java
 *
 * Thin, fluent-style wrapper over JSONObject.
 * (c) 2020 Andrea Spadaccini
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.edumips64.client;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONString;

/* A fluent wrapper over GWT JSONObject */
public class FluentJsonObject {
    private JSONObject json;

    public FluentJsonObject() {
        json = new JSONObject();
    }

    public FluentJsonObject (JSONObject json) {
        this.json = json;
    }

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

    public FluentJsonObject put(String key, boolean value) {
        json.put(key, JSONBoolean.getInstance(value));
        return this;
    }

    public JSONObject toJsonObject() {
        return json;
    }

    public String toString() {
        return json.toString();
    }
}
