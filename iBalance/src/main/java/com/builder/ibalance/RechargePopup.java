package com.builder.ibalance;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Map;
import java.util.TreeMap;

public class RechargePopup extends Activity implements View.OnClickListener
{
    Map<String, Integer> carriers = new TreeMap<>();
    Map<String, Integer> circle = new TreeMap<>();
    Intent rechargeIntent = null;
    String number = "",carrierExtra="Airtel",circleExtra="Karnataka";
    int carrierId = 1, circleId = 1, amount = 0;
    TextView mobileNumberView,carrierCircleView,amountView;
    ImageButton paytmButton,mobiKwikButton,freeChargeButton;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recaharge_popup);

        rechargeIntent = getIntent();
        if (rechargeIntent != null)
        {
            number = rechargeIntent.getStringExtra("NUMBER");
            amount = rechargeIntent.getIntExtra("AMOUNT", 0);
            circleExtra = rechargeIntent.getStringExtra("CIRCLE");
            carrierExtra = rechargeIntent.getStringExtra("CARRIER");
        }
        mobileNumberView = (TextView) findViewById(R.id.mobile_no_id);
        carrierCircleView = (TextView) findViewById(R.id.carrier_circle_id);
        amountView = (TextView) findViewById(R.id.amount_id);
        mobileNumberView.setText(number);
        carrierCircleView.setText(carrierExtra+", "+circleExtra);
        amountView.setText("Rs. "+amount);

        paytmButton = (ImageButton) findViewById(R.id.paytm_recharge);
        paytmButton.setOnClickListener(this);
        mobiKwikButton = (ImageButton) findViewById(R.id.mobikwik_recharge);
        mobiKwikButton.setOnClickListener(this);
        freeChargeButton = (ImageButton) findViewById(R.id.freecharge_recharge);
        freeChargeButton.setOnClickListener(this);

    }


    @Override
    public void onClick(View v)
    {

        Intent intent = null;
        switch (v.getId())
        {

            case R.id.paytm_recharge:
                //Paytm
                try
                {
                    getPackageManager().getPackageInfo("net.one97.paytm", 0);
                    //If app Installed
                    intent = new Intent();
                    intent.setComponent(new ComponentName("net.one97.paytm", "net.one97.paytm.AJRJarvisSplash"));
                    intent.putExtra("mobileNumber", number);
                    intent.putExtra("referralSource", "SimplyApp");
                    intent.putExtra("amount", amount+"");
                    startActivity(intent);

                } catch (PackageManager.NameNotFoundException e)
                {
                    //Ask them to install the app
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=net.one97.paytm&referrer=utm_source=SimplyApp")));
                    e.printStackTrace();
                }


                break;
            case R.id.mobikwik_recharge:
                //Mobikwik

                try
                {
                    getPackageManager().getPackageInfo("com.mobikwik_new", 0);
                    //If app Installed
                    intent = new Intent("com.mobikwik_new.RECHARGE");
                    intent.setComponent(new ComponentName("com.mobikwik_new", "com.mobikwik_new.billers.RechargeCategory"));
                    initializeMapForMobiKwik();
                    circleId = circle.get(circleExtra);
                    carrierId = carriers.get(carrierExtra);
                    intent.putExtra("mobileNumber", number);
                    intent.putExtra("circleIndex", circleId);
                    intent.putExtra("operatorIndex", carrierId);
                    intent.putExtra("src", "SimplyApp");
                    intent.putExtra("amount", amount);
                    startActivity(intent);

                } catch (PackageManager.NameNotFoundException e)
                {
                    //Ask them to install the app
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.mobikwik_new&referrer=utm_source=SimplyApp")));
                    e.printStackTrace();
                }
                break;
            case R.id.freecharge_recharge:
                //FeeCharge
                break;
            default:
        }
    }

    private void initializeMapForMobiKwik()
    {
        carriers.put("Aircel", 6);
        carriers.put("Airtel", 1);
        carriers.put("BSNL", 3);
        carriers.put("MTNL", 7);
        carriers.put("Etisalat-DB", 1);
        carriers.put("Idea", 8);
        carriers.put("Loop", 1);
        carriers.put("MTS", 11);
        carriers.put("PING CDMA", 1);
        carriers.put("Reliance-GSM", 5);
        carriers.put("Spice", 1);
        carriers.put("S-Tel", 1);
        carriers.put("T24", 1);
        carriers.put("Tata-Docomo", 10);
        carriers.put("Tata Indicom", 9);
        carriers.put("Uninor", 12);
        carriers.put("Virgin", 1);
        carriers.put("Vodafone", 2);
        carriers.put("Videocon", 13);
        // circle.clear();
        circle.put("Andhra Pradesh", 1);
        circle.put("Assam", 2);
        circle.put("Bihar-Jharkhand", 3);
        circle.put("Chennai", 4);
        circle.put("Delhi-NCR", 5);
        circle.put("Gujarat", 6);
        circle.put("Haryana", 7);
        circle.put("Himachal Pradesh", 8);
        circle.put("Jammu-Kashmir", 9);
        circle.put("Karnataka", 10);
        circle.put("Kerala", 11);
        circle.put("Kolkata", 12);
        circle.put("Maharashtra-Goa", 13);
        circle.put("Madhya Pradesh-Chattisgarh", 14);
        circle.put("Mumbai", 15);
        circle.put("North East", 16);
        circle.put("Orissa", 17);
        circle.put("Punjab", 18);
        circle.put("Rajasthan", 19);
        circle.put("Tamil Nadu", 20);
        circle.put("UP(EAST))", 21);
        circle.put("UP(WEST)-Uttarakhand", 22);
        circle.put("West Bengal", 23);
    }
}
