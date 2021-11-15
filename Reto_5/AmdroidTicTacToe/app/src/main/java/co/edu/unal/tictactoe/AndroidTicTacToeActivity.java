package co.edu.unal.tictactoe;

import java.lang.Thread.*;

import androidx.appcompat.app.AppCompatActivity;

import android.app.*;
import android.content.DialogInterface;
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

public class AndroidTicTacToeActivity extends AppCompatActivity {

    // Representa el estado interno del juego
    private TicTacToeGame mGame;
    // Turno al iniciar
    private boolean mStart = true;
    //Botones que conforman el tablero
    private Button mBoardButtons[];
    //Textos variados mostrados
    private TextView mInfoTextView;
    //Indicador de que se acaba el juego
    private boolean mGameOver = false;
    //Indicador del turno
    private boolean mHumanTurn = true;
    //Juego nuevo automático
    private boolean autoNewGame = false;
    private int autoNewGameDelay = 5000;

    // Card Estadisticas

    // Ties / Human Wins / Computer Wins
    private int[] stats = {0,0,0};

    private TextView mStatsTiesNumber;
    private TextView mStatsHumanWinsNumber;
    private TextView mStatsComputerWinsNumber;

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
                    int selected = 0;
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
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                CharSequence toastText = getResources().getString(R.string.toast_auto_new_game)+ " "+ getResources().getString(R.string.str_true);
                                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                                autoNewGame = true;
                                startNewGame();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                CharSequence toastText = getResources().getString(R.string.toast_auto_new_game)+ " "+ getResources().getString(R.string.str_false);
                                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                                autoNewGame = false;
                                startNewGame();
                            }
                        });
                dialog = builder.create();
                break;
        }
        return dialog;
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

        startNewGame();
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
    protected void onPause() {
        super.onPause();
        mHumanMP.release();
        mComputerMP.release();
        mTieMP.release();
        mHumanWinsMP.release();
        mComputerMP.release();
        mBonkMP.release();
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
                        computerMakeMove(1000);
                    } else{
                        checkWinner(winner);
                    }
                }
            }

            // So we aren't notified of continued events when finger is moved
            return false;
        }
    };


    private void computerMakeMove(int delay){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mComputerMP.start();
                int move = mGame.getComputerMove();
                setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                int winner = mGame.checkForWinner();
                checkWinner(winner);
                mHumanTurn = true;
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
            mStatsTiesNumber.setText(String.valueOf(stats[0]));
            mTieMP.start();
            mGameOver = true;
            checkAutoNewGame(autoNewGameDelay);
        }else if (winner == 2) {
            mInfoTextView.setText(R.string.result_human_wins);
            stats[1]++;
            mStatsHumanWinsNumber.setText(String.valueOf(stats[1]));
            mHumanWinsMP.start();
            mGameOver = true;
            checkAutoNewGame(autoNewGameDelay);
        } else {
            mInfoTextView.setText(R.string.result_computer_wins);
            stats[2]++;
            mStatsComputerWinsNumber.setText(String.valueOf(stats[2]));
            mComputerWinsMP.start();
            mGameOver = true;
            checkAutoNewGame(autoNewGameDelay);
        }
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