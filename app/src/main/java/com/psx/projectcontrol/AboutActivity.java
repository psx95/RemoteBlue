package com.psx.projectcontrol;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class AboutActivity extends AppCompatActivity {

    RelativeLayout contactUs, rateUs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setupActionBar();
        contactUs = (RelativeLayout) findViewById(R.id.contact_us_about);
        rateUs = (RelativeLayout) findViewById(R.id.rate_us_about);
        contactUs.setOnClickListener(listener);
        rateUs.setOnClickListener(listener);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.contact_us_about){
                // send action intent for email
                Log.d("OnClick","recieved");
                sendEmail();
            }
            else if (id == R.id.rate_us_about){
                rateUs();
            }
        }
    };

    private void sendEmail ()
    {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("mailto:"));
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL,new String[] { "remoteblue@gmail.com" });
        intent.putExtra(Intent.EXTRA_SUBJECT,"Your Subject");
        try {
            startActivity(Intent.createChooser(intent,"Send Email Using"));
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(),"No Email client found on device",Toast.LENGTH_SHORT).show();
        }


    }

    private void rateUs () {
        try{
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.psx.ProjectControl")));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
