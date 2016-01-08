package com.builder.ibalance.models.PopupModels;

import android.os.Parcel;
import android.os.Parcelable;

import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.models.USSDModels.PackCall;


/**
 * Created by Shabaz on 06-Jan-16.
 */
public class PackCallPopup implements Parcelable
{
    private float call_cost = 0.0f, current_balance = 0.0f, call_rate = 0.0f, total_spent = 0.0f,pack_bal_used = 0.0f,pack_bal_left = 0.0f;
    String pack_name="N/A",used_metric="secs",left_metric="secs",validity="N/A";
    int pack_duration_used=0;

    public PackCallPopup()
    {
        this.call_cost = 0.5f;
        this.current_balance = 200.45f;
        this.call_rate = 1.7f;
        this.duration = 30;
        this.sim_slot = 0;
        this.message = "Lorem Ipsum";
        this.total_spent = 105.43f;
        this.name = "Shabaz";
        this.number = "997211547";
        this.carrier_circle = "Unkown/Unkown";
        this.image_uri = null;
        this.pack_bal_used = -1.0f;
        this.pack_bal_left = -1.0f;
        this.pack_name = "ALL Local";
        this.used_metric = "S";
        this.left_metric = "S";
        this.validity = "13/01/2016";
        this.pack_duration_used =60;
        this.pack_duration_left = 2140;

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

    public float getPack_bal_used()
    {
        return pack_bal_used;
    }

    public float getPack_bal_left()
    {
        return pack_bal_left;
    }

    public String getPack_name()
    {
        return pack_name;
    }

    public String getUsed_metric()
    {
        return used_metric;
    }

    public String getLeft_metric()
    {
        return left_metric;
    }

    public String getValidity()
    {
        return validity;
    }

    public int getPack_duration_used()
    {
        return pack_duration_used;
    }

    public int getPack_duration_left()
    {
        return pack_duration_left;
    }

    public int getDuration()
    {
        return duration;
    }

    public int getSim_slot()
    {
        return sim_slot;
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

    public String getMessage()
    {
        return message;
    }

    @Override

    public String toString()
    {
        return "PackCallPopup{" +
                "call_cost=" + call_cost +
                ", current_balance=" + current_balance +
                ", call_rate=" + call_rate +
                ", total_spent=" + total_spent +
                ", pack_bal_used=" + pack_bal_used +
                ", pack_bal_left=" + pack_bal_left +
                ", pack_name='" + pack_name + '\'' +
                ", used_metric='" + used_metric + '\'' +
                ", left_metric='" + left_metric + '\'' +
                ", validity='" + validity + '\'' +
                ", pack_duration_used=" + pack_duration_used +
                ", pack_duration_left=" + pack_duration_left +
                ", duration=" + duration +
                ", sim_slot=" + sim_slot +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", carrier_circle='" + carrier_circle + '\'' +
                ", image_uri='" + image_uri + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    int pack_duration_left=0;
    private int duration = 0, sim_slot;
    private String name = "Unkown";
    private String number = "xxxxxxxxxxx";
    private String carrier_circle = "Unkown/Unkown";
    private String image_uri = null;
    private String message = "";



    public boolean isMinsType()
    {
        //its minutes type or it is pack balance type
        if (pack_duration_used == -1 && pack_duration_left == -1)
        {
            return false;
        }
        return true;
    }

    public PackCallPopup(PackCall packCall)
    {

        //there will be  much coupling with the view
        this.pack_duration_used = packCall.pack_duration_used;
        this.pack_duration_left = packCall.pack_duration_left;
        this.used_metric = packCall.used_metric;
        this.left_metric = packCall.left_metric;
        if(used_metric==null && left_metric==null)
            used_metric = left_metric = "secs";
        if(left_metric==null)
            left_metric = used_metric;
        else if(used_metric==null)
            used_metric = left_metric;
        this.pack_name = packCall.pack_name;
        this.validity = packCall.validity;
        this.sim_slot = packCall.sim_slot;
        this.number = packCall.ph_number;
        if(packCall.pack_bal_used>=0.0f)
        {
            this.call_cost = packCall.pack_bal_used;
        }
        else this.call_cost = 0.0f;
        this.current_balance = packCall.main_bal;
        this.call_rate = 1.7f;

        try
        {
            this.call_rate = (packCall.pack_bal_used / packCall.call_duration) * 100;
        } catch (Exception e)
        {

        }
        if(packCall.call_duration>0)
        {
            this.duration = packCall.call_duration;
        }
        this.message = packCall.original_message;
        this.pack_bal_used = packCall.pack_bal_used;
        this.pack_bal_left = packCall.pack_bal_left;
    }
    public void addUserDetails(ContactDetailModel userDetails)
    {
        this.name = userDetails.name;
        this.image_uri = userDetails.image_uri;
        this.carrier_circle = userDetails.carrier + "," + userDetails.circle;
        this.total_spent = userDetails.total_cost;
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
    protected PackCallPopup(Parcel in)
    {
        call_cost = in.readFloat();
        current_balance = in.readFloat();
        call_rate = in.readFloat();
        total_spent = in.readFloat();
        pack_bal_used = in.readFloat();
        pack_bal_left = in.readFloat();
        pack_name = in.readString();
        used_metric = in.readString();
        left_metric = in.readString();
        validity = in.readString();
        pack_duration_used = in.readInt();
        pack_duration_left = in.readInt();
        duration = in.readInt();
        sim_slot = in.readInt();
        name = in.readString();
        number = in.readString();
        carrier_circle = in.readString();
        image_uri = in.readString();
        message = in.readString();
    }

    public static final Creator<PackCallPopup> CREATOR = new Creator<PackCallPopup>()
    {
        @Override
        public PackCallPopup createFromParcel(Parcel in)
        {
            return new PackCallPopup(in);
        }

        @Override
        public PackCallPopup[] newArray(int size)
        {
            return new PackCallPopup[size];
        }
    };
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
        dest.writeFloat(pack_bal_used);
        dest.writeFloat(pack_bal_left);
        dest.writeString(pack_name);
        dest.writeString(used_metric);
        dest.writeString(left_metric);
        dest.writeString(validity);
        dest.writeInt(pack_duration_used);
        dest.writeInt(pack_duration_left);
        dest.writeInt(duration);
        dest.writeInt(sim_slot);
        dest.writeString(name);
        dest.writeString(number);
        dest.writeString(carrier_circle);
        dest.writeString(image_uri);
        dest.writeString(message);
    }
}
