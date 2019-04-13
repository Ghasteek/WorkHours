package com.workhours;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.CompoundButton;


@SuppressWarnings("WeakerAccess")
public class SettingsTheme extends AppCompatActivity {
    SharedPreferences pref;
    Switch darkModeSwitch;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case (android.R.id.home):
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unused")
    private void showRestartDialog(
            DialogInterface.OnClickListener deleteButtonClickListener, Boolean isChecked) {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        final String checked = isChecked.toString();
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
        builder.setMessage(R.string.restart_app_dialog_msg);
        builder.setPositiveButton(R.string.shutdown, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (checked.equals("true")){
                    pref.edit().putString("layout", "dark").apply();
                } else {
                    pref.edit().putString("layout", "light").apply();
                }
                Intent mStartActivity = new Intent(SettingsTheme.this, MainActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(SettingsTheme.this, mPendingIntentId, mStartActivity,
                        PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) SettingsTheme.this.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                //System.exit(0);
            }
        });
        // Create and show the AlertDialog
        AlertDialog restartDialog = builder.create();
        restartDialog.show();
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
        setContentView(R.layout.activity_settings_theme);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        darkModeSwitch = findViewById(R.id.darkModeSwitchId);


        if (pref.contains("layout")){
            String savedLayout = pref.getString("layout", "light");
            if (savedLayout != null && savedLayout.equals("light")){
                darkModeSwitch.setChecked(false);
            } else {darkModeSwitch.setChecked(true);}
        }

        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //Toast.makeText(SettingsTheme.this, "posilam -" + isChecked, Toast.LENGTH_SHORT).show();

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        };
                showRestartDialog(discardButtonClickListener, isChecked);
            }
        });
    }
}
