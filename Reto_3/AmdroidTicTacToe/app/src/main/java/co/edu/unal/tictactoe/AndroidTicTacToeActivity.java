package co.edu.unal.tictactoe;

import java.lang.Thread.*;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AndroidTicTacToeActivity extends AppCompatActivity {

    // Representa el estado interno del juego
    private TicTacToeGame mGame;
    //Botones que conforman el tablero
    private Button mBoardButtons[];
    //Textos variados mostrados
    private TextView mInfoTextView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        menu.add(R.string.menu_new_game);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startNewGame();
        return true;
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
        mGame = new TicTacToeGame();

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
        mInfoTextView.setText(R.string.first_human);
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
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            computerMove();
                            int winner = mGame.checkForWinner();
                            checkWinner(winner);
                        }
                    }, 1000);

                } else {
                    checkWinner(winner);
                }
            }
        }
    }

    private void checkWinner(int winner){
        if (winner == 0)
            mInfoTextView.setText(R.string.turn_human);
        else if (winner == 1) {
            mInfoTextView.setText(R.string.result_tie);
            disableBoard();
        }else if (winner == 2) {
            mInfoTextView.setText(R.string.result_human_wins);
            disableBoard();
        } else {
            mInfoTextView.setText(R.string.result_computer_wins);
            disableBoard();
        }
    }

    private void disableBoard(){
        for(int i = 0; i < mBoardButtons.length; i++){
            mBoardButtons[i].setEnabled(false);
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