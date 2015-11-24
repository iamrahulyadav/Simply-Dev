package com.builder.ibalance;

import android.app.Activity;

import android.os.Bundle;

import android.view.Window;
import android.view.WindowManager;
import io.fabric.sdk.android.Fabric;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.digits.sdk.android.Digits;

public class DigitLoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splashscreen);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_digit_login);
        TwitterAuthConfig authConfig =  new TwitterAuthConfig("consumerKey", "consumerSecret");
        Fabric.with(this, new TwitterCore(authConfig), new Digits());
    }


}
