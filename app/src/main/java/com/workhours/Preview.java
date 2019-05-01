package com.workhours;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.workhours.data.ShiftsContract;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;


public class Preview extends AppCompatActivity {

    TextView overtimeFromLastMonthValue, workHoursPlanValue, workHoursDoneValue, workHoursMonthlyDifferenceValue,
                workHoursToNextMonthValue, usedHolidayValue, remainingHolidayValue, publicHolidaysValue, showMonthYear;
    SharedPreferences temp, pref;
    ImageButton monthUp, monthDown, changeSelection;
    //Calendar calendar;
    public static int year;
    public static int month;
    public static String showMonthYearString, workHoursPlanValueString, workHoursDoneValueString, workHoursMonthlyDifferenceValueString,
                workHoursToNextMonthValueString, usedHolidayValueString, remainingHolidayValueString, publicHolidaysValueString, overtimeFromLastMonthValueString;
    public static String yearMonthStr;



    @Override
    protected void onStart() {
        super.onStart();
        showData(year, month);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.preview_menu, menu);
        return true;
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
        showMonthYearString = getString(R.string.firstRow, monthArray[i], year);
        showMonthYear.setText(showMonthYearString);
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
        showMonthYearString = getString(R.string.firstRow, monthArray[i], year);
        showMonthYear.setText(showMonthYearString);


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


        String selection = ShiftsContract.ShiftEntry.COLUMN_HOLIDAY + " LIKE ? AND " + ShiftsContract.ShiftEntry.COLUMN_DATE + " LIKE ?";

        String[] projectionHolidays = {
                ShiftsContract.ShiftEntry.COLUMN_HOLIDAY};

        String selectionHolidays = ShiftsContract.ShiftEntry.COLUMN_HOLIDAY + " BETWEEN ? AND ? AND " + ShiftsContract.ShiftEntry.COLUMN_DATE + " LIKE ?";


        String[] selectionArgs = new String[] { String.valueOf(ShiftsContract.ShiftEntry.HOLIDAY_SHIFT), yearMonthStr + "%" };

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
        workHoursPlanValueString = Tools.timeIntToStr(workHoursPlan);
        workHoursPlanValue.setText(workHoursPlanValueString);                              // setup value of work hours plan to this month

        workHoursDoneValueString = Tools.timeIntToStr(workHoursThisMonth);
        workHoursDoneValue.setText(workHoursDoneValueString);

        int workHoursMonthlyDifference = workHoursThisMonth - workHoursPlan;
        workHoursMonthlyDifferenceValueString = Tools.timeIntToStr(workHoursMonthlyDifference);
        workHoursMonthlyDifferenceValue.setText(workHoursMonthlyDifferenceValueString);

        // získání string tohoto měsíce YYYYMM
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        int todayInt = Tools.dateDateToInt(today);
        String todayYearMonthString = String.valueOf(todayInt).substring(0, 6);

        if (yearMonthStr.equals(todayYearMonthString)) {
            workHoursToNextMonthValueString = Tools.timeIntToStr(overtimeToNextMonth + overtimeSUmFromDb + temp.getInt("overtimeSum", 0));
            workHoursToNextMonthValue.setText(workHoursToNextMonthValueString);
        } else {
            workHoursToNextMonthValueString = Tools.timeIntToStr(overtimeToNextMonth + overtimeSUmFromDb);
            workHoursToNextMonthValue.setText(workHoursToNextMonthValueString);
            }

        usedHolidayValueString = String.valueOf(usedHoliday);
        usedHolidayValue.setText(usedHolidayValueString);

        remainingHolidayValueString = String.valueOf(temp.getInt("holidaySum", 0));
        remainingHolidayValue.setText(remainingHolidayValueString);

        publicHolidaysValueString = String.valueOf(usedPublicHoliday);
        publicHolidaysValue.setText(publicHolidaysValueString);

