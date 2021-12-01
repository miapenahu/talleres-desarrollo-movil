package co.edu.unal.tictactoe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MultiTicTacToeActivity extends AppCompatActivity {

    // Representa el estado interno del juego
    private TicTacToeGame mGame;
    // Turno al iniciar
    private boolean mPlayer1Start = false;
    //Botones que conforman el tablero
    private Button mBoardButtons[];
    //Textos variados mostrados
    private TextView mInfoTextView;
    private TextView mStatsTiesNumber;
    private TextView mStatsPlayer1WinsNumber;
    private TextView mStatsPlayer2WinsNumber;
    //Indicador de que se acaba el juego
    private boolean mGameOver = false;
    //Indicador del turno
    private boolean mPlayer1Turn = true;
    //Juego nuevo automático
    private boolean autoNewGame = false;
    //Delays
    private int autoNewGameDelay = 5000;
    private int computerMoveDelay = 1000;
    // Conteo stats: Ties / Player1 Wins / Player2 Wins
    private int[] stats = {0,0,0};
    //Dialogos
    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;
    static final int DIALOG_AUTO_NEW_GAME = 2;
    // Tablero
    private BoardView mBoardView;
    //Sonidos
    MediaPlayer mPlayerMP;
    MediaPlayer mAdversaryMP;
    MediaPlayer mDefeatMP;
    MediaPlayer mVictoryMP;
    MediaPlayer mTieMP;
    MediaPlayer mBonkMP;
    //Preferencias compartidas
    private SharedPreferences mPrefs;

    //Multiplayer
    String username = "";
    String nombreSala = "";
    char mRole = ' ';

    FirebaseDatabase database;
    //Refs Triqui
    DatabaseReference player1TurnRef;
    DatabaseReference boardRef;
    DatabaseReference salaRef;
    DatabaseReference player1StartRef;
    DatabaseReference resTiesRef;
    DatabaseReference resPlayer1WinsRef;
    DatabaseReference resPlayer2WinsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_tic_tac_toe);

        //Juego
        mGame = new TicTacToeGame();
        mBoardView = (BoardView) findViewById(R.id.board);
        mBoardView.setGame(mGame);
        mBoardView.setOnTouchListener(mTouchListener);

        //Informacion
        mInfoTextView = findViewById(R.id.information);
        //Stats
        mStatsTiesNumber = findViewById(R.id.TiesNumber);
        mStatsPlayer1WinsNumber = findViewById(R.id.Player1WinsNumber);
        mStatsPlayer2WinsNumber = findViewById(R.id.Player2WinsNumber);

        //Firebase
        database = FirebaseDatabase.getInstance();

        SharedPreferences preferences = getSharedPreferences("user_prefs",0);
        username = preferences.getString("username","");

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            nombreSala = extras.getString("nombreSala");
            if(nombreSala.equals(username)){
                mRole = TicTacToeGame.PLAYER1_PLAYER;
            } else {
                mRole = TicTacToeGame.PLAYER2_PLAYER;
            }
            stats[0] = extras.getInt("numTies");
            stats[1] = extras.getInt("numPlayer1Wins");
            stats[2] = extras.getInt("numPlayer2Wins");
        }

        //Listener para los mensajes entrantes
        player1TurnRef = database.getReference("salas/" + nombreSala + "/jugador1Turno");
        player1TurnRef.setValue(mPlayer1Turn);
        player1StartRef = database.getReference("salas/" + nombreSala + "/jugador1Comienza");
        player1StartRef.setValue(mPlayer1Start);
        boardRef = database.getReference("salas/" + nombreSala +"/tablero");
        boardRef.setValue(mGame.getBoardStateList());
        salaRef = database.getReference("salas/"+ nombreSala);
        resTiesRef = database.getReference("salas/"+nombreSala+"/stats/empates");
        resTiesRef.setValue(stats[0]);
        resPlayer1WinsRef = database.getReference("salas/"+nombreSala+"/stats/p1_gana");
        resPlayer1WinsRef.setValue(stats[1]);
        resPlayer2WinsRef = database.getReference("salas/"+nombreSala+"/stats/p2_gana");
        resPlayer2WinsRef.setValue(stats[2]);
        addRoomEventLIstener();


        if (savedInstanceState == null) startNewGame();

        displayScores();


    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayerMP.release();
        mAdversaryMP.release();
        mTieMP.release();
        mVictoryMP.release();
        mAdversaryMP.release();
        mBonkMP.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlayerMP = MediaPlayer.create(getApplicationContext(), R.raw.aoe_ii_mining_camp_sound);
        mAdversaryMP = MediaPlayer.create(getApplicationContext(), R.raw.aoe_ii_watch_tower_sound);
        mTieMP = MediaPlayer.create(getApplicationContext(), R.raw.aoe_ii_monk_conversion);
        mVictoryMP = MediaPlayer.create(getApplicationContext(),R.raw.aoe_ii_victorious);
        mDefeatMP = MediaPlayer.create(getApplicationContext(),R.raw.aoe_ii_defeated);
        mBonkMP = MediaPlayer.create(getApplicationContext(),R.raw.bonk);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_multiplayer, menu);
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
            default:
                return super.onOptionsItemSelected(item);
        }
        //return true;
    }


    // Preparando el tablero
    private void startNewGame(){
        player1StartRef.setValue(mPlayer1Start);
        mGame.clearBoard();
        mGameOver = false;
        mBoardView.invalidate();  //Redibujar el tablero
        //Cambiar quien empieza jugando
        mPlayer1Start = !mPlayer1Start;
        mPlayer1Turn = mPlayer1Start;
        // El jugador 1 va primero
         if (mPlayer1Start) {
            mInfoTextView.setText(R.string.first_player1);
        } else {
            mInfoTextView.setText(R.string.first_player2);
        }
        if(mRole == TicTacToeGame.PLAYER1_PLAYER) { //Si es el host del juego
            boardRef.setValue(mGame.getBoardStateList());
            player1TurnRef.setValue(mPlayer1Turn);
        }

    }

    private void displayScores(){
        mStatsTiesNumber.setText(String.valueOf(stats[0]));
        mStatsPlayer1WinsNumber.setText(String.valueOf(stats[1]));
        mStatsPlayer2WinsNumber.setText(String.valueOf(stats[2]));
    }

    // Listen for touches on the board
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            // Determine which cell was touched
            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;
            if(mPlayer1Turn){
                if(mRole == TicTacToeGame.PLAYER1_PLAYER){
                    mInfoTextView.setText(R.string.turn_player1);
                    playerMakeMove(TicTacToeGame.PLAYER1_PLAYER, pos);
                }
            } else {
                if(mRole == TicTacToeGame.PLAYER2_PLAYER) {
                    mInfoTextView.setText(R.string.turn_player2);
                    playerMakeMove(TicTacToeGame.PLAYER2_PLAYER, pos);
                }
            }
            // So we aren't notified of continued events when finger is moved
            return false;
        }
    };

    private void playerMakeMove(char player, int pos){
        if (!mGameOver && setMove(player, pos)) {
            boardRef.setValue(mGame.getBoardStateList());
            // If no winner yet, let the computer make a move
            int winner = mGame.checkForWinner();
            if (winner != 0) {
                checkWinner(winner);
            } else {
                mPlayer1Turn = !mPlayer1Turn;
                player1TurnRef.setValue(mPlayer1Turn);
            }
        }
    }

    private void soundPlayerMove(char player){
        try {
            if (player == mRole) {
                mPlayerMP.start();
            } else {
                mAdversaryMP.start();
            }
        } catch (Exception e){
            System.out.println(e.getStackTrace());
        }
    }

    private void soundAndLabelsCheckWinner(int winner){
        try {
            if (winner == 1) {
                mTieMP.start();
            } else if ((winner == 3 && mRole == TicTacToeGame.PLAYER2_PLAYER)
                    || (winner == 2 && mRole == TicTacToeGame.PLAYER1_PLAYER)) {
                mVictoryMP.start();
                mInfoTextView.setText(R.string.result_you_win);
            } else if ((winner == 2 && mRole == TicTacToeGame.PLAYER2_PLAYER)
                    || (winner == 3 && mRole == TicTacToeGame.PLAYER1_PLAYER)) {
                mDefeatMP.start();
                mInfoTextView.setText(R.string.result_you_lose);
            }
        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }
    }

    private void checkWinner(int winner){
        if (winner == 1) {
            mInfoTextView.setText(R.string.result_tie);
            stats[0]++;
            resTiesRef.setValue(stats[0]);
        }else if(winner == 2){

            stats[1]++;
            resPlayer1WinsRef.setValue(stats[1]);
        } else {

            stats[2]++;
            resPlayer2WinsRef.setValue(stats[2]);
        }
        checkAutoNewGame(autoNewGameDelay);
        mGameOver = true;
        soundAndLabelsCheckWinner(winner);
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
            soundPlayerMove(player);
            mBoardView.invalidate();
            return true;
        } else{
            return false;
        }
    }

    private void addRoomEventLIstener(){
        player1StartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mPlayer1Start = snapshot.getValue(Boolean.class);
                startNewGame();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        player1TurnRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mPlayer1Turn = snapshot.getValue(Boolean.class);
                /*if(mRol == TicTacToeGame.PLAYER1_PLAYER){
                    if(snapshot.getValue(Boolean.class)){ //Es turno del host (player 1)
                        Toast.makeText(MultiTicTacToeActivity.this, "Debería ser tu turno, player 1", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MultiTicTacToeActivity.this, "Debería ser el turno del player 2", Toast.LENGTH_SHORT).show();
                    }
                } else{
                    if(snapshot.getValue(Boolean.class)){ //Es turno del host (player 1)
                        Toast.makeText(MultiTicTacToeActivity.this, "Debería ser el turno del player 1", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MultiTicTacToeActivity.this, "Debería ser tu turno, player 2", Toast.LENGTH_SHORT).show();
                    }
                }*/

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        boardRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //Actualizar el tablero local cuando cambie el remoto
                char player = snapshot.getValue(String.class).charAt(0);
                if(player == TicTacToeGame.PLAYER1_PLAYER || player == TicTacToeGame.PLAYER2_PLAYER) {
                    playerMakeMove(player, Integer.parseInt(snapshot.getKey()));
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        resTiesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stats[0] = snapshot.getValue(Integer.class);
                displayScores();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        resPlayer1WinsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stats[1] = snapshot.getValue(Integer.class);
                displayScores();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        resPlayer2WinsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stats[2] = snapshot.getValue(Integer.class);
                displayScores();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}