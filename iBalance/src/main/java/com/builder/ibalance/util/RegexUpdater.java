package com.builder.ibalance.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.parse.FindCallback;
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

    public void update(final int NEW_PARSER_VERSION)
    {
        //V10Log.d(tag,"Started Updating the Regexes!!!!");

        ParseQuery regexQuery = new ParseQuery("REGEXES");
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
                if (objects== null)
                {
                    //V10Log.d(tag,"The was an Error in Updating Regexes!!!!");
                } else
                {
                    //V10Log.d(tag,"Got "+list.size()+ " Entries");
                    SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("GOOGLE_PREFS", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    for (Object p : objects)
                    {
                        ParseObject regObj = (ParseObject) p;
                        editor.putString(regObj.getString("TYPE"), regObj.getString("REGEX"));
                    }
                    editor.putInt("PARSER_VERSION", NEW_PARSER_VERSION);
                    editor.commit();
                    //V10Log.d(tag, "Finished Updating the Regexes!!!!");

                }
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
             * @param o         Generally the results of the operation.
             * @param throwable Generally an {@link Throwable} that was thrown by the operation, if there was any.
             */
            @Override
            public void done(Object o, Throwable throwable)
            {

            }
        });
    }
}
