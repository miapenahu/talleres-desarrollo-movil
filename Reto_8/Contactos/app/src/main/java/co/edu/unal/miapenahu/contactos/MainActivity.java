package co.edu.unal.miapenahu.contactos;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
//import android.support.v7.app.ActionBarActivity;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "MESSAGE";
    private ListView lvCompanies;
    private SearchView svCompanies;
    private Button bConsultancyFilter;
    private Button bDevelopmentFilter;
    private Button bFabricFilter;
    //Filtros
    private String searchText = "";
    private boolean filterConsultancy = false;
    private boolean filterDevelopment = false;
    private boolean filterFabric = false;
    //Listas
    private ArrayList<String> names_list;
    private ArrayList<Integer> index_list;
    DBHelper mydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bConsultancyFilter = findViewById(R.id.bConsultancyFilter);
        bConsultancyFilter.setOnClickListener(filtersListener());
        bDevelopmentFilter = findViewById(R.id.bDevelopmentFilter);
        bDevelopmentFilter.setOnClickListener(filtersListener());
        bFabricFilter = findViewById(R.id.bFabricFilter);
        bFabricFilter.setOnClickListener(filtersListener());

        mydb = new DBHelper(this);
        lvCompanies = (ListView)findViewById(R.id.listView1);

        setNewListViewAdapter("","");

        svCompanies = findViewById(R.id.searchViewCompanies);
        svCompanies.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText;
                setNewListViewAdapter(DBHelper.CONTACTS_COLUMN_NAME, newText);
                return false;
            }
        });

        lvCompanies.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
                // TODO Auto-generated method stub
                int id_To_Search = index_list.get(arg2);

                //System.out.println("desired index: "+ index_list.get(arg2));

                Bundle dataBundle = new Bundle();
                dataBundle.putInt("id", id_To_Search);

                Intent intent = new Intent(getApplicationContext(),DisplayContact.class);

                intent.putExtras(dataBundle);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);

        switch(item.getItemId()) {
            case R.id.item1:Bundle dataBundle = new Bundle();
                dataBundle.putInt("id",0);

                Intent intent = new Intent(getApplicationContext(),DisplayContact.class);
                intent.putExtras(dataBundle);

                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keycode, event);
    }

    private View.OnClickListener filtersListener(){
        View.OnClickListener ocl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()) {
                    case R.id.bConsultancyFilter:
                        if(filterConsultancy) {
                            v.setBackgroundColor(Color.rgb(163, 163, 163));
                            filterConsultancy = !filterConsultancy;
                        } else {
                            v.setBackgroundColor(Color.rgb(1,135,134));
                            filterConsultancy = !filterConsultancy;
                        }
                        setNewListViewAdapter(DBHelper.CONTACTS_COLUMN_NAME,searchText);
                        break;
                    case R.id.bDevelopmentFilter:
                        if(filterDevelopment) {
                            v.setBackgroundColor(Color.rgb(163, 163, 163));
                            filterDevelopment = !filterDevelopment;
                        } else {
                            v.setBackgroundColor(Color.rgb(1,135,134));
                            filterDevelopment = !filterDevelopment;
                        }
                        setNewListViewAdapter(DBHelper.CONTACTS_COLUMN_NAME,searchText);
                        break;
                    case R.id.bFabricFilter:
                        if(filterFabric) {
                            v.setBackgroundColor(Color.rgb(163, 163, 163));
                            filterFabric = !filterFabric;
                        } else {
                            v.setBackgroundColor(Color.rgb(1,135,134));
                            filterFabric = !filterFabric;
                        }
                        setNewListViewAdapter(DBHelper.CONTACTS_COLUMN_NAME,searchText);
                        break;
                }
            }
        };
        return ocl;
    }

    private void setNewListViewAdapter(String column, String name){
        Pair<ArrayList<String>,ArrayList<Integer>> pair = mydb.getContactsFiltered(column, name, filterConsultancy, filterDevelopment, filterFabric);
        names_list = pair.first;
        index_list = pair.second;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, names_list);
        lvCompanies.setAdapter(adapter);
    }
}