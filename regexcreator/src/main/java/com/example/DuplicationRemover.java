package com.example;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.Choice.ptln;

/**
 * Created by Shabaz on 14-Nov-15.
 */
public class DuplicationRemover
{
    File call_json = new File("G:/SimplyV2/TextMining/json/CALL_PATTERN.json");
    String dir = "G:/SimplyV2/TextMining/unique_pat/from_sarvesh/data pack/";
    File call_messages = new File("G:/SimplyV2/TextMining/unique_pat/from_sarvesh/data pack/tata docomo.txt");
    File unmatched_call_messages = new File("G:/SimplyV2/TextMining/unique_pat/from_sarvesh/data pack/tata docomo_unmatched.txt");
    static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    FileInputStream mFileReader ;
    byte[] data;
    int cont =0,unmatched_count= 0;

    StringBuilder unmatchedMessages = new StringBuilder();
    StringBuilder finalReport = new StringBuilder();
    public DuplicationRemover()
    {
        FileOutputStream mWriter;
        try
        {
            mFileReader = new FileInputStream(call_json);


            data = new byte[(int) call_json.length()];
            mFileReader.read(data);
            mFileReader.close();
            String str = new String(data, "UTF-8");/*
            JSONObject assetJson = new JSONObject(str);
            ptln("NORMAL_CALL = "+assetJson.getString("NORMAL_CALL"));
            ptln("PACK_CALL = "+assetJson.getString("PACK_CALL"));
            ptln("NORMAL_DATA = "+assetJson.getString("NORMAL_DATA"));
            ptln("PACK_DATA = "+assetJson.getString("PACK_DATA"));
            ptln("NORMAL_SMS = "+assetJson.getString("NORMAL_SMS"));
            ptln("PACK_SMS = "+assetJson.getString("PACK_SMS"));
            ptln("VOICE_DATA = "+assetJson.getString("VOICE_DATA"));*/
            JSONArray mJsonArray = null, callJson;
            mJsonArray = new JSONArray(str);
            File folder = new File(dir);
            for (final File fileEntry : folder.listFiles())
            {
                cont =0;
                unmatched_count= 0;

                if (fileEntry.isDirectory())
                    continue;
                unmatchedMessages = new StringBuilder();
                ptln("------------------"+fileEntry.getName()+"-------------------------------------------");
                finalReport.append("\n\n"+fileEntry.getName()+":");
                call_messages = fileEntry;
                mFileReader = new FileInputStream(call_messages);
                data = new byte[(int) call_messages.length()];
                mFileReader.read(data);
                mFileReader.close();
                str = new String(data, "UTF-8");
                callJson = new JSONArray(str);
                Pattern mPattern;
                Matcher mMatcher;
                int count = 0;
                int regexLength = mJsonArray.length();
                int messagesLenth = callJson.length();
                int found = 0;
                for (int i = 0; i < messagesLenth; i++)
                {

                    String message = ((JSONObject) callJson.get(i)).getString("TOTAL");
                    found = 0;
                    try
                    {

                        for (int j = 0; j < regexLength; j++)
                        {
                            JSONObject currentObject = (JSONObject) mJsonArray.get(j);
                            String regex = currentObject.getString("REGEX");
                            mPattern = Pattern.compile(regex);
                            mMatcher = mPattern.matcher(message);
                            if (mMatcher.find())
                            {

                                found++;
                                if (found == 1)
                                {
                                    count++;
                                }
                                ptln("Matched! " + found + " Times");
                                ptln("Message: " + message);
                                ptln("Regex: " + regex);
                                //callSearch(mMatcher);
                                datSearch(mMatcher);
                            }
                        }
                        if (found == 0)
                        {
                            unmatched_count++;
                            unmatchedMessages.append(message + "\n\n");
                        }
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
                ptln("Total Messages = " + messagesLenth);
                finalReport.append("\nTotal Messages = " + messagesLenth);
                ptln("Parsed Messages = " + count);
                finalReport.append("\nParsed Messages = " + count);
                ptln("Not Parsed Messages = " + unmatched_count);
                finalReport.append("\nNot Parsed Messages = " + unmatched_count);
                ptln("---------------------------------------------------------------------------------------------------------------------------------");
                finalReport.append("\n---------------------------------------------------------------------------------------------------------------------------------");
                try
                {
                    unmatched_call_messages = new File(dir+call_messages.getName()+"_unmatched.json");
                    mWriter = new FileOutputStream(unmatched_call_messages);
                    mWriter.write(unmatchedMessages.toString().getBytes());
                    mWriter.flush();
                    mWriter.close();
                } catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            ptln(finalReport.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {


        }

    }

    private void datSearch(Matcher mMatcher)
    {
      /*type 			- TYPE [types (3G,2G,-1,GPRS) optional (will be empty)or(Exception might be thrown assume 2G) ]
        data used 		- DUSED mandatory
        data used Metric- DUSEDM[B KB MB GB  (KB,MB,K,empty (assume K)) and optional]
        data left		- DLEFT mandatory
        data used Metric- DLEFTM [B KB MB GB  optional (KB,MB,K,empty (assume K))]
        validity 		- VAL [ optional (will be empty)or(exception thrown)]
        balance 		- MBAL ? (optional -1)]*/

        try{
            ptln("TYPE = " + mMatcher.group("TYPE"));
        }
        catch (IllegalArgumentException e)
        {
            ptln("TYPE Does not Exist");
        }
        try{

            ptln("Data Used = " + mMatcher.group("DUSED"));
        }
        catch (IllegalArgumentException e)
        {
            try
            {
                String mbString = null;
                Float mb = null;
                try
                {

                    mbString = mMatcher.group("DMBUSED");
                }
                catch (IllegalArgumentException e3)
                {
                    ptln("DATA USED MB Does not Exist");
                }
                if(mbString==null)
                {
                    mb = 0.0f;
                }
                else
                {
                    mb = Float.parseFloat(mbString);
                }
                Float kb = Float.parseFloat(mMatcher.group("DKBUSED"));
                mb+=(kb/1000);
                ptln("Data USED = "+mb+" MB");
            }
            catch (IllegalArgumentException e1)
            {
                e1.printStackTrace();
                ptln("DATA USED Does not Exist");
            }
        }
        try{
            ptln("DATA Used Metric = " + mMatcher.group("DUSEDM"));
        }
        catch (IllegalArgumentException e)
        {
            ptln("DATA Used Metric Does not Exist");
        }
        try
        {
            ptln("Data Left = " + mMatcher.group("DLEFT"));
        }
        catch (IllegalArgumentException e)
        {
            try
            {
                String mbString = null;
                Float mb = null;
                try
                {

                    mbString = mMatcher.group("DMBLEFT");
                }
                catch (IllegalArgumentException e3)
                {
                    ptln("DATA USED MB Does not Exist");
                }
                if(mbString==null)
                {
                    mb = 0.0f;
                }
                else
                {
                    mb = Float.parseFloat(mbString);
                }
                Float kb = Float.parseFloat(mMatcher.group("DKBLEFT"));
                mb+=(kb/1000);
                ptln("Data Left = "+mb+" MB");
            }
            catch (IllegalArgumentException e1)
            {
                e1.printStackTrace();
                ptln("DATA Left Does not Exist");
            }
        }

        try{
            ptln("DATA Left Metric = " + mMatcher.group("DLEFTM"));
        }
        catch (IllegalArgumentException e)
        {
            ptln("DATA Left Metric Does not Exist");
        }
        try{
            ptln("Vaidity = " + mMatcher.group("VAL"));
        }
        catch (IllegalArgumentException e)
        {
            ptln("Vaidity Does not Exist");
        }
        try{

            ptln("Balance = " + mMatcher.group("MBAL"));
        }
        catch (IllegalArgumentException e)
        {
            ptln("Balance Does not Exist");

        }


        ptln("-------------------------------------------------------------------------------------------------------------");
    }

    private void callSearch(Matcher mMatcher)
    {

        ptln("Cost = " + mMatcher.group("COST"));
        ptln("Balance = " + mMatcher.group("MBAL"));
        String duration = null;
        try
        {
            duration = mMatcher.group("DURS");
            ptln("Duration Secs = " + duration);
        }catch (IllegalArgumentException e0)
        {
            try{
                duration = mMatcher.group("DURC");
                ptln("Duration Clock = " + duration);
            }
            catch (IllegalArgumentException e1)
            {
                try
                {
                    duration = mMatcher.group("DURMIN") + ":" + mMatcher.group("DURSEC");
                    ptln("Duration Min:Secs = " + duration);
                }
                catch (IllegalArgumentException e2)
                {
                    ptln("No Duration Found A-Hole Telecos");
                }
            }
        }


        ptln("-------------------------------------------------------------------------------------------------------------");

    }


}
