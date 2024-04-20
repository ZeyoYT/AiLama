package me.ailama.handler.other;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearXNG {
    public String query;
    public String number_of_results;

    public ArrayList<SearXNGResult> results;
}
