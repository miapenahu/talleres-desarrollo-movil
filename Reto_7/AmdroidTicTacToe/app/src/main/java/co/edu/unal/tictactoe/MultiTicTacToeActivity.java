package co.edu.unal.tictactoe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
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
    private boolean mPlayer1Start = true;
    //Botones que conforman el tablero
    private Button mBoardButtons[];
    //Textos variados mostrados
    private TextView mInfoTextView;
    private TextView mPlayerTextView;
    private ImageView mPlayerImageView;
    private TextView mStatsTiesNumber;
    private TextView mStatsPlayer1WinsNumber;
    private TextView mStatsPlayer2WinsNumber;
    //Indicador de que se acaba el juego
    private boolean mGameOver = false;
    //Indicador del turno
    private boolean mPlayer1Turn = true;
    //Nuevo Juego
    private boolean mNewGame = false;
    //Juego nuevo automático
    private boolean mAutoNewGame = false;
    //Revisa si el host se ha ido
    private boolean mPlayer1Gone = false;
    //Revisa si el player2 ha llegado
    private boolean mPlayer2Joined = false;
    private int mDelayPlayer1Gone = 3000; //Delay para mostrar el dialogo de sala cerrada
    //Delays
    private int autoNewGameDelay = 5000;
    private int computerMoveDelay = 1000;
    // Conteo stats: Ties / Player1 Wins / Player2 Wins
    private int[] stats = {0,0,0};
    //Dialogos
    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;
    static final int DIALOG_AUTO_NEW_GAME = 2;
    static final int DIALOG_SALA_CERRADA = 3;
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
    DatabaseReference playerRef;
    DatabaseReference player1Ref;
    DatabaseReference player2Ref;
    DatabaseReference player1TurnRef;
    DatabaseReference newGameRef;
    DatabaseReference autoNewGameRef;
    DatabaseReference boardRef;
    DatabaseReference salaRef;
    DatabaseReference player1StartRef;
    DatabaseReference resTiesRef;
    DatabaseReference resPlayer1WinsRef;
    DatabaseReference resPlayer2WinsRef;
    //Listeners Firebase
    ValueEventListener playerVEL;
    ValueEventListener player1VEL;
    ValueEventListener player2VEL;
    ValueEventListener player1TurnVEL;
    ValueEventListener newGameVEL;
    ValueEventListener autoNewGameVEL;
    ChildEventListener boardCEL;
    ValueEventListener salaVEL;
    ValueEventListener player1StartVEL;
    ValueEventListener resTiesVEL;
    ValueEventListener resPlayer1WinsVEL;
    ValueEventListener resPlayer2WinsVEL;
    //Runnables
    Runnable runClose;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharArray("board", mGame.getBoardState());
        outState.putBoolean("mPlayer1Start",mPlayer1Start);
        outState.putBoolean("mGameOver", mGameOver);
        outState.putBoolean("mPlayer1Turn", mPlayer1Turn);
        outState.putBoolean("mAutoNewGame", mAutoNewGame);
        outState.putCharSequence("info", mInfoTextView.getText());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Content
        setContentView(R.layout.activity_multi_tic_tac_toe);

        //Juego
        mGame = new TicTacToeGame();
        mBoardView = (BoardView) findViewById(R.id.board);
        mBoardView.setGame(mGame);


        //Informacion
        mInfoTextView = findViewById(R.id.information);
        mPlayerTextView = findViewById(R.id.playerTextView);
        mPlayerImageView = findViewById(R.id.playerImageView);
        String playerText = "";
        //Stats
        mStatsTiesNumber = findViewById(R.id.TiesNumber);
        mStatsPlayer1WinsNumber = findViewById(R.id.Player1WinsNumber);
        mStatsPlayer2WinsNumber = findViewById(R.id.Player2WinsNumber);

        if(savedInstanceState != null){ //equivalente a onRestoreInstanceState
            System.out.println("second time config");
            mGame.setBoardState(savedInstanceState.getCharArray("board"));
            mGameOver = savedInstanceState.getBoolean("mGameOver");
            mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
            mPlayer1Start = savedInstanceState.getBoolean("mPlayer1Start");
            mPlayer1Turn = savedInstanceState.getBoolean("mPlayer1Turn", mPlayer1Turn);
            mAutoNewGame = savedInstanceState.getBoolean("mAutoNewGame", mAutoNewGame);
            mBoardView.invalidate();
        }

        //El listener va después porque actualizaría el juego vacío
        mBoardView.setOnTouchListener(mTouchListener);

        System.out.println("savdstate: "+ savedInstanceState);
        System.out.println("savedstate == null: "+savedInstanceState == null);

        //Firebase
        database = FirebaseDatabase.getInstance();

        mPrefs = getSharedPreferences("user_prefs",0);
        username = mPrefs.getString("username","");

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            nombreSala = extras.getString("nombreSala");
            if(nombreSala.equals(username)){
                mRole = TicTacToeGame.PLAYER1_PLAYER;
                playerText = getResources().getString(R.string.str_player_1) +": "+username;
                mPlayerImageView.setImageResource(R.drawable.x_min);
            } else {
                mRole = TicTacToeGame.PLAYER2_PLAYER;
                playerText = getResources().getString(R.string.str_player_2) +": "+username;
                mPlayerImageView.setImageResource(R.drawable.o_min);
            }
            if(extras.getBoolean("salaCerrada",false)){
                mDelayPlayer1Gone = 0;
            }
        }

        mPlayerTextView.setText(playerText); //Información del jugador
        setTitle("Sala de " + nombreSala); //Título de la sala

        //Listener para los mensajes entrantes
        player1Ref = database.getReference("salas/" + nombreSala + "/jugador1");
        player2Ref = database.getReference("salas/" + nombreSala + "/jugador2");
        if(mRole == TicTacToeGame.PLAYER1_PLAYER) {
            playerRef = player1Ref;
        }else if(mRole == TicTacToeGame.PLAYER2_PLAYER) {
            playerRef = player2Ref;
        }
        player1TurnRef = database.getReference("salas/" + nombreSala + "/jugador1Turno");
        player1StartRef = database.getReference("salas/" + nombreSala + "/jugador1Comienza");
        newGameRef = database.getReference("salas/" + nombreSala + "/nuevoJuego");
        autoNewGameRef = database.getReference("salas/" + nombreSala + "/autoNuevoJuego");
        boardRef = database.getReference("salas/" + nombreSala +"/tablero");
        salaRef = database.getReference("salas/"+ nombreSala);
        resTiesRef = database.getReference("salas/"+nombreSala+"/stats/empates");
        resPlayer1WinsRef = database.getReference("salas/"+nombreSala+"/stats/p1_gana");
        resPlayer2WinsRef = database.getReference("salas/"+nombreSala+"/stats/p2_gana");

        clearListeners();

        inGameEventsListener();

        if (savedInstanceState == null) { //Primera ejecución de la actividad
            player1TurnRef.setValue(mPlayer1Turn);
            player1StartRef.setValue(mPlayer1Start);
            newGameRef.setValue(mNewGame);
            autoNewGameRef.setValue(mAutoNewGame);
            boardRef.setValue(mGame.getBoardStateList());
            //if(mRole == TicTacToeGame.PLAYER2_PLAYER) mPlayer1Start = !mPlayer1Start;
            startNewGame();
            //newGameRef.setValue(true);
        }

        displayScores();


    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        System.out.println("Actividad destruída");
        clearListeners();
    }

    @Override
    protected void onStop() {
        super.onStop();
        playerRef.setValue(""); //Libera el puesto del jugador al detener
        System.out.println("Actividad detenida");
        // Guardar los puntajes actuales
        /*SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("mHumanWins", stats[1]);
        ed.putInt("mComputerWins", stats[2]);
        ed.putInt("mTies", stats[0]);
        ed.commit();*/
    }


    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("On pause called");
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
        playerRef.setValue(username); //Recupera el puesto del jugador al resumir
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
                //startNewGame();
                //mNewGame = true;
                newGameRef.setValue(true);
                return true;
            case R.id.auto_new_game:
                showDialog(DIALOG_AUTO_NEW_GAME);
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
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch(id) {
            case DIALOG_AUTO_NEW_GAME:
                builder.setMessage(R.string.auto_new_game_question)
                        .setCancelable(false)
                        .setNegativeButton(R.string.str_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                CharSequence toastText = getResources().getString(R.string.toast_auto_new_game)+ " "+ getResources().getString(R.string.str_false);
                                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                                //mAutoNewGame = false;
                                autoNewGameRef.setValue(false);
                                //startNewGame();
                                newGameRef.setValue(true);
                            }
                        })
                        .setPositiveButton(R.string.str_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                CharSequence toastText = getResources().getString(R.string.toast_auto_new_game)+ " "+ getResources().getString(R.string.str_true);
                                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                                //mAutoNewGame = true;
                                autoNewGameRef.setValue(true);
                                //startNewGame();
                                newGameRef.setValue(true);
                            }
                        });
                dialog = builder.create();
                break;

            case DIALOG_SALA_CERRADA:
                // Create the quit confirmation dialog
                builder.setMessage(R.string.dialog_sala_cerrada)
                        .setCancelable(false)
                        .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MultiTicTacToeActivity.this.finish();
                            }
                        });
                dialog = builder.create();
                break;
        }
        return dialog;
    }

    @Override
    public void onBackPressed() {
        playerRef.setValue(""); //Libera el puesto del jugador al salir
        Intent intent = new Intent(getApplicationContext(),LobbyActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        //finish();
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
             if(mRole == TicTacToeGame.PLAYER1_PLAYER){
                 mInfoTextView.setText(R.string.first_human);
             } else{
                 mInfoTextView.setText(R.string.first_player1);
             }

        } else {
             if(mRole == TicTacToeGame.PLAYER2_PLAYER){
                 mInfoTextView.setText(R.string.first_human);
             } else{
                 mInfoTextView.setText(R.string.first_player2);
             }
        }
        if(mRole == TicTacToeGame.PLAYER1_PLAYER) { //Si es el host del juego
            boardRef.setValue(mGame.getBoardStateList());
            player1TurnRef.setValue(mPlayer1Turn);
        }
        mNewGame = false;
        newGameRef.setValue(mNewGame);
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
                if(mRole == TicTacToeGame.PLAYER1_PLAYER && mPlayer2Joined){
                    playerMakeMove(TicTacToeGame.PLAYER1_PLAYER, pos);
                }
            } else {
                if(mRole == TicTacToeGame.PLAYER2_PLAYER) {

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
                if(player != mRole){
                    mInfoTextView.setText(R.string.turn_human);
                }else if(player == TicTacToeGame.PLAYER1_PLAYER){ //Mostrar que es el turno del rival
                    mInfoTextView.setText(R.string.turn_player2);
                } else {
                    mInfoTextView.setText(R.string.turn_player1);
                }
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
        if(mAutoNewGame){
            CharSequence toastText = getResources().getString(R.string.begin_auto_new_game);
            Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    newGameRef.setValue(true);
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

    private void clearListeners(){
        //ValueEventListener playerVEL;
        if(playerVEL != null) playerRef.removeEventListener(playerVEL);
        if(player1VEL != null) player1Ref.removeEventListener(player1VEL);
        if(player2VEL != null) player2Ref.removeEventListener(player2VEL);
        if(player1TurnVEL != null) player1TurnRef.removeEventListener(player1TurnVEL);
        if(newGameVEL != null) newGameRef.removeEventListener(newGameVEL);
        if(autoNewGameVEL != null) autoNewGameRef.removeEventListener(autoNewGameVEL);
        if(boardCEL != null) boardRef.removeEventListener(boardCEL);
        if(salaVEL != null) salaRef.removeEventListener(salaVEL);
        if(player1StartVEL != null) player1StartRef.removeEventListener(player1StartVEL);
        if(resTiesVEL != null) resTiesRef.removeEventListener(resTiesVEL);
        if(resPlayer1WinsVEL != null) resPlayer1WinsRef.removeEventListener(resPlayer1WinsVEL);
        if(resPlayer2WinsVEL != null) resPlayer2WinsRef.removeEventListener(resPlayer2WinsVEL);
    }

    private void inGameEventsListener(){

        /*playerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/

        player1VEL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    final Handler handler = new Handler();

                    if(snapshot.getValue(String.class).equals("")){
                        mPlayer1Gone = true;
                    } else{
                        mPlayer1Gone = false;
                    }

                    runClose = new Runnable() {
                        public void run() {
                            //if (mRole == TicTacToeGame.PLAYER2_PLAYER && snapshot.getValue(String.class).equals("")) {
                            if (mRole == TicTacToeGame.PLAYER2_PLAYER && mPlayer1Gone ){ //&& !salaRef.child("jugador1").equals("")) {
                                //Si el jugador 1 sale de la sala, cierra la sesión al jugador 2
                                try {
                                    showDialog(DIALOG_SALA_CERRADA);
                                } catch (Exception e) {
                                    Toast.makeText(MultiTicTacToeActivity.this, R.string.dialog_sala_cerrada, Toast.LENGTH_SHORT).show();
                                    System.out.println(e.getStackTrace());
                                    finish();
                                }
                            } /*else if(snapshot.getValue(String.class).equals("")){
                                System.out.println("JUgador 1 value: "+ salaRef.child("jugador1").equals(snapshot.getRef()));
                                System.out.println("El valor era nulo, pero ya no lo es");
                            }*/
                        }
                    };

                    handler.postDelayed(runClose, mDelayPlayer1Gone);

                    /*if(!mPlayer1Gone){ //Si el jugador se ha ido, cancelar el handler
                        System.out.println("handler cancelado!");
                        handler.removeCallbacks(runClose);
                    } else {

                    }
                    */
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        player1Ref.addValueEventListener(player1VEL);

        player2VEL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue(String.class).equals("")){ //Si no hay segundo jugador en la sala
                    if(mRole == TicTacToeGame.PLAYER1_PLAYER){
                        //Impedir al player 1 jugar
                        mPlayer2Joined = false;
                        Toast.makeText(MultiTicTacToeActivity.this, "Jugador 2 ha salido, esperando nuevo jugador...", Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(mRole == TicTacToeGame.PLAYER1_PLAYER) {
                        Toast.makeText(MultiTicTacToeActivity.this, "Jugador 2: " + snapshot.getValue(String.class) + " ha llegado!", Toast.LENGTH_SHORT).show();
                        mPlayer2Joined = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        player2Ref.addValueEventListener(player2VEL);

        player1StartVEL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) mPlayer1Start = snapshot.getValue(Boolean.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        player1StartRef.addValueEventListener(player1StartVEL);

        player1TurnVEL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) mPlayer1Turn = snapshot.getValue(Boolean.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        player1TurnRef.addValueEventListener(player1TurnVEL);

        newGameVEL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    mNewGame = snapshot.getValue(Boolean.class);
                    if (mNewGame) {
                        CharSequence toastText = getResources().getString(R.string.toast_new_game);
                        Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                        startNewGame();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        newGameRef.addValueEventListener(newGameVEL);

        autoNewGameVEL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    mAutoNewGame = snapshot.getValue(Boolean.class);
                    if(mAutoNewGame) Toast.makeText(getApplicationContext(), R.string.toast_auto_activated, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        autoNewGameRef.addValueEventListener(autoNewGameVEL);

        boardCEL = new ChildEventListener() {
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
        };

        boardRef.addChildEventListener(boardCEL);

        resTiesVEL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    stats[0] = snapshot.getValue(Integer.class);
                    displayScores();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        resTiesRef.addValueEventListener(resTiesVEL);

        resPlayer1WinsVEL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    stats[1] = snapshot.getValue(Integer.class);
                    displayScores();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        resPlayer1WinsRef.addValueEventListener(resPlayer1WinsVEL);

        resPlayer2WinsVEL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    stats[2] = snapshot.getValue(Integer.class);
                    displayScores();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        resPlayer2WinsRef.addValueEventListener(resPlayer2WinsVEL);

    }
}