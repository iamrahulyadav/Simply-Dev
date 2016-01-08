package com.builder.ibalance.models.PopupModels;

import android.os.Parcel;
import android.os.Parcelable;

import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.models.USSDModels.PackSMS;

/**
 * Created by Shabaz on 07-Jan-16.
 */
public class PackSmsPopup implements Parcelable
{
    //UserDetails
    private float total_spent = 0.0f;
    private String name = "Unkown";
    private String carrier_circle = "Unkown/Unkown";
    private String image_uri = null;
    //event details
    private String number = "xxxxxxxxxxx";
    int sim_slot=0;
    //USSD Details
    float sms_cost=-1.0f,main_bal=-1.0f,used_sms,left_sms;
    String message="",validity=null,pack_type=null;
    public PackSmsPopup(PackSMS mPackSMS)
    {
        this.sms_cost = 0.0f;
        this.main_bal = mPackSMS.main_bal;
        this.used_sms = mPackSMS.used_sms;
        this.left_sms = mPackSMS.rem_sms;
        this.validity = mPackSMS.validity;
        this.pack_type = mPackSMS.pack_type;
        number = mPackSMS.ph_number;
        this.sim_slot = mPackSMS.sim_slot;
        this.message = mPackSMS.original_message;
    }

    protected PackSmsPopup(Parcel in)
    {
        total_spent = in.readFloat();
        name = in.readString();
        carrier_circle = in.readString();
        image_uri = in.readString();
        number = in.readString();
        sim_slot = in.readInt();
        sms_cost = in.readFloat();
        main_bal = in.readFloat();
        used_sms = in.readFloat();
        left_sms = in.readFloat();
        message = in.readString();
        validity = in.readString();
        pack_type = in.readString();
    }

    public void addUserDetails(ContactDetailModel userDetails)
    {
        this.name = userDetails.name;
        this.image_uri = userDetails.image_uri;
        this.carrier_circle = userDetails.carrier + "," + userDetails.circle;
        this.total_spent = userDetails.total_cost;
    }

    public static final Creator<PackSmsPopup> CREATOR = new Creator<PackSmsPopup>()
    {
        @Override
        public PackSmsPopup createFromParcel(Parcel in)
        {
            return new PackSmsPopup(in);
        }

        @Override
        public PackSmsPopup[] newArray(int size)
        {
            return new PackSmsPopup[size];
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

    @Override
    public String toString()
    {
        return "PackSmsPopup{" +
                "total_spent=" + total_spent +
                ", name='" + name + '\'' +
                ", carrier_circle='" + carrier_circle + '\'' +
                ", image_uri='" + image_uri + '\'' +
                ", number='" + number + '\'' +
                ", sim_slot=" + sim_slot +
                ", sms_cost=" + sms_cost +
                ", main_bal=" + main_bal +
                ", used_sms=" + used_sms +
                ", left_sms=" + left_sms +
                ", message='" + message + '\'' +
                ", validity='" + validity + '\'' +
                ", pack_type='" + pack_type + '\'' +
                '}';
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
        dest.writeFloat(total_spent);
        dest.writeString(name);
        dest.writeString(carrier_circle);
        dest.writeString(image_uri);
        dest.writeString(number);
        dest.writeInt(sim_slot);
        dest.writeFloat(sms_cost);
        dest.writeFloat(main_bal);
        dest.writeFloat(used_sms);
        dest.writeFloat(left_sms);
        dest.writeString(message);
        dest.writeString(validity);
        dest.writeString(pack_type);
    }

    public float getTotal_spent()
    {
        return total_spent;
    }

    public String getName()
    {
        return name;
    }

    public String getCarrier_circle()
    {
        return carrier_circle;
    }

    public String getImage_uri()
    {
        return image_uri;
    }

    public String getNumber()
    {
        return number;
    }

    public int getSim_slot()
    {
        return sim_slot;
    }

    public float getSms_cost()
    {
        return sms_cost;
    }

    public float getMain_bal()
    {
        return main_bal;
    }

    public float getUsed_sms()
    {
        return used_sms;
    }

    public float getLeft_sms()
    {
        return left_sms;
    }

    public String getMessage()
    {
        return message;
    }

    public String getValidity()
    {
        return validity;
    }

    public String getPack_type()
    {
        return pack_type;
    }
}
