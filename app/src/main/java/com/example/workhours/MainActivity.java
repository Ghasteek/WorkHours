package com.example.workhours;

import android.app.Application;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.workhours.data.ShiftsContract;
import com.example.workhours.data.ShiftsContract.ShiftEntry;
import com.example.workhours.data.ShiftsDbHelper;
import com.example.workhours.data.ShiftsProvider;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TimePickerDialog.OnTimeSetListener{    // pro time picker t5eba implementovat TimePickerDialog.OnTimeSetListener

    TextView pickedTimeIn,pickedTimeOut, thisMonthView,shiftsInfoView, overtimeThisMonthView, overtimeSumView;
    ProgressBar monthShifts;
    SharedPreferences pref, temp;
    Calendar calendar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
                                                                                                        // prichod picker
        Button showTimePickerIn = (Button) findViewById(R.id.showTimePickerIn);
        showTimePickerIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals.whichTime = "IN";
                DialogFragment timePickerIn = new TimePickerFragment();
                timePickerIn.show(getSupportFragmentManager(), "time in picker");
            }
        });

        Button showTimePickerOut = (Button) findViewById(R.id.showTimePickerOut);                             // odchod picker
        showTimePickerOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals.whichTime = "OUT";
                DialogFragment timePickerOut = new TimePickerFragment();
                timePickerOut.show(getSupportFragmentManager(), "time out picker");
            }
        });


        pref = getApplicationContext().getSharedPreferences("Settings", 0);             // definovani SharedPreference
        temp = getApplicationContext().getSharedPreferences("Temporary", 0);

        pickedTimeIn = (TextView) findViewById(R.id.pickedTimeInView);                               // definmování textových polí na hlavní stránce
        pickedTimeOut = (TextView) findViewById(R.id.pickedTimeOutView);
        thisMonthView = (TextView) findViewById(R.id.thisMonthViewId);
        shiftsInfoView = (TextView) findViewById(R.id.shiftsInfoViewId);
        overtimeThisMonthView = (TextView) findViewById(R.id.overtimeThisMonthViewId);
        overtimeSumView =  (TextView) findViewById(R.id.overtimeSumViewId);
        monthShifts = (ProgressBar) findViewById(R.id.monthShiftsProgress);

        calendar = Calendar.getInstance();

        String timeInStr = pickedTimeIn.getText().toString();                                           // prirazeni shared preference do globalnich promennych kvuli time pickeru
        String timeOutStr = pickedTimeOut.getText().toString();
        String[] timeInArray = timeInStr.split(":");
        String[] timeOutArray = timeOutStr.split(":");
        MainActivity.Globals.timeInHours = Integer.parseInt(timeInArray[0]);
        MainActivity.Globals.timeInMinutes = Integer.parseInt(timeInArray[1]);
        MainActivity.Globals.timeOutHours = Integer.parseInt(timeOutArray[0]);
        MainActivity.Globals.timeOutMinutes = Integer.parseInt(timeOutArray[1]);

        if (pref.contains("defaultInTime")){
            pickedTimeIn.setText(pref.getString("defaultInTime", ""));
        }
        if (pref.contains("defaultOutTime")){
            pickedTimeOut.setText(pref.getString("defaultOutTime", ""));
        }
        //displayDatabaseInfo();
        showInfo();
    }

    private void showInfo() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        String[] projection = {
                ShiftEntry.COLUMN_DATE,
                ShiftEntry.COLUMN_SHIFT_LENGHT,
                ShiftEntry.COLUMN_OVERTIME,
                ShiftEntry.COLUMN_HOLIDAY};

        String selection = ShiftEntry.COLUMN_HOLIDAY + "=? AND " + ShiftEntry.COLUMN_DATE + " LIKE ?";

        int monthLength = (int) (Math.log10(month) + 1);                                                    // logarytmicka metoda zjisteni poctu cifer v cisle
        String monthStr;
        if (monthLength == 1) {
            monthStr = "0" + (month + 1);
        } else { monthStr = String.valueOf(month); }

        String[] selectionArgs = new String[] { String.valueOf(ShiftEntry.HOLIDAY_SHIFT), String.valueOf(year) + monthStr + "%" };

        Cursor cursor = getContentResolver().query(
                ShiftsContract.ShiftEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        String [] monthArray = getResources().getStringArray(R.array.months);                           // nastavení popisku tohoto měsíce a roku
        int i = month;
        thisMonthView.setText(monthArray[i] + " " + String.valueOf(year));

        int overtimeIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_OVERTIME);
        int overTimeSumThisMonth = 0;

        try{
            int shiftsThisMonth = cursor.getCount();                                                        //nastavení popisku odpracovaných směn //TODO spočítat kolik je v měsíci pracovních dní
            shiftsInfoView.setText(getResources().getString(R.string.alreadyWorked) + " " + shiftsThisMonth);
            monthShifts.setMax(20);
            monthShifts.setProgress(shiftsThisMonth);

            while (cursor.moveToNext()){
                overTimeSumThisMonth = overTimeSumThisMonth + cursor.getInt(overtimeIndex);
            }
            overtimeThisMonthView.setText(getResources().getString(R.string.thisMonthOvertime) + " " + Tools.timeIntToStr(overTimeSumThisMonth) );
        } finally {
            cursor.close();
        }

        if (temp.contains("overtimeSum")){
            overtimeSumView.setText( getResources().getString(R.string.sumOfOvertime) + " " + Tools.timeIntToStr(temp.getInt("overtimeSum",0)) + "h");
        } else {overtimeSumView.setText( getResources().getString(R.string.sumOfOvertime) + " 0h");}
    }

    private void displayDatabaseInfo() {
        String[] projection = {
                ShiftEntry.COLUMN_DATE,
                ShiftEntry.COLUMN_ARRIVAL,
                ShiftEntry.COLUMN_DEPARTURE,
                ShiftEntry.COLUMN_BREAK_LENGHT,
                ShiftEntry.COLUMN_SHIFT_LENGHT,
                ShiftEntry.COLUMN_OVERTIME,
                ShiftEntry.COLUMN_HOLIDAY};

        String selection = ShiftEntry.COLUMN_HOLIDAY + "=?";

        String[] selectionArgs = new String[] { String.valueOf(ShiftEntry.HOLIDAY_SHIFT) };

        Cursor cursor = getContentResolver().query(
                ShiftEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        TextView displayView = (TextView) findViewById(R.id.text_view_table);

        int dateColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_DATE);
        int arrivalColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_ARRIVAL);
        int departureColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_DEPARTURE);
        int breakColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_BREAK_LENGHT);
        int shiftColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_SHIFT_LENGHT);
        int overtimeColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_OVERTIME);
        int holidayColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_HOLIDAY);

        try {
            displayView.setText("Number of rows in shifts database table: " + cursor.getCount() + "\n\n");
            displayView.append(ShiftEntry.COLUMN_DATE + " - " +
                    ShiftEntry.COLUMN_ARRIVAL + " - " +
                    ShiftEntry.COLUMN_DEPARTURE + " - " +
                    ShiftEntry.COLUMN_BREAK_LENGHT + " - " +
                    ShiftEntry.COLUMN_SHIFT_LENGHT + " - " +
                    ShiftEntry.COLUMN_OVERTIME + " - " +
                    ShiftEntry.COLUMN_HOLIDAY + "/n");                                                  // vypsani jmen sloupcu

            while (cursor.moveToNext()){                                                                // vypsani obsahu cursoru
                int date = cursor.getInt(dateColumnIndex);
                int arrival = cursor.getInt(arrivalColumnIndex);
                int departure = cursor.getInt(departureColumnIndex);
                int breakLenght = cursor.getInt(breakColumnIndex);
                int shift = cursor.getInt(shiftColumnIndex);
                int overtime = cursor.getInt(overtimeColumnIndex);
                int holiday = cursor.getInt(holidayColumnIndex);

                displayView.append(("\n" + date + " - " + arrival + " - " + departure + " - " + breakLenght + " - " + shift + " - " + overtime + " - " + holiday));
            }
        } finally {
            cursor.close();
        }

        if (temp.contains("overtimeSum")){
            overtimeSumView.setText(" overtime   " + Tools.timeIntToStr(temp.getInt("overtimeSum",0)));
        } else {overtimeSumView.setText("0");}
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (Globals.whichTime == "IN"){
            TextView timeInView = (TextView) findViewById(R.id.pickedTimeInView);
            int minLength = (int) (Math.log10(minute) + 1);                                                     // logarytmicka metoda zjisteni poctu cifer v cisle
            if (minLength == 2){
                timeInView.setText(hourOfDay + ":" + minute);
            } else {timeInView.setText(hourOfDay + ":0" + minute);}
            Globals.timeInHours = hourOfDay;
            Globals.timeInMinutes = minute;
        } else {
            TextView timeOutView = (TextView) findViewById(R.id.pickedTimeOutView);
            int minLength = (int) (Math.log10(minute) + 1);
            if (minLength == 2){
                timeOutView.setText(hourOfDay + ":" + minute);
            } else {timeOutView.setText(hourOfDay + ":0" + minute);}
            int pause = pref.getInt("defaultPause", 0);
            int calculatedTime = (((hourOfDay * 60) + minute)) - (((Globals.timeInHours * 60) + Globals.timeInMinutes)) - pause;  // vypocet odpracovanych minut, (odchod - prichod) - pauza
            int hours = calculatedTime / 60;                                                                                    // vypocet hodin
            int minutes = (calculatedTime - (hours * 60));                                                                      // vypocet minut
            TextView calculatedView = (TextView) findViewById(R.id.calculatedTimeView);
            int minLength2 = (int) (Math.log10(minutes) + 1);
            if (minLength2 == 2){
                calculatedView.setText(hours + ":" + minutes);
            } else {calculatedView.setText(hours + ":0" + minutes);}
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        showInfo();
        //displayDatabaseInfo();
    }
                                                                                                        // po sem je definice buttonu a jeho onclickListeneru + onTimeSet abz vratil cas
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (item.getItemId()){                                                                     // akce po kliknutí na Settings z menu/main
            case R.id.action_settings:
                Intent settings = new Intent(MainActivity.this, Settings.class);
                startActivity(settings);
                return true;
            case R.id.action_dummyData:
                insertDummyData();
                showInfo();
                //displayDatabaseInfo();
                return true;
            case R.id.action_add:
                Intent addIntent = new Intent(MainActivity.this, Shift.class);
                startActivity(addIntent);
                return true;
            case R.id.action_shiftsTable:
                Intent ShiftTable = new Intent(MainActivity.this, ShiftTable.class);
                startActivity(ShiftTable);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            Intent i = new Intent(MainActivity.this, Settings.class);                   // volani aktivity
            startActivity(i);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void insertDummyData (){
        ContentValues shiftValues = new ContentValues();
            shiftValues.put(ShiftEntry.COLUMN_DATE, 20190302);
            shiftValues.put(ShiftEntry.COLUMN_ARRIVAL, 360);
            shiftValues.put(ShiftEntry.COLUMN_DEPARTURE, 915);
            shiftValues.put(ShiftEntry.COLUMN_BREAK_LENGHT, 30);
            shiftValues.put(ShiftEntry.COLUMN_SHIFT_LENGHT, 525);
            shiftValues.put(ShiftEntry.COLUMN_OVERTIME, 45);
            shiftValues.put(ShiftEntry.COLUMN_HOLIDAY, ShiftEntry.HOLIDAY_SHIFT);
        Uri newUri = getContentResolver().insert(ShiftEntry.CONTENT_URI, shiftValues);
        if (newUri == null) {
            Toast.makeText(this, getText(R.string.editor_insert_shift_failed), Toast.LENGTH_SHORT).show();
        } else {Toast.makeText(this, getText(R.string.editor_insert_shift_successful), Toast.LENGTH_SHORT).show();}
    }


    public static class Globals {
        public static String whichTime;
        public static int timeInHours;
        public static int timeInMinutes;
        public static int timeOutHours;
        public static int timeOutMinutes;
    }
}