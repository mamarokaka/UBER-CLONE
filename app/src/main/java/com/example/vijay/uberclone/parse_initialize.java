package com.example.vijay.uberclone;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

/**
 * Created by vijay on 8/20/18.
 */

public class parse_initialize extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("bb263083e97b59ae5f932361205ed1dc98b9e427")
                // if defined
                .clientKey("9ab235da1f24262e7dfd37f6c7274f123d1c11b8")
                .server("http://18.222.196.7:80/parse")
                .build()
        );

        //ParseUser.enableAutomaticUser();
        ParseACL defaultACL =new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL,true);
    }
}
