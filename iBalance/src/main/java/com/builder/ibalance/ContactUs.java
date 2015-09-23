package com.builder.ibalance;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.appsflyer.AppsFlyerLib;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.module.messagecenter.UnreadMessagesListener;
import com.builder.ibalance.util.MyApplication;
import com.flurry.android.FlurryAgent;
import com.kahuna.sdk.KahunaAnalytics;

public class ContactUs extends Activity implements OnClickListener {
	TextView conNumber,conMail;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_us);
		Apptentive.engage(this, "Contact US");
		AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"Contact Us","");
		conNumber = (TextView) findViewById(R.id.contactus_number);
		conMail = (TextView) findViewById(R.id.contactus_mail);
		conNumber.setOnClickListener(this);
		conMail.setOnClickListener(this);
		Button messageCenterButton = (Button)findViewById(R.id.chat_now);
		messageCenterButton.setOnClickListener(new View.OnClickListener(){
		  public void onClick(View v) {
			  showApptentiveChat();
			
		  }
		});

				
	}

void showApptentiveChat()
{
	SharedPreferences mPreferences = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
	Map<String, String> customData = new HashMap<String, String>();
	
	customData.put("DEVICE_ID", mPreferences.getString("DEVICE_ID", "0000"));

	Apptentive.engage(ContactUs.this, "Simply Support");
    Apptentive.showMessageCenter(ContactUs.this,customData);
    Apptentive.setUnreadMessagesListener(
  		  new UnreadMessagesListener() {
  		    public void onUnreadMessageCountChanged(final int unreadMessages) {
  		     Log.d("Shabaz Apptentive ", "unread messages  = "+unreadMessages);
  		    }
  		  }
  		);

}


	@Override
	protected void onStart() {
		FlurryAgent.logEvent("ContactUs", true);
		super.onStart();
        KahunaAnalytics.start();
	}




	@Override
	protected void onStop() {
		FlurryAgent.endTimedEvent("ContactUs");
		super.onStop();
        KahunaAnalytics.stop();
	}




	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.contactus_number:
			Intent intent = new Intent(Intent.ACTION_DIAL);
			intent.setData(Uri.parse("tel:09739663487"));
			startActivity(intent); 
			break;
		case R.id.contactus_mail:
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.setType("text/html");
			//sendIntent.setType("message/rfc822");
			sendIntent.putExtra(Intent.EXTRA_EMAIL  , new String[]{"simplyappcontact@gmail.com"});
			//sendIntent.putExtra(Intent.EXTRA_EMAIL, "ibalanceapp@gmail.com");
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Simply FeedBack");
			sendIntent.putExtra(Intent.EXTRA_TEXT, "");
			startActivity(Intent.createChooser(sendIntent, "Send FeedBack"));
			break;
		default:
			break;
		}
		
	}
}
