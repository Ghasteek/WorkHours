package com.example.workhours;

import android.app.Application;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.workhours.data.ShiftsContract.ShiftEntry;
import com.example.workhours.data.ShiftsDbHelper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TimePickerDialog.OnTimeSetListener {    // pro time picker t5eba implementovat TimePickerDialog.OnTimeSetListener

    TextView pickedTimeIn;
    TextView pickedTimeOut;
    SharedPreferences pref;
    private ShiftsDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        /*Button next = (Button) findViewById(R.id.button);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), Settings.class);
                startActivityForResult(myIntent, 0);
            }

        });*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
                                                                                                        // prichod picker
        Button showTimePickerIn = (Button) findViewById(R.id.showTimePickerIn);
        showTimePickerIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals.whichTinme = "IN";
                DialogFragment timePickerIn = new TimePickerFragment();
                timePickerIn.show(getSupportFragmentManager(), "time in picker");
            }
        });

        TextView showTimePickerOut2 = (TextView) findViewById(R.id.pickedTimeOutView);                  // odchod picker pri kliku na label
        showTimePickerOut2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals.whichTinme = "OUT";
                DialogFragment timePickerOut = new TimePickerFragment();
                timePickerOut.show(getSupportFragmentManager(), "time out picker");
            }
        });

        Button showTimePickerOut = (Button) findViewById(R.id.showTimePickerOut);                             // odchod picker
        showTimePickerOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Globals.whichTinme = "OUT";
                DialogFragment timePickerOut = new TimePickerFragment();
                timePickerOut.show(getSupportFragmentManager(), "time out picker");
            }
        });


        pref = getApplicationContext().getSharedPreferences("Settings", 0);             // definovani SharedPreference a dvou editu
        pickedTimeIn = (TextView) findViewById(R.id.pickedTimeInView);
        pickedTimeOut = (TextView) findViewById(R.id.pickedTimeOutView);

        String timeInStr = pickedTimeIn.getText().toString();                                           // prirazeni shared preference do globalnich promennych kvuli time pickeru
        String timeOutStr = pickedTimeOut.getText().toString();
        String[] timeInArray = timeInStr.split(":");
        String[] timeOutArray = timeOutStr.split(":");
        MainActivity.Globals.timeInHours = Integer.parseInt(timeInArray[0]);
        MainActivity.Globals.timeInMinutes = Integer.parseInt(timeInArray[1]);
        MainActivity.Globals.timeOutHours = Integer.parseInt(timeOutArray[0]);
        MainActivity.Globals.timeOutMinutes = Integer.parseInt(timeOutArray[1]);

        if (pref.contains("defaultInTime")){
            pickedTimeIn.setText(pref.getString("defaultInTime", ""));
        }
        if (pref.contains("defaultOutTime")){
            pickedTimeOut.setText(pref.getString("defaultOutTime", ""));
        }

    // testovaci kus kodu
        mDbHelper = new ShiftsDbHelper(this);

        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        //ShiftsDbHelper mDbHelper = new ShiftsDbHelper(this);

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Perform this raw SQL query "SELECT * FROM pets"
        // to get a Cursor that contains all rows from the pets table.
        Cursor cursor = db.rawQuery("SELECT * FROM " + ShiftEntry.TABLE_NAME, null);
        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            TextView displayView = (TextView) findViewById(R.id.text_view_table);
            displayView.setText("Number of rows in pets database table: " + cursor.getCount());
            /*int i=0;                                                                                  // vypsani jmen sloupcu
            String [] columnsArray = cursor.getColumnNames();
            String columns = "";
            for (i=0;i<cursor.getColumnCount();i++){
                columns = columns + columnsArray[i] + ", ";
            }
            displayView.setText("jména slooupců: " + columns);*/
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }



    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (Globals.whichTinme == "IN"){
            TextView timeInView = (TextView) findViewById(R.id.pickedTimeInView);
            int minLength = (int) (Math.log10(minute) + 1);                                                     // logarztmicka metoda yjisteni poctu cifer v cisle
            if (minLength == 2){
                timeInView.setText(hourOfDay + ":" + minute);
            } else {timeInView.setText(hourOfDay + ":0" + minute);}
            Globals.timeInHours = hourOfDay;
            Globals.timeInMinutes = minute;
        } else {
            TextView timeOutView = (TextView) findViewById(R.id.pickedTimeOutView);
            int minLength = (int) (Math.log10(minute) + 1);
            if (minLength == 2){
                timeOutView.setText(hourOfDay + ":" + minute);
            } else {timeOutView.setText(hourOfDay + ":0" + minute);}
            int pause = pref.getInt("defaultPause", 0);
            int calculatedTime = (((hourOfDay * 60) + minute)) - (((Globals.timeInHours * 60) + Globals.timeInMinutes)) - pause;  // vypocet odpracovanych minut, (odchod - prichod) - pauza
            int hours = calculatedTime / 60;                                                                                    // vypocet hodin
            int minutes = (calculatedTime - (hours * 60));                                                                      // vypocet minut
            TextView calculatedView = (TextView) findViewById(R.id.calculatedTimeView);
            int minLength2 = (int) (Math.log10(minutes) + 1);
            if (minLength2 == 2){
                calculatedView.setText(hours + ":" + minutes);
            } else {calculatedView.setText(hours + ":0" + minutes);}
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        displayDatabaseInfo();
    }
                                                                                                        // po sem je definice buttonu a jeho onclickListeneru + onTimeSet abz vratil cas
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (item.getItemId()){                                                                     // akce po kliknutí na Settings z menu/main
            case R.id.action_settings:
                Intent settings = new Intent(MainActivity.this, Settings.class);
                startActivity(settings);
                return true;
            case R.id.action_dummyData:
                insertDummyData();
                displayDatabaseInfo();
                return true;
            case R.id.action_add:
                Intent addIntent = new Intent(MainActivity.this, Shift.class);
                startActivity(addIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            Intent i = new Intent(MainActivity.this, Settings.class);                   // volani aktivity
            startActivity(i);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void insertDummyData (){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues shiftValues = new ContentValues();
            shiftValues.put(ShiftEntry.COLUMN_DATE, 20190222);
            shiftValues.put(ShiftEntry.COLUMN_ARRIVAL, 360);
            shiftValues.put(ShiftEntry.COLUMN_DEPARTURE, 915);
            shiftValues.put(ShiftEntry.COLUMN_BREAK_LENGHT, 30);
            shiftValues.put(ShiftEntry.COLUMN_SHIFT_LENGHT, 525);
            shiftValues.put(ShiftEntry.COLUMN_HOLIDAY, ShiftEntry.HOLIDAY_SHIFT);
        long newRowId =  db.insert(ShiftEntry.TABLE_NAME, null, shiftValues);
    }

    public void plusH(View view) {
        TextView tv2 = (TextView) findViewById(R.id.hhView);
        String hhStr = (String) tv2.getText();
        TextView tv = (TextView) findViewById(R.id.hView);
        String hStr = (String) tv.getText();
        int hh = Integer.parseInt(hhStr);
        int h = Integer.parseInt(hStr);
        int max = 0;
        if ((h < 2) && (hh < 4)){
            max = 2;
        } else {max = 1;};
        int smer = 1;                                                                                   // 1 je plus, 0 je minus
        int incremented = cycle(max, smer, hStr);
        String vysledek = Integer.toString(incremented);
        tv.setText(vysledek);
    }

    public void minusH(View view) {
        TextView tv2 = (TextView) findViewById(R.id.hhView);
        String hhStr = (String) tv2.getText();
        TextView tv = (TextView) findViewById(R.id.hView);
        String hStr = (String) tv.getText();
        int hh = Integer.parseInt(hhStr);
        int h = Integer.parseInt(hStr);
        int max = 0;
        if ((h < 2) && (hh < 5)){
            max = 2;
        } else {max = 1;};
        int smer = 0;                                                                                   // 1 je plus, 0 je minus
        int incremented = cycle(max, smer, hStr);
        String vysledek = Integer.toString(incremented);
        tv.setText(vysledek);
    }

    public void plusHH(View view) {
        TextView tv2 = (TextView) findViewById(R.id.hhView);
        String hhStr = (String) tv2.getText();
        TextView tv = (TextView) findViewById(R.id.hView);
        String hStr = (String) tv.getText();
        int hh = Integer.parseInt(hhStr);
        int h = Integer.parseInt(hStr);
        int max = 9;
        if (h == 2 && hh == 3 ){
            tv2.setText("0");
            tv.setText("0");
        } else {
            int smer = 1;                                                                               // 1 je plus, 0 je minus
            int incremented = cycle(max, smer, hhStr);
            String vysledek = Integer.toString(incremented);
            tv2.setText(vysledek);
        }
    }

    public void minusHH(View view) {
        TextView tv2 = (TextView) findViewById(R.id.hhView);
        String hhStr = (String) tv2.getText();
        TextView tv = (TextView) findViewById(R.id.hView);
        String hStr = (String) tv.getText();
        int hh = Integer.parseInt(hhStr);
        int h = Integer.parseInt(hStr);
        int max ;
        if (h == 2){
            max = 3;
        } else {max = 9;};
        int smer = 0;                                                                                   // 1 je plus, 0 je minus
        int incremented = cycle(max, smer, hhStr);
        String vysledek = Integer.toString(incremented);
        tv2.setText(vysledek);
    }

    public void plusM(View view) {
        TextView tv2 = (TextView) findViewById(R.id.mmView);
        String mmStr = (String) tv2.getText();
        TextView tv = (TextView) findViewById(R.id.mView);
        String mStr = (String) tv.getText();
        int mm = Integer.parseInt(mmStr);
        int m = Integer.parseInt(mStr);
        int max = 5;
        int smer = 1;                                                                                   // 1 je plus, 0 je minus
        int incremented = cycle(max, smer, mStr);
        String vysledek = Integer.toString(incremented);
        tv.setText(vysledek);
    }

    public void minusM(View view) {
        TextView tv = (TextView) findViewById(R.id.mView);
        String mStr = (String) tv.getText();
        int m = Integer.parseInt(mStr);
        int max = 5;
        int smer = 0;                                                                                   // 1 je plus, 0 je minus
        int incremented = cycle(max, smer, mStr);
        String vysledek = Integer.toString(incremented);
        tv.setText(vysledek);
    }

    public void plusMM(View view) {
        TextView tv2 = (TextView) findViewById(R.id.mmView);
        String mmStr = (String) tv2.getText();
        TextView tv = (TextView) findViewById(R.id.mView);
        String mStr = (String) tv.getText();
        int mm = Integer.parseInt(mmStr);
        int m = Integer.parseInt(mStr);
        int max = 9;
        if (m == 5 && mm == 9 ){
            tv2.setText("0");
            tv.setText("0");
        } else {
            int smer = 1;                                                                               // 1 je plus, 0 je minus
            int incremented = cycle(max, smer, mmStr);
            String vysledek = Integer.toString(incremented);
            tv2.setText(vysledek);
        }
    }

    public void minusMM(View view) {
        int max = 9;
        int smer = 0;                                                                                   // 1 je plus, 0 je minus
        TextView tv = (TextView) findViewById(R.id.mmView);
        String currentStr = (String) tv.getText();
        int incremented = cycle(max, smer, currentStr);
        String vysledek = Integer.toString(incremented);
        tv.setText(vysledek);
    }

    public int cycle(int max, int smer, String currentStr) {
        int current = Integer.parseInt(currentStr);
        int incremented = 0;
        if (smer==1) {
            if (current < max) {
                return (current + 1);
            }
            else {
                return (0);
            }
        } else {
            if (current > 0) {
                return (current - 1);
            }
            else {
                return (max);
            }
        }
    }

    public void shlukHodiny(View view) {
        int h, hh, m, mm = 0;
        TextView tv1 = (TextView) findViewById(R.id.hView);
        String hStr = (String) tv1.getText();
        TextView tv2 = (TextView) findViewById(R.id.hhView);
        String hhStr = (String) tv2.getText();
        TextView tv3 = (TextView) findViewById(R.id.mView);
        String mStr = (String) tv3.getText();
        TextView tv4 = (TextView) findViewById(R.id.mmView);
        String mmStr = (String) tv4.getText();
        h = Integer.parseInt(hStr);
        hh = Integer.parseInt(hhStr);
        m = Integer.parseInt(mStr);
        mm = Integer.parseInt(mmStr);
        TextView tv5 = (TextView) findViewById(R.id.hodinyString);
        tv5.setText(hStr + hhStr + ":" + mStr + mmStr);

    }

    public static class Globals {
        public static String whichTinme;
        public static int timeInHours;
        public static int timeInMinutes;
        public static int timeOutHours;
        public static int timeOutMinutes;
    }
}