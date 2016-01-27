package com.builder.ibalance;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.builder.ibalance.util.ConstantsAndStatics;
import com.flurry.android.FlurryAgent;

public class ServiceEnableTranslucent extends AppCompatActivity implements View.OnClickListener
{
    ImageView imageView ;
    boolean firstTimeHint = true;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_enable_translucent);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Button mButton = (Button) findViewById(R.id.got_it);
        imageView = (ImageView) findViewById(R.id.recorder_on_img);


            if(!BuildConfig.DEBUG)
            {
                SharedPreferences userDataPref = getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY,MODE_PRIVATE);
                firstTimeHint = userDataPref.getBoolean("FIRST_SERVICE_HINT",true);

                if(firstTimeHint)
                {
                    FlurryAgent.logEvent("ONBOARD_HINT_SHOWN");
                    userDataPref.edit().putBoolean("FIRST_SERVICE_HINT",false).apply();
                }
                else {
                    FlurryAgent.logEvent("ONBOARD_REPEAT_HINT_SHOWN");
                }
            }
        if(!Build.MANUFACTURER.toUpperCase().contains("XIAOMI")&& (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT || Build.MANUFACTURER.toUpperCase().contains("SAMSUNG") || Build.MANUFACTURER.toUpperCase().contains("LG")|| Build.MANUFACTURER.toUpperCase().contains("SONY") ))
        {
            imageView.setImageResource(R.drawable.service_enable_black);
        }
        else
        {
            imageView.setImageResource(R.drawable.service_enable_white);
        }
        imageView.setOnClickListener(this);
        mButton.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v)
    {
        if(!BuildConfig.DEBUG)
        {


            if(firstTimeHint)
            {
                FlurryAgent.logEvent("ONBOARD_HINT_CLICKED");
            }
            else
            {
                FlurryAgent.logEvent("ONBOARD_REPEAT_HINT_CLICKED");
            }
        }
        LayoutInflater inflater = getLayoutInflater();

        // Call toast.xml file for toast layout
	       View toastRoot = inflater.inflate(R.layout.custom_toast_layout, null);

	        Toast toast = new Toast(getApplicationContext());

	        // Set layout to toast
	        toast.setView(toastRoot);
	        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_VERTICAL|Gravity.FILL_HORIZONTAL,
	                0, 0);
	        toast.setDuration(Toast.LENGTH_LONG);
        for(int i=0;i<2;i++)
	        toast.show();
        this.finish();
    }
}
