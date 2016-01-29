package com.builder.ibalance;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.builder.ibalance.messages.BalanceRefreshMessage;
import com.builder.ibalance.messages.UpdateBalanceOnScreen;
import com.builder.ibalance.util.ConstantsAndStatics;
import com.builder.ibalance.util.GlobalData;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.ReflectionHelper;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.parse.ParseObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;

public class BalanceRefreshActivity extends AppCompatActivity implements View.OnClickListener
{
    final String TAG = BalanceRefreshActivity.class.getSimpleName();
    SharedPreferences userPref;
    JSONObject ussdCodes;
    String ussdToDial = "";
    EditText ussdCodeText;
    TextView refreshHeading;
    Button refreshButton;
    int sim_slot = 0;
    String carrier = "";
    String refreshType = "";

    @Override
    protected void onStop()
    {
        FlurryAgent.endTimedEvent("BalanceRefreshActivity");
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    @DebugLog
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_refresh);
        this.setFinishOnTouchOutside(false);
        FlurryAgent.logEvent("BalanceRefreshActivity",true);
        if(!Helper.isAccessibilityEnabled(ConstantsAndStatics.accessibiltyID))
        {
            Toast.makeText(BalanceRefreshActivity.this, "Need to Enable Simply Recorder First", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this,OnBoardingActivity.class));
            this.finish();
        }
        EventBus.getDefault().register(this);
        /*You shall receive two extras
        1. The SIM_SLOT, by default 0, you do the extra addition to dial for dual sim here
        2.The type to Check it will bethe KEY to the JSOn Object,they are as follows
                "MAIN_BAL" : "*111*2#",
				"DATA_2G" : "*111*6*2#",
				"DATA_3G" : "*111*6*2#",
				"SMS_BAL" : "*149#",
				"MOB_NUMBER"
        */

        Intent mIntent = this.getIntent();
        sim_slot = mIntent.getIntExtra("SIM_SLOT", 0);
        refreshType = mIntent.getStringExtra("TYPE");
        userPref = getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY, MODE_PRIVATE);
        ussdCodeText = (EditText) findViewById(R.id.ussd_edit_id);
        refreshHeading = (TextView) findViewById(R.id.alert_heading);
        refreshButton = (Button) findViewById(R.id.refresh_btn);
        ImageButton enableEdit = (ImageButton) findViewById(R.id.enable_edit);
        enableEdit.setOnClickListener(this);
        refreshButton.setOnClickListener(this);
        if(GlobalData.globalSimList==null)
        {
            GlobalData.globalSimList =  new Helper.SharedPreferenceHelper().getDualSimDetails();

        }
        carrier = GlobalData.globalSimList.get(sim_slot).getCarrier();
        if(TextUtils.isEmpty(carrier))
        {
            //TODO change this
            carrier = "Airtel";
        }
        ussdCodes = getUssdCodes(carrier);
        refreshHeading.setText(carrier + ", " + getDisplayType(refreshType));
        if (refreshType != null)
        {
            try
            {
                ussdToDial = ussdCodes.getString(refreshType);
            } catch (Exception e)
            {
                //TODO Change event this
                ussdToDial = "*123#";
                Crashlytics.logException(e);
            }
        }
        ussdCodeText.setText(ussdToDial);
       //V17Log.d(TAG, "USSD to Dial" + ussdToDial);
    }



    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.refresh_btn:
                ussdToDial = ussdCodeText.getText().toString();

                if(ussdToDial.matches("^\\*(\\d+\\**)+#$"))
                {
                    Helper.logGA("REFRESH","STARTED");
                    Helper.logFlurry("REFRESH","TYPE","STARTED");
                    dialUSSDCode();

                    wait_for_ussd();
                }
                else
                {
                    Toast.makeText(this,"Invalid USSD Code",Toast.LENGTH_LONG).show();


                }
                break;
            case R.id.enable_edit:
                ussdCodeText.setEnabled(true);
                break;

        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void dialUSSDCode()
    {
        String extras[] = {
                "extra_asus_dial_use_dualsim",
                "com.android.phone.extra.slot",
                "slot",
                "simslot",
                "simSlot",
                "sim_slot",
                "subscription",
                "Subscription",
                "phone",
                "com.android.phone.DialingMode",
                "simId",
                "simnum",
                "phone_type",
                "slotId",
                "slot_id",
                "slotIdx"
        };

        ConstantsAndStatics.WAITING_FOR_REFRESH = true;
        ConstantsAndStatics.REFRESH_TYPE = ConstantsAndStatics.USSD_TYPES.MAIN_BALANCE;
       //V17Log.d(TAG, "Dialing " + ussdToDial);
            ussdToDial = ussdToDial.replace("*", Uri.encode("*")).replace("#", Uri.encode("#"));
            Intent mIntent = new Intent(Intent.ACTION_CALL);
            Uri data = Uri.parse("tel:" + ussdToDial);
            mIntent.setData(data);
            if (GlobalData.globalSimList == null)
            {
                GlobalData.globalSimList = new Helper.SharedPreferenceHelper().getDualSimDetails();
            }
            if (GlobalData.globalSimList.size() > 1)
            {
                for (String extraKey : extras)
                {
                    mIntent.putExtra(extraKey, sim_slot);
                }
                if (GlobalData.globalSimList.get(0).subid != -1)
                {
                    mIntent.putExtra("sub_id", GlobalData.globalSimList.get(0).subid);
                    mIntent.putExtra("subscription", GlobalData.globalSimList.get(0).subid);
                    mIntent.putExtra("Subscription", GlobalData.globalSimList.get(0).subid);
                }
                try
                {

                    if (ReflectionHelper.classExists("android.telecom.TelecomManager"))
                    {
                        TelecomManager localTelecomManager = (TelecomManager) this.getSystemService(Context.TELECOM_SERVICE);
                        List localList = localTelecomManager.getCallCapablePhoneAccounts();
                       //V17Log.d("Shabaz Account ", localList.toString());
                        //List localList = (List) ReflectionHelper.getObject((Object) localTelecomManager, localTelecomManager.getClass().getName(), "getAllPhoneAccountHandles", null);
                        if ((localList != null) && (Helper.isExists(localList, sim_slot)))
                        {
                            mIntent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", (Parcelable) localList.get(sim_slot));
                        }
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            startActivity(mIntent);

    }

    public void onEvent(BalanceRefreshMessage message)
   {
       SharedPreferences userDataPref = getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY,MODE_PRIVATE);
       boolean firstRefresh = userDataPref.getBoolean("FIRST_REFRESH",true);
       if(message.isSuccessful())
       {
           if(firstRefresh)
           {

               Helper.logGA("ONBOARD","REFRESH_SUCCESS");
               Helper.logFlurry("ONBOARD","ACTION","REFRESH_SUCCESS");
               userDataPref.edit().putBoolean("FIRST_REFRESH",false).apply();
           }
           Helper.logGA("REFRESH",refreshType,"SUCCESS");
           Helper.logFlurry("REFRESH","TYPE",refreshType,"ACTION","SUCCESS");
           success_message(message);
           updateValues(message);
       }
       else
       {
           if(firstRefresh)
           {
               Helper.logGA("ONBOARD","REFRESH_FAIL");
               Helper.logFlurry("ONBOARD","ACTION","REFRESH_FAIL");
               userDataPref.edit().putBoolean("FIRST_REFRESH",false).apply();
           }
           Helper.logGA("REFRESH",refreshType,"FAILURE");
           Helper.logFlurry("REFRESH","TYPE",refreshType,"ACTION","FAILURE");
           unsuccess_message(message.getOriginalMessage());
       }
   }

    private void updateValues(BalanceRefreshMessage message)
    {
    }

    private void wait_for_ussd()
    {
        findViewById(R.id.wait_msg_id).setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_2).setVisibility(View.GONE);
        final Button mcancelButton = (Button) findViewById(R.id.refresh_btn);
        mcancelButton.setText("Cancel");
        mcancelButton.setOnClickListener(null);
        mcancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Helper.logGA("REFRESH",refreshType,"CANCELLED");
                Helper.logFlurry("REFRESH","TYPE",refreshType,"ACTION","CANCELLED");
                finish();
            }
        });
    }
    private void success_message(final BalanceRefreshMessage message)
    {
        String rupeeSymbol = getResources().getString(R.string.rupee_symbol);
        findViewById(R.id.success_layout_bal).setVisibility(View.VISIBLE);
        findViewById(R.id.success_layout_msg).setVisibility(View.VISIBLE);
        findViewById(R.id.success_layout_button).setVisibility(View.VISIBLE);
        findViewById(R.id.wait_msg_id).setVisibility(View.GONE);
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.refresh_btn).setVisibility(View.GONE);
        findViewById(R.id.message_layout).setVisibility(View.VISIBLE);

        Button okay = (Button) findViewById(R.id.refresh_okay);
        okay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SharedPreferences userPreferences = getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY,MODE_PRIVATE);
                userPreferences.edit().putFloat("CURRENT_BALANCE_"+sim_slot,message.getBalance()).commit();
                UpdateBalanceOnScreen mUpdateBalanceOnScreen  = new UpdateBalanceOnScreen(refreshType,message.getValidity(),message.getBalance());
                EventBus.getDefault().postSticky(mUpdateBalanceOnScreen);
                BalanceRefreshActivity.this.finish();
            }
        });
        Button report = (Button) findViewById(R.id.refresh_report);
        report.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {

                    ParseObject pObj = new ParseObject("BALANCE_REFRESH");
                    pObj.put("DEVICE_ID", Helper.getDeviceId());
                    pObj.put("REFRESH_TYPE", refreshType);
                    pObj.put("MESSAGE", message.detailstoLog());
                    pObj.put("PARSER_RESULTS", message.getOriginalMessage());
                    pObj.put("USSD_CODE", ussdToDial);
                    pObj.put("CARRIER", carrier);
                    pObj.saveEventually();
                    Toast.makeText(BalanceRefreshActivity.this,"Thank you for reporting. We will fix it ASAP",Toast.LENGTH_LONG).show();
                    BalanceRefreshActivity.this.finish();

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        });

        ((TextView)findViewById(R.id.balance_text)).setText(rupeeSymbol+" "+String.format("%.2f",message.getBalance()));


        final TextView originalMessage = ((TextView) findViewById(R.id.original_message_text));
        originalMessage.setText(message.getOriginalMessage());
        originalMessage.setVisibility(View.VISIBLE);

    }

    private void unsuccess_message(String originalMessage)
    {

        findViewById(R.id.success_layout_msg).setVisibility(View.VISIBLE);
        findViewById(R.id.success_layout_bal).setVisibility(View.GONE);
        findViewById(R.id.success_layout_button).setVisibility(View.GONE);
        findViewById(R.id.wait_msg_id).setVisibility(View.GONE);
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.refresh_btn).setVisibility(View.GONE);
        findViewById(R.id.sorry_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.message_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.sorry__msg_id).setVisibility(View.VISIBLE);
        findViewById(R.id.sorry_layout_button).setVisibility(View.VISIBLE);
        TextView originalMessageText = ((TextView) findViewById(R.id.original_message_text));
        originalMessageText.setText(originalMessage);
        originalMessageText.setVisibility(View.VISIBLE);
        Button dismiss = (Button) findViewById(R.id.cancel_btn);
        Button tryAgain = (Button) findViewById(R.id.try_again_btn);
        tryAgain.setOnClickListener(new View.OnClickListener()
        {
            @Override
            @DebugLog
            public void onClick(View v)
            {
                Intent mIntent = new Intent(BalanceRefreshActivity.this,BalanceRefreshActivity.class);
                mIntent.putExtra("SIM_SLOT",sim_slot);
                mIntent.putExtra("TYPE",refreshType);
                startActivity(mIntent);
                BalanceRefreshActivity.this.finish();
            }
        });

        dismiss.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BalanceRefreshActivity.this.finish();
            }
        });
        ParseObject pObj = new ParseObject("INVALID_BALANCE_REFRESH");
        pObj.put("DEVICE_ID", Helper.getDeviceId());
        pObj.put("REFRESH_TYPE", refreshType);
        pObj.put("MESSAGE", originalMessage);
        pObj.put("USSD_CODE", ussdToDial);
        pObj.put("CARRIER", carrier);
        pObj.saveEventually();

    }
    private String getDisplayType(String refreshType)
{
    switch (refreshType)
    {

        case "MAIN_BAL":
            return "Main Balance";
        case "DATA_2G":
            return "2G Balance";
        case "DATA_3G":
            return "3G Balance";
        case "SMS_BAL":
            return "SMS Balance";
        case "MOB_NUMBER":
            return "Mobile Number";
    }
    return "";
}


    @DebugLog
    private JSONObject getUssdCodes(String carrier)
    {
        //TODO Update USSD number from dropbox
        JSONObject mJsonObject = null;
        if(TextUtils.isEmpty(carrier))
        {
            //TODO change this
            carrier = "Airtel";
        }
        try
        {
            String raw = userPref.getString(carrier, null);
            if (TextUtils.isEmpty(raw)) {
                throw new JSONException("First Time");
            }
            mJsonObject = new JSONObject(raw);
        } catch (Exception e)
        {
            try
            {
                JSONObject mObject = new JSONObject(loadJSONFromAsset("USSDCodes.json"));
                mObject = mObject.getJSONObject("USSD_CODE");
                mJsonObject = mObject.getJSONObject(carrier);
               //V17Log.d("BAL Refresh", mJsonObject.toString());
                userPref.edit().putString(carrier, mJsonObject.toString()).apply();
            } catch (Exception e1)
            {
                Crashlytics.logException(e1);
            }
        }
        return mJsonObject;
    }

    @DebugLog
    String loadJSONFromAsset(String fileName)
    {
        String json = null;
        try
        {
            InputStream is = MyApplication.context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex)
        {
            ex.printStackTrace();
            return null;
        }

        return json;
    }


}
