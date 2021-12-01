package co.edu.unal.tictactoe;

import java.lang.Thread.*;

import androidx.appcompat.app.AppCompatActivity;

import android.app.*;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AndroidTicTacToeActivity extends AppCompatActivity {

    // Representa el estado interno del juego
    private TicTacToeGame mGame;
    // Turno al iniciar
    private boolean mStart = true;
    //Botones que conforman el tablero
    private Button mBoardButtons[];
    //Textos variados mostrados
    private TextView mInfoTextView;
    private TextView mStatsTiesNumber;
    private TextView mStatsHumanWinsNumber;
    private TextView mStatsComputerWinsNumber;
    //Indicador de que se acaba el juego
    private boolean mGameOver = false;
    //Indicador del turno
    private boolean mHumanTurn = true;
    //Juego nuevo automático
    private boolean autoNewGame = false;
    //Delays
    private int autoNewGameDelay = 5000;
    private int computerMoveDelay = 1000;
    // Conteo stats: Ties / Human Wins / Computer Wins
    private int[] stats = {0,0,0};
    //Dialogos
    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;
    static final int DIALOG_AUTO_NEW_GAME = 2;
    // Tablero
    private BoardView mBoardView;
    //Sonidos
    MediaPlayer mHumanMP;
    MediaPlayer mComputerMP;
    MediaPlayer mComputerWinsMP;
    MediaPlayer mHumanWinsMP;
    MediaPlayer mTieMP;
    MediaPlayer mBonkMP;
    //Preferencias compartidas
    private SharedPreferences mPrefs;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharArray("board", mGame.getBoardState());
        outState.putBoolean("mStart",mStart);
        outState.putBoolean("mGameOver", mGameOver);
        outState.putBoolean("mHumanTurn", mHumanTurn);
        outState.putBoolean("autoNewGame", autoNewGame);
        outState.putInt("mHumanWins", stats[1]);
        outState.putInt("mComputerWins", stats[2]);
        outState.putInt("mTies", stats[0]);
        outState.putCharSequence("info", mInfoTextView.getText());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //Si hay un estado en el juego, recuperarlo
        mGame.setBoardState(savedInstanceState.getCharArray("board"));
        mGameOver = savedInstanceState.getBoolean("mGameOver");
        mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
        mStart = savedInstanceState.getBoolean("mStart");
        stats[1] = savedInstanceState.getInt("mHumanWins");
        stats[2] = savedInstanceState.getInt("mComputerWins");
        stats[0] = savedInstanceState.getInt("mTies");
        mHumanTurn = savedInstanceState.getBoolean("mHumanTurn", mHumanTurn);
        autoNewGame = savedInstanceState.getBoolean("autoNewGame", autoNewGame);

        if(!mHumanTurn){ //Si es el turno del computador
            computerMakeMove(computerMoveDelay);
        }

        if(mGameOver){ //Si ya se acabó el juego
            checkAutoNewGame(autoNewGameDelay);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Juego
        mGame = new TicTacToeGame();
        mBoardView = (BoardView) findViewById(R.id.board);
        mBoardView.setGame(mGame);
        mBoardView.setOnTouchListener(mTouchListener);
        //Informacion
        mInfoTextView = findViewById(R.id.information);
        //Stats
        mStatsTiesNumber = findViewById(R.id.TiesNumber);
        mStatsHumanWinsNumber = findViewById(R.id.HumanWinsNumber);
        mStatsComputerWinsNumber = findViewById(R.id.ComputerWinsNumber);
        if (savedInstanceState == null) startNewGame();

        mPrefs = getSharedPreferences("triqui_prefs", MODE_PRIVATE);

        stats[1] = mPrefs.getInt("mHumanWins", 0);
        stats[2] = mPrefs.getInt("mComputerWins", 0);
        stats[0] = mPrefs.getInt("mTies", 0);
        mGame.setDifficultyLevel(mPrefs.getInt("mDifficulty", 0)); //Por defecto, fácil
        autoNewGame = mPrefs.getBoolean("autoNewGame",false);

        displayScores();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHumanMP.release();
        mComputerMP.release();
        mTieMP.release();
        mHumanWinsMP.release();
        mComputerMP.release();
        mBonkMP.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHumanMP = MediaPlayer.create(getApplicationContext(), R.raw.aoe_ii_mining_camp_sound);
        mComputerMP = MediaPlayer.create(getApplicationContext(), R.raw.aoe_ii_watch_tower_sound);
        mTieMP = MediaPlayer.create(getApplicationContext(), R.raw.aoe_ii_monk_conversion);
        mHumanWinsMP = MediaPlayer.create(getApplicationContext(),R.raw.aoe_ii_victorious);
        mComputerWinsMP = MediaPlayer.create(getApplicationContext(),R.raw.aoe_ii_defeated);
        mBonkMP = MediaPlayer.create(getApplicationContext(),R.raw.bonk);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Guardar los puntajes actuales
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("mHumanWins", stats[1]);
        ed.putInt("mComputerWins", stats[2]);
        ed.putInt("mTies", stats[0]);
        ed.putInt("mDifficulty",mGame.getDifficultyLevel().getValue());
        ed.putBoolean("autoNewGame",autoNewGame);
        ed.commit();
    }

    //Menu de Opciones
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.new_game:
                CharSequence toastText = getResources().getString(R.string.toast_new_game);
                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                startNewGame();
                return true;
            case R.id.reset_games:
                resetGameCount();
                return true;
            case R.id.ai_difficulty:
                showDialog(DIALOG_DIFFICULTY_ID);
                return true;
            case R.id.auto_new_game:
                showDialog(DIALOG_AUTO_NEW_GAME);
                return true;
            case R.id.quit:
                showDialog(DIALOG_QUIT_ID);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        //return true;
    }

    //Dialogos
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch(id) {
            case DIALOG_DIFFICULTY_ID:
                builder.setTitle(R.string.difficulty_choose);
                final CharSequence[] levels = {
                        getResources().getString(R.string.difficulty_easy),
                        getResources().getString(R.string.difficulty_harder),
                        getResources().getString(R.string.difficulty_expert)};
                // 0 -- Easy
                // 1 -- Harder
                // 2 -- Expert
                int selected = mGame.getDifficultyLevel().getValue();
                builder.setSingleChoiceItems(levels, selected, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss(); // Close dialog
                        // System.out.println(item);
                        if(item == 0){
                            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
                        } else if(item == 1){
                            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                        } else if(item == 2){
                            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
                        }
                        // Display the selected difficulty level
                        CharSequence toastText = getResources().getString(R.string.toast_difficulty) +" "+ levels[item];
                        Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();

                        startNewGame();
                    }
                });
                dialog = builder.create();

                break;
            case DIALOG_QUIT_ID:
                // Create the quit confirmation dialog
                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AndroidTicTacToeActivity.this.finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                dialog = builder.create();
                break;
            case DIALOG_AUTO_NEW_GAME:
                builder.setMessage(R.string.auto_new_game_question)
                        .setCancelable(false)
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                CharSequence toastText = getResources().getString(R.string.toast_auto_new_game)+ " "+ getResources().getString(R.string.str_false);
                                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                                autoNewGame = false;
                                startNewGame();
                            }
                        })
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                CharSequence toastText = getResources().getString(R.string.toast_auto_new_game)+ " "+ getResources().getString(R.string.str_true);
                                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                                autoNewGame = true;
                                startNewGame();
                            }
                        });
                dialog = builder.create();
                break;
        }
        return dialog;
    }

    private void resetGameCount(){
        mBonkMP.start();
        stats = new int[]{0, 0, 0};
        mStatsTiesNumber.setText(String.valueOf(stats[0]));
        mStatsHumanWinsNumber.setText(String.valueOf(stats[1]));
        mStatsComputerWinsNumber.setText(String.valueOf(stats[2]));

        // Display the toast for reset games
        CharSequence toastText = getResources().getString(R.string.toast_reset_games);
        Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();

        startNewGame();
    }


    // Preparando el tablero
    private void startNewGame(){
        mStart = !mStart;
        mGame.clearBoard();
        mGameOver = false;
        mBoardView.invalidate();  //Redibujar el tablero
        // La persona va primero
        if(!mStart) {
            mInfoTextView.setText(R.string.first_human);
        } else {
            mInfoTextView.setText(R.string.first_computer);
            computerMakeMove(0);
        }
    }

    private void displayScores(){
        mStatsTiesNumber.setText(String.valueOf(stats[0]));
        mStatsHumanWinsNumber.setText(String.valueOf(stats[1]));
        mStatsComputerWinsNumber.setText(String.valueOf(stats[2]));
    }

    // Listen for touches on the board
    private OnTouchListener mTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            // Determine which cell was touched
            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;
            if(mHumanTurn){
                if (!mGameOver && setMove(TicTacToeGame.HUMAN_PLAYER, pos)){
                    mHumanMP.start();
                    // If no winner yet, let the computer make a move
                    int winner = mGame.checkForWinner();
                    if (winner == 0) {
                        mInfoTextView.setText(R.string.turn_computer);
                        mHumanTurn = false;
                        computerMakeMove(computerMoveDelay);
                    } else{
                        checkWinner(winner);
                    }
                }
            }

            // So we aren't notified of continued events when finger is moved
            return false;
        }
    };


    private void computerMakeMove(int delay) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    try {
                    mComputerMP.start();
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    int winner = mGame.checkForWinner();
                    checkWinner(winner);
                    mHumanTurn = true;
                    } catch (Exception e) {
                        System.out.println(e.getStackTrace());
                    }
                }
            }, delay);

    }


    private void checkWinner(int winner){
        if (winner == 0) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    int winner = mGame.checkForWinner();
                    if (winner == 0) {
                        mInfoTextView.setText(R.string.turn_human);
                    }
                }
            }, 1300);
        }else if (winner == 1) {
            mInfoTextView.setText(R.string.result_tie);
            stats[0]++;
            //mStatsTiesNumber.setText(String.valueOf(stats[0]));
            mTieMP.start();
            mGameOver = true;
            checkAutoNewGame(autoNewGameDelay);
        }else if (winner == 2) {
            mInfoTextView.setText(R.string.result_human_wins);
            stats[1]++;
            //mStatsHumanWinsNumber.setText(String.valueOf(stats[1]));
            mHumanWinsMP.start();
            mGameOver = true;
            checkAutoNewGame(autoNewGameDelay);
        } else {
            mInfoTextView.setText(R.string.result_computer_wins);
            stats[2]++;
            //mStatsComputerWinsNumber.setText(String.valueOf(stats[2]));
            mComputerWinsMP.start();
            mGameOver = true;
            checkAutoNewGame(autoNewGameDelay);
        }
        displayScores();
    }



    private void checkAutoNewGame(int delay){
        if(autoNewGame){
            CharSequence toastText = getResources().getString(R.string.begin_auto_new_game);
            Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    startNewGame();
                }
            }, delay);
        }
    }

    private boolean setMove(char player, int location) {
        if(mGame.setMove(player, location)){
            mBoardView.invalidate();
            return true;
        } else{
            return false;
        }
    }
}