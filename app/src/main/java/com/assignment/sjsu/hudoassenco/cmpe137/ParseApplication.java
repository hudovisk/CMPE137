package com.assignment.sjsu.hudoassenco.cmpe137;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    //See: http://stackoverflow.com/q/30135858
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Album.class);
        ParseObject.registerSubclass(Photo.class);
        ParseObject.registerSubclass(Comment.class);
        ParseObject.registerSubclass(Feed.class);

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "JKF0NbqkkyhUFhelLkqQhjbUMeMeqveLPwfcpbvv", "90ig345UBcaoriqawpM7iYfrnRueWqNlLYYGL70C");
        ParseFacebookUtils.initialize(this);
    }
}
