package co.edu.unal.miapenahu.contactos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Notebook.db";
    public static final String CONTACTS_TABLE_NAME = "contacts";
    public static final String CONTACTS_COLUMN_ID = "id";
    public static final String CONTACTS_COLUMN_NAME = "name";
    public static final String CONTACTS_COLUMN_URL = "url";
    public static final String CONTACTS_COLUMN_PHONE = "phone";
    public static final String CONTACTS_COLUMN_EMAIL = "email";
    public static final String CONTACTS_COLUMN_PRODUCTS = "products";
    public static final String CONTACTS_COLUMN_ISCONSULTANCY = "isconsultancy";
    public static final String CONTACTS_COLUMN_ISDEVELOPMENT = "isdevelopment";
    public static final String CONTACTS_COLUMN_ISFABRIC = "isfabric";

    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table contacts " +
                        "(id integer primary key, name text, url text, phone text,email text, products text,place text, isconsultancy integer, isdevelopment integer, isfabric integer)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS contacts");
        onCreate(db);
    }

    public boolean insertContact (String name, String url, String phone, String email, String products, int isconsultancy, int isdevelopment, int isfabric) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("url", url);
        contentValues.put("phone", phone);
        contentValues.put("email", email);
        contentValues.put("products", products);
        contentValues.put("isconsultancy", isconsultancy);
        contentValues.put("isdevelopment", isdevelopment);
        contentValues.put("isfabric", isfabric);
        db.insert("contacts", null, contentValues);
        return true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+CONTACTS_TABLE_NAME+" where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, CONTACTS_TABLE_NAME);
        return numRows;
    }

    public boolean updateContact (Integer id, String name, String url, String phone, String email, String products, int isconsultancy, int isdevelopment, int isfabric){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("url", url);
        contentValues.put("phone", phone);
        contentValues.put("email", email);
        contentValues.put("products", products);
        contentValues.put("isconsultancy", isconsultancy);
        contentValues.put("isdevelopment", isdevelopment);
        contentValues.put("isfabric", isfabric);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteContact (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("contacts",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    @SuppressLint("Range")
    public Pair<ArrayList<String>,ArrayList<Integer>> getAllContacts() {
        ArrayList<String> array_list = new ArrayList<String>();
        ArrayList<Integer> index_list = new ArrayList<>();
        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts", null );
        res.moveToFirst();

        while(res.isAfterLast() == false) {
            //System.out.println("col index: "+res.getString(res.getColumnIndex(CONTACTS_COLUMN_ID)));
            index_list.add(Integer.parseInt(res.getString(res.getColumnIndex(CONTACTS_COLUMN_ID))));
            array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)));
            res.moveToNext();
        }
        return new Pair<>(array_list,index_list);
    }

    @SuppressLint("Range")
    public Pair<ArrayList<String>,ArrayList<Integer>> getContactsFiltered(String column, String name, boolean filterConsultancy, boolean filterDevelopment, boolean filterFabric) {
        ArrayList<String> array_list = new ArrayList<String>();
        ArrayList<Integer> index_list = new ArrayList<>();
        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "select * from contacts";
        String where = "";
        if(filterConsultancy) where += " " + CONTACTS_COLUMN_ISCONSULTANCY + "== 1";
        if(filterDevelopment) {
            if (!where.isEmpty()) where += " and";
            where += " " + CONTACTS_COLUMN_ISDEVELOPMENT + "== 1";
        }
        if(filterFabric) {
            if (!where.isEmpty()) where += " and";
            where += " " + CONTACTS_COLUMN_ISFABRIC + "== 1";
        }


        if(!column.isEmpty() && !name.isEmpty()){
            if(!where.isEmpty()) where += " and";
            where += " " + column + " like '%" + name + "%'";
            query += " where";
        } else {
            if(!where.isEmpty()) query += " where";
        }

        query += where;
        System.out.println(query);

        Cursor res =  db.rawQuery( query, null );
        res.moveToFirst();

        while(res.isAfterLast() == false) {
            //System.out.println("col index: "+res.getString(res.getColumnIndex(CONTACTS_COLUMN_ID)));
            index_list.add(Integer.parseInt(res.getString(res.getColumnIndex(CONTACTS_COLUMN_ID))));
            array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)));
            res.moveToNext();
        }
        return new Pair<>(array_list,index_list);
    }
}