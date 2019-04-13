package com.workhours;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

@SuppressWarnings("WeakerAccess")
public class Settings extends AppCompatActivity {

    private TextView firstRowView, secondRowView, thirdRowView;

    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {                      // listener to any user touch on a view for editing
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            //Toast.makeText(Settings.this, "view -" + view.getId(), Toast.LENGTH_LONG).show();
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    int idSelected = view.getId();

                    if (idSelected == firstRowView.getId()) {
                        Intent settingsDefault = new Intent(Settings.this, SettingsDefault.class);
                        startActivity(settingsDefault);
                    } else if (idSelected == secondRowView.getId()) {
                        Intent settingsCorrection = new Intent(Settings.this, SettingsCorrection.class);
                        startActivity(settingsCorrection);
                    } else if (idSelected == thirdRowView.getId()) {
                        Intent settingsTheme = new Intent(Settings.this, SettingsTheme.class);
                        startActivity(settingsTheme);
                    }
                case MotionEvent.ACTION_UP:
                    view.performClick();
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
                onBackPressed();
                return true;
            case (R.id.action_save):
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Settings", 0);
        if (pref.contains("layout")){
            String savedLayout = pref.getString("layout", "light");
            if (savedLayout != null && savedLayout.equals("light")){
                setTheme(R.style.AppTheme);
            } else {
                setTheme(R.style.AppDarkTheme);
            }
        }
        setContentView(R.layout.activity_settings);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        firstRowView = findViewById(R.id.firstRow);
        secondRowView = findViewById(R.id.secondRow);
        thirdRowView = findViewById(R.id.thirdRow);

        firstRowView.setOnTouchListener(mTouchListener);
        secondRowView.setOnTouchListener(mTouchListener);
        thirdRowView.setOnTouchListener(mTouchListener);


    }
}