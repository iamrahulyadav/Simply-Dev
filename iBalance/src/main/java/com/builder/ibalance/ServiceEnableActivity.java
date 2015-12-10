package com.builder.ibalance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.builder.ibalance.util.MyApplication;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class ServiceEnableActivity extends AppCompatActivity implements OnClickListener{

    //final static String tag = ServiceEnableActivity.class.getSimpleName();
    static int num_of_toggle = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_service_enable);
		
		Button nextButton = (Button) findViewById(R.id.splash_next_button);
		nextButton.setOnClickListener(this);
        ImageView gifImageView = (ImageView) findViewById(R.id.splash_simply_rec_img_id);

		AnimationDrawable anim = new AnimationDrawable();
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT || android.os.Build.MANUFACTURER.toUpperCase().contains("XIAOMI"))
        {
            //White Screen service enable
			anim.addFrame(getResources().getDrawable(R.drawable.service_white_1), 2000);
			anim.addFrame(getResources().getDrawable(R.drawable.service_white_2), 700);


            //Picasso.with(this).load(R.drawable.service_enable_white).into(gifImageView);
        }
        else
        {
            anim.addFrame(getResources().getDrawable(R.drawable.service_black_1), 2000);
            anim.addFrame(getResources().getDrawable(R.drawable.service_black_2), 700);
        }
		gifImageView.setBackgroundDrawable(anim);
		anim.setOneShot(false);
		anim.start();
        gifImageView.setOnClickListener(this);
		final TelephonyManager mtelTelephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery("SERVICE_STATUS");
		query.whereEqualTo("DEVICE_ID", mtelTelephonyManager.getDeviceId());
		// Retrieve the object by Device id
		query.addDescendingOrder("createdAt");
		query.getFirstInBackground(new GetCallback<ParseObject>() {
		  public void done(ParseObject simply_service_status, ParseException e) {
		    if (e == null)
            {// Now let's update it
                simply_service_status.put("SERVICE_STATUS", "OFF");
                simply_service_status.increment("SERVICE_TOGGLE_COUNT");
                simply_service_status.saveEventually();
		    }
            else
            {
                simply_service_status = new ParseObject("SERVICE_STATUS");
                simply_service_status.put("DEVICE_ID", mtelTelephonyManager.getDeviceId());
                simply_service_status.put("SERVICE_STATUS", "OFF");
                simply_service_status.put("APP_VERSION", BuildConfig.VERSION_CODE);
                simply_service_status.put("SERVICE_TOGGLE_COUNT",0);
                simply_service_status.saveEventually();

            }
		  }
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.service_enable, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
        num_of_toggle++;
        if(num_of_toggle>4)
        {
            Toast.makeText(MyApplication.context,"Please Restart your phone and\n          Try Again!",Toast.LENGTH_LONG).show();
        }
		switch (v.getId()) {
        case R.id.splash_simply_rec_img_id:
		case R.id.splash_next_button:
			LayoutInflater inflater = getLayoutInflater();
	          
	        // Call toast.xml file for toast layout 
	        View toastRoot = inflater.inflate(R.layout.custom_toast_layout, null);
	          
	        Toast toast = new Toast(getApplicationContext());
	         
	        // Set layout to toast 
	        toast.setView(toastRoot);
	        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,
	                0, 0);
	        toast.setDuration(Toast.LENGTH_LONG);
	        toast.show();
			Intent intent = new Intent(
					android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, 0);
			break;
		default:
			break;
		}
		
		
	}
}
