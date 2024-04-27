package me.ailama.handler.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearXNGResult {
    public String url;
    public String title;
    public String content;
    public String engine;
    public String template;
    public List<String> parsed_url;
    public List<String> engines;
    public List<Integer> positions;
    public double score;
    public String category;
}
