package com.builder.ibalance.database.models;

import com.builder.ibalance.database.helpers.IbalanceContract;

/**
 * Created by Shabaz on 03-Oct-15.
 */
public class ContactDetailModel
{

    public String number,name,carrier,circle,image_uri;
    public int in_count;
    public int in_duration;
    public int out_count;

    public int out_duration;
    public int miss_count;

    public ContactDetailModel(String number, String name, String carrier, String circle, String image_uri, int in_count, int in_duration, int out_count, int out_duration, int miss_count)
    {
        this.number = number;
        this.name = name;
        this.carrier = carrier;
        this.circle = circle;
        this.image_uri = image_uri;
        this.in_count = in_count;
        this.in_duration = in_duration;
        this.out_count = out_count;
        this.out_duration = out_duration;
        this.miss_count = miss_count;
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
                            IbalanceContract.ContactDetailEntry.TABLE_NAME +
                            " ("+
                            IbalanceContract.ContactDetailEntry.COLUMN_NAME_NUMBER+","+
                            IbalanceContract.ContactDetailEntry.COLUMN_NAME_NAME+","+
                            IbalanceContract.ContactDetailEntry.COLUMN_NAME_CARRIER+","+
                            IbalanceContract.ContactDetailEntry.COLUMN_NAME_CIRCLE+","+
                            IbalanceContract.ContactDetailEntry.COLUMN_NAME_IMAGE_URI+","+
                            IbalanceContract.ContactDetailEntry.COLUMN_NAME_IN_COUNT+","+
                            IbalanceContract.ContactDetailEntry.COLUMN_NAME_IN_DURATION+","+
                            IbalanceContract.ContactDetailEntry.COLUMN_NAME_OUT_COUNT+","+
                            IbalanceContract.ContactDetailEntry.COLUMN_NAME_OUT_DURATION+","+
                            IbalanceContract.ContactDetailEntry.COLUMN_NAME_MISS_COUNT+
                            ") " +
                            "VALUES " +
                            "('"+number+"','"
                            +name+"','"
                            +carrier+"','"
                            +circle+"','"
                            +image_uri+"',"
                            +in_count+","
                            +in_duration+","
                            +out_count+", "
                            +out_duration+", "
                            +miss_count
                            +")";
        }
    }
