package com.example.workhours;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends AppCompatActivity {

    EditText timeIn;
    EditText timeOut;
    EditText breakTime;
    SharedPreferences pref;

    public void save_settings(){
        SharedPreferences.Editor editor = pref.edit();

        String timeInStr = timeIn.getText().toString();
        String timeOutStr = timeOut.getText().toString();
        String defaultPauseStr = breakTime.getText().toString();

        editor.putString("defaultInTime", timeInStr);
        editor.putString("defaultOutTime", timeOutStr);
        editor.putInt("defaultPause", Tools.timeStrToInt(defaultPauseStr));

        String[] timeInArray = timeInStr.split(":");
        String[] timeOutArray = timeOutStr.split(":");
        MainActivity.Globals.timeInHours = Integer.parseInt(timeInArray[0]);
        MainActivity.Globals.timeInMinutes = Integer.parseInt(timeInArray[1]);
        MainActivity.Globals.timeOutHours = Integer.parseInt(timeOutArray[0]);
        MainActivity.Globals.timeOutMinutes = Integer.parseInt(timeOutArray[1]);
        editor.apply();                                                                        // apply() nevraci true pokud vse probehne v poradku, pokud chci hlidat vysledek, pouyit commit() + hlidat navrat true
        Toast.makeText(this, "Settings saved.", Toast.LENGTH_SHORT).show();
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
                //onBackPressed();
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
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        pref = getApplicationContext().getSharedPreferences("Settings", 0);             // definovani SharedPreference a editu
        timeIn = (EditText) findViewById(R.id.defaultArrivalTimeEdit);
        timeOut = (EditText) findViewById(R.id.defaultDepartureTimeEdit);
        breakTime = (EditText) findViewById(R.id.defaultBreakTimeEdit);

        if (pref.contains("defaultInTime")){
            timeIn.setText(pref.getString("defaultInTime", ""));
        }
        if (pref.contains("defaultOutTime")){
            timeOut.setText(pref.getString("defaultOutTime", ""));
        }
        if (pref.contains("defaultPause")){
            breakTime.setText(Tools.timeIntToStr(pref.getInt("defaultPause",0)));
        }
    }
}