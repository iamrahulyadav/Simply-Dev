package com.builder.ibalance.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.builder.ibalance.BalanceWidget;
import com.builder.ibalance.BuildConfig;
import com.builder.ibalance.MainActivity;
import com.builder.ibalance.R;
import com.builder.ibalance.UssdPopup;
import com.builder.ibalance.database.helpers.BalanceHelper;
import com.builder.ibalance.database.helpers.ContactDetailHelper;
import com.builder.ibalance.database.helpers.NormalDataHelper;
import com.builder.ibalance.database.helpers.NormalSMSHelper;
import com.builder.ibalance.database.helpers.PackCallHelper;
import com.builder.ibalance.database.helpers.PackDataHelper;
import com.builder.ibalance.database.helpers.PackSMSHelper;
import com.builder.ibalance.database.helpers.RechargeHelper;
import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.database.models.RechargeEntry;
import com.builder.ibalance.messages.OutgoingCallMessage;
import com.builder.ibalance.messages.OutgoingSmsMessage;
import com.builder.ibalance.models.PopupModels.NormalCallPopup;
import com.builder.ibalance.models.PopupModels.NormalDataPopup;
import com.builder.ibalance.models.PopupModels.NormalSmsPopup;
import com.builder.ibalance.models.PopupModels.PackCallPopup;
import com.builder.ibalance.models.PopupModels.PackDataPopup;
import com.builder.ibalance.models.PopupModels.PackSmsPopup;
import com.builder.ibalance.models.USSDModels.NormalCall;
import com.builder.ibalance.models.USSDModels.NormalData;
import com.builder.ibalance.models.USSDModels.NormalSMS;
import com.builder.ibalance.models.USSDModels.PackCall;
import com.builder.ibalance.models.USSDModels.PackData;
import com.builder.ibalance.models.USSDModels.PackSMS;
import com.builder.ibalance.models.USSDModels.USSDBase;
import com.builder.ibalance.parsers.USSDParser;
import com.builder.ibalance.util.ConstantsAndStatics;
import com.builder.ibalance.util.GlobalData;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RecorderUpdaterService extends AccessibilityService
{
    final static String tag = RecorderUpdaterService.class.getSimpleName();
    static String TAG = "RecorderService", second = null;
    boolean isRegistered = false;
    volatile boolean callEventDetailsReady = false;
    volatile boolean callussdDetailsReady = false;
    OutgoingCallMessage tempCallEventDetails;
    USSDBase tempCallUSSDDetails;
    long startTime,endTime;
    float previousBalance = (float) -20.0;
    CallLogObserver mCallLogObserver;
    AccessibilityNodeInfo dismissNode = null;
    String text;
   /* private void displayPopUp()
    {

        if (lastNumber != null)
        {
            if (noUSSDMsgHandler == null)
                noUSSDMsgHandler = new Handler();
            Runnable r = new Runnable()
            {
                public void run()
                {
                    mCallDetailsModel = null;
                    lastNumber = null;
                    sim_slot = 0;
                  //  Toast.makeText(MyApplication.context, "USSD Message was not Received", Toast.LENGTH_LONG).show();
                }
            };
           //V10Log.d(tag + "  displayPopUp", " Last Number =" + lastNumber);
            if (mCallDetailsModel != null)
            {

                Intent popup_intent = new Intent(getApplicationContext(),
                        UssdPopup.class);
                popup_intent.putExtra("TYPE", 1);
                String number = lastNumber;



                ContactDetailModel mDetails = new ContactDetailHelper().getPopUpDetails(number);
                mCallDetailsModel.setSim_slot(sim_slot);
                mCallDetailsModel.setName(mDetails.name);
                mCallDetailsModel.setImage_uri(mDetails.image_uri);
                mCallDetailsModel.setNumber(number);

                mCallDetailsModel.setCarrier_circle(mDetails.carrier + ',' + mDetails.circle);
                mCallDetailsModel.setTotal_spent(mDetails.total_cost);
               //V16//V16Log.d(TAG,"Call detail = "+mCallDetailsModel.toString());
                popup_intent.putExtra("DATA", mCallDetailsModel);
                popup_intent
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                popup_intent
                        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                popup_intent
                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                noUSSDMsgHandler.removeCallbacks(r);
                noUSSDMsgHandler = null;
                dismissNode = null;
                endTime = System.nanoTime();
               //V16//V16Log.d(TAG,"Time = "+((endTime-startTime)/1000000)+"ms");
                startActivity(popup_intent);
                NormalCall entry = new NormalCall(_id,
                        new Date().getTime(),
                        sim_slot,
                        mCallDetailsModel.getCall_cost(),
                        mCallDetailsModel.getCurrent_balance(),
                        mCallDetailsModel.getDuration(),
                        lastNumber,
                        mCallDetailsModel.getMessage());
                mCallDetailsModel = null;
                lastNumber = null;
                addToDatabase(entry);
            } else
            {
               //V10Log.d(tag, "USSD Message Not Received");

                noUSSDMsgHandler.postDelayed(r, 15000);
                //wait for 30 seconds and show that USSD message was not received
            }
        } else
        {
           //V10Log.d(tag, "CallLog Not Updated");
            //wait for callLog to update
        }
    }
*/
    void addToDatabase(USSDBase entryBase)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY,
            Context.MODE_PRIVATE);

        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        int sim_slot = 0;
        switch (entryBase.getType())
        {
            case ConstantsAndStatics.USSD_TYPES.NORMAL_CALL:
                NormalCall callDetails = (NormalCall)entryBase;
                BalanceHelper mBalanceHelper = new BalanceHelper();
                checkForRecharge(entryBase);
                mBalanceHelper.addEntry(callDetails);
                ////V10Log.d(tag + "Current Bal", details.bal + " ");
                sharedPreferences.edit().putFloat("CURRENT_BALANCE_"+callDetails.sim_slot, (float) callDetails.main_bal).commit();
                break;
            case ConstantsAndStatics.USSD_TYPES.PACK_CALL:
                PackCall mPackDetails = (PackCall) entryBase;
                sim_slot = mPackDetails.sim_slot;
                BalanceHelper mBalanceHelper2 = new BalanceHelper();
                mBalanceHelper2.addEntry(mPackDetails.getBaseCallDetails());
                (new PackCallHelper()).addEntry(mPackDetails);
                ////V10Log.d(tag + "Current Bal", details.bal + " ");
                mEditor.putBoolean("PACK_CALL_ACTIVE_"+sim_slot,true);
                if(mPackDetails.isMinsType())
                {
                    //time pack
                    mEditor.putBoolean("MIN_TYPE_"+sim_slot,true);
                    mEditor.putInt("PACK_CALL_DUR_REMAINING_"+sim_slot,mPackDetails.pack_duration_left);
                    mEditor.putString("PACK_CALL_TYPE_"+sim_slot,mPackDetails.pack_name);
                    mEditor.putString("PACK_CALL_DUR_METRIC_"+sim_slot,mPackDetails.left_metric);
                    mEditor.putString("PACK_CALL_VALIDITY_"+sim_slot,mPackDetails.validity);

                }
                else
                {
                    //Money pack
                    mEditor.putBoolean("MIN_TYPE_"+sim_slot,false);
                    mEditor.putFloat("PACK_CALL_BAL_REMAINING_"+sim_slot,mPackDetails.pack_bal_left);
                    mEditor.putString("PACK_CALL_TYPE_"+sim_slot,mPackDetails.pack_name);
                    mEditor.putString("PACK_CALL_VALIDITY_"+sim_slot,mPackDetails.validity);
                }
                mEditor.commit();
                if(mPackDetails.main_bal>0)
                {
                    mEditor.putFloat("CURRENT_BALANCE_"+sim_slot, (float) mPackDetails.main_bal);
                }
                break;
            case ConstantsAndStatics.USSD_TYPES.NORMAL_SMS:
                NormalSMS mNormalSMSDetails = (NormalSMS) entryBase;
                NormalSMSHelper mNormalSMSHelper = new NormalSMSHelper();

                if(mNormalSMSDetails.main_bal>0)
                {
                    sim_slot = guessSimSlot(mNormalSMSDetails.cost,mNormalSMSDetails.main_bal);
                    mEditor.putFloat("CURRENT_BALANCE_"+sim_slot, (float) mNormalSMSDetails.main_bal);
                }
                mNormalSMSDetails.sim_slot = sim_slot;
                mNormalSMSHelper.addEntry(mNormalSMSDetails);
                ////V10Log.d(tag + "Current Bal", details.bal + " ");

                break;
            case ConstantsAndStatics.USSD_TYPES.PACK_SMS:
                PackSMS packSMSDetails = (PackSMS) entryBase;
                NormalSMSHelper mNormalSMSHelper2 = new NormalSMSHelper();
                sim_slot=0;
                if(packSMSDetails.main_bal>0)
                {
                    sim_slot = guessSimSlot(0.0f,packSMSDetails.main_bal);
                    mEditor.putFloat("CURRENT_BALANCE_"+sim_slot,  packSMSDetails.main_bal);
                }
                packSMSDetails.sim_slot = sim_slot;
                //Add the base in normal sms , that it is sync with system SMS dB
                mNormalSMSHelper2.addEntry(packSMSDetails.getBaseDetails());
                PackSMSHelper mPackSMSHelper = new PackSMSHelper();
                mPackSMSHelper.addEntry(packSMSDetails);
                mEditor.putBoolean("PACK_SMS_ACTIVE_"+sim_slot,true);
                mEditor.putInt("PACK_SMS_REMAINING_"+sim_slot,packSMSDetails.rem_sms);
                mEditor.putString("PACK_SMS_TYPE_"+sim_slot,packSMSDetails.pack_type);
                mEditor.putString("PACK_SMS_VALIDITY_"+sim_slot,packSMSDetails.validity);
                ////V10Log.d(tag + "Current Bal", details.bal + " ");

                break;
            case ConstantsAndStatics.USSD_TYPES.NORMAL_DATA:
                NormalData normalDataDetails = (NormalData) entryBase;
                NormalDataHelper normalDataHelper = new NormalDataHelper();
                sim_slot=0;
                if(normalDataDetails.main_bal>0)
                {
                    sim_slot = guessSimSlot(normalDataDetails.cost,normalDataDetails.main_bal);
                    mEditor.putFloat("CURRENT_BALANCE_"+sim_slot,  normalDataDetails.main_bal);
                }
                normalDataDetails.sim_slot = sim_slot;
                normalDataHelper.addEntry(normalDataDetails);
                ////V10Log.d(tag + "Current Bal", details.bal + " ");
                break;
            case ConstantsAndStatics.USSD_TYPES.PACK_DATA:
                PackData packDataDetails = (PackData) entryBase;
                PackDataHelper packDataHelper = new PackDataHelper();
                sim_slot=0;
                if(packDataDetails.main_bal>0)
                {
                    sim_slot = guessSimSlot(0.0f,packDataDetails.main_bal);
                    mEditor.putFloat("CURRENT_BALANCE_"+sim_slot,  packDataDetails.main_bal);
                }
                packDataDetails.sim_slot = sim_slot;
                packDataHelper.addEntry(packDataDetails);

                mEditor.putBoolean("PACK_DATA_ACTIVE_"+sim_slot,true);
                mEditor.putFloat("PACK_DATA_REMAINING_"+sim_slot,packDataDetails.data_left);
                mEditor.putString("PACK_DATA_TYPE_"+sim_slot,packDataDetails.pack_type);
                mEditor.putString("PACK_DATA_VALIDITY_"+sim_slot,packDataDetails.validity);
                break;
            default:
        }
        mEditor.commit();
    }

    private int guessSimSlot(float cost, float main_bal)
    {
        //TODO need Testing

        if(GlobalData.globalSimList == null)
        {
            GlobalData.globalSimList =  new Helper.SharedPreferenceHelper().getDualSimDetails();
        }
        if(GlobalData.globalSimList.size()>1)
        {
            SharedPreferences sharedPreferences = getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY,
                    Context.MODE_PRIVATE);

            float bal_diff_0,bal_diff_1,pre_bal_1,pre_bal_0;
            pre_bal_0 = sharedPreferences.getFloat("CURRENT_BALANCE_0",9999.0f);
            pre_bal_1 = sharedPreferences.getFloat("CURRENT_BALANCE_1",9999.0f);
            bal_diff_0 = pre_bal_0 - cost - main_bal;
            bal_diff_1 = pre_bal_1 - cost - main_bal;
            if(bal_diff_0<=bal_diff_1)
            {
                return 0;
            }
            else
            {
                return 1;
            }

        }
        else
        {
            //Single Sim
            return 0;


        }

    }

    private void checkForRecharge(USSDBase base)
    {
        NormalCall details = null;
        if(base instanceof NormalCall)
            details = (NormalCall)base;
        SharedPreferences sharedPreferences = getSharedPreferences("USER_DATA",
                Context.MODE_PRIVATE);
        previousBalance = sharedPreferences.getFloat(
                "CURRENT_BALANCE_"+details.sim_slot, (float) -20.0);
        ////V10Log.d(tag, "previousBalance " + previousBalance);
        // if there has been a Recharge
        if (previousBalance >= 0.0)
        {
            if (details.main_bal - previousBalance > 1.0)
            {
                RechargeHelper mRechargeHelper = new RechargeHelper();
                ////V10Log.d(tag, "Recharge = "+ (details.bal -
                // previousBalance + details.callCost));
                ParseObject pObj = new ParseObject("RECHARGES");
                pObj.put("DEVICE_ID", Helper.getDeviceId());
                pObj.put("Total", text);
                pObj.put("NUMBER",
                        sharedPreferences.getString("VERIFIED_NUMBER", sharedPreferences.getString("NUMBER", "0000")));
                pObj.put("CARRIER",
                        sharedPreferences.getString("CARRIER_"+details.sim_slot, sharedPreferences.getString("CARRIER","Unknown")));
                pObj.put("CIRCLE",
                        sharedPreferences.getString("CIRCLE_"+details.sim_slot, sharedPreferences.getString("CIRCLE","Unknown")));
                pObj.put("Recharge", (details.main_bal
                        - previousBalance + details.call_cost));
                pObj.saveEventually();
                mRechargeHelper
                        .addRechargeEntry(new RechargeEntry(
                                details.date,
                                (details.main_bal - previousBalance + details.call_cost),
                                details.main_bal + details.call_cost));
            }
        }
    }

    private String getEventType(AccessibilityEvent event)
    {
        switch (event.getEventType())
        {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                return "TYPE_NOTIFICATION_STATE_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                return "TYPE_VIEW_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                return "TYPE_VIEW_FOCUSED";
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                return "TYPE_VIEW_LONG_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                return "TYPE_VIEW_SELECTED";
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                return "TYPE_WINDOW_STATE_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                return "TYPE_VIEW_TEXT_CHANGED";
        }
        return "default";
    }

