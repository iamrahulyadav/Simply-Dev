package com.example;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.example.Choice.ptln;

/**
 * Created by Shabaz on 03-Dec-15.
 */
public class Experiment
{
    File mFile = new File("G:/SimplyV2/Experiments/SERVICE_STATUS.json");
    FileInputStream mFileReader ;
    byte[] data;
    public Experiment()
    {

        try
        {
            mFileReader = new FileInputStream(mFile);
            data = new byte[(int) mFile.length()];
            mFileReader.read(data);
            mFileReader.close();
            String str = new String(data, "UTF-8");
            JSONArray mJsonArray = null;
                mJsonArray = new JSONArray(str);

            int i=0,len = mJsonArray.length();
            ptln("Total Users = "+len);
            int off=0, on =0, toggle = 0,ton=0,toff=0;
            boolean t = false;
            for(i=0;i<len;i++)
            {
                t=false;
                JSONObject mObject = mJsonArray.getJSONObject(i);

                if(mObject.getInt("SERVICE_TOGGLE_COUNT")>1)
                {
                    toggle++;
                    t= true;
                }
                if(mObject.getString("SERVICE_STATUS").equals("ON"))
                {
                    on++;
                    if(t)
                        ton++;
                }
                else{

                    off++;
                    if(t)
                        toff++;
                }
            }

            ptln("SERVICE On             = "+on);
            ptln("SERVICE OFF            = "+off);
            ptln("SERVICE On with Toggle = "+ton);
            ptln("SERVICE Off with Toggle= "+toff);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }
}
