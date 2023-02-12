package com.example.triviagame;

public class HiScores {
    private String player_name;
    private int score;
    private String date;

    public HiScores() {}

    public HiScores(String player_name, int score, String date) {
        this.player_name = player_name;
        this.score = score;
        this.date = date;
    }

    public String getPlayer_name() {
        return player_name;
    }

    public void setPlayer_name(String player_name) {
        this.player_name = player_name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
