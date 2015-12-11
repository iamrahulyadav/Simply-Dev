package com.builder.ibalance;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;

import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.flurry.android.FlurryAgent;
import com.kahuna.sdk.Kahuna;

public class ContactUs extends AppCompatActivity implements OnClickListener
{
    final static String tag = ContactUs.class.getSimpleName();
    View conNumber, conMail, debuginfo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
       //V10AppsFlyerLib.sendTrackingWithEvent(MyApplication.context, "Contact Us", "");
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
  		    //V10Log.d("Shabaz Apptentive ", "unread messages  = "+unreadMessages);
  		    }
  		  }
  		);

}*/


    @Override
    protected void onStart()
    {
        FlurryAgent.logEvent("ContactUs", true);
        super.onStart();
        Kahuna.getInstance().start();
    }


    @Override
    protected void onStop()
    {
        FlurryAgent.endTimedEvent("ContactUs");
        super.onStop();
        Kahuna.getInstance().stop();
    }


    @Override
    public void onClick(View v)
    {
        Intent sendIntent;
        String deviceId = ((TelephonyManager) MyApplication.context.getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        switch (v.getId())
        {
            case R.id.whatapp_contact:
                //Intent intent = new Intent(Intent.ACTION_DIAL);
                if (!Helper.contactExists("+919739663487"))
                {
                   //V10Log.d(tag, "Whatsapp contact not found adding contact");
                    Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                    intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                    intent.putExtra(ContactsContract.Intents.Insert.EMAIL, "simplyappcontact@gmail.com")
                            .putExtra(ContactsContract.Intents.Insert.NAME, "Simply App")
                            .putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                            .putExtra(ContactsContract.Intents.Insert.PHONE,"+919739663487" )
                            .putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);

                    startActivity(intent);
                } else
                {
                    startActivity(Helper.openWhatsApp("+919739663487", deviceId));
                }

                //startActivity(intent);
                break;
            case R.id.mail_contact:
                sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/html");
                //sendIntent.setType("message/rfc822");
                sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"simplyappcontact@gmail.com"});
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Simply FeedBack");
                sendIntent.putExtra(Intent.EXTRA_TEXT, "-----Support Info---------\n" + deviceId + "\n-------Don't delete--------\n");
                startActivity(Intent.createChooser(sendIntent, "Send FeedBack"));
                break;
            case R.id.share_debug_button:
                sendIntent = new Intent();
                try
                {

                    PackageInfo info = getPackageManager().getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
                    sendIntent.setPackage("com.whatsapp");

                } catch (Exception e)
                {
                    // some code
                }

                sendIntent.setAction(Intent.ACTION_SEND);
                String sendString = "-----Support Info-------\n" + deviceId + "\n-----------------------";
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, sendString);
                startActivity(sendIntent);

            default:
                break;
        }

    }
}

