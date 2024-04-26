package me.ailama.handler.JsonBuilder;

import java.util.List;

public class JsonObject {

    private final StringBuilder jsonBuilder;

    public JsonObject() {
        jsonBuilder = new StringBuilder();
    }

    public JsonObject string(String key, String value) {
        jsonBuilder.append("\"").append(key).append("\"").append(":").append("\"").append(value).append("\"").append(",");
        return this;
    }

    public JsonObject number(String key, Number value) {
        jsonBuilder.append("\"").append(key).append("\"").append(":").append(value).append(",");
        return this;
    }

    public JsonObject bool(String key, boolean value) {
        jsonBuilder.append("\"").append(key).append("\"").append(":").append(value).append(",");
        return this;
    }

    public JsonObject add(String key, Object value) {
        if(value instanceof String) {
            string(key, (String) value);
        } else if(value instanceof Number) {
            number(key, (Number) value);
        } else if(value instanceof Boolean) {
            bool(key, (Boolean) value);
        } else if(value instanceof List) {
            array(key, (List<?>) value);
        } else if(value instanceof JsonArray) {
            array(key, (JsonArray) value);
        } else if(value instanceof JsonObject) {
            object(key, (JsonObject) value);
        } else if (value == null) {
            nullKey(key);
        }

        return this;
    }

    public JsonObject array(String key, List<?> value) {

        jsonBuilder.append("\"").append(key).append("\"").append(":").append("[");

        for(Object object : value) {
            if(object instanceof String) {
                jsonBuilder.append("\"").append(object).append("\"").append(",");
            } else {
                jsonBuilder.append(object).append(",");
            }
        }

        if(jsonBuilder.charAt(jsonBuilder.length() - 1) == ',') {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
        }

        jsonBuilder.append("]").append(",");
        return this;
    }

    public JsonObject array(String key, JsonArray array) {
        jsonBuilder.append("\"").append(key).append("\"").append(":").append(array.build()).append(",");
        return this;
    }

    public JsonObject object(String key, Object value) {
        jsonBuilder.append("\"").append(key).append("\"").append(":").append(value).append(",");
        return this;
    }

    public JsonObject object(String key, JsonObject value) {
        jsonBuilder.append("\"").append(key).append("\"").append(":").append(value.build()).append(",");
        return this;
    }

    public JsonObject nullKey(String key) {
        jsonBuilder.append("\"").append(key).append("\"").append(":").append("null").append(",");
        return this;
    }

    public String build() {
        if(jsonBuilder.charAt(jsonBuilder.length() - 1) == ',') {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
        }

        // add curly braces
        jsonBuilder.insert(0, "{").append("}");

        return jsonBuilder.toString();
    }

}
