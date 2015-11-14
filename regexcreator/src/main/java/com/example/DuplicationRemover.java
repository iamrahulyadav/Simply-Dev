package com.example;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.Choice.ptln;

/**
 * Created by Shabaz on 14-Nov-15.
 */
public class DuplicationRemover
{
    File call_json = new File("G:/SimplyV2/TextMining/json/CALL_PATTERNS.json");
    static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    FileInputStream mFileReader ;
    byte[] data;
    public DuplicationRemover()
    {
        FileInputStream mReader;
        try
        {
            mFileReader = new FileInputStream(call_json);


            data = new byte[(int) call_json.length()];
            mFileReader.read(data);
            mFileReader.close();

            String str = new String(data, "UTF-8");
            JSONArray mJsonArray = null;
            mJsonArray = new JSONArray(str);
            Pattern mPattern;
            Matcher mMatcher;
            String testString = reader.readLine();
            int len = mJsonArray.length();
            for(int i=0;i<len;i++)
            {
                JSONObject currentObject = (JSONObject) mJsonArray.get(i);
                mPattern = Pattern.compile(currentObject.getString("REGEX"));
                mMatcher = mPattern.matcher(testString);
                if(mMatcher.find())
                {
                    ptln("Cost = "+mMatcher.group(currentObject.getInt("COST_POS")));
                    ptln("Balance = "+mMatcher.group(currentObject.getInt("MAIN_BAL_POS")));
                    try
                    {
                        ptln("Cost = "+mMatcher.group(currentObject.getInt("DURATION_SEC_POS")));
                    }
                    catch (JSONException e)
                    {
                        JSONArray durationArray = currentObject.getJSONArray("DURATION_CLOCK");
                        int hh = durationArray.getInt(0);
                        int mm = durationArray.getInt(1);
                        int ss = durationArray.getInt(2);
                        if(hh!=-1)
                        ptln("Duration HH = "+mMatcher.group(hh));
                        if(mm!=-1)
                        ptln("Duration MM = "+mMatcher.group(mm));
                        if(ss!=-1)
                        ptln("Duration SS = "+mMatcher.group(ss));
                    }


                }

            }
            mFileReader.close();
        }catch (Exception e)
        {

        }
    }

}