boolean cancelButtonFound = false;

    public void onAccessibilityEvent(AccessibilityEvent event)
    {
        cancelButtonFound = false;
        text = getTextFromNode(event.getSource());// getEventText(event);
        if(text==null || text.isEmpty())
            return;
        SharedPreferences sharedPreferences = getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY,
                Context.MODE_PRIVATE);
        String previousUssdMessage = sharedPreferences.getString("PREV_MSG","");
        sharedPreferences.edit().putString("PREV_MSG",text).apply();
        if(previousUssdMessage.equals(text))
        {
           //V16//V16//V16Log.d(TAG,"Duplicate event: "+previousUssdMessage);
            return;
        }
        String original_message = text;
        text = text.replace("\r", "_").replace("\n", "_").replace("\u0011"," ").replace("ยง"," ").toUpperCase();
       //V16//V16Log.d(TAG,String.format("onAccessibilityEvent: [type] %s [class] %s [package] %s [time] %s [text] %s",
                        //getEventType(event), event.getClassName(),
                        //event.getPackageName(), event.getEventTime(), text));
        if (event.getClassName().toString().toUpperCase(Locale.US).contains("ALERT"))
        {
        try
        {
            USSDParser parser = new USSDParser();
            if (parser.parseMessage(text)==true) // if Valid
            {
               //V16//V16Log.d(TAG,"Successful Parse");
                ///Original Message
                parser.getDetails().original_message = original_message;
                processUSSD(parser);
            }
            else
            {
               //V16//V16Log.d(TAG,"UnSuccessful Parse");
                logOnParse(text);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        }

    }
    public void mockAccesibility(String text)
    {
        cancelButtonFound = false;
        text = text.replace("\r", "_").replace("\n", "_").replace("\u0011"," ").replace("ยง"," ").toUpperCase();
        //if (event.getClassName().toString().toUpperCase(Locale.US).contains("ALERT"))
        {
            try
            {
                USSDParser parser = new USSDParser();
                if (parser.parseMessage(text)==true) // if Valid
                {
                   //V16//V16Log.d(TAG,"Successful Parse");
                    processUSSD(parser);
                }
                else
                {
                   //V16//V16Log.d(TAG,"UnSuccessful Parse");
                    logOnParse(text);
                }

            }
            catch (Exception e)
            {

            }

        }

    }
    private void processUSSD(USSDParser parser)
    {
        USSDBase ussDetails = parser.getDetails();
       //V16//V16Log.d(TAG,ussDetails.toString());
        switch (ussDetails.getType())
        {
            case ConstantsAndStatics.USSD_TYPES.NORMAL_CALL:
               //V16//V16Log.d(TAG,"Type Normal call");
                //reset callDbUpdate
                //You have to wait for event details to show the pop up
                processCallUSSD(ussDetails);
                break;
            case ConstantsAndStatics.USSD_TYPES.PACK_CALL:
               //V16//V16Log.d(TAG,"Type Pack call");
                processCallUSSD(ussDetails);
                break;
            case ConstantsAndStatics.USSD_TYPES.NORMAL_SMS:
               //V16//V16Log.d(TAG,"Type Normal SMS");
                ///Need to fetch the number messaged to
                processSMS(ussDetails);
                break;
            case ConstantsAndStatics.USSD_TYPES.PACK_SMS:
               //V16//V16Log.d(TAG,"Type Pack SMS");
                processSMS(ussDetails);
                break;
            case ConstantsAndStatics.USSD_TYPES.NORMAL_DATA:
               //V16//V16Log.d(TAG,"Type Normal Data");
                //Just need to display the pop, no other data as of now
                processData(ussDetails);
                break;
            case ConstantsAndStatics.USSD_TYPES.PACK_DATA:
               //V16//V16Log.d(TAG,"Type Pack Data");
                processData(ussDetails);
                break;
        }

    }


    private void processCallEvent(Message msg)
    {
       //V16//V16Log.d(TAG,"processCallEvent");
        callEventDetailsReady = true;
        tempCallEventDetails = (OutgoingCallMessage) msg.obj;
        processAllCallDetails();
        final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

        exec.schedule(new Runnable(){
            @Override
            public void run(){
                //Reset all the race condition variables, If USSD message didn't arrive then insert an incomplete entry
                finalizeCall();
            }
        }, 10, TimeUnit.SECONDS);
    }

    private void finalizeCall()
    {
        //TODO Insert incomplete call details
       //V16//V16Log.d(TAG,"Resetting all Race Condition Variables");
        callussdDetailsReady = false;
        callEventDetailsReady = false;
        tempCallEventDetails = null;
        tempCallUSSDDetails = null;

    }

    private void processAllCallDetails()
    {
       //V16//V16Log.d(TAG,"processAllCallDetails ");
       //V16//V16Log.d(TAG,"callussdDetailsReady "+callussdDetailsReady);
       //V16//V16Log.d(TAG,"callEventDetailsReady "+callEventDetailsReady);

        if(callussdDetailsReady && callEventDetailsReady)
        {
           //V16//V16Log.d(TAG,"Event details = "+ tempCallEventDetails.toString());
            ((NormalCall)tempCallUSSDDetails).eventDetails(tempCallEventDetails);
           //V16//V16Log.d(TAG,"Call Details = "+tempCallUSSDDetails.toString());
            Intent popup_intent = new Intent(getApplicationContext(),
                    UssdPopup.class);

            ContactDetailModel userDetails = new ContactDetailHelper().getPopUpDetails(tempCallEventDetails.lastNumber);
            if(tempCallUSSDDetails instanceof  NormalCall)
            {
                NormalCallPopup   mNormalCallDetails = new NormalCallPopup((NormalCall) tempCallUSSDDetails);
                mNormalCallDetails.addUserDetails(userDetails);

                popup_intent.putExtra("TYPE", tempCallUSSDDetails.getType());
                popup_intent.putExtra("DATA", mNormalCallDetails);
               //V16//V16Log.d(TAG,"Call detail = "+mNormalCallDetails.toString());

               //V16//V16Log.d(TAG,"Displaying pop_up");
                showPopup(popup_intent);
            }
            else
            {
                PackCallPopup mPackCallPopupDetails = new PackCallPopup((PackCall) tempCallUSSDDetails);
                mPackCallPopupDetails.addUserDetails(userDetails);

                popup_intent.putExtra("TYPE", tempCallUSSDDetails.getType());
                popup_intent.putExtra("DATA", mPackCallPopupDetails);
               //V16//V16Log.d(TAG,"Call detail = "+mPackCallPopupDetails.toString());

                showPopup(popup_intent);
            }


           //V16//V16Log.d(TAG,"Adding to Db");
            addToDatabase(tempCallUSSDDetails);
        }
        //wait till both are filled otherwise reset them after 10 secs
    }
    void showPopup(Intent popup_intent)
    {

        popup_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        popup_intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        popup_intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(popup_intent);
    }
    private void processCallUSSD(USSDBase ussDetails)
    {
       //V16//V16Log.d(TAG,"processCallUSSD ");
        callussdDetailsReady  = true;
        if(tempCallUSSDDetails == null)
        {
            //EventDetails are not ready
            tempCallUSSDDetails = ussDetails;
        }
        else
        {
            //Event Details Already Came In
            ((NormalCall) tempCallUSSDDetails).USSDDetails((NormalCall)ussDetails);
        }
        processAllCallDetails();

    }

    private void processSMS(USSDBase ussDetails)
    {
        OutgoingSmsMessage eventDetails = getSmsEventDetails();
        Intent popup_intent = new Intent(getApplicationContext(),
                UssdPopup.class);
        if (eventDetails!=null)
        {
            ContactDetailModel userDetails = new ContactDetailHelper().getPopUpDetails(eventDetails.lastNumber);
            if(ussDetails instanceof NormalSMS)
            {
                ((NormalSMS)ussDetails).eventDetails(eventDetails);
                NormalSmsPopup smsPopupDetails = new NormalSmsPopup((NormalSMS)ussDetails);
                smsPopupDetails.addUserDetails(userDetails);
                popup_intent.putExtra("TYPE", ussDetails.getType());

                popup_intent.putExtra("DATA", smsPopupDetails);
               //V16//V16Log.d(TAG,"Normal SMS detail = "+smsPopupDetails.toString());



                showPopup(popup_intent);
            }
            else if(ussDetails instanceof PackSMS)
            {
                ((PackSMS)ussDetails).eventDetails(eventDetails);
                PackSmsPopup smsPopupDetails = new PackSmsPopup((PackSMS)ussDetails);
                smsPopupDetails.addUserDetails(userDetails);
                popup_intent.putExtra("TYPE", ussDetails.getType());
                popup_intent.putExtra("DATA", smsPopupDetails);
               //V16//V16Log.d(TAG,"SMS Pack detail = "+smsPopupDetails.toString());

                showPopup(popup_intent);
            }

        }
        else
        {
           //V16//V16Log.d(TAG,"Event Details Null");
        }

       //V16//V16Log.d(TAG,"Adding SMS to Db");
        addToDatabase(ussDetails);

    }



    private void processData(USSDBase ussDetails)
    {
        Intent popup_intent = new Intent(getApplicationContext(),
                UssdPopup.class);
        if(ussDetails instanceof NormalData)
        {
            NormalDataPopup normalDataDetails = new NormalDataPopup((NormalData)ussDetails);
            popup_intent.putExtra("TYPE", ussDetails.getType());

            popup_intent.putExtra("DATA", normalDataDetails);
           //V16//V16Log.d(TAG,"Normal Data detail = "+normalDataDetails.toString());

            if (dismissNode != null)
                dismissNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);

            showPopup(popup_intent);
        }
        else if(ussDetails instanceof PackData)
        {
            PackDataPopup packDataDetails = new PackDataPopup((PackData)ussDetails);
            popup_intent.putExtra("TYPE", ussDetails.getType());

            popup_intent.putExtra("DATA", packDataDetails);
           //V16//V16Log.d(TAG,"Pack Data detail = "+packDataDetails.toString());

            if (dismissNode != null)
                dismissNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);

            showPopup(popup_intent);
        }
        dismissNode = null;

       //V16//V16Log.d(TAG,"Adding Data to Db");
        addToDatabase(ussDetails);
    }

    private void logOnParse(String text)
    {
        if(text==null)
            return;
        SharedPreferences sharedPreferences = MyApplication.context.getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY,Context.MODE_PRIVATE);
        ParseObject pObj = new ParseObject("Invalid_USSD");
        pObj.put("DEVICE_ID",Helper.getDeviceId());
        pObj.put("Total", text);
        pObj.put("NUMBER",
                sharedPreferences.getString("VERIFIED_NUMBER", sharedPreferences.getString("NUMBER", "0000")));
        pObj.put("CARRIER",
                sharedPreferences.getString("CARRIER_0", sharedPreferences.getString("CARRIER","Unknown")));
        pObj.put("CIRCLE",
                sharedPreferences.getString("CIRCLE_0", sharedPreferences.getString("CIRCLE","Unknown")));
        pObj.saveEventually();
    }
    private OutgoingSmsMessage getSmsEventDetails()
    {
        SharedPreferences mPreferences = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        long previous_id = mPreferences.getLong("SMS_PREV_ID", -1l);
        Cursor cursor = MyApplication.context.getContentResolver().query(
                Uri.parse("content://sms/"),
                new String[]{"_id","address"},
                "_id" + ">? AND "+ "type" + " = "+ "2",
                new String[]{String.valueOf(previous_id)},
                "_id" + " DESC LIMIT 1");
        OutgoingSmsMessage mdetails=null;
        while (cursor.moveToNext())
        {
            long id;
            String number;
            id = cursor.getLong(cursor.getColumnIndex("_id"));
            number = cursor.getString(cursor.getColumnIndex("address"));
            //TODO Dual Sim Implementation
            mdetails = new OutgoingSmsMessage(id,number,0);
            mPreferences.edit().putLong("SMS_PREV_ID",id).apply();
        }
       //V16//V16Log.d(TAG,"SMS Details: "+mdetails);
        return mdetails;
    }
    /*public void onAccessibilityEvent(AccessibilityEvent event)
    {
        startTime = System.nanoTime();
        mCallDetailsModel = null;
        cancelButtonFound = false;
        text = getTextFromNode(event.getSource());// getEventText(event);
        text = text.replace("\r", "_").replace("\n", "_").replace("\u0011"," ").toUpperCase();
       //V10Log.d(TAG, "Dismissed AccessibilityNodeInfo");
        // text += getEventText(event);
        // = sb.toString();
        String ussd_details = String
                .format("onAccessibilityEvent: [type] %s [class] %s [package] %s [time] %s [text] %s",
                        getEventType(event), event.getClassName(),
                        event.getPackageName(), event.getEventTime(), text);
       //V10Log.d("USSD", ussd_details);

        SharedPreferences sharedPreferences = getSharedPreferences("USER_DATA",
                Context.MODE_PRIVATE);
        String type = "unknown";
        try
        {
            if (event.getClassName().toString().toUpperCase(Locale.US).contains("ALERT"))
            {
                // Toast.makeText(MyApplication.context, ussd_details,
                // Toast.LENGTH_LONG).show();

                USSDParser parser = new USSDParser();
                if (parser.parseMessage(text)) // if Valid
                {
                    Tracker t = ((MyApplication) this.getApplication())
                            .getTracker(TrackerName.APP_TRACKER);
                    Editor editor;
                    Intent popup_intent;
                    switch (parser.getType())
                    {
                    *//*
					 * NORMAL_CALL,//1 NORMAL_SMS,//2 NORMAL_DATA,//3
					 * VOICE_PACK,//4 SMS_PACK,//5 DATA_PACK,//6 BALANCE,//7
					 *//*
                        case NORMAL_CALL:
                            type = "NORMAL_CALL";
                            NormalCall details = (NormalCall) parser.getDetails();
                                ////V10Log.d(TAG + " test", "did a back");
                                FlurryAgent.logEvent("POPUP_SHOWN");

                               //V10AppsFlyerLib.sendTrackingWithEvent(MyApplication.context, "POPUP_SHOWN", "");
                                t.send(new HitBuilders.EventBuilder()
                                        .setCategory("POPUP").setAction("SHOWN")
                                        .setLabel("").build());
                                float call_rate = 1.7f;
                                try
                                {
                                    call_rate = (details.callCost / details.callDuration) * 100;
                                } catch (Exception e)
                                {

                                }
                                mCallDetailsModel = new CallDetailsModel(details.callCost, details.bal, call_rate, details.callDuration, details.message);

                                //Toast.makeText(this, "USSD POPUP Display", Toast.LENGTH_SHORT).show();
                                displayPopUp();
                                updateWidget((details).bal.toString());


                            break;
                        case NORMAL_DATA:
                        {
                            type = "NORMAL_DATA";
                            NormalData details1 = (NormalData) parser.getDetails();
                            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                            {
                                if (!hasEditText)
                                {
                                    performGlobalAction(GLOBAL_ACTION_BACK);
                                } else
                                {
                                    if (dismissNode != null)
                                        dismissNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                                FlurryAgent.logEvent("POPUP_SHOWN_NDATA");

                                //V10AppsFlyerLib.sendTrackingWithEvent(MyApplication.context, "POPUP_SHOWN_NDATA","");
                                t.send(new HitBuilders.EventBuilder().setCategory("POPUP").setAction("NDATA_SHOWN").setLabel("").build());
                                ////V10Log.d(TAG + " test", "did a back");
                                popup_intent = new Intent(getApplicationContext(), UssdPopup.class);
                                popup_intent.putExtra("TYPE", 3);
                                popup_intent.putExtra("BALANCE", details1.bal.toString());
                                popup_intent.putExtra("DATA_CONSUMED", String.format("%.3f", details1.data_consumed));
                                popup_intent.putExtra("DATA_COST", String.format("%.3f", details1.cost));
                                popup_intent.putExtra("MESSAGE", details1.message);
                                popup_intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
                                popup_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                popup_intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                popup_intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(popup_intent);
                            } else
                            {
                                FlurryAgent.logEvent("NDATA_POPUP_NOT_SHOWN");
                                //V10AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"NDATA_POPUP_NOT_SHOWN", "");
                                t.send(new HitBuilders.EventBuilder().setCategory("POPUP").setAction("NDATA_NOT_SHOWN").setLabel("").build());
                            }
                            NormalDataHelper mNormalDataHelper = new NormalDataHelper();

                            mNormalDataHelper.addEntry(details1);
                            ////V10Log.d(tag + "Current Bal", details1.bal + " ");
                            editor = sharedPreferences.edit();
                            //Hard coded but have to solve it later
                            editor.putFloat("CURRENT_BALANCE_0", (float) details1.bal);
                            editor.commit();
                        }
                            break;
                        case DATA_PACK:
                        {
                            type = "DATA_PACK";
                            DataPack details2 = (DataPack) parser.getDetails();
                            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                            {
                                if (!hasEditText)
                                {
                                    performGlobalAction(GLOBAL_ACTION_BACK);
                                } else
                                {
                                    if (dismissNode != null)
                                        dismissNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                                FlurryAgent.logEvent("POPUP_SHOWN_PDATA");
                                //V10AppsFlyerLib.sendTrackingWithEvent(MyApplication.context, "POPUP_SHOWN_PDATA","");
                                t.send(new HitBuilders.EventBuilder().setCategory("POPUP").setAction("PDATA_SHOWN").setLabel("").build());
                                ////V10Log.d(TAG + " test", "did a back");

                                popup_intent = new Intent(getApplicationContext(), UssdPopup.class);
                                popup_intent.putExtra("TYPE", 6);
                                popup_intent.putExtra("BALANCE", details2.bal.toString());
                                popup_intent.putExtra("DATA_CONSUMED", String.format("%.2f", details2.data_consumed));
                                popup_intent.putExtra("DATA_LEFT", details2.data_left + "");
                                popup_intent.putExtra("VALIDITY", details2.validity);
                                popup_intent.putExtra("MESSAGE", details2.message);
                                popup_intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
                                popup_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                popup_intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                popup_intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(popup_intent);

                            } else
                            {
                                FlurryAgent.logEvent("PDATA_POPUP_NOT_SHOWN");
                                //V10AppsFlyerLib.sendTrackingWithEvent( MyApplication.context,"PDATA_POPUP_NOT_SHOWN", "");
                                t.send(new HitBuilders.EventBuilder().setCategory("POPUP").setAction("PDATA_NOT_SHOWN").setLabel("").build());
                            }
                            DataPackHelper mDataPackHelper = new DataPackHelper();

                            mDataPackHelper.addEntry(details2);
                            ////V10Log.d(tag + "Current Bal", details2.bal + " ");
                            editor = sharedPreferences.edit();
                            editor.putFloat("CURRENT_DATA", (float) details2.data_left);
                            editor.commit();
                        }
                            break;
                        case NORMAL_SMS:
                        {
                            type = "NORMAL_SMS";
                            NormalSMS detail3 = (NormalSMS) parser.getDetails();
                            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                            {
                                if (!hasEditText)
                                {
                                    performGlobalAction(GLOBAL_ACTION_BACK);
                                } else
                                {
                                    if (dismissNode != null)
                                        dismissNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                                ////V10Log.d(TAG + " test", "did a back");
                                FlurryAgent.logEvent("POPUP_SMS_SHOWN");

                                //V10AppsFlyerLib.sendTrackingWithEvent(MyApplication.context, "POPUP_SMS_SHOWN","");
                                t.send(new HitBuilders.EventBuilder().setCategory("POPUP").setAction("SMS_SHOWN").setLabel("").build());
                                popup_intent = new Intent(getApplicationContext(), UssdPopup.class);
                                popup_intent.putExtra("TYPE", 2);
                                popup_intent.putExtra("BALANCE", detail3.bal.toString());
                                popup_intent.putExtra("SMS_COST", String.format("%.2f", detail3.cost));
                                popup_intent.putExtra("MESSAGE", detail3.message);
                                popup_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                popup_intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                popup_intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(popup_intent);
                                updateWidget((detail3).bal.toString());
                            } else
                            {

                                FlurryAgent.logEvent("POPUP_SMS_NOT_SHOWN");

                                //V10AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"POPUP_SMS_NOT_SHOWN", "");
                                t.send(new HitBuilders.EventBuilder().setCategory("POPUP").setAction("SMS_NOT_SHOWN").setLabel("").build());
                            }
                            NormalSMSHelper mNormalSMSHelper = new NormalSMSHelper();

                            previousBalance = sharedPreferences.getFloat("CURRENT_BALANCE_0", (float) -20.0);
                            ////V10Log.d(tag, "previousBalance " + previousBalance);
                            // if the entry is duplicate
                            if (Float.compare(previousBalance, detail3.bal) == 0)
                            {
                                ////V10Log.d(tag, "Duplicate  previousBalance ");
                                return;
                            }
                            // if there has been a Recharge
                            if (previousBalance >= 0.0)
                            {
                                if (detail3.bal - previousBalance > 1.0)
                                {
                                    RechargeHelper mRechargeHelper = new RechargeHelper();
                                    ////V10Log.d(tag, "Recharge = "+ (details.bal -
                                    // previousBalance + details.callCost));
                                    ParseObject pObj = new ParseObject("RECHARGES");
                                    pObj.put("DEVICE_ID", sharedPreferences.getString("DEVICE_ID", "123456"));
                                    pObj.put("Total", text);
                                    pObj.put("NUMBER", sharedPreferences.getString("NUMBER", "0000"));
                                    pObj.put("CARRIER", sharedPreferences.getString("CARRIER", "Unknown"));
                                    pObj.put("CIRCLE", sharedPreferences.getString("CIRCLE", "Unknown"));
                                    pObj.put("Recharge", (detail3.bal - previousBalance + detail3.cost));
                                    pObj.saveEventually();
                                    mRechargeHelper.addRechargeEntry(new RechargeEntry(detail3.date, (detail3.bal - previousBalance + detail3.cost), detail3.bal + detail3.cost));
                                }
                            }
                            mNormalSMSHelper.addEntry(detail3);
                            ////V10Log.d(tag + "Current Bal", details.bal + " ");
                            editor = sharedPreferences.edit();
                            editor.putFloat("CURRENT_BALANCE_0", (float) detail3.bal);
                            editor.commit();
                        }
                            break;

                        default:
                            break;
                    }
                    ParseObject pObj1 = new ParseObject("VALID_USSD");
                    pObj1.put("DEVICE_ID",
                            sharedPreferences.getString("DEVICE_ID", "123456"));
                    pObj1.put("TYPE", type);
                    pObj1.put("MESSAGE", text);
                    pObj1.put("DUAL_SIM",
                            sharedPreferences.getString("DUAL_SIM", "NO"));
                    pObj1.put("CARRIER",
                            sharedPreferences.getString("CARRIER", "Unknown"));
                    pObj1.put("CIRCLE",
                            sharedPreferences.getString("CIRCLE", "Unknown"));
                    pObj1.saveEventually();
                } else// invalid USSD Message
                {
                    ////V10Log.d(TAG + "Updater", "invalid USSD");

                    (new RegexUpdater()).check();
                    ParseObject pObj = new ParseObject("Invalid_USSD");
                    pObj.put("DEVICE_ID",Helper.getDeviceId());
                    if(text==null)
                        text="";
                    pObj.put("Total", text);
                    pObj.put("NUMBER",
                            sharedPreferences.getString("NUMBER", "0000"));
                    pObj.put("CARRIER",
                            sharedPreferences.getString("CARRIER_0", "Unknown"));
                    pObj.put("CIRCLE",
                            sharedPreferences.getString("CIRCLE_0", "Unknown"));
                    pObj.saveEventually();
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        hasEditText = false;

    }*/
    public String getTextFromNode(AccessibilityNodeInfo accessibilityNodeInfo)
    {
        StringBuilder sb = new StringBuilder();
        if (accessibilityNodeInfo == null)
        {
            ////V10Log.d("TEST", "accessibilityNodeInfo is null");
            return "";
        }

        int j = accessibilityNodeInfo.getChildCount();
        ////V10Log.d("TEST", "number of children = " + j);
        for (int i = 0; i < j; i++)
        {

            AccessibilityNodeInfo ac = accessibilityNodeInfo.getChild(i);

            if (ac == null)
            {
                ////V10Log.d(tag+"USSD","ac is null");
                continue;
            }
            if (ac.getChildCount() > 0)
            {
                ////V10Log.d(tag+"USSD", "More than one subchild"+
                // ac.getChildCount());
                sb.append(getTextFromNode(ac));
            }
            ////V10Log.d(tag+"USSD",ac.getClassName()+"");
            if (ac.getClassName().equals(TextView.class.getName()))
            {

                sb.append(ac.getText());
                ////V10Log.d("TEST", "Number:" + i + "   " + sb);
            }
            else if (ac.getClassName().equals(Button.class.getName()) && !cancelButtonFound)
            {
                //V10Log.d("TEST", "Button " + ac.getText());
                if(ac.getText()!=null )
                {
                    String cancelButtonText  = ac.getText().toString().toUpperCase().replace(" ","");
                    if(cancelButtonText.equals("CANCEL")||cancelButtonText.equals("DISMISS")||cancelButtonText.equals("OK")||cancelButtonText.equals("OKAY"))
                    {
                        cancelButtonFound = true;
                    }
                }
                dismissNode = ac;
                //V10Log.d("TEST", "Performed a Click ");
            }

        }
        return sb.toString();
    }
    private void updateWidget(String balance)
    {
        ////V10Log.d(tag,"Updating Widget"+ balance);

        AppWidgetManager mgr = AppWidgetManager
                .getInstance(getApplicationContext());

        ComponentName thisWidget = new ComponentName(getApplicationContext(),
                BalanceWidget.class);
        int[] allWidgetIds = mgr.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds)
        {

            RemoteViews remoteViews = new RemoteViews(getApplicationContext()
                    .getPackageName(), R.layout.balance_widget_layout);
            SharedPreferences mSharedPreferences = getApplicationContext()
                    .getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
            float currBalance = Float.parseFloat(balance);
            ////V10Log.d(tag, "balance = " + balance);
            // Set the text
            remoteViews.setTextViewText(
                    R.id.widget_balance,
                    getApplicationContext().getResources().getString(
                            R.string.rupee_symbol)
                            + " " + balance);

            long firstDate = mSharedPreferences.getLong("FIRST_DATE",
                    Long.parseLong("1420050600000"));

            int total_out_duration = mSharedPreferences.getInt(
                    "TOTAL_OUT_DURATION", 100);
            ////V10Log.d(TAG, "Toatl OUT Duration = " + total_out_duration);

            float call_rate = mSharedPreferences.getFloat("CALL_RATE",
                    (float) 1.7);
            ////V10Log.d(TAG, "CALL_RATE= " + call_rate);

            int numberOfDays = (int) ((new Date().getTime() - firstDate) / (1000 * 60 * 60 * 24));
            ////V10Log.d(TAG, "No of Days = " + numberOfDays);

            float total_cost_inPaise = total_out_duration * call_rate;
            ////V10Log.d(TAG, "Toatal cost = " + total_cost_inPaise);
            int predictedDays = (int) (currBalance * numberOfDays / (total_cost_inPaise / 100));
            ////V10Log.d(TAG, "predicted DAys = " + predictedDays);
            if (predictedDays == 0)
            {
                remoteViews.setTextViewText(R.id.widget_prediction,
                        "Your Balance will get over Today");
            } else
            {
                String readableDays = getReadableDays(predictedDays);
                String text = "Your Balance is predicted to getover in "
                        + readableDays + " Days";
                final SpannableStringBuilder sb = new SpannableStringBuilder(
                        text);

                final StyleSpan bss = new StyleSpan(
                        android.graphics.Typeface.BOLD); // Span to make text
                // bold
                sb.setSpan(bss, text.indexOf("in ") + 3, text.length(),
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make characters
                // Bold
                remoteViews.setTextViewText(R.id.widget_prediction, sb);
				/*
				 * remoteViews.setTextViewText(R.id.widget_prediction,
				 * "Keep Track of your Balance with iBalance");
				 */
            }
            mgr.updateAppWidget(widgetId, remoteViews);
            mgr.notifyAppWidgetViewDataChanged(widgetId,
                    R.layout.balance_widget_layout);
        }

    }

    private String getReadableDays(int predictedDays)
    {
        String ret;
        if (predictedDays < 30)
            return predictedDays + "";
        else
        {

            ret = predictedDays % 30 + "";
            ret = (predictedDays / 30) + "Months" + ret;
        }
        return ret;
    }

    @Override
    public void onInterrupt()
    {
       //V16//V16Log.d(TAG, "onInterrupt");
    }

    @Override
    protected void onServiceConnected()
    {
        super.onServiceConnected();

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.packageNames = new String[]{"com.android.phone"};
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
        Handler mHandler = new CallLogObserverHandler();
        mCallLogObserver = new CallLogObserver(mHandler);
        getContentResolver().registerContentObserver(
                android.provider.CallLog.Calls.CONTENT_URI, false,
                mCallLogObserver);
        ////V10Log.d(TAG, "onServiceConnected");
        if (ConstantsAndStatics.WAITING_FOR_SERVICE)
        {
            Tracker t = ((MyApplication) this.getApplication()).getTracker(
                    TrackerName.APP_TRACKER);
            t.send(new HitBuilders.EventBuilder()
                    .setCategory("SERVICE_STATUS")
                    .setAction("OFF")
                    .setLabel(Helper.getDeviceId())
                    .build());
            Map<String, String> params = new HashMap<String, String>();
            params.put("STATUS", "OFF");
            params.put("DEVICE_ID", Helper.getDeviceId());

            FlurryAgent.logEvent("SERVICE",params);
            TelephonyManager mtelTelephonyManager = (TelephonyManager) this
                    .getSystemService(Context.TELEPHONY_SERVICE);
            ParseQuery<ParseObject> query = ParseQuery
                    .getQuery("SERVICE_STATUS");
            query.whereEqualTo("DEVICE_ID", mtelTelephonyManager.getDeviceId());
            // Retrieve the object by Device id

            query.addDescendingOrder("createdAt");
            query.getFirstInBackground(new GetCallback<ParseObject>()
            {
                public void done(ParseObject service_status, ParseException e)
                {
                    if (e == null)
                    {
                        // Now let's update it pl
                       //V10Log.d(TAG, "Service On");
                        service_status.put("APP_VERSION", BuildConfig.VERSION_CODE);
                        service_status.put("SERVICE_STATUS", "ON");
                        service_status.increment("SERVICE_TOGGLE_COUNT");
                        service_status.saveEventually();

                    }
                }
            });

            ////V10Log.d(TAG, "OpeningMain Activity");
            ConstantsAndStatics.WAITING_FOR_SERVICE = false;
            Intent openApplication = new Intent(getApplicationContext(),
                    MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            openApplication.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            openApplication.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(openApplication);
        }
        // finish();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    private class CallLogObserverHandler extends Handler
    {

        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
           //V10Log.d(tag + " handleMessage", "What = " + msg.what + "  Contents" + msg.obj.toString());
            if (msg.what == 1729)
            {
                processCallEvent(msg);
                //displayPopUp();
            }

        }
    }
}
