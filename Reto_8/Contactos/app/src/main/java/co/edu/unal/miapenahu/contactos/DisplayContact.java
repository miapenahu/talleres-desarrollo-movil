package co.edu.unal.miapenahu.contactos;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayContact extends AppCompatActivity {
    int from_Where_I_Am_Coming = 0;
    private DBHelper mydb ;

    TextView name ;
    TextView url;
    TextView phone;
    TextView email;
    TextView products;
    CheckBox consultancy;
    CheckBox development;
    CheckBox fabric;
    int id_To_Update = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_contact);
        name = (TextView) findViewById(R.id.editTextName);
        url = (TextView) findViewById(R.id.editTextURL);
        phone = (TextView) findViewById(R.id.editTextPhone);
        email = (TextView) findViewById(R.id.editTextEmail);
        products = (TextView) findViewById(R.id.editTextProducts);
        //Checkboxes
        consultancy = findViewById(R.id.cbConsultancy);
        consultancy.setOnClickListener(CheckBoxListener());
        development = findViewById(R.id.cbDevelopment);
        development.setOnClickListener(CheckBoxListener());
        fabric = findViewById(R.id.cbFabric);
        fabric.setOnClickListener(CheckBoxListener());

        mydb = new DBHelper(this);

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            int Value = extras.getInt("id");
            System.out.println("value: "+Value);
            if(Value>0){
                //means this is the view part not the add contact part.
                Cursor rs = mydb.getData(Value);
                id_To_Update = Value;
                rs.moveToFirst();

                @SuppressLint("Range") String tName = rs.getString(rs.getColumnIndex(DBHelper.CONTACTS_COLUMN_NAME));
                @SuppressLint("Range") String tUrl = rs.getString(rs.getColumnIndex(DBHelper.CONTACTS_COLUMN_URL));
                @SuppressLint("Range") String tPhone = rs.getString(rs.getColumnIndex(DBHelper.CONTACTS_COLUMN_PHONE));
                @SuppressLint("Range") String tEmail = rs.getString(rs.getColumnIndex(DBHelper.CONTACTS_COLUMN_EMAIL));
                @SuppressLint("Range") String tProducts = rs.getString(rs.getColumnIndex(DBHelper.CONTACTS_COLUMN_PRODUCTS));
                @SuppressLint("Range") int tConsultancy = rs.getInt(rs.getColumnIndex(DBHelper.CONTACTS_COLUMN_ISCONSULTANCY));
                @SuppressLint("Range") int tDevelopment = rs.getInt(rs.getColumnIndex(DBHelper.CONTACTS_COLUMN_ISDEVELOPMENT));
                @SuppressLint("Range") int tFabric = rs.getInt(rs.getColumnIndex(DBHelper.CONTACTS_COLUMN_ISFABRIC));

                //System.out.println("tC: "+ tConsultancy+", tD: "+ tDevelopment+", tF: "+tFabric);

                if (!rs.isClosed())  {
                    rs.close();
                }
                Button b = (Button)findViewById(R.id.button1);
                b.setVisibility(View.INVISIBLE);

                name.setText((CharSequence)tName);
                name.setFocusable(false);
                name.setClickable(false);

                setTitle(getResources().getString(R.string.company) +": " + tName);

                url.setText((CharSequence)tUrl);
                url.setFocusable(false);
                url.setClickable(false);

                phone.setText((CharSequence)tPhone);
                phone.setFocusable(false);
                phone.setClickable(false);

                email.setText((CharSequence)tEmail);
                email.setFocusable(false);
                email.setClickable(false);

                products.setText((CharSequence)tProducts);
                products.setFocusable(false);
                products.setClickable(false);

                consultancy.setChecked(!(tConsultancy == 0));
                consultancy.setFocusable(false);
                consultancy.setClickable(false);

                development.setChecked(!(tDevelopment == 0));
                development.setFocusable(false);
                development.setClickable(false);

                fabric.setChecked(!(tFabric == 0));
                fabric.setFocusable(false);
                fabric.setClickable(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Bundle extras = getIntent().getExtras();

        if(extras !=null) {
            int Value = extras.getInt("id");
            if(Value>0){
                getMenuInflater().inflate(R.menu.display_contact, menu);
            } else{
                getMenuInflater().inflate(R.menu.main_menu, menu);
            }
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case R.id.Edit_Contact:
                Button b = (Button)findViewById(R.id.button1);
                b.setVisibility(View.VISIBLE);
                name.setEnabled(true);
                name.setFocusableInTouchMode(true);
                name.setClickable(true);

                url.setEnabled(true);
                url.setFocusableInTouchMode(true);
                url.setClickable(true);

                phone.setEnabled(true);
                phone.setFocusableInTouchMode(true);
                phone.setClickable(true);

                email.setEnabled(true);
                email.setFocusableInTouchMode(true);
                email.setClickable(true);

                products.setEnabled(true);
                products.setFocusableInTouchMode(true);
                products.setClickable(true);

                consultancy.setEnabled(true);
                consultancy.setFocusable(true);
                consultancy.setClickable(true);

                development.setEnabled(true);
                development.setFocusable(true);
                development.setClickable(true);

                fabric.setEnabled(true);
                fabric.setFocusable(true);
                fabric.setClickable(true);

                return true;
            case R.id.Delete_Contact:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.deleteContact)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mydb.deleteContact(id_To_Update);
                                Toast.makeText(getApplicationContext(), R.string.deleted_success,
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                AlertDialog d = builder.create();
                d.setTitle(R.string.titleDeleteDialog);
                d.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }


    public void run(View view) {
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            int Value = extras.getInt("id");
            if(Value>0){
                if(mydb.updateContact(id_To_Update,name.getText().toString(), url.getText().toString(),
                        phone.getText().toString(), email.getText().toString(), products.getText().toString(),
                        consultancy.isChecked()? 1:0, development.isChecked()? 1:0, fabric.isChecked()? 1:0)){
                    Toast.makeText(getApplicationContext(), R.string.updated_success, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                } else{
                    Toast.makeText(getApplicationContext(), R.string.updated_failure, Toast.LENGTH_SHORT).show();
                }
            } else{
                if(mydb.insertContact(name.getText().toString(), url.getText().toString(),
                        phone.getText().toString(), email.getText().toString(), products.getText().toString(),
                        consultancy.isChecked()? 1:0, development.isChecked()? 1:0, fabric.isChecked()? 1:0)){
                    Toast.makeText(getApplicationContext(), R.string.created_success,
                            Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(getApplicationContext(), R.string.created_failure,
                            Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        }
    }

    private View.OnClickListener CheckBoxListener(){
        View.OnClickListener ocl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.cbConsultancy:
                        if (consultancy.isChecked())
                            //Toast.makeText(getApplicationContext(), "Android", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
        return ocl;
    }

}