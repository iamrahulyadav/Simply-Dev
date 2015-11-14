package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Choice
{
    static int choice = 1;

    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    public static void main(String[] args) throws IOException
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
