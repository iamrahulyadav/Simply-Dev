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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.Choice.ptln;


public class Tests {
    FileInputStream mFileReader ;
    byte[] data;
    Tests() throws Exception
    {
        File regexes = new File("G:/SimplyV2/TextMining/json/CALL_PATTERNS.json");
        Pattern mPattern;
        Matcher mMatcher;
        int total=0,tsuccessful=0;
        mFileReader = new FileInputStream(regexes);
        data = new byte[(int) regexes.length()];
        mFileReader.read(data);
        mFileReader.close();

        String str = new String(data, "UTF-8");
        JSONArray regexArray = null;
        regexArray = new JSONArray(str);
        int length = regexArray.length();
        JsonFactory f = new MappingJsonFactory();
        FileInputStream mFileReader;
        FileOutputStream mFileWriter;
        FileOutputStream allRemWriter;
        File folder = new File("G:/SimplyV2/TextMining/unique_pat/from_sarvesh/call_duplicate");
        int[] countArr = new int[length];
        for (int i = 0; i <length; i++)
        {
            countArr[i]=0;
        }
        for (final File fileEntry : folder.listFiles())
        {
            if (fileEntry.isDirectory())
                continue;
            ptln("File = "+fileEntry.getName());
            File outputFile = new File("G:/SimplyV2/TextMining/unique_pat/from_sarvesh/call_duplicate/Processed/",fileEntry.getName()+"_might_be_good"+".txt");
            if(!outputFile.exists()) {
                outputFile.createNewFile();
            }
            File allRem = new File("G:/SimplyV2/TextMining/unique_pat/from_sarvesh/call_duplicate/Processed/",fileEntry.getName()+"_all_rem"+".txt");
            if(!allRem.exists()) {
                allRem.createNewFile();
            }
            mFileWriter = new FileOutputStream(outputFile);
            mFileWriter.write("{\"results\":[".getBytes());
            allRemWriter = new FileOutputStream(allRem);
            allRemWriter.write("{\"results\":[".getBytes());
            JsonParser jp = f.createJsonParser(fileEntry);

            JsonToken current;

            current = jp.nextToken();
            if (current != JsonToken.START_OBJECT)
            {
                System.out.println("Error: root should be object: quiting.");
                return;
            }
            int processed =0;
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
                            String message = node.get("Total").asText().toUpperCase();
                            processed++;
                            String regex;
                            for(int i=0;i<length;i++)
                            {
                                found = 0;

                                regex = regexArray.getJSONObject(i).getString("REGEX");
                                mPattern = Pattern.compile(regex);
                                mMatcher = mPattern.matcher(message);
                                if (mMatcher.find())
                                {
                                    countArr[i]++;
                                    found++;
                                    success++;
                                    break;
                                    //callSearch(mMatcher);
                                    //datSearch(mMatcher);
                                }
                            }
                            if (found == 0)
                            {
                                if(message.matches("\\d+\\.\\d+"))
                                {
                                    mFileWriter.write(("{\"M\":\"" + message + "\"},\n").getBytes());
                                }
                                allRemWriter.write(("{\"M\":\"" + message + "\"},\n").getBytes());
                            }

                        }
                        mFileWriter.write(("\"M\":\""+" "+"\"]}\n").getBytes());
                        mFileWriter.flush();
                        allRemWriter.write(("\"M\":\""+" "+"\"]}\n").getBytes());
                        allRemWriter.flush();
                        mFileWriter.close();
                        allRemWriter.close();
                        ptln(fileEntry.getName());
                        total+=processed;
                        tsuccessful+=success;
                        ptln("Processed = "+processed);
                        ptln("Successful = "+success);
                        ptln("UnSuccessful = "+(processed-success));


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
        }
        ptln("Total Processed = "+total);
        ptln("Total Successful = "+tsuccessful);
        ptln("UnSuccessful = "+(total-tsuccessful));
        for(int i=0;i<length;i++)
        {
           ptln( (i+1)+" : "+ regexArray.getJSONObject(i).getString("REGEX"));
            ptln("Used "+countArr[i]+"  times");
        }
    }


}