package com.example.workhours;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.content.DialogInterface;

import com.example.workhours.data.ShiftsContract.ShiftEntry;

import java.util.Calendar;
//import com.example.workhours.data.ShiftsDbHelper;


public class Shift extends AppCompatActivity {
    private EditText date;
    private EditText arriveTime;
    private EditText departureTime;
    private EditText breakLenght;
    private TextView shiftLenght;
    private TextView overtimeLegth;
    private EditText holidayType;
    private Uri clickedShiftUri;
    private boolean mShiftChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {                      // listener to any user touch on a view for editing
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mShiftChanged = true;
            return false;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long

        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case (android.R.id.home):
                if (!mShiftChanged) {
                    //NavUtils.navigateUpFromSameTask(Shift.this);
                    super.onBackPressed();
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            case (R.id.action_save):
                saveShift();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mShiftChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.shift_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (clickedShiftUri == null){
            menu.getItem(1).setVisible(false);
            setDefault();
        }
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

        date.setOnTouchListener(mTouchListener);
        arriveTime.setOnTouchListener(mTouchListener);
        departureTime.setOnTouchListener(mTouchListener);
        breakLenght.setOnTouchListener(mTouchListener);
        holidayType.setOnTouchListener(mTouchListener);

        Intent intent = getIntent();
        clickedShiftUri = intent.getData();

        if (clickedShiftUri == null){
            setTitle(getString(R.string.addShift));
            setDefault();
        } else {
            setTitle(getString(R.string.editShift));

            fillUp(clickedShiftUri);

        }
    }

    public void setDefault() {
        SharedPreferences pref = (SharedPreferences) getApplicationContext().getSharedPreferences("Settings", 0);             // definovani SharedPreference a editu
        EditText date = (EditText) findViewById(R.id.dateEdit);
        EditText timeIn = (EditText) findViewById(R.id.arrivalEdit);
        EditText timeOut = (EditText) findViewById(R.id.departureEdit);
        EditText breakTime = (EditText) findViewById(R.id.breakLenghtEdit);
        EditText holidayType = (EditText) findViewById(R.id.holidayTypeEdit);
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        if (pref.contains("defaultInTime")){
            timeIn.setText(pref.getString("defaultInTime", ""));
        }
        if (pref.contains("defaultOutTime")){
            timeOut.setText(pref.getString("defaultOutTime", ""));
        }
        if (pref.contains("defaultPause")){
            breakTime.setText(Tools.timeIntToStr(pref.getInt("defaultPause",0)));
        }
        holidayType.setText("0");
        date.setText(day + "." + month + "." + year);

        Toast.makeText(this, getText(R.string.defaults_loaded), Toast.LENGTH_SHORT).show();

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

    public void saveShift (){
        String dateHELP = date.getText().toString();
        if (dateHELP.length() == 0){
            Toast.makeText(this, getText(R.string.empty_date_warning), Toast.LENGTH_LONG).show();
            return;
        }
        int dateInt = Tools.dateStrToInt(dateHELP);                                                     // prevod datumu na integer ve formatu RRRRMMDD

        String arrivalHelp = arriveTime.getText().toString();
        if (arrivalHelp.length() == 0){
            Toast.makeText(this, getText(R.string.empty_arrival_warning), Toast.LENGTH_LONG).show();
            return;
        }
        int arriveTimeInt = Tools.timeStrToInt(arrivalHelp);                                            // prevod casu prichodu na integer v minutach

        String departureHelp = departureTime.getText().toString();
        if (departureHelp.length() == 0){
            Toast.makeText(this, getText(R.string.empty_departure_warning), Toast.LENGTH_LONG).show();
            return;
        }
        int departureTimeInt = Tools.timeStrToInt(departureHelp);                                       // prevod casu odchodu na integer v minutach

        String breakHelp = breakLenght.getText().toString();
        if (breakHelp.length() == 0){
            Toast.makeText(this, getText(R.string.empty_break_warning), Toast.LENGTH_LONG).show();
            return;
        }
        int breakLengthInt = Tools.timeStrToInt(breakHelp);                                             // prevod delky pauzy na integer v minutach

        int shiftLengthInt = departureTimeInt - arriveTimeInt - breakLengthInt;                         // vypocet delky smeny

        int overtimeLengthInt = shiftLengthInt - 480 ;

        String holidayHelp = holidayType.getText().toString();
        if (holidayHelp.length() == 0){
            Toast.makeText(this, getText(R.string.empty_holiday_warning), Toast.LENGTH_LONG).show();
            return;
        }
        int holidayTypeInt = Integer.parseInt(holidayHelp);                                            // prevod zadaneho typu (smena/dovolena/nahradni volno) na integer

        ContentValues shiftValues = new ContentValues();
        shiftValues.put(ShiftEntry.COLUMN_DATE, dateInt);
        shiftValues.put(ShiftEntry.COLUMN_ARRIVAL, arriveTimeInt);
        shiftValues.put(ShiftEntry.COLUMN_DEPARTURE, departureTimeInt);
        shiftValues.put(ShiftEntry.COLUMN_BREAK_LENGHT, breakLengthInt);
        shiftValues.put(ShiftEntry.COLUMN_SHIFT_LENGHT, shiftLengthInt);
        shiftValues.put(ShiftEntry.COLUMN_OVERTIME, overtimeLengthInt);
        shiftValues.put(ShiftEntry.COLUMN_HOLIDAY, holidayTypeInt);

        if (clickedShiftUri == null) {
            Uri newUri = getContentResolver().insert(ShiftEntry.CONTENT_URI, shiftValues);
            if (newUri == null) {
                Toast.makeText(this, getText(R.string.editor_insert_shift_failed), Toast.LENGTH_SHORT).show();
            } else {Toast.makeText(this, getText(R.string.editor_insert_shift_successful), Toast.LENGTH_SHORT).show();}
        } else {
            int rowsAffected = getContentResolver().update(clickedShiftUri, shiftValues, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getText(R.string.editor_update_shift_failed), Toast.LENGTH_SHORT).show();
            } else {Toast.makeText(this, getText(R.string.editor_update_shift_successful), Toast.LENGTH_SHORT).show();}
        }
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}