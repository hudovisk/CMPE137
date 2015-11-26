package com.assignment.sjsu.hudoassenco.cmpe137;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;

public class ParseApplication extends Application {

    //See: http://stackoverflow.com/q/30135858
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "JKF0NbqkkyhUFhelLkqQhjbUMeMeqveLPwfcpbvv", "90ig345UBcaoriqawpM7iYfrnRueWqNlLYYGL70C");
        ParseFacebookUtils.initialize(this);
    }
}
