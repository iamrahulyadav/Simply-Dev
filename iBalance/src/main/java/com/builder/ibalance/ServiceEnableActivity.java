package com.builder.ibalance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class ServiceEnableActivity extends Activity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_service_enable);
		
		Button nextButton = (Button) findViewById(R.id.next_button);
		nextButton.setOnClickListener(this);
		ImageView info = (ImageView) findViewById(R.id.bal_rec_info);
		info.setOnClickListener(this);
		TelephonyManager mtelTelephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery("IBALANCE_USERS");
		query.whereEqualTo("DEVICE_ID", mtelTelephonyManager.getDeviceId());
		// Retrieve the object by Device id
		query.addDescendingOrder("createdAt");
		query.getFirstInBackground(new GetCallback<ParseObject>() {
		  public void done(ParseObject ibalanceUser, ParseException e) {
		    if (e == null) {
		      // Now let's update it 
		      ibalanceUser.put("SERVICE_STATUS", "OFF");
		      ibalanceUser.saveEventually();
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
		switch (v.getId()) {
		case R.id.next_button:
			LayoutInflater inflater = getLayoutInflater();
	          
	        // Call toast.xml file for toast layout 
	        View toastRoot = inflater.inflate(R.layout.custom_toast_layout, null);
	          
	        Toast toast = new Toast(getApplicationContext());
	         
	        // Set layout to toast 
	        toast.setView(toastRoot);
	        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,
	                0, 0);
	        toast.setDuration(20000);
	        toast.show();
			Intent intent = new Intent(
					android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
			startActivityForResult(intent, 0);
			break;
		case R.id.bal_rec_info:
			Toast.makeText(this, "Its a special service to record the balance messages!", Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
		
		
	}
}
