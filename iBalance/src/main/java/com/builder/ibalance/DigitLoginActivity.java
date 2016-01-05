package com.builder.ibalance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.builder.ibalance.util.MyApplication;
import com.digits.sdk.android.AuthCallback;
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
                SharedPreferences.Editor mEditor = mSharedPreferences.edit();
                mEditor.putBoolean("USER_VERIFIED",true);
                mEditor.putString("VERIFIED_NUMBER",phoneNumber);
                mEditor.commit();
                Intent intent  = new Intent(getApplicationContext(),SplashscreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }

            @Override
            public void failure(DigitsException exception) {
                // Do something on failure
                Toast.makeText(MyApplication.context,"Error: Skipping Login",Toast.LENGTH_LONG);
                Intent intent  = new Intent(getApplicationContext(),SplashscreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        };
     }

    public AuthCallback getAuthCallback(){
        return authCallback;
    }





}



