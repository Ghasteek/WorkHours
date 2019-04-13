package com.workhours;


import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import com.workhours.data.ShiftsContract;
import java.util.Calendar;
import java.util.Date;

@SuppressWarnings("WeakerAccess")
public class Preview extends AppCompatActivity {

    TextView overtimeFromLastMonthValue, workHoursPlanValue, workHoursDoneValue, workHoursMonthlyDifferenceValue,
                workHoursToNextMonthValue, usedHolidayValue, remainingHolidayValue, publicHolidaysValue, showMonthYear;
    SharedPreferences temp, pref;
    ImageButton monthUp, monthDown, changeSelection;
    //Calendar calendar;
    public static int year;
    public static int month;



    @Override
    protected void onStart() {
        super.onStart();
        showData(year, month);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        temp = getApplicationContext().getSharedPreferences("Temporary", 0);
        pref = getApplicationContext().getSharedPreferences("Settings", 0);
        super.onCreate(savedInstanceState);
        pref = getApplicationContext().getSharedPreferences("Settings", 0);
        if (pref.contains("layout")){
            String savedLayout = pref.getString("layout", "light");
            if (savedLayout != null && savedLayout.equals("light")){
                setTheme(R.style.AppTheme);
            } else {
                setTheme(R.style.AppDarkTheme);
            }
        }
        setContentView(R.layout.activity_preview);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        overtimeFromLastMonthValue = findViewById(R.id.overtimeFromLastMonthValueId);
        workHoursPlanValue = findViewById(R.id.workHoursPlanValueId);
        workHoursDoneValue = findViewById(R.id.workHoursDoneValueId);
        workHoursMonthlyDifferenceValue = findViewById(R.id.workHoursMonthlyDifferenceValueId);
        workHoursToNextMonthValue = findViewById(R.id.workHoursToNextMonthValueId);
        usedHolidayValue = findViewById(R.id.usedHolidayValueId);
        remainingHolidayValue = findViewById(R.id.remainingHolidayValueId);
        publicHolidaysValue = findViewById(R.id.publicHolidaysValueId);

        monthDown = findViewById(R.id.monthDownId);
        showMonthYear = findViewById(R.id.showMonthYearId);
        changeSelection = findViewById(R.id.button);
        monthUp = findViewById(R.id.monthUpId);

        if (year == 0) {
            Calendar cal = Calendar.getInstance();
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH) + 1;
        }
        String [] monthArray = getResources().getStringArray(R.array.months);
        int i = month - 1;
        showMonthYear.setText(getString(R.string.firstRow, monthArray[i], year));
        showData(year, month);

        changeSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonthYearPickerDialog pickerDialog = new MonthYearPickerDialog();
                pickerDialog.setListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year2, int month2, int i2) {
                        //Toast.makeText(ShiftTable.this, year + " - " + month, Toast.LENGTH_SHORT).show();
                        year = year2;
                        month = month2;
                        showData(year, month);
                    }
                });
                pickerDialog.show(getSupportFragmentManager(), "MonthYearPickerDialog");
            }
        });

        monthDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (month == 1){
                    month = 12;
                    year = --year;
                } else {
                    month = --month;
                }
                showData(year, month);
            }
        });

        monthUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (month == 12){
                    month = 1;
                    year = ++year;
                } else {
                    month = ++month;
                }
                showData(year, month);
            }
        });


    }

    public void showData(int year, int month) {
        String [] monthArray = getResources().getStringArray(R.array.months);
        int i = month - 1;
        showMonthYear.setText(getString(R.string.firstRow, monthArray[i], year));

        String yearMonthStr;
        if (month <= 9 ) {
            yearMonthStr = year + "0" + month;
        } else { yearMonthStr = year + "" + month;}

        //Toast.makeText(this, "zobrazuji - " + yearMonthStr, Toast.LENGTH_LONG).show();
        String [] projectionOvertime =  {
                ShiftsContract.ShiftEntry.COLUMN_DATE_MONTHS,
                ShiftsContract.ShiftEntry.COLUMN_OVERTIMESUM_MONTHS};
        int yearMonthInt = Integer.parseInt(yearMonthStr);
        String selectionOvertime = ShiftsContract.ShiftEntry.COLUMN_DATE_MONTHS + " <= " + yearMonthInt;

        Cursor cursorOvertime = getContentResolver().query(
                ShiftsContract.ShiftEntry.CONTENT_URI_MONTHS,
                projectionOvertime,
                selectionOvertime,
                null,
                null);

        int overtimeSUmFromDb = 0;
        int overtimeToNextMonth = 0;

        if (cursorOvertime != null){
            int overtimeLengthColumnIndex = cursorOvertime.getColumnIndex(ShiftsContract.ShiftEntry.COLUMN_OVERTIMESUM_MONTHS);
            int dateSumOvertimeIndex = cursorOvertime.getColumnIndex(ShiftsContract.ShiftEntry.COLUMN_DATE_MONTHS);
            //overtimeSUmFromDb = cursorOvertime.getInt(1);
            while (cursorOvertime.moveToNext()){
                if (cursorOvertime.getInt(dateSumOvertimeIndex) == Integer.parseInt(yearMonthStr)) {
                    overtimeToNextMonth = cursorOvertime.getInt(overtimeLengthColumnIndex);
                    //Toast.makeText(this, "do dalsiho - " + cursorOvertime.getInt(overtimeLengthColumnIndex), Toast.LENGTH_LONG).show();
                }else {
                    overtimeSUmFromDb = overtimeSUmFromDb + cursorOvertime.getInt(overtimeLengthColumnIndex);
                    //overtimeSUmFromDb = cursorOvertime.getInt(overtimeLengthColumnIndex);
                    //overtimeSUmFromDb = cursorOvertime.getColumnName(0);
                }
            }
            cursorOvertime.close();
        }

        String[] projection = {
                ShiftsContract.ShiftEntry.COLUMN_DATE,
                ShiftsContract.ShiftEntry.COLUMN_SHIFT_LENGTH,
                ShiftsContract.ShiftEntry.COLUMN_OVERTIME,
                ShiftsContract.ShiftEntry.COLUMN_HOLIDAY};


        String selection = ShiftsContract.ShiftEntry.COLUMN_HOLIDAY + " BETWEEN ? AND ? AND " + ShiftsContract.ShiftEntry.COLUMN_DATE + " LIKE ?";

        String[] projectionHolidays = {
                ShiftsContract.ShiftEntry.COLUMN_HOLIDAY};

        String selectionHolidays = ShiftsContract.ShiftEntry.COLUMN_HOLIDAY + " BETWEEN ? AND ? AND " + ShiftsContract.ShiftEntry.COLUMN_DATE + " LIKE ?";


        String[] selectionArgs = new String[] { String.valueOf(ShiftsContract.ShiftEntry.HOLIDAY_SHIFT), String.valueOf(ShiftsContract.ShiftEntry.HOLIDAY_COMPENSATION), yearMonthStr + "%" };

        String[] selectionArgsHolidays = new String[] { String.valueOf(ShiftsContract.ShiftEntry.HOLIDAY_PUBLIC), String.valueOf(ShiftsContract.ShiftEntry.HOLIDAY_VACATION), yearMonthStr + "%" };

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


        int holidaysThisMonth = 0, shiftLengthIndex, holidayTypeIndex, workHoursThisMonth = 0, usedHoliday = 0, usedPublicHoliday = 0;
        if (cursor2 != null && cursor != null) {
            holidaysThisMonth = cursor2.getCount();

            shiftLengthIndex = cursor.getColumnIndex(ShiftsContract.ShiftEntry.COLUMN_SHIFT_LENGTH);
            holidayTypeIndex = cursor2.getColumnIndex(ShiftsContract.ShiftEntry.COLUMN_HOLIDAY);

            while (cursor.moveToNext()){
                workHoursThisMonth = workHoursThisMonth + cursor.getInt(shiftLengthIndex);
            }
            while (cursor2.moveToNext()){
                if (cursor2.getInt(holidayTypeIndex) == ShiftsContract.ShiftEntry.HOLIDAY_VACATION) { usedHoliday ++;}
                if (cursor2.getInt(holidayTypeIndex) == ShiftsContract.ShiftEntry.HOLIDAY_PUBLIC) { usedPublicHoliday ++;}
            }
            cursor.close();
            cursor2.close();
        }

        String workDaysInMonth = Tools.getWorkDaysInMonth(month, year);
        int workDaysInMonthCorrected = Integer.parseInt(workDaysInMonth) - holidaysThisMonth;
        String defaultShiftLengthLoaded = "8:00";
        if (pref.contains("defaultShift")) {defaultShiftLengthLoaded = "" + pref.getString("defaultShift", "8:00");}
        int defaultShiftLength = Tools.timeStrToInt(defaultShiftLengthLoaded);
        int workHoursPlan = workDaysInMonthCorrected * defaultShiftLength;
        workHoursPlanValue.setText(Tools.timeIntToStr(workHoursPlan));                              // setup value of work hours plan to this month

        workHoursDoneValue.setText(Tools.timeIntToStr(workHoursThisMonth));

        int workHoursMonthlyDifference = workHoursThisMonth - workHoursPlan;
        workHoursMonthlyDifferenceValue.setText(Tools.timeIntToStr(workHoursMonthlyDifference));

        // získání string tohoto měsíce YYYYMM
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        int todayInt = Tools.dateDateToInt(today);
        String todayYearMonthString = String.valueOf(todayInt).substring(0, 6);

        if (yearMonthStr.equals(todayYearMonthString)) {
            workHoursToNextMonthValue.setText(Tools.timeIntToStr(temp.getInt("overtimeSum", 0)));
        } else {workHoursToNextMonthValue.setText(Tools.timeIntToStr(overtimeToNextMonth + overtimeSUmFromDb));}

        usedHolidayValue.setText(String.valueOf(usedHoliday));

        remainingHolidayValue.setText(String.valueOf(temp.getInt("holidaySum", 0)));

        publicHolidaysValue.setText(String.valueOf(usedPublicHoliday));

        overtimeFromLastMonthValue.setText(Tools.timeIntToStr(overtimeSUmFromDb));
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (android.R.id.home):
                super.onBackPressed();
                year = 0;
                month = 0;
                return true;
        }
        return true;
    }
}