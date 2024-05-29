package me.ailama.handler.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tool {

    public boolean tooled;
    public boolean rawResponse;
    public String name;

    public HashMap<String, Object> parameters;

    public String[] response;
}
