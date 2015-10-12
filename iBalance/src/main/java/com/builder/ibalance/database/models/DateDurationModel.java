package com.builder.ibalance.database.models;

import com.builder.ibalance.database.helpers.IbalanceContract;

/**
 * Created by Shabaz on 03-Oct-15.
 */
public class DateDurationModel
{
    long date;
    int day_of_the_week;
    int in_count;
    int in_duration;
    int out_count;
    int out_duration;
    int miss_count;

    public DateDurationModel(long date, int day_of_the_week,int in_count, int in_duration, int out_count, int out_duration, int miss_count)
    {
        this.date = date;
        this.day_of_the_week = day_of_the_week;
        this.in_count = in_count;
        this.in_duration = in_duration;
        this.out_count = out_count;
        this.out_duration = out_duration;
        this.miss_count = miss_count;
    }

    public void clear()
    {
        this.date = 0l;
        this.day_of_the_week = 0;
        this.in_count = 0;
        this.in_duration = 0;
        this.out_count = 0;
        this.out_duration = 0;
        this.miss_count = 0;
    }
    public void setDate(long date)
    {
        this.date = date;
    }
    public void setDay_of_the_week(int day)
    {
        this.day_of_the_week = day;
    }
    public void increment_in_count()
    {
        this.in_count++;
    }

    public void increment_out_count()
    {
        this.out_count++;
    }

    public void increment_miss_count()
    {
        this.miss_count++;
    }

    public void add_to_in_duration(int duration)
    {
        this.in_duration += duration;
    }

    public void add_to_out_duration(int duration)
    {
        this.out_duration += duration;
    }


    @Override
    public String toString()
    {
        return
                "INSERT INTO " +
                        IbalanceContract.DateDurationEntry.TABLE_NAME +
                        "("+
                        IbalanceContract.DateDurationEntry.COLUMN_NAME_DATE+","+
                        IbalanceContract.DateDurationEntry.COLUMN_NAME_WEEK_DAY+","+
                        IbalanceContract.DateDurationEntry.COLUMN_NAME_IN_COUNT+","+
                        IbalanceContract.DateDurationEntry.COLUMN_NAME_IN_DURATION+","+
                        IbalanceContract.DateDurationEntry.COLUMN_NAME_OUT_COUNT+","+
                        IbalanceContract.DateDurationEntry.COLUMN_NAME_OUT_DURATION+","+
                        IbalanceContract.DateDurationEntry.COLUMN_NAME_MISS_COUNT+
                        ") " +
                        "VALUES " +
                        "("+date+","
                        +day_of_the_week+","
                        +in_count+","
                        +in_duration+","
                        +out_count+", "
                        +out_duration+", "
                        +miss_count
                        +")";
    }
}
