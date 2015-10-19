package com.builder.ibalance;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.appsflyer.AppsFlyerLib;
import com.apptentive.android.sdk.Apptentive;
import com.builder.ibalance.util.MyApplication;
import com.flurry.android.FlurryAgent;
import com.kahuna.sdk.KahunaAnalytics;

public class ContactUs extends Activity implements OnClickListener {
    final static String tag = ContactUs.class.getSimpleName();
	View conNumber,conMail,debuginfo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_us);
		Apptentive.engage(this, "Contact US");
		AppsFlyerLib.sendTrackingWithEvent(MyApplication.context, "Contact Us", "");
		conNumber = (View) findViewById(R.id.whatapp_contact);
		conMail = (View) findViewById(R.id.mail_contact);
		debuginfo = (View) findViewById(R.id.share_debug_button);
		conNumber.setOnClickListener(this);
        debuginfo.setOnClickListener(this);
		conMail.setOnClickListener(this);


				
	}

/*void showApptentiveChat()
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

}*/


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
        Intent sendIntent;
        String deviceId = ((TelephonyManager) MyApplication.context.getSystemService(TELEPHONY_SERVICE)).getDeviceId();
		switch (v.getId()) {
		case R.id.whatapp_contact:
			//Intent intent = new Intent(Intent.ACTION_DIAL);
            if(!openWhatsApp("919739663487",deviceId))
            {
                Log.d(tag,"Whatsapp contact not found adding contact");
                Intent addContactIntent = new Intent(Contacts.Intents.Insert.ACTION, Contacts.People.CONTENT_URI);
                addContactIntent.putExtra(Contacts.Intents.Insert.NAME, "Simply App Support"); // an example, there is other data available
                addContactIntent.putExtra(Contacts.Intents.Insert.PHONE, "+919739663487");
                startActivity(addContactIntent);
            }

			//startActivity(intent);
			break;
		case R.id.mail_contact:
			sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.setType("text/html");
			//sendIntent.setType("message/rfc822");
			sendIntent.putExtra(Intent.EXTRA_EMAIL  , new String[]{"simplyappcontact@gmail.com"});
			//sendIntent.putExtra(Intent.EXTRA_EMAIL, "ibalanceapp@gmail.com");
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Simply FeedBack");
			sendIntent.putExtra(Intent.EXTRA_TEXT, "-----Support Info---------\n" + deviceId + "-------Don't delete--------\n");
			startActivity(Intent.createChooser(sendIntent, "Send FeedBack"));
			break;
        case  R.id.share_debug_button:
            sendIntent = new Intent();
            try{

                PackageInfo info=getPackageManager().getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
                sendIntent.setPackage("com.whatsapp");

            } catch (Exception e){
                // some code
            }

            sendIntent.setAction(Intent.ACTION_SEND);
            String sendString = "-----Support Info---------\n"+deviceId+"--------------------------------g";
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, sendString);
            startActivity(sendIntent);

            default:
			break;
		}
		
	}
    public boolean openWhatsApp( String number,String deviceID) {
/// number is the phone number
        Uri mUri = Uri.parse("smsto:+"+number);
        Intent mIntent = new Intent(Intent.ACTION_SENDTO, mUri);
        mIntent.setPackage("com.whatsapp");
        mIntent.putExtra("sms_body", deviceID);
        mIntent.putExtra("chat", true);
        startActivity(mIntent);
        return  true;
        /*String whatsappid = number+"@s.whatsapp.net";
        Log.d(tag,"Whatsapp id = "+whatsappid);
        Cursor c = MyApplication.context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Contacts.Data._ID}, ContactsContract.Data.DATA1 + "=?",
                new String[]{whatsappid}, null);
        if(c.moveToFirst())
        {
            Log.d(tag, "ContactsContract.Contacts.Data._ID = " + c.getString(0));
            Intent whatsapp = new Intent(Intent.ACTION_VIEW, Uri.parse("content://com.android.contacts/data/" + c.getString(0)));

            c.close();

            if (whatsapp != null)
            {
                whatsapp.setPackage("com.whatsapp");
                Log.d(tag,"Sending text using Whatsapp");
                whatsapp.setType("text/plain");
                whatsapp.putExtra(Intent.EXTRA_TEXT, "-----SupportInfo-----\n" + deviceID + "------------------");
                startActivity(whatsapp);
                return true;
            }
            else {

                Log.d(tag,"Whatsapp Intent was null");
                return false;
            }

        }
        else
        {
            c.close();
            Log.d(tag,"Whatsapp Look up failed");
            return false;
        }*/
        /*Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER};
        Cursor cur = MyApplication.context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;*/
    }
}
