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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.builder.ibalance.models.PopupModels.NormalCallPopup;
import com.builder.ibalance.models.PopupModels.NormalDataPopup;
import com.builder.ibalance.models.PopupModels.NormalSmsPopup;
import com.builder.ibalance.models.PopupModels.PackCallPopup;
import com.builder.ibalance.models.PopupModels.PackDataPopup;
import com.builder.ibalance.models.PopupModels.PackSmsPopup;
import com.builder.ibalance.util.CircleTransform;
import com.builder.ibalance.util.ConstantsAndStatics;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

public class UssdPopup extends AppCompatActivity
{
    final String TAG = UssdPopup.class.getSimpleName();
	Button dismiss,open_app;
        TextView originalMessage;
     ImageButton expand ;
    String rupee_symbol;
    boolean recharge = false;
    boolean rate = false;
    boolean share = false;
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

//TODO coded by Sarvesh, need extensive code reuse and optimization
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        recharge = false;
        rate = false;
        share = false;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.ussd_popup);
		Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
		Intent mIntent = this.getIntent();
        rupee_symbol = getResources().getString(R.string.rupee_symbol);
        originalMessage = (TextView) findViewById(R.id.originalMessage);
        expand = (ImageButton) findViewById(R.id.expandOriginalMessage);
        switch (mIntent.getIntExtra("TYPE", -1))
        {
            case ConstantsAndStatics.USSD_TYPES.NORMAL_CALL:
               //V16Log.d(TAG,"Type Normal call");
                displayNormalCallPopUp((NormalCallPopup)mIntent.getParcelableExtra("DATA"));
                break;
            case ConstantsAndStatics.USSD_TYPES.PACK_CALL:
               //V16Log.d(TAG,"Type Pack call");
                displayCallPackPopup((PackCallPopup) mIntent.getParcelableExtra("DATA"));
                break;
            case ConstantsAndStatics.USSD_TYPES.NORMAL_SMS:
               //V16Log.d(TAG,"Type Normal SMS");
                displayNormalSMSPopup((NormalSmsPopup)mIntent.getParcelableExtra("DATA"));
                break;
            case ConstantsAndStatics.USSD_TYPES.PACK_SMS:
               //V16Log.d(TAG,"Type Pack SMS");
                displaySMSPackPopup((PackSmsPopup)mIntent.getParcelableExtra("DATA"));
                break;
            case ConstantsAndStatics.USSD_TYPES.NORMAL_DATA:
               //V16Log.d(TAG,"Type Normal Data");
                displayNormalDataPopup((NormalDataPopup)mIntent.getParcelableExtra("DATA"));
                break;
            case ConstantsAndStatics.USSD_TYPES.PACK_DATA:
               //V16Log.d(TAG,"Type Pack Data");
                displayDataPackPopup((PackDataPopup)mIntent.getParcelableExtra("DATA"));
                break;
        }

		/*field1 = (TextView)findViewById(R.id.field1);
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
		head4.setTypeface(tf);*/
		/* NORMAL_CALL,//1
		 NORMAL_SMS,//2
		 NORMAL_DATA,//3
		 VOICE_PACK,//4
		 SMS_PACK,//5
		 DATA_PACK,//6
		 BALANCE,//7*//*
        String cost,balance;
		switch (mIntent.getIntExtra("TYPE", -1)) {
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

            final CallDetailsModel details = mIntent.getParcelableExtra("DATA");
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
				rate = true;
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
			 cost = mIntent.getStringExtra("SMS_COST");
			head1.setText("SMS Cost");
			field1.setText(rupee_symbol+" "+cost);
			balance = mIntent.getStringExtra("BALANCE");
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
            final String message = mIntent.getStringExtra("MESSAGE");
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
			cost = mIntent.getStringExtra("DATA_COST");
			head1.setText("Data Cost");
			field1.setText(rupee_symbol+" "+cost);

			balance = mIntent.getStringExtra("BALANCE");
			head2.setText("Current Balance");
			field2.setText(rupee_symbol+" "+balance);
			
			String data_consumed = mIntent.getStringExtra("DATA_CONSUMED");
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
            final String message2 = mIntent.getStringExtra("MESSAGE");
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
			 data_consumed = mIntent.getStringExtra("DATA_CONSUMED");
			head1.setText("Data Used");
			field1.setText(data_consumed+" MB");

			String data_left = mIntent.getStringExtra("DATA_LEFT");
			head2.setText("Data Left");
			field2.setText(data_left+" MB");
			
			balance = mIntent.getStringExtra("BALANCE");
			head3.setText("Current Balance");
			field3.setText(rupee_symbol+" "+balance);
			head4.setText("Valid Till");
			field4.setText(mIntent.getStringExtra("VALIDITY"));
            final String message3 = mIntent.getStringExtra("MESSAGE");
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
		}*/
		if(recharge)
		{
            TextView infoText = ((TextView)findViewById(R.id.ussd_share_info));
            infoText.setVisibility(View.VISIBLE);
            infoText.setText("Please take a few seconds to Rate the App, it means a lot :)");
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
        else if(rate)
        {
            Button rateButton = (Button) findViewById(R.id.ussd_open_app);
            rateButton.setText("Rate App");
            ((LinearLayout)findViewById(R.id.ussd_dismiss).getParent()).setVisibility(View.GONE);
            LinearLayout.LayoutParams mLayout = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1);
            ((LinearLayout) rateButton.getParent()).setLayoutParams(mLayout);

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

        }
        else if(share)
        {
            //Will Be handled in normal call popup

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
    //Handle a bug in Call rate, it is set default to 0.0f
    private void displayNormalCallPopUp(final NormalCallPopup details) {
        TextView plan_id = (TextView) findViewById(R.id.plan_id);
        plan_id.setText("Main Bal");
        TextView head_1 = (TextView) findViewById(R.id.head1);
        head_1.setText("Call Cost");
        TextView head_2 = (TextView) findViewById(R.id.head2);
        head_2.setText("Main Balance");
        TextView head_3 = (TextView) findViewById(R.id.head3);
        head_3.setText("Call Duration");
        TextView head_4 = (TextView) findViewById(R.id.head4);
        head_4.setText("Call Rate");

        String rupee_symbol = getResources().getString(R.string.rupee_symbol);
        TextView field_1 = (TextView) findViewById(R.id.field1);
        if(details.getCall_cost()<0)
            field_1.setText("N/A");
        else
            field_1.setText(rupee_symbol+" "+String.format("%.2f", details.getCall_cost()));
        TextView field_2 = (TextView) findViewById(R.id.field2);
        SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        float minimum_bal = mSharedPreferences.getFloat("MINIMUM_BALANCE",10.0f);
        int popUpCount = mSharedPreferences.getInt("POP_UP_COUNT",1);
        mSharedPreferences.edit().putInt("POP_UP_COUNT",popUpCount+1);
        if(popUpCount>=10)
        {
            //TODO this is not a good structure, half here and half in onCreate fix it later
            rate = true;
        }
        else if(!details.getName().matches("[0-9]+"))
        {
            //To stop inflating the normmal one
            share = true;
            TextView infoText = ((TextView)findViewById(R.id.ussd_share_info));
            infoText.setVisibility(View.VISIBLE);

            SpannableStringBuilder mBuilder = new SpannableStringBuilder();
            mBuilder.append("Like Simply ?\nLet ");

            Spannable span = new SpannableString(details.getName());
            span.setSpan(new RelativeSizeSpan(1.1f), 0 , details.getName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0 , details.getName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new UnderlineSpan(), 0 , details.getName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mBuilder.append(span);
            mBuilder.append(" know about it!");
            infoText.setText(mBuilder);
            Button shareButton = (Button) findViewById(R.id.ussd_open_app);
            shareButton.setText("Share Now");
            ((LinearLayout)findViewById(R.id.ussd_dismiss).getParent()).setVisibility(View.GONE);
            LinearLayout.LayoutParams mLayout = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1);
            ((LinearLayout) shareButton.getParent()).setLayoutParams(mLayout);
            shareButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    if(Helper.whatsappInstalledOrNot())
                    {
                        //TODO Add Tracking info
                        ConstantsAndStatics.PASTE_SHARE_APP = true;
                        startActivity(Helper.openWhatsApp(details.getNumber(), "To Track your prepaid Balance and know how much you spend on your contacts.\nTry out \"Simply\": http://bit.ly/getSimply "));
                    }
                    else
                    {
                        Uri uri = Uri.parse("smsto:"+details.getNumber());
                        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                        it.putExtra("sms_body","To Track your prepaid Balance and know how much you spend on your contacts.\nTry out \"Simply\": http://bit.ly/getSimply ");

                        startActivity(it);
                    }
                }
            });

        }
        if(details.getCurrent_balance() <= minimum_bal)
        {
            field_2.setTextColor(Color.parseColor("#ff0000"));
            recharge = true;
        }
        if(details.getCurrent_balance()<0)
            field_2.setText("N/A");
        else
            field_2.setText(rupee_symbol+" "+String.format("%.2f", details.getCurrent_balance()));
        TextView field_3 = (TextView) findViewById(R.id.field3);
        field_3.setText(getTimeFormatted(details.getDuration()));
        TextView field_4 = (TextView) findViewById(R.id.field4);

        try{
            field_4.setText(String.format("%.1f", details.getCall_rate())+"P/s");

        }
        catch(Exception e)
        {
            field_4.setText("-- P/s");
        }
        findViewById(R.id.validity_layout).setVisibility(View.GONE);
        findViewById(R.id.separator_id).setVisibility(View.GONE);
        findViewById(R.id.ussd_contact_layout).setVisibility(View.VISIBLE);

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
        //User Details
        View contactLayout = findViewById(R.id.ussd_contact_layout);
        contactLayout.setVisibility(View.VISIBLE);
        ImageView contact_picture = (ImageView) contactLayout.findViewById(R.id.ussd_contact_picture);
        TextView contact_name = (TextView) contactLayout.findViewById(R.id.ussd_contact_name);
        TextView contact_number = (TextView) contactLayout.findViewById(R.id.ussd_contact_number);
        TextView contact_carrier_circle = (TextView) contactLayout.findViewById(R.id.ussd_contact_carrier_circle);
        TextView contact_total_spent = (TextView) contactLayout.findViewById(R.id.ussd_contact_total_spent);
        TextView contact_call_cost = (TextView) contactLayout.findViewById(R.id.ussd_contact_call_cost);


        contact_name.setText(details.getName());
        Picasso.with(this).load(details.getImage_uri()).transform(new CircleTransform()).placeholder(R.drawable.default_contact_picture).into(contact_picture);
        contact_number.setText(details.getNumber());
        contact_carrier_circle.setText(details.getCarrier_circle());
        contact_total_spent.setText(rupee_symbol+" "+String.format("%.2f",details.getTotal_spent()));
        contact_call_cost.setText("+ "+details.getCall_cost());
    }
    private void displayCallPackPopup(PackCallPopup details)
    {
        if(details.isMinsType())
        {
            CallPack_Type1(details);
        }
        else
        {
            CallPack_Type2(details);
        }
    }

    private void displayNormalSMSPopup(final NormalSmsPopup details) {
        TextView plan_id = (TextView) findViewById(R.id.plan_id);
        plan_id.setText("SMS: Main Bal");

        TextView head_1 = (TextView) findViewById(R.id.head1);
        head_1.setText("SMS Cost");
        TextView head_2 = (TextView) findViewById(R.id.head2);
        head_2.setText("Main Balance");



        TextView field_1 = (TextView) findViewById(R.id.field1);
        if(details.getSms_cost()<0.0f)
            field_1.setText("N/A");
        else
            field_1.setText(rupee_symbol+" "+details.getSms_cost());
        TextView field_2 = (TextView) findViewById(R.id.field2);
        if(details.getMain_bal()<0.0f)
            field_2.setText("N/A");
        else
        {
            SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
            float minimum_bal = mSharedPreferences.getFloat("MINIMUM_BALANCE",10.0f);
            int popUpCount = mSharedPreferences.getInt("POP_UP_COUNT",1);
            mSharedPreferences.edit().putInt("POP_UP_COUNT",popUpCount+1);
            if(popUpCount%10==0)
            {
                rate = true;
            }
            if(details.getMain_bal() <= minimum_bal)
            {
                field_2.setTextColor(Color.parseColor("#ff0000"));
                recharge = true;
            }
            field_2.setText(rupee_symbol + " " + details.getMain_bal());
        }
        findViewById(R.id.sec_column).setVisibility(View.GONE);
        findViewById(R.id.validity_layout).setVisibility(View.GONE);
        findViewById(R.id.separator_id).setVisibility(View.GONE);
        findViewById(R.id.ussd_contact_layout).setVisibility(View.VISIBLE);

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
        //User Details
        View contactLayout = findViewById(R.id.ussd_contact_layout);
        contactLayout.setVisibility(View.VISIBLE);
        ImageView contact_picture = (ImageView) contactLayout.findViewById(R.id.ussd_contact_picture);
        TextView contact_name = (TextView) contactLayout.findViewById(R.id.ussd_contact_name);
        TextView contact_number = (TextView) contactLayout.findViewById(R.id.ussd_contact_number);
        TextView contact_carrier_circle = (TextView) contactLayout.findViewById(R.id.ussd_contact_carrier_circle);
        TextView contact_total_spent = (TextView) contactLayout.findViewById(R.id.ussd_contact_total_spent);
        TextView contact_call_cost = (TextView) contactLayout.findViewById(R.id.ussd_contact_call_cost);


        contact_name.setText(details.getName());
        Picasso.with(this).load(details.getImage_uri()).transform(new CircleTransform()).placeholder(R.drawable.default_contact_picture).into(contact_picture);
        contact_number.setText(details.getNumber());
        contact_carrier_circle.setText(details.getCarrier_circle());
        contact_total_spent.setText(rupee_symbol+" "+String.format("%.2f",details.getTotal_spent()));
        contact_call_cost.setText("+ "+String.format("%.2f",details.getSms_cost()));

    }

    private void displaySMSPackPopup(final PackSmsPopup details) {
        TextView plan_id = (TextView) findViewById(R.id.plan_id);
        if(details.getPack_type()==null)
            plan_id.setText("SMS pack");
        else
            plan_id.setText(details.getPack_type());
        TextView head_1 = (TextView) findViewById(R.id.head1);
        head_1.setText("SMS Used");
        TextView head_2 = (TextView) findViewById(R.id.head2);
        head_2.setText("SMS left");
        TextView head_3 = (TextView) findViewById(R.id.head3);
        head_3.setText("Main Balance");


        TextView field_1 = (TextView) findViewById(R.id.field1);
        if(details.getUsed_sms()<0)
            field_1.setText("N/A");
        else
            field_1.setText(details.getUsed_sms()+"");
        TextView field_2 = (TextView) findViewById(R.id.field2);
        if(details.getLeft_sms()<0)
            field_2.setText("N/A");
        else
            field_2.setText(details.getLeft_sms()+"");
        TextView field_3 = (TextView) findViewById(R.id.field3);
        if(details.getMain_bal()<0)
            field_3.setText("N/A");
        else
            field_3.setText(rupee_symbol+" "+details.getMain_bal());

        findViewById(R.id.field4_lout).setVisibility(View.GONE);
        findViewById(R.id.validity_layout).setVisibility(View.VISIBLE);
        TextView validity = (TextView) findViewById(R.id.validity);
        if(details.getValidity()==null)
            validity.setText("N/A");
        else
            validity.setText(details.getValidity());

        findViewById(R.id.ussd_contact_layout).setVisibility(View.VISIBLE);

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
        //User Details
        View contactLayout = findViewById(R.id.ussd_contact_layout);
        contactLayout.setVisibility(View.VISIBLE);
        ImageView contact_picture = (ImageView) contactLayout.findViewById(R.id.ussd_contact_picture);
        TextView contact_name = (TextView) contactLayout.findViewById(R.id.ussd_contact_name);
        TextView contact_number = (TextView) contactLayout.findViewById(R.id.ussd_contact_number);
        TextView contact_carrier_circle = (TextView) contactLayout.findViewById(R.id.ussd_contact_carrier_circle);
        TextView contact_total_spent = (TextView) contactLayout.findViewById(R.id.ussd_contact_total_spent);
        TextView contact_call_cost = (TextView) contactLayout.findViewById(R.id.ussd_contact_call_cost);


        contact_name.setText(details.getName());
        Picasso.with(this).load(details.getImage_uri()).transform(new CircleTransform()).placeholder(R.drawable.default_contact_picture).into(contact_picture);
        contact_number.setText(details.getNumber());
        contact_carrier_circle.setText(details.getCarrier_circle());
        contact_total_spent.setText(rupee_symbol+" "+String.format("%.2f",details.getTotal_spent()));
        contact_call_cost.setText("+ "+details.getSms_cost());

    }

    private void displayNormalDataPopup(final NormalDataPopup details) {
        TextView plan_id = (TextView) findViewById(R.id.plan_id);
        plan_id.setText("Data: Main Bal");
        TextView head_1 = (TextView) findViewById(R.id.head1);
        head_1.setText("Data Cost");
        TextView head_2 = (TextView) findViewById(R.id.head2);
        head_2.setText("Main Balance");
        TextView head_3 = (TextView) findViewById(R.id.head3);
        head_3.setText("Data Used");
        TextView head_4 = (TextView) findViewById(R.id.head4);
        head_4.setText("Data Rate");

        TextView field_1 = (TextView) findViewById(R.id.field1);
        if(details.cost<0.0f)
            field_1.setText("N/A");
        else
            field_1.setText(rupee_symbol+" "+details.cost);
        TextView field_2 = (TextView) findViewById(R.id.field2);
        if(details.getMain_bal()<0.0f)
            field_2.setText("N/A");
        else
        {
            SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
            float minimum_bal = mSharedPreferences.getFloat("MINIMUM_BALANCE",10.0f);
            int popUpCount = mSharedPreferences.getInt("POP_UP_COUNT",1);
            mSharedPreferences.edit().putInt("POP_UP_COUNT",popUpCount+1);
            if(popUpCount%10==0)
            {
                rate = true;
            }
            if(details.getMain_bal() <= minimum_bal)
            {
                field_2.setTextColor(Color.parseColor("#ff0000"));
                recharge = true;
            }
            field_2.setText(rupee_symbol + " " + details.getMain_bal());
        }
        TextView field_3 = (TextView) findViewById(R.id.field3);
        if(details.data_used<0.0f)
            field_3.setText("N/A");
        else
            field_3.setText(details.getData_used()+" MB");
        TextView field_4 = (TextView) findViewById(R.id.field4);
        float dataRate = -1.0f;
        try
        {
            float data_10_kb = details.getData_used()/100;
            dataRate = data_10_kb/details.getCost();
        }
        catch (Exception e){}
        field_4.setText(String.format("%.1f",dataRate)+" P/KB");
        findViewById(R.id.validity_layout).setVisibility(View.GONE);
        findViewById(R.id.separator_id).setVisibility(View.GONE);
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
    }

    private void displayDataPackPopup(final PackDataPopup details) {
        TextView plan_id = (TextView) findViewById(R.id.plan_id);
        if(details.getPack_type()==null)
            plan_id.setText("Data Pack");
        else
            plan_id.setText(details.getPack_type());
        TextView head_1 = (TextView) findViewById(R.id.head1);

        head_1.setText("Data Used");
        TextView head_2 = (TextView) findViewById(R.id.head2);
        head_2.setText("Main Balance");
        TextView head_3 = (TextView) findViewById(R.id.head3);
        head_3.setText("Data Left");
        findViewById(R.id.field4_lout).setVisibility(View.GONE);

        String rupee_symbol = getResources().getString(R.string.rupee_symbol);
        TextView field_1 = (TextView) findViewById(R.id.field1);
        if(details.getData_used()<0.0f)
            field_1.setText("N/A");
        else
            field_1.setText(details.data_used+" MB");
        TextView field_2 = (TextView) findViewById(R.id.field2);
        if(details.getMain_bal()<0.0f)
            field_2.setText("N/A");
        else
        {
            SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
            float minimum_bal = mSharedPreferences.getFloat("MINIMUM_BALANCE",10.0f);
            int popUpCount = mSharedPreferences.getInt("POP_UP_COUNT",1);
            mSharedPreferences.edit().putInt("POP_UP_COUNT",popUpCount+1);
            if(popUpCount%10==0)
            {
                rate = true;
            }
            if(details.getMain_bal() <= minimum_bal)
            {
                field_2.setTextColor(Color.parseColor("#ff0000"));
                recharge = true;
            }
            field_2.setText(rupee_symbol + " " + details.main_bal);
        }
        TextView field_3 = (TextView) findViewById(R.id.field3);
        if(details.getMain_bal()<0.0f)
            field_3.setText("N/A");
        else
            field_3.setText(details.getData_left()+" MB");

        findViewById(R.id.field4_lout).setVisibility(View.GONE);
        findViewById(R.id.validity_layout).setVisibility(View.VISIBLE);
        TextView validity = (TextView) findViewById(R.id.validity);
        if(details.getValidity()==null)
            validity.setText("N/A");
        else
            validity.setText(details.getValidity());
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
    }

    private void CallPack_Type1(final PackCallPopup details) {
        TextView plan_id = (TextView) findViewById(R.id.plan_id);
        if(details.getPack_name()!=null)
            plan_id.setText(details.getPack_name());
        else
            plan_id.setText("Call Pack");
        TextView head_1 = (TextView) findViewById(R.id.head1);
        head_1.setText("Duration Used");
        TextView head_2 = (TextView) findViewById(R.id.head2);
        head_2.setText("Duration Left");
        TextView head_3 = (TextView) findViewById(R.id.head3);
        head_3.setText("Call Duration");
        TextView head_4 = (TextView) findViewById(R.id.head4);
        head_4.setText("Main Balance");

        TextView field_1 = (TextView) findViewById(R.id.field1);
        if(details.getPack_duration_used()<0)
            field_1.setText("N/A");
        else
            field_1.setText(details.getPack_duration_used()+" "+details.getUsed_metric());
        TextView field_2 = (TextView) findViewById(R.id.field2);
        if(details.getPack_duration_left()<0)
            field_2.setText("N/A");
        else
            field_2.setText(details.getPack_duration_left()+" "+details.getLeft_metric());
        TextView field_3 = (TextView) findViewById(R.id.field3);
        field_3.setText(getTimeFormatted(details.getDuration()));
        TextView field_4 = (TextView) findViewById(R.id.field4);
        if(details.getCurrent_balance()<0)
            field_4.setText("N/A");
        else
            field_4.setText(rupee_symbol+" "+String.format("%.2f",details.getCurrent_balance()));
        findViewById(R.id.validity_layout).setVisibility(View.VISIBLE);
        TextView validity = (TextView) findViewById(R.id.validity);
        if(details.getValidity()==null)
            validity.setText("N/A");
        else
            validity.setText(details.getValidity());
        findViewById(R.id.ussd_contact_layout).setVisibility(View.VISIBLE);

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
        //User Details
        View contactLayout = findViewById(R.id.ussd_contact_layout);
        contactLayout.setVisibility(View.VISIBLE);
        ImageView contact_picture = (ImageView) contactLayout.findViewById(R.id.ussd_contact_picture);
        TextView contact_name = (TextView) contactLayout.findViewById(R.id.ussd_contact_name);
        TextView contact_number = (TextView) contactLayout.findViewById(R.id.ussd_contact_number);
        TextView contact_carrier_circle = (TextView) contactLayout.findViewById(R.id.ussd_contact_carrier_circle);
        TextView contact_total_spent = (TextView) contactLayout.findViewById(R.id.ussd_contact_total_spent);
        TextView contact_call_cost = (TextView) contactLayout.findViewById(R.id.ussd_contact_call_cost);


        contact_name.setText(details.getName());
        Picasso.with(this).load(details.getImage_uri()).transform(new CircleTransform()).placeholder(R.drawable.default_contact_picture).into(contact_picture);
        contact_number.setText(details.getNumber());
        contact_carrier_circle.setText(details.getCarrier_circle());
        contact_total_spent.setText(rupee_symbol+" "+String.format("%.2f",details.getTotal_spent()));
        contact_call_cost.setText("+ "+String.format("%.2f",details.getCall_cost()));
    }

    private void CallPack_Type2(final PackCallPopup details) {
        TextView plan_id = (TextView) findViewById(R.id.plan_id);
        if(details.getPack_name()!=null)
            plan_id.setText(details.getPack_name());
        else
            plan_id.setText("Call Pack");
        TextView head_1 = (TextView) findViewById(R.id.head1);
        head_1.setText("Call Cost");
        TextView head_2 = (TextView) findViewById(R.id.head2);
        head_2.setText("Pack Balance");
        TextView head_3 = (TextView) findViewById(R.id.head3);
        head_3.setText("Call Duration");
        TextView head_4 = (TextView) findViewById(R.id.head4);
        head_4.setText("Main Balance");

        TextView field_1 = (TextView) findViewById(R.id.field1);
        if(details.getPack_bal_used()<0)
            field_1.setText("N/A");
        else
            field_1.setText(rupee_symbol+" "+details.getPack_bal_used());
        TextView field_2 = (TextView) findViewById(R.id.field2);
        if(details.getPack_bal_left()<0)
            field_2.setText("N/A");
        else
            field_2.setText(rupee_symbol+" "+details.getPack_bal_left());
        TextView field_3 = (TextView) findViewById(R.id.field3);
        field_3.setText(getTimeFormatted(details.getDuration()));
        TextView field_4 = (TextView) findViewById(R.id.field4);
        if(details.getCurrent_balance()<0)
            field_4.setText("N/A");
        else
            field_4.setText(rupee_symbol+""+details.getCurrent_balance());
        findViewById(R.id.validity_layout).setVisibility(View.VISIBLE);
        TextView validity = (TextView) findViewById(R.id.validity);
        if(details.getValidity()==null)
            validity.setText("N/A");
        else
            validity.setText(details.getValidity());
        findViewById(R.id.ussd_contact_layout).setVisibility(View.VISIBLE);

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
        //User Details
        View contactLayout = findViewById(R.id.ussd_contact_layout);
        contactLayout.setVisibility(View.VISIBLE);
        ImageView contact_picture = (ImageView) contactLayout.findViewById(R.id.ussd_contact_picture);
        TextView contact_name = (TextView) contactLayout.findViewById(R.id.ussd_contact_name);
        TextView contact_number = (TextView) contactLayout.findViewById(R.id.ussd_contact_number);
        TextView contact_carrier_circle = (TextView) contactLayout.findViewById(R.id.ussd_contact_carrier_circle);
        TextView contact_total_spent = (TextView) contactLayout.findViewById(R.id.ussd_contact_total_spent);
        TextView contact_call_cost = (TextView) contactLayout.findViewById(R.id.ussd_contact_call_cost);


        contact_name.setText(details.getName());
        Picasso.with(this).load(details.getImage_uri()).transform(new CircleTransform()).placeholder(R.drawable.default_contact_picture).into(contact_picture);
        contact_number.setText(details.getNumber());
        contact_carrier_circle.setText(details.getCarrier_circle());
        contact_total_spent.setText(rupee_symbol+" "+String.format("%.2f",details.getTotal_spent()));
        contact_call_cost.setText("+ "+details.getCall_cost());

    }

	private String getTimeFormatted(int totalSecs) {
        if(totalSecs<0)
            return "N/A";
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
