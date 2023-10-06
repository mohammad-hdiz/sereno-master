package com.hdiz.datacollection.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.hdiz.datacollection.R;
import com.hdiz.datacollection.fragments.IdentifierFragment;
import com.hdiz.datacollection.services.MyJobService;

import java.io.File;
import java.util.Random;


public class SplashActivity extends AppCompatActivity {

    public static final String DB_NAME_PREF = "db_name";
    public static final String IDENTIFIER_PREF = "identifier";
    private String identifier_str;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initJObScheduler();

        if(!preferenceFileExist(DB_NAME_PREF,getApplicationContext())) {
            SharedPreferences.Editor editor = getSharedPreferences(DB_NAME_PREF, MODE_PRIVATE).edit();
            editor.putString("id", getSaltString());
            editor.apply();
        }


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(preferenceFileExist(IDENTIFIER_PREF, getApplicationContext())){
                    SharedPreferences prefs = getSharedPreferences(IDENTIFIER_PREF, MODE_PRIVATE);
                    identifier_str = prefs.getString("identifier", "null"); //"null" is the default value.

                    Intent intent = new Intent(SplashActivity.this, PatientListActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Call Only, if you wants to clears the activity stack else ignore it.
                    intent.putExtra("identifier",identifier_str);
                    startActivity(intent);
                    finish();
                }
                else{
                    IdentifierFragment identifierFragment = new IdentifierFragment(getApplicationContext());
                    identifierFragment.show(getFragmentManager(),"identifier");
                }

            }
        }, 3000);

       }

    public static boolean preferenceFileExist(String fileName, Context context) {
        File f = new File(context.getApplicationInfo().dataDir + "/shared_prefs/"
                + fileName + ".xml");
        return f.exists();
    }
    private String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 5) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    private void initJObScheduler(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ComponentName componentName = new ComponentName(this, MyJobService.class);
            JobInfo.Builder builder = new JobInfo.Builder(101,componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                builder.setPeriodic(15*60*1000,30*60*1000);
            }else {
                builder.setPeriodic(15 * 60 * 1000);
            }

            JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        }
    }

}