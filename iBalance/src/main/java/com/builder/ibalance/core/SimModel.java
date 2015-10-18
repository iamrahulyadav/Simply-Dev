package com.builder.ibalance.core;

import java.util.ArrayList;

/**
 * Created by Shabaz on 7/29/2015.
 */
public class SimModel {

    /*srno -"
            " imei -"
            " sopcode - "
            " sopname - "
            " nopcode - "
            " nopname - "
            " op - "
            " subid - "
            " simSlot - "
            " dualType - "
            " supported - "*/
    public String serial;
    public String imei;
    public String sim_imsi;
    public String network_imsi;
    public String carrier;
    public String carrier_display_name;

    public String getCircle()
    {
        return circle;
    }

    public String getSubscriber_id()
    {
        return subscriber_id;
    }

    public static ArrayList<String> getCall_log_columns()
    {
        return call_log_columns;
    }

    public static boolean isUses_subscription()
    {
        return uses_subscription;
    }

    public static String getDebugInfo()
    {
        return debugInfo;
    }

    public String circle;
    public String subscriber_id;
       public     long subid;
    public static ArrayList<String> call_log_columns = new ArrayList<>();
    int simslot =0;
    public static boolean uses_subscription =false;
    public static int dual_type = 0;//unknown
    public static boolean two_slots = true; // has two slots or not
    public static int getDual_type()
    {
        return dual_type;
    }

    public static void setDual_type(int dual_type)
    {
        SimModel.dual_type = dual_type;
    }

    public static boolean isTwo_slots()
    {
        return two_slots;
    }

    public static void setTwo_slots(boolean two_slots)
    {
        SimModel.two_slots = two_slots;
    }

    public static String debugInfo = "Empty";

    public SimModel(Builder mBuilder)
    {
        this.serial = mBuilder.serial;
        this.imei = mBuilder.imei;
        this.sim_imsi = mBuilder.sim_imsi;
        this.network_imsi = mBuilder.network_imsi;
        this.carrier = mBuilder.carrier;
        this.carrier_display_name = mBuilder.carrier_display_name;
        this.subid = mBuilder.subid;
        this.simslot = mBuilder.simslot;
        this.circle = mBuilder.circle;
        this.subscriber_id = mBuilder.subscriber_id;
    }
    public SimModel(String serial, String imei, String sim_imsi, String network_imsi, String carrier,String circle, String carrier_display_name, long subid, int simslot )
    {
        this.serial = serial;
        this.imei = imei;
        this.sim_imsi = sim_imsi;
        this.network_imsi = network_imsi;
        this.carrier = carrier;
        this.carrier_display_name = carrier_display_name;
        this.subid = subid;
        this.simslot = simslot;
        this.circle = circle;
    }
    public static class Builder
    {


        //Mandatory
        private long subid;
        private int simslot;
        private String network_imsi;
        private String carrier;

        //optional
        private String serial = "";
        private String imei = "";
        private String sim_imsi = "";
        private String carrier_display_name = "";
        private String circle ="";
        private String subscriber_id ="";

        public Builder(long subid, int simslot, String sim_imsi, String carrier)
        {
            this.subid = subid;
            this.simslot = simslot;
            this.sim_imsi = sim_imsi;
            this.carrier = carrier;
        }

        public Builder serial(String serial)
        {
            this.serial= serial;
            return this;
        }

        public Builder imei(String imei)
        {
            this.imei= imei;
            return this;
        }
        public Builder network_imsi(String sim_imsi)
        {
            this.network_imsi= network_imsi;
            return this;
        }
        public Builder circle(String circle)
        {
            this.circle= circle;
            return this;
        }
        public Builder subscriber_id(String subscriber_id)
        {
            this.subscriber_id= subscriber_id;
            return this;
        }
        public Builder carrier_display_name(String carrier_display_name)
        {
            this.carrier_display_name= carrier_display_name;
            return this;
        }
        public SimModel build()
        {
            return new SimModel(this);
        }

    }
    public String getSerial()
    {
        return serial;
    }

    public String getImei()
    {
        return imei;
    }

    public String getSim_imsi()
    {
        return sim_imsi;
    }

    public String getNetwork_imsi()
    {
        return network_imsi;
    }

    public String getCarrier()
    {
        return carrier;
    }

    public String getCarrier_display_name()
    {
        return carrier_display_name;
    }

    public long getSubid()
    {
        return subid;
    }

    public int getSimslot()
    {
        return simslot;
    }
    String getDualTypeName(int dual_type)
    {
        switch (dual_type)
        {
            case 0:
                return "TYPE_UNKNOWN";
            case 1:
                return "TYPE_LOLIPOP";
            case 2:
                return "TYPE_MEDIATEK";
            case 3:
                return "TYPE_GEMINI";
            case 4:
                return "TYPE_MOTO";
            case 5:
                return "TYPE_LG";
            case 6:
                return "TYPE_ASUS";
            case 7:
                return "TYPE_XIAOMI";
            case 8:
                return "TYPE_KARBONN";
            case 9:
                return "TYPE_NEXUS";
            case 10:
                return "TYPE_DUOS_ONE";
            case 11:
                return "TYPE_DUOS_TWO";
            case 12:
                return "TYPE_DUOS_DS";
            case 13:
                return "TYPE_SAMSUNG_RIL";
            case 14:
                return "TYPE_SUBSCRIPTION";
            case 15:
                return "TYPE_MICROMAX_BOLT";
            case 16:
                return "TYPE_OTHERS";
            case 17:
                return "TYPE_SINGLE_SIM";
            default: return "TYPE_UNKNOWN";
        }
    }

    @Override
    public String toString()
    {
        return "SimModel{" +
                "\nserial='" + serial + '\'' +
                "\n imei='" + imei + '\'' +
                "\n sim_imsi='" + sim_imsi + '\'' +
                "\n network_imsi='" + network_imsi + '\'' +
                "\n carrier='" + carrier + '\'' +
                "\n Circle='" + circle + '\'' +
                "\n carrier_display_name='" + carrier_display_name + '\'' +
                "\n subid=" + subid +
                "\n simslot=" + simslot +
                "\n SubscriberId=" + subscriber_id +
                "\n dualtype = " + getDualTypeName( dual_type) +
                "\n HasTwoSlots = " + two_slots +
                "\n CallLogColumnName = " + call_log_columns.toString()+
                "}\n-------------------------------------------------------------------";
    }
}

