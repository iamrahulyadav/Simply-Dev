package com.builder.ibalance.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by Shabaz on 18-Oct-15.
 */
public class RegexUpdater
{
    final static String tag = RegexUpdater.class.getSimpleName();
    public void update(final int NEW_PARSER_VERSION)
    {
        Log.d(tag,"Started Updating the Regexes!!!!");

        ParseQuery regexQuery = new ParseQuery("REGEXES");
        regexQuery.findInBackground(new FindCallback()
        {


            @Override
            public void done(List list, com.parse.ParseException e)
            {
                if(e!=null)
                {
                    Log.d(tag,"The was an Error in Updating Regexes!!!!");
                }
                else
                {
                    Log.d(tag,"Got "+list.size()+ " Entries");
                    SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("GOOGLE_PREFS", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    for (Object p:list)
                    {
                        ParseObject regObj = (ParseObject)p;
                        editor.putString(regObj.getString("TYPE"),regObj.getString("REGEX"));
                    }
                    editor.putInt("PARSER_VERSION",NEW_PARSER_VERSION);
                    editor.commit();
                    Log.d(tag, "Finished Updating the Regexes!!!!");

                }
            }
        });
    }
}