        overtimeFromLastMonthValueString = Tools.timeIntToStr(overtimeSUmFromDb);
        overtimeFromLastMonthValue.setText(overtimeFromLastMonthValueString);
    }

    public void exportPdf() {

        PdfDocument exportFile = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();

        // start a page
        PdfDocument.Page page = exportFile.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(12f);
        paint.setSubpixelText(true);
        paint.setUnderlineText(true);
        canvas.drawText(getString(R.string.exportFirstRow, showMonthYearString), 180, 50, paint);
        paint.setTextSize(10f);
        paint.setSubpixelText(false);
        paint.setUnderlineText(false);

// nastavení vzdálenosti řádků
        int diff = 17;

        String[] projection = {
                ShiftsContract.ShiftEntry._ID,
                ShiftsContract.ShiftEntry.COLUMN_DATE,
                ShiftsContract.ShiftEntry.COLUMN_ARRIVAL,
                ShiftsContract.ShiftEntry.COLUMN_DEPARTURE,
                ShiftsContract.ShiftEntry.COLUMN_BREAK_LENGTH,
                ShiftsContract.ShiftEntry.COLUMN_SHIFT_LENGTH,
                ShiftsContract.ShiftEntry.COLUMN_HOLIDAY};

        String selection = ShiftsContract.ShiftEntry.COLUMN_DATE + " LIKE ?";

        String[] selectionArgs = new String[] { yearMonthStr + "%"};
        String sortOrder =  ShiftsContract.ShiftEntry.COLUMN_DATE + " ASC";
        Cursor cursor = getContentResolver().query(
                ShiftsContract.ShiftEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder);

        canvas.drawText(getString(R.string.exportDate), 80, 90, paint);
        canvas.drawText(getString(R.string.exportDay), 150, 90, paint);
        canvas.drawText(getString(R.string.exportArrival), 190, 90, paint);
        canvas.drawText(getString(R.string.exportDeparture), 240, 90, paint);
        canvas.drawText(getString(R.string.exportBreak), 290, 90, paint);
        canvas.drawText(getString(R.string.exportHoursDone), 340, 90, paint);
        canvas.drawLine(70, 95, 425, 97, paint);
        int y = 110;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int shiftLength = cursor.getInt(cursor.getColumnIndexOrThrow(ShiftsContract.ShiftEntry.COLUMN_SHIFT_LENGTH));
                int date = cursor.getInt(cursor.getColumnIndexOrThrow(ShiftsContract.ShiftEntry.COLUMN_DATE));
                int arrival = cursor.getInt(cursor.getColumnIndexOrThrow(ShiftsContract.ShiftEntry.COLUMN_ARRIVAL));
                int departure = cursor.getInt(cursor.getColumnIndexOrThrow(ShiftsContract.ShiftEntry.COLUMN_DEPARTURE));
                int breakLength = cursor.getInt(cursor.getColumnIndexOrThrow(ShiftsContract.ShiftEntry.COLUMN_BREAK_LENGTH));
                int holidayType = cursor.getInt(cursor.getColumnIndexOrThrow(ShiftsContract.ShiftEntry.COLUMN_HOLIDAY));

                String dayStr = Tools.getDayOfWeekStr(date);
                String arrivalStr = "", departureStr = "", breakStr = "", shiftLengthStr = "";

                if (holidayType == 0) {
                    arrivalStr = Tools.timeIntToStr(arrival);
                    departureStr = Tools.timeIntToStr(departure);
                    breakStr = Tools.timeIntToStr(breakLength);
                    shiftLengthStr = Tools.timeIntToStr(shiftLength);
                } else if (holidayType == 1) {
                    shiftLengthStr = "-" + pref.getString("defaultShift", "8:00");
                } else if (holidayType == 2) {
                    arrivalStr = getString(R.string.publicHoliday);
                } else if (holidayType == 3) {
                    arrivalStr = getString(R.string.vacation);
                }

                int day = Integer.parseInt(String.valueOf(date).substring(6));

                if (dayStr.equals("Po") && day == 3) {
                    canvas.drawText(Tools.dateIntToStr(date-2), 80, y, paint);
                    canvas.drawText("  " + "So", 150, y, paint);
                    y = y + diff;
                    canvas.drawText(Tools.dateIntToStr(date-1), 80, y, paint);
                    canvas.drawText("  " + "Ne", 150, y, paint);
                    y = y + diff;
                } else if (dayStr.equals("Po") && day == 2){
                    canvas.drawText(Tools.dateIntToStr(date-1), 80, y, paint);
                    canvas.drawText("  " + "Ne", 150, y, paint);
                    y = y + diff;
                }

                canvas.drawText(Tools.dateIntToStr(date), 80, y, paint);
                canvas.drawText("  " + dayStr, 150, y, paint);
                canvas.drawText("  " + arrivalStr, 190, y, paint);
                canvas.drawText("  " + departureStr, 240, y, paint);
                canvas.drawText("  " + breakStr, 290, y, paint);
                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText("  " + shiftLengthStr, 380, y, paint);
                paint.setTextAlign(Paint.Align.LEFT);

                y = y + diff;


                if (dayStr.equals("Pá") && (day + 2) <= Tools.getMonthMax(yearMonthStr) ) {
                    canvas.drawText(Tools.dateIntToStr(date+1), 80, y, paint);
                    canvas.drawText("  " + "So", 150, y, paint);
                    y = y + diff;
                    canvas.drawText(Tools.dateIntToStr(date+2), 80, y, paint);
                    canvas.drawText("  " + "Ne", 150, y, paint);
                    y = y + diff;
                } else if (dayStr.equals("Pá") && (day + 1) == Tools.getMonthMax(yearMonthStr)){
                    canvas.drawText(Tools.dateIntToStr(date+1), 80, y, paint);
                    canvas.drawText("  " + "So", 150, y, paint);
                    y = y + diff;
                }

            }
            cursor.close();
        }

        canvas.drawLine(70, y - 14, 425, y - 12, paint);
        y = y + 9;

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(getString(R.string.overtimeFromLastMonth), 80, y , paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(overtimeFromLastMonthValueString + " h", 340, y, paint);
        y = y + diff;

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(getString(R.string.workHoursPlan), 80, y, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(workHoursPlanValueString + " h", 340, y, paint);
        y = y + diff;

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(getString(R.string.workHoursDone), 80, y, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(workHoursDoneValueString + " h", 340, y, paint);
        y = y + diff;

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(getString(R.string.workHoursMonthlyDifference), 80, y, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(workHoursMonthlyDifferenceValueString + " h", 340, y, paint);
        y = y + diff;

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(getString(R.string.usedHoliday), 80, y, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(usedHolidayValueString + " d", 340, y, paint);
        y = y + diff;

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(getString(R.string.workHoursToNextMonth), 80, y, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(workHoursToNextMonthValueString + " h", 340, y, paint);


        exportFile.finishPage(page);
        // draw text on the graphics object of the page

        String directory_path = Environment.getExternalStorageDirectory().getPath() + "/Download/";
        File file = new File(directory_path);
        if (!file.exists()) {
            boolean isDirectoryMade = file.mkdirs();

            if (isDirectoryMade) { Log.e(null, "Directory made");}
        }
        String targetPdf = directory_path + yearMonthStr + ".pdf";
        File filePath = new File(targetPdf);
        try {
            exportFile.writeTo(new FileOutputStream(filePath));
            Toast.makeText(this, getString(R.string.exportSuccessful), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("main", "error "+e.toString());
            Toast.makeText(this, getString(R.string.exportUnSuccessful), Toast.LENGTH_LONG).show();
        }
        // close the document
        exportFile.close();
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
        builder.setMessage(R.string.exportDialogMsg);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                exportPdf();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();

            }
        });
        // Create and show the AlertDialog
        AlertDialog updateDialog = builder.create();
        updateDialog.show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (android.R.id.home):
                super.onBackPressed();
                year = 0;
                month = 0;
                return true;
            case (R.id.action_export):
                showUpdateDialog();
                return true;
        }
        return true;
    }
}