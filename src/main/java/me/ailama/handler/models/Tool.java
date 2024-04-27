package me.ailama.handler.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tool {

    public boolean tooled;
    public String name;

    public HashMap<String, Object> arguments;


    public String[] response;
}
