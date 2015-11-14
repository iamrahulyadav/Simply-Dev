package com.example;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
/**
 * Created by Shabaz on 14-Nov-15.
 */
public class FileCleaner
{
    byte[] data = null ;
    public FileCleaner()
    {
        FileInputStream mFileReader;
        FileOutputStream mFileWriter;
        File folder = new File("G:/SimplyV2/TextMining/unique_pat");
       for (final File fileEntry : folder.listFiles())
       {
           if (fileEntry.isDirectory())
               continue;
           Choice.ptln("Working on " + fileEntry.getName());
           try
           {
               mFileReader = new FileInputStream(fileEntry);


               data = new byte[(int) fileEntry.length()];
               mFileReader.read(data);
               mFileReader.close();

               String str = new String(data, "UTF-8");
               JSONArray mJsonArray = null;
               try
               {
                   mJsonArray = new JSONArray(str);
                   File outputFile = new File("G:/SimplyV2/TextMining/unique_pat/" + "ussd_"+fileEntry.getName() +".txt");
                   Choice.ptln("Writing to " + outputFile.getName());
                   mFileWriter = new FileOutputStream(outputFile);
                   int len = mJsonArray.length();
                   JSONObject mJsonObject;
                   for (int i = 0; i < len; i++)
                   {
                       mJsonObject = mJsonArray.getJSONObject(i);
                       String output = mJsonObject.getString("Total").replace("\n", "_") + "\n";
                       mFileWriter.write(output.getBytes());
                   }
                   mFileWriter.close();

               } catch (JSONException e)
               {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
               }
           } catch (IOException e)
           {
               e.printStackTrace();
           }
       }
    }
}
 /*   int verify, putData;
                File fileTemp = new File("G:/SimplyV2/TextMining/unique_pat/AIRCEL_unique_pat_call_temp.txt");
                FileWriter fw = new FileWriter(fileTemp);
                BufferedWriter bw = new BufferedWriter(fw);
                FileReader fr = new FileReader(fileEntry);
                BufferedReader br = new BufferedReader(fr);
                int i=0;
                while( (verify=br.read()) != -1 ){
                    if(verify != -1){
                       if((char)verify== '\"')
                       {
                           i++;
                           if(i==3)
                               bw.write("");
                           else
                           if (i==4)
                           {
                               i=0;
                               bw.write("");
                           }
                           else
                           bw.write(verify);
                       }
                        else
                           bw.write(verify);
                        bw.flush();
                    }
                }
                br.close();
                bw.close();*/