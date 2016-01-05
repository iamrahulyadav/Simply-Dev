package com.example;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Choice
{
    static int choice = 1;

    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    public static void main(String[] args) throws IOException, JSONException
    {
        if(args.length>0)
        {
            choice = Integer.parseInt(args[0]);
        }
        else
        {
            ptln("Enter Choice");
            ptln("1 --- Duplicate Check");
            ptln("2 --- Create JSON");
            ptln("3 --- Clean Files");
            ptln("4 --- Experiments");
            ptln("5 --- Tests");
            ptln("6 --- Create Encrypted Regex");
            ptln("7 --- Sort JSON");
            ptln("8 --- USSDParser");
            ptln("9 --- Scratch Pad");
            ptln("10 --- TRAI");
            readChoice();
        }
        switch (choice)
        {
            case 1:
                new DuplicationRemover();
                break;
            case 2:
                new RegexJsonCreator();
                break;
            case 3:
                new FileCleaner();
                break;
            case 4:
                new Experiment();
                break;
            case 5:
                try
                {
                    new Tests();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            case 6:
                new Encdec();
                break;
            case 7:
                try
                {
                    new SortJson();
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
                break;

            case 8:
                new USSDParser();
                break;

            case 9:
                new ScratchPad();
                break;
            case 10:
                new TRAI();
                break;

            default:
                ptln("Wrong Input");
                 readChoice();

        }
    }

    private static void readChoice()
    {
        try{
            choice = Integer.parseInt(br.readLine());
        }
        catch (Exception e)
        {
            ptln("Wrong Choice\n");
        }
    }

    static void ptln(String s)
    {
        System.out.println(s);
    }
}
