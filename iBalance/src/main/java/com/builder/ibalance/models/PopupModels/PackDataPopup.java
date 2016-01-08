package com.builder.ibalance.models.PopupModels;

import android.os.Parcel;
import android.os.Parcelable;

import com.builder.ibalance.models.USSDModels.PackData;

/**
 * Created by Shabaz on 08-Jan-16.
 */
public class PackDataPopup implements Parcelable
{
    public float cost, data_used,main_bal,data_left;
    public int sim_slot;
    String message="",validity=null,pack_type=null;

    public PackDataPopup(PackData packData)
    {
        this.data_used = packData.data_used;
        this.data_left = packData.data_left;
        this.main_bal = packData.main_bal;
        this.validity = packData.validity;
        this.pack_type = packData.pack_type;
        this.sim_slot = packData.sim_slot;
        this.message = packData.original_message;
    }

    protected PackDataPopup(Parcel in)
    {
        cost = in.readFloat();
        data_used = in.readFloat();
        main_bal = in.readFloat();
        data_left = in.readFloat();
        sim_slot = in.readInt();
        message = in.readString();
        validity = in.readString();
        pack_type = in.readString();
    }

    public static final Creator<PackDataPopup> CREATOR = new Creator<PackDataPopup>()
    {
        @Override
        public PackDataPopup createFromParcel(Parcel in)
        {
            return new PackDataPopup(in);
        }

        @Override
        public PackDataPopup[] newArray(int size)
        {
            return new PackDataPopup[size];
        }
    };

    @Override
    public String toString()
    {
        return "PackDataPopup{" +
                "cost=" + cost +
                ", data_used=" + data_used +
                ", main_bal=" + main_bal +
                ", data_left=" + data_left +
                ", sim_slot=" + sim_slot +
                ", message='" + message + '\'' +
                ", validity='" + validity + '\'' +
                ", pack_type='" + pack_type + '\'' +
                '}';
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
        dest.writeFloat(cost);
        dest.writeFloat(data_used);
        dest.writeFloat(main_bal);
        dest.writeFloat(data_left);
        dest.writeInt(sim_slot);
        dest.writeString(message);
        dest.writeString(validity);
        dest.writeString(pack_type);
    }
}
