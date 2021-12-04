package co.edu.unal.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername;
    Button bIngresar;
    Button bEliminar;
    Button bSingleP;

    String username = "";

    FirebaseDatabase database;
    DatabaseReference userRef;
    DatabaseReference usuariosRef;

    private SharedPreferences preferences;

    //Listeners
    ValueEventListener usersVEL;
    ValueEventListener userVEL;

    //Validar datos
    boolean mValidationResults = false;

    //Dialogos
    static final int DIALOG_ELIMINAR_USER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        bIngresar = findViewById(R.id.bIngresar);
        bEliminar = findViewById(R.id.bEliminar);
        bSingleP = findViewById(R.id.bSingleP);

        database = FirebaseDatabase.getInstance();


        //Verificar si el usuario existe y obtiene su referencia
        preferences = getSharedPreferences("user_prefs",0);
        username = preferences.getString("username","");

        usuariosRef = database.getReference("users");
        userRef = database.getReference("users/" + username);

        if(usersVEL!= null) {
            usuariosRef.removeEventListener(usersVEL);
        } else if(userVEL != null){
            userRef.removeEventListener(userVEL);
        } else{
            System.out.println("EventListener es nulo");
        }

        etUsername.setText(username);

        if(!username.equals("")){
            etUsername.setEnabled(false);
            bEliminar.setVisibility(View.VISIBLE);
            //userRef = database.getReference("users/" + username);
            //addEventListener();
            //userRef.setValue("");
        }

        addButtonsEventListeners();


    }

    //Dialogos
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch(id) {
            case DIALOG_ELIMINAR_USER:
                builder.setMessage("¿Seguro de eliminar este usuario, y su respectiva sala?")
                        .setCancelable(false)
                        .setNegativeButton(R.string.str_no, null)
                        .setPositiveButton(R.string.str_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences preferences = getSharedPreferences("user_prefs",0);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();

                                //Quitar el listener del usuario actual, si ya se ha creado
                                if(userVEL != null) {
                                    userRef.removeEventListener(userVEL);
                                }
                                usuariosRef.child(username).removeValue();
                                DatabaseReference salasRef = database.getReference("salas");
                                salasRef.child(username).removeValue();
                                etUsername.setEnabled(true);
                                etUsername.setText("");
                                bEliminar.setVisibility(View.INVISIBLE);
                            }
                        });
                dialog = builder.create();
                break;
        }
        return dialog;
    }

    private void addButtonsEventListeners(){
        bIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ingresando el usuario en el sistema
                username = etUsername.getText().toString();
                userRef = database.getReference("users/" + username);
                usersAddEventListener();
            }
        });

        bEliminar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ELIMINAR_USER);
                //startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                //finish();
                /*Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {

                    }
                }, 5000);*/
                //Intent intent = getIntent();
            }
        });

        bSingleP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AndroidTicTacToeActivity.class));
            }
        });
    }

    private void usersAddEventListener(){
        usersVEL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (preferences.getString("username", "").equals("")) { // Está en shared prefs?
                        if (!username.equals("")) {
                            if (snapshot.child(username).exists()) {
                                //mValidationResults = false;
                                //if (!mValidationResults) {
                                    etUsername.setText("");
                                    System.out.println("Ya existe un usuario con ese nombre");
                                    Toast.makeText(LoginActivity.this, "Ya existe un usuario con ese nombre", Toast.LENGTH_SHORT).show();
                                //}
                            } else {
                                //mValidationResults = true;
                                //userRef = database.getReference("users/" + username);
                                addEventListener();
                                userRef.setValue("");
                                Toast.makeText(LoginActivity.this, "El registro se realizó exitosamente", Toast.LENGTH_SHORT).show();
                                System.out.println("El registro se realizó exitosamente");
                            }
                        } else {
                            System.out.println("El username está vacío!");
                        }
                    } else { //Si ya está guardado en shared prefs, entonces dejarlo ingresar
                        if (snapshot.child(username).exists()) {
                            //mValidationResults = true;
                            //userRef = database.getReference("users/" + username);
                            addEventListener();
                            userRef.setValue("");
                            Toast.makeText(LoginActivity.this, "Sesión iniciada exitosamente", Toast.LENGTH_SHORT).show();
                            System.out.println("Sesión iniciada exitosamente");
                        }
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        usuariosRef.addValueEventListener(usersVEL);
    }



    private void addEventListener(){

        userVEL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Si fue exitoso, continuar a la siguiente actividad luego de guardar el username
                if(!username.equals("")){
                    SharedPreferences preferences = getSharedPreferences("user_prefs",0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("username",username);
                    editor.apply();

                    etUsername.setEnabled(false);
                    bEliminar.setVisibility(View.VISIBLE);

                    //Quitar el listener de los usuarios para evitar llamados innecesarios cuando se oprima el bEliminarD
                    usuariosRef.removeEventListener(usersVEL);

                    startActivity(new Intent(getApplicationContext(), LobbyActivity.class));
                    //finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Error
                bIngresar.setText("Ingresar");
                bIngresar.setEnabled(true);
                Toast.makeText(LoginActivity.this,"¡No se pudo ingresar!",Toast.LENGTH_SHORT).show();
            }
        };
        //Leer de la base de datos
        userRef.addValueEventListener(userVEL);
    }

}