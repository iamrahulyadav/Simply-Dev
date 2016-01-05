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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Shabaz on 24-Dec-15.
 */
public class SortJson
{
    File file = new File("G:/SimplyV2/TextMining/ALL_DATA/Processed2/Invalid_USSD.json_all_rem.txt");
    FileOutputStream fout = new FileOutputStream("G:/SimplyV2/TextMining/ALL_DATA/Processed2/INVALID_USSD_SORTED.json");
    public SortJson() throws IOException, JSONException
    {

        sortStrings();
        //sortJson();


    }

    private void sortStrings() throws IOException
    {
        //new InputStreamReader(System.in));//
        Charset charset = Charset.forName("UTF-8");
        BufferedReader br = new BufferedReader(Files.newBufferedReader(Paths.get("G:/SimplyV2/TextMining/ALL_DATA/Processed2/Invalid_USSD.json_all_rem.txt"),charset));
        String message = null;
        ArrayList<String> mList = new ArrayList<>();
        while ((message = br.readLine())!=null)
        {
            mList.add(message.trim());
        }
        Collections.sort(mList);
        for (String t:mList)
        {
            fout.write((t+"\n").getBytes());
        }
        fout.flush();
        fout.close();
    }
    private void sortJson() throws IOException, JSONException
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
            // TODO Auto-generated catch block
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

        JSONArray sortedJsonArray = new JSONArray();

        List<String> jsonValues = new ArrayList<String>();
        JSONObject tobj;
        for (int i = 0; i < jArray.length(); i++)
        {
            tobj = jArray.getJSONObject(i);
            //ptln(tobj.toString());
            jsonValues.add((String) tobj.get("M"));
        }
        Collections.sort(jsonValues);/*, new Comparator<JSONObject>()
            {
                //You can change "Name" with "ID" if you want to sort by ID
                private static final String KEY_NAME = "M";

                @Override
                public int compare(JSONObject c, JSONObject b)
                {
                    String valA = new String();
                    String valB = new String();

                    try
                    {
                        valA = (String) c.get(KEY_NAME);
                        valB = (String) b.get(KEY_NAME);
                        if (valA.isEmpty() || valB.isEmpty()) return -1;
                    } catch (JSONException e)
                    {
                        //do something
                    }
                    try{
                        return valA.compareTo(valB);
                    }
                    catch (IllegalArgumentException w)
                    {
                        ptln("val A= "+valA);
                        ptln("val A= "+valA);
                        return  -1;
                    }
                    //if you want to change the sort order, simply use the following:
                    //return -valA.compareTo(valB);
                }
            });*/



        /*for (int i = 0; i < jArray.length(); i++)
        {
            sortedJsonArray.put(jsonValues.get(i));
        }*/

        int i;

        for (String t:jsonValues)
        {
            fout.write((t+"\n").getBytes());
        }


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
