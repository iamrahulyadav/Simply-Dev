package com.builder.ibalance.models.PopupModels;

import android.os.Parcel;
import android.os.Parcelable;

import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.models.USSDModels.NormalCall;

/**
 * Created by Shabaz on 06-Jan-16.
 */
public class NormalCallPopup implements Parcelable
{
    private float call_cost = 0.0f, current_balance = 0.0f, call_rate = 0.0f;
    private int duration = 0, sim_slot=0;
    private String message = "";
    //UserDetails
    private float total_spent = 0.0f;
    private String name = "Unkown";
    private String number = "xxxxxxxxxxx";
    private String carrier_circle = "Unkown/Unkown";

    public NormalCallPopup()
    {
        this.call_cost = 0.5f;
        this.current_balance = 200.45f;
        this.call_rate = 1.7f;
        this.duration = 30;
        this.sim_slot = 0;
        this.message = "Lorem Ipsum";
        this.total_spent = 105.43f;
        this.name = "Shabaz";
        this.number = "9686034642";
        this.carrier_circle = "Unkown/Unkown";
        this.image_uri = null;
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

    public int getDuration()
    {
        return duration;
    }

    public int getSim_slot()
    {
        return sim_slot;
    }

    public String getMessage()
    {
        return message;
    }

    public float getTotal_spent()
    {
        return total_spent;
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

    private String image_uri = null;

    public NormalCallPopup(NormalCall mNormalCall)
    {
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

    public void addUserDetails(ContactDetailModel userDetails)
    {
        this.name = userDetails.name;
        this.image_uri = userDetails.image_uri;
        this.carrier_circle = userDetails.carrier + "," + userDetails.circle;
        this.total_spent = userDetails.total_cost;
    }
    protected NormalCallPopup(Parcel in)
    {
        call_cost = in.readFloat();
        current_balance = in.readFloat();
        call_rate = in.readFloat();
        total_spent = in.readFloat();
        duration = in.readInt();
        sim_slot = in.readInt();
        name = in.readString();
        number = in.readString();
        carrier_circle = in.readString();
        image_uri = in.readString();
        message = in.readString();
    }

    @Override
    public String toString()
    {
        return "NormalCallPopup{" +
                "call_cost=" + call_cost +
                ", current_balance=" + current_balance +
                ", call_rate=" + call_rate +
                ", duration=" + duration +
                ", sim_slot=" + sim_slot +
                ", message='" + message + '\'' +
                ", total_spent=" + total_spent +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", carrier_circle='" + carrier_circle + '\'' +
                ", image_uri='" + image_uri + '\'' +
                '}';
    }

    public static final Creator<NormalCallPopup> CREATOR = new Creator<NormalCallPopup>()
    {
        @Override
        public NormalCallPopup createFromParcel(Parcel in)
        {
            return new NormalCallPopup(in);
        }

        @Override
        public NormalCallPopup[] newArray(int size)
        {
            return new NormalCallPopup[size];
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
        dest.writeFloat(call_cost);
        dest.writeFloat(current_balance);
        dest.writeFloat(call_rate);
        dest.writeFloat(total_spent);
        dest.writeInt(duration);
        dest.writeInt(sim_slot);
        dest.writeString(name);
        dest.writeString(number);
        dest.writeString(carrier_circle);
        dest.writeString(image_uri);
        dest.writeString(message);
    }
}
