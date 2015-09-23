package com.builder.ibalance.parsers;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.telephony.SmsMessage;
import android.util.Log;

import com.builder.ibalance.database.models.Base;
import com.builder.ibalance.database.models.DataPack;
import com.builder.ibalance.database.models.NormalCall;
import com.builder.ibalance.database.models.NormalData;
import com.builder.ibalance.database.models.NormalSMS;
import com.builder.ibalance.util.ConstantsAndStatics.USSDMessageType;
import com.builder.ibalance.util.MyApplication;

public class USSDParser {
	
	final String TAG = USSDParser.class.getSimpleName();
	USSDMessageType type;
	Base details;
	public Base getDetails() {
		return details;
	}

	public boolean parseMessage(String message)
	{
		if(parseForNormalCall(message))
		{
			return true;
		}
		else
		if(parseForNormalData(message))
		{
			return true;
		}
		else 
		if(parseForDataPack(message))
		{
			return true;
		}
		else 
		if (parseForNomalSMS(message)) {
			return true;
		}
			
		
		return false;//Invalid
	}
	

	private boolean parseForNomalSMS(String message) {
		Float balance=(float) 0.0, cost = (float) 0.0;
		Long time = (new Date()).getTime();
		String lastNumber = "";
		int count=0;
		
		String smsCost = "(Last SMS|SMS cost|SMS Cost|SMS COST|SMS charge from Main Bal):?\\s*R?s?\\s*[\\.=]?\\s*(\\d+\\.?\\d+)";
		Pattern pcc;
		Matcher mcc;
		 pcc = Pattern.compile(smsCost);
		 mcc = pcc.matcher(message);
		if (mcc.find()) {
			cost = Float.parseFloat(mcc.group(2));
			count++;
			System.out.println(TAG+" SMS "+ "Found SMS Cost " + cost);
		}
	
		boolean flag=false;
		// get the remaining balance
		String smsBal = "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|main balance left|balance left|RemainingSMSBal|Account Balance is):?\\s*R?s?\\s*[\\.=]?\\s*(\\d+\\.?\\d+)";
		pcc = Pattern.compile(smsBal);
		mcc = pcc.matcher(message);
		if (mcc.find()) {
			flag=true;
			count++;
			balance = Float.parseFloat(mcc.group(2));
			System.out.println(TAG +" SMS"+ "Found bal " + balance.toString());
		}
		
		{
			//Rs not optional overide the previous
			smsBal = "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|main balance left|balance left|Main Bal:):?\\s*Rs\\s*[\\.=]?\\s*(\\d+\\.?\\d+)";
			pcc = Pattern.compile(smsBal);
			mcc = pcc.matcher(message);
			if (mcc.find()) {
				if(!flag)
				count++;
				balance = Float.parseFloat(mcc.group(2));
				System.out.println(TAG +" SMS"+ "Found bal " + balance.toString());
			}
		}
		if(count==2)
		{
			System.out.println("Found SMS Pattern");
			// Create Sent box URI
			Uri sentURI = Uri.parse("content://sms/sent");
			 
			// List required columns
			String[] reqCols = new String[] {"address"};
			 
			// Get Content Resolver object, which will deal with Content Provider
			ContentResolver cr = MyApplication.context.getContentResolver();
			 
			// Fetch Sent SMS Message from Built-in Content Provider
			Cursor c = cr.query(sentURI, reqCols, null, null, "date "+ "DESC  , address" + " LIMIT 1");
			if(c.moveToFirst())
			{
				lastNumber = c.getString(c.getColumnIndex("address"));
			}
			type = USSDMessageType.NORMAL_SMS;
			details = new NormalSMS(time, cost, balance, lastNumber, message);
			Log.d(TAG+" SMS", "details = "+details.toString());
			return true;
		}
		return false;
	}
	private boolean parseForDataPack(String message) {
		Float data_consumed = (float)0.0,bal=(float)0.0,data_left = (float)0.0;
		int count=0;
		//3G usage:0.04MB 3G Bal:1048.45MB Val:Jun 02 2015 Bal:Rs.93.35
		//USSD2G usage:9.91MB 2G Bal:163.65MB Val:May 13 2015 Bal:Rs.60.44
		//Data_CHRG:0.00 INR, Bal_Left=71.85 INR, Vol_Used:0.027343 MB,Freebie_bal:492.76 MB, Pack_exp: May 13 2015
		//Ur last data session Usage was: 0.018 MB, Ur Data pack is 3G 1 GB Pack, 557.98 MB can be used till 2015-05-24, Unbilled amount for data usage is Rs 0.00
		//Data Session Charge:Rs 0. Bal Rs.0.01. Vol Used :0.06 MB. Data Left: 748 MB. Val :24/05/2015.
		//CHRG:0.00INR,VOL:0.064MB, Available 3G Pack Benefit 212.720MB,Val till 24-05-2015,BAL-LEFT:99.560INR.
		//Data Session Charge:Rs 0. Bal Rs.0.01. Vol Used :0.00 MB. Data Left: 749 MB. Val :24/05/2015.
		//Your last call of 10Kb was charged from 50MBFREE.Remaining Balance 46Mb 806Kb. Your Main Account Balance: 15.155
		try{
		String dataUsedRegex =  "[usage|Vol_Used|last data session Usage|VOL|Vol|vol|USAGE|Usage|Vol Used|vol used|VOL USED|InternetUsage|DataUsage|Data_Usage|Data\\-Usage|Consumed volume|Consumed_volume|ConsumedVolume|Vol_Used|Volume Used|Vol\\-Used][\\s+]?:?[\\s+]?(\\d+\\.\\d+)[\\s+]?(MB|Mb)";
		Pattern pcc;
		Matcher mcc;
		 pcc = Pattern.compile(dataUsedRegex);
		 mcc = pcc.matcher(message);
		if (mcc.find()) {
			data_consumed = Float.parseFloat(mcc.group(1));
			count++;
			//Log.d(TAG+" DATA ", "Found Data Pack Consumed " + data_consumed);
		}
		String bsnlDataUsedRegex = "Your last call of (\\d+)Mb|MB\\s*(\\d+)Kb|KB";
		pcc = Pattern.compile(bsnlDataUsedRegex);
		 mcc = pcc.matcher(message);
		 if (mcc.find()) {
			 System.out.println("Group 2 "+mcc.group(2));
				data_consumed = Float.parseFloat(mcc.group(1))+(Float.parseFloat(mcc.group(2))/1000);
				count++;
				System.out.println(TAG+" DATA BSNL"+ "Found Data Pack Consumed " + data_consumed);
			}
		 else
		 {
			  bsnlDataUsedRegex = "Your last call of\\s*(\\d+)Kb|KB";
				pcc = Pattern.compile(bsnlDataUsedRegex);
				 mcc = pcc.matcher(message);
				 if (mcc.find()) {
					 
						data_consumed = Float.parseFloat(mcc.group(1))/1000;
						count++;
						System.out.println(TAG+" DATA BSNL"+ "Found Data Pack Consumed " + data_consumed);
					}
		 }
		String dataLeftRegex = "(Bal|BAL|bal|Data Left|DATA LEFT|data left|Available 3G Pack Benefit|Available 2G Pack Benefit|Freebie_bal|Data_Left)\\s*:?\\s*(\\d+\\.?\\d*)\\s?MB";
		pcc = Pattern.compile(dataLeftRegex);
		 mcc = pcc.matcher(message);
		if (mcc.find()) {
			data_left = Float.parseFloat(mcc.group(2));
			count++;
			//Log.d(TAG+" DATA ", "Found Data Pack Left " + data_left);
		}
		//Your last call of 10Kb was charged from 50MBFREE.Remaining Balance 46Mb 806Kb. Your Main Account Balance: 15.155
		String bsnlDataLeftRegex = "Remaining Balance (\\d+)(Mb|MB)\\s*(\\d+)Kb|KB";
		pcc = Pattern.compile(bsnlDataLeftRegex);
		 mcc = pcc.matcher(message);
		 if (mcc.find()) {

			 System.out.println("Group 1 "+mcc.group(1));
			 System.out.println("Group 2 "+mcc.group(3));
			 data_left = Float.parseFloat(mcc.group(1))+(Float.parseFloat(mcc.group(3))/1000);
				count++;
				System.out.println(TAG+" DATA BSNL"+ "Found Data Pack data_left " + data_left);
			}
		 else
		 {
			 bsnlDataLeftRegex = "Remaining Balance\\s*(\\d+)Kb|KB";
				pcc = Pattern.compile(bsnlDataLeftRegex);
				 mcc = pcc.matcher(message);
				 if (mcc.find()) {
					 
					 data_left = Float.parseFloat(mcc.group(1))/1000;
						count++;
						//Log.d(TAG+" DATA BSNL", "Found Data Pack data_left " + data_consumed);
					}
		 }
		
		// get the remaining balance there are two different types to get main balance 
		 boolean flag = false;
			String remBalanceRegex = "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|Main Account Balance)\\s*:?\\s*(Rs)?\\s*[\\.]?=?\\s*(\\d+\\.\\d+)";
			pcc = Pattern.compile(remBalanceRegex);
			mcc = pcc.matcher(message);
			if (mcc.find()) {
				count++;
				flag = true;
				bal = Float.parseFloat(mcc.group(3));
				System.out.println(TAG +" DATA "+ "Found DataPack Rs bal " + bal.toString());
			}
			//hack to the problem posed by BSNL
			 remBalanceRegex = "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|Main Account Balance)\\s*:?\\s*(Rs)\\s*[\\.]?=?\\s*(\\d+\\.\\d+)";
			pcc = Pattern.compile(remBalanceRegex);
			mcc = pcc.matcher(message);
			if (mcc.find()) {
				if(!flag)
				count++;
				
				bal = Float.parseFloat(mcc.group(3));
				System.out.println(TAG +" DATA "+ "Found Hack DataPack Rs bal " + bal.toString());
			}
		else{
		String INRremBalanceRegex = "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left)\\s*:?\\s*R?s?\\s*[\\.]?=?\\s*(\\d+\\.\\d+)\\s*INR";
		pcc = Pattern.compile(INRremBalanceRegex);
		mcc = pcc.matcher(message);
		if (mcc.find()) {
			count++;
			bal = Float.parseFloat(mcc.group(2));
			//Log.d(TAG +" DATA ", "Found DataPack INR bal " + bal.toString());
		}
		}
		String validityRegex = "(Val|val|VAL|Pack_exp|can be used till|Val till)\\s*:?\\s*(\\d\\d/\\d\\d/\\d\\d\\d\\d|\\d\\d\\-\\d\\d\\-\\d\\d\\d\\d|\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d|[a-zA-Z]{3}\\s\\d\\d\\s\\d\\d\\d\\d)";
		pcc = Pattern.compile(validityRegex);
		mcc = pcc.matcher(message);
		String validity = "Unknown";
		if (mcc.find()) {
			count++;
			validity = mcc.group(2);
			System.out.println(TAG +" DATA "+ "Found DataPack Validity " + mcc.group(2));
		}
		if(count>=3)
			{
			//Log.d(TAG+" DATA ","Found Data pack Message");
			type = USSDMessageType.DATA_PACK;
			details = new DataPack((new Date()).getTime(), data_consumed, data_left, bal, validity, message);
			return true;
			}

		//Log.d(TAG+" DATA ","NOT a Data pack Message");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//Log.d(TAG, "Failed to parse");
		}
		return false;
	}

	public USSDMessageType getType()
	{
		
		return type;
	}
	
	private boolean parseForNormalCall(String message) {
		int secs = 0, count = 0;
		Float bal = (float) 0.0, callCost = (float) 0.0;
		Long time = (new Date()).getTime();
		//Log.d(TAG+" CALL", "parseForNormalCall");
		//Log.d(TAG+" CALL" + "from service", message);
		//Log.d(TAG+" CALL" + "time from service", time.toString());

		Pattern pcc;
		Matcher mcc;
			// get the call cost
		//Call_Durn : 00:00:51, Call_CHRG: 0.849, Bal_Left= 70.997
		//Last call charge for 00:01:50 is from Main Bal:0.500.Main Bal:Rs.24.460,28-07-2027.RCH91 All India calls at 25p for 84days
		//Your last call cost Rs.0.225,talk time used 0:0:15,main balance left 15.365.
		//Your last call cost Rs.0.660,talk time used 0:0:44,main balance left 25.258.null
		try{
			
			String callCostRegex = "(Call|call|Voice|voice|VOICE|Last|LAST|last)?_?\\s*(Deduction:CORE BAL|Call charged from:Main Bal|Charge|call cost|CALL COST|Call Cost|charge|cost|Cost|COST|CHRG|CHARGE|from Main Bal|CHRG:main_cost|Usage|USAGE|usage)\\s*:?\\s*R?s?\\s*-?:?[\\.=]?\\s*(\\d+\\.\\d+)";
			 pcc = Pattern.compile(callCostRegex);
			 mcc = pcc.matcher(message);

			if (mcc.find()) {
				callCost = Float.parseFloat(mcc.group(3));
				count++;
				//Log.d(TAG+" CALL", "Found callCost " + callCost.toString());
				// System.out.println("Airtel call cost: " + mcc.group(1));
			}
			boolean flag=false;
			// get the remaining balance
			String remBalanceRegex =  "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|main balance left|balance left|Current bal is|BAL_LEFT: main|BAL_LEFT:main|BAL_LEFT : main|BAL_LEFT :main|Balance :Talktime|Remaing Main Account Bal)\\s*:?-?\\s?R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)\\s*(INR)?";
			pcc = Pattern.compile(remBalanceRegex);
			mcc = pcc.matcher(message);
			if (mcc.find()) {
				flag = true;
				count++;
				bal = Float.parseFloat(mcc.group(2));
				//Log.d(TAG +" CALL", "Found bal " + bal.toString());
			}
			
			{
				//Rs not optional overide the previous
				remBalanceRegex = "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|main balance left|balance left|Main Bal:|BAL_LEFT: main|BAL_LEFT:main|BAL_LEFT : main|BAL_LEFT :main|Balance :Talktime):?\\s*Rs\\s*[\\.=]?\\s*(\\d+\\.\\d+)";
				pcc = Pattern.compile(remBalanceRegex);
				mcc = pcc.matcher(message);
				if (mcc.find()) {
					if(!flag)
					count++;
					bal = Float.parseFloat(mcc.group(2));
					System.out.println(TAG +" CALL"+ "Found bal  " + bal.toString());
				}
			}
			if(message.contains("Remaining bal after the call: Main Bal")|| message.contains("RemainingBal:CORE BAL"))
			{
				 remBalanceRegex =  "(Remaining bal after the call: Main Bal|RemainingBal:CORE BAL):?\\s*R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)";
				pcc = Pattern.compile(remBalanceRegex);
				mcc = pcc.matcher(message);
				if (mcc.find()) {
					
					count++;
					bal = Float.parseFloat(mcc.group(2));
					System.out.println(TAG +" CALL"+ "Found IDEA bal " + bal.toString());
				}
				
			}
			if(message.contains("Available Main Bal"))
			{
				 remBalanceRegex =  "(Available Main Bal|AVAILABLE MAIN BAL|available main bal):?\\s*R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)";
					pcc = Pattern.compile(remBalanceRegex);
					mcc = pcc.matcher(message);
					if (mcc.find()) {
						
						count++;
						callCost = bal;
						bal = Float.parseFloat(mcc.group(2));
						System.out.println(TAG +" CALL"+ "Found Reliance bal " + bal.toString());
					}
			}
			//Call_Durn : 00:00:51,
			 // get the call duration
			
				String callDurationRegex = "\\d+:\\d+:\\d+";

				pcc = Pattern.compile(callDurationRegex);
				mcc = pcc.matcher(message);

				if (mcc.find()) {
					String callDuration = mcc.group();

					//Log.d(TAG +" CALL"+ "call Duration", callDuration);
					String sub[] = callDuration.split(":");
					//Log.d(TAG + "call sec 0 ", sub[0]);
					//Log.d(TAG + "call sec 1 ", sub[1]);
					//Log.d(TAG + "call sec 2 ", sub[2]);
					secs = Integer.parseInt(sub[0]) * 60 * 60;
					secs += Integer.parseInt(sub[1]) * 60;
					secs += Integer.parseInt(sub[2]);
					//Log.d(TAG +" CALL"+ "found airtel/vodafone call Duration ",secs + " ");
					count++;
				} else {

					callDurationRegex = "(duration|Duration|DURATION|DURN|durn|DUR|dur|Dur|Call_Durn:):?\\s*(\\d+)\\s*(Sec|sec|SEC)(s|S)?";
					pcc = Pattern.compile(callDurationRegex);
					mcc = pcc.matcher(message);
					if (mcc.find()) {

						secs = Integer.parseInt(mcc.group(2));
						//Log.d(TAG+" CALL", "Found Docomo/idea/aircel" + secs);
						count++;
					}
				}
				if(secs>36000)
				{
					callDurationRegex = "(duration|Duration|DURATION|DURN|durn|DUR|dur|Dur|Call_Durn:):?\\s*(\\d+)\\s*(Sec|sec|SEC)(s|S)?";
					pcc = Pattern.compile(callDurationRegex);
					mcc = pcc.matcher(message);
					if (mcc.find()) {

						secs = Integer.parseInt(mcc.group(2));
						//Log.d(TAG+" CALL", "Found Docomo/idea/aircel" + secs);
						count++;
					}
				}
				if(count>=3)
				{
					//Log.d(TAG +" CALL", "message was of NormalCall");
					String lastNumber = CallLog.Calls
							.getLastOutgoingCall(MyApplication.context);
					type = USSDMessageType.NORMAL_CALL;
					details = new NormalCall(time,callCost,bal,secs,lastNumber,message);
					return true;
				}
				
				//Log.d(TAG+" CALL", "message was of NOT OF NormalCall");
	}
				catch(Exception e)
				{
					e.printStackTrace();
					//Log.d(TAG, "Failed to parse");
				}
				
		return false;
	}
	
	private boolean parseForNormalData(String message) {
		//Log.d(TAG+" DATA", "parseForNormalData");
		int count =0;
		Float bal = (float) 0.0, cost = (float) 0.0,data_consumed =(float) 0.0;
		Long time = (new Date()).getTime();
		//Log.d(TAG+" DATA" + "from service", message);
		//Log.d(TAG+" DATA" + "time from service", time.toString());

		Pattern pcc;
		Matcher mcc;
			// get the call cost
		//docomo Data Session Charge:Rs 0.10. Bal Rs.6.23. Vol Used :0.00 MB. .
		//Session date:19.05.2015, Session cost: 0.96INR, Consumed volume: 0.232MB, Current Balance: 27.10INR. Dial *121# for Internet Packs.
			try{
			String costRegex = "[Data|DATA|data]?(Session|session|SESSION)\\s*(Charge|charge|cost|Cost|CHRG|CHARGE)\\s*:?\\s?R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)[INR]?";
			 pcc = Pattern.compile(costRegex);
			 mcc = pcc.matcher(message);

			if (mcc.find()) {
				cost = Float.parseFloat(mcc.group(3));
				count++;
				//Log.d(TAG+" DATA", "Found Normal Data Cost " + cost.toString());
				// System.out.println("Airtel call cost: " + mcc.group(1));
			}
			
			String dataConsumedRegex = "(DataUsage|Data_Usage|Data-Usage|Consumed volume|Consumed_volume|ConsumedVolume|Vol Used|Vol_Used|Volume Used|Vol-Used)\\s*:?\\s*(\\d+\\.\\d+)\\s?(MB|mb|Mb)";
			pcc = Pattern.compile(dataConsumedRegex);
			 mcc = pcc.matcher(message);

			if (mcc.find()) {
				data_consumed = Float.parseFloat(mcc.group(2));
				count++;
				System.out.println(TAG+" DATA "+ "Found Normal Data Consumed " + data_consumed);

				//Log.d(TAG+" DATA", "Found Normal Data Consumed " + data_consumed);
				// System.out.println("Airtel call cost: " + mcc.group(1));
			}
			
			
			

			// get the remaining balance
			String remBalanceRegex = "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left)\\s*?:?\\s*?R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)";
			pcc = Pattern.compile(remBalanceRegex);
			mcc = pcc.matcher(message);
			if (mcc.find()) {
				count++;
				bal = Float.parseFloat(mcc.group(2));
				//Log.d(TAG +" CALL", "Found bal " + bal.toString());
			}
			if(count>=2)
				
		{
				type = USSDMessageType.NORMAL_DATA;
				details = new NormalData(time, cost, data_consumed, bal, message);
				//Log.d(TAG+" DATA", "message was of NOT OF NormalDATA");
				return true;
		}
	}
			catch(Exception e)
			{
				e.printStackTrace();
				//Log.d(TAG, "Failed to parse");
			}
		return false;
	}
	

}
