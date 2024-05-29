package me.ailama.handler.JsonBuilder;

import java.util.List;

public class JsonArray {

        private final StringBuilder jsonBuilder;

        public JsonArray() {
            jsonBuilder = new StringBuilder();
        }

        public JsonArray objects(JsonObject... jsonObjects) {
            for(JsonObject jsonObject : jsonObjects) {
                jsonBuilder.append(jsonObject.build()).append(",");
            }
            return this;
        }

        public JsonArray objects(List<JsonObject> jsonObjects) {
            for(JsonObject jsonObject : jsonObjects) {
                jsonBuilder.append(jsonObject.build()).append(",");
            }
            return this;
        }

        public JsonArray addString(String value) {
            jsonBuilder.append("\"").append(value).append("\",");
            return this;
        }

        public String build() {

            if(jsonBuilder.charAt(jsonBuilder.length() - 1) == ',') {
                jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
            }

            jsonBuilder.insert(0, "[");
            jsonBuilder.append("]");

            return jsonBuilder.toString();
        }
}
