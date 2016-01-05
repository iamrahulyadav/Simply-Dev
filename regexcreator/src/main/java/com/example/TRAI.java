package com.example;

import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.example.Choice.ptln;

/**
 * Created by Shabaz on 05-Jan-16.
 */
public class TRAI
{
    File file = new File("G:/SimplyV2/TextMining/Bhayyaji/VALID_USSD_Sorted.json");
    FileOutputStream fout = null;
    String temp;
    public TRAI() throws IOException, JSONException
    {


        fout = new FileOutputStream("G:/SimplyV2/TextMining/Bhayyaji/CASE_STUDY.csv");
        FileInputStream fileStram;
        byte[] data = null;
        try
        {
            fileStram = new FileInputStream(file);

            data = new byte[(int) file.length()];
            fileStram.read(data);
            fileStram.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        String str = new String(data, "UTF-8");
        JSONArray jArray = null;
        try
        {
            //str = str.replaceAll("\\n\\r*","");
            jArray = new JSONArray(str);
        } catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        fout.write("DATE,MAIN_BAL,COST,DURATION,MESSAGE".getBytes());
        fout.flush();
        for(int i=0;i<jArray.length();i++)
        {
            if(normalCall((jArray.getJSONObject(i)).getString("MESSAGE").toUpperCase()))
            {
                fout.write(((jArray.getJSONObject(i)).getString("createdAt")+","+temp+","+((jArray.getJSONObject(i)).getString("MESSAGE"))+"\n").getBytes());
            }

        }
        fout.flush();
        fout.close();
        //sortJsonArray();

    }
    private boolean packCall(String message) throws JSONException
    {
        //Log.d(TAG, "Trying Pack Call");
        Matcher result = findDetails(getPackCallRegex(), message);
        if (result != null)
        {
            ptln(message);
            /*
            TYPE : optional Pack Type LOCALA2A loc+nat etc etc
            CUSED : Call Pack Used duration
            CUSEDM: Call Pack  Used Metric SEC?|MINS?
            CLEFT : Call Pack duration left used the CUSED metric to infer sec or mins
            MBAL : mainBal
            PBUSED:(opt) Pack balance used if pack is in the form of amount
            PBAL: (opt)Pack balance left if pack is in the form of amount
            //Skipping cost COST :(opt) call cost in case pack was just a rate cutter
            CUMIN : (opt) Call Pack Used Min
            CUSEC : (opt) Call Pack Used Sec
            CLHR : (opt) Call Pack Left Hour
            CLMIN : (opt) Call Pack Left Min
            CLSEC : (opt) Call Pack Left Sec
            DURC : (opt)Actual Call Duration clock 00:00:00
            DURS : (opt)Actual Call Duration only secs
            DURHR : (opt)Actual Call Duration HR
            DURMIN :(opt) Actual Call Duration MIN
            DURSEC :(opt) Actual Call Duration SEC
            VAL : Validity
            */
            /*
            Story Time:
            There are two types of packs one giving free mins/secs and other giving a special balance with expiry data
            both are mutually exclusive
            */
            float mainBal,packBal=-1.0f,packCost=-1.0f;
            int usedSecs,leftSecs,durSec;
            String usedMetric = "SECS",leftMetric=null,validity,type;
            boolean freeMinType = false;
            try
            {
                usedSecs = Integer.parseInt(result.group("CUSED"));

                freeMinType = true;
            }
            catch (RuntimeException e)
            {
                int min =0 ,sec = 0;
                int err = 0;
                try
                {
                    min = Integer.parseInt(result.group("CUMIN"));
                    freeMinType = true;

                }
                catch (RuntimeException e1)
                {
                    min = 0;
                    err--;
                }
                try
                {
                    sec = Integer.parseInt(result.group("CUSEC"));
                    freeMinType = true;
                }
                catch (RuntimeException e1)
                {
                    sec = 0;
                    err--;
                }
                if(err<=-2)
                    usedSecs = -1;
                else
                    usedSecs = min*60 + sec;
            }
            try
            {
                leftSecs = Integer.parseInt(result.group("CLEFT"));
                freeMinType = true;
            }
            catch (RuntimeException e)
            {
                int hr = 0,min =0 ,sec = 0;
                int err = 0;
                try
                {
                    hr = Integer.parseInt(result.group("CLHR"));
                    freeMinType = true;
                }
                catch (RuntimeException e1)
                {
                    hr = 0;
                    err--;
                }
                try
                {
                    min = Integer.parseInt(result.group("CLMIN"));
                    freeMinType = true;

                }
                catch (RuntimeException e1)
                {
                    min = 0;
                    err--;
                }
                try
                {
                    sec = Integer.parseInt(result.group("CLSEC"));
                    freeMinType = true;
                }
                catch (RuntimeException e1)
                {
                    sec = 0;
                    err--;
                }
                if(err<=-3)
                    leftSecs = -1;
                else
                {
                    leftMetric = "SECS";
                    leftSecs = (hr * 60 * 60) + (min * 60) + sec;
                }
            }
            try
            {
                //OnlySecs
                durSec = Integer.parseInt(result.group("CUSED"));
            }
            catch (RuntimeException e)
            {
                try
                {
                    //Clock Timing
                    String clock = result.group("DURC");
                    String sub[] = clock.split(":");
                    durSec = Integer.parseInt(sub[0]) * 60 * 60;
                    durSec += Integer.parseInt(sub[1]) * 60;
                    durSec += Integer.parseInt(sub[2]);
                }
                catch (RuntimeException e1)
                {
                    //Hr mim sec split
                    int hr = 0,min =0 ,sec = 0;
                    int err = 0;
                    try
                    {
                        hr = Integer.parseInt(result.group("DURHR"));
                    }
                    catch (RuntimeException e2)
                    {
                        hr = 0;
                        err--;
                    }
                    try
                    {
                        min = Integer.parseInt(result.group("DURMIN"));

                    }
                    catch (RuntimeException e2)
                    {
                        min = 0;
                        err--;
                    }
                    try
                    {
                        sec = Integer.parseInt(result.group("DURSEC"));
                        freeMinType = true;
                    }
                    catch (RuntimeException e2)
                    {
                        sec = 0;
                        err--;
                    }
                    if(err<=-3)
                        durSec = -1;
                    else
                    {
                        durSec = (hr * 60 * 60) + (min * 60) + sec;
                    }

                }
            }
            if(durSec!=-1 && usedSecs != -1)
            {
                if(usedSecs==durSec)
                {
                    usedMetric = "SECS";
                }
                //ceil of secs to get upper bound mins
                else if(usedSecs == ((durSec+60-1)/60))
                {
                    usedMetric = "MINS";
                }
                else if(usedSecs%60==0)
                {
                    //Per minute plan but measured in secs
                    usedMetric = "SECS";

                }
                else
                {
                    try
                    {
                        usedMetric = result.group("CUSEDM");
                    }
                    catch (RuntimeException e)
                    {
                        usedMetric = "SECS";
                    }
                }

            }
            else
            {
                try
                {
                    usedMetric = result.group("CUSEDM");
                }
                catch (RuntimeException e)
                {
                    usedMetric = "SECS";
                }
            }
            if(leftMetric==null)
                leftMetric = usedMetric;
            try
            {
                validity = result.group("VAL");
            }
            catch (RuntimeException e)
            {
                validity = null;
            }
            try
            {
                type = result.group("TYPE");
            }
            catch (RuntimeException e)
            {
                type = null;
            }
            try
            {
                mainBal = Float.parseFloat(result.group("MBAL"));
            }
            catch (RuntimeException e)
            {
                mainBal = -1.0f;
            }
            try
            {
                packCost = Float.parseFloat(result.group("PBUSED"));
            }
            catch (RuntimeException e)
            {
                packCost = -1.0f;
            }
            try
            {
                packBal = Float.parseFloat(result.group("PBAL"));
            }
            catch (RuntimeException e)
            {
                packBal = -1.0f;
            }
            if(usedSecs!=-1 && leftSecs!=-1)
            ptln("Type = "+type+" Used "+usedSecs+" metric = "+usedMetric+" Duration = "+durSec+" Left = "+leftSecs+" Metric= "+leftMetric+" Validity = "+validity+" Main Bal ="+mainBal+"\n");

            ptln("Type = "+type+" Duration = "+durSec+" Pcost = "+packCost+" Pbal = "+packBal+" Validity = "+validity+" Main Bal ="+mainBal+"\n");
            return  true;
        }
        return false;
    }
    private boolean normalCall(String message) throws JSONException
    {
        // Log.d(TAG, "Trying Normal Call");
        Matcher result = findDetails(getNormalCallRegex(), message);
        if (result != null)
        {
            // Log.d(TAG, "Matched!");
            String bal = result.group("MBAL");
            //to take care of number like 1,208.990
            bal = bal.replace(",","").trim();
            //to take care of number like "20."
            if(bal.charAt(bal.length()-1)=='.')
                bal = bal.substring(0,bal.length()-2);
            float mainBal = Float.parseFloat(bal);
            //ptln("Cost = " + mMatcher.group("COST"));
            // ptln("Balance = " +);
            int duration = -1;
            float cost = -1.0f;
            try
            {
                cost = Float.parseFloat(result.group("COST"));
            } catch (IndexOutOfBoundsException e)
            {
                cost = -1.0f;
            }
            try
            {
                duration = Integer.parseInt(result.group("DURS"));
                //ptln("Duration Secs = " + duration);
            } catch (IndexOutOfBoundsException e0)
            {
                try
                {
                    String clock = result.group("DURC");
                    String sub[] = clock.split(":");
                    duration = Integer.parseInt(sub[0]) * 60 * 60;
                    duration += Integer.parseInt(sub[1]) * 60;
                    duration += Integer.parseInt(sub[2]);
                    //ptln("Duration Clock = " + duration);
                } catch (IndexOutOfBoundsException e1)
                {
                    try
                    {
                        duration = Integer.parseInt(result.group("DURMIN")) * 60;
                        duration += Integer.parseInt(result.group("DURSEC"));
                        //ptln("Duration Min:Secs = " + duration);
                    } catch (IndexOutOfBoundsException e2)
                    {
                        duration = -1;
                        //ptln("No Duration Found A-Hole Telecos");
                    }
                }
            }
            ptln("Bal = "+mainBal+" Cost = "+cost+" Dur = "+duration);
            temp = mainBal+","+cost+","+duration;
            //this.type = USSDMessageType.NORMAL_CALL;
            //details = new NormalCall((new Date()).getTime(), cost, mainBal, duration, message);
            //Log.d(TAG, "details = " + details.toString());
            return true;
        }
        //Log.d(TAG, "Normal Call Didn't Match");
        return false;
    }
    private JSONArray getPackCallRegex()
    {
        return getJSONArray("G:/SimplyV2/TextMining/json/Final 05-01-16/PACK_CALL_PATTERNS.json");
    }
    private JSONArray getNormalCallRegex()
    {
        return getJSONArray("G:/SimplyV2/TextMining/json/Final 05-01-16/NORMAL_CALL_PATTERNS.json");
    }
    JSONArray getJSONArray(String fileName)
    {
        FileInputStream mFileReader = null;
        File jsonFile = null;
        try
        {
            jsonFile = new File(fileName);
            mFileReader = new FileInputStream(jsonFile);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }


        byte[] data = new byte[(int) jsonFile.length()];
        try
        {
            mFileReader.read(data);
            mFileReader.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
        String str = null;
        try
        {
            str = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        JSONArray mJsonArray = null;
        try
        {
            mJsonArray = new JSONArray(str);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return mJsonArray;
    }
    private Matcher findDetails(JSONArray regexArray, String message) throws JSONException
    {
        int length = regexArray.length();
        //Log.d(TAG, "Trying with " + length + "Regexes");
        Pattern mPattern;
        Matcher mMatcher;
        String regex;
        for (int i = 0; i < length; i++)
        {
            regex = regexArray.getJSONObject(i).getString("REGEX");
            //Log.d(TAG, "Trying with : " + regex);
            mPattern = Pattern.compile(regex);
            mMatcher = mPattern.matcher(message);
            if (mMatcher.find())
            {
                //Log.d(TAG, "Found a Match");
                ptln("Matched with "+regex);
                return mMatcher;

            }
        }
        //Log.d(TAG, "No Match Found");
        return null;
    }
    void sortJsonArray() throws IOException, JSONException
    {
        FileInputStream fileStram;
        byte[] data = null;
        try
        {
            fileStram = new FileInputStream(file);

            data = new byte[(int) file.length()];
            fileStram.read(data);
            fileStram.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        String str = new String(data, "UTF-8");
        JSONArray jArray = null;
        try
        {
            //str = str.replaceAll("\\n\\r*","");
            jArray = (new JSONObject(str)).getJSONArray("results");
        } catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<JSONObject> mList = new ArrayList<>();
        for(int i=0;i<jArray.length();i++)
        {
            mList.add(jArray.getJSONObject(i));
        }
        JSONArray sortedJsonArray = new JSONArray();

        Collections.sort(mList, new Comparator<JSONObject>()
        {
            //You can change "Name" with "ID" if you want to sort by ID
            private static final String KEY_NAME = "createdAt";

            public int compare(JSONObject c, JSONObject b)
            {
                Date valA = null;
                Date valB = null;

                try
                {
                    valA = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).parse(((String) c.get(KEY_NAME)).replaceAll("Z$", "+0000"));
                    valB = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).parse(((String) b.get(KEY_NAME)).replaceAll("Z$", "+0000"));
                } catch (JSONException e)
                {
                    //do something
                } catch (ParseException e)
                {
                    e.printStackTrace();
                }
                try{
                    return -valA.compareTo(valB);
                }
                catch (IllegalArgumentException w)
                {
                    return  1;
                }
                //if you want to change the sort order, simply use the following:
                //return -valA.compareTo(valB);
            }
        });



        for (int i = 0; i < mList.size(); i++)
        {
            sortedJsonArray.put(mList.get(i));
        }

        int i;

            fout.write((sortedJsonArray.toString()).getBytes());



        fout.close();

        //fout_g.close();
        //FileOutputStream fout_g = new FileOutputStream("E:\\demo_g.txt");
        /*for (i = 0; i < sortedJsonArray.length(); i++)
        {
            JSONObject person = (JSONObject) sortedJsonArray.get(i);

            if (person.has("M"))
            {
                String name = (String) person.get("M") + "\n";

                String sds = person.toString() + ",\n";

                String total = person.getString("M");
                //if(total.equals(""))
                //	continue;
        *//*if(!name.matches("(.*)ALERT(.*)")&&!name.matches("(.*)FLEXI(.*)")&&!name.matches("(.*)AttentionAllow(.*)")&&!name.matches("(.*)Call vianull(.*)")&&!name.matches("(.*)Emergency(.*)")&&
                !name.matches("(.*)Accept call(.*)")&&!name.matches("(.*)MMI code canceled(.*)")&&!name.matches("(.*)Preferred network type(.*)")&&!name.matches("(.*)Call not sent(.*)")&&!name.matches("(.*)Messaging would like to send(.*)")&&
			!name.matches("(.*)Network Mode(.*)")&&!name.matches("(.*)Call vianull(.*)")&&!name.matches("(.*)Service(.*)")&&!name.matches("(.*)Select SIM(.*)")&&!name.matches("(.*)Connection problem(.*)")&&
			!name.matches("(.*)Not registered on network(.*)")&&!name.matches("(.*)Call via(.*)")&&!name.matches("(.*)Try Again Later(.*)")&&!name.matches("(.*)Select SIM(.*)")&&!name.matches("(.*)Connection problem(.*)")&&
			!name.matches("(.*)Network mode(.*)")&&!name.matches("(.*)Alert(.*)")&&!name.matches("(.*)Audio(.*)")&&!name.matches("(.*)Select SIM(.*)")&&!name.matches("(.*)Connection problem(.*)"))*//*
                //if(name.toUpperCase().contains("BAL")||name.toUpperCase().contains("MAIN"))
                //{
                System.out.println(sds);
                fout.write(sds.getBytes());
                //	}
                //else
                //{
                //fout_g.write(sds.getBytes());
                //}


            }

        }*/

    }
}
