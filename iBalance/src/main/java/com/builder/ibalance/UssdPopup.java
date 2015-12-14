package com.builder.ibalance;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.builder.ibalance.services.CallDetailsModel;
import com.builder.ibalance.util.CircleTransform;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

public class UssdPopup extends AppCompatActivity
{
	TextView field1,field2,field3,field4,head1,head2,head3,head4;
	Button dismiss,open_app;
	String from = "CALL"; //what type of popup
	 @Override
	    protected void onStart() {
	        super.onStart();
	        // Your Code Here
	    }

	    @Override
	    protected void onStop() {
	        super.onStop();
	        // Your Code Here
	    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		boolean recharge = false;
		boolean share = false;
		setContentView(R.layout.ussd_popup);
		Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
		Intent mintent = this.getIntent();
		field1 = (TextView)findViewById(R.id.field1);
		field1.setTypeface(tf);
		field2 =(TextView)findViewById(R.id.field2);
		field2.setTypeface(tf);
        field3 = (TextView)findViewById(R.id.field3);
		field3.setTypeface(tf);
        field4 = (TextView)findViewById(R.id.field4);
		field4.setTypeface(tf);
		head1 = (TextView)findViewById(R.id.head1);
		head1.setTypeface(tf);
		head2 =(TextView)findViewById(R.id.head2);
		head2.setTypeface(tf);
		head3 = (TextView)findViewById(R.id.head3);
		head3.setTypeface(tf);
		head4 = (TextView)findViewById(R.id.head4);
		head4.setTypeface(tf);
        final TextView originalMessage = (TextView) findViewById(R.id.originalMessage);
        final ImageButton expand = (ImageButton) findViewById(R.id.expandOriginalMessage);

		/* NORMAL_CALL,//1
		 NORMAL_SMS,//2
		 NORMAL_DATA,//3
		 VOICE_PACK,//4
		 SMS_PACK,//5
		 DATA_PACK,//6
		 BALANCE,//7
	
*/

        String cost,balance;
        String rupee_symbol = getResources().getString(R.string.rupee_symbol);
		switch (mintent.getIntExtra("TYPE", -1)) {
		case 1:
            from = "CALL";
            View contactLayout = findViewById(R.id.ussd_contact_layout);
            contactLayout.setVisibility(View.VISIBLE);
            ImageView contact_picture = (ImageView) contactLayout.findViewById(R.id.ussd_contact_picture);
            TextView contact_name = (TextView) contactLayout.findViewById(R.id.ussd_contact_name);
            TextView contact_number = (TextView) contactLayout.findViewById(R.id.ussd_contact_number);
            TextView contact_carrier_circle = (TextView) contactLayout.findViewById(R.id.ussd_contact_carrier_circle);
            TextView contact_total_spent = (TextView) contactLayout.findViewById(R.id.ussd_contact_total_spent);
            TextView contact_call_cost = (TextView) contactLayout.findViewById(R.id.ussd_contact_call_cost);

            final CallDetailsModel details = mintent.getParcelableExtra("DATA");
			expand.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if(originalMessage.getVisibility() == View.GONE)
					{
						expand.setImageResource(R.drawable.minus_icon);
						originalMessage.setText(details.getMessage());
						originalMessage.setVisibility(View.VISIBLE);
					}
					else
					{
						expand.setImageResource(R.drawable.plus_icon);
						originalMessage.setVisibility(View.GONE);
					}
				}
			});
			head1.setText("Call Cost");
			field1.setText(rupee_symbol+" "+details.getCall_cost());

			head2.setText("Current Balance");
            SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
			float minimum_bal = mSharedPreferences.getFloat("MINIMUM_BALANCE",10.0f);
			int popUpCount = mSharedPreferences.getInt("POP_UP_COUNT",1);
			mSharedPreferences.edit().putInt("POP_UP_COUNT",popUpCount+1);
			if(popUpCount%10==0)
			{
				share = true;
			}
			if(details.getCurrent_balance() <= minimum_bal)
			{
				field2.setTextColor(Color.parseColor("#ff0000"));
				recharge = true;
			}
			field2.setText(rupee_symbol+" "+details.getCurrent_balance());

			head3.setText("Call Duration");
			field3.setText(getTimeFormatted(details.getDuration()));
			String call_rate;
			try{
			 call_rate = String.format("%.1f", details.getCall_rate())+ " p/s";
			}
			catch(Exception e)
			{
				call_rate = "--";
			}
			head4.setText("Call Rate");
			field4.setText(call_rate);

            contact_name.setText(details.getName());
            Picasso.with(this).load(details.getImage_uri()).transform(new CircleTransform()).placeholder(R.drawable.default_contact_picture).into(contact_picture);
            contact_number.setText(details.getNumber());
            contact_carrier_circle.setText(details.getCarrier_circle());
            contact_total_spent.setText(rupee_symbol+" "+String.format("%.2f",details.getTotal_spent()));
            contact_call_cost.setText("+ "+details.getCall_cost());
			//Toast.makeText(this,"SimSlot = "+details.getSim_slot(),Toast.LENGTH_LONG).show();
			break;
			//Normal SMS
		case 2:
            from = "SMS";
			 cost = mintent.getStringExtra("SMS_COST");
			head1.setText("SMS Cost");
			field1.setText(rupee_symbol+" "+cost);
			balance = mintent.getStringExtra("BALANCE");
			head2.setText("Current Balance");
			field2.setText(rupee_symbol+" "+balance);
			((LinearLayout)findViewById(R.id.layout3)).setVisibility(View.GONE);
			String sms_rate = "0.0";
			try{
			 sms_rate = String.format("%.1f", (Float.parseFloat(cost)))+" "+ rupee_symbol+"/SMS";
			}
			catch(Exception e)
			{
				sms_rate = "--";
			}
			head4.setText("SMS Rate");
			field4.setText(sms_rate);
            final String message = mintent.getStringExtra("MESSAGE");
            expand.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(originalMessage.getVisibility() == View.GONE)
                    {
                        expand.setImageResource(R.drawable.minus_icon);
                        originalMessage.setText(message);
                        originalMessage.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        expand.setImageResource(R.drawable.plus_icon);
                        originalMessage.setVisibility(View.GONE);
                    }
                }
            });
			break;
			//Normal Data
		case 3:
            from = "NORMAL_DATA" ;
			cost = mintent.getStringExtra("DATA_COST");
			head1.setText("Data Cost");
			field1.setText(rupee_symbol+" "+cost);

			balance = mintent.getStringExtra("BALANCE");
			head2.setText("Current Balance");
			field2.setText(rupee_symbol+" "+balance);
			
			String data_consumed = mintent.getStringExtra("DATA_CONSUMED");
			head3.setText("Data Used");
			field3.setText(data_consumed+" MB");
			String data_rate;
			try{
				data_rate = String.format("%.1f", (Float.parseFloat(cost)/Float.parseFloat(data_consumed)))+ " p/10KB";
			}
			catch(Exception e)
			{
				data_rate = "--";
			}
			head4.setText("Data Rate");
			field4.setText(data_rate);
            final String message2 = mintent.getStringExtra("MESSAGE");
            expand.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(originalMessage.getVisibility() == View.GONE)
                    {
                        expand.setImageResource(R.drawable.minus_icon);
                        originalMessage.setText(message2);
                        originalMessage.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        expand.setImageResource(R.drawable.plus_icon);
                        originalMessage.setVisibility(View.GONE);
                    }
                }
            });
			break;
			//Data Pack
		case 6:
            from = "PACK_DATA";
			 data_consumed = mintent.getStringExtra("DATA_CONSUMED");
			head1.setText("Data Used");
			field1.setText(data_consumed+" MB");

			String data_left = mintent.getStringExtra("DATA_LEFT");
			head2.setText("Data Left");
			field2.setText(data_left+" MB");
			
			balance = mintent.getStringExtra("BALANCE");
			head3.setText("Current Balance");
			field3.setText(rupee_symbol+" "+balance);
			head4.setText("Valid Till");
			field4.setText(mintent.getStringExtra("VALIDITY"));
            final String message3 = mintent.getStringExtra("MESSAGE");
            expand.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(originalMessage.getVisibility() == View.GONE)
                    {
                        expand.setImageResource(R.drawable.minus_icon);
                        originalMessage.setText(message3);
                        originalMessage.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        expand.setImageResource(R.drawable.plus_icon);
                        originalMessage.setVisibility(View.GONE);
                    }
                }
            });
			break;
		default:
			break;
		}
		if(recharge)
		{
			findViewById(R.id.ussd_normal_layout).setVisibility(View.GONE);
			View v = findViewById(R.id.ussd_recharge_layout);
                    v.setVisibility(View.VISIBLE);
            Button rechargeButton = (Button) v.findViewById(R.id.ussd_recharge);
            rechargeButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    Intent mIntent = new Intent(getApplicationContext(), SplashscreenActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    mIntent.putExtra("FROM", "POPUP");
                    mIntent.putExtra("RECHARGE", true);
                    Helper.logUserEngageMent(from + "_RECHARGE");
                    startActivity(mIntent);
                    finish();
                }
            });

		}
        else if(share)
        {
            Button rateButton = (Button) findViewById(R.id.ussd_dismiss);
            rateButton.setText("Rate App");
            Button shareButton = (Button) findViewById(R.id.ussd_open_app);

            shareButton.setText("Share App");
            rateButton.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {

                    Tracker t = ((MyApplication) getApplication()).getTracker(
                            MyApplication.TrackerName.APP_TRACKER);
                    t.send(new HitBuilders.EventBuilder().setCategory("RATE")
                            .setAction("Rate").setLabel("").build());
                    FlurryAgent.logEvent("Rate");
                    AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,
                            "Rate", "");
                    Uri uri = Uri.parse("market://details?id=" + UssdPopup.this.getPackageName());
                    //Log.d(tag,"URI = "+ uri);
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + UssdPopup.this.getPackageName())));
                    }

                }
            });
            shareButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    boolean isWhatsappInstalled = Helper.whatsappInstalledOrNot();
                    Tracker t = ((MyApplication) getApplication()).getTracker(
                            MyApplication.TrackerName.APP_TRACKER);
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "To Track your prepaid Balance and know how much you spend on your contacts.\nTry out \"Simply\": https://goo.gl/v3YMrN ");
                    sendIntent.setType("text/plain");
                    if(isWhatsappInstalled)
                    {

                        // Build and send an Event.
                        t.send(new HitBuilders.EventBuilder()
                                .setCategory("SHARE")
                                .setAction("WhatsApp")
                                .setLabel("POPUP")
                                .build());

                        FlurryAgent.logEvent("WhatsApp_Share");
                        //V10AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"WhatsApp_Share","");
                        sendIntent.setPackage("com.whatsapp");
                        startActivity(sendIntent);
                    }
                    else
                    {
                        // Build and send an Event.
                        t.send(new HitBuilders.EventBuilder()
                                .setCategory("SHARE")
                                .setAction("OTHER_SHARE")
                                .setLabel("POPUP")
                                .build());
                        //V10AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"OTHER_SHARE","");
                        FlurryAgent.logEvent("Other_Share");
                        startActivity(sendIntent);
                    }
                }
            });
        }
		else
		{
			dismiss = (Button) findViewById(R.id.ussd_dismiss);
            ImageView infoButton = (ImageView) findViewById(R.id.info_button);
            open_app = (Button) findViewById(R.id.ussd_open_app);
            infoButton.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {

                    Toast.makeText(getApplicationContext(), "This is a Custom message,With call rate info.", Toast.LENGTH_LONG).show();

                }
            });
            dismiss.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    Helper.logUserEngageMent(from + "_DISMISS");
                    finish();

                }
            });
            open_app.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    Intent mIntent = new Intent(getApplicationContext(), SplashscreenActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    mIntent.putExtra("FROM", "POPUP");
                    Helper.logUserEngageMent(from + "_OPEN");
                    startActivity(mIntent);
                    finish();

                }
            });
		}
	}
	private String getTimeFormatted(int totalSecs) {
		String min,sec,hr;
		Integer hrs,mins,secs;
		secs = totalSecs % 60;
		if(secs<10)
			sec = "0"+secs;
		else
			sec = ""+secs;
		totalSecs = totalSecs/60;
		mins = totalSecs %60;
		if(mins<10)
			min = "0"+mins;
		else
			min = ""+mins;
		totalSecs = totalSecs/60;
		hrs = totalSecs;
		if(hrs<10)
			hr = "0"+hrs;
		else
			hr = ""+hrs;
		return hr+":"+min+":"+sec;
	}
	

}
