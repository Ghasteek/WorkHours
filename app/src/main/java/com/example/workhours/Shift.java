package com.example.workhours;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;

import com.example.workhours.data.ShiftsContract.ShiftEntry;
import com.example.workhours.data.ShiftsDbHelper;


public class Shift extends AppCompatActivity {
    private EditText date;
    private EditText arriveTime;
    private EditText departureTime;
    private EditText breakLenght;
    private TextView shiftLenght;
    private TextView overtimeLegth;
    private EditText holidayType;
    private ShiftsDbHelper mDbHelper;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long

        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case (android.R.id.home):
                onBackPressed();
                return true;
            case (R.id.action_save):
                addShift();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_shift);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        date = (EditText) findViewById(R.id.dateEdit);
        arriveTime = (EditText) findViewById(R.id.arrivalEdit);
        departureTime = (EditText) findViewById(R.id.departureEdit);
        breakLenght = (EditText) findViewById(R.id.breakLenghtEdit);
        shiftLenght = (TextView) findViewById(R.id.shiftLenghtView);
        overtimeLegth = (TextView) findViewById(R.id.overtimeLengthView);
        holidayType = (EditText) findViewById(R.id.holidayTypeEdit);
        // TODO spinner??

        mDbHelper = new ShiftsDbHelper(this);

        Intent intent = getIntent();
        Uri clickedShiftUri = intent.getData();

        if (clickedShiftUri == null){
            setTitle(getString(R.string.addShift));
            setDefault();
        } else {
            setTitle(getString(R.string.editShift));
            fillUp(clickedShiftUri);
        }
    }

    public void setDefault() {

    }

    public void fillUp (Uri uri){
        Cursor cursor = getContentResolver().query(
                uri,
                null,
                null,
                null,
                null);

            while (cursor.moveToNext()) {
                int dateColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_DATE);
                int arrivalColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_ARRIVAL);
                int departureColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_DEPARTURE);
                int breakLengthColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_BREAK_LENGHT);
                int shiftLengthColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_SHIFT_LENGHT);
                int overtimeLengthColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_OVERTIME);
                int holidayTypeColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_HOLIDAY);

                String dateStr = Tools.dateIntToStr(cursor.getInt(dateColumnIndex));
                String arrivalStr = Tools.timeIntToStr(cursor.getInt(arrivalColumnIndex));
                String departureStr = Tools.timeIntToStr(cursor.getInt(departureColumnIndex));
                String breakLenghtStr = Tools.timeIntToStr(cursor.getInt(breakLengthColumnIndex));
                String shiftLenghtStr = Tools.timeIntToStr(cursor.getInt(shiftLengthColumnIndex));
                String overtimeLengthStr = Tools.timeIntToStr(cursor.getInt(overtimeLengthColumnIndex));
                String holidayTypeStr = String.valueOf(cursor.getInt(holidayTypeColumnIndex));

                date.setText(dateStr);
                arriveTime.setText(arrivalStr);
                departureTime.setText(departureStr);
                breakLenght.setText(breakLenghtStr);
                shiftLenght.setText(shiftLenghtStr);
                overtimeLegth.setText(overtimeLengthStr);
                holidayType.setText(holidayTypeStr);
            }
    }

    public void addShift (){
        String dateHELP = date.getText().toString();                                                    // prevod datumu na integer ve formatu RRRRMMDD
        int dateInt = Tools.dateStrToInt(dateHELP);

        String arrivalHelp = arriveTime.getText().toString();                                           // prevod casu prichodu na integer v minutach
        int arriveTimeInt = Tools.timeStrToInt(arrivalHelp);

        String departureHelp = departureTime.getText().toString();                                      // prevod casu odchodu na integer v minutach
        int departureTimeInt = Tools.timeStrToInt(departureHelp);

        String breakHelp = breakLenght.getText().toString();                                            // prevod delky pauzy na integer v minutach
        int breakLengthInt = Tools.timeStrToInt(breakHelp);

        int shiftLengthInt = departureTimeInt - arriveTimeInt - breakLengthInt;                         // vypocet delky smeny

        int overtimeLengthInt = shiftLengthInt - 480 ;

        String holidayHelp = holidayType.getText().toString();                                          // prevod zadaneho typu (smena/dovolena/nahradni volno) na integer
        int holidayTypeInt = Integer.parseInt(holidayHelp);

        ContentValues shiftValues = new ContentValues();
        shiftValues.put(ShiftEntry.COLUMN_DATE, dateInt);
        shiftValues.put(ShiftEntry.COLUMN_ARRIVAL, arriveTimeInt);
        shiftValues.put(ShiftEntry.COLUMN_DEPARTURE, departureTimeInt);
        shiftValues.put(ShiftEntry.COLUMN_BREAK_LENGHT, breakLengthInt);
        shiftValues.put(ShiftEntry.COLUMN_SHIFT_LENGHT, shiftLengthInt);
        shiftValues.put(ShiftEntry.COLUMN_OVERTIME, overtimeLengthInt);
        shiftValues.put(ShiftEntry.COLUMN_HOLIDAY, holidayTypeInt);

        Uri newUri = getContentResolver().insert(ShiftEntry.CONTENT_URI, shiftValues);

        if (newUri == null) {
            Toast.makeText(this, getText(R.string.editor_insert_shift_failed), Toast.LENGTH_SHORT).show();
        } else {Toast.makeText(this, getText(R.string.editor_insert_shift_successful), Toast.LENGTH_SHORT).show();}
    }
}