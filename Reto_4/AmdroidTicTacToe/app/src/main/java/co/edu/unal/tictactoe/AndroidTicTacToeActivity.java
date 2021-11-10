package co.edu.unal.tictactoe;

import java.lang.Thread.*;

import androidx.appcompat.app.AppCompatActivity;

import android.app.*;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidTicTacToeActivity extends AppCompatActivity {

    // Representa el estado interno del juego
    private TicTacToeGame mGame;
    // Turno al iniciar
    private boolean mStart;
    //Botones que conforman el tablero
    private Button mBoardButtons[];
    //Textos variados mostrados
    private TextView mInfoTextView;

    // Card Estadisticas

    // Ties / Human Wins / Computer Wins
    private int[] stats = {0,0,0};

    private TextView mStatsTiesNumber;
    private TextView mStatsHumanWinsNumber;
    private TextView mStatsComputerWinsNumber;

    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;

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
                mStart = !mStart;
                startNewGame();
                return true;
            case R.id.reset_games:
                resetGameCount();
                return true;
            case R.id.ai_difficulty:
                showDialog(DIALOG_DIFFICULTY_ID);
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
                            CharSequence toastText = getResources().getString(R.string.difficulty_toast) +" "+ levels[item];
                            Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
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

        }
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBoardButtons = new Button[TicTacToeGame.BOARD_SIZE];
        mBoardButtons[0] = findViewById(R.id.one);
        mBoardButtons[1] = findViewById(R.id.two);
        mBoardButtons[2] = findViewById(R.id.three);
        mBoardButtons[3] = findViewById(R.id.four);
        mBoardButtons[4] = findViewById(R.id.five);
        mBoardButtons[5] = findViewById(R.id.six);
        mBoardButtons[6] = findViewById(R.id.seven);
        mBoardButtons[7] = findViewById(R.id.eight);
        mBoardButtons[8] = findViewById(R.id.nine);

        mInfoTextView = findViewById(R.id.information);
        //Stats
        mStatsTiesNumber = findViewById(R.id.TiesNumber);
        mStatsHumanWinsNumber = findViewById(R.id.HumanWinsNumber);
        mStatsComputerWinsNumber = findViewById(R.id.ComputerWinsNumber);

        mGame = new TicTacToeGame();
        mStart = false;

        startNewGame();
    }

    private void resetGameCount(){
        stats = new int[]{0, 0, 0};
        mStatsTiesNumber.setText(String.valueOf(stats[0]));
        mStatsHumanWinsNumber.setText(String.valueOf(stats[1]));
        mStatsComputerWinsNumber.setText(String.valueOf(stats[2]));
        startNewGame();
    }


    // Preparando el tablero
    private void startNewGame(){
        mGame.clearBoard();
        // Reiniciar todos los botones
        for(int i = 0; i < mBoardButtons.length; i++){
            mBoardButtons[i].setText("");
            mBoardButtons[i].setEnabled(true);
            mBoardButtons[i].setOnClickListener(new ButtonClickListener(i));
        }
        // La persona va primero
        if(!mStart) {
            mInfoTextView.setText(R.string.first_human);
        } else{
            mInfoTextView.setText(R.string.first_computer);
            computerMakeMove();
        }
    }
    // Maneja los clicks en los botones del tablero
    private class ButtonClickListener implements View.OnClickListener {
        int location;
        public ButtonClickListener(int location) {
            this.location = location;
        }
        public void onClick(View view) {
            if (mBoardButtons[location].isEnabled()) {
                    setMove(TicTacToeGame.HUMAN_PLAYER, location);
                    // If no winner yet, let the computer make a move
                    int winner = mGame.checkForWinner();
                    if (winner == 0) {
                        mInfoTextView.setText(R.string.turn_computer);
                        computerMakeMove();
                    } else{
                        checkWinner(winner);
                    }
            }
        }
    }

    private void computerMakeMove(){
        disableBoard();
        computerMove();
        enableBoard();
        int winner = mGame.checkForWinner();
        checkWinner(winner);
        /*Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                computerMove();
                enableBoard();
                int winner = mGame.checkForWinner();
                checkWinner(winner);
            }
        }, 1000);*/
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
            disableBoard();
        }else if (winner == 2) {
            mInfoTextView.setText(R.string.result_human_wins);
            stats[1]++;
            mStatsHumanWinsNumber.setText(String.valueOf(stats[1]));
            disableBoard();
        } else {
            mInfoTextView.setText(R.string.result_computer_wins);
            stats[2]++;
            mStatsComputerWinsNumber.setText(String.valueOf(stats[2]));
            disableBoard();
        }
    }

    private void disableBoard(){
        for(int i = 0; i < mBoardButtons.length; i++){
            mBoardButtons[i].setEnabled(false);
        }
    }

    private void enableBoard(){
        for(int i = 0; i < mBoardButtons.length; i++){
            if(mBoardButtons[i].getText().length() == 0)
                mBoardButtons[i].setEnabled(true);
        }
    }

    private void computerMove(){
        int move = mGame.getComputerMove();
        setMove(TicTacToeGame.COMPUTER_PLAYER, move);
    }

    private void setMove(char player, int location) {
        mGame.setMove(player, location);
        mBoardButtons[location].setEnabled(false);
        mBoardButtons[location].setText(String.valueOf(player));
        if (player == TicTacToeGame.HUMAN_PLAYER)
            mBoardButtons[location].setTextColor(Color.rgb(0, 200, 0));
        else
            mBoardButtons[location].setTextColor(Color.rgb(200, 0, 0));
    }
}