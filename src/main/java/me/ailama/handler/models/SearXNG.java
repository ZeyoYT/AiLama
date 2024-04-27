package me.ailama.handler.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearXNG {
    public String query;
    public String number_of_results;

    public ArrayList<SearXNGResult> results;
}
