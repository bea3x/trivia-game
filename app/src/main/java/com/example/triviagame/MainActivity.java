package com.example.triviagame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Animation.AnimationListener {
    /**
     * database variables
     */
    private List<Questions> questionsList;
    private List<Questions> questionsEasy, questionsAvg, questionsDiff;
    private Questions currentQuestion;
    private int qnCounter;

    /**
     * layout views
     * @param savedInstanceState
     */
    TextView txt_question, timer,score;
    Button btn_choice1, btn_choice2, btn_choice3, btn_choice4;  //options
    ImageView lifeline_switch, lifeline_askHost, lifeline_fifty50, btn_walkAway; //lifelines
    ImageView btn_leaveGame;   //exit with current money button(walk away)

    /**
     * hidden views
     * @param savedInstanceState
     */
    RelativeLayout infoBox;
    TextView infoMsg;
    Button btn_yes, btn_no;

    /**
     * money popup dialog box
     * @param savedInstanceState
     */
    Dialog moneyPopup;
    String[] moneyTree = new String[12];
    int moneyTreeCount, correctAnsCount, money;

    /**
     * local variables
     * @param savedInstanceState
     */
    String btn_selected;
    int btn_chosen; //identifier of btn_selected
    boolean isCorrect, gameOver; // checkAnswer variable
    NumberFormat moneyFormat;


    boolean reallyExit = false; // popup variable

    /**
     * drawable resources
     * @param savedInstanceState
     */
    Drawable bgClicked,bgUnclicked,bgCorrect,bgIncorrect;

    //extra to be passed to save score
    public final static String EXTRA_SCORE = "com.example.triviagame.EXTRA_SCORE";

    //countdown
    private static final long COUNTDOWN_IN_MS = 60000;
    CountDownTimer countDownTimer;
    long timeLeft;

    //package name
    public static String PACKAGE_NAME;

    //MEDIAPLAYER SFXS
    MediaPlayer sfx_btn_click, sfx_btn_whoosh;
    MediaPlayer sfx_win, sfx_lose, sfx_final_answer, million_win;
    MediaPlayer sfx_question, sfx_lets_play, theme_intro;

    HomeWatcher mHomeWatcher;
    String correctAnswerVal;

    //Anim
    Animation animBlink,animFadeIn, prizeFadeIn, animFadeOut;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //views to variables
        txt_question = findViewById(R.id.txt_question);
        timer = findViewById(R.id.timer);
        btn_choice1 = findViewById(R.id.btn_choice1);
        btn_choice2 = findViewById(R.id.btn_choice2);
        btn_choice3 = findViewById(R.id.btn_choice3);
        btn_choice4 = findViewById(R.id.btn_choice4);
        lifeline_switch = findViewById(R.id.btn_lifeline_switch);
        lifeline_askHost = findViewById(R.id.btn_lifeline_askHost);
        lifeline_fifty50 = findViewById(R.id.btn_lifeline_fifty50);
        btn_leaveGame = findViewById(R.id.btn_leave);

        PACKAGE_NAME = getApplicationContext().getPackageName();

        //ImageView
        lifeline_fifty50.setClickable(true);
        lifeline_askHost.setClickable(true);
        lifeline_switch.setClickable(true);
        btn_leaveGame.setClickable(true);

        //hidden views
        infoBox = findViewById(R.id.infoBox);
        infoMsg = findViewById(R.id.txt_infoMsg);
        btn_yes = findViewById(R.id.btn_yes);
        btn_no = findViewById(R.id.btn_no);

        //get questions from db
        getQuestionsDB();
        //getQuestions("easy");
        currentQuestion = new Questions();

        //get res values
        getRes();
        getSfx();

        //local var initializations
        qnCounter = 0;
        isCorrect = false;
        gameOver = false;
        btn_selected = "";
        correctAnswerVal = "";

        //money format (add commas to string value -> int)
        moneyFormat = NumberFormat.getInstance();
        moneyFormat.setGroupingUsed(true);

        // money tree variables
        moneyTreeCount = 1;
        correctAnsCount = 0;
        money = 0;

        moneyTree = new String[]{getString(R.string.startMoney),
                getString(R.string.round1Prize), getString(R.string.round2Prize),
                getString(R.string.round3Prize), getString(R.string.round4Prize),
                getString(R.string.round5Prize), getString(R.string.round6Prize),
                getString(R.string.round7Prize), getString(R.string.round8Prize),
                getString(R.string.round9Prize), getString(R.string.round10Prize),
                getString(R.string.round11Prize), getString(R.string.round12Prize)};

        moneyPopup = new Dialog(this);
        moneyPopup.setCanceledOnTouchOutside(false);
        showMoneyPopup();

        //animations
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        prizeFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);

        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);




        //button onClickListener(s)
        btn_choice1.setOnClickListener(this);
        btn_choice2.setOnClickListener(this);
        btn_choice3.setOnClickListener(this);
        btn_choice4.setOnClickListener(this);

        lifeline_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSFX(sfx_btn_whoosh, 500);
                showQuestion();
                //lifeline_switch.setVisibility(View.INVISIBLE);
                infoBox.setVisibility(View.INVISIBLE);
                lifeline_switch.setImageResource(R.drawable.lifelines_switch_usedmdpi);
                lifeline_switch.setClickable(false);
            }
        });

        lifeline_askHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSFX(sfx_btn_whoosh, 500);
                toastMsg("Host says: " + currentQuestion.getAnswer());
                lifeline_askHost.setImageResource(R.drawable.lifelines_askhost_usedmdpi);
                lifeline_askHost.setClickable(false);
            }
        });

        lifeline_fifty50.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFadeIn();
                playSFX(sfx_btn_whoosh, 500);
                Button choices[] = {btn_choice1, btn_choice2, btn_choice3, btn_choice4};
                int removedButtons = 0;
                for (int i = 0; i < 4; i++) {
                    String currentBtn = choices[i].getText().toString();
                    if (!currentBtn.equalsIgnoreCase(correctAnswerVal)) {
                        choices[i].setVisibility(View.INVISIBLE);
                        //objFadeIn(choices[i], animFadeOut);
                        removedButtons++;
                    }
                    if (removedButtons == 2) {
                        break;
                    }
                }

                lifeline_fifty50.setImageResource(R.drawable.lifelines_fifty50_usedmdpi);
                lifeline_fifty50.setClickable(false);
            }
        });

        btn_leaveGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSFX(sfx_btn_whoosh, 500);
                if (!reallyExit) {
                    openDialog();
                    reallyExit = true;
                } else {
                    saveScorePopup(true);
                }

            }
        });



    }

    public void openDialog() {
        NewDialog exitConfirm = new NewDialog();
        exitConfirm.show(getSupportFragmentManager(), "exit dialog");
    }

    @Override
    public void onClick(View v) {
        clearFadeIn();
        playSFX(sfx_btn_click, 300);
        switch (v.getId()) {
            case R.id.btn_choice1:
                btn_choice1.setBackground(bgClicked); //change button color
                clearSelection(btn_choice1);
                btn_selected = btn_choice1.getText().toString(); //get string of clicked btn
                btn_chosen = R.id.btn_choice1;  //identifier of clicked btn
                break;
            case R.id.btn_choice2:
                btn_choice2.setBackground(bgClicked);
                clearSelection(btn_choice2);
                btn_selected = btn_choice2.getText().toString();
                btn_chosen = R.id.btn_choice2;
                break;
            case R.id.btn_choice3:
                btn_choice3.setBackground(bgClicked);
                clearSelection(btn_choice3);
                btn_selected = btn_choice3.getText().toString();
                btn_chosen = R.id.btn_choice3;
                break;
            case R.id.btn_choice4:
                btn_choice4.setBackground(bgClicked);
                clearSelection(btn_choice4);
                btn_selected = btn_choice4.getText().toString();
                btn_chosen = R.id.btn_choice4;
                break;
            default:
                break;
        }
        confirmAnswerPopup();
    }

    /**
     * for accessing database
     * @param
     */
    private void getQuestionsDB() {
        DbAccess dbAccess = DbAccess.getInstance(this);
        dbAccess.open();
        //questionsList = dbAccess.getAllQuestions(difficulty);
        questionsEasy = dbAccess.getAllQuestions("easy");
        questionsAvg = dbAccess.getAllQuestions("average");
        questionsDiff = dbAccess.getAllQuestions("difficult");
        dbAccess.close();

        //Collections.shuffle(questionsList);
        Collections.shuffle(questionsEasy);
        Collections.shuffle(questionsAvg);
        Collections.shuffle(questionsDiff);
    }

    private void getQuestions(String difficulty) {
        if (difficulty.equalsIgnoreCase("easy")) {
            currentQuestion = questionsEasy.get(qnCounter);
        } else if (difficulty.equalsIgnoreCase("average")) {
            currentQuestion = questionsAvg.get(qnCounter);
        } else if (difficulty.equalsIgnoreCase("difficult")) {
            currentQuestion = questionsDiff.get(qnCounter);
        }
    }



    /**
     * game functions
     */


    private void showQuestion() {
        qnCounter++;
        setClickLifelines(true);
        sfx_question.start();
        sfx_question.setLooping(true);
        resetValues();
        //currentQuestion = questionsList.get(qnCounter);
        String diff = getDifficulty();
        getQuestions(diff);
        String qn = moneyTreeCount + ": " + currentQuestion.getQuestion();
        String parsedStr = qn.replaceAll("(.{70})", "$1-\n");
        txt_question.setText(parsedStr);

        String choice1 = currentQuestion.getChoice1();
        if (choice1.length() > 26) choice1 = choice1.replaceAll("(.{20})", "$1-\n");

        String choice2 = currentQuestion.getChoice2();
        if (choice2.length() > 26) choice2 = choice2.replaceAll("(.{20})", "$1-\n");

        String choice3 = currentQuestion.getChoice3();
        if (choice3.length() > 26) choice3 = choice3.replaceAll("(.{20})", "$1-\n");

        String choice4 = currentQuestion.getChoice4();
        if (choice4.length() > 26) choice4 = choice4.replaceAll("(.{20})", "$1-\n");

        correctAnswerVal = currentQuestion.getAnswer();

        if (correctAnswerVal.length() > 26) {
            correctAnswerVal = correctAnswerVal.replaceAll("(.{20})", "$1-\n");
        }

        btn_choice1.setText(choice1);
        btn_choice2.setText(choice2);
        btn_choice3.setText(choice3);
        btn_choice4.setText(choice4);

        objFadeIn(txt_question,animFadeIn);

        objFadeIn(btn_choice1,animFadeIn);
        objFadeIn(btn_choice2,animFadeIn);
        objFadeIn(btn_choice3,animFadeIn);
        objFadeIn(btn_choice4,animFadeIn);

        /**
        if (correctAnsCount > 8) {
            //getQuestions("difficult");
        } else if (correctAnsCount > 3) {
            //getQuestions("average");
        }**/
        timeLeft = COUNTDOWN_IN_MS;
    }

    private String getDifficulty() {
        if (correctAnsCount > 8) {
            return "difficult";
        } else if (correctAnsCount > 3) {
            return "average";
        } else {
            return "easy";
        }
    }

    private void updateQnCounter() {
        if (correctAnsCount == 8 || correctAnsCount == 3) {
            qnCounter = 0;
        }
    }

    private void clearFadeIn() {
        txt_question.clearAnimation();
        btn_choice1.clearAnimation();
        btn_choice2.clearAnimation();
        btn_choice3.clearAnimation();
        btn_choice4.clearAnimation();
    }


    private void checkAnswer() {
        if (btn_selected.equalsIgnoreCase(correctAnswerVal)) {
            answerIsCorrect();
        } else {
            answerIsWrong();
        }
    }

    private void answerIsCorrect() {
        updateQnCounter();
        playSFX(sfx_win, 0);
        correctAnsCount++;
        setClickLifelines(false);
        if (correctAnsCount == 12) {
            playerWins();
        }
        moneyTreeCount++;
        makeCorrectBlink();
        //Button btn_correct = findViewById(btn_chosen);
        //btn_correct.setBackground(bgCorrect);
        correctAnswerPopup();

    }

    private void setClickLifelines(boolean enabled) {
        if (enabled) {
            int unclicked[] = {R.drawable.lifelines_switchhdpi,
                    R.drawable.lifelines_fifty50hdpi,R.drawable.lifelines_askhosthdpi};
            ImageView lifelines[] = {lifeline_switch, lifeline_fifty50, lifeline_askHost};
            for (int i = 0; i < 3; i++) {
                if (lifelines[i].getDrawable().getConstantState() ==
                        getResources().getDrawable(unclicked[i]).getConstantState()) {
                    lifelines[i].setEnabled(enabled);
                    //toastMsg(String.valueOf(lifelines[i]));
                }
            }
        } else {
            //toastMsg("iam here");
            lifeline_switch.setEnabled(enabled);
            lifeline_fifty50.setEnabled(enabled);
            lifeline_askHost.setEnabled(enabled);
        }
    }
    private void playerWins() {
        sfx_question.stop();

        findCorrectButton().clearAnimation();
        playSFX(million_win, 0);
        txt_question.setText("2 MILLION GRAND PRIZE WINNER");
        makeBlink(txt_question);
        Button choices[] = {btn_choice1, btn_choice2, btn_choice3, btn_choice4};

        for (int i = 0; i < 4; i++) {
            choices[i].setVisibility(View.GONE);
        }


    }

    private void answerIsWrong() {
        playSFX(sfx_lose, 0);
        setClickLifelines(false);
        Button btn_correct = findViewById(btn_chosen);
        btn_correct.setBackground(bgIncorrect);
        makeCorrectBlink();
        wrongAnswerPopup();
    }

    private void checkPrize() {
        if (correctAnsCount == 12) {
            money = 2000000;
        } else if (correctAnsCount >= 7) {
            money = 150000;
        } else if (correctAnsCount >= 2) {
            money = 20000;
        } else {
            money = 0;
        }
    }

    private void currentMoney() {
        money = Integer.parseInt(moneyTree[correctAnsCount]);
    }

    private String getMoneyVal(int moneyInt) {

        return moneyFormat.format(moneyInt);
    }

    /**
     * reset type functions
     */

    private void resetValues() {
        clearSelection();
        btn_selected = "";
        setClickChoices(true);
        infoMsg.setVisibility(View.INVISIBLE);

        showButtons();
        reallyExit = false;
    }

    private void clearSelection(Button btnClicked) {
        Button choices[] = {btn_choice1, btn_choice2, btn_choice3, btn_choice4};

        for (int i = 0; i < 4; i++) {
            if (!btnClicked.equals(choices[i])) {
                choices[i].setBackground(bgUnclicked);
            }
        }
    }

    private void clearSelection() {
        Button choices[] = {btn_choice1, btn_choice2, btn_choice3, btn_choice4};

        for (int i = 0; i < 4; i++) choices[i].setBackground(bgUnclicked);

    }

    private void showButtons() {
        Button choices[] = {btn_choice1, btn_choice2, btn_choice3, btn_choice4};

        for (int i = 0; i < 4; i++) choices[i].setVisibility(View.VISIBLE);
    }


    private void setClickChoices(boolean enabled) {
        if (enabled) {
            btn_choice1.setEnabled(true);
            btn_choice2.setEnabled(true);
            btn_choice3.setEnabled(true);
            btn_choice4.setEnabled(true);
        } else {
            btn_choice1.setEnabled(false);
            btn_choice2.setEnabled(false);
            btn_choice3.setEnabled(false);
            btn_choice4.setEnabled(false);
        }
    }


    /**
     * infoBox popups
     */
    private void confirmAnswerPopup() {
        countDownTimer.cancel();
        btn_yes.setVisibility(View.VISIBLE);
        btn_no.setVisibility(View.VISIBLE);
        infoMsg.setVisibility(View.VISIBLE);
        infoBox.setVisibility(View.VISIBLE);
        infoMsg.setText(R.string.confirmAnsDialog);

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                playSFX(sfx_btn_click, 500);
                setClickChoices(false);
                checkAnswer();
            }
        });
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSFX(sfx_btn_click, 500);
                infoBox.setVisibility(View.INVISIBLE);
                clearSelection();
                updateCountDownText();
                startCountdown();
            }
        });
    }

    private void correctAnswerPopup() {
        currentMoney();
        if (infoMsg.getVisibility() == View.INVISIBLE || infoBox.getVisibility() == View.INVISIBLE ) {
            infoMsg.setVisibility(View.VISIBLE);
            infoBox.setVisibility(View.VISIBLE);
        }
        //infoMsgTitle.setText(R.string.winMsg);
        infoMsg.setText("P" + getMoneyVal(money));
        btn_yes.setVisibility(View.INVISIBLE);
        btn_no.setVisibility(View.INVISIBLE);

        objFadeIn(infoMsg,prizeFadeIn);

        //Handler
        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                showMoneyPopup();
            }
        };
        if (correctAnsCount == 12) {
            saveScorePopup(false);
        } else {
            handler.postDelayed(r, 2000);
        }
        //infoMsgInst.setText(R.string.continueGameDialog);


        /**
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoBox.setVisibility(View.INVISIBLE);
                findCorrectButton().clearAnimation();
                if (correctAnsCount == 12) {
                    saveScorePopup(false);
                } else {
                    showMoneyPopup();
                }
            }
        });
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!reallyExit) {
                    toastMsg("Pindutin ulit ang 'x' kung talagang ayaw mo na");
                    reallyExit = true;
                } else {
                    saveScorePopup(true);
                }
            }
        });**/
    }

    private void saveScorePopup(boolean walkAway) {
        infoBox.setVisibility(View.VISIBLE);
        infoMsg.setVisibility(View.VISIBLE);
        btn_yes.setVisibility(View.VISIBLE);
        btn_no.setVisibility(View.VISIBLE);
        infoMsg.setText(R.string.saveScoreDialog);
        clearSelection();

        if (walkAway) {
            currentMoney();
        } else {
            checkPrize();
        }

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSFX(sfx_btn_click, 500);
                saveScore(String.valueOf(money));
            }
        });
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSFX(sfx_btn_click, 500);
                backToMenu();
            }
        });
    }

    private void wrongAnswerPopup() {
        checkPrize();

        btn_yes.setVisibility(View.VISIBLE);
        btn_no.setVisibility(View.VISIBLE);
        infoBox.setVisibility(View.VISIBLE);
        //infoMsgTitle.setText(R.string.loseMsg);
        infoMsg.setText("Nanalo ka ng P" + getMoneyVal(money)
                + ".\nGusto mo bang i-save ang iyong iskor?");
        setClickChoices(false);
        //infoMsgInst.setText(R.string.saveScoreDialog);

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSFX(sfx_btn_click, 500);
                checkPrize();
                saveScore(String.valueOf(money));
            }
        });
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSFX(sfx_btn_click, 500);
                backToMenu();
            }
        });
    }

    public void showMoneyPopup() {
        playSFX(sfx_btn_whoosh, 500);
        moneyPopup.setContentView(R.layout.money_tree_popup);
        Button btn_close_popup;

        TextView roundLbl[] = new TextView[13];
        TextView roundNum[] = new TextView[13];
        LinearLayout roundDet[] = new LinearLayout[13];
        btn_close_popup = moneyPopup.findViewById(R.id.btn_nextQn);
        ImageView roundCircle[] = new ImageView[13];

        int gray = getResources().getColor(R.color.gray_ish);
        int white = getResources().getColor(R.color.white);
        int i;
        for (i = 1; i < 13; i++) {
            String txt_det = "round" + i + "Det";
            String txt_num = "round" + i + "Lbl";
            String txt_lbl = "round" + i + "Money";
            String circle = "round" + i + "Circle";
            int rId = getResources().getIdentifier(txt_det, "id", getPackageName());
            roundDet[i] = moneyPopup.findViewById(rId);
            int numId = getResources().getIdentifier(txt_num, "id", getPackageName());
            roundNum[i] = moneyPopup.findViewById(numId);
            int lblId = getResources().getIdentifier(txt_lbl, "id", getPackageName());
            roundLbl[i] = moneyPopup.findViewById((lblId));
            int defaultColor = roundLbl[i].getCurrentTextColor();
            int cId = getResources().getIdentifier(circle, "id", getPackageName());
            roundCircle[i] = moneyPopup.findViewById(cId);
            int money = Integer.parseInt(moneyTree[i]);

            if (i < moneyTreeCount) {
                if (i == 2 || i == 7) {
                    String star = "round" + i + "Star";
                    int starId = getResources().getIdentifier(star, "id", getPackageName());
                    ImageView imgStar = moneyPopup.findViewById(starId);
                    imgStar.setImageResource(R.drawable.ic_star_filled);
                }
                roundLbl[i].setTextColor(gray);
                roundNum[i].setTextColor(gray);
                roundCircle[i].setVisibility(View.VISIBLE);
            } else {
                roundLbl[i].setTextColor(defaultColor);
                roundNum[i].setTextColor(defaultColor);
            }
            if (i < 11) roundLbl[i].setText(moneyFormat.format(money));
            else roundLbl[i].setText(moneyTree[i].charAt(0) + " MILLION");
        }

        btn_close_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (findCorrectButton() != null) findCorrectButton().clearAnimation();
                moneyPopup.dismiss();
                showQuestion();
                startCountdown();
                playSFX(sfx_lets_play, 0);

                infoBox.setVisibility(View.INVISIBLE);
                infoMsg.setVisibility(View.INVISIBLE);
            }
        });

        /**
         * round details
         */
        roundDet[moneyTreeCount].setBackgroundResource(R.drawable.moneytree_outline);


        moneyPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        moneyPopup.show();
    }




    /**
     * add on functions
     * @param
     */

    private Button findCorrectButton() {
        String correctAnswer = currentQuestion.getAnswer();
        Button choices[] = {btn_choice1,btn_choice2,btn_choice3,btn_choice4};
        Button correctBtn = null;

        for (int i = 0; i < 4; i++) {
            if (choices[i].getText().toString().equalsIgnoreCase(correctAnswerVal)) {
                correctBtn = choices[i];
            }
        }
        return correctBtn;
    }

    private void makeCorrectBlink() {
        findCorrectButton().setBackground(bgCorrect);
        makeBlink(findCorrectButton());
    }

    private void makeBlink(View obj) {
        animBlink = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        obj.startAnimation(animBlink);
    }


    private void objFadeIn(View obj, Animation animation) {
        obj.startAnimation(animFadeIn);
    }

    private void toastMsg(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void getRes() {
        bgClicked = getResources().getDrawable(R.drawable.btn_clicked_shadow);
        bgUnclicked = getResources().getDrawable(R.drawable.btn_unclicked_shadow);
        bgCorrect = getResources().getDrawable(R.drawable.btn_correct_shadow);
        bgIncorrect = getResources().getDrawable(R.drawable.btn_wrong_shadow);
    }

    /**
     * timer functions
     */

    private void startCountdown() {
        countDownTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timeLeft = 0;
                updateCountDownText();
                checkAnswer();
            }
        }.start();
    }

    private void updateCountDownText() {
        int seconds = (int) (timeLeft / 1000);
        timer.setText(String.valueOf(seconds));

        if(timeLeft < 10000) {
            timer.setTextColor(Color.RED);
        } else {
            timer.setTextColor(Color.WHITE);
        }

    }

    /**
     * open new activity
     */
    private void saveScore(String final_score) {
        Intent i = new Intent(this, SaveScore.class);
        i.putExtra(EXTRA_SCORE, final_score);
        startActivity(i);
        //finish();

    }

    private void backToMenu() {
        startActivity(new Intent(this, MainMenu.class));
    }

    /**
     * musicservice ++
     */

    private void getSfx() {
        million_win = MediaPlayer.create(this, R.raw.million_win);
        sfx_btn_click = MediaPlayer.create(this, R.raw.sfx_btn_click);
        sfx_btn_whoosh = MediaPlayer.create(this, R.raw.sfx_btn_whoosh);
        sfx_final_answer = MediaPlayer.create(this, R.raw.sfx_final_answer);
        sfx_lets_play = MediaPlayer.create(this, R.raw.sfx_lets_play);
        sfx_lose = MediaPlayer.create(this, R.raw.sfx_lose);
        sfx_question = MediaPlayer.create(this, R.raw.sfx_question);
        sfx_win = MediaPlayer.create(this, R.raw.sfx_win);
        theme_intro = MediaPlayer.create(this, R.raw.theme_intro);
    }

    /**
    private void playSFX(MediaPlayer media) {
        MediaPlayer mediaFile[] = {million_win, sfx_btn_click, sfx_btn_whoosh, sfx_final_answer,
                sfx_lets_play,sfx_lets_play,sfx_lose,sfx_question,sfx_win};

        for (int i = 0; i < mediaFile.length; i++) {
            if (mediaFile[i] != media) {
                mediaFile[i].stop();
            }
        }
    }

    private void playSFX(MediaPlayer media, boolean speedUp, int adv) {
        MediaPlayer mediaFile[] = {million_win, sfx_btn_click, sfx_btn_whoosh, sfx_final_answer,
        sfx_lets_play,sfx_lets_play,sfx_lose,sfx_question,sfx_win};

        for (int i = 0; i < mediaFile.length; i++) {
            if (mediaFile[i] != media && mediaFile[i].isPlaying()) {
                mediaFile[i].stop();
            }
        }
        if (speedUp) {
            media.seekTo(adv);
        }
        media.start();

    }
        **/

    private void playSFX(MediaPlayer media, int adv) {
        media.seekTo(adv);
        media.start();
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        sfx_question.stop();
        if (million_win.isPlaying()) million_win.stop();

        //finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sfx_question.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        sfx_question.pause();
        if (million_win.isPlaying()) million_win.stop();

    }

    @Override
    public void onBackPressed() {
        if (reallyExit) {
            saveScorePopup(true);
            sfx_question.stop();
            if (million_win.isPlaying()) million_win.stop();
            //finish();
        } else {
            reallyExit = true;
            openDialog();
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        clearFadeIn();

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
