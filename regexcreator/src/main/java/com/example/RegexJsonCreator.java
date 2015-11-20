package com.example;

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
    static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String regex;
    JSONArray allJson = null;
    File call_json = new File("G:/SimplyV2/TextMining/json/CALL_PATTERNS.json");
    String carrier = "AIRCEL";
    int costPos = -1, durationSecPos = -1,mainBalPos = -1,durationClockPosHH = -1,durationClockPosMM = -1,durationClockPosSS = -1;
    int cont = -1;
    FileInputStream mFileReader ;
    FileOutputStream mFileWriter;
    byte[] data;
    public RegexJsonCreator()
    {
        try
        {
            mFileReader = new FileInputStream(call_json);


            data = new byte[(int) call_json.length()];
            mFileReader.read(data);
            mFileReader.close();

            String str = new String(data, "UTF-8");
            JSONArray mJsonArray = null;
                mJsonArray = new JSONArray(str);
            do
            {
                //regex
                //cost
                //durationSec -1 if not applicable
                //mainBal
                //d HH
                //d MM
                //d SS
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
                }

                mJsonArray.put(entry);

            }while (true);

            mFileWriter = new FileOutputStream(call_json);
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
}
