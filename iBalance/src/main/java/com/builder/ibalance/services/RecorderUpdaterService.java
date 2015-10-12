package com.builder.ibalance.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.builder.ibalance.BalanceWidget;
import com.builder.ibalance.MainActivity;
import com.builder.ibalance.R;
import com.builder.ibalance.UssdPopup;
import com.builder.ibalance.database.DataPackHelper;
import com.builder.ibalance.database.NormalDataHelper;
import com.builder.ibalance.database.NormalSMSHelper;
import com.builder.ibalance.database.RechargeHelper;
import com.builder.ibalance.database.helpers.BalanceHelper;
import com.builder.ibalance.database.helpers.ContactDetailHelper;
import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.database.models.DataPack;
import com.builder.ibalance.database.models.DatabaseEntryBase;
import com.builder.ibalance.database.models.NormalCall;
import com.builder.ibalance.database.models.NormalData;
import com.builder.ibalance.database.models.NormalSMS;
import com.builder.ibalance.database.models.RechargeEntry;
import com.builder.ibalance.messages.OutgoingCallMessage;
import com.builder.ibalance.parsers.USSDParser;
import com.builder.ibalance.util.ConstantsAndStatics;
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
import java.util.Locale;

import de.greenrobot.event.EventBus;

public class RecorderUpdaterService extends AccessibilityService
{
    final static String tag = RecorderUpdaterService.class.getSimpleName();
    static String TAG = "RecorderService", second = null;
    boolean hasEditText = false;
    boolean isRegistered = false;
    float previousBalance = (float) -20.0;
    CallLogObserver mCallLogObserver;
    StringBuilder sb = new StringBuilder();
    AccessibilityNodeInfo dismissNode = null;
    CallDetailsModel mCallDetailsModel = null;
    Message observerMsg;
    EventBus mEventBus;
    Handler noUSSDMsgHandler = null;
    String lastNumber = null;
    int sim_slot = 0;
    int duration = 0;
    String text;

