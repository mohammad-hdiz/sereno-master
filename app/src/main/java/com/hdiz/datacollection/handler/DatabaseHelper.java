package com.hdiz.datacollection.handler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Table Name
    public static final String TABLE_NAME = "INDIVIDUALS";

    // Table columns
    public static final String _ID = "_id";
    public static final String PATIENT = "patient_str";
    public static final String QR = "qrcode";
    public static final String SERVER_ID = "server_id";
    public static final String DATE_TIME = "date_time";
    public static final String IS_SYNC = "is_sync";
    public static final String VISIT = "visit_str";
    public static final String THERMAL = "thermal_path";
    public static final String VISIBLE = "visible_path";

    // Database Information
    static final String DB_NAME = "SERENO";

    // database version
    static final int DB_VERSION = 1;

    // Creating table query
    private static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PATIENT + " TEXT NOT NULL, " + QR + " TEXT NOT NULL, " + SERVER_ID+ " TEXT NOT NULL, " + DATE_TIME + " TEXT NOT NULL, " + IS_SYNC+ " TEXT NOT NULL , " + VISIT + " TEXT NOT NULL , " + THERMAL + " TEXT NOT NULL, " + VISIBLE + " TEXT NOT NULL);";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
