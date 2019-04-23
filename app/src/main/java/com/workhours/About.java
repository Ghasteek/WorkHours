package com.workhours;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class About extends AppCompatActivity {
    TextView mail, version;

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
        setContentView(R.layout.activity_about);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        version = findViewById(R.id.aboutSecondRowId);
        version.setText(getString(R.string.aboutSecondRow, MainActivity.Globals.versionStr));

        mail = findViewById(R.id.aboutFourthRowId);

        mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = "Docházka " + MainActivity.Globals.versionStr;
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","podolsky.tomas@gmail.com", null));
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                startActivity(Intent.createChooser(intent, "Choose an Email client :"));
            }
        });
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

}
