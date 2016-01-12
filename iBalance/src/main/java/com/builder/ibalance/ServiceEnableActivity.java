package com.builder.ibalance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.builder.ibalance.util.ConstantsAndStatics;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.HashMap;
import java.util.Map;

public class ServiceEnableActivity extends Activity implements OnClickListener{

    final static String tag = ServiceEnableActivity.class.getSimpleName();
    static int num_of_toggle = 0;
    ImageView recorderOn, refreshBal;
    String accessibiltyID = "com.builder.ibalance/.services.RecorderUpdaterService";
    TextView downloadText,recorderText,refreshText,percentageText,contactUs;
    Button nextButton;
    ProgressBar mProgressBar;
    View recorderOnView,refreshBalView;
    SharedPreferences userDataPref;
    boolean isEnabledAccess =false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_service_enable);

        int progress = 33;
        recorderOnView = findViewById(R.id.recorder_on_layout);
        recorderOnView.setOnClickListener(this);
        refreshBalView = findViewById(R.id.refresh_bal_layout);
        refreshBalView.setOnClickListener(this);
		downloadText = (TextView) findViewById(R.id.download_text);
        recorderText = (TextView) findViewById(R.id.recorder_text);
        refreshText = (TextView) findViewById(R.id.refresh_text);
        percentageText = (TextView) findViewById(R.id.progress_percentage);
        contactUs = (TextView) findViewById(R.id.onboarding_contact);
        contactUs.setOnClickListener(this);
        nextButton = (Button) findViewById(R.id.splash_next_button);
        nextButton.setOnClickListener(this);
        mProgressBar = (ProgressBar) findViewById(R.id.onboarding_progress_bar);

        recorderOn = (ImageView) findViewById(R.id.recorder_on);
        refreshBal = (ImageView) findViewById(R.id.refresh_bal);
        downloadText.setPaintFlags(downloadText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        contactUs.setPaintFlags(contactUs.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        userDataPref = getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY,MODE_PRIVATE);
        boolean hasRefreshedBAl = userDataPref.getBoolean("REFRESHED_BAL",false);
        float currentBal = userDataPref.getFloat("CURRENT_BALANCE_0",userDataPref.getFloat("CURRENT_BALANCE_1",-20.0f));
        if(currentBal>0.0f)
            hasRefreshedBAl = true;
        if(hasRefreshedBAl)
        {
            refreshBal.setImageResource(R.drawable.checked);
            refreshText.setPaintFlags(refreshText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            progress += 33;
        }
        isEnabledAccess = Helper.isAccessibilityEnabled(accessibiltyID);
        if(isEnabledAccess)
        {
            recorderOn.setImageResource(R.drawable.checked);
            recorderText.setPaintFlags(recorderText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            nextButton.setText("Turn ON Recorder >");
            progress +=33;
        }
        else
        {
            refreshBalView.setBackgroundResource(R.color.grey);
        }
        if(isEnabledAccess && hasRefreshedBAl==false)
        {
            nextButton.setText( "Refresh Balance >");
        }
        percentageText.setText(progress+"%");
        mProgressBar.setProgress(progress);
        /*ImageView gifImageView = (ImageView) findViewById(R.id.splash_simply_rec_img_id);

		AnimationDrawable anim = new AnimationDrawable();
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT || Build.MANUFACTURER.toUpperCase().contains("XIAOMI"))
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
        gifImageView.setOnClickListener(this);*/
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
                Tracker t = ((MyApplication) ServiceEnableActivity.this.getApplication()).getTracker(
                        MyApplication.TrackerName.APP_TRACKER);
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("SERVICE_STATUS")
                        .setAction("OFF")
                        .setLabel(Helper.getDeviceId())
                        .build());
                Map<String, String> params = new HashMap<String, String>();
                params.put("STATUS", "OFF");
                params.put("DEVICE_ID", Helper.getDeviceId());
                FlurryAgent.logEvent("SERVICE",params);
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
	public void onClick(View v)
    {
        if(num_of_toggle>4)
        {
            Toast.makeText(MyApplication.context,"Please Restart your phone and Try Again!",Toast.LENGTH_LONG).show();
        }
        Intent intent;
		switch (v.getId()) {
        case R.id.recorder_on_layout:
            num_of_toggle++;
            intent= new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, 0);
            intent = new Intent(this, ServiceEnableTranslucent.class);
            startActivity(intent);
            break;
        case R.id.refresh_bal_layout:
            if(!isEnabledAccess)
            {
                Toast.makeText(MyApplication.context,"Please Turn on Simply Recorder First",Toast.LENGTH_LONG).show();
            }
            else
            {
                //userDataPref.edit().putBoolean("REFRESHED_BAL",true).apply();
                refreshBalance();
            }

            break;
		case R.id.splash_next_button:
            if(!isEnabledAccess)
            {
                num_of_toggle++;
                intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);

                startActivityForResult(intent, 0);
                intent = new Intent(this, ServiceEnableTranslucent.class);
                startActivity(intent);
            }
            else
            {
                refreshBalance();
                //userDataPref.edit().putBoolean("REFRESHED_BAL",true).apply();
            }
			break;
        case R.id.onboarding_contact:
            if (!Helper.contactExists("+919739663487"))
            {
                //V10Log.d(tag, "Whatsapp contact not found adding contact");
                //add number and email to the contact
                Intent contactIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
                contactIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                contactIntent.putExtra(ContactsContract.Intents.Insert.EMAIL, "simplyappcontact@gmail.com").putExtra(ContactsContract.Intents.Insert.NAME, "Simply App").putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK).putExtra(ContactsContract.Intents.Insert.PHONE, "+919739663487").putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);

                startActivity(contactIntent);
            } else
            {
                //Sending Device Id doesn't work
                startActivity(Helper.openWhatsApp("+919739663487", ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId()));
            }
                break;
		default:
			break;
		}
		
		
	}

    private void refreshBalance()
    {
        startActivity((new Intent(this,BalanceRefreshActivity.class)).putExtra("SIM_SLOT",0).putExtra("TYPE","MAIN_BAL"));
    }
}
