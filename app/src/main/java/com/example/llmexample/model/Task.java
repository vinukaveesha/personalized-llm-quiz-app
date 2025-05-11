package com.example.llmexample.model;

public class Task {
    private String title;
    private String description;
    private String source;

    public Task(String title, String description, String source) {
        this.title = title;
        this.description = description;
        this.source = source;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSource() { return source; }
}