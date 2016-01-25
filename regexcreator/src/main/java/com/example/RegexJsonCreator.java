package com.example;

import com.google.code.regexp.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.example.Choice.ptln;

/**
 * Created by Shabaz on 14-Nov-15.
 */
public class RegexJsonCreator
{
     BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String regex;
    JSONArray allJson = null;
    String TYPE = "SMS_PACK";
    //File regex_json = new File("G:/SimplyV2/TextMining/json/V2/NORMAL_CALL_PATTERNS.json");
    //File regex_json = new File("G:/SimplyV2/TextMining/json/V2/NORMAL_DATA_PATTERNS.json");
    //File regex_json = new File("G:/SimplyV2/TextMining/json/V2/NORMAL_SMS_PATTERNS.json");
    //File regex_json = new File("G:/SimplyV2/TextMining/json/V2/PACK_CALL_PATTERNS.json");
    //File regex_json = new File("G:/SimplyV2/TextMining/json/V2/PACK_DATA_PATTERNS.json");
    File regex_json = new File("G:/SimplyV2/TextMining/json/V2/PACK_SMS_PATTERNS.json");
    //File regex_json = new File("G:/SimplyV2/TextMining/json/V2/MAIN_BALANCE_PATTERNS.json");
    int costPos = -1, durationSecPos = -1,mainBalPos = -1,durationClockPosHH = -1,durationClockPosMM = -1,durationClockPosSS = -1;
    int usedData = -1, leftData = -1,type = -1,usedDataMetric = -1,leftDataMetric = -1,validity = -1,mainbalance = -1;
    int cont = -1;
    FileInputStream mFileReader ;
    FileOutputStream mFileWriter;
    byte[] data;
    public RegexJsonCreator()
    {
        try
        {
            if(!regex_json.exists())
            {
                regex_json.createNewFile();
                mFileWriter = new FileOutputStream(regex_json);
                mFileWriter.write("[]".getBytes());
                mFileWriter.flush();
                mFileWriter.close();
            }

            mFileReader = new FileInputStream(regex_json);


            data = new byte[(int) regex_json.length()];
            mFileReader.read(data);
            mFileReader.close();

            String str = new String(data, "UTF-8");
            JSONArray mJsonArray = null;
                mJsonArray = new JSONArray(str);
            int id = 0,version=2;
            while (true)
            {
                //regex
                //cost
                //durationSec -1 if not applicable
                //mainBal
                //d HH
                //d MM
                //d SS
                ptln("Current id = "+(id+1));
                ptln("Enter the regex for call -1 to Stop OR 0 to undo");
                regex = reader.readLine();
                if(regex.equals("-1"))
                    break;
                else if(regex.equals("0"))
                {
                    mJsonArray.remove(--id);
                    continue;
                }
                //JSONObject entry = getnewEntryforData(regex);
                JSONObject entry = getnewEntryforSMSPack(regex);

                //entry.put("CARRIER",carrier);
                entry.put("id",id++);
                entry.put("version",version);
                //entry.put("type",TYPE);
               /*
                Call Entry
                ptln("Enter the regex for call");
                regex = reader.readLine();
                if(regex.equals("-1"))
                    break;
                ptln("Enter cost Position");
                costPos = Integer.parseInt(reader.readLine());
                ptln("Enter Duration Seconds position");
                durationSecPos = Integer.parseInt(reader.readLine());
                ptln("Enter main Bal Pos");
                mainBalPos = Integer.parseInt(reader.readLine());
                if (durationSecPos == -1)
                {
                    ptln("Enter Duration clock starting pos ( HH )");
                    durationClockPosHH = Integer.parseInt(reader.readLine());
                    durationClockPosMM = Integer.parseInt(reader.readLine());
                    durationClockPosSS = Integer.parseInt(reader.readLine());
                }
                ptln("Enter -1 to stop or any number to continue");
                JSONObject entry = new JSONObject();
                entry.put("REGEX",regex);
                entry.put("CARRIER",carrier);
                entry.put("COST_POS",costPos);
                entry.put("MAIN_BAL_POS", mainBalPos);
                if(durationSecPos !=-1)
                {
                    entry.put("DURATION_SEC_POS",durationSecPos);
                }
                else
                {
                    JSONArray durationClockArray = new JSONArray() ;
                    durationClockArray.put(0,durationClockPosHH);
                    durationClockArray.put(1,durationClockPosMM);
                    durationClockArray.put(2,durationClockPosSS);
                    entry.put("DURATION_CLOCK",durationClockArray);
                }*/

                mJsonArray.put(entry);

            }
            testJson(mJsonArray);
            mFileWriter = new FileOutputStream(regex_json);
            mFileWriter.write(mJsonArray.toString().getBytes());
            mFileWriter.flush();

        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (JSONException e)
        {
            e.printStackTrace();

        }
        finally
        {
            try
            {
                mFileWriter.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }


    }

    private void testJson(JSONArray regex_array)
    {
        for (int i = 0; i < regex_array.length(); i++)
        {
            try
            {
                regex = regex_array.getJSONObject(i).getString("REGEX");
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
            //Log.d(TAG, "Trying with : " + regex);
            try
            {
                Pattern mPattern = Pattern.compile(regex);
            }
            catch (Exception e)
            {
                ptln("Failed "+i+1);
            }
        }
        ptln("Finished Checking");
    }

    private JSONObject getnewEntryforSMSPack(String regex) throws JSONException
    {
        JSONObject entry = new JSONObject();
        entry.put("REGEX",regex);
        return entry;
    }

    private JSONObject getnewEntryforCall(String regex) throws JSONException
    {
        JSONObject entry = new JSONObject();
        entry.put("REGEX",regex);
        return entry;
    }

    public JSONObject getnewEntryforData(String regex) throws IOException,JSONException
    {
        JSONObject entry = new JSONObject();
        /*
                Data
                Notes
                type types (3G,2G,-1,GPRS) (will be empty)or(Exception might be thrown assume 2G)
                data used
                data used Metric B KB MB GB  (KB,MB,K,empty (assume K))
                data left
                data used Metric B KB MB GB  (KB,MB,K,empty (assume K))
                validity (will be empty)or(exception thrown)
                balance ? (optional -1)*/
        entry.put("REGEX",regex);
        /*ptln("Enter TypeIndex");
        type = Integer.parseInt(reader.readLine());
        entry.put("TYPE_POS",type);

        ptln("Enter usedData Index");
        usedData =Integer.parseInt(reader.readLine());
        entry.put("USED_POS",usedData);

        ptln("Enter usedDataMetric");
        usedDataMetric = Integer.parseInt(reader.readLine());
        entry.put("USED_MET_POS",usedDataMetric);

        ptln("Enter leftData");
        leftData = Integer.parseInt(reader.readLine());
        entry.put("LEFT_POS",leftData);

        ptln("Enter leftDataMetric");
        leftDataMetric = Integer.parseInt(reader.readLine());
        entry.put("LEFT_MET_POS",leftDataMetric);

        ptln("Enter validity");
        validity = Integer.parseInt(reader.readLine());
        entry.put("VALIDITY",validity);

        ptln("Enter mainbalance");
        mainbalance  = Integer.parseInt(reader.readLine());
        entry.put("MAIN_BAL",mainbalance);*/

        return entry;
    }
}