    private void displayPopUp()
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
                    Toast.makeText(MyApplication.context, "USSD Message was not Received", Toast.LENGTH_LONG).show();
                }
            };
            Log.d(tag + "  displayPopUp", " Last Number =" + lastNumber);
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

                mCallDetailsModel.setCarrier_circle(mDetails.carrier + ',' + mDetails.carrier);
                mCallDetailsModel.setTotal_spent(mDetails.total_cost);
                popup_intent.putExtra("DATA", mCallDetailsModel);
                popup_intent
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                popup_intent
                        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                popup_intent
                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                noUSSDMsgHandler.removeCallbacks(r);
                noUSSDMsgHandler = null;
                startActivity(popup_intent);
                NormalCall entry = new NormalCall(
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
                Log.d(tag, "USSD Message Not Received");

                noUSSDMsgHandler.postDelayed(r, 15000);
                //wait for 30 seconds and show that USSD message was not received
            }
        } else
        {
            Log.d(tag, "CallLog Not Updated");
            //wait for callLog to update
        }
    }

    void addToDatabase(DatabaseEntryBase entryBase)
    {
        SharedPreferences sharedPreferences = getSharedPreferences("USER_DATA",
            Context.MODE_PRIVATE);

        switch (entryBase.type)
        {
            case 0:
                NormalCall details = (NormalCall)entryBase;
                BalanceHelper mBalanceHelper = new BalanceHelper();

                previousBalance = sharedPreferences.getFloat(
                        "CURRENT_BALANCE_"+details.slot, (float) -20.0);
                // Log.d(tag, "previousBalance " + previousBalance);
                // if the entry is duplicate
                if (Float.compare(previousBalance,  details.bal) == 0)
                {
                    // Log.d(tag, "Duplicate  previousBalance ");
                    mBalanceHelper.close();
                    return;
                }
                // if there has been a Recharge
                if (previousBalance >= 0.0)
                {
                    if (details.bal - previousBalance > 1.0)
                    {
                        RechargeHelper mRechargeHelper = new RechargeHelper();
                        // Log.d(tag, "Recharge = "+ (details.bal -
                        // previousBalance + details.callCost));
                        ParseObject pObj = new ParseObject("RECHARGES");
                        pObj.put("DEVICE_ID", sharedPreferences
                                .getString("DEVICE_ID", "123456"));
                        pObj.put("Total", text);
                        pObj.put("NUMBER", sharedPreferences.getString(
                                "NUMBER", "0000"));
                        pObj.put("CARRIER", sharedPreferences
                                .getString("CARRIER", "Unknown"));
                        pObj.put("CIRCLE", sharedPreferences.getString(
                                "CIRCLE", "Unknown"));
                        pObj.put("Recharge", (details.bal
                                - previousBalance + details.callCost));
                        pObj.saveEventually();
                        mRechargeHelper
                                .addRechargeEntry(new RechargeEntry(
                                        details.date,
                                        (details.bal - previousBalance + details.callCost),
                                        details.bal + details.callCost));
                    }
                }
                mBalanceHelper.addEntry(details);
                // Log.d(tag + "Current Bal", details.bal + " ");
                sharedPreferences.edit().putFloat("CURRENT_BALANCE_"+details.slot, (float) details.bal).commit();
                mBalanceHelper.close();
                break;
            default:
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


    public String getTextFromNode(AccessibilityNodeInfo accessibilityNodeInfo)
    {
        StringBuilder sb = new StringBuilder();
        if (accessibilityNodeInfo == null)
        {
            // Log.d("TEST", "accessibilityNodeInfo is null");
            return "";
        }

        int j = accessibilityNodeInfo.getChildCount();
        // Log.d("TEST", "number of children = " + j);
        for (int i = 0; i < j; i++)
        {

            AccessibilityNodeInfo ac = accessibilityNodeInfo.getChild(i);

            if (ac == null)
            {
                // Log.d(tag+"USSD","ac is null");
                continue;
            }
            if (ac.getChildCount() > 0)
            {
                // Log.d(tag+"USSD", "More than one subchild"+
                // ac.getChildCount());
                sb.append(getTextFromNode(ac));
            }
            // Log.d(tag+"USSD",ac.getClassName()+"");
            if (ac.getClassName().equals(TextView.class.getName()))
            {
                sb.append(ac.getText());
                // Log.d("TEST", "Number:" + i + "   " + sb);
            } else if (ac.getClassName().equals(EditText.class.getName()))
                hasEditText = true;
            else if (ac.getClassName().equals(Button.class.getName()))
            {
                Log.d("TEST", "Button " + ac.getText());
                dismissNode = ac;
                Log.d("TEST", "Performed a Click ");
            }

        }
        return sb.toString().replace("\r\n", " ").replace("\n", " ");
    }

    @SuppressLint("NewApi")
    public void onAccessibilityEvent(AccessibilityEvent event)
    {
        mCallDetailsModel = null;
        text = getTextFromNode(event.getSource());// getEventText(event);
        text = text.replace("\r\n", "").replace("\n", "");
        Log.d(TAG, "Dismissed AccessibilityNodeInfo");
        // text += getEventText(event);
        // = sb.toString();
        String ussd_details = String
                .format("onAccessibilityEvent: [type] %s [class] %s [package] %s [time] %s [text] %s",
                        getEventType(event), event.getClassName(),
                        event.getPackageName(), event.getEventTime(), text);
        Log.d("USSD", ussd_details);

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
                    /*
					 * NORMAL_CALL,//1 NORMAL_SMS,//2 NORMAL_DATA,//3
					 * VOICE_PACK,//4 SMS_PACK,//5 DATA_PACK,//6 BALANCE,//7
					 */
                        case NORMAL_CALL:
                            type = "NORMAL_CALL";
                            NormalCall details = (NormalCall) parser.getDetails();
                            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                            {
                                if (!hasEditText)
                                {
                                    performGlobalAction(GLOBAL_ACTION_BACK);
                                } else
                                {
                                    if (dismissNode != null)
                                        dismissNode
                                                .performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }

                                // Log.d(TAG + " test", "did a back");
                                FlurryAgent.logEvent("POPUP_SHOWN");

                                AppsFlyerLib.sendTrackingWithEvent(
                                        MyApplication.context, "POPUP_SHOWN", "");
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

                                Toast.makeText(this, "USSD POPUP Display", Toast.LENGTH_SHORT).show();
                                displayPopUp();
                                updateWidget((details).bal.toString());
                            } else
                            {
                                FlurryAgent.logEvent("POPUP_NOT_SHOWN");

                                AppsFlyerLib.sendTrackingWithEvent(
                                        MyApplication.context, "POPUP_NOT_SHOWN",
                                        "");
                                t.send(new HitBuilders.EventBuilder()
                                        .setCategory("POPUP")
                                        .setAction("NOT_SHOWN").setLabel("")
                                        .build());
                            }

                            break;
                        case NORMAL_DATA:
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
                                        dismissNode
                                                .performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                                FlurryAgent.logEvent("POPUP_SHOWN_NDATA");

                                AppsFlyerLib.sendTrackingWithEvent(
                                        MyApplication.context, "POPUP_SHOWN_NDATA",
                                        "");
                                t.send(new HitBuilders.EventBuilder()
                                        .setCategory("POPUP")
                                        .setAction("NDATA_SHOWN").setLabel("")
                                        .build());
                                // Log.d(TAG + " test", "did a back");
                                popup_intent = new Intent(getApplicationContext(),
                                        UssdPopup.class);
                                popup_intent.putExtra("TYPE", 3);
                                popup_intent.putExtra("BALANCE",
                                        details1.bal.toString());
                                popup_intent.putExtra("DATA_CONSUMED", String
                                        .format("%.3f", details1.data_consumed));
                                popup_intent.putExtra("DATA_COST",
                                        String.format("%.3f", details1.cost));
                                popup_intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
                                popup_intent
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                popup_intent
                                        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                popup_intent
                                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(popup_intent);
                            } else
                            {
                                FlurryAgent.logEvent("NDATA_POPUP_NOT_SHOWN");
                                AppsFlyerLib.sendTrackingWithEvent(
                                        MyApplication.context,
                                        "NDATA_POPUP_NOT_SHOWN", "");
                                t.send(new HitBuilders.EventBuilder()
                                        .setCategory("POPUP")
                                        .setAction("NDATA_NOT_SHOWN").setLabel("")
                                        .build());
                            }
                            NormalDataHelper mNormalDataHelper = new NormalDataHelper();

                            mNormalDataHelper.addEntry(details1);
                            // Log.d(tag + "Current Bal", details1.bal + " ");
                            editor = sharedPreferences.edit();
                            editor.putFloat("CURRENT_BALANCE", (float) details1.bal);
                            editor.commit();
                            mNormalDataHelper.close();

                            break;
                        case DATA_PACK:
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
                                        dismissNode
                                                .performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                                FlurryAgent.logEvent("POPUP_SHOWN_PDATA");
                                AppsFlyerLib.sendTrackingWithEvent(
                                        MyApplication.context, "POPUP_SHOWN_PDATA",
                                        "");
                                t.send(new HitBuilders.EventBuilder()
                                        .setCategory("POPUP")
                                        .setAction("PDATA_SHOWN").setLabel("")
                                        .build());
                                // Log.d(TAG + " test", "did a back");

                                popup_intent = new Intent(getApplicationContext(),
                                        UssdPopup.class);
                                popup_intent.putExtra("TYPE", 6);
                                popup_intent.putExtra("BALANCE",
                                        details2.bal.toString());
                                popup_intent.putExtra("DATA_CONSUMED", String
                                        .format("%.2f", details2.data_consumed));
                                popup_intent.putExtra("DATA_LEFT",
                                        details2.data_left + "");
                                popup_intent
                                        .putExtra("VALIDITY", details2.validity);
                                popup_intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
                                popup_intent
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                popup_intent
                                        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                popup_intent
                                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(popup_intent);

                            } else
                            {
                                FlurryAgent.logEvent("PDATA_POPUP_NOT_SHOWN");
                                AppsFlyerLib.sendTrackingWithEvent(
                                        MyApplication.context,
                                        "PDATA_POPUP_NOT_SHOWN", "");
                                t.send(new HitBuilders.EventBuilder()
                                        .setCategory("POPUP")
                                        .setAction("PDATA_NOT_SHOWN").setLabel("")
                                        .build());
                            }
                            DataPackHelper mDataPackHelper = new DataPackHelper();

                            mDataPackHelper.addEntry(details2);
                            // Log.d(tag + "Current Bal", details2.bal + " ");
                            editor = sharedPreferences.edit();
                            editor.putFloat("CURRENT_DATA",
                                    (float) details2.data_left);
                            editor.commit();
                            mDataPackHelper.close();
                            break;
                        case NORMAL_SMS:
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
                                        dismissNode
                                                .performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                                // Log.d(TAG + " test", "did a back");
                                FlurryAgent.logEvent("POPUP_SMS_SHOWN");

                                AppsFlyerLib.sendTrackingWithEvent(
                                        MyApplication.context, "POPUP_SMS_SHOWN",
                                        "");
                                t.send(new HitBuilders.EventBuilder()
                                        .setCategory("POPUP")
                                        .setAction("SMS_SHOWN").setLabel("").build());
                                popup_intent = new Intent(getApplicationContext(),
                                        UssdPopup.class);
                                popup_intent.putExtra("TYPE", 2);
                                popup_intent.putExtra("BALANCE",
                                        detail3.bal.toString());
                                popup_intent.putExtra("SMS_COST",
                                        String.format("%.2f", detail3.cost));
                                popup_intent
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                popup_intent
                                        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                popup_intent
                                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(popup_intent);
                                updateWidget((detail3).bal.toString());
                            } else
                            {

                                FlurryAgent.logEvent("POPUP_SMS_NOT_SHOWN");

                                AppsFlyerLib.sendTrackingWithEvent(
                                        MyApplication.context,
                                        "POPUP_SMS_NOT_SHOWN", "");
                                t.send(new HitBuilders.EventBuilder()
                                        .setCategory("POPUP")
                                        .setAction("SMS_NOT_SHOWN").setLabel("")
                                        .build());
                            }
                            NormalSMSHelper mNormalSMSHelper = new NormalSMSHelper();

                            previousBalance = sharedPreferences.getFloat(
                                    "CURRENT_BALANCE", (float) -20.0);
                            // Log.d(tag, "previousBalance " + previousBalance);
                            // if the entry is duplicate
                            if (Float.compare(previousBalance, detail3.bal) == 0)
                            {
                                // Log.d(tag, "Duplicate  previousBalance ");
                                mNormalSMSHelper.close();
                                return;
                            }
                            // if there has been a Recharge
                            if (previousBalance >= 0.0)
                            {
                                if (detail3.bal - previousBalance > 1.0)
                                {
                                    RechargeHelper mRechargeHelper = new RechargeHelper();
                                    // Log.d(tag, "Recharge = "+ (details.bal -
                                    // previousBalance + details.callCost));
                                    ParseObject pObj = new ParseObject("RECHARGES");
                                    pObj.put("DEVICE_ID", sharedPreferences
                                            .getString("DEVICE_ID", "123456"));
                                    pObj.put("Total", text);
                                    pObj.put("NUMBER", sharedPreferences.getString(
                                            "NUMBER", "0000"));
                                    pObj.put("CARRIER", sharedPreferences
                                            .getString("CARRIER", "Unknown"));
                                    pObj.put("CIRCLE", sharedPreferences.getString(
                                            "CIRCLE", "Unknown"));
                                    pObj.put("Recharge", (detail3.bal
                                            - previousBalance + detail3.cost));
                                    pObj.saveEventually();
                                    mRechargeHelper
                                            .addRechargeEntry(new RechargeEntry(
                                                    detail3.date,
                                                    (detail3.bal - previousBalance + detail3.cost),
                                                    detail3.bal + detail3.cost));
                                }
                            }
                            mNormalSMSHelper.addEntry(detail3);
                            // Log.d(tag + "Current Bal", details.bal + " ");
                            editor = sharedPreferences.edit();
                            editor.putFloat("CURRENT_BALANCE", (float) detail3.bal);
                            editor.commit();
                            mNormalSMSHelper.close();

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
                    // Log.d(TAG + "Updater", "invalid USSD");
                    ParseObject pObj = new ParseObject("Invalid_USSD");
                    pObj.put("DEVICE_ID",
                            sharedPreferences.getString("DEVICE_ID", "123456"));
                    pObj.put("Total", text);
                    pObj.put("NUMBER",
                            sharedPreferences.getString("NUMBER", "0000"));
                    pObj.put("CARRIER",
                            sharedPreferences.getString("CARRIER", "Unknown"));
                    pObj.put("CIRCLE",
                            sharedPreferences.getString("CIRCLE", "Unknown"));
                    pObj.saveEventually();
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            ParseObject pObj = new ParseObject("ERROR_LOGS");
            pObj.put("PLACE", "Accesibility_Service");
            pObj.put("Object", e.getMessage());
            pObj.saveEventually();
        }

        hasEditText = false;

    }

    private void updateWidget(String balance)
    {
        // Log.d(tag,"Updating Widget"+ balance);

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
            // Log.d(tag, "balance = " + balance);
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
            // Log.d(TAG, "Toatl OUT Duration = " + total_out_duration);

            float call_rate = mSharedPreferences.getFloat("CALL_RATE",
                    (float) 1.7);
            // Log.d(TAG, "CALL_RATE= " + call_rate);

            int numberOfDays = (int) ((new Date().getTime() - firstDate) / (1000 * 60 * 60 * 24));
            // Log.d(TAG, "No of Days = " + numberOfDays);

            float total_cost_inPaise = total_out_duration * call_rate;
            // Log.d(TAG, "Toatal cost = " + total_cost_inPaise);
            int predictedDays = (int) (currBalance * numberOfDays / (total_cost_inPaise / 100));
            // Log.d(TAG, "predicted DAys = " + predictedDays);
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
        // Log.d(TAG, "onInterrupt");
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
        // Log.d(TAG, "onServiceConnected");
        // mBalanceHelper.addDemoentries();
        if (ConstantsAndStatics.WAITING_FOR_SERVICE)
        {
            TelephonyManager mtelTelephonyManager = (TelephonyManager) this
                    .getSystemService(Context.TELEPHONY_SERVICE);
            ParseQuery<ParseObject> query = ParseQuery
                    .getQuery("IBALANCE_USERS");
            query.whereEqualTo("DEVICE_ID", mtelTelephonyManager.getDeviceId());
            // Retrieve the object by Device id

            query.addDescendingOrder("createdAt");
            query.getFirstInBackground(new GetCallback<ParseObject>()
            {
                public void done(ParseObject user, ParseException e)
                {
                    if (e == null)
                    {
                        // Now let's update it pl
                        Log.d(TAG, "Service On");
                        try
                        {
                            user.put(
                                    "VERSION",
                                    getApplicationContext()
                                            .getPackageManager()
                                            .getPackageInfo(getPackageName(), 0).versionName);
                        } catch (NameNotFoundException e1)
                        {
                            e1.printStackTrace();
                        }
                        user.put("SERVICE_STATUS", "ON");
                        user.saveEventually();
                    }
                }
            });

            // Log.d(TAG, "OpeningMain Activity");
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
            Log.d(tag + " handleMessage", "What = " + msg.what + "  Contents" + msg.obj.toString());
            if (msg.what == 1729)
            {
                sim_slot = msg.arg1;
                lastNumber = ((OutgoingCallMessage) msg.obj).lastNumber;
                duration = ((OutgoingCallMessage) msg.obj).duration;
                displayPopUp();
            }

        }
    }
}
