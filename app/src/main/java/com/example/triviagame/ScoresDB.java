package com.example.triviagame;

import android.provider.BaseColumns;

public class ScoresDB {
    public static class HiScoresDB implements BaseColumns {
        public static final String TABLE_NAME_SCORES = "high_scores";
        public static final String COLUMN_PLAYER_NAME = "player_name";
        public static final String COLUMN_MONEY_WON = "score";
        public static final String COLUMN_DATE_PLAYED = "date_played";
    }

    private ScoresDB() {}
}
