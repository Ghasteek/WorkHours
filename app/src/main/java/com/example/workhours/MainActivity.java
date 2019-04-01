package com.example.workhours;

import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.database.Cursor;
import android.widget.Toast;
import com.example.workhours.data.ShiftsContract;
import com.example.workhours.data.ShiftsContract.ShiftEntry;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TimePickerDialog.OnTimeSetListener{    // pro time picker třeba implementovat TimePickerDialog.OnTimeSetListener

    private TextView todayArrivalInfo,todayDepartureInfo, thisMonthView,shiftsInfoView, overtimeSumView, todayBreak;
    //private TextView textView;
    private ProgressBar monthShifts;
    private SharedPreferences pref, temp;
    private Calendar calendar;
    private ImageButton showTimePickerIn, showTimePickerOut, editTodayButton;
    private EditText todayBreakInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        pref = getApplicationContext().getSharedPreferences("Settings", 0);                 // definovani SharedPreference
        temp = getApplicationContext().getSharedPreferences("Temporary", 0);

        if (pref.contains("layout")){
            String savedLayout = pref.getString("layout", "light");
            if (savedLayout.equals("light")){
                setTheme(R.style.AppTheme);
                setTheme(R.style.MainTheme);
            } else {
                setTheme(R.style.AppDarkTheme);
                setTheme(R.style.MainTheme);
            }
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

                                                                                                        // vypnuti floating action buttonu
        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(false);
        navigationView.setNavigationItemSelectedListener(this);

        editTodayButton = findViewById(R.id.editTodayButtonId);
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
        showTimePickerIn =  findViewById(R.id.arriveButtonId);
        showTimePickerIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals.whichTime = "IN";
                DialogFragment timePickerIn = new TimePickerFragment();
                timePickerIn.show(getSupportFragmentManager(), "time in picker");
            }
        });

        showTimePickerOut = findViewById(R.id.departureButtonId);                                       // odchod picker
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


        todayArrivalInfo = findViewById(R.id.todayArrivalInfoId);                                       // definmování textových polí na hlavní stránce
        todayDepartureInfo = findViewById(R.id.todayDepartureInfoId);

        thisMonthView = findViewById(R.id.thisMonthViewId);
        shiftsInfoView = findViewById(R.id.shiftsInfoViewId);
        overtimeSumView =  findViewById(R.id.overtimeSumViewId);
        monthShifts = findViewById(R.id.monthShiftsProgress);
        editTodayButton = findViewById(R.id.editTodayButtonId);
        todayBreakInput = findViewById(R.id.todayBreakInputId);
        todayBreak = findViewById(R.id.todayBreakId);
        //textView = findViewById(R.id.textViewId);

        calendar = Calendar.getInstance();

        String [] timeInArray = {}, timeOutArray = {};
        String timeInHelp = pref.getString("defaultInTime", "6:00");
        String timeOutHelp = pref.getString("defaultOutTime", "14:30");

        if (timeInHelp != null && timeInHelp.contains(":")){timeInArray = timeInHelp.split(":");}
        if (timeOutHelp != null && timeOutHelp.contains(":")) {timeOutArray = timeOutHelp.split(":");}
        //int prefBreak = pref.getInt("defaultPause", 30);
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

        String[] projection = {ShiftEntry.COLUMN_DATE,};                                                //get last row from DB according date
        String sortOrder = ShiftEntry.COLUMN_DATE + " DESC LIMIT 1";
        Cursor cursor = getContentResolver().query(
                ShiftEntry.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder);


        if (cursor != null && cursor.getCount() != 0) {
            int dateColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_DATE);
            //TextView displayView = (TextView) findViewById(R.id.text_view_table);
            int lastDbDate = 0;
            try {                                                                                   //get date of last row in DB according to date
                while (cursor.moveToNext()) {
                    lastDbDate = cursor.getInt(dateColumnIndex);
                }
            } finally {
                cursor.close();
            }
            //textView.setText("");
            if (lastDbDate < (Tools.dateDateToInt(today) - 1) ) {                                    // if there is row in db, that has lower date than today, insert incomplete rows into DB only for work days
                String workDaysArrayStr = Tools.getWorkDaysInPeriod(lastDbDate, todayInt);
                String[] workDaysArray = workDaysArrayStr.split("-");
                if (workDaysArray.length != 0 && !workDaysArray[0].equals("")) {
                    for (String aWorkDaysArray : workDaysArray) {
                        insertIncomplete(aWorkDaysArray);
                        //textView.append("\n " + i + " - *" + workDaysArray[i] + "*");
                    }
                }
                //Toast.makeText(this, " " + workDaysArray.length, Toast.LENGTH_LONG).show();
            } //else {displayView.append("\n zaznamy kompletni");}
