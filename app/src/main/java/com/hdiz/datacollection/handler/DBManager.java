package com.hdiz.datacollection.handler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.hdiz.datacollection.objects.Patient;

public class DBManager {

    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String patient_str, String qrcode, String server_id, String date_time,
                       String is_sync, String visit_str, String thermal, String visible) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.PATIENT, patient_str);
        contentValue.put(DatabaseHelper.QR, qrcode);
        contentValue.put(DatabaseHelper.SERVER_ID, server_id);
        contentValue.put(DatabaseHelper.DATE_TIME, date_time);
        contentValue.put(DatabaseHelper.IS_SYNC, is_sync);
        contentValue.put(DatabaseHelper.VISIT, visit_str);
        contentValue.put(DatabaseHelper.THERMAL, thermal);
        contentValue.put(DatabaseHelper.VISIBLE, visible);

        database.insert(DatabaseHelper.TABLE_NAME, null, contentValue);
    }

    public int update(String patient_str, String qrcode, String server_id,
                      String date_time, String is_sync, String visit_str, String thermal, String visible) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.PATIENT, patient_str);
        contentValues.put(DatabaseHelper.SERVER_ID, server_id);
        contentValues.put(DatabaseHelper.DATE_TIME, date_time);
        contentValues.put(DatabaseHelper.IS_SYNC, is_sync);
        contentValues.put(DatabaseHelper.VISIT, visit_str);
        contentValues.put(DatabaseHelper.THERMAL, thermal);
        contentValues.put(DatabaseHelper.VISIBLE, visible);
        int i = database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper.QR  + "= ?", new String[] {qrcode});
        return i;
    }

    public void delete(long _id) {
        database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper._ID + "=" + _id, null);
    }
    public Cursor searachQrcode(String qrcode){
        Cursor cur = database.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME + " WHERE " +
                DatabaseHelper.QR + "='" + qrcode + "'", null);

        return  cur;
    }

    public String searchPatient(String qrcode) {
        String patient_str = null;
        Cursor cur = database.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME + " WHERE " + DatabaseHelper.QR + "='" + qrcode + "'", null);

        if (cur.moveToFirst()) {
            do {
                patient_str = cur.getString(1);
            } while (cur.moveToNext());
        }
        cur.close();

        return patient_str;
    }

    public Cursor SyncablePatient(String isSync) {

        Cursor cur = database.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME + " WHERE " + DatabaseHelper.IS_SYNC + "='" + isSync + "'", null);

        if (cur != null) {
            cur.moveToFirst();
        }

        return cur;
    }
    public Cursor queueAll() {

        String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_NAME;
        Cursor cursor = database.rawQuery(selectQuery, null);
        return cursor;
    }

}