package co.edu.unal.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LobbyActivity extends AppCompatActivity {

    ListView lvSalas;
    Button bCrearSala;

    List<String> lobby;
    List<List<Integer>> statsLobby;
    List<Integer> statsSala;

    String username = "";
    String nombreSala = "";

    FirebaseDatabase database;
    DatabaseReference salaRef;
    DatabaseReference lobbyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        database = FirebaseDatabase.getInstance();

        //Obtener el username y asignarle a su nombre de Sala su username
        SharedPreferences preferences = getSharedPreferences("user_prefs",0);
        username = preferences.getString("username","");
        nombreSala = username;

        lvSalas = findViewById(R.id.lvSalas);
        bCrearSala = findViewById(R.id.bCrearSala);

        //Todas las salas disponibles existentes
        lobby = new ArrayList<>();
        statsLobby = new ArrayList<>();
        statsSala = new ArrayList<>();
        statsSala.add(0,-1);

        bCrearSala.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Crear la sala y añadir al usuario actual como jugador 1
                bCrearSala.setText("Creando Sala...");
                bCrearSala.setEnabled(false);
                nombreSala = username;
                salaRef = database.getReference("salas/" + nombreSala + "/jugador1");
                addRoomEventListener();
                salaRef.setValue(username);

            }
        });

        lvSalas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Unirse a una sala previamente creada y añadir el usuario actual como jugador 2
                nombreSala = lobby.get(position);
                statsSala = statsLobby.get(position);
                salaRef = database.getReference("salas/" + nombreSala + "/jugador2");
                addRoomEventListener();
                salaRef.setValue(username);
            }
        });

        //Mostrar si la nueva sala está disponible
        addLobbyEventListener();
    }

    private void addRoomEventListener(){
        salaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Ingresar en la sala
                bCrearSala.setText("Crear Sala");
                bCrearSala.setEnabled(true);

                Intent intent = new Intent(getApplicationContext(),MultiTicTacToeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("nombreSala",nombreSala);
                if(statsSala.get(0) >= 0) {
                    intent.putExtra("numTies", statsSala.get(0));
                    intent.putExtra("numPlayer1Wins", statsSala.get(1));
                    intent.putExtra("numPlayer2Wins", statsSala.get(2));
                } else {
                    intent.putExtra("numTies", 0);
                    intent.putExtra("numPlayer1Wins", 0);
                    intent.putExtra("numPlayer2Wins", 0);
                }
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Error
                bCrearSala.setText("Crear Sala");
                bCrearSala.setEnabled(true);
                Toast.makeText(LobbyActivity.this, "¡Error al crear sala!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addLobbyEventListener(){
        lobbyRef = database.getReference("salas");
        lobbyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Mostrar la lista de las salas
                lobby.clear();
                Iterable<DataSnapshot> salas = snapshot.getChildren();
                for(DataSnapshot dss : salas){
                    lobby.add(dss.getKey());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(LobbyActivity.this, android.R.layout.simple_list_item_1, lobby);
                    lvSalas.setAdapter(adapter);
                    List<Integer> elem = new ArrayList<>();
                    elem.add(0,-1);
                    elem.add(1,-1);
                    elem.add(2,-1);
                    Iterable<DataSnapshot> sala = dss.getChildren();
                    for(DataSnapshot ss : sala){
                        System.out.println("sala.getKey(): "+ss.getKey());
                        if(ss.getKey().equals("stats")){
                            Iterable<DataSnapshot> statsDS = ss.getChildren();
                            for(DataSnapshot stt : statsDS){
                                System.out.println("stt.getKey(): "+stt.getKey());
                                if(stt.getKey().equals("empates")){
                                    elem.add(0,stt.getValue(Integer.class));
                                } else if(stt.getKey().equals("p1_gana")){
                                    elem.add(1,stt.getValue(Integer.class));
                                } else if(stt.getKey().equals("p2_gana")){
                                    elem.add(2,stt.getValue(Integer.class));
                                }
                            }
                        }
                    }
                    System.out.println("Elem: "+elem.toString());
                    statsLobby.add(elem);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //No hacer nada en caso de error
            }
        });
    }
}