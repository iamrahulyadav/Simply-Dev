package com.builder.ibalance.database.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.builder.ibalance.database.helpers.IbalanceContract;

/**
 * Created by Shabaz on 03-Oct-15.
 */
public class ContactDetailModel implements Parcelable
{

    public String number="",name="",carrier="Unknown",circle="Unknown",image_uri;
    public int in_count =0;
    public int in_duration=0;
    public int out_count=0;

    public int out_duration=0;
    public int miss_count=0;
    public float total_cost = 0f;

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

    public ContactDetailModel(String number, String name, String carrier, String circle)
    {
        this.number = number;
        this.name = name;
        this.carrier = carrier;
        this.circle = circle;
    }

    public ContactDetailModel(String name, String carrier, String circle, String image_uri, float total_cost)
    {
        this.name = name;
        this.carrier = carrier;
        this.circle = circle;
        this.image_uri = image_uri;
        this.total_cost = total_cost;
    }

    public ContactDetailModel(String number, String name, String carrier, String circle, String image_uri, int in_count, int in_duration, int out_count, int out_duration, int miss_count,float total_cost)
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
        this.total_cost = total_cost;
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


    protected ContactDetailModel(Parcel in)
    {
        number = in.readString();
        name = in.readString();
        carrier = in.readString();
        circle = in.readString();
        image_uri = in.readString();
        in_count = in.readInt();
        in_duration = in.readInt();
        out_count = in.readInt();
        out_duration = in.readInt();
        miss_count = in.readInt();
        total_cost = in.readFloat();
    }

    public static final Creator<ContactDetailModel> CREATOR = new Creator<ContactDetailModel>()
    {
        @Override
        public ContactDetailModel createFromParcel(Parcel in)
        {
            return new ContactDetailModel(in);
        }

        @Override
        public ContactDetailModel[] newArray(int size)
        {
            return new ContactDetailModel[size];
        }
    };
    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents()
    {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(number);
        dest.writeString(name);
        dest.writeString(carrier);
        dest.writeString(circle);
        dest.writeString(image_uri);
        dest.writeInt(in_count);
        dest.writeInt(in_duration);
        dest.writeInt(out_count);
        dest.writeInt(out_duration);
        dest.writeInt(miss_count);
        dest.writeFloat(total_cost);
    }
}
