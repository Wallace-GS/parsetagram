package com.codepath.parsetagram;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Post.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("2IFaYJgtQcmGqrgNwGbG2iOn9JUhbMUrY5mdvPlA")
                .clientKey("aoOhaLUiKgo0iywiw1oeOXyrdjK9s3TrcdMeRh92")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
