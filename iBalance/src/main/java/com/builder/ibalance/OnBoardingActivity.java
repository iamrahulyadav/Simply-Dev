package com.builder.ibalance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.builder.ibalance.util.ConstantsAndStatics;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.flurry.android.FlurryAgent;
import com.kahuna.sdk.Kahuna;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class OnBoardingActivity extends Activity implements OnClickListener{

    final static String tag = OnBoardingActivity.class.getSimpleName();
    static int num_of_toggle = 0;
    ImageView recorderOn, refreshBal;
    TextView downloadText,recorderText,refreshText,percentageText,contactUs,doItLater;
    ImageButton closeButton;
    //Button nextButton;
    ProgressBar mProgressBar;
    View recorderOnView,refreshBalView;
    SharedPreferences userDataPref;
    boolean isEnabledAccess =false;
    boolean firstTimeOnBoard = true;


    @Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_onboarding);

        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT); //set below the setContentview


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
        doItLater = (TextView) findViewById(R.id.do_it_later);
        doItLater.setOnClickListener(this);
        closeButton = (ImageButton) findViewById(R.id.close_button);
        closeButton.setOnClickListener(this);/*
        nextButton = (Button) findViewById(R.id.splash_next_button);
        nextButton.setOnClickListener(this);*/
        mProgressBar = (ProgressBar) findViewById(R.id.onboarding_progress_bar);

        recorderOn = (ImageView) findViewById(R.id.recorder_on);
        refreshBal = (ImageView) findViewById(R.id.refresh_bal);
        downloadText.setPaintFlags(downloadText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        contactUs.setPaintFlags(contactUs.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        doItLater.setPaintFlags(doItLater.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        userDataPref = getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY,MODE_PRIVATE);

            firstTimeOnBoard =userDataPref.getBoolean("FIRST_ONBOARD",true);
            if(firstTimeOnBoard)
            {
                Helper.logGA("ONBOARD","START");
                Helper.logFlurry("ONBOARD","ACTION","START");

                userDataPref.edit().putBoolean("FIRST_ONBOARD",false).apply();
            }
            else {
                Helper.logGA("ONBOARD_REPEAT","START");
                Helper.logFlurry("ONBOARD_REPEAT","ACTION","START");
            }
        boolean hasRefreshedBal = userDataPref.getBoolean("REFRESHED_BAL",false);

        float currentBal = userDataPref.getFloat("CURRENT_BALANCE_0",userDataPref.getFloat("CURRENT_BALANCE_1",-200.0f));
        if(currentBal>-20.0f)
            hasRefreshedBal = true;
        if(hasRefreshedBal)
        {
            refreshBal.setImageResource(R.drawable.checked);
            refreshText.setPaintFlags(refreshText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            progress += 33;
        }
        isEnabledAccess = Helper.isAccessibilityEnabled(ConstantsAndStatics.accessibiltyID);
        if(isEnabledAccess)
        {
            recorderOn.setImageResource(R.drawable.checked);
            recorderText.setPaintFlags(recorderText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            //nextButton.setText("Turn ON Recorder >");
            progress +=34;
        }
        else
        {
            refreshBalView.setBackgroundResource(R.color.grey);
        }
        percentageText.setText(progress+"%");
        mProgressBar.setProgress(progress);

        if(!isEnabledAccess)
        {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("SERVICE_STATUS");
            query.whereEqualTo("DEVICE_ID", Helper.getDeviceId());
            // Retrieve the object by Device id
            query.addDescendingOrder("createdAt");
            query.getFirstInBackground(new GetCallback<ParseObject>()
            {
                public void done(ParseObject simply_service_status, ParseException e)
                {
                    if (e == null)
                    {// Now let's update it
                        Helper.logGA("SERVICE","OFF");
                        Helper.logFlurry("SERVICE","STATUS","OFF");
                        simply_service_status.put("SERVICE_STATUS", "OFF");
                        simply_service_status.increment("SERVICE_TOGGLE_COUNT");
                        simply_service_status.saveEventually();
                /*Tracker t = ((MyApplication) OnBoardingActivity.this.getApplication()).getTracker(
                        MyApplication.TrackerName.APP_TRACKER);
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("SERVICE_STATUS")
                        .setAction("OFF")
                        .setLabel(Helper.getDeviceId())
                        .build());
                Map<String, String> params = new HashMap<String, String>();
                params.put("STATUS", "OFF");
                params.put("DEVICE_ID", Helper.getDeviceId());
                FlurryAgent.logEvent("SERVICE",params);*/
                    } else
                    {
                        simply_service_status = new ParseObject("SERVICE_STATUS");
                        simply_service_status.put("DEVICE_ID", Helper.getDeviceId());
                        simply_service_status.put("SERVICE_STATUS", "OFF");
                        simply_service_status.put("APP_VERSION", BuildConfig.VERSION_CODE);
                        simply_service_status.put("SERVICE_TOGGLE_COUNT", 0);
                        simply_service_status.saveEventually();

                    }
                }
            });
        }
		
	}


    /**
     * Called when the activity has detected the user's press of the back
     * key.  The default implementation simply finishes the current activity,
     * but you can override this to do whatever you want.
     */
    @Override
    public void onBackPressed()
    {
        Helper.logGA("ONBOARD","DISMISS");
        Helper.logFlurry("ONBOARD","ACTION","DISMISS");
        super.onBackPressed();
    }

    @Override
	public void onClick(View v)
    {
        if(num_of_toggle>3)
        {
            Toast.makeText(MyApplication.context,"Please Restart your phone and Try Again!",Toast.LENGTH_LONG).show();
        }
        Intent intent;
		switch (v.getId()) {
        case R.id.recorder_on_layout:
            num_of_toggle++;
                if(firstTimeOnBoard)
                {
                    Helper.logGA("ONBOARD","GO_TO_SETTINGS");
                    Helper.logFlurry("ONBOARD","ACTION","GO_TO_SETTINGS");
                }
                else
                {
                    Helper.logGA("ONBOARD_REPEAT","GO_TO_SETTINGS");
                    Helper.logFlurry("ONBOARD_REPEAT","ACTION","GO_TO_SETTINGS");
                }
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
                    boolean firstRefresh =userDataPref.getBoolean("FIRST_REFRESH",true);
                    if(firstRefresh)
                    {
                        Helper.logGA("ONBOARD","REFRESH_CLICKED");
                        Helper.logFlurry("ONBOARD","ACTION","REFRESH_CLICKED");

                        userDataPref.edit().putBoolean("FIRST_REFRESH",false).apply();
                    }
                    else {
                        //This should not come if it is Repeat
                        Helper.logGA("ONBOARD_REPEAT","REFRESH_CLICKED");
                        Helper.logFlurry("ONBOARD_REPEAT","ACTION","REFRESH_CLICKED");
                    }
                userDataPref.edit().putBoolean("REFRESHED_BAL",true).apply();
                startActivity((new Intent(this,BalanceRefreshActivity.class)).putExtra("SIM_SLOT",0).putExtra("TYPE","MAIN_BAL"));
                this.finish();
            }

            break;
		/*case R.id.splash_next_button:
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
			break;*/
        case R.id.onboarding_contact:
            if(!BuildConfig.DEBUG)
            {
                if (firstTimeOnBoard) {
                    Helper.logGA("ONBOARD","CONTACT");
                    Helper.logFlurry("ONBOARD","ACTION","CONTACT");
                }
                else {
                    Helper.logGA("ONBOARD_REPEAT","CONTACT");
                    Helper.logFlurry("ONBOARD_REPEAT","ACTION","CONTACT");
                }
            }
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
                //Sending Device Id does work B-)
                ConstantsAndStatics.PASTE_DEVICE_ID = true;
                startActivity(Helper.openWhatsApp("+919739663487", ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId()));
            }
                break;
            case R.id.do_it_later:
            case R.id.close_button:
                    if (firstTimeOnBoard) {
                        Helper.logGA("ONBOARD","DISMISS");
                        Helper.logFlurry("ONBOARD","ACTION","DISMISS");
                    }
                    else {
                        Helper.logGA("ONBOARD_REPEAT","DISMISS");
                        Helper.logFlurry("ONBOARD_REPEAT","ACTION","DISMISS");
                    }
                finish();
                break;
		default:
			break;
		}
		
		
	}
    @Override
    protected void onStart() {
        super.onStart();

        Kahuna.getInstance().start();
        FlurryAgent.logEvent("OnBoardActivity", true);
    }

    @Override
    protected void onStop() {
        Kahuna.getInstance().stop();
        FlurryAgent.endTimedEvent("OnBoardActivity");
        super.onStop();
    }

}