//TODO zkontrolovat doplnění defaultních dní dovolené pro temp při překlopení roku
            String dateHelp = String.valueOf(lastDbDate);
            int lastDbDateYear = Integer.parseInt(dateHelp.substring(0, 4));
            int todayYear = calendar.get(Calendar.YEAR);
            //Toast.makeText(this, lastDbDateYear + " / " + todayYear, Toast.LENGTH_LONG).show();
            if (lastDbDateYear < todayYear) {                                                       //if last date in db is from last year, then get whole holiday pool into temporary
                int oldHoliday = temp.getInt("holidaySum", 0);
                int newHoliday = pref.getInt("holiday_days", 0) + oldHoliday;
                SharedPreferences.Editor editorTemp = temp.edit();
                editorTemp.putInt("holidaySum", newHoliday);
                editorTemp.apply();
            }
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private void showInfo() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        String[] projection = {
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

        String[] selectionArgsHolidays = new String[] { String.valueOf(ShiftEntry.HOLIDAY_PUBLIC), String.valueOf(ShiftEntry.HOLIDAY_VACATION), String.valueOf(year) + monthStr + "%" };

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
        thisMonthView.setText(getString(R.string.firstRow, monthArray[month], year));

        if (cursor != null && cursor2 != null) {
            try {
                int shiftsThisMonth = cursor.getCount();                                                    //nastavení popisku odpracovaných směn
                int holidaysThisMonth = cursor2.getCount();
                String workDaysInMonth = Tools.getWorkDaysInMonth(month, year);
                int workDaysInMonthCorrected = Integer.parseInt(workDaysInMonth) - holidaysThisMonth;
                shiftsInfoView.setText(getString(R.string.secondRow, shiftsThisMonth, workDaysInMonthCorrected));
                monthShifts.setMax(workDaysInMonthCorrected);
                monthShifts.setProgress(shiftsThisMonth);
            } finally {
                cursor.close();
                cursor2.close();
            }
        }

        if (temp.contains("overtimeSum")){
            overtimeSumView.setText(getString(R.string.thirdRow,Tools.timeIntToStr(temp.getInt("overtimeSum",0))));
        } else {overtimeSumView.setText(getString(R.string.thirdRow,"0"));}

        if (temp.contains("arrivalTime")){
            if ((temp.getInt("arrivalDate", 0)) == (Tools.dateDateToInt(calendar.getTime()))) {
                String help = Tools.timeIntToStr(temp.getInt("arrivalTime", 0));
                todayArrivalInfo.setText(getString(R.string.todayShiftArrivalLabel, help));
                showTimePickerIn.setEnabled(false);
                editTodayButton.setVisibility(View.INVISIBLE);
            } else {
                //Toast.makeText(this,"neni vyplnena smena ze vcera ", Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editorTemp = temp.edit();
                editorTemp.remove("arrivalTime");
                editorTemp.remove("arrivalDate");
                editorTemp.remove("incompleteUri");
                editorTemp.apply();
                todayArrivalInfo.setText(getString(R.string.todayShiftArrivalNaLabel));
            }
            todayBreakInput.setVisibility(View.VISIBLE);
            todayBreak.setVisibility(View.VISIBLE);
        } else {
            editTodayButton.setVisibility(View.INVISIBLE);
            todayBreakInput.setVisibility(View.INVISIBLE);
            todayBreak.setVisibility(View.INVISIBLE);
            showTimePickerIn.setEnabled(true);
            showTimePickerOut.setEnabled(true);
            todayArrivalInfo.setText(getString(R.string.todayShiftArrivalNaLabel));
        }

        if (temp.contains("departureTime")){
            if ((temp.getInt("departureDate", 0)) == (Tools.dateDateToInt(calendar.getTime()))) {
                String help = Tools.timeIntToStr(temp.getInt("departureTime", 0));
                todayDepartureInfo.setText(getString(R.string.todayShiftDepartureLabel, help));
                showTimePickerOut.setEnabled(false);
            } else {
                SharedPreferences.Editor editorTemp = temp.edit();
                editorTemp.remove("departureTime");
                editorTemp.remove("departureDate");
                editorTemp.remove("incompleteUri");
                editorTemp.apply();
                todayDepartureInfo.setText(getString(R.string.todayShiftDepartureNaLabel));
            }
            editTodayButton.setVisibility(View.VISIBLE);
            todayBreakInput.setVisibility(View.INVISIBLE);
            todayBreak.setVisibility(View.INVISIBLE);
            } else {
            todayDepartureInfo.setText(getString(R.string.todayShiftDepartureNaLabel));
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
        String departureTimeHelp, arrivalTimeHelp ;
        if (Globals.whichTime.equals("IN")){
            int minLength = (int) (Math.log10(minute) + 1);                                                     // logarytmicka metoda zjisteni poctu cifer v cisle
            if (minLength == 2){
                arrivalTimeHelp = hourOfDay + ":" + minute;
            } else {arrivalTimeHelp = hourOfDay + ":0" + minute;}
            todayArrivalInfo.setText(getString(R.string.todayShiftArrivalLabel, arrivalTimeHelp));

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

            Uri incompleteShiftUri = getContentResolver().insert(ShiftEntry.CONTENT_URI, shiftValues);      // insertin of incomplete row into DB after adding arrival time
            String uriStr = null;
            if (incompleteShiftUri != null) {
                uriStr = incompleteShiftUri.toString();
            }
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
            todayDepartureInfo.setText(getString(R.string.todayShiftDepartureLabel, departureTimeHelp));
            Globals.timeOutHours = hourOfDay;
            Globals.timeOutMinutes = minute;
            SharedPreferences.Editor editorTemp = temp.edit();
            int arrivalTimeInt = temp.getInt("arrivalTime", 0);
            int departureTimeInt = Tools.timeStrToInt(departureTimeHelp);
            int breakLengthInt = Tools.timeStrToInt(todayBreakInput.getText().toString());
            int shiftLengthInt = departureTimeInt - arrivalTimeInt - breakLengthInt;                                // vypocet delky smeny
            String defaultShiftHelpStr = "";
            int defaultShiftHelp = 0;
            if (pref.contains("defaultShift")){
                defaultShiftHelpStr = pref.getString("defaultShift", "8:00");
            }
            if (defaultShiftHelpStr != null){defaultShiftHelp = Tools.timeStrToInt(defaultShiftHelpStr);}
            int overtimeLengthInt = shiftLengthInt - defaultShiftHelp ;

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
                int rowsAffected = getContentResolver().update(updatingUri, shiftValues, null, null);           // update of incomplete row to regular row
                if (rowsAffected == 0) {
                    Toast.makeText(this, getText(R.string.editor_update_shift_failed), Toast.LENGTH_SHORT).show();
                } //else {Toast.makeText(this, getText(R.string.editor_update_shift_successful), Toast.LENGTH_SHORT).show();}

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
            }  //else {Toast.makeText(this,"chyba ", Toast.LENGTH_LONG).show();}
            todayBreakInput.setVisibility(View.INVISIBLE);
            todayBreak.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    protected void onStart(){
        super.onStart();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        showInfo();
        checkYesterday();
    }
                                                                                                        // po sem je definice buttonu a jeho onclickListeneru + onTimeSet abz vratil cas
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.

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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


    private void insertIncomplete(String dateInput){
        String arrivalString = pref.getString("defaultInTime", "6:00");
        int arrivalHelp = 0;
        if (arrivalString != null) {
            arrivalHelp = Tools.timeStrToInt(arrivalString);
        }
        ContentValues shiftValues = new ContentValues();
        shiftValues.put(ShiftEntry.COLUMN_DATE, Tools.dateStrToInt(dateInput));
        shiftValues.put(ShiftEntry.COLUMN_ARRIVAL, arrivalHelp);
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

    private void insertDummyData (){
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

    @SuppressWarnings("WeakerAccess")
    public static class Globals {
        public static String whichTime;
        public static int timeInHours;
        public static int timeInMinutes;
        public static int timeOutHours;
        public static int timeOutMinutes;
        public static boolean isEdited;
    }
}