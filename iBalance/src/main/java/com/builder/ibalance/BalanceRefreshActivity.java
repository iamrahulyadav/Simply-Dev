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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.builder.ibalance.messages.BalanceRefreshMessage;
import com.builder.ibalance.util.ConstantsAndStatics;
import com.builder.ibalance.util.GlobalData;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.ReflectionHelper;
import com.crashlytics.android.Crashlytics;

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

    @Override
    @DebugLog
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_refresh);
        EventBus.getDefault().register(this);
        /*You sill receive two extras
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
        String refreshType = mIntent.getStringExtra("TYPE");
        userPref = getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY, MODE_PRIVATE);
        ussdCodeText = (EditText) findViewById(R.id.ussd_edit_id);
        refreshHeading = (TextView) findViewById(R.id.refresh_heading);
        refreshButton = (Button) findViewById(R.id.refresh_btn);
        refreshButton.setOnClickListener(this);
        String carrier = GlobalData.globalSimList.get(sim_slot).getCarrier();
        ussdCodes = getUssdCodes(carrier);
        refreshHeading.setText("Dialing for " + carrier + ", " + getDisplayType(refreshType));
        if (refreshType != null)
        {
            try
            {
                ussdToDial = ussdCodes.getString(refreshType);
            } catch (JSONException e)
            {
                Crashlytics.logException(e);
            }
        }
        ussdCodeText.setText(ussdToDial);
        Log.d(TAG, "USSD to Dial" + ussdToDial);
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
                dialUSSDCode();
                wait_for_ussd();
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void dialUSSDCode()
    {
        Log.wtf("Shjhhd","fcd");
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
        Log.d(TAG, "Dialing " + ussdToDial);
        ussdToDial = ussdToDial.replace("*", Uri.encode("*")).replace("#", Uri.encode("#"));
        Intent mIntent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + ussdToDial);
        mIntent.setData(data);
        if(GlobalData.globalSimList==null)
        {
            GlobalData.globalSimList = new Helper.SharedPreferenceHelper().getDualSimDetails();
        }
        if(GlobalData.globalSimList.size()>1)
        {
            for (String extraKey : extras)
            {
                mIntent.putExtra(extraKey, sim_slot);
            }
            if(GlobalData.globalSimList.get(0).subid != -1)
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
                    Log.d("Shabaz Account ",localList.toString());
                    //List localList = (List) ReflectionHelper.getObject((Object) localTelecomManager, localTelecomManager.getClass().getName(), "getAllPhoneAccountHandles", null);
                    if ((localList != null) && (Helper.isExists(localList, sim_slot)))
                    {
                        mIntent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", (Parcelable) localList.get(sim_slot));
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        startActivity(mIntent);
    }

    public void onEvent(BalanceRefreshMessage message)
   {
       if(message.isSuccessful())
       {
           success_message(message);
           updateValues(message);
       }
       else
       {
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
        findViewById(R.id.refresh_btn).setVisibility(View.GONE);
    }
    private void success_message(BalanceRefreshMessage message)
    {
        findViewById(R.id.success_layout_bal).setVisibility(View.VISIBLE);
        findViewById(R.id.success_layout_msg).setVisibility(View.VISIBLE);
        findViewById(R.id.success_layout_button).setVisibility(View.VISIBLE);
        findViewById(R.id.wait_msg_id).setVisibility(View.GONE);
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        ((TextView)findViewById(R.id.balance_text)).setText(message.getBalance()+"");
        ((TextView)findViewById(R.id.original_message_text)).setText(message.getOriginalMessage());
    }

    private void unsuccess_message(String originalMessage)
    {
        findViewById(R.id.success_layout_bal).setVisibility(View.GONE);
        findViewById(R.id.success_layout_button).setVisibility(View.GONE);
        findViewById(R.id.wait_msg_id).setVisibility(View.GONE);
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.sorry_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.sorry__msg_id).setVisibility(View.VISIBLE);
        findViewById(R.id.sorry_layout_button).setVisibility(View.VISIBLE);
        findViewById(R.id.success_layout_msg).setVisibility(View.VISIBLE);
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
        try
        {
            String raw = userPref.getString(carrier, null);
            if (TextUtils.isEmpty(raw)) throw new JSONException("First Time");
            mJsonObject = new JSONObject(raw);
        } catch (JSONException e)
        {
            try
            {
                JSONObject mObject = new JSONObject(loadJSONFromAsset("USSDCodes.json"));
                mObject = mObject.getJSONObject("USSD_CODE");
                mJsonObject = mObject.getJSONObject(carrier);
                Log.d("BAL Refresh", mJsonObject.toString());
                userPref.edit().putString(carrier, mJsonObject.toString());
            } catch (JSONException e1)
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
