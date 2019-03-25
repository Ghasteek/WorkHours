package com.example.workhours;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.widget.TextView;
import android.widget.Toast;

import com.example.workhours.data.ShiftsContract;

public class ShiftTable extends AppCompatActivity {

    /*TextView showMonthYear;
    ImageButton changeSelection;
    String monthYearStr;*/
    public static int year;
    public static int month;


    @Override
    protected void onStart() {
        super.onStart();
        showData(year, month);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift_table);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        final TextView showMonthYear = findViewById(R.id.showMonthYearId);
        final ImageButton changeSelection = findViewById(R.id.button);
        if (year == 0) {
            Calendar cal = Calendar.getInstance();
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH) + 1;
        }
        String [] monthArray = getResources().getStringArray(R.array.months);
        int i = month - 1;
        showMonthYear.setText(monthArray[i] + " " + String.valueOf(year));
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
                        String [] monthArray = getResources().getStringArray(R.array.months);
                        int i = month - 1;
                        showMonthYear.setText(monthArray[i] + " " + String.valueOf(year));
                        showData(year, month);
                    }
                });
                pickerDialog.show(getSupportFragmentManager(), "MonthYearPickerDialog");
            }
        });
        //setting emptyView if no data in cursor
        ListView lvItems = (ListView) findViewById(R.id.ListViewItems);
        View emptyView = findViewById(R.id.empty_view);
        lvItems.setEmptyView(emptyView);

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent addIntent = new Intent(ShiftTable.this, Shift.class);
                Uri clickedShiftUri = ContentUris.withAppendedId(ShiftsContract.ShiftEntry.CONTENT_URI, id);
                addIntent.setData(clickedShiftUri);
                startActivity(addIntent);
                //Toast.makeText(ShiftTable.this, "URI - " + clickedShiftUri, Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void showData(int year, int month) {
        String[] projection = {
                ShiftsContract.ShiftEntry._ID,
                ShiftsContract.ShiftEntry.COLUMN_DATE,
                ShiftsContract.ShiftEntry.COLUMN_SHIFT_LENGTH,
                ShiftsContract.ShiftEntry.COLUMN_OVERTIME,
                ShiftsContract.ShiftEntry.COLUMN_HOLIDAY};

        String selection = ShiftsContract.ShiftEntry.COLUMN_DATE + " LIKE ?";

        int monthLength = (int) (Math.log10(month) + 1);                   // logarytmicka metoda zjisteni poctu cifer v cisle
        String monthStr;
        if (monthLength == 1) {
            monthStr = "0" + month;
        } else { monthStr = String.valueOf(month); }

        String[] selectionArgs = new String[] { String.valueOf(year)  + monthStr + "%"};
        String sortOrder =  ShiftsContract.ShiftEntry.COLUMN_DATE + " ASC";
        Cursor cursor = getContentResolver().query(
                ShiftsContract.ShiftEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder);

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
