package com.workhours;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;


@SuppressWarnings("WeakerAccess")
public class SettingsCorrection extends AppCompatActivity {

    EditText holidayCorrection, overtimeCorrection;
    SharedPreferences temp, pref;


    public void save_settings(){
        SharedPreferences.Editor editorTemp = temp.edit();


        String isValidCorrection = overtimeCorrection.getText().toString();
        if (!isValidCorrection.isEmpty()) {
            int overtimeOld = 0;
            if (temp.contains("overtimeSum")) {
                overtimeOld = temp.getInt("overtimeSum", 0);
            }
            int correction = Integer.parseInt(overtimeCorrection.getText().toString());
            int overtimeNew = overtimeOld + correction;
            editorTemp.putInt("overtimeSum", overtimeNew);
            editorTemp.apply();
        }

        String isValidHolidayCorrection = holidayCorrection.getText().toString();
        if (!isValidHolidayCorrection.isEmpty()){
            int holidayOld = temp.getInt("holidaySum", 0);
            if (temp.contains("holidaySum")) {
                holidayOld = temp.getInt("holidaySum", 0);
            }
            int correctionHoliday = Integer.parseInt(holidayCorrection.getText().toString());
            int holidayNew = holidayOld + correctionHoliday;
            editorTemp.putInt("holidaySum", holidayNew);
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
        setContentView(R.layout.activity_settings_correction);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        temp = getApplicationContext().getSharedPreferences("Temporary", 0);

        overtimeCorrection = findViewById(R.id.overtimeCorrectionEdit);
        holidayCorrection = findViewById(R.id.holidayCorrectionEdit);
    }
}