package co.edu.unal.tictactoe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

    String username = "";

    FirebaseDatabase database;
    DatabaseReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        bIngresar = findViewById(R.id.bIngresar);

        database = FirebaseDatabase.getInstance();

        //Verificar si el usuario existe y obtiene su referencia
        SharedPreferences preferences = getSharedPreferences("user_prefs",0);
        username = preferences.getString("username","");
        if(!username.equals("")){
            userRef = database.getReference("users/" + username);
            addEventListener();
            userRef.setValue("");
        }

        bIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ingresando el usuario en el sistema
                username = etUsername.getText().toString();
                etUsername.setText("");
                if(!username.equals("")){
                    bIngresar.setText("Ingresando...");
                    bIngresar.setEnabled(false);
                    userRef = database.getReference("users/" + username);
                    addEventListener();
                    userRef.setValue("");
                }
            }
        });
    }

    private void addEventListener(){
        //Leer de la base de datos
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Si fue exitoso, continuar a la siguiente actividad luego de guardar el username
                if(!username.equals("")){
                    SharedPreferences preferences = getSharedPreferences("user_prefs",0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("username",username);
                    editor.apply();

                    startActivity(new Intent(getApplicationContext(), LobbyActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Error
                bIngresar.setText("Ingresar");
                bIngresar.setEnabled(true);
                Toast.makeText(LoginActivity.this,"¡No se pudo ingresar!",Toast.LENGTH_SHORT).show();
            }
        });
    }

}