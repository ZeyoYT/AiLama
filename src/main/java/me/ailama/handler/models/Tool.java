package me.ailama.handler.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.LinkedHashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tool {

    public boolean tooled;
    public String name;

    public LinkedHashMap<String, Object> parameters;

    public String[] response;
}
