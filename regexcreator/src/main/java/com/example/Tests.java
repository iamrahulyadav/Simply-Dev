package com.example;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.Choice.ptln;


public class Tests
{
    FileInputStream mFileReader;
    byte[] data, data1;

    Tests() throws Exception
    {
        long starTime = System.nanoTime();
        File regexes = new File("G:/SimplyV2/TextMining/json/ALL.json");
        ArrayList<String> buffer = new ArrayList<>();
        ArrayList<String> mismatchbuffer = new ArrayList<>();
        //File regexes1 = new File("G:/SimplyV2/TextMining/json/SMS_PACK_PATTERNS.json");
        Pattern mPattern;
        Matcher mMatcher;
        int total = 0, tsuccessful = 0;
        mFileReader = new FileInputStream(regexes);
        data = new byte[(int) regexes.length()];
        mFileReader.read(data);
        mFileReader.close();
       /* mFileReader = new FileInputStream(regexes1);
        data1 = new byte[(int) regexes1.length()];
        mFileReader.read(data1);
        mFileReader.close();*/

        String str = new String(data, "UTF-8");
        // String str1 = new String(data1, "UTF-8");
        JSONArray regexArray = null;
        //JSONArray regexArray1 = null;
        regexArray = new JSONArray(str);
        // regexArray1 = new JSONArray(str1);
        int length = regexArray.length();
        // int length1 = regexArray1.length();
        JsonFactory f = new MappingJsonFactory();
        FileInputStream mFileReader;
        FileOutputStream mFileWriter;
        FileOutputStream allRemWriter;
        FileOutputStream misMatchWriter;
        long nCall = 0, pCall = 0, nData = 0, pData = 0, nSMS = 0, pSMS = 0;
        int[] countArr = new int[length];
        ArrayList<Pattern> patterns = new ArrayList<>();
        for (int i = 0; i < length; i++)
        {
            countArr[i] = 0;
            patterns.add(Pattern.compile(regexArray.getJSONObject(i).getString("REGEX")));
        }

        File fileEntry = new File("G:/SimplyV2/TextMining/ALL_DATA/Invalid_USSD.json");
        ptln("File = " + fileEntry.getName());
        File allRem = new File("G:/SimplyV2/TextMining/ALL_DATA/Processed2/", fileEntry.getName() + "_all_rem" + ".txt");
        //File mismatch = new File("G:/SimplyV2/TextMining/ALL_DATA/Processed2/", fileEntry.getName() + "_mismatch" + ".txt");

        if (!allRem.exists())
        {
            allRem.createNewFile();
        }
        /*if (!mismatch.exists())
        {
            mismatch.createNewFile();
        }*//*
            mFileWriter = new FileOutputStream(outputFile);
            mFileWriter.write("{\"results\":[".getBytes());*/
        allRemWriter = new FileOutputStream(allRem);
        //allRemWriter.write("{\"results\":[".getBytes());
        //misMatchWriter = new FileOutputStream(mismatch);
        //misMatchWriter.write("{\"results\":[".getBytes());
        JsonParser jp = f.createJsonParser(fileEntry);

        JsonToken current;

        current = jp.nextToken();
        if (current != JsonToken.START_OBJECT)
        {
            System.out.println("Error: root should be object: quiting.");
            return;
        }
        long processed = 0;
        long unSuccess = 0;
        int found = 0;
        while (jp.nextToken() != JsonToken.END_OBJECT)
        {
            String fieldName = jp.getCurrentName();
            processed = 0;
            // move from field name to field value
            int success = 0;
            current = jp.nextToken();
            if (fieldName.equals("results"))
            {
                if (current == JsonToken.START_ARRAY)
                {
                    // For each of the records in the array
                    while (jp.nextToken() != JsonToken.END_ARRAY)
                    {
                        // read the record into a tree model,
                        // this moves the parsing position to the end of it
                        // And now we have random access to everything in the object

                        JsonNode node = jp.readValueAsTree();
                        String message = "";
                        if (node == null) continue;
                        try
                        {
                            message = node.get("MESSAGE").asText().toUpperCase().replace("\n", "_").replace('\u0011', ' ').replace("ยง"," ").replace("\r", "");
                        } catch (Exception e)
                        {
                            try
                            {
                                message = node.get("Total").asText().toUpperCase().replace("\n", "_").replace('\u0011', ' ').replace("ยง"," ").replace("\r", "");
                            } catch (Exception e1)
                            {
                                continue;
                            }
                        }
                        processed++;
                        for (int i = 0; i < length; i++)
                        {
                            found = 0;
                            mMatcher = patterns.get(i).matcher(message);
                            String otype, ctype;
                            if (mMatcher.find())
                            {
                                if(i<22)
                                {
                                    ctype = "NORMAL_CALL";
                                }
                                else if(i<35)
                                {
                                    ctype = "DATA";
                                }
                                else if(i<43)
                                {
                                    ctype = "NORMAL_SMS";
                                }
                                else if(i<74)
                                {
                                    ctype = "CALL_PACK";
                                }
                                else if(i<93)
                                {
                                    ctype = "DATA_PACK";
                                }
                                else
                                {
                                    ctype = "SMS_PACK";
                                }
                                //otype = node.get("TYPE").asText();

                               /* if(!otype.equals(ctype))
                                {
                                    //mismatchbuffer.add("{\"M\":\"" + "O = "+otype+"\nC = "+ctype+"\n"+message + "\"},\n");
                                    mismatchbuffer.add("O = "+otype+"\tC = "+ctype+"\t"+message);
                                    //misMatchWriter.write(("{\"M\":\"" + "O = "+otype+"\nC = "+ctype+"\n"+message + "\"},\n").getBytes());
                                }*/
                                countArr[i]++;
                                found++;
                                success++;
                                switch (ctype)
                                {
                                    case "NORMAL_CALL":
                                        nCall++;
                                        break;
                                    case "DATA_PACK":
                                        pData++;
                                        break;
                                    case "SMS_PACK":
                                        pSMS++;
                                        break;
                                    case "CALL_PACK":
                                        pCall++;
                                        break;
                                    case "NORMAL_SMS":
                                        nSMS++;
                                        break;
                                    case "DATA":
                                        nData++;
                                        break;
                                    default:
                                }
                                break;

                                //callSearch(mMatcher);
                                //datSearch(mMatcher);
                            }
                        }
                        if (found == 0)
                        {
                                /*if(message.matches("\\d+\\.\\d+"))
                                {
                                    mFileWriter.write(("{\"M\":\"" + message + "\"},\n").getBytes());
                                }*/
                            unSuccess++;
                            buffer.add( message);

                        }
                        if(unSuccess%5000==0)
                        {
                            for (String s:buffer)
                            {
                                allRemWriter.write(( s + "\n").getBytes());
                            }

                            buffer.clear();
                        }
                    }
                   /* for (String s:mismatchbuffer)
                    {
                        misMatchWriter.write((s+ "\n").getBytes());
                    }
                    mismatchbuffer.clear();*/

                    allRemWriter.flush();
                    //misMatchWriter.flush();
                    //misMatchWriter.close();
                    allRemWriter.close();
                    ptln(fileEntry.getName());
                    total += processed;
                    tsuccessful += success;


                } else
                {
                    System.out.println("Error: records should be an array: skipping.");
                    jp.skipChildren();
                }
            } else
            {
                System.out.println("Unprocessed property: " + fieldName);
                jp.skipChildren();
            }
        }
        ptln("Total Processed = " + total);
        ptln("Total Successful = " + tsuccessful);
        ptln("UnSuccessful = " + (total - tsuccessful));
        ptln("Normal Call = " + nCall);
        ptln("Pack Call = " + pCall);
        ptln("Normal Data = " + nData);
        ptln("Pack Data = " + pData);
        ptln("Normal SMS = " + nSMS);
        ptln("Pack SMS = " + pSMS);
        for (int i = 0; i < length; i++)
        {
            ptln((i + 1) + " : " + regexArray.getJSONObject(i).getString("REGEX"));
            ptln("Used " + countArr[i] + "  times");
        }
        ptln("Time Taken = "+ (((System.nanoTime())-starTime)/1000000000L)+" Secs" );
    }


}