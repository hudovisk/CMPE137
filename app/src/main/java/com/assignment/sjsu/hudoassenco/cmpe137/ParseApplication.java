package com.assignment.sjsu.hudoassenco.cmpe137;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by hudoassenco on 11/24/15.
 */
public class ParseApplication extends Application {

    //See: http://stackoverflow.com/q/30135858
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "ZJwjmK7r9gPksXGBLJrKaMqYyukE3bynlAwC14OE", "utgkyU8HhbgxABHmvvntC3IgnBOqHhB7HyokBKUH");
    }
}
