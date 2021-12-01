package co.edu.unal.tictactoe;

/* TicTacToeConsole.java
 * By Frank McCown (Harding University)
 *
 * This is a tic-tac-toe game that runs in the console window.  The human
 * is X and the computer is O.
 */

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class TicTacToeGame {

    // The computer's difficulty levels
    public enum DifficultyLevel {
        Easy(0), Harder(1), Expert(2);

        private final int value;
        private DifficultyLevel(int value){
            this.value = value;
        }

        public int getValue(){
            return value;
        }
    }
    // Current difficulty level
    private DifficultyLevel mDifficultyLevel = DifficultyLevel.Harder;

    //public static int BOARD_SIZE;
    private char mBoard[] = {'1','2','3','4','5','6','7','8','9'};
    public static final int BOARD_SIZE = 9;

    public static final char HUMAN_PLAYER = 'X';
    public static final char COMPUTER_PLAYER = 'O';
    //Simbolos para el multijugador
    public static final char PLAYER1_PLAYER = 'X';
    public static final char PLAYER2_PLAYER = 'O';
    public static final char OPEN_SPOT = ' '; //Representa con un espacio las pocisiones libres

    private Random mRand;

    public TicTacToeGame() {

        // Seed the random number generator
        mRand = new Random();
    }

    // Check for a winner.  Return
    //  0 if no winner or tie yet
    //  1 if it's a tie
    //  2 if X won
    //  3 if O won
    public int checkForWinner() {

        // Check horizontal wins
        for (int i = 0; i <= 6; i += 3)	{
            if (mBoard[i] == HUMAN_PLAYER &&
                    mBoard[i+1] == HUMAN_PLAYER &&
                    mBoard[i+2]== HUMAN_PLAYER)
                return 2;
            if (mBoard[i] == COMPUTER_PLAYER &&
                    mBoard[i+1]== COMPUTER_PLAYER &&
                    mBoard[i+2] == COMPUTER_PLAYER)
                return 3;
        }

        // Check vertical wins
        for (int i = 0; i <= 2; i++) {
            if (mBoard[i] == HUMAN_PLAYER &&
                    mBoard[i+3] == HUMAN_PLAYER &&
                    mBoard[i+6]== HUMAN_PLAYER)
                return 2;
            if (mBoard[i] == COMPUTER_PLAYER &&
                    mBoard[i+3] == COMPUTER_PLAYER &&
                    mBoard[i+6]== COMPUTER_PLAYER)
                return 3;
        }

        // Check for diagonal wins
        if ((mBoard[0] == HUMAN_PLAYER &&
                mBoard[4] == HUMAN_PLAYER &&
                mBoard[8] == HUMAN_PLAYER) ||
                (mBoard[2] == HUMAN_PLAYER &&
                        mBoard[4] == HUMAN_PLAYER &&
                        mBoard[6] == HUMAN_PLAYER))
            return 2;
        if ((mBoard[0] == COMPUTER_PLAYER &&
                mBoard[4] == COMPUTER_PLAYER &&
                mBoard[8] == COMPUTER_PLAYER) ||
                (mBoard[2] == COMPUTER_PLAYER &&
                        mBoard[4] == COMPUTER_PLAYER &&
                        mBoard[6] == COMPUTER_PLAYER))
            return 3;

        // Check for tie
        for (int i = 0; i < BOARD_SIZE; i++) {
            // If we find a number, then no one has won yet
            if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER)
                return 0;
        }

        // If we make it through the previous loop, all places are taken, so it's a tie
        return 1;
    }

    public int getComputerMove()
    {
        int move = -1;

        if(mDifficultyLevel == DifficultyLevel.Easy){
            move = getRandomMove();
        } else if(mDifficultyLevel == DifficultyLevel.Harder){
            move = getWinningMove();
            if(move == -1)
                move = getRandomMove();
        } else if(mDifficultyLevel == DifficultyLevel.Expert){
            move = getWinningMove();
            if(move == -1)
                move = getBlockingMove();
            if(move == -1)
                move = getRandomMove();
        }

        System.out.println("Computer is moving to " + (move + 1));

        //mBoard[move] = COMPUTER_PLAYER;

        return move;
    }

    private int getWinningMove(){
        int move = -1;
        // First see if there's a move O can make to win
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER) {
                char curr = mBoard[i];
                mBoard[i] = COMPUTER_PLAYER;
                if (checkForWinner() == 3) {
                    mBoard[i] = curr;
                    System.out.println("Computer is moving to " + (i + 1));
                    move = i;
                    break;
                }
                else
                    mBoard[i] = curr;
            }
        }
        return move;
    }

    private int getBlockingMove(){
        int move= -1;
        // See if there's a move O can make to block X from winning
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER) {
                char curr = mBoard[i];   // Save the current number
                mBoard[i] = HUMAN_PLAYER;
                if (checkForWinner() == 2) {
                    mBoard[i] = curr;
                    //mBoard[i] = COMPUTER_PLAYER;
                    System.out.println("Computer is moving to " + (i + 1));
                    move = i;
                    break;
                }
                else
                    mBoard[i] = curr;
            }
        }
        return move;
    }

    private int getRandomMove(){
        // Generate random move
        int move;
        do
        {
            move = mRand.nextInt(BOARD_SIZE);
        } while (mBoard[move] == HUMAN_PLAYER || mBoard[move] == COMPUTER_PLAYER);
        return move;
    }

    /** Clear the board of all X's and O's by setting all spots to OPEN_SPOT. */
    public void clearBoard(){
        for (int i = 0; i < BOARD_SIZE; i++) {
            mBoard[i] = OPEN_SPOT;
        }
    }
    /** Set the given player at the given location on the game board.
     * The location must be available, or the board will not be changed.
     *
     * @param player - The HUMAN_PLAYER or COMPUTER_PLAYER
     * @param location - The location (0-8) to place the move
     */
    public boolean setMove(char player, int location){
        if(mBoard[location] == OPEN_SPOT) {
            mBoard[location] = player;
            return true;
        } else{
            return false;
        }
    }

    //Getters y setters nivel de dificultad
    public DifficultyLevel getDifficultyLevel() {
        return mDifficultyLevel;
    }

    public int getDifficultyLevelInt() {
        DifficultyLevel dl = mDifficultyLevel;
        return dl.getValue();
    }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        mDifficultyLevel = difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        for(DifficultyLevel dl : DifficultyLevel.values()) {
            if(dl.getValue() == difficultyLevel){
                mDifficultyLevel = dl;
                break;
            }

        }
    }

    //Obtener el jugador que ocupe una casilla
    public int getBoardOccupant(int i){
        return mBoard[i];
    }

    public char[] getBoardState(){
        return mBoard;
    }

    public List<String> getBoardStateList(){
        List<String> boardList = new ArrayList<>();
        for (char elem : mBoard){
            boardList.add(""+elem);
        }
        return boardList;
    }

    public void setBoardState(char[] prevState){
        mBoard = prevState.clone();
    }

    public void setBoardStateList(List<String> boardList){
        for(int i = 0; i < boardList.size(); i++){
            mBoard[i] = boardList.get(i).charAt(0);
        }
    }
}