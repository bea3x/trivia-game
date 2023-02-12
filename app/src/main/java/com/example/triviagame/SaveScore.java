package com.example.triviagame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SaveScore extends AppCompatActivity implements View.OnClickListener{
    private EditText txt_player_name;
    private TextView txt_player_score, txt_date, txt_score_words;
    private Button saveConfirm;
    QuizDBHelper scoredb;
    HiScores playerDet;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String date;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_save_score);

        scoredb = new QuizDBHelper(this);
        txt_player_name = findViewById(R.id.name);
        txt_player_score = findViewById(R.id.showScore);
        saveConfirm = findViewById(R.id.btn_saveConfirm);
        txt_date = findViewById(R.id.sysdate);
        txt_score_words = findViewById(R.id.scoreInWords);
        date = "";

        final Intent saveScore = getIntent();
        final String pScore = saveScore.getStringExtra(MainActivity.EXTRA_SCORE);
        txt_player_score.setText(pScore);
        getCurrentDate();
        txt_score_words.setText(amtToWords(pScore));


        saveConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backToMenu = new Intent(SaveScore.this, MainMenu.class);
                String player_name = txt_player_name.getText().toString();

                if (player_name.equalsIgnoreCase("")) {
                    toastMsg("Please enter a name");
                } else {
                    int finalScore = Integer.parseInt(pScore);
                    playerDet = new HiScores(player_name, finalScore, date);
                    insertData(playerDet);
                    startActivity(backToMenu);

                    //finish();
                }


            }
        });
    }

    private String amtToWords(String score) {
        String amount = "";
        if (score.equalsIgnoreCase(getString(R.string.round1Prize))) {
            amount = "Ten Thousand";
        } else if (score.equalsIgnoreCase(getString(R.string.round2Prize))) {
            amount = "Twenty Thousand";
        } else if (score.equalsIgnoreCase(getString(R.string.round3Prize))) {
            amount = "Thirty-Five Thousand";
        } else if (score.equalsIgnoreCase(getString(R.string.round4Prize))) {
            amount = "Fifty Thousand";
        } else if (score.equalsIgnoreCase(getString(R.string.round5Prize))) {
            amount = "Seventy Thousand";
        } else if (score.equalsIgnoreCase(getString(R.string.round6Prize))) {
            amount = "One Hundred Thousand";
        } else if (score.equalsIgnoreCase(getString(R.string.round7Prize))) {
            amount = "One Hundred-Fifty Thousand";
        } else if (score.equalsIgnoreCase(getString(R.string.round8Prize))) {
            amount = "Two Hundred-Fifty Thousand";
        } else if (score.equalsIgnoreCase(getString(R.string.round9Prize))) {
            amount = "Four Hundred Thousand";
        } else if (score.equalsIgnoreCase(getString(R.string.round10Prize))) {
            amount = "Six Hundred Thousand";
        } else if (score.equalsIgnoreCase(getString(R.string.round11Prize))) {
            amount = "One Million";
        } else if (score.equalsIgnoreCase(getString(R.string.round12Prize))) {
            amount = "Two Million";
        } else {
            amount = "0";
        }
        amount += " Pesos";
        return amount;
    }

    private void getCurrentDate() {
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        date = dateFormat.format(calendar.getTime());
        txt_date.setText(date);

    }

    public void insertData(HiScores details) {
        scoredb.open();
        boolean addSuccess = scoredb.addScore(details);

        if (addSuccess) {
            toastMsg("added successfully");
        } else {
            toastMsg("something went wrong");
        }
        scoredb.close();
    }

    private void toastMsg(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {

    }


    boolean reallyExit = false;
    @Override
    public void onBackPressed() {

        Intent backToMenu = new Intent(SaveScore.this, MainMenu.class);
        if (!reallyExit) {
            toastMsg("Pindutin muli ang back para ikansel ang pag-save");
            reallyExit = true;
        } else {
            startActivity(backToMenu);

            //finish();
        }
    }
}
