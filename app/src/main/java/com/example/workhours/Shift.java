package com.example.workhours;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.content.DialogInterface;

import com.example.workhours.data.ShiftsContract;
import com.example.workhours.data.ShiftsContract.ShiftEntry;
import java.util.Calendar;

@SuppressWarnings("WeakerAccess")
public class Shift extends AppCompatActivity {
    EditText date;
    EditText arriveTime;
    EditText departureTime;
    EditText breakLenght;
    TextView shiftLenght;
    TextView overtimeLength;
    Uri clickedShiftUri;
    boolean mShiftChanged = false;
    Spinner holidayTypeSpinner;
    String overtimeLengthStr;
    SharedPreferences temp, pref;
    int holidayTypeInt;
    InputMethodManager im;


    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {                      // listener to any user touch on a view for editing
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mShiftChanged = true;
                    return false;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.showSoftInput(view, 0);
                    return false;
                default:
                    break;
            }
            return true;
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate (Bundle savedInstanceState){
        temp = getSharedPreferences("Temporary", 0);
        pref = getSharedPreferences("Settings", 0);
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
        setContentView(R.layout.content_shift);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        date = findViewById(R.id.dateEdit);
        arriveTime = findViewById(R.id.arrivalEdit);
        departureTime = findViewById(R.id.departureEdit);
        breakLenght = findViewById(R.id.breakLenghtEdit);
        shiftLenght = findViewById(R.id.shiftLenghtView);
        overtimeLength = findViewById(R.id.overtimeLengthView);
        holidayTypeSpinner = findViewById(R.id.holidaySpinner);

        String [] holidayArray = getResources().getStringArray(R.array.holidayType);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
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

            if (cursor != null) {
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
                    holidayTypeInt = cursor.getInt(holidayTypeColumnIndex);


                    date.setText(dateStr);
                    arriveTime.setText(arrivalStr);
                    departureTime.setText(departureStr);
                    breakLenght.setText(breakLenghtStr);
                    shiftLenght.setText(shiftLenghtStr);
                    overtimeLength.setText(overtimeLengthStr);


                    if (holidayTypeInt == ShiftEntry.HOLIDAY_INCOMPLETE) {
                        holidayTypeSpinner.setSelection(0);
                    } else {
                        holidayTypeSpinner.setSelection(cursor.getInt(holidayTypeColumnIndex));
                    }
                }
                cursor.close();
            }
    }

    public void saveShift (){
        String dateHELP = date.getText().toString();

        String[] projection = {
                ShiftEntry.COLUMN_DATE};

        String selection = ShiftEntry.COLUMN_DATE + "=?";
        String selectionArgs[] = {""+Tools.dateStrToInt(dateHELP)};
        Cursor cursor = getContentResolver().query(
                ShiftsContract.ShiftEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
        int cursorCount = 0;
        if (cursor != null) {
            cursorCount = cursor.getCount();
            cursor.close();
        }
        if ((cursorCount != 0) && (clickedShiftUri == null)){
            Toast.makeText(this, getText(R.string.used_date_warning), Toast.LENGTH_LONG).show();
            return;
        }
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

        int shiftLengthInt = 480;                                                                             // vypocet delky smeny
        String defaultShiftLoaded = "8:00";
        if (pref.contains("defaultShift")) {
            defaultShiftLoaded = pref.getString("defaultShift", "8:00");
        }

        if (departureTimeInt != 0) {
            shiftLengthInt = departureTimeInt - arriveTimeInt - breakLengthInt;
        } else {
            if (defaultShiftLoaded != null) {
                shiftLengthInt = Tools.timeStrToInt(defaultShiftLoaded);
                }                                                                                       // pomocny odecet pro fungovani nasledujicich vzorcu
            }
        int overtimeLengthInt = 0;
        if (defaultShiftLoaded != null) {
            overtimeLengthInt = shiftLengthInt - (Tools.timeStrToInt(defaultShiftLoaded));
        }

        int overtimeLengthOriginal = 0;
        if (clickedShiftUri != null) {
            overtimeLengthOriginal = Tools.timeStrToInt(overtimeLengthStr);
        }
        int overtimeDifference = overtimeLengthInt - overtimeLengthOriginal;
        //Toast.makeText(this,"rozdíl overtime je " + overtimeDifference, Toast.LENGTH_LONG).show();

        int holidayTypeSelectedInt = holidayTypeSpinner.getSelectedItemPosition();

        String defaultShiftLoadedStr = "" + pref.getString("defaultShift", "8:00");

        SharedPreferences.Editor editorTemp = temp.edit();
        if (holidayTypeSelectedInt == ShiftEntry.HOLIDAY_SHIFT) {
            int overtimeSumOld = 0;
            if (temp.contains("overtimeSum")) {
                overtimeSumOld = temp.getInt("overtimeSum", 0);
            }
            int overtimeSumNew = overtimeSumOld + overtimeDifference;
            editorTemp.putInt("overtimeSum", overtimeSumNew);
            }else {
                departureTimeInt = arriveTimeInt + (Tools.timeStrToInt(defaultShiftLoadedStr)) + breakLengthInt;
                }

        if (holidayTypeSelectedInt == ShiftEntry.HOLIDAY_COMPENSATION && holidayTypeInt != ShiftEntry.HOLIDAY_COMPENSATION){
            int oldOvertime = temp.getInt("overtimeSum", 0);
            int newOvertime = oldOvertime - (Tools.timeStrToInt(defaultShiftLoadedStr)) - overtimeLengthInt;
            editorTemp.putInt("overtimeSum", newOvertime);
            overtimeLengthInt = 0 - (Tools.timeStrToInt(defaultShiftLoadedStr));
            shiftLengthInt = Tools.timeStrToInt(defaultShiftLoadedStr);
        } else if (holidayTypeSelectedInt != ShiftEntry.HOLIDAY_COMPENSATION && holidayTypeInt == ShiftEntry.HOLIDAY_COMPENSATION) {
            int oldOvertime = temp.getInt("overtimeSum", 0);
            int newOvertime = oldOvertime + (Tools.timeStrToInt(defaultShiftLoadedStr)) + overtimeLengthInt;
            editorTemp.putInt("overtimeSum", newOvertime);
        }

        if ((holidayTypeSelectedInt == ShiftEntry.HOLIDAY_VACATION) && (holidayTypeInt != ShiftEntry.HOLIDAY_VACATION)) { // je zvolena dovolena, ale nebyla
            int oldHolidaySum = temp.getInt("holidaySum", 0);
            int newHolidaySum = oldHolidaySum - 1;
            editorTemp.putInt("holidaySum", newHolidaySum);


            int oldOvertime = temp.getInt("overtimeSum", 0);
            int newOvertime = oldOvertime - overtimeLengthInt;
            editorTemp.putInt("overtimeSum", newOvertime);



            shiftLengthInt = Tools.timeStrToInt(defaultShiftLoadedStr);
            departureTimeInt = arriveTimeInt + shiftLengthInt + breakLengthInt;
            overtimeLengthInt = 0;

        }
        if ((holidayTypeSelectedInt != ShiftEntry.HOLIDAY_VACATION) && (holidayTypeInt == ShiftEntry.HOLIDAY_VACATION)) { // neni zvolena dovolena, ale byla
            int oldHolidaySum = temp.getInt("holidaySum", 0);
            int newHolidaySum = oldHolidaySum + 1;
            editorTemp.putInt("holidaySum", newHolidaySum);

            int oldOvertime = temp.getInt("overtimeSum", 0);
            int newOvertime = oldOvertime + overtimeLengthInt;
            editorTemp.putInt("overtimeSum", newOvertime);
        }

        editorTemp.apply();

        ContentValues shiftValues = new ContentValues();
        shiftValues.put(ShiftEntry.COLUMN_DATE, dateInt);
        shiftValues.put(ShiftEntry.COLUMN_ARRIVAL, arriveTimeInt);
        shiftValues.put(ShiftEntry.COLUMN_DEPARTURE, departureTimeInt);
        shiftValues.put(ShiftEntry.COLUMN_BREAK_LENGTH, breakLengthInt);
        shiftValues.put(ShiftEntry.COLUMN_SHIFT_LENGTH, shiftLengthInt);
        shiftValues.put(ShiftEntry.COLUMN_OVERTIME, overtimeLengthInt);
        shiftValues.put(ShiftEntry.COLUMN_HOLIDAY, holidayTypeSelectedInt);

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

        if (MainActivity.Globals.isEdited) {
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

    @SuppressWarnings("unused")
    private void showDeleteShiftDialog(
            DialogInterface.OnClickListener deleteButtonClickListener) {
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
        SharedPreferences.Editor editorTemp = temp.edit();

        int holidayTypeSelectedInt = holidayTypeSpinner.getSelectedItemPosition();

        if (holidayTypeSelectedInt == ShiftEntry.HOLIDAY_SHIFT) {
            int overtimeLengthOldSum = temp.getInt("overtimeSum", 0);
            int overtimeHelp = Tools.timeStrToInt(overtimeLengthStr);
            int overtimeNewSum = overtimeLengthOldSum - overtimeHelp;
            //String asd = overtimeLength.getText().toString();
            //nt asdasd = Tools.timeStrToInt(asd);
            //Toast.makeText(this, "ubrano " + asd + " / " + asdasd, Toast.LENGTH_LONG).show();

            editorTemp.putInt("overtimeSum", overtimeNewSum);
        }
        if (holidayTypeSelectedInt == ShiftEntry.HOLIDAY_COMPENSATION) {
            int overtimeOld = temp.getInt("overtimeSum", 0);
            String defaultShiftHelp = "" + pref.getString("defaultShift", "0");
            int overtimeNew = overtimeOld + (Tools.timeStrToInt(defaultShiftHelp));
            editorTemp.putInt("overtimeSum", overtimeNew);
        }

        if (holidayTypeInt == ShiftEntry.HOLIDAY_VACATION) {
            int oldHolidaySum = temp.getInt("holidaySum", 0);
            int newHolidaySum = oldHolidaySum + 1;
            editorTemp.putInt("holidaySum", newHolidaySum);
        }

        getContentResolver().delete(clickedShiftUri, null, null);
        //int rowsDeleted = getContentResolver().delete(clickedShiftUri, null, null);
        //Toast.makeText(this, "Smazano " + rowsDeleted + " zaznamu.", Toast.LENGTH_SHORT).show();

        int dateTest = Tools.dateStrToInt(date.getText().toString());
        int dateSaved = temp.getInt("arrivalDate", 0);
        //Toast.makeText(this, "Smazano "+ dateTest + " --" + dateSaved, Toast.LENGTH_SHORT).show();
        if ((MainActivity.Globals.isEdited) || (dateTest == dateSaved)) {
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