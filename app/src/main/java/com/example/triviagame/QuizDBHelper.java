package com.example.triviagame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.triviagame.ScoresDB.HiScoresDB;

import java.util.ArrayList;
import java.util.List;

public class QuizDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TriviaQuizScores.db";
    private static final String TAG = "QuizDBHelper";
    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase db;

    public QuizDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;

        final String SQL_CREATE_SCORESDB = "CREATE TABLE " +
                HiScoresDB.TABLE_NAME_SCORES + " ( " +
                HiScoresDB._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                HiScoresDB.COLUMN_PLAYER_NAME + " TEXT, " +
                HiScoresDB.COLUMN_MONEY_WON + " INTEGER, " +
                HiScoresDB.COLUMN_DATE_PLAYED + " TEXT " +
                ")";

        db.execSQL(SQL_CREATE_SCORESDB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + HiScoresDB.TABLE_NAME_SCORES);
        onCreate(db);
    }

    /**
     * Open the database connection.
     */
    public void open() {
        this.db = getReadableDatabase();
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (db != null) {
            this.db.close();
        }
    }


    public boolean addScore(HiScores details) {
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(HiScoresDB.COLUMN_PLAYER_NAME, details.getPlayer_name());
        cv.put(HiScoresDB.COLUMN_MONEY_WON, details.getScore());
        cv.put(HiScoresDB.COLUMN_DATE_PLAYED, details.getDate());

        Log.d(TAG, "addDetails: Adding " + details.getScore() +
                " to " + HiScoresDB.TABLE_NAME_SCORES);

        long result = db.insert(HiScoresDB.TABLE_NAME_SCORES, null, cv);

        if (result == -1) {
            return false;
        } else {
            return true;
        }

    }



    public List<HiScores> getTopScores() {
        db = this.getReadableDatabase();
        List<HiScores> scoresList = new ArrayList<>();
        String query = "SELECT * FROM " + HiScoresDB.TABLE_NAME_SCORES + " ORDER BY " +
                HiScoresDB.COLUMN_MONEY_WON + " DESC, " +
                HiScoresDB.COLUMN_DATE_PLAYED + " DESC";
        Cursor c = db.rawQuery(query, null);

        int counter = 0;

        try {
            if (c.moveToFirst()) {
                do {
                    HiScores playerDet = new HiScores();

                    playerDet.setPlayer_name(c.getString(c.getColumnIndex(HiScoresDB.COLUMN_PLAYER_NAME)));
                    playerDet.setScore(c.getInt(c.getColumnIndex(HiScoresDB.COLUMN_MONEY_WON)));
                    playerDet.setDate(c.getString(c.getColumnIndex(HiScoresDB.COLUMN_DATE_PLAYED)));

                    scoresList.add(playerDet);
                    counter++;
                } while (c.moveToNext() && counter < 5);
            } else {
                Log.d("SQL ERROR", "No record in database");
            }
        } catch (Exception e) {
            Log.d("SQL ERROR", "Error while trying to read from database");
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }

        return  scoresList;

    }



    public Cursor getScores() {
        db = this.getReadableDatabase();
        String query = "SELECT * FROM " + HiScoresDB.TABLE_NAME_SCORES + " ORDER BY " +
                HiScoresDB.COLUMN_MONEY_WON + " DESC";
        Cursor c = db.rawQuery(query, null);
        return c;

    }


}
