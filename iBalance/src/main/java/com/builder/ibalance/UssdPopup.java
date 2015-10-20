package com.builder.ibalance;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.builder.ibalance.services.CallDetailsModel;
import com.builder.ibalance.util.CircleTransform;
import com.builder.ibalance.util.Helper;
import com.bumptech.glide.Glide;

public class UssdPopup extends Activity {
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
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
            CallDetailsModel details = mintent.getParcelableExtra("DATA");
			head1.setText("Call Cost");
			field1.setText(rupee_symbol+" "+details.getCall_cost());

			head2.setText("Current Balance");
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
            Glide.with(this).load(details.getImage_uri()).transform(new CircleTransform(this)).placeholder(R.drawable.default_contact_picture).into(contact_picture);
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
			break;
		default:
			break;
		}
		
		dismiss = (Button) findViewById(R.id.ussd_dismiss);
		ImageView infoButton = (ImageView) findViewById(R.id.info_button);
		open_app = (Button) findViewById(R.id.ussd_open_app);
		infoButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Toast.makeText(getApplicationContext(), "This is a Custom message,With call rate info.",Toast.LENGTH_LONG).show();
				
			}
		});
		dismiss.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
                Helper.logUserEngageMent(from+"_DISMISS");
				finish();
				
			}
		});
		open_app.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent mIntent = new Intent(getApplicationContext(),SplashscreenActivity.class);
				mIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				mIntent.putExtra("FROM", "POPUP");
                Helper.logUserEngageMent(from+"_OPEN");
				startActivity(mIntent);
     			finish();
				
			}
		});
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
