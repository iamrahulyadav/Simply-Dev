package com.builder.ibalance.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.parse.ConfigCallback;
import com.parse.FindCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by Shabaz on 18-Oct-15.
 */
public class RegexUpdater
{
    final static String tag = RegexUpdater.class.getSimpleName();

    int PARSER_VERSION = 1;
    int NEW_PARSER_VERSION = 1;
    public void check()
    {
        PARSER_VERSION =  MyApplication.context.getSharedPreferences("GOOGLE_PREFS", Context.MODE_PRIVATE).getInt("PARSER_VERSION_V2",1);
        ParseConfig.getInBackground(new ConfigCallback()
        {
            @Override
            public void done(ParseConfig config, ParseException e)
            {
                if (e == null)
                {
                    Log.d(tag, "Yay! Config was fetched from the server.");
                } else
                {
                    Log.e(tag, "Failed to fetch. Using Cached Config.");
                    config = ParseConfig.getCurrentConfig();
                }
                if ((config != null))
                {
                    NEW_PARSER_VERSION = config.getInt("PARSER_VERSION_V2");
                    Log.d(tag,"NEW_PARSER_VERSION = "+NEW_PARSER_VERSION);
                    if (NEW_PARSER_VERSION > PARSER_VERSION)
                    {
                        update(NEW_PARSER_VERSION);
                    }
                }
                //Log.d(tag, String.format("The ad frequency is %d!", adFrequency));
            }
        });
    }

     void update(final int NEW_PARSER_VERSION)
    {
        Log.d(tag,"Started Updating the Regexes!!!!");

        ParseQuery regexQuery = new ParseQuery("REGEXES");
        regexQuery.whereGreaterThanOrEqualTo("VERSION",NEW_PARSER_VERSION);
        regexQuery.findInBackground(new FindCallback()
        {


            /**
             * Override this function with the code you want to run after the fetch is complete.
             *
             * @param objects The objects that were retrieved, or null if it did not succeed.
             * @param e
             */
            @Override
            public void done(List objects, ParseException e)
            {

            }

            /**
             * {@code done(t1, t2)} must be overridden when you are doing a background operation. It is called
             * when the background operation completes.
             * <p/>
             * If the operation is successful, {@code t1} will contain the results and {@code t2} will be
             * {@code null}.
             * <p/>
             * If the operation was unsuccessful, {@code t1} will be {@code null} and {@code t2} will contain
             * information about the operation failure.
             *
             * @param object         Generally the results of the operation.
             * @param throwable Generally an {@link Throwable} that was thrown by the operation, if there was any.
             */
            @Override
            public void done(Object object, Throwable throwable)
            {

                Log.d(tag,"Other Done was called");
                if(throwable==null)
                {
                    Log.d(tag,"Fetched new Regexes");
                    if (object== null)
                    {
                        Crashlytics.logException(throwable);
                        //throwable.printStackTrace();
                        Log.d(tag,"The was an Error in Updating Regexes!!!!");
                    } else
                    {
                        List objects = (List)object;
                        //Log.d(tag,"Got "+objects.size()+ " Entries");
                        SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("GOOGLE_PREFS", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        for (Object p : objects)
                        {
                            ParseObject regObj = (ParseObject) p;
                            //Log.d(tag,"Updating  "+regObj.getString("TYPE"));
                            editor.putString(regObj.getString("TYPE"), regObj.getString("REGEX"));
                        }
                        editor.putInt("PARSER_VERSION_V2", NEW_PARSER_VERSION);
                        editor.commit();
                        //Log.d(tag, "Finished Updating the Regexes!!!!");

                    }
                }
                else
                {
                    Crashlytics.logException(throwable);
                }
            }
        });
    }
}
