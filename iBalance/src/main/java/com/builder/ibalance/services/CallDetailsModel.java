package com.builder.ibalance.services;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Shabaz on 28-Sep-15.
 */
public class CallDetailsModel implements Parcelable
{
    private float   call_cost = 0.0f,
                    current_balance = 0.0f,
                    call_rate = 0.0f,
                    total_spent = 0.0f;
    private int duration = 0;
    private String  name = "Unkown",
                    number = "xxxxxxxxxxx",
                    carrier_circle = "Unkown/Unkown",
                    image_uri = null;

    public CallDetailsModel(float call_cost, float current_balance, float call_rate, int duration)
    {
        this.call_cost = call_cost;
        this.current_balance = current_balance;
        this.call_rate = call_rate;
        this.duration = duration;
    }

    public void setTotal_spent(float total_spent)
    {
        this.total_spent = total_spent;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    public void setCarrier_circle(String carrier_circle)
    {
        this.carrier_circle = carrier_circle;
    }

    public void setImage_uri(String image_uri)
    {
        this.image_uri = image_uri;
    }

    public CallDetailsModel(float call_cost, float current_balance, float call_rate, int duration, float total_spent, String name, String number, String carrier_circle, String image_uri)
    {
        this.call_cost = call_cost;
        this.current_balance = current_balance;
        this.call_rate = call_rate;
        this.duration = duration;
        this.total_spent = total_spent;
        this.name = name;
        this.number = number;
        this.carrier_circle = carrier_circle;
        this.image_uri = image_uri;
    }

    protected CallDetailsModel(Parcel in)
    {
        call_cost = in.readFloat();
        current_balance = in.readFloat();
        call_rate = in.readFloat();
        duration = in.readInt();
        total_spent = in.readFloat();
        name = in.readString();
        number = in.readString();
        carrier_circle = in.readString();
        image_uri = in.readString();
    }

    public static final Creator<CallDetailsModel> CREATOR = new Creator<CallDetailsModel>()
    {
        @Override
        public CallDetailsModel createFromParcel(Parcel in)
        {
            return new CallDetailsModel(in);
        }

        @Override
        public CallDetailsModel[] newArray(int size)
        {
            return new CallDetailsModel[size];
        }
    };

    public float getCall_cost()
    {
        return call_cost;
    }

    public float getCurrent_balance()
    {
        return current_balance;
    }

    public float getCall_rate()
    {
        return call_rate;
    }

    public float getTotal_spent()
    {
        return total_spent;
    }

    public int getDuration()
    {
        return duration;
    }

    public String getName()
    {
        return name;
    }

    public String getNumber()
    {
        return number;
    }

    public String getCarrier_circle()
    {
        return carrier_circle;
    }

    public String getImage_uri()
    {
        return image_uri;
    }

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
        dest.writeFloat(call_cost);
        dest.writeFloat(current_balance);
        dest.writeFloat(call_rate);
        dest.writeInt(duration);
        dest.writeFloat(total_spent);
        dest.writeString(name);
        dest.writeString(number);
        dest.writeString(carrier_circle);
        dest.writeString(image_uri);
    }
}
