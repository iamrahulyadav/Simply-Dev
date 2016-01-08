package com.builder.ibalance.models.PopupModels;


import android.os.Parcel;
import android.os.Parcelable;

import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.models.USSDModels.NormalSMS;

/**
 * Created by Shabaz on 07-Jan-16.
 */
public class NormalSmsPopup implements Parcelable
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
    float sms_cost=-1.0f,main_bal=-1.0f;
    String message="";
    public NormalSmsPopup(NormalSMS mNormalSMS)
    {
        this.sms_cost = mNormalSMS.cost;
        this.main_bal = mNormalSMS.main_bal;
        this.number = mNormalSMS.ph_number;
        this.sim_slot = mNormalSMS.sim_slot;
        this.message = mNormalSMS.original_message;
    }

    @Override
    public String toString()
    {
        return "NormalSmsPopup{" +
                "total_spent=" + total_spent +
                ", name='" + name + '\'' +
                ", carrier_circle='" + carrier_circle + '\'' +
                ", image_uri='" + image_uri + '\'' +
                ", number='" + number + '\'' +
                ", sim_slot=" + sim_slot +
                ", sms_cost=" + sms_cost +
                ", main_bal=" + main_bal +
                ", message='" + message + '\'' +
                '}';
    }

    public void addUserDetails(ContactDetailModel userDetails)
    {
        this.name = userDetails.name;
        this.image_uri = userDetails.image_uri;
        this.carrier_circle = userDetails.carrier + "," + userDetails.circle;
        this.total_spent = userDetails.total_cost;
    }

    protected NormalSmsPopup(Parcel in)
    {
        total_spent = in.readFloat();
        name = in.readString();
        carrier_circle = in.readString();
        image_uri = in.readString();
        number = in.readString();
        sim_slot = in.readInt();
        sms_cost = in.readFloat();
        main_bal = in.readFloat();
        message = in.readString();
    }

    public static final Creator<NormalSmsPopup> CREATOR = new Creator<NormalSmsPopup>()
    {
        @Override
        public NormalSmsPopup createFromParcel(Parcel in)
        {
            return new NormalSmsPopup(in);
        }

        @Override
        public NormalSmsPopup[] newArray(int size)
        {
            return new NormalSmsPopup[size];
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
        dest.writeFloat(total_spent);
        dest.writeString(name);
        dest.writeString(carrier_circle);
        dest.writeString(image_uri);
        dest.writeString(number);
        dest.writeInt(sim_slot);
        dest.writeFloat(sms_cost);
        dest.writeFloat(main_bal);
        dest.writeString(message);
    }
}
