package com.workhours;

import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.util.Calendar;
import android.widget.TextView;
import com.workhours.data.ShiftsContract;

@SuppressWarnings("WeakerAccess")
public class ShiftTable extends AppCompatActivity {

    /*TextView showMonthYear;
    ImageButton changeSelection;
    String monthYearStr;*/
    public static int year;
    public static int month;
    SharedPreferences pref;
    TextView showMonthYear;
    ImageButton changeSelection, monthUp, monthDown;


    @Override
    protected void onStart() {
        super.onStart();
        showData(year, month);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        setContentView(R.layout.activity_shift_table);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

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
        //setting emptyView if no data in cursor
        ListView lvItems = findViewById(R.id.ListViewItems);
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

        ListView lvItems = findViewById(R.id.ListViewItems);
        ShiftCursorAdapter shiftsAdapter = new ShiftCursorAdapter(this, cursor);
        lvItems.setAdapter(shiftsAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case (android.R.id.home):
                onBackPressed();
                year = 0;
                month = 0;
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
