package com.workhours;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;


@SuppressWarnings("WeakerAccess")
public class SettingsDefault extends AppCompatActivity {

    EditText timeIn, timeOut, breakTime, defaultShiftLength, holidayDaysEdit;
    SharedPreferences temp, pref;


    public void save_settings(){
        SharedPreferences.Editor editor = pref.edit();
        SharedPreferences.Editor editorTemp = temp.edit();

        String timeInStr = timeIn.getText().toString();
        String timeOutStr = timeOut.getText().toString();
        String defaultPauseStr = breakTime.getText().toString();
        String defaultShiftStr = defaultShiftLength.getText().toString();
        int holidayDays = Integer.parseInt(holidayDaysEdit.getText().toString());

        editor.putString("defaultInTime", timeInStr);
        editor.putString("defaultOutTime", timeOutStr);
        editor.putInt("defaultPause", Tools.timeStrToInt(defaultPauseStr));
        editor.putString("defaultShift", defaultShiftStr);
        editor.putInt("holiday_days", holidayDays);
//TODO předělat time pickery aby si braly default in a default out časy ze shared preferenecs a né z glogals
        String[] timeInArray = timeInStr.split(":");
        String[] timeOutArray = timeOutStr.split(":");
        MainActivity.Globals.timeInHours = Integer.parseInt(timeInArray[0]);
        MainActivity.Globals.timeInMinutes = Integer.parseInt(timeInArray[1]);
        MainActivity.Globals.timeOutHours = Integer.parseInt(timeOutArray[0]);
        MainActivity.Globals.timeOutMinutes = Integer.parseInt(timeOutArray[1]);
        editor.apply();                                                                             // apply() nevraci true pokud vse probehne v poradku, pokud chci hlidat vysledek, pouyit commit() + hlidat navrat true

        if (!temp.contains("holidaySum")) {     //TODO při změně defaultního počtu dovolené, změnit i dovolenou v temp
            editorTemp.putInt("holidaySum", holidayDays);
            editorTemp.apply();
        }
        Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show();
    }

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
                save_settings();
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
        setContentView(R.layout.activity_settings_default);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        pref = getApplicationContext().getSharedPreferences("Settings", 0);             // definovani SharedPreference a editu
        temp = getApplicationContext().getSharedPreferences("Temporary", 0);
        timeIn = findViewById(R.id.defaultArrivalTimeEdit);
        timeOut = findViewById(R.id.defaultDepartureTimeEdit);
        breakTime = findViewById(R.id.defaultBreakTimeEdit);
        defaultShiftLength = findViewById(R.id.defaultShiftLengthEdit);
        holidayDaysEdit = findViewById(R.id.holidayDaysEdit);

        if (pref.contains("defaultInTime")){
            timeIn.setText(pref.getString("defaultInTime", ""));
        }
        if (pref.contains("defaultOutTime")){
            timeOut.setText(pref.getString("defaultOutTime", ""));
        }
        if (pref.contains("defaultPause")){
            breakTime.setText(Tools.timeIntToStr(pref.getInt("defaultPause",0)));
        }
        if (pref.contains("defaultShift")){
            defaultShiftLength.setText(pref.getString("defaultShift",""));
        }
        if (pref.contains("holiday_days")){
            holidayDaysEdit.setText(getString(R.string.justNumber, pref.getInt("holiday_days",0)));
        }
    }
}