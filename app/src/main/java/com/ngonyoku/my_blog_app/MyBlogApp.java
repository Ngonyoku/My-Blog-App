package com.ngonyoku.my_blog_app;

import android.app.Application;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class MyBlogApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        /*Picasso Offline Capabilities*/
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);/*Allows us to Identify where the Image was loaded from.
         It's responsible for the little triangle at the top left corner of the image*/
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }
}
