package com.builder.ibalance;

import android.app.Activity;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.builder.ibalance.util.MyApplication;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;

public class DigitLoginActivity extends MyApplication{
    private AuthCallback authCallback,mauthCallback;

    @Override
    public void onCreate() {
             super.onCreate();
        authCallback = new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                // Do something with the session
                SharedPreferences mSharedPreferences = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
                mSharedPreferences.edit().putBoolean("USER_VERIFIED",true).commit();
                boolean IsVerified = mSharedPreferences.getBoolean("USER_VERIFIED",false);
                Intent intent  = new Intent(getApplicationContext(),SplashscreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }

            @Override
            public void failure(DigitsException exception) {
                // Do something on failure
            }
        };
     }

    public AuthCallback getAuthCallback(){
        return authCallback;
    }





}



