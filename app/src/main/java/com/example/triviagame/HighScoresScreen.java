package com.example.triviagame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class HighScoresScreen extends AppCompatActivity implements View.OnClickListener{
    //TextView top_scorers_list;
    QuizDBHelper scoredb;
    private HiScores playerDet;
    List<HiScores> topScoresList;
    String txt_scores;
    NumberFormat moneyFormatted;
    Button btn_back;

    HomeWatcher mHomeWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_leaderboard);
        btn_back = findViewById(R.id.btn_backtoMenu);


        moneyFormatted = NumberFormat.getInstance();

        topScoresList = new ArrayList<>();
        scoredb = new QuizDBHelper(this);
        txt_scores = "";

        playerDet = new HiScores();

        showTopScores();


        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent intent = new Intent(Leaderboard.this, MainMenu.class);
                //startActivity(intent);
                HighScoresScreen.super.finish();
            }
        });

        /**
         * MusicService
         */

        doBindService();
        Intent music = new Intent();
        music.setClass(this, MusicService.class);
        startService(music);

        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
            @Override
            public void onHomeLongPressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
        });
        mHomeWatcher.startWatch();

    }
    private String getMoneyVal(int moneyInt) {

        return moneyFormatted.format(moneyInt);
    }

    public void showTopScores() {

        /**
        int counter = 0;
        if (topScoresList.size() >= 5) counter = 5;
        else counter = topScoresList.size();
         **/

        topScoresList = scoredb.getTopScores();

        HiScores showDet = new HiScores();


        TextView scoresDet;

        for (int i = 0; i < topScoresList.size(); i++) {
            showDet = topScoresList.get(i);
            String name = "scorer" + (i+1) + "Name";
            String score = "scorer" + (i+1) + "Score";
            String date = "scorer" + (i+1) + "Date";

            int nameId = getResources().getIdentifier(name, "id", getPackageName());
            int scoreId = getResources().getIdentifier(score, "id", getPackageName());
            int dateId = getResources().getIdentifier(date, "id", getPackageName());

            scoresDet = findViewById(nameId);
            scoresDet.setText(showDet.getPlayer_name());

            scoresDet = findViewById(scoreId);
            scoresDet.setText(getMoneyVal(showDet.getScore()));

            scoresDet = findViewById(dateId);
            scoresDet.setText(showDet.getDate());

        }

        if (topScoresList.isEmpty()) toastMsg("No data found :(");
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void getAllScores() {

        /**
        Cursor c = scoredb.getScores();

        if (c.moveToFirst()) {
            do {
                playerDet.setPlayer_name(c.getString(c.getColumnIndex(ScoresDB.HiScoresDB.COLUMN_PLAYER_NAME)));
                playerDet.setScore(c.getString(c.getColumnIndex(ScoresDB.HiScoresDB.COLUMN_MONEY_WON)));

                topScoresList.add(playerDet);
            } while (c.moveToNext());
        } else {
            toastMsg("no record in database");
        }**/

    }

    private boolean mIsBound = false;
    private MusicService mServ;
    private ServiceConnection Scon =new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((MusicService.ServiceBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService(){
        bindService(new Intent(this,MusicService.class),
                Scon, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService()
    {
        if(mIsBound)
        {
            unbindService(Scon);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        doUnbindService();
        Intent music = new Intent();
        music.setClass(this,MusicService.class);
        stopService(music);
        mHomeWatcher.stopWatch();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mServ != null) {
            mServ.resumeMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        PowerManager pm = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isInteractive();
        }

        if (!isScreenOn) {
            if (mServ != null) {
                mServ.pauseMusic();
            }
        }
    }

    private void toastMsg(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {

    }
}
