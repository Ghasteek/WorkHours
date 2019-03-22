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

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TimePickerDialog.OnTimeSetListener{    // pro time picker třeba implementovat TimePickerDialog.OnTimeSetListener

    TextView todayArrivalInfo,todayDepartureInfo, thisMonthView,shiftsInfoView, overtimeThisMonthView, overtimeSumView, todayBreak, textView;
    ProgressBar monthShifts;
    SharedPreferences pref, temp;
    Calendar calendar;
    ImageButton showTimePickerIn, showTimePickerOut, editTodayButton;
    EditText todayBreakInput;


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
        navigationView.getMenu().getItem(0).setChecked(false);
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
        todayBreakInput = (EditText) findViewById(R.id.todayBreakInputId);
        todayBreak = (TextView) findViewById(R.id.todayBreakId);
        //textView = (TextView) findViewById(R.id.textViewId);

        calendar = Calendar.getInstance();

        String[] timeInArray = pref.getString("defaultInTime", "6:00").split(":");
        String[] timeOutArray = pref.getString("defaultOutTime", "14:30").split(":");
        int prefBreak = pref.getInt("defaultPause", 30);
        Globals.timeInHours = Integer.parseInt(timeInArray[0]);
        Globals.timeInMinutes = Integer.parseInt(timeInArray[1]);
        Globals.timeOutHours = Integer.parseInt(timeOutArray[0]);
        Globals.timeOutMinutes = Integer.parseInt(timeOutArray[1]);

        checkYesterday();
        showInfo();
    }

    private void checkYesterday() {
        Date today = calendar.getTime();
        int todayInt = Tools.dateDateToInt(today);

        String[] projection = {ShiftEntry.COLUMN_DATE,};                                            //get last row from DB according date
        String sortOrder = ShiftEntry.COLUMN_DATE + " DESC LIMIT 1";
        Cursor cursor = getContentResolver().query(
                ShiftEntry.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder);

        int dateColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_DATE);
        if (cursor.getCount() != 0) {
            //TextView displayView = (TextView) findViewById(R.id.text_view_table);
            int lastDbDate = 0;
            try {                                                                                   //get date of last row in DB according to date
                while (cursor.moveToNext()) {
                    lastDbDate = cursor.getInt(dateColumnIndex);
                    }
                } finally {
                    cursor.close();
                }
            if (lastDbDate < (Tools.dateDateToInt(today) - 1)) {                                    // if there is row in db, that has lower date than today, insert incomplete rows into DB only for work days
                String workDaysArrayStr = Tools.getWorkDaysInPeriod(lastDbDate, todayInt);
                String[] workDaysArray = workDaysArrayStr.split("-");
                for (int i = 0; i < workDaysArray.length; i++) {
                    insertIncomplete(workDaysArray[i]);
                    //displayView.append("\n " + workDaysArray[i]);
                }
            } //else {displayView.append("\n zaznamy kompletni");}
//TODO zkontrolovat doplnění defaultních dní dovolené pro temp při překlopení roku
            String dateHelp = String.valueOf(lastDbDate);
            int lastDbDateYear = Integer.parseInt(dateHelp.substring(0,4));
            int todayYear = calendar.get(Calendar.YEAR);
            //Toast.makeText(this, lastDbDateYear + " / " + todayYear, Toast.LENGTH_LONG).show();
            if (lastDbDateYear < todayYear) {                                                       //if last date in db is from last year, then get whole holiday pool into temporary
                int oldHoliday = temp.getInt("holidaySum", 0);
                int newHoliday = pref.getInt("holiday_days", 0) + oldHoliday;
                SharedPreferences.Editor editorTemp = temp.edit();
                editorTemp.putInt("holidaySum", newHoliday);
                editorTemp.apply();
                }

            int lastDbDateMonth = Integer.parseInt(dateHelp.substring(5,6));
            int todayMonth = calendar.get(Calendar.MONTH) - 1;
            if (lastDbDateMonth < todayMonth) {                                                     // if last date in DB is from last month, then put into DB row with date yyyyMM and actual overtime SUM
//TODO dodělat přidání záznamu se sumou přesčasů pro daný měsíc
            }
            }
    }

    private void showInfo() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        String[] projection = {
                ShiftEntry.COLUMN_DATE,
                ShiftEntry.COLUMN_SHIFT_LENGTH,
                ShiftEntry.COLUMN_OVERTIME,
                ShiftEntry.COLUMN_HOLIDAY};


        String selection = ShiftEntry.COLUMN_HOLIDAY + " BETWEEN ? AND ? AND " + ShiftEntry.COLUMN_DATE + " LIKE ?";

        String[] projectionHolidays = {
                ShiftEntry.COLUMN_HOLIDAY};

        String selectionHolidays = ShiftEntry.COLUMN_HOLIDAY + " BETWEEN ? AND ? AND " + ShiftEntry.COLUMN_DATE + " LIKE ?";

        int monthLength = (int) (Math.log10(month+1) + 1);                                                    // logarytmicka metoda zjisteni poctu cifer v cisle
        String monthStr;
        if (monthLength == 1) {
            monthStr = "0" + (month + 1);
        } else { monthStr = String.valueOf(month); }

        String[] selectionArgs = new String[] { String.valueOf(ShiftEntry.HOLIDAY_SHIFT), String.valueOf(ShiftEntry.HOLIDAY_COMPENSATION), String.valueOf(year) + monthStr + "%" };

        String[] selectionArgsHolidays = new String[] { String.valueOf(ShiftEntry.HOLIDAY_SHIFT), String.valueOf(ShiftEntry.HOLIDAY_VACATION), String.valueOf(year) + monthStr + "%" };

        Cursor cursor = getContentResolver().query(
                ShiftsContract.ShiftEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        Cursor cursor2 = getContentResolver().query(
                        ShiftsContract.ShiftEntry.CONTENT_URI,
                        projectionHolidays,
                        selectionHolidays,
                        selectionArgsHolidays,
                        null);


        String [] monthArray = getResources().getStringArray(R.array.months);                           // nastavení popisku tohoto měsíce a roku
        int i = month;
        thisMonthView.setText(monthArray[i] + " " + String.valueOf(year));

        int overtimeIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_OVERTIME);
        int overTimeSumThisMonth = 0;

        try{
            int shiftsThisMonth = cursor.getCount();                                                    //nastavení popisku odpracovaných směn
            int shiftsAndHolidaysThisMonth = cursor2.getCount();
            String workDaysInMonth = Tools.getWorkDaysInMonth(month, year);
            shiftsInfoView.setText(getResources().getString(R.string.alreadyWorked) + " " + shiftsAndHolidaysThisMonth + " " + getResources().getString(R.string.daysFrom) + " " + workDaysInMonth);
            monthShifts.setMax(Integer.parseInt(workDaysInMonth));
            monthShifts.setProgress(shiftsThisMonth);

            while (cursor.moveToNext()){
                overTimeSumThisMonth = overTimeSumThisMonth + cursor.getInt(overtimeIndex);
            }
            overtimeThisMonthView.setText(getResources().getString(R.string.thisMonthOvertime) + " " + Tools.timeIntToStr(overTimeSumThisMonth));
        } finally {
            cursor.close();
            cursor2.close();
        }

        if (temp.contains("overtimeSum")){
            overtimeSumView.setText( getResources().getString(R.string.sumOfOvertime) + " " + Tools.timeIntToStr(temp.getInt("overtimeSum",0)) + "h");
        } else {overtimeSumView.setText( getResources().getString(R.string.sumOfOvertime) + " 0h");}

        if (temp.contains("arrivalTime")){
            if ((temp.getInt("arrivalDate", 0)) == (Tools.dateDateToInt(calendar.getTime()))) {
                todayArrivalInfo.setText(getResources().getString(R.string.todayShiftArrivalLabel) + " " + Tools.timeIntToStr(temp.getInt("arrivalTime", 0)));
                showTimePickerIn.setEnabled(false);
                editTodayButton.setVisibility(View.INVISIBLE);
            } else {
                //Toast.makeText(this,"neni vyplnena smena ze vcera ", Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editorTemp = temp.edit();
                editorTemp.remove("arrivalTime");
                editorTemp.remove("arrivalDate");
                editorTemp.remove("incompleteUri");
                editorTemp.apply();
                todayArrivalInfo.setText(getResources().getString(R.string.todayShiftArrivalLabel) + " " + getResources().getText(R.string.notInserted));
            }
            todayBreakInput.setVisibility(View.VISIBLE);
            todayBreak.setVisibility(View.VISIBLE);
        } else {
            editTodayButton.setVisibility(View.INVISIBLE);
            todayBreakInput.setVisibility(View.INVISIBLE);
            todayBreak.setVisibility(View.INVISIBLE);
            showTimePickerIn.setEnabled(true);
            showTimePickerOut.setEnabled(true);
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
            editTodayButton.setVisibility(View.VISIBLE);
            todayBreakInput.setVisibility(View.INVISIBLE);
            todayBreak.setVisibility(View.INVISIBLE);
            } else {
            todayDepartureInfo.setText(getResources().getString(R.string.todayShiftDepartureLabel) + " " + getResources().getString(R.string.notInserted));
            if (temp.contains("arrivalTime")) {
                editTodayButton.setVisibility(View.INVISIBLE);
                todayBreakInput.setVisibility(View.VISIBLE);
                todayBreak.setVisibility(View.VISIBLE);
                }
            }

        if (pref.contains("defaultPause")) {
            todayBreakInput.setText(Tools.timeIntToStr(pref.getInt("defaultPause", 30)));
        }
    }


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
            todayBreakInput.setVisibility(View.VISIBLE);
            todayBreak.setVisibility(View.VISIBLE);
            //Toast.makeText(this,"ukladam incomplete zaznam " + uriStr, Toast.LENGTH_LONG).show();
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
            int breakLengthInt = Tools.timeStrToInt(todayBreakInput.getText().toString());
            int shiftLengthInt = departureTimeInt - arrivalTimeInt - breakLengthInt;                         // vypocet delky smeny
            int overtimeLengthInt = shiftLengthInt - (Tools.timeStrToInt(pref.getString("defaultShift", "8:00"))) ;

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
                } else {
                    //Toast.makeText(this, getText(R.string.editor_update_shift_successful), Toast.LENGTH_SHORT).show();
                    }

                int oldOvertime = temp.getInt("overtimeSum", 0);
                int newOvertime = oldOvertime + overtimeLengthInt;
                editorTemp.putInt("overtimeSum", newOvertime);
                editorTemp.putInt("departureTime", Tools.timeStrToInt(departureTimeHelp));
                editorTemp.putInt("departureDate", Tools.dateDateToInt(calendar.getTime()));
                editorTemp.apply();
                showTimePickerOut.setEnabled(false);
                //Toast.makeText(this,"updatuji nekompletni zaznam ", Toast.LENGTH_LONG).show();
                editTodayButton.setVisibility(View.VISIBLE);
                showInfo();
            }  else {
                //Toast.makeText(this,"chyba ", Toast.LENGTH_LONG).show();
                }
            todayBreakInput.setVisibility(View.INVISIBLE);
            todayBreak.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    protected void onStart(){
        super.onStart();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        showInfo();
        checkYesterday();

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

       switch (item.getItemId()){                                                                     // akce po kliknutí na Settings z menu/main
            case R.id.nav_settings:
                Intent settings = new Intent(MainActivity.this, Settings.class);
                startActivity(settings);
                return true;
            case R.id.nav_addShift:
                Intent addIntent = new Intent(MainActivity.this, Shift.class);
                startActivity(addIntent);
                return true;
            case R.id.nav_shiftTable:
                Intent ShiftTable = new Intent(MainActivity.this, ShiftTable.class);
                startActivity(ShiftTable);
                return true;
            case R.id.nav_preview:
                Intent Preview = new Intent(MainActivity.this, Preview.class);
                startActivity(Preview);
                return true;
       }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


    public void insertIncomplete (String dateInput){
        ContentValues shiftValues = new ContentValues();
        shiftValues.put(ShiftEntry.COLUMN_DATE, Tools.dateStrToInt(dateInput));
        shiftValues.put(ShiftEntry.COLUMN_ARRIVAL, Tools.timeStrToInt(pref.getString("defaultInTime", "6:00")));
        shiftValues.put(ShiftEntry.COLUMN_DEPARTURE, 0);
        shiftValues.put(ShiftEntry.COLUMN_BREAK_LENGTH, pref.getInt("defaultPause", 30));
        shiftValues.put(ShiftEntry.COLUMN_SHIFT_LENGTH, 0);
        shiftValues.put(ShiftEntry.COLUMN_OVERTIME, 0);
        shiftValues.put(ShiftEntry.COLUMN_HOLIDAY, ShiftEntry.HOLIDAY_INCOMPLETE);
        Uri newUri = getContentResolver().insert(ShiftEntry.CONTENT_URI, shiftValues);
        if (newUri == null) {
            Toast.makeText(this, getText(R.string.editor_insert_shift_failed), Toast.LENGTH_SHORT).show();
        } //else {Toast.makeText(this, getText(R.string.editor_insert_shift_successful), Toast.LENGTH_SHORT).show();}
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