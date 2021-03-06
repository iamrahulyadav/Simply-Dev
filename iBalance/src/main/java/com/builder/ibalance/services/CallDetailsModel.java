package com.builder.ibalance.services;

import android.os.Parcel;
import android.os.Parcelable;

import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.models.USSDModels.NormalCall;

/**
 * Created by Shabaz on 28-Sep-15.
 */
public class CallDetailsModel implements Parcelable
{
    // Used for both Normal
    private float call_cost = 0.0f, current_balance = 0.0f, call_rate = 0.0f, total_spent = 0.0f,pack_bal_used = 0.0f,pack_bal_left = 0.0f;
    String pack_name="N/A",used_metric="secs",left_metric="secs",validity="N/A";
    int pack_duration_used,pack_duration_left,call_type;
    private int duration = 0, sim_slot;
    private String name = "Unkown";
    private String number = "xxxxxxxxxxx";
    private String carrier_circle = "Unkown/Unkown";
    private String image_uri = null;
    private String message = "";

    public void addUserDetails(ContactDetailModel userDetails)
    {
        this.name = userDetails.name;
        this.image_uri = userDetails.image_uri;
        this.carrier_circle = userDetails.carrier + "," + userDetails.circle;
        this.total_spent = userDetails.total_cost;
    }




    public CallDetailsModel(NormalCall mNormalCall)
    {
        this.call_type = mNormalCall.getType();
        this.sim_slot = mNormalCall.sim_slot;
        this.number = mNormalCall.ph_number;
        this.call_cost = mNormalCall.call_cost;
        this.current_balance = mNormalCall.main_bal;
        this.call_rate = 1.7f;
        try
        {
            this.call_rate = (mNormalCall.call_cost / mNormalCall.call_duration) * 100;
        } catch (Exception e)
        {

        }
        this.duration = mNormalCall.call_duration;
        this.message = mNormalCall.original_message;
    }

    public String getMessage()
    {
        return message;
    }



    public int getSim_slot()
    {
        return sim_slot;
    }

    protected CallDetailsModel(Parcel in)
    {
        sim_slot = in.readInt();
        call_cost = in.readFloat();
        current_balance = in.readFloat();
        call_rate = in.readFloat();
        duration = in.readInt();
        total_spent = in.readFloat();
        name = in.readString();
        number = in.readString();
        carrier_circle = in.readString();
        image_uri = in.readString();
        message = in.readString();
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
        dest.writeInt(sim_slot);
        dest.writeFloat(call_cost);
        dest.writeFloat(current_balance);
        dest.writeFloat(call_rate);
        dest.writeInt(duration);
        dest.writeFloat(total_spent);
        dest.writeString(name);
        dest.writeString(number);
        dest.writeString(carrier_circle);
        dest.writeString(image_uri);
        dest.writeString(message);
    }

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


}
