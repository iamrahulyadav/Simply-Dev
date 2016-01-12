package com.builder.ibalance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.builder.ibalance.util.ConstantsAndStatics;
import com.builder.ibalance.util.GlobalData;
import com.builder.ibalance.util.MyApplication;
import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import hugo.weaving.DebugLog;

public class BalanceRefreshActivity extends AppCompatActivity implements View.OnClickListener
{
    final String TAG = BalanceRefreshActivity.class.getSimpleName();
    SharedPreferences userPref;
    JSONObject ussdCodes;
    String ussdToDial ="" ;
    TextView ussdCodeText,refreshHeading;
    Button refreshButton;
    @Override
    @DebugLog
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_refresh);
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
        int sim_slot = mIntent.getIntExtra("SIM_SLOT",0);
        String refreshType = mIntent.getStringExtra("TYPE");
        userPref = getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY,MODE_PRIVATE);
        ussdCodeText = (TextView) findViewById(R.id.ussd_code);
        refreshHeading = (TextView) findViewById(R.id.refresh_bal_heading);
        refreshButton = (Button) findViewById(R.id.refresh_balance_button);
        refreshButton.setOnClickListener(this);
        String carrier = GlobalData.globalSimList.get(sim_slot).getCarrier();
        ussdCodes = getUssdCodes(carrier);
        refreshHeading.setText("Dialing for "+carrier+", "+getDisplayType(refreshType));
        if(refreshType!=null)
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
        Log.d(TAG,"USSD to Dial"+ussdToDial);
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
        JSONObject mJsonObject=null;
        try
        {
            String raw = userPref.getString(carrier,null);
            if(TextUtils.isEmpty(raw))
                throw new JSONException("First Time");
            mJsonObject = new JSONObject(raw);
        } catch (JSONException e)
        {
            try
            {
                JSONObject mObject = new JSONObject(loadJSONFromAsset("USSDCodes.json"));
                mObject = mObject.getJSONObject("USSD_CODE");
                mJsonObject = mObject.getJSONObject(carrier);
                Log.d("BAL Refresh",mJsonObject.toString());
                userPref.edit().putString(carrier,mJsonObject.toString());
            } catch (JSONException e1)
            {
                Crashlytics.logException(e1);
            }
        }
        return mJsonObject;
    }
    @DebugLog
    String loadJSONFromAsset(String fileName) {
        String json = null;
        try {
            InputStream is = MyApplication.context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
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
            case R.id.refresh_balance_button:
                Log.d(TAG,"Dialing "+ussdToDial);
                ussdToDial = ussdToDial.replace("*",Uri.encode("*")).replace("#",Uri.encode("#"));
                Intent mIntent = new Intent(Intent.ACTION_CALL);
                Uri data = Uri.parse("tel:" + ussdToDial);
                mIntent.setData(data);
                startActivity(mIntent);
                break;
        }
    }
}
