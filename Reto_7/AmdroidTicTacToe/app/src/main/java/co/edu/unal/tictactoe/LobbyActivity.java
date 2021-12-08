package co.edu.unal.tictactoe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.google.firebase.database.ChildEventListener;
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
    List<String> lobbyStrings;

    String username = "";
    String nombreSala = "";

    FirebaseDatabase database;

    DatabaseReference salaRef;
    DatabaseReference lobbyRef;

    ValueEventListener salaVEL;
    ValueEventListener lobbyVEL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        database = FirebaseDatabase.getInstance();

        //Obtener el username y asignarle a su nombre de Sala su username
        SharedPreferences preferences = getSharedPreferences("user_prefs",0);
        username = preferences.getString("username","");
        nombreSala = username;

        setTitle("Salas para " + nombreSala); //Título de la sala

        lvSalas = findViewById(R.id.lvSalas);
        bCrearSala = findViewById(R.id.bCrearSala);

        clearListeners();

        //Todas las salas disponibles existentes
        lobby = new ArrayList<>();
        lobbyStrings = new ArrayList<>();

        bCrearSala.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Crear la sala y añadir al usuario actual como jugador 1
                bCrearSala.setText("Creando Sala...");
                bCrearSala.setEnabled(false);
                nombreSala = username;
                salaRef = database.getReference("salas/" + nombreSala + "/jugador1");
                salaRef.setValue(username);
                addRoomEventListener();
            }
        });

        lvSalas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Unirse a una sala previamente creada y añadir el usuario actual como jugador 2
                nombreSala = lobby.get(position);
                System.out.println("nombreSala: "+nombreSala);
                if(nombreSala.equals(username)) {
                    salaRef = database.getReference("salas/" + nombreSala + "/jugador1");
                } else {
                    salaRef = database.getReference("salas/" + nombreSala + "/jugador2");
                }
                salaRef.setValue(username);
                addRoomEventListener();
            }
        });

        //Mostrar si la nueva sala está disponible
        addLobbyEventListener();
    }

    /*@Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }*/

    private void clearListeners(){
        if(salaVEL != null) salaRef.removeEventListener(salaVEL);
        if(lobbyVEL != null) lobbyRef.removeEventListener(lobbyVEL);
    }

    private void addRoomEventListener(){
        salaVEL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Ingresar en la sala
                if(snapshot.exists()) {
                    if (!snapshot.getValue(String.class).equals("")) { //Si no está saliendo de una sala
                        Intent intent = new Intent(getApplicationContext(), MultiTicTacToeActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("nombreSala", nombreSala);
                        if(lobbyStrings.contains(nombreSala)) intent.putExtra("salaCerrada",true);
                        startActivity(intent);
                    }
                    bCrearSala.setText("Crear Sala");
                    bCrearSala.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Error
                bCrearSala.setText("Crear Sala");
                bCrearSala.setEnabled(true);
                Toast.makeText(LobbyActivity.this, "¡Error al crear sala!", Toast.LENGTH_SHORT).show();
            }
        };
        salaRef.addValueEventListener(salaVEL);
    }

    private void addLobbyEventListener(){
        lobbyRef = database.getReference("salas");

        lobbyVEL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Mostrar la lista de las salas
                lobby.clear();
                lobbyStrings.clear();

                Iterable<DataSnapshot> salas = snapshot.getChildren();
                for(DataSnapshot sala : salas){
                    boolean check_if_host_online = false; //Mirar si el host se encuentra en la sala
                    //lobby.add(sala.getKey());
                    Iterable<DataSnapshot> itemsJuego = sala.getChildren();
                    for(DataSnapshot item : itemsJuego){
                        if(item.getKey().equals("jugador1")){
                            if(!item.getValue(String.class).equals("")){
                                check_if_host_online = true;

                            }
                        }
                    }

                    lobby.add(sala.getKey());
                    if(check_if_host_online){
                        lobbyStrings.add(sala.getKey() + " (Online)");
                    } else{
                        lobbyStrings.add(sala.getKey());
                    }

                    ArrayAdapter<String> adapter;
                    adapter = new ArrayAdapter<>(LobbyActivity.this, android.R.layout.simple_list_item_1, lobbyStrings);
                    lvSalas.setAdapter(adapter);

                    if(sala.getKey().equals(username)){
                        bCrearSala.setText("Ir a mi Sala");
                        bCrearSala.setEnabled(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //No hacer nada en caso de error
            }
        };

        lobbyRef.addValueEventListener(lobbyVEL);
    }
}