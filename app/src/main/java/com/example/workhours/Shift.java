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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.content.DialogInterface;
import com.example.workhours.data.ShiftsContract.ShiftEntry;
import java.util.Calendar;


public class Shift extends AppCompatActivity {
    private EditText date;
    private EditText arriveTime;
    private EditText departureTime;
    private EditText breakLenght;
    private TextView shiftLenght;
    private TextView overtimeLegth;
    private Uri clickedShiftUri;
    private boolean mShiftChanged = false;
    private Spinner holidayTypeSpinner;
    private String overtimeLengthStr;

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
            case (R.id.action_delete):
                DialogInterface.OnClickListener deleteButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        };
                showDeleteShiftDialog(deleteButtonClickListener);
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

        date = findViewById(R.id.dateEdit);
        arriveTime = findViewById(R.id.arrivalEdit);
        departureTime = findViewById(R.id.departureEdit);
        breakLenght = findViewById(R.id.breakLenghtEdit);
        shiftLenght = findViewById(R.id.shiftLenghtView);
        overtimeLegth = findViewById(R.id.overtimeLengthView);
        holidayTypeSpinner = findViewById(R.id.holidaySpinner);

        String [] holidayArray = getResources().getStringArray(R.array.holidayType);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_dropdown_item, holidayArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holidayTypeSpinner.setAdapter(spinnerArrayAdapter);

        date.setOnTouchListener(mTouchListener);
        arriveTime.setOnTouchListener(mTouchListener);
        departureTime.setOnTouchListener(mTouchListener);
        breakLenght.setOnTouchListener(mTouchListener);
        holidayTypeSpinner.setOnTouchListener(mTouchListener);

        Intent intent = getIntent();
        clickedShiftUri = intent.getData();

        if (clickedShiftUri == null){
            setTitle(getString(R.string.addShift));
            setDefault();
        } else {
            setTitle(getString(R.string.editShift));
            date.setEnabled(false);
            fillUp(clickedShiftUri);

        }
    }

    public void setDefault() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Settings", 0);             // definovani SharedPreference a editu
        EditText date = findViewById(R.id.dateEdit);
        EditText timeIn = findViewById(R.id.arrivalEdit);
        EditText timeOut = findViewById(R.id.departureEdit);
        EditText breakTime = findViewById(R.id.breakLenghtEdit);

        Calendar cal = Calendar.getInstance();

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        String dateStr = day + "." + month + "." + year;

        date.setText(dateStr);

        if (pref.contains("defaultInTime")){
            timeIn.setText(pref.getString("defaultInTime", ""));
        }
        if (pref.contains("defaultOutTime")){
            timeOut.setText(pref.getString("defaultOutTime", ""));
        }
        if (pref.contains("defaultPause")){
            breakTime.setText(Tools.timeIntToStr(pref.getInt("defaultPause",0)));
        }
        holidayTypeSpinner.setSelection(0);

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
                int breakLengthColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_BREAK_LENGTH);
                int shiftLengthColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_SHIFT_LENGTH);
                int overtimeLengthColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_OVERTIME);
                int holidayTypeColumnIndex = cursor.getColumnIndex(ShiftEntry.COLUMN_HOLIDAY);

                String dateStr = Tools.dateIntToStr(cursor.getInt(dateColumnIndex));
                String arrivalStr = Tools.timeIntToStr(cursor.getInt(arrivalColumnIndex));
                String departureStr = Tools.timeIntToStr(cursor.getInt(departureColumnIndex));
                String breakLenghtStr = Tools.timeIntToStr(cursor.getInt(breakLengthColumnIndex));
                String shiftLenghtStr = Tools.timeIntToStr(cursor.getInt(shiftLengthColumnIndex));
                overtimeLengthStr = Tools.timeIntToStr(cursor.getInt(overtimeLengthColumnIndex));

                date.setText(dateStr);
                arriveTime.setText(arrivalStr);
                departureTime.setText(departureStr);
                breakLenght.setText(breakLenghtStr);
                shiftLenght.setText(shiftLenghtStr);
                overtimeLegth.setText(overtimeLengthStr);
                int holidayTypeInt = cursor.getInt(holidayTypeColumnIndex);
                if ( holidayTypeInt == 4 ) {
                    holidayTypeSpinner.setSelection(0);
                } else{holidayTypeSpinner.setSelection(cursor.getInt(holidayTypeColumnIndex));}
            }
        cursor.close();
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
        int overtimeLenghtOriginal = 0;
        if (clickedShiftUri != null) {
            overtimeLenghtOriginal = Tools.timeStrToInt(overtimeLengthStr);
        }
        int overtimeDifference = overtimeLengthInt - overtimeLenghtOriginal;
        //Toast.makeText(this,"rozdíl overtime je " + overtimeDifference, Toast.LENGTH_LONG).show();

        int holidayTypeInt = holidayTypeSpinner.getSelectedItemPosition();
        SharedPreferences temp = getApplicationContext().getSharedPreferences("Temporary", 0);             // definovani SharedPreference a editu
        SharedPreferences.Editor editorTemp = temp.edit();
        if (holidayTypeInt == 0) {
            int overtimeSumOld = 0;
            if (temp.contains("overtimeSum")) {
                overtimeSumOld = temp.getInt("overtimeSum", 0);
            }
            int overtimeSumNew = overtimeSumOld + overtimeDifference;
            editorTemp.putInt("overtimeSum", overtimeSumNew);
            editorTemp.apply();
            }

        ContentValues shiftValues = new ContentValues();
        shiftValues.put(ShiftEntry.COLUMN_DATE, dateInt);
        shiftValues.put(ShiftEntry.COLUMN_ARRIVAL, arriveTimeInt);
        shiftValues.put(ShiftEntry.COLUMN_DEPARTURE, departureTimeInt);
        shiftValues.put(ShiftEntry.COLUMN_BREAK_LENGTH, breakLengthInt);
        shiftValues.put(ShiftEntry.COLUMN_SHIFT_LENGTH, shiftLengthInt);
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

        if (MainActivity.Globals.isEdited == true) {
            SharedPreferences.Editor editor = temp.edit();
            editor.putInt("arrivalTime", arriveTimeInt);
            editor.putInt("departureTime", departureTimeInt);
            editor.apply();
            MainActivity.Globals.isEdited = false;
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

    private void showDeleteShiftDialog(
            DialogInterface.OnClickListener deleteButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_acceptance_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteShift();
            }
        });
        builder.setNegativeButton(R.string.keep_shift, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog deleteDialog = builder.create();
        deleteDialog.show();
    }

    private void deleteShift(){
        SharedPreferences temp = getApplicationContext().getSharedPreferences("Temporary", 0);             // definovani SharedPreference
        SharedPreferences.Editor editorTemp = temp.edit();

        int holidayTypeInt = holidayTypeSpinner.getSelectedItemPosition();

        if (holidayTypeInt == 0) {
            int overtimeLengthOldSum = temp.getInt("overtimeSum", 0);
            int overtimeHelp = Tools.timeStrToInt(overtimeLengthStr);
            int overtimeNewSum = overtimeLengthOldSum - overtimeHelp;
            String asd = overtimeLegth.getText().toString();
            int asdasd = Tools.timeStrToInt(asd);
            Toast.makeText(this, "ubrano " + asd + " / " + asdasd, Toast.LENGTH_LONG).show();

            editorTemp.putInt("overtimeSum", overtimeNewSum);
        }

        int rowsDeleted = getContentResolver().delete(clickedShiftUri, null, null);
        //Toast.makeText(this, "Smazano " + rowsDeleted + " zaznamu.", Toast.LENGTH_SHORT).show();

        int dateTest = Tools.dateStrToInt(date.getText().toString());
        int dateSaved = temp.getInt("arrivalDate", 0);
        //Toast.makeText(this, "Smazano "+ dateTest + " --" + dateSaved, Toast.LENGTH_SHORT).show();
        if ((MainActivity.Globals.isEdited == true) || (dateTest == dateSaved)) {
            editorTemp.remove("arrivalTime");
            editorTemp.remove("departureTime");
            editorTemp.remove("incompleteUri");
            editorTemp.remove("departureDate");
            editorTemp.remove("arrivalDate");
            MainActivity.Globals.isEdited = false;
        }
        editorTemp.apply();
        finish();
    }
}