package com.example.triviagame;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DbAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase db;
    private static DbAccess instance;

    /**
     * Private constructor to aboid object creation from outside classes.
     *
     * @param context
     */
    private DbAccess(Context context) {
        this.openHelper = new DbHelper(context);
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context the Context
     * @return the instance of DabaseAccess
     */
    public static DbAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DbAccess(context);
        }
        return instance;
    }

    /**
     * Open the database connection.
     */
    public void open() {
        this.db = openHelper.getReadableDatabase();
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (db != null) {
            this.db.close();
        }
    }



    public List<Questions> getAllQuestions(String difficulty) {
        List<Questions> questionsList = new ArrayList<>();
        String query = "SELECT * FROM " + QuizDB.QuestionsDB.TABLE_NAME;
        Cursor c = db.rawQuery(query, null);

        if (c.moveToFirst()) {
            do {
                Questions question = new Questions();
                question.setQuestion(c.getString(c.getColumnIndex(QuizDB.QuestionsDB.COLUMN_QUESTION)));
                question.setChoice1(c.getString(c.getColumnIndex(QuizDB.QuestionsDB.COLUMN_CHOICE1)));
                question.setChoice2(c.getString(c.getColumnIndex(QuizDB.QuestionsDB.COLUMN_CHOICE2)));
                question.setChoice3(c.getString(c.getColumnIndex(QuizDB.QuestionsDB.COLUMN_CHOICE3)));
                question.setChoice4(c.getString(c.getColumnIndex(QuizDB.QuestionsDB.COLUMN_CHOICE4)));
                question.setAnswer(c.getString(c.getColumnIndex(QuizDB.QuestionsDB.COLUMN_ANSWER)));
                question.setDifficulty(c.getString(c.getColumnIndex(QuizDB.QuestionsDB.COLUMN_DIFFICULTY)));

                if (c.getString(c.getColumnIndex(QuizDB.QuestionsDB.COLUMN_DIFFICULTY)).equalsIgnoreCase(difficulty)) {
                    questionsList.add(question);
                }

            } while (c.moveToNext());
        }

        c.close();
        return questionsList;
    }


}
