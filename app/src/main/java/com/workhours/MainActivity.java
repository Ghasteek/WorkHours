package com.workhours;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
import com.workhours.data.ShiftsContract;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private TextView updateAvailable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        pref = getApplicationContext().getSharedPreferences("Settings", 0);                 // definovani SharedPreference
        temp = getApplicationContext().getSharedPreferences("Temporary", 0);

        Globals.theme = "light";
        if (pref.contains("layout")) {Globals.theme = pref.getString("layout", "light");}

        if (pref.contains("layout")){
            String savedLayout = pref.getString("layout", "light");
            if (savedLayout != null && savedLayout.equals("light")){
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
        updateAvailable = findViewById(R.id.updateAvailableId);
        //textView = findViewById(R.id.textViewId);

        View hView =  navigationView.getHeaderView(0);
        TextView headerNameWithVersion = hView.findViewById(R.id.headerNameWithVersionId);
        headerNameWithVersion.setText(getString(R.string.nav_header_title, Globals.versionStr));

        updateAvailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeUpdate();
            }
        });

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

        File file = new File(Environment.getExternalStorageDirectory() + "/"
                + Utils.downloadDirectory + "/version.txt");
        boolean isFileDeleted = file.delete();
        if (isFileDeleted) { Log.e(null, "version.txt deleted");}
        File file2 = new File(Environment.getExternalStorageDirectory() + "/" + Utils.downloadDirectory + "/workHours.apk");
        boolean isFile2Deleted = file2.delete();
        if (isFile2Deleted) { Log.e(null, "workHours.apk deleted");}

        checkYesterday();
        showInfo();
        checkVersionFromWeb();
    }


    private void checkYesterday() {
        Date today = calendar.getTime();
        int todayInt = Tools.dateDateToInt(today);

        String[] projection = {ShiftsContract.ShiftEntry.COLUMN_DATE,};                                                //get last row from DB according date
        String sortOrder = ShiftsContract.ShiftEntry.COLUMN_DATE + " DESC LIMIT 1";
        Cursor cursor = getContentResolver().query(
                ShiftsContract.ShiftEntry.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder);


        if (cursor != null && cursor.getCount() != 0) {
            int dateColumnIndex = cursor.getColumnIndex(ShiftsContract.ShiftEntry.COLUMN_DATE);
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
                                                                                                    // add new month into MONTHS table if it is new month with actual overtime sum and resetting actual temp overtime
        String[] projection2 = {ShiftsContract.ShiftEntry.COLUMN_DATE_MONTHS,};
        String sortOrder2 = ShiftsContract.ShiftEntry.COLUMN_DATE_MONTHS + " DESC LIMIT 1";
        Cursor cursor2 = getContentResolver().query(
                ShiftsContract.ShiftEntry.CONTENT_URI_MONTHS,
                projection2,
                null,
                null,
                sortOrder2);

        String thisMonthString = String.valueOf(todayInt).substring(0, 6);
        int thisMonth = Integer.parseInt(thisMonthString);
        String todayYearStr = String.valueOf(todayInt).substring(0, 4);
        String todayMonthStr = String.valueOf(todayInt).substring(4, 6);
        int todayYearInt = Integer.parseInt(todayYearStr);
        int todayMonthInt = Integer.parseInt(todayMonthStr);
        String lastMonthToDb = "";

        if (todayMonthInt == 1){
            lastMonthToDb = (todayYearInt - 1) + "12";
        } else if (todayMonthInt >= 10 && todayMonthInt <= 12) {
            lastMonthToDb = todayYearStr + (todayMonthInt - 1);
        }
        else if (todayMonthInt >= 2 && todayMonthInt <= 9) {
            lastMonthToDb = todayYearStr + "0" + (todayMonthInt - 1);
        }

        if (cursor2 != null && cursor2.getCount() != 0) {
            int dateMonthsColumnIndex = cursor2.getColumnIndex(ShiftsContract.ShiftEntry.COLUMN_DATE_MONTHS);
            int lastDbMonthDate = 0;
            try {
                while (cursor2.moveToNext()) {
                    lastDbMonthDate = cursor2.getInt(dateMonthsColumnIndex);
                }
            } finally {
                cursor2.close();
            }
            if (lastDbMonthDate < (thisMonth - 1)) {
                int actualOverwatch = 0;
                if (temp.contains("overtimeSum")){
                    actualOverwatch = temp.getInt("overtimeSum", 0);
                    temp.edit().putInt("overtimeSum", 0).apply();
                }
                ContentValues monthValues = new ContentValues();
                monthValues.put(ShiftsContract.ShiftEntry.COLUMN_DATE_MONTHS, lastMonthToDb);
                monthValues.put(ShiftsContract.ShiftEntry.COLUMN_OVERTIMESUM_MONTHS, actualOverwatch);
                Uri newUriMonth = getContentResolver().insert(ShiftsContract.ShiftEntry.CONTENT_URI_MONTHS, monthValues);
                if (newUriMonth == null) {
                    Toast.makeText(this, getText(R.string.addMonthFailed), Toast.LENGTH_SHORT).show();
                } //else { Toast.makeText(this, getText(R.string.editor_insert_shift_successful), Toast.LENGTH_SHORT).show();}
            }

        } else /*if (cursor2 != null && cursor2.getCount() == 0)*/ {
            ContentValues monthValues = new ContentValues();
            monthValues.put(ShiftsContract.ShiftEntry.COLUMN_DATE_MONTHS, Integer.parseInt(lastMonthToDb));
            monthValues.put(ShiftsContract.ShiftEntry.COLUMN_OVERTIMESUM_MONTHS, 0);
            Uri newUriMonth = getContentResolver().insert(ShiftsContract.ShiftEntry.CONTENT_URI_MONTHS, monthValues);
            if (newUriMonth == null) {
                Toast.makeText(this, getText(R.string.addMonthFailed), Toast.LENGTH_SHORT).show();
            } //else { Toast.makeText(this, getText(R.string.editor_insert_shift_successful), Toast.LENGTH_SHORT).show(); }
        }
    }

    private void showInfo() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        String[] projection = {
                ShiftsContract.ShiftEntry.COLUMN_HOLIDAY};


        String selection = ShiftsContract.ShiftEntry.COLUMN_HOLIDAY + " BETWEEN ? AND ? AND " + ShiftsContract.ShiftEntry.COLUMN_DATE + " LIKE ?";

        String[] projectionHolidays = {
                ShiftsContract.ShiftEntry.COLUMN_HOLIDAY};

        String selectionHolidays = ShiftsContract.ShiftEntry.COLUMN_HOLIDAY + " BETWEEN ? AND ? AND " + ShiftsContract.ShiftEntry.COLUMN_DATE + " LIKE ?";

        int monthLength = (int) (Math.log10(month+1) + 1);                                                    // logarytmicka metoda zjisteni poctu cifer v cisle
        String monthStr;
        if (monthLength == 1) {
            monthStr = "0" + (month + 1);
        } else { monthStr = String.valueOf(month); }

        String yearStr = String.valueOf(year);

        String[] selectionArgs = new String[] { String.valueOf(ShiftsContract.ShiftEntry.HOLIDAY_SHIFT), String.valueOf(ShiftsContract.ShiftEntry.HOLIDAY_COMPENSATION), yearStr + monthStr + "%" };

        String[] selectionArgsHolidays = new String[] { String.valueOf(ShiftsContract.ShiftEntry.HOLIDAY_PUBLIC), String.valueOf(ShiftsContract.ShiftEntry.HOLIDAY_VACATION), yearStr + monthStr + "%" };

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


        String[] projectionOvertime = {ShiftsContract.ShiftEntry.COLUMN_OVERTIMESUM_MONTHS };

        //String selectionOvertime = ShiftEntry.COLUMN_DATE_MONTHS + " BETWEEN ? AND ? AND " + ShiftEntry.COLUMN_DATE + " LIKE ?";

        //String[] selectionArgsOvertime = new String[] { String.valueOf(ShiftEntry.HOLIDAY_PUBLIC), String.valueOf(ShiftEntry.HOLIDAY_VACATION), String.valueOf(year) + monthStr + "%" };

        Cursor cursorOvertime = getContentResolver().query(
                ShiftsContract.ShiftEntry.CONTENT_URI_MONTHS,
                projectionOvertime,
                null, //selectionOvertime,
                null, //selectionArgsOvertime,
                null);

        int overtimeSUmFromDb = 0;
        //String overtimeSUmFromDb = "";

        if (cursorOvertime != null){
            int overtimeLengthColumnIndex = cursorOvertime.getColumnIndex(ShiftsContract.ShiftEntry.COLUMN_OVERTIMESUM_MONTHS);
            //overtimeSUmFromDb = cursorOvertime.getInt(1);
            while (cursorOvertime.moveToNext()){
                overtimeSUmFromDb = overtimeSUmFromDb + cursorOvertime.getInt(overtimeLengthColumnIndex);
                //overtimeSUmFromDb = cursorOvertime.getInt(overtimeLengthColumnIndex);
                //overtimeSUmFromDb = cursorOvertime.getColumnName(0);
            }
            cursorOvertime.close();
        }


        String [] monthArray = getResources().getStringArray(R.array.months);                           // nastavení popisku tohoto měsíce a roku
        thisMonthView.setText(getString(R.string.firstRow, monthArray[month], year));

        if (cursor != null && cursor2 != null) {
            try {
                int shiftsThisMonth = cursor.getCount();                                                    //nastavení popisku odpracovaných směn
                int holidaysThisMonth = cursor2.getCount();
                String workDaysInMonth = Tools.getWorkDaysInMonth((month+1), year);
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
            overtimeSumView.setText(getString(R.string.thirdRow, Tools.timeIntToStr(temp.getInt("overtimeSum",0) + overtimeSUmFromDb)));
        } else {overtimeSumView.setText(getString(R.string.thirdRow, Tools.timeIntToStr(overtimeSUmFromDb)));}

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
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_DATE, Tools.dateDateToInt(calendar.getTime()));
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_ARRIVAL, Tools.timeStrToInt(arrivalTimeHelp));
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_DEPARTURE, 0);
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_BREAK_LENGTH, pref.getInt("defaultPause", 30));
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_SHIFT_LENGTH, 0);
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_OVERTIME, 0);
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_HOLIDAY, ShiftsContract.ShiftEntry.HOLIDAY_INCOMPLETE);

            Uri incompleteShiftUri = getContentResolver().insert(ShiftsContract.ShiftEntry.CONTENT_URI, shiftValues);      // insertin of incomplete row into DB after adding arrival time
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
            String defaultShiftHelpStr = "8:00";
            int defaultShiftHelp = 0;
            if (pref.contains("defaultShift")){
                defaultShiftHelpStr = pref.getString("defaultShift", "8:00");
            }
            if (defaultShiftHelpStr != null){defaultShiftHelp = Tools.timeStrToInt(defaultShiftHelpStr);}
            int overtimeLengthInt = shiftLengthInt - defaultShiftHelp ;

            ContentValues shiftValues = new ContentValues();
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_DATE, temp.getInt("arrivalDate", 0));
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_ARRIVAL,arrivalTimeInt);
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_DEPARTURE, departureTimeInt);
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_BREAK_LENGTH, breakLengthInt);
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_SHIFT_LENGTH, shiftLengthInt);
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_OVERTIME, overtimeLengthInt);
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_HOLIDAY, ShiftsContract.ShiftEntry.HOLIDAY_SHIFT);

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
        switch (item.getItemId()){                                                                     // akce po kliknutí na Settings z menu/main
            case R.id.action_settings:
                Intent settings = new Intent(MainActivity.this, Settings.class);
                startActivity(settings);
                return true;
            case R.id.action_dummyData:
                insertDummyData();
                showInfo();
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
           case R.id.nav_about:
               Intent About = new Intent(MainActivity.this, About.class);
               startActivity(About);
               return true;
           /*case R.id.nav_get:
                openDownloadedFolder();
               return  true;*/
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
        shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_DATE, Tools.dateStrToInt(dateInput));
        shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_ARRIVAL, arrivalHelp);
        shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_DEPARTURE, 0);
        shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_BREAK_LENGTH, pref.getInt("defaultPause", 30));
        shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_SHIFT_LENGTH, 0);
        shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_OVERTIME, 0);
        shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_HOLIDAY, ShiftsContract.ShiftEntry.HOLIDAY_INCOMPLETE);
        Uri newUri = getContentResolver().insert(ShiftsContract.ShiftEntry.CONTENT_URI, shiftValues);
        if (newUri == null) {
            Toast.makeText(this, getText(R.string.editor_insert_shift_failed), Toast.LENGTH_SHORT).show();
        } //else {Toast.makeText(this, getText(R.string.editor_insert_shift_successful), Toast.LENGTH_SHORT).show();}
    }

    private void insertDummyData (){
        ContentValues shiftValues = new ContentValues();
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_DATE, 20190302);
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_ARRIVAL, 360);
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_DEPARTURE, 915);
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_BREAK_LENGTH, 30);
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_SHIFT_LENGTH, 525);
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_OVERTIME, 45);
            shiftValues.put(ShiftsContract.ShiftEntry.COLUMN_HOLIDAY, ShiftsContract.ShiftEntry.HOLIDAY_SHIFT);
        Uri newUri = getContentResolver().insert(ShiftsContract.ShiftEntry.CONTENT_URI, shiftValues);
        if (newUri == null) {
            Toast.makeText(this, getText(R.string.editor_insert_shift_failed), Toast.LENGTH_SHORT).show();
        } else {Toast.makeText(this, getText(R.string.editor_insert_shift_successful), Toast.LENGTH_SHORT).show();}
    }

    private boolean isConnectingToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null)
            return networkInfo.isConnected();
        else
            return false;
    }

    private void checkVersionFromWeb() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {
            Log.e(null, "Permissions granted.");
            if (isConnectingToInternet()) {
                new DownloadTask(MainActivity.this, Utils.downloadVersionUrl);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 5s = 5000ms
                        int webVersion = 0;
                        FileInputStream is;
                        BufferedReader reader;
                        File file = new File(Environment.getExternalStorageDirectory() + "/"
                                + Utils.downloadDirectory + "/version.txt");

                        if (file.exists()) {
                            try {
                                is = new FileInputStream(file);
                                reader = new BufferedReader(new InputStreamReader(is));
                                String line = reader.readLine();
                                webVersion = Integer.parseInt(line);

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            boolean isFileDeleted = file.delete();
                            if (isFileDeleted) { Log.e(null, "version.txt deleted. Web version: " + webVersion);}
                        }
                        if (Globals.version < webVersion){
                            updateAvailable.setText(getString(R.string.updateAvailable, webVersion, MainActivity.Globals.version));
                        }
                    }
                }, 5000);
            }
        }
    }

    public void makeUpdate() {
        new DownloadTask(MainActivity.this, Utils.downloadApkUrl);
        showUpdateDialog();
    }

    private void startUpdate() {
        File apkFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/workHours.apk");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri fileUri = android.support.v4.content.FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", apkFile);
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        startActivity(intent);
        finish();
        //todo dodělat kotrolu staženého spouboru - přidání globální isDownloaded proměnné která se nastaví do TRUE pokud se stáhne soubor a pokud se stáhne tak zobrazit manuální spuštění updatu, pokud bude stažený tak provést spuštění
    }

    @SuppressWarnings("unused")
    private void showUpdateDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        String savedLayout = "light";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (pref.contains("layout")) { savedLayout = pref.getString("layout", "light");}
        if (savedLayout != null) {
            switch (savedLayout) {
                case "light":
                    builder = new AlertDialog.Builder(this);
                    break;
                case "dark":
                    builder = new AlertDialog.Builder(this, R.style.darkDialogTheme);
                    break;
                default:
                    builder = new AlertDialog.Builder(this, R.style.darkDialogTheme);
            }
        }
        builder.setMessage(R.string.makeUpdateDialogMsg);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //openDownloadedFolder();
                        startUpdate();
                    }
                }, 10000);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    File file2 = new File(Environment.getExternalStorageDirectory() + "/"
                            + Utils.downloadDirectory + "/workHours.apk");
                    boolean isFile2Deleted = file2.delete();
                    if (isFile2Deleted) { Log.e(null, "workHours.apk deleted");}
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog updateDialog = builder.create();
        updateDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkVersionFromWeb();
                }
                return;
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startUpdate();
                }
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class Globals {
        public static String whichTime;
        public static int timeInHours;
        public static int timeInMinutes;
        public static int timeOutHours;
        public static int timeOutMinutes;
        public static boolean isEdited;
        public static String theme;
        public static int version = 103; //TODO S převerzováním upravit verzi i v globals!!!
        public static String versionStr = "v1.0.3.";
    }
}