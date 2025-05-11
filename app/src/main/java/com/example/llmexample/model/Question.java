package com.example.llmexample.model;

import org.json.JSONArray;

public class Question {
    private final String question;
    private final JSONArray options;
    private final int correctIndex;

    public Question(String question, JSONArray options, int correctIndex) {
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
    }

    public String getQuestion() { return question; }
    public JSONArray getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }

}