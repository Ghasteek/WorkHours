package com.example.workhours;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.widget.TextView;
import android.widget.Toast;

import com.example.workhours.data.ShiftsContract;

public class ShiftTable extends AppCompatActivity {

    Button click_me;
    String monthYearStr;

    SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy");
    SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_table);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        /*Spinner spinnerMonths = (Spinner) findViewById(R.id.spinnerMonth);
        ArrayAdapter<String> spinnerMonthsArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.months));
        spinnerMonths.setAdapter(spinnerMonthsArrayAdapter);*/


        // od sem je to month year picker https://github.com/abhinav011085/MonthYearPicker
        final TextView click_me = findViewById(R.id.textView);

        click_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonthYearPickerDialog pickerDialog = new MonthYearPickerDialog();
                pickerDialog.setListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int i2) {
                        Toast.makeText(ShiftTable.this, year + " - " + month, Toast.LENGTH_SHORT).show();
                        monthYearStr = year + "-" + (month + 1) + "-" + i2;
                        click_me.setText(formatMonthYear(monthYearStr));
                        showData(year, month);
                    }
                });
                pickerDialog.show(getSupportFragmentManager(), "MonthYearPickerDialog");
            }
        });
        //po sem

        //showData();
    }

    public String formatMonthYear(String str) {
        Date date = null;
        try {
            date = input.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return sdf.format(date);
    }

    public void showData(int year, int month) {
        String[] projection = {
                ShiftsContract.ShiftEntry._ID,
                ShiftsContract.ShiftEntry.COLUMN_DATE,
                ShiftsContract.ShiftEntry.COLUMN_SHIFT_LENGHT,
                ShiftsContract.ShiftEntry.COLUMN_HOLIDAY};

        String selection = ShiftsContract.ShiftEntry.COLUMN_DATE + " LIKE ?";

        int monthLength = (int) (Math.log10(month) + 1);                   // logarytmicka metoda zjisteni poctu cifer v cisle
        String monthStr;
        if (monthLength == 1) {
            monthStr = "0" + month;
        } else { monthStr = String.valueOf(month); }

        String[] selectionArgs = new String[] { String.valueOf(year)  + monthStr + "%"};

        Cursor cursor = getContentResolver().query(
                ShiftsContract.ShiftEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        ListView lvItems = (ListView) findViewById(R.id.ListViewItems);
        ShiftCursorAdapter shiftsAdapter = new ShiftCursorAdapter(this, cursor);
        lvItems.setAdapter(shiftsAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case (android.R.id.home):
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
