package com.example.workhours;


import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.workhours.data.ShiftsContract;

import java.util.Calendar;

public class Preview extends AppCompatActivity {

    TextView workHoursPlanValue, workHoursDoneValue, workHoursMonthlyDifferenceValue,
                workHoursToNextMonthValue, usedHolidayValue, remainingHolidayValue, publicHolidaysValue;
    SharedPreferences temp, pref;
    Calendar calendar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        temp = getApplicationContext().getSharedPreferences("Temporary", 0);
        pref = getApplicationContext().getSharedPreferences("Settings", 0);
        calendar = Calendar.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        workHoursPlanValue = findViewById(R.id.workHoursPlanValueId);
        workHoursDoneValue = findViewById(R.id.workHoursDoneValueId);
        workHoursMonthlyDifferenceValue = findViewById(R.id.workHoursMonthlyDifferenceValueId);
        workHoursToNextMonthValue = findViewById(R.id.workHoursToNextMonthValueId);
        usedHolidayValue = findViewById(R.id.usedHolidayValueId);
        remainingHolidayValue = findViewById(R.id.remainingHolidayValueId);
        publicHolidaysValue = findViewById(R.id.publicHolidaysValueId);


        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        String[] projection = {
                ShiftsContract.ShiftEntry.COLUMN_DATE,
                ShiftsContract.ShiftEntry.COLUMN_SHIFT_LENGTH,
                ShiftsContract.ShiftEntry.COLUMN_OVERTIME,
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

        String[] selectionArgs = new String[] { String.valueOf(ShiftsContract.ShiftEntry.HOLIDAY_SHIFT), String.valueOf(ShiftsContract.ShiftEntry.HOLIDAY_COMPENSATION), String.valueOf(year) + monthStr + "%" };

        String[] selectionArgsHolidays = new String[] { String.valueOf(ShiftsContract.ShiftEntry.HOLIDAY_PUBLIC), String.valueOf(ShiftsContract.ShiftEntry.HOLIDAY_VACATION), String.valueOf(year) + monthStr + "%" };

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


        int holidaysThisMonth = cursor2.getCount();
        String workDaysInMonth = Tools.getWorkDaysInMonth(month, year);
        int workDaysInMonthCorrected = Integer.parseInt(workDaysInMonth) - holidaysThisMonth;
        int defaultShiftLength = 0;
        if (pref.contains("defaultShift")) {defaultShiftLength = Tools.timeStrToInt(pref.getString("defaultShift", "8:00"));}
        int workHoursPlan = workDaysInMonthCorrected * defaultShiftLength;
        workHoursPlanValue.setText(Tools.timeIntToStr(workHoursPlan));                              // setup value of work hours plan to this month

        int workHoursThisMonth = 0;
        int usedHoliday = 0;
        int usedPublicHoliday = 0;
        int shiftLengthIndex = cursor.getColumnIndex(ShiftsContract.ShiftEntry.COLUMN_SHIFT_LENGTH);
        int holidayTypeIndex = cursor2.getColumnIndex(ShiftsContract.ShiftEntry.COLUMN_HOLIDAY);
        while (cursor.moveToNext()){
            workHoursThisMonth = workHoursThisMonth + cursor.getInt(shiftLengthIndex);
        }
        while (cursor2.moveToNext()){
            if (cursor2.getInt(holidayTypeIndex) == ShiftsContract.ShiftEntry.HOLIDAY_VACATION) { usedHoliday ++;}
            if (cursor2.getInt(holidayTypeIndex) == ShiftsContract.ShiftEntry.HOLIDAY_PUBLIC) { usedPublicHoliday ++;}
        }

        workHoursDoneValue.setText(Tools.timeIntToStr(workHoursThisMonth));

        int workHoursMonthlyDifference = workHoursThisMonth - workHoursPlan;
        workHoursMonthlyDifferenceValue.setText(Tools.timeIntToStr(workHoursMonthlyDifference));

        workHoursToNextMonthValue.setText(Tools.timeIntToStr(temp.getInt("overtimeSum", 0)));

        usedHolidayValue.setText(String.valueOf(usedHoliday));

        remainingHolidayValue.setText(String.valueOf(temp.getInt("holidaySum", 0)));

        cursor.close();
        cursor2.close();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (android.R.id.home):
                super.onBackPressed();
                return true;
        }
        return true;
    }

}