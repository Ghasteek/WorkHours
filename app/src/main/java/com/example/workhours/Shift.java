package com.example.workhours;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.workhours.data.ShiftsContract.ShiftEntry;


public class Shift extends AppCompatActivity {
    private EditText date;
    private EditText arriveTime;
    private EditText departureTime;
    private EditText breakLenght;
    private TextView shiftLenght;
    private EditText holidayType;

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
                //save_settings();
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
        holidayType = (EditText) findViewById(R.id.holidayTypeEdit);
        // TODO spinner??
    }

}
