package com.workhours.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.workhours.data.ShiftsContract.ShiftEntry;

@SuppressWarnings("WeakerAccess")
public class ShiftsDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "WorkHours.db";


    public ShiftsDbHelper (Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //definovani promenne SQL_CREATE_ENTRIES pro vytvoreni tabulky
        String SQL_CREATE_SHIFTS_TABLE =
                "CREATE TABLE " + ShiftEntry.TABLE_NAME + " ("
                        + ShiftEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + ShiftEntry.COLUMN_DATE + " INTEGER, "
                        + ShiftEntry.COLUMN_ARRIVAL + " INTEGER, "
                        + ShiftEntry.COLUMN_DEPARTURE + " INTEGER, "
                        + ShiftEntry.COLUMN_BREAK_LENGTH + " INTEGER, "
                        + ShiftEntry.COLUMN_SHIFT_LENGTH + " INTEGER, "
                        + ShiftEntry.COLUMN_OVERTIME + " INTEGER, "
                        + ShiftEntry.COLUMN_HOLIDAY + " INTEGER);";

        db.execSQL(SQL_CREATE_SHIFTS_TABLE);

        String SQL_CREATE_MONTHS_TABLE =
                "CREATE TABLE " + ShiftEntry.TABLE_MONTHS_NAME + " ("
                        + ShiftEntry._ID_MONTHS + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + ShiftEntry.COLUMN_DATE_MONTHS + " INTEGER, "
                        + ShiftEntry.COLUMN_OVERTIMESUM_MONTHS + " INTEGER);";

        db.execSQL(SQL_CREATE_MONTHS_TABLE);
        }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        //TODO dokoncit on upgrade classu
    }
}
