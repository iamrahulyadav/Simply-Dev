package com.builder.ibalance;

import android.app.Activity;

import android.os.Bundle;

import android.view.Window;
import android.view.WindowManager;

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
    }


}
