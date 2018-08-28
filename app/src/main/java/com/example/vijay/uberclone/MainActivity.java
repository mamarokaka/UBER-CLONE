package com.example.vijay.uberclone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends AppCompatActivity {

    public void startRiderActivity(){
        if (ParseUser.getCurrentUser().get("userOrDriver")=="rider"){
            Intent intent=new Intent(getApplicationContext(),riderActivity.class);
            startActivity(intent);

        }
        else {
            Intent intent=new Intent(getApplicationContext(),seeRequestListActivity.class);
            startActivity(intent);
        }
    }

    public void getStarted(View view){
        Switch button=findViewById(R.id.switch1);
        String currentType="rider";
        if (button.isChecked()){
            currentType="driver";
        }
        ParseUser.getCurrentUser().put("userOrDriver",currentType);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e==null){
                    startRiderActivity();
                }
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        if (ParseUser.getCurrentUser()==null){
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e==null){
                        Log.i("log","user logged in");
                    }
                    else Log.i("log","user logged in failed");
                }
            });
        }/*else {
            if (ParseUser.getCurrentUser().get("userOrDriver")!=null){
                Log.i("current user","logged in successfully"+ParseUser.getCurrentUser().get("userOrDriver"));
                startRiderActivity();
            }
        }*/

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }
}
