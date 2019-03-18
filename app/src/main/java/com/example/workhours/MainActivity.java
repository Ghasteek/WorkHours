package com.example.workhours;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TimePickerDialog.OnTimeSetListener{    // pro time picker třeba implementovat TimePickerDialog.OnTimeSetListener

    TextView todayArrivalInfo,todayDepartureInfo, thisMonthView,shiftsInfoView, overtimeThisMonthView, overtimeSumView;
    ProgressBar monthShifts;
    SharedPreferences pref, temp;
    Calendar calendar;
    ImageButton showTimePickerIn, showTimePickerOut, editTodayButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

                                                                                                        // vypnuti floating action buttonu
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        editTodayButton = (ImageButton) findViewById(R.id.editTodayButtonId);
        editTodayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addIntent = new Intent(MainActivity.this, Shift.class);
                Uri editTodayUri = Uri.parse(temp.getString("incompleteUri", ""));
                addIntent.setData(editTodayUri);
                startActivity(addIntent);
                Globals.isEdited = true;
            }
        });
                                                                                                        // prichod picker
        showTimePickerIn = (ImageButton) findViewById(R.id.arriveButtonId);
        showTimePickerIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals.whichTime = "IN";
                DialogFragment timePickerIn = new TimePickerFragment();
                timePickerIn.show(getSupportFragmentManager(), "time in picker");
            }
        });

        showTimePickerOut = (ImageButton) findViewById(R.id.departureButtonId);               // odchod picker
        showTimePickerOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(temp.contains("incompleteUri")) {
                    Globals.whichTime = "OUT";
                    DialogFragment timePickerOut = new TimePickerFragment();
                    timePickerOut.show(getSupportFragmentManager(), "time out picker");
                } else {Toast.makeText(getApplicationContext(), getResources().getString(R.string.arrivalNeeded), Toast.LENGTH_LONG).show();}
            }
        });


        pref = getApplicationContext().getSharedPreferences("Settings", 0);             // definovani SharedPreference
        temp = getApplicationContext().getSharedPreferences("Temporary", 0);

        todayArrivalInfo = (TextView) findViewById(R.id.todayArrivalInfoId);                               // definmování textových polí na hlavní stránce
        todayDepartureInfo = (TextView) findViewById(R.id.todayDepartureInfoId);

        thisMonthView = (TextView) findViewById(R.id.thisMonthViewId);
        shiftsInfoView = (TextView) findViewById(R.id.shiftsInfoViewId);
        overtimeThisMonthView = (TextView) findViewById(R.id.overtimeThisMonthViewId);
        overtimeSumView =  (TextView) findViewById(R.id.overtimeSumViewId);
        monthShifts = (ProgressBar) findViewById(R.id.monthShiftsProgress);
        editTodayButton = (ImageButton) findViewById(R.id.editTodayButtonId);

        calendar = Calendar.getInstance();

        String[] timeInArray = pref.getString("defaultInTime", "").split(":");
        String[] timeOutArray = pref.getString("defaultOutTime", "").split(":");
        MainActivity.Globals.timeInHours = Integer.parseInt(timeInArray[0]);
        MainActivity.Globals.timeInMinutes = Integer.parseInt(timeInArray[1]);
        MainActivity.Globals.timeOutHours = Integer.parseInt(timeOutArray[0]);
        MainActivity.Globals.timeOutMinutes = Integer.parseInt(timeOutArray[1]);

        checkYesterday();
        showInfo();


        if (temp.contains("arrivalTime")) {
            editTodayButton.setVisibility(View.VISIBLE);
        } else {
            editTodayButton.setVisibility(View.INVISIBLE);
            showTimePickerIn.setEnabled(true);
            showTimePickerOut.setEnabled(true);
        }
    }

    private void checkYesterday() {
        Date today = calendar.getTime();
        int todayInt = Tools.dateDateToInt(today);
        //TODO zjistit kolik je dnu od posledniho zaznamu

        String[] projection = {ShiftEntry.COLUMN_DATE,};

        String sortOrder = ShiftEntry.COLUMN_DATE + " DESC LIMIT 1";

        Cursor cursor = getContentResolver().query(
                ShiftEntry.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder);

        int dateColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_DATE);
        TextView displayView = (TextView) findViewById(R.id.text_view_table);
        int lastDbDate = 0, dateTemp, differenceDays;
        try {
            displayView.setText(ShiftEntry.COLUMN_DATE + " \n ");                                                  // vypsani jmen sloupcu

            while (cursor.moveToNext()){                                                                // vypsani obsahu cursoru
                lastDbDate = cursor.getInt(dateColumnIndex);
                dateTemp = temp.getInt("arrivalDate", 0);
                differenceDays = todayInt - lastDbDate - 1;
                displayView.append("\n posledni zaznam v db z " + lastDbDate + "\n datum z temp.: " + dateTemp);
                displayView.append("\n Pracovnich dnu od posledniho zaznamu v db: " + differenceDays);
            }
        } finally {
            cursor.close();
        }

        String workDaysArray  = Tools.getWorkDaysInPeriod(lastDbDate, todayInt);
        displayView.append("\n " + workDaysArray);
    }

    private void showInfo() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        String[] projection = {
                ShiftEntry.COLUMN_DATE,
                ShiftEntry.COLUMN_SHIFT_LENGTH,
                ShiftEntry.COLUMN_OVERTIME,
                ShiftEntry.COLUMN_HOLIDAY};

        String selection = ShiftEntry.COLUMN_HOLIDAY + "=? AND " + ShiftEntry.COLUMN_DATE + " LIKE ?";

        int monthLength = (int) (Math.log10(month+1) + 1);                                                    // logarytmicka metoda zjisteni poctu cifer v cisle
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
            int shiftsThisMonth = cursor.getCount();                                                        //nastavení popisku odpracovaných směn
            String workDaysInMonth = Tools.getWorkDaysInMonth(month, year);
            shiftsInfoView.setText(getResources().getString(R.string.alreadyWorked) + " " + shiftsThisMonth + " " + getResources().getString(R.string.daysFrom) + " " + workDaysInMonth);
            monthShifts.setMax(Integer.parseInt(workDaysInMonth));
            monthShifts.setProgress(shiftsThisMonth);

            while (cursor.moveToNext()){
                overTimeSumThisMonth = overTimeSumThisMonth + cursor.getInt(overtimeIndex);
            }
            overtimeThisMonthView.setText(getResources().getString(R.string.thisMonthOvertime) + " " + Tools.timeIntToStr(overTimeSumThisMonth));
        } finally {
            cursor.close();
        }

        if (temp.contains("overtimeSum")){
            overtimeSumView.setText( getResources().getString(R.string.sumOfOvertime) + " " + Tools.timeIntToStr(temp.getInt("overtimeSum",0)) + "h");
        } else {overtimeSumView.setText( getResources().getString(R.string.sumOfOvertime) + " 0h");}

        //TODO dodělat kontrolu, že poslední zadaná směna byla ze včerejška, pokud ne, přidat incomplete směny za chybějící dny, upozornit že je třeba doplnit údaje
        // vybrat z DB poslední záznam a porovnat jeho datum s datumem v temp ulozenym pri poslednim zásahu do DB
        if (temp.contains("arrivalTime")){
            if ((temp.getInt("arrivalDate", 0)) == (Tools.dateDateToInt(calendar.getTime()))) {
                todayArrivalInfo.setText(getResources().getString(R.string.todayShiftArrivalLabel) + " " + Tools.timeIntToStr(temp.getInt("arrivalTime", 0)));
                showTimePickerIn.setEnabled(false);
            } else {
                Toast.makeText(this,"neni vyplnena smena ze vcera ", Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editorTemp = temp.edit();
                editorTemp.remove("arrivalTime");
                editorTemp.remove("arrivalDate");
                editorTemp.remove("incompleteUri");
                editorTemp.apply();
                todayArrivalInfo.setText(getResources().getString(R.string.todayShiftArrivalLabel) + " " + getResources().getText(R.string.notInserted));
            }
        } else {
            todayArrivalInfo.setText(getResources().getString(R.string.todayShiftArrivalLabel) + " " + getResources().getText(R.string.notInserted));
        }

        if (temp.contains("departureTime")){
            if ((temp.getInt("departureDate", 0)) == (Tools.dateDateToInt(calendar.getTime()))) {
                todayDepartureInfo.setText(getResources().getString(R.string.todayShiftDepartureLabel) + " " + Tools.timeIntToStr(temp.getInt("departureTime", 0)));
                showTimePickerOut.setEnabled(false);
            } else {
                SharedPreferences.Editor editorTemp = temp.edit();
                editorTemp.remove("departureTime");
                editorTemp.remove("departureDate");
                editorTemp.remove("incompleteUri");
                editorTemp.apply();
                todayDepartureInfo.setText(getResources().getString(R.string.todayShiftDepartureLabel) + " " + getResources().getString(R.string.notInserted));
            }

        } else {
            todayDepartureInfo.setText(getResources().getString(R.string.todayShiftDepartureLabel) + " " + getResources().getString(R.string.notInserted));
        }
        if (temp.contains("arrivalTime")) {
            editTodayButton.setVisibility(View.VISIBLE);
        } else {editTodayButton.setVisibility(View.INVISIBLE);
            showTimePickerIn.setEnabled(true);
            showTimePickerOut.setEnabled(true);
            }
    }

    /*private void displayDatabaseInfo() {
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
    }*/

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String departureTimeHelp = "";
        String arrivalTimeHelp = "";
        if (Globals.whichTime == "IN"){
            int minLength = (int) (Math.log10(minute) + 1);                                                     // logarytmicka metoda zjisteni poctu cifer v cisle
            if (minLength == 2){
                arrivalTimeHelp = hourOfDay + ":" + minute;
            } else {arrivalTimeHelp = hourOfDay + ":0" + minute;}
            todayArrivalInfo.setText(getResources().getString(R.string.todayShiftArrivalLabel) + " " + arrivalTimeHelp);
            Globals.timeInHours = hourOfDay;
            Globals.timeInMinutes = minute;
            SharedPreferences.Editor editorTemp = temp.edit();
            editorTemp.putInt("arrivalTime", Tools.timeStrToInt(arrivalTimeHelp));
            editorTemp.putInt("arrivalDate", Tools.dateDateToInt(calendar.getTime()));

            ContentValues shiftValues = new ContentValues();
            shiftValues.put(ShiftEntry.COLUMN_DATE, Tools.dateDateToInt(calendar.getTime()));
            shiftValues.put(ShiftEntry.COLUMN_ARRIVAL, Tools.timeStrToInt(arrivalTimeHelp));
            shiftValues.put(ShiftEntry.COLUMN_DEPARTURE, 0);
            shiftValues.put(ShiftEntry.COLUMN_BREAK_LENGTH, pref.getInt("defaultPause", 30));
            shiftValues.put(ShiftEntry.COLUMN_SHIFT_LENGTH, 0);
            shiftValues.put(ShiftEntry.COLUMN_OVERTIME, 0);
            shiftValues.put(ShiftEntry.COLUMN_HOLIDAY, ShiftEntry.HOLIDAY_INCOMPLETE);

            Uri incompleteShiftUri = getContentResolver().insert(ShiftEntry.CONTENT_URI, shiftValues);
            String uriStr = incompleteShiftUri.toString();

            editorTemp.putString("incompleteUri", uriStr);
            editorTemp.apply();
            showTimePickerIn.setEnabled(false);
            Toast.makeText(this,"ukladam incomplete zaznam " + uriStr, Toast.LENGTH_LONG).show();

        } else {
            int minLength = (int) (Math.log10(minute) + 1);
            if (minLength == 2){
                departureTimeHelp = hourOfDay + ":" + minute;
            } else {departureTimeHelp = hourOfDay + ":0" + minute;}
            todayDepartureInfo.setText(getResources().getString(R.string.todayShiftDepartureLabel) + " " + departureTimeHelp);
            Globals.timeOutHours = hourOfDay;
            Globals.timeOutMinutes = minute;
            SharedPreferences.Editor editorTemp = temp.edit();
            int arrivalTimeInt = temp.getInt("arrivalTime", 0);
            int departureTimeInt = Tools.timeStrToInt(departureTimeHelp);
            int breakLengthInt = pref.getInt("defaultPause", 30);
            int shiftLengthInt = departureTimeInt - arrivalTimeInt - breakLengthInt;                         // vypocet delky smeny
            int overtimeLengthInt = shiftLengthInt - 480 ;

            ContentValues shiftValues = new ContentValues();
            shiftValues.put(ShiftEntry.COLUMN_DATE, temp.getInt("arrivalDate", 0));
            shiftValues.put(ShiftEntry.COLUMN_ARRIVAL,arrivalTimeInt);
            shiftValues.put(ShiftEntry.COLUMN_DEPARTURE, departureTimeInt);
            shiftValues.put(ShiftEntry.COLUMN_BREAK_LENGTH, breakLengthInt);
            shiftValues.put(ShiftEntry.COLUMN_SHIFT_LENGTH, shiftLengthInt);
            shiftValues.put(ShiftEntry.COLUMN_OVERTIME, overtimeLengthInt);
            shiftValues.put(ShiftEntry.COLUMN_HOLIDAY, ShiftEntry.HOLIDAY_SHIFT);

            if (temp.contains("incompleteUri")) {
                 Uri updatingUri = Uri.parse(temp.getString("incompleteUri", " "));
                int rowsAffected = getContentResolver().update(updatingUri, shiftValues, null, null);
                if (rowsAffected == 0) {
                    Toast.makeText(this, getText(R.string.editor_update_shift_failed), Toast.LENGTH_SHORT).show();
                } else {Toast.makeText(this, getText(R.string.editor_update_shift_successful), Toast.LENGTH_SHORT).show();}

                int oldOvertime = temp.getInt("overtimeSum", 0);
                int newOvertime = oldOvertime + overtimeLengthInt;
                editorTemp.putInt("overtimeSum", newOvertime);
                editorTemp.putInt("departureTime", Tools.timeStrToInt(departureTimeHelp));
                editorTemp.putInt("departureDate", Tools.dateDateToInt(calendar.getTime()));
                editorTemp.apply();
                showTimePickerOut.setEnabled(false);
                Toast.makeText(this,"updatuji nekompletni zaznam ", Toast.LENGTH_LONG).show();
                editTodayButton.setVisibility(View.VISIBLE);
                showInfo();
            }  else {Toast.makeText(this,"chyba ", Toast.LENGTH_LONG).show();}
        }
    }


    @Override
    protected void onStart(){
        super.onStart();
        showInfo();
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
            shiftValues.put(ShiftEntry.COLUMN_BREAK_LENGTH, 30);
            shiftValues.put(ShiftEntry.COLUMN_SHIFT_LENGTH, 525);
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
        public static boolean isEdited;
    }
}