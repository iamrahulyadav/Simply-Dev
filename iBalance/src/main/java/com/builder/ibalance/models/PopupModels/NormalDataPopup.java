package com.builder.ibalance.models.PopupModels;

import android.os.Parcel;
import android.os.Parcelable;

import com.builder.ibalance.models.USSDModels.NormalData;

/**
 * Created by Shabaz on 08-Jan-16.
 */
public class NormalDataPopup implements Parcelable
{
    public float cost, data_used,main_bal;
    public int sim_slot;
    String message="";

    @Override
    public String toString()
    {
        return "NormalDataPopup{" +
                "cost=" + cost +
                ", data_used=" + data_used +
                ", main_bal=" + main_bal +
                ", sim_slot=" + sim_slot +
                ", message='" + message + '\'' +
                '}';
    }

    public NormalDataPopup(NormalData mNormalData)
    {
        this.cost = mNormalData.cost;
        this.data_used = mNormalData.data_used;
        this.main_bal = mNormalData.main_bal;
        this.message = mNormalData.original_message;
        this.sim_slot = mNormalData.sim_slot;
    }


    protected NormalDataPopup(Parcel in)
    {
        cost = in.readFloat();
        data_used = in.readFloat();
        main_bal = in.readFloat();
        sim_slot = in.readInt();
        message = in.readString();
    }
    public static final Creator<NormalDataPopup> CREATOR = new Creator<NormalDataPopup>()
    {
        @Override
        public NormalDataPopup createFromParcel(Parcel in)
        {
            return new NormalDataPopup(in);
        }

        @Override
        public NormalDataPopup[] newArray(int size)
        {
            return new NormalDataPopup[size];
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
        dest.writeFloat(cost);
        dest.writeFloat(data_used);
        dest.writeFloat(main_bal);
        dest.writeInt(sim_slot);
        dest.writeString(message);
    }
}
