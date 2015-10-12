package com.builder.ibalance.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.builder.ibalance.database.helpers.IMSIHelper;
import com.builder.ibalance.util.Constants;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.Helper.SharedPreferenceHelper;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.ReflectionHelper;
import com.mediatek.telephony.TelephonyManagerEx;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.builder.ibalance.util.Helper.toastHelper;

/**
 * Created by Shabaz on 7/29/2015.
 */
public class DualSim
{
    final String tag = DualSim.class.getSimpleName();
    IMSIHelper imsiHelper;
    ArrayList<SimModel> sim_list = new ArrayList<>();//will get updated in functions
    TelephonyManager mTelephonyManager;
    Class<?> telephonyClass;
    SharedPreferenceHelper mSharedPreferenceHelper = new SharedPreferenceHelper();
    String[] knownSerial = new String[]{"-1","-1"};
    boolean sim_details_known = false;

    public ArrayList<SimModel> getSimList(int type) //provide a short cut if already sim details are fetched
    {
        checkDualSim();
        if (mTelephonyManager == null)
            mTelephonyManager = (TelephonyManager) MyApplication.context.getSystemService(Context.TELEPHONY_SERVICE);
        if (type == 0)//don't know the type start afresh
        {
            toastHelper("Detecting Fresh");
            sim_details_known = false; //details no yet known
            sim_list.clear();
            Log.d(tag, "getSimList");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) //API 22,Yay Dual Sim Support
            {
                Log.d(tag, "getSimListForLolipop");
                SimModel.dual_type = Constants.TYPE_LOLIPOP;
                getSimListForLolipop(sim_details_known);

            }
            else if (hasSubscriptionManager())
            {
                SimModel.dual_type = Constants.TYPE_SUBSCRIPTION;
                getSimListForSubscriptionManager(sim_details_known);
                //java.util.List<android.telephony.SubInfoRecord> android.telephony.SubscriptionManager.getSubInfoUsingSlotId(int)
            }
            else if (com.mediatek.compatibility.gemini.GeminiSupport.isGeminiFeatureEnabled())
            {
                SimModel.dual_type = Constants.TYPE_MEDIATEK;
                getSimListforMediaTek(sim_details_known);

            } else if (isTypeGemini())
            {
                SimModel.dual_type = Constants.TYPE_GEMINI;
                getSimListForGemini(sim_details_known);

            } else if (isTypeDuosOne())
            {
                SimModel.dual_type = Constants.TYPE_DUOS_ONE;
                getSimListForDuosoOne(sim_details_known);

            } else if (isTypeDuosTwo())
            {
                SimModel.dual_type = Constants.TYPE_DUOS_TWO;
                getSimListForDuosTwo(sim_details_known);

            } else if (isTypeDuosDs())
            {
                SimModel.dual_type = Constants.TYPE_DUOS_DS;
                getSimListForDuosDs(sim_details_known);

            } else if (isTypeNexus())
            {
                SimModel.dual_type = Constants.TYPE_NEXUS;
                getSimListForNexus(sim_details_known);

            } else if (isTypeKarbonn())
            {
                SimModel.dual_type = Constants.TYPE_KARBONN;
                getSimListForKarbonn(sim_details_known);

            } else if (isTypeXiaomi())
            {
                SimModel.dual_type = Constants.TYPE_XIAOMI;
                getSimListForXiaomi(sim_details_known);

            } else if (isTypeAsus())
            {
                SimModel.dual_type = Constants.TYPE_ASUS;
                getSimListForAsus(sim_details_known);

            } else if (isTypeMoto())
            {
                SimModel.dual_type = Constants.TYPE_MOTO;
                getSimListForMoto(sim_details_known);

            }
            else if(isTypeSamsungRIL())
            {
                SimModel.dual_type = Constants.TYPE_SAMSUNG_RIL;
                getSimListForSamsungRIL(sim_details_known);

            }
            else if(isTypeMicromaxBolt())
            {
                SimModel.dual_type = Constants.TYPE_MICROMAX_BOLT;
                getSimListForMicromaxBolt(sim_details_known);
            }
            if(sim_list.isEmpty() || SimModel.dual_type == Constants.TYPE_UNKNOWN)
            {
                //Consider this as Single Sim
                SimModel.dual_type = Constants.TYPE_SINGLE_SIM;
                getSimListForSingleSim(sim_details_known);

            }

        }
        // If it is already known then take a short cut just check if Sim Serials are changed
        else
        {
            sim_list = mSharedPreferenceHelper.getDualSimDetails();
            if(sim_list!=null)
            {
                for(SimModel model:sim_list)
                {
                    if(model.getSimslot() == 0)
                        knownSerial[0] = model.getSerial();
                    else
                        knownSerial[1] = model.getSerial();
                }
                sim_details_known = true;
            }
            else
            {
                sim_details_known = false;
            }
            toastHelper("Detecting from prefernces "+ type);
            switch (type)
            {

                case Constants.TYPE_UNKNOWN:
                    getSimList(0);
                    break;
                case Constants.TYPE_LOLIPOP:
                    getSimListForLolipop(sim_details_known);
                    break;
                case Constants.TYPE_MEDIATEK:
                    getSimListforMediaTek(sim_details_known);
                    break;
                case Constants.TYPE_GEMINI:
                    getSimListForGemini(sim_details_known);
                    break;
                case Constants.TYPE_MOTO:
                    getSimListForMoto(sim_details_known);
                    break;
                case Constants.TYPE_LG:
                    //TODO : LG Dual Sim Implementation
                    break;
                case Constants.TYPE_ASUS:
                    getSimListForAsus(sim_details_known);
                    break;
                case Constants.TYPE_XIAOMI:
                    getSimListForXiaomi(sim_details_known);
                    break;
                case Constants.TYPE_KARBONN:
                    getSimListForKarbonn(sim_details_known);
                    break;
                case Constants.TYPE_NEXUS:
                    getSimListForNexus(sim_details_known);
                    break;
                case Constants.TYPE_DUOS_ONE:
                    getSimListForDuosoOne(sim_details_known);
                    break;
                case Constants.TYPE_DUOS_TWO:
                    getSimListForDuosTwo(sim_details_known);
                    break;
                case Constants.TYPE_DUOS_DS:
                    getSimListForDuosDs(sim_details_known);
                    break;
                case Constants.TYPE_SAMSUNG_RIL:
                    getSimListForSamsungRIL(sim_details_known);
                    break;
                case Constants.TYPE_SUBSCRIPTION:
                    getSimListForSubscriptionManager(sim_details_known);
                    break;
                case Constants.TYPE_MICROMAX_BOLT:
                    getSimListForMicromaxBolt(sim_details_known);
                    break;
                case Constants.TYPE_OTHERS:
                    //TODO ????? TYPE _OTHERS
                    break;
                case Constants.TYPE_SINGLE_SIM:
                    getSimListForSingleSim(sim_details_known);
                    break;
                default:
                    getSimList(0);
            }

        }


        return sim_list;
    }

    private boolean isTypeMicromaxBolt()
    {
        Object[] parameterType = new Object[1];
        parameterType[0] = Integer.valueOf(0);
        Object custom_object = ReflectionHelper.getObject(null, "android.telephony.TelephonyManager", "getDefault", parameterType);
        if (custom_object instanceof TelephonyManager) //if not an instance
        {
            return true;
        }
        return false;
    }

    private void getSimListForMicromaxBolt(boolean sim_details_known)
    {

        String serial0 = null, imei, sim_imsi, network_imsi, carrier, carrier_display_name, circle;
        String serial1 = null;
        SimModel mSimModel;
        ArrayList<String> carrier_circle;
        Object[] parameters = new Object[1];
        if (sim_details_known) //already known then just check serials
        {
            parameters[0] = 0;
            Object dualTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.TelephonyManager", "getDefault", parameters);
            Object[] empty_parameter = new Object[0];
            serial0 = (String) ReflectionHelper.getObject(dualTelephonyManager, "android.telephony.TelephonyManager", "getSimSerialNumber", empty_parameter);
            if (hasChanged(serial0, 0))
                getSimList(0);
            parameters[0] = 1;
            dualTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.TelephonyManager", "getDefault", parameters);
            serial1 = (String) ReflectionHelper.getObject(dualTelephonyManager, "android.telephony.TelephonyManager", "getSimSerialNumber", empty_parameter);
            if (hasChanged(serial1, 1))
                getSimList(0);
            return; // Everything as it was eaelier

        }
        parameters[0] = 0;
        Object dualTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.TelephonyManager", "getDefault", parameters);
        if (dualTelephonyManager != null)
        {
            Object[] empty_parameter = new Object[0];
            serial0 = (String) ReflectionHelper.getObject(dualTelephonyManager, "android.telephony.TelephonyManager", "getSimSerialNumber", empty_parameter);
            if (serial0 != null && !serial0.equals(""))
            {
                imei = (String) ReflectionHelper.getObject(dualTelephonyManager, "android.telephony.TelephonyManager", "getDeviceId", empty_parameter);
                network_imsi = (String) ReflectionHelper.getObject(dualTelephonyManager, "android.telephony.TelephonyManager", "getNetworkOperator", empty_parameter);
                sim_imsi = (String) ReflectionHelper.getObject(dualTelephonyManager, "android.telephony.TelephonyManager", "getSimOperator", empty_parameter);
                carrier_display_name = (String) ReflectionHelper.getObject(dualTelephonyManager, "android.telephony.TelephonyManager", "getNetworkOperatorName", empty_parameter);
                if (network_imsi == null) //Network IMSi might not be available when the sim state is not ready
                    network_imsi = sim_imsi;
                carrier_circle = getCarrierCirclefromIMSI(sim_imsi);
                if (carrier_circle == null)
                {
                    getCarrierCircleManually();
                }
                carrier = carrier_circle.get(0);
                circle = carrier_circle.get(1);
                mSimModel = new SimModel.Builder(-1, 0, sim_imsi, carrier)
                        .serial(serial0)
                        .imei(imei)
                        .carrier_display_name(carrier_display_name)
                        .circle(circle)
                        .network_imsi(network_imsi)
                        .build();
                sim_list.add(mSimModel);
            }
        }
        //for sim 2
        parameters[0] = 1;
        dualTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.TelephonyManager", "getDefault", parameters);
        if (dualTelephonyManager != null)
        {
            Object[] empty_parameter = new Object[0];
            imei = (String) ReflectionHelper.getObject(dualTelephonyManager, "android.telephony.TelephonyManager", "getDeviceId", empty_parameter);
            if (imei == null)
            {
                SimModel.two_slots = false;
                //If there is only one slot then there is no need for checking Sim Serial
                toastHelper("Single Sim" + imei);
            } else
            {
                serial1 = (String) ReflectionHelper.getObject(dualTelephonyManager, "android.telephony.TelephonyManager", "getSimSerialNumber", empty_parameter);
                if (serial1 != null && !serial1.equals(""))
                {

                    network_imsi = (String) ReflectionHelper.getObject(dualTelephonyManager, "android.telephony.TelephonyManager", "getNetworkOperator", empty_parameter);
                    sim_imsi = (String) ReflectionHelper.getObject(dualTelephonyManager, "android.telephony.TelephonyManager", "getSimOperator", empty_parameter);
                    carrier_display_name = (String) ReflectionHelper.getObject(dualTelephonyManager, "android.telephony.TelephonyManager", "getNetworkOperatorName", empty_parameter);
                    if (network_imsi == null) //Network IMSi might not be available when the sim state is not ready
                        network_imsi = sim_imsi;
                    carrier_circle = getCarrierCirclefromIMSI(sim_imsi);
                    if (carrier_circle == null)
                    {
                        getCarrierCircleManually();
                    }
                    carrier = carrier_circle.get(0);
                    circle = carrier_circle.get(1);
                    mSimModel = new SimModel.Builder(-1, 1, sim_imsi, carrier)
                            .serial(serial1)
                            .imei(imei)
                            .carrier_display_name(carrier_display_name)
                            .circle(circle)
                            .network_imsi(network_imsi)
                            .build();
                    sim_list.add(mSimModel);
                }
            }
        }
        if (serial0 == null || serial1 == null)
            Constants.IS_SINGLE_SIM = true;
        else
        {
            Constants.IS_SINGLE_SIM = false;
        }
        if (serial0 == null && serial1 == null) //BBoth sim serials are null Sims might not be inserted
        {
            SimModel.dual_type = Constants.TYPE_UNKNOWN;
        }
    }



    private void getSimListForSamsungRIL(boolean sim_details_known)
    {
        String serial0 = null, imei, sim_imsi, network_imsi, carrier, carrier_display_name, circle;
        String serial1 = null;
        SimModel mSimModel;
        ArrayList<String> carrier_circle;
        Class<Enum> rilClass = null;//telephony.getClass();
        try
        {
            //For Sure no
            rilClass = (Class<Enum>) Class.forName("com.android.internal.telephony.RILConstants$SimCardID");
        } catch (ClassNotFoundException e)
        {
            // Will Not Happen/ Must Not Happen.
        }
        String sim_id_str = "ID_ZERO";
        Object[] parameters = new Object[1];
        parameters[0] =rilClass;
        Method getTeleDefault = ReflectionHelper.getMethod(null, "android.telephony.TelephonyManager", "getDefault", parameters);
        parameters[0] = Enum.valueOf(rilClass, sim_id_str);
        Object multiSimTelephonyManager = null;
        try
        {
            multiSimTelephonyManager = getTeleDefault.invoke(null,parameters);
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        } catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
        if(sim_details_known) //already known then just check serials
        {
            if(multiSimTelephonyManager==null && !knownSerial[0].equals("-1"))
            {
                getSimList(0);
            }
            Object[] empty_parameter = new Object[0];
            serial0 = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getSimSerialNumber", empty_parameter);
            if(hasChanged(serial0,0))
                getSimList(0);
            sim_id_str = "ID_ONE";
            parameters[0] =rilClass;
            getTeleDefault = ReflectionHelper.getMethod(null, "android.telephony.TelephonyManager", "getDefault", parameters);
            parameters[0] = Enum.valueOf(rilClass, sim_id_str);
            multiSimTelephonyManager = null;
            try
            {
                multiSimTelephonyManager = getTeleDefault.invoke(null,parameters);
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            } catch (InvocationTargetException e)
            {
                e.printStackTrace();
            }
            if(multiSimTelephonyManager==null && !knownSerial[1].equals("-1"))
            {
                getSimList(0);
            }
            serial1 = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getSimSerialNumber", empty_parameter);
            if(hasChanged(serial1,1))
                getSimList(0);
            return; // Everything as it was eaelier

        }
        if (multiSimTelephonyManager != null)
        {
            Object[] empty_parameter = new Object[0];
            serial0 = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getSimSerialNumber", empty_parameter);
            if (serial0 != null && !serial0.isEmpty())
            {
                imei = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getDeviceId", empty_parameter);
                network_imsi = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getNetworkOperator", empty_parameter);
                sim_imsi = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getSimOperator", empty_parameter);
                carrier_display_name = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getNetworkOperatorName", empty_parameter);
                if (network_imsi == null) //Network IMSi might not be available when the sim state is not ready
                    network_imsi = sim_imsi;
                carrier_circle = getCarrierCirclefromIMSI(network_imsi);
                if (carrier_circle == null)
                {
                    getCarrierCircleManually();
                }
                carrier = carrier_circle.get(0);
                circle = carrier_circle.get(1);
                mSimModel = new SimModel.Builder(-1, 0, sim_imsi, carrier)
                        .serial(serial0)
                        .imei(imei)
                        .carrier_display_name(carrier_display_name)
                        .circle(circle)
                        .network_imsi(network_imsi)
                        .build();
                sim_list.add(mSimModel);
            }
        }
        //for sim 2
        sim_id_str = "ID_ONE";
        parameters[0] =rilClass;
        getTeleDefault = ReflectionHelper.getMethod(null, "android.telephony.TelephonyManager", "getDefault", parameters);
        parameters[0] = Enum.valueOf(rilClass, sim_id_str);
        multiSimTelephonyManager = null;
        try
        {
            multiSimTelephonyManager = getTeleDefault.invoke(null,parameters);
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        } catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
        if (multiSimTelephonyManager != null)
        {
            Object[] empty_parameter = new Object[0];
            imei = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getDeviceId", empty_parameter);
            if (imei == null)
            {
                SimModel.two_slots = false;
                //If there is only one slot then there is no need for checking Sim Serial
                toastHelper("Has Single  Slot" + imei);
            } else
            {
                serial1 = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getSimSerialNumber", empty_parameter);
                if (serial1 != null && !serial1.isEmpty())
                {
                    imei = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getDeviceId", empty_parameter);
                    network_imsi = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getNetworkOperator", empty_parameter);
                    sim_imsi = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getSimOperator", empty_parameter);
                    carrier_display_name = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getNetworkOperatorName", empty_parameter);
                    if (network_imsi == null) //Network IMSi might not be available when the sim state is not ready
                        network_imsi = sim_imsi;
                    carrier_circle = getCarrierCirclefromIMSI(network_imsi);
                    if (carrier_circle == null)
                    {
                        getCarrierCircleManually();
                    }
                    carrier = carrier_circle.get(0);
                    circle = carrier_circle.get(1);
                    mSimModel = new SimModel.Builder(-1, 1, sim_imsi, carrier)
                            .serial(serial1)
                            .imei(imei)
                            .carrier_display_name(carrier_display_name)
                            .circle(circle)
                            .network_imsi(network_imsi)
                            .build();
                    sim_list.add(mSimModel);
                }
            }
        }

        if (serial0 == null || serial1 == null)
            Constants.IS_SINGLE_SIM = true;
        else
        {
            Constants.IS_SINGLE_SIM = false;
        }
        if (serial0 == null && serial1 == null) //BBoth sim serials are null Sims might not be inserted
        {
            SimModel.dual_type = Constants.TYPE_UNKNOWN;
        }

    }

    private boolean isTypeSamsungRIL()
    {

        try
        {
            Class rilClass =Class.forName("com.android.internal.telephony.RILConstants$SimCardID");
            if(ReflectionHelper.methodCheck(null,"android.telephony.TelephonyManager","getDefault",rilClass))
            {
                return true;
            }
            else
            {
                return false;
            }

        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private boolean hasSubscriptionManager()
    {
        return ReflectionHelper.methodCheck(null, "android.telephony.SubscriptionManager", "getActiveSubInfoList", new Class[0]);
    }

    private void getSimListForSubscriptionManager(boolean sim_details_known)
    {
        Log.d(tag, "Function getSimListForSubscriptionManager");
        Object sim[] = new Object[3]; //just to be safe
        //        SimModel.two_slots = true;
        SimModel.uses_subscription = true;
        Constants.IS_SINGLE_SIM = false;
        Class subInfoRecord = null;
        try
        {
            subInfoRecord = Class.forName("android.telephony.SubInfoRecord");
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        Class<?> sunInfoRecObj = subInfoRecord.getComponentType();
        ArrayList<?> subInfoList = new ArrayList<>();

        subInfoList = (ArrayList<?>)ReflectionHelper.getObject(null, "android.telephony.SubscriptionManager", "getActiveSubInfoList", new Class[0]);
        Log.d(tag,"subInfoList = "+subInfoList.toString());
        if(sim_details_known) //already known then just check serials
        {
            for(int i=0;i<subInfoList.size();i++)
            {
                sim[i] = subInfoList.get(i);
                Field iccField = null;
                try
                {
                    iccField = subInfoRecord.getField("iccId");
                } catch (NoSuchFieldException e)
                {
                    e.printStackTrace();
                }
                String serial = null;
                if(iccField!=null)
                {
                    try
                    {
                        serial = (String) iccField.get(sim[i]);
                    } catch (IllegalAccessException e)
                    {
                        e.printStackTrace();
                    }
                }
                if(!knownSerial[i].equals(serial))
                    getSimList(0);
            }

            return; //Everyrhing is as it was earlier
        }
        if(subInfoList.size()<2)
            Constants.IS_SINGLE_SIM = true;
        for(int i=0;i<subInfoList.size();i++)
        {

            //
            sim[i] = subInfoList.get(i);
            Log.d(tag,"Sub "+i+" ="+sim[i].toString());
            Field iccField =null,subField=null,slotField=null,mccField=null,mncField=null,display_carier_field = null;
            String serial=null,sim_imsi=null,network_imsi=null,imei=null,display_carier = null;
            long subsciption = 1;
            int slot = i;
            try
            {
                iccField = subInfoRecord.getField("iccId");
                iccField.setAccessible(true);
                subField = subInfoRecord.getField("subId");
                subField.setAccessible(true);
                slotField = subInfoRecord.getField("slotId");
                slotField.setAccessible(true);;
                mncField = subInfoRecord.getField("mnc");
                mncField.setAccessible(true);
                mccField = subInfoRecord.getField("mcc");
                mccField.setAccessible(true);
                display_carier_field = subInfoRecord.getField("displayName");
                display_carier_field.setAccessible(true);
            } catch (NoSuchFieldException e)
            {
                Log.d(tag,"Field Notavailable");
                e.printStackTrace();
            }
            try
            {
                serial = (String) iccField.get(sim[i]);
                subsciption = (long) subField.get(sim[i]);
                slot = (int)slotField.get(sim[i]);
                int mnc,mcc;
                mnc = (int) mncField.get(sim[i]);
                mcc = (int) mccField.get(sim[i]);
                Object[] param = new Object[1];
                param[0] = Long.valueOf(subsciption);
                sim_imsi = network_imsi = (String) ReflectionHelper.getObject(mTelephonyManager,null,"getSimOperator",param);// mTelephonyManager.getSimOperator(subsciption); //mcc+"" + mnc;
                display_carier = (String) display_carier_field.get(sim[i]);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            ArrayList<String> carrier_circle = getCarrierCirclefromIMSI(sim_imsi);
            SimModel mSimModel = new SimModel.Builder(subsciption,slot, sim_imsi, carrier_circle.get(0))
                    .network_imsi(network_imsi)
                    .carrier_display_name(display_carier)
                    .circle(carrier_circle.get(1))
                    .imei("")//skip for 5.1
                    .serial(serial)
                    .build();
            sim_list.add(mSimModel);

        }
        if (sim_list.isEmpty())
        {
            SimModel.dual_type = Constants.TYPE_UNKNOWN;
            sim_list.clear();
            Log.d(tag, "Network Not Available");
        }

    }
    private boolean isTypeGemini()
    {
        return ReflectionHelper.methodCheck(mTelephonyManager, "android.telephony.TelephonyManager", "getSimSerialNumberGemini", int.class);
    }

    private void getSimListForGemini(boolean sim_details_known)
    {
        String serial[] = new String[2], imei, sim_imsi, network_imsi, carrier, carrier_display_name, circle;
        SimModel mSimModel;
        ArrayList<String> carrier_circle;
        if (mTelephonyManager == null)
            mTelephonyManager = (TelephonyManager) MyApplication.context.getSystemService(Context.TELEPHONY_SERVICE);
        Object[] parameter = new Object[1];
        if(sim_details_known) //already known then just check serials
        {
            parameter[0] = 0;
            serial[0] = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getSimSerialNumberGemini", parameter);
            if(hasChanged(serial[0],0))
                getSimList(0);
            parameter[0] = 1;
            serial[1] = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getSimSerialNumberGemini", parameter);

            if(hasChanged(serial[1],1))
                getSimList(0);
            return; // Everything as it was eaelier

        }
        for (int i = 0; i < 2; i++)
        {
            parameter[0] = i;
            imei = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getDeviceIdGemini", parameter);
            if (imei == null)
            {
                SimModel.two_slots = false;
                //If there is only one slot then there is no need for checking Sim Serial
                toastHelper("Single Sim" + imei);
            } else
            {
                serial[i] = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getSimSerialNumberGemini", parameter);
                if (serial[i] != null)
                {
                    network_imsi = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getNetworkOperatorGemini", parameter);
                    sim_imsi = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getSimOperatorGemini", parameter);
                    carrier_display_name = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getNetworkOperatorNameGemini", parameter);
                    if (network_imsi == null) //Network IMSi might not be available when the sim state is not ready
                        network_imsi = sim_imsi;
                    carrier_circle = getCarrierCirclefromIMSI(network_imsi);
                    if (carrier_circle == null)
                    {
                        carrier_circle = getCarrierCircleManually();
                    }
                    carrier = carrier_circle.get(0);
                    circle = carrier_circle.get(1);
                    mSimModel = new SimModel.Builder(-1, i, sim_imsi, carrier)
                            .serial(serial[i])
                            .imei(imei)
                            .carrier_display_name(carrier_display_name)
                            .circle(circle)
                            .network_imsi(network_imsi)
                            .build();
                    sim_list.add(mSimModel);
                }
            }
        }
        if (serial[0] == null || serial[1] == null)
            Constants.IS_SINGLE_SIM = true;
        else
        {
            Constants.IS_SINGLE_SIM = false;
        }
        if (serial[0] == null && serial[1] == null) //BBoth sim serials are null Sims might not be inserted
        {
            SimModel.dual_type = Constants.TYPE_UNKNOWN;
        }
    }

    private boolean isTypeKarbonn()
    {
        if (ReflectionHelper.methodCheck(null, "android.telephony.TelephonyManager", "getDeviceId", int.class) &&
                ReflectionHelper.methodCheck(null, "android.telephony.TelephonyManager", "getSimSerialNumber", int.class) &&
                ReflectionHelper.methodCheck(null, "android.telephony.TelephonyManager", "getSimOperator", int.class)
                )
            return true;
        else
            return false;
    }

    private void getSimListForKarbonn(boolean sim_details_known)
    {
        String serial0, imei, sim_imsi, network_imsi, carrier, carrier_display_name, circle;
        String serial1 = null;
        SimModel mSimModel;
        ArrayList<String> carrier_circle;
        if (mTelephonyManager == null)
            mTelephonyManager = (TelephonyManager) MyApplication.context.getSystemService(Context.TELEPHONY_SERVICE);
        Object[] parameter = new Object[1];
        if(sim_details_known) //already known then just check serials
        {
            parameter[0] = 0;
            serial0 = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getSimSerialNumber", parameter);
            if(hasChanged(serial0,0))
                getSimList(0);
            parameter[0] = 1;
            serial1 = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getSimSerialNumber", parameter);
            if(hasChanged(serial1,1))
                getSimList(0);
            return; // Everything as it was eaelier

        }
        parameter[0] = 0;
        serial0 = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getSimSerialNumber", parameter);
        if (serial0 != null)
        {
            imei = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getDeviceId", parameter);
            network_imsi = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getNetworkOperator", parameter);
            sim_imsi = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getSimOperator", parameter);
            carrier_display_name = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getNetworkOperatorName", parameter);
            if (network_imsi == null) //Network IMSi might not be available when the sim state is not ready
                network_imsi = sim_imsi;
            carrier_circle = getCarrierCirclefromIMSI(network_imsi);
            if (carrier_circle == null)
            {
                getCarrierCircleManually();
            }
            carrier = carrier_circle.get(0);
            circle = carrier_circle.get(1);
            mSimModel = new SimModel.Builder(-1, 0, sim_imsi, carrier)
                    .serial(serial0)
                    .imei(imei)
                    .carrier_display_name(carrier_display_name)
                    .circle(circle)
                    .network_imsi(network_imsi)
                    .build();
            sim_list.add(mSimModel);
        }
        //do the same for sim 2
        parameter[0] = 1;
        imei = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getDeviceId", parameter);
        if (imei == null)
        {
            SimModel.two_slots = false;
            //If there is only one slot then there is no need for checking Sim Serial
            toastHelper("Single Sim" + imei);
        } else
        {
            serial1 = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getSimSerialNumber", parameter);
            if (serial1 != null)
            {

                network_imsi = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getNetworkOperator", parameter);
                sim_imsi = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getSimOperator", parameter);
                carrier_display_name = (String) ReflectionHelper.getObject(mTelephonyManager, "android.telephony.TelephonyManager", "getNetworkOperatorName", parameter);
                if (network_imsi == null)
                    network_imsi = sim_imsi;
                carrier_circle = getCarrierCirclefromIMSI(network_imsi);
                if (carrier_circle == null)
                {
                    getCarrierCircleManually();
                }
                carrier = carrier_circle.get(0);
                circle = carrier_circle.get(1);
                mSimModel = new SimModel.Builder(-1, 1, sim_imsi, carrier)
                        .serial(serial1)
                        .imei(imei)
                        .carrier_display_name(carrier_display_name)
                        .circle(circle)
                        .network_imsi(network_imsi)
                        .build();
                sim_list.add(mSimModel);
            }
        }
        if (serial0 == null || serial1 == null)
            Constants.IS_SINGLE_SIM = true;
        else
        {
            Constants.IS_SINGLE_SIM = false;
        }
        if (serial0 == null && serial1 == null) //BBoth sim serials are null Sims might not be inserted
        {
            SimModel.dual_type = Constants.TYPE_UNKNOWN;
        }

    }

    private boolean isTypeDuosOne()
    {

        Object custom_object = ReflectionHelper.getObject(null, "android.telephony.MSimTelephonyManager", "getDefault", new Object[0]);
        if (!(custom_object instanceof TelephonyManager)) //if not an instance
        {
            return false;
        }
        if (ReflectionHelper.methodCheck(null, "android.telephony.MSimTelephonyManager", "getSimOperator", int.class))
        {
            return true;
        }
        return false;
    }

    private void getSimListForDuosoOne( boolean sim_details_known)
    {
        String serial0, imei, sim_imsi, network_imsi, carrier, carrier_display_name, circle;
        String serial1;
        SimModel mSimModel;
        ArrayList<String> carrier_circle;
        if(sim_details_known)
        {

        }
        //TODO : see whats happening in this

    }

    private boolean isTypeDuosTwo()
    {
        Integer[] mInteger = new Integer[1];
        mInteger[0] = 0;
        Object custom_object = ReflectionHelper.getObject(null, "android.telephony.MultiSimTelephonyManager", "getDefault", mInteger);
        if (!(custom_object instanceof TelephonyManager))
        {
            return false;
        }
        if (ReflectionHelper.methodCheck(custom_object, null, "getDeviceId", new Class[0]))
        {
            return true;
        }
        return false;
    }

    private void getSimListForDuosTwo(boolean sim_details_known)
    {
        String serial0 = null, imei, sim_imsi, network_imsi, carrier, carrier_display_name, circle;
        String serial1 = null;
        SimModel mSimModel;
        ArrayList<String> carrier_circle;
        Object[] parameters = new Object[1];
        if(sim_details_known) //already known then just check serials
        {
            parameters[0] = 0;
            Object multiSimTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.MultiSimTelephonyManager", "getDefault", parameters);
            Object[] empty_parameter = new Object[0];
            serial0 = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getSimSerialNumber", empty_parameter);
            if(hasChanged(serial0,0))
                getSimList(0);
            parameters[0] = 1;
            multiSimTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.MultiSimTelephonyManager", "getDefault", parameters);
            serial1 = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getSimSerialNumber", empty_parameter);
            if(hasChanged(serial1,1))
                getSimList(0);
            return; // Everything as it was eaelier

        }
        parameters[0] = 0;
        Object multiSimTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.MultiSimTelephonyManager", "getDefault", parameters);
        if (multiSimTelephonyManager != null)
        {
            Object[] empty_parameter = new Object[0];
            serial0 = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getSimSerialNumber", empty_parameter);
            if (serial0 != null && !serial0.equals(""))
            {
                imei = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getDeviceId", empty_parameter);
                network_imsi = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getNetworkOperator", empty_parameter);
                sim_imsi = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getSimOperator", empty_parameter);
                carrier_display_name = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getNetworkOperatorName", empty_parameter);
                if (network_imsi == null) //Network IMSi might not be available when the sim state is not ready
                    network_imsi = sim_imsi;
                carrier_circle = getCarrierCirclefromIMSI(network_imsi);
                if (carrier_circle == null)
                {
                    getCarrierCircleManually();
                }
                carrier = carrier_circle.get(0);
                circle = carrier_circle.get(1);
                mSimModel = new SimModel.Builder(-1, 0, sim_imsi, carrier)
                        .serial(serial0)
                        .imei(imei)
                        .carrier_display_name(carrier_display_name)
                        .circle(circle)
                        .network_imsi(network_imsi)
                        .build();
                sim_list.add(mSimModel);
            }
        }
        //for sim 2
        parameters[0] = 1;
        multiSimTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.MultiSimTelephonyManager", "getDefault", parameters);
        if (multiSimTelephonyManager != null)
        {
            Object[] empty_parameter = new Object[0];
            imei = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getDeviceId", empty_parameter);
            if (imei == null)
            {
                SimModel.two_slots = false;
                //If there is only one slot then there is no need for checking Sim Serial
                toastHelper("Single Sim" + imei);
            } else
            {
                serial1 = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getSimSerialNumber", empty_parameter);
                if (serial1 != null && !serial1.equals(""))
                {

                    network_imsi = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getNetworkOperator", empty_parameter);
                    sim_imsi = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getSimOperator", empty_parameter);
                    carrier_display_name = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getNetworkOperatorName", empty_parameter);
                    if (network_imsi == null) //Network IMSi might not be available when the sim state is not ready
                        network_imsi = sim_imsi;
                    carrier_circle = getCarrierCirclefromIMSI(network_imsi);
                    if (carrier_circle == null)
                    {
                        getCarrierCircleManually();
                    }
                    carrier = carrier_circle.get(0);
                    circle = carrier_circle.get(1);
                    mSimModel = new SimModel.Builder(-1, 1, sim_imsi, carrier)
                            .serial(serial1)
                            .imei(imei)
                            .carrier_display_name(carrier_display_name)
                            .circle(circle)
                            .network_imsi(network_imsi)
                            .build();
                    sim_list.add(mSimModel);
                }
            }
        }

        if (serial0 == null || serial1 == null) //if any one is null it means only one sim is inserted
            Constants.IS_SINGLE_SIM = true;
        else
        {
            Constants.IS_SINGLE_SIM = false;
        }
        if (serial0 == null && serial1 == null) //BBoth sim serials are null Sims might not be inserted
        {
            SimModel.dual_type = Constants.TYPE_UNKNOWN;
        }


    }

    private boolean isTypeDuosDs()
    {
        Object custom_object = ReflectionHelper.getObject(null, "android.telephony.MultiSimTelephonyManager", "getDefault", new Object[0]);
        if (!(custom_object instanceof TelephonyManager))
        {
            return false;
        }
        if (ReflectionHelper.methodCheck(custom_object, null, "getDeviceIdDs", int.class))
        {
            return true;
        }
        return false;
    }

    private void getSimListForDuosDs(boolean sim_details_known)
    {
        String serial0 = null, imei, sim_imsi, network_imsi, carrier, carrier_display_name, circle;
        String serial1 = null;
        SimModel mSimModel;
        ArrayList<String> carrier_circle;
        Object[] parameters = new Object[1];
        if(sim_details_known) //already known then just check serials
        {
            parameters[0] = 0;
            Object multiSimTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.MultiSimTelephonyManager", "getDefault", parameters);
            Object[] empty_parameter = new Object[0];
            serial0 = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getSimSerialNumber", empty_parameter);
            if(hasChanged(serial0,0))
                getSimList(0);
            parameters[0] = 1;
            multiSimTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.MultiSimTelephonyManager", "getDefault", parameters);
            serial1 = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getSimSerialNumber", empty_parameter);
            if(hasChanged(serial1,1))
                getSimList(0);
            return; // Everything as it was eaelier

        }
        parameters[0] = 0;
        Object multiSimTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.MultiSimTelephonyManager", "getDefault", parameters);
        if (multiSimTelephonyManager != null)
        {
            Object[] empty_parameter = new Object[0];
            serial0 = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getSimSerialNumber", empty_parameter);
            if (serial0 != null && !serial0.equals(""))
            {
                imei = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getDeviceIdDs", parameters);
                network_imsi = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getNetworkOperator", empty_parameter);
                sim_imsi = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getSimOperator", empty_parameter);
                carrier_display_name = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getNetworkOperatorName", empty_parameter);
                if (network_imsi == null) //Network IMSi might not be available when the sim state is not ready
                    network_imsi = sim_imsi;
                carrier_circle = getCarrierCirclefromIMSI(network_imsi);
                if (carrier_circle == null)
                {
                    getCarrierCircleManually();
                }
                carrier = carrier_circle.get(0);
                circle = carrier_circle.get(1);
                mSimModel = new SimModel.Builder(-1, 0, sim_imsi, carrier)
                        .serial(serial0)
                        .imei(imei)
                        .carrier_display_name(carrier_display_name)
                        .circle(circle)
                        .network_imsi(network_imsi)
                        .build();
                sim_list.add(mSimModel);
            }
        }
        //for sim 2
        parameters[0] = 1;
        multiSimTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.MultiSimTelephonyManager", "getDefault", parameters);
        if (multiSimTelephonyManager != null)
        {
            Object[] empty_parameter = new Object[0];
            imei = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getDeviceIdDs", parameters);
            if (imei == null)
            {
                SimModel.two_slots = false;
                //If there is only one slot then there is no need for checking Sim Serial
                toastHelper("Single Sim" + imei);
            } else
            {
                serial1 = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getSimSerialNumber", empty_parameter);
                if (serial1 != null && !serial1.equals(""))
                {

                    network_imsi = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getNetworkOperator", empty_parameter);
                    sim_imsi = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getSimOperator", empty_parameter);
                    carrier_display_name = (String) ReflectionHelper.getObject(multiSimTelephonyManager, "android.telephony.MultiSimTelephonyManager", "getNetworkOperatorName", empty_parameter);
                    if (network_imsi == null) //Network IMSi might not be available when the sim state is not ready
                        network_imsi = sim_imsi;
                    carrier_circle = getCarrierCirclefromIMSI(network_imsi);
                    if (carrier_circle == null)
                    {
                        getCarrierCircleManually();
                    }
                    carrier = carrier_circle.get(0);
                    circle = carrier_circle.get(1);
                    mSimModel = new SimModel.Builder(-1, 1, sim_imsi, carrier)
                            .serial(serial1)
                            .imei(imei)
                            .carrier_display_name(carrier_display_name)
                            .circle(circle)
                            .network_imsi(network_imsi)
                            .build();
                    sim_list.add(mSimModel);
                }
            }
        }

        if (serial0 == null || serial1 == null)
            Constants.IS_SINGLE_SIM = true;
        else
        {
            Constants.IS_SINGLE_SIM = false;
        }
        if (serial0 == null && serial1 == null) //BBoth sim serials are null Sims might not be inserted
        {
            SimModel.dual_type = Constants.TYPE_UNKNOWN;
        }
    }

    private boolean isTypeXiaomi()
    {
        return ReflectionHelper.methodCheck(null, "miui.telephony.SimInfoManager", "getInsertedSimInfoList", Context.class);
    }

    private void getSimListForXiaomi(boolean sim_details_known)
    {
        String serial0, imei, sim_imsi, network_imsi, carrier, carrier_display_name, circle;
        String serial1 = null;
        long simId0 = 1,simId1 =2;
        SimModel.uses_subscription = true;
        /*
         public static class SimInfoRecord2
          {
            public int mDataRoaming = 0;
            public String mDisplayName = "";
            public String mIccId = "";
            public boolean mIsActivte = true;
            public String mNumber = "";
            public long mSimInfoId;
            public int mSlotId = -1;
          }
         public static long getSimIdBySlotId(Context paramContext, int paramInt)
          {
            return SimInfoWrapper.getInstance(paramContext).getSimIdBySlotId(paramInt);
          }
    }
            */
        SimModel mSimModel;
        ArrayList<String> carrier_circle;
        Object[] simIdParameters = new Object[2];
        simIdParameters[0] = MyApplication.context;
        Object[] empty_parameter = new Object[0];
        Object mSimTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.MSimTelephonyManager", "getDefault", empty_parameter);
        Object[] parameter = new Object[1];
        if(sim_details_known) //already known then just check serials
        {
            parameter[0] = 0;
            serial0 = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getSimSerialNumber", parameter);
            if(hasChanged(serial0,0))
                getSimList(0);
            parameter[0] = 1;
            serial1 = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getSimSerialNumber", parameter);
            if(hasChanged(serial1,1))
                getSimList(0);
            return; // Everything is as it was earlier

        }
        parameter[0] = 0;
        serial0 = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getSimSerialNumber", parameter);
        if (serial0 != null && !serial0.equals(""))
        {
            imei = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getDeviceId", parameter);
            network_imsi = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getNetworkOperator", parameter);
            sim_imsi = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getSimOperator", parameter);
            carrier_display_name = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getNetworkOperatorName", parameter);
            if (network_imsi == null) //Network IMSi might not be available when the sim state is not ready
                network_imsi = sim_imsi;
            carrier_circle = getCarrierCirclefromIMSI(network_imsi);
            if (carrier_circle == null)
            {
                getCarrierCircleManually();
            }
            simIdParameters[1] = 0;
            simId0 = (long) ReflectionHelper.getObject(null, "miui.telephony.SimInfoManager", "getSimIdBySlotId", simIdParameters);
            carrier = carrier_circle.get(0);
            circle = carrier_circle.get(1);
            mSimModel = new SimModel.Builder(simId0, 0, sim_imsi, carrier)
                    .serial(serial0)
                    .imei(imei)
                    .carrier_display_name(carrier_display_name)
                    .circle(circle)
                    .network_imsi(network_imsi)
                    .build();
            sim_list.add(mSimModel);
        }
        //do the same for sim 2
        parameter[0] = 1;
        imei = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getDeviceId", parameter);
        Log.d(tag, imei + "");
        if (imei == null)
        {
            SimModel.two_slots = false;
            //If there is only one slot then there is no need for checking Sim Serial
            toastHelper("Single Sim" + imei);
        } else
        {
            serial1 = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getSimSerialNumber", parameter);
            if (serial1 != null && !serial1.equals(""))
            {
                network_imsi = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getNetworkOperator", parameter);
                sim_imsi = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getSimOperator", parameter);
                carrier_display_name = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getNetworkOperatorName", parameter);
                if (network_imsi == null)
                    network_imsi = sim_imsi;
                carrier_circle = getCarrierCirclefromIMSI(network_imsi);
                if (carrier_circle == null)
                {
                    getCarrierCircleManually();
                }
                carrier = carrier_circle.get(0);
                circle = carrier_circle.get(1);
                simIdParameters[1] = 1;
                simId1 = (long) ReflectionHelper.getObject(null, "miui.telephony.SimInfoManager", "getSimIdBySlotId", simIdParameters);
                mSimModel = new SimModel.Builder(simId1, 1, sim_imsi, carrier)
                        .serial(serial1)
                        .imei(imei)
                        .carrier_display_name(carrier_display_name)
                        .circle(circle)
                        .network_imsi(network_imsi)
                        .build();
                sim_list.add(mSimModel);
            }
        }
        if (serial0 == null || serial1 == null)
            Constants.IS_SINGLE_SIM = true;
        else
        {
            Constants.IS_SINGLE_SIM = false;
        }
        if (serial0 == null && serial1 == null) //Both sim serials are null Sims might not be inserted
        {
            SimModel.dual_type = Constants.TYPE_UNKNOWN;
        }

    }

    private boolean hasChanged(String serial, int i)
    {
        //current serial is null and there was known serail then referesh
        if(serial == null )
        {
            if (knownSerial[i].equals("-1"))
                return false;
            else
                return true;
        }

        //known is not same as current serial then refresh
        if(!knownSerial[i].equals(serial))
            return true;
        return false;
    }

    private boolean isTypeAsus()
    {
        return ReflectionHelper.methodCheck(null, "android.telephony.TelephonyManager", "getTmBySlot", int.class);
    }


    private void getSimListForAsus(boolean sim_details_known)
    {
        //This ne needs a subscriber_id also as the call log uses a part of subsriber_id as sim_index
        String serial0 = null, imei, sim_imsi, network_imsi, carrier, carrier_display_name, circle,subscriber_id;
        String serial1 = null;
        SimModel mSimModel;
        ArrayList<String> carrier_circle;
        Object[] parameters = new Object[1];
        if(sim_details_known) //already known then just check serials
        {
            parameters[0] = 0;
            Object multiSimTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.TelephonyManager", "getTmBySlot", parameters);
            Object[] empty_parameter = new Object[0];
            serial0 = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getSimSerialNumber", empty_parameter);
            if(hasChanged(serial0,0))
                getSimList(0);
            parameters[0] = 1;
            multiSimTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.TelephonyManager", "getTmBySlot", parameters);
            empty_parameter = new Object[0];
            serial1 = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getSimSerialNumber", empty_parameter);
            if(hasChanged(serial1,1))
                getSimList(0);
            return; // Everything as it was eaelier

        }
        parameters[0] = 0;
        Object multiSimTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.TelephonyManager", "getTmBySlot", parameters);
        if (multiSimTelephonyManager != null)
        {
            Object[] empty_parameter = new Object[0];
            TelephonyManager mTelephonyManager = (TelephonyManager)multiSimTelephonyManager;
            {
                Log.d("ASUS","getDeviceId = "+mTelephonyManager.getDeviceId());
                Log.d("ASUS","getSimOperator = "+mTelephonyManager.getSimOperator());
                Log.d("ASUS","getSimSerialNumber = "+mTelephonyManager.getSimSerialNumber());
                Log.d("ASUS","getSubscriberId = "+mTelephonyManager.getSubscriberId());
            }
            serial0 = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getSimSerialNumber", empty_parameter);
            if (serial0 != null && !serial0.equals(""))
            {
                imei = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getDeviceId", empty_parameter);
                network_imsi = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getNetworkOperator", empty_parameter);
                sim_imsi = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getSimOperator", empty_parameter);
                carrier_display_name = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getNetworkOperatorName", empty_parameter);
                subscriber_id = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getSubscriberId", empty_parameter);
                if (network_imsi == null) //Network IMSi might not be available when the sim state is not ready
                    network_imsi = sim_imsi;
                carrier_circle = getCarrierCirclefromIMSI(network_imsi);
                if (carrier_circle == null)
                {
                    getCarrierCircleManually();
                }
                carrier = carrier_circle.get(0);
                circle = carrier_circle.get(1);
                mSimModel = new SimModel.Builder(-1, 0, sim_imsi, carrier)
                        .serial(serial0)
                        .imei(imei)
                        .carrier_display_name(carrier_display_name)
                        .circle(circle)
                        .network_imsi(network_imsi)
                        .subscriber_id(subscriber_id)
                        .build();
                sim_list.add(mSimModel);
            }
        }
        //for sim 2
        parameters[0] = 1;
        multiSimTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.TelephonyManager", "getTmBySlot", parameters);
        if (multiSimTelephonyManager != null)
        {
            TelephonyManager mTelephonyManager = (TelephonyManager)multiSimTelephonyManager;
            {
                Log.d("ASUS","getDeviceId = "+mTelephonyManager.getDeviceId());
                Log.d("ASUS","getSimOperator = "+mTelephonyManager.getSimOperator());
                Log.d("ASUS","getSimSerialNumber = "+mTelephonyManager.getSimSerialNumber());
                Log.d("ASUS","getSubscriberId = "+mTelephonyManager.getSubscriberId());
            }
            Object[] empty_parameter = new Object[0];
            imei = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getDeviceId", empty_parameter);
            if (imei == null)
            {
                SimModel.two_slots = false;
                //If there is only one slot then there is no need for checking Sim Serial
                toastHelper("Single Sim" + imei);
            } else
            {
                serial1 = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getSimSerialNumber", empty_parameter);
                if (serial1 != null && !serial1.equals(""))
                {
                    imei = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getDeviceId", empty_parameter);
                    network_imsi = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getNetworkOperator", empty_parameter);
                    sim_imsi = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getSimOperator", empty_parameter);
                    carrier_display_name = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getNetworkOperatorName", empty_parameter);
                    subscriber_id = (String) ReflectionHelper.getObject(multiSimTelephonyManager, null, "getSubscriberId", empty_parameter);
                    if (network_imsi == null) //Network IMSi might not be available when the sim state is not ready
                        network_imsi = sim_imsi;
                    carrier_circle = getCarrierCirclefromIMSI(network_imsi);
                    if (carrier_circle == null)
                    {
                        getCarrierCircleManually();
                    }
                    carrier = carrier_circle.get(0);
                    circle = carrier_circle.get(1);
                    mSimModel = new SimModel.Builder(-1, 1, sim_imsi, carrier)
                            .serial(serial1)
                            .imei(imei)
                            .carrier_display_name(carrier_display_name)
                            .circle(circle)
                            .network_imsi(network_imsi)
                            .subscriber_id(subscriber_id)
                            .build();
                    sim_list.add(mSimModel);
                }
            }
        }

        if (serial0 == null || serial1 == null)
            Constants.IS_SINGLE_SIM = true;
        else
        {
            Constants.IS_SINGLE_SIM = false;
        }
        if (serial0 == null && serial1 == null) //BBoth sim serials are null Sims might not be inserted
        {
            SimModel.dual_type = Constants.TYPE_UNKNOWN;
        }
    }

    private boolean isTypeMoto()
    {
        if (ReflectionHelper.methodCheck(null, "android.telephony.MSimTelephonyManager", "getSimOperator", int.class)
                && ReflectionHelper.methodCheck(null, "android.telephony.MSimTelephonyManager", "getSimState", int.class))
        {
            return true;
        }
        return false;
    }

    private void getSimListForMoto(boolean sim_details_known)
    {
        String serial0, imei, sim_imsi, network_imsi, carrier, carrier_display_name, circle;
        String serial1 = null;
        SimModel mSimModel;
        ArrayList<String> carrier_circle;
        Object[] empty_parameter = new Object[0];
        Object mSimTelephonyManager = ReflectionHelper.getObject(null, "android.telephony.MSimTelephonyManager", "getDefault", empty_parameter);
        Object[] parameter = new Object[1];
        if(sim_details_known) //already known then just check serials
        {
            parameter[0] = 0;
            serial0 = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getSimSerialNumber", parameter);
            if(hasChanged(serial0,0))
                getSimList(0);
            parameter[0] = 1;
            serial1 = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getSimSerialNumber", parameter);
            if(hasChanged(serial1,1))
                getSimList(0);
            return; // Everything as it was eaelier

        }
        parameter[0] = 0;
        serial0 = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getSimSerialNumber", parameter);
        if (serial0 != null && !serial0.equals(""))
        {
            imei = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getDeviceId", parameter);
            network_imsi = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getNetworkOperator", parameter);
            sim_imsi = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getSimOperator", parameter);
            carrier_display_name = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getNetworkOperatorName", parameter);
            if (network_imsi == null) //Network IMSi might not be available when the sim state is not ready
                network_imsi = sim_imsi;
            carrier_circle = getCarrierCirclefromIMSI(network_imsi);
            if (carrier_circle == null)
            {
                getCarrierCircleManually();
            }
            carrier = carrier_circle.get(0);
            circle = carrier_circle.get(1);
            mSimModel = new SimModel.Builder(-1, 0, sim_imsi, carrier)
                    .serial(serial0)
                    .imei(imei)
                    .carrier_display_name(carrier_display_name)
                    .circle(circle)
                    .network_imsi(network_imsi)
                    .build();
            sim_list.add(mSimModel);
        }
        //do the same for sim 2
        parameter[0] = 1;
        imei = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getDeviceId", parameter);
        Log.d(tag, imei + "");
        if (imei == null)
        {
            SimModel.two_slots = false;
            //If there is only one slot then there is no need for checking Sim Serial
            toastHelper("Single Sim" + imei);
        } else
        {
            serial1 = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getSimSerialNumber", parameter);
            if (serial1 != null && !serial1.equals(""))
            {
                network_imsi = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getNetworkOperator", parameter);
                sim_imsi = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getSimOperator", parameter);
                carrier_display_name = (String) ReflectionHelper.getObject(mSimTelephonyManager, "android.telephony.MSimTelephonyManager", "getNetworkOperatorName", parameter);
                if (network_imsi == null)
                    network_imsi = sim_imsi;
                carrier_circle = getCarrierCirclefromIMSI(network_imsi);
                if (carrier_circle == null)
                {
                    getCarrierCircleManually();
                }
                carrier = carrier_circle.get(0);
                circle = carrier_circle.get(1);
                mSimModel = new SimModel.Builder(-1, 1, sim_imsi, carrier)
                        .serial(serial1)
                        .imei(imei)
                        .carrier_display_name(carrier_display_name)
                        .circle(circle)
                        .network_imsi(network_imsi)
                        .build();
                sim_list.add(mSimModel);
            }
        }
        if (serial0 == null || serial1 == null)
            Constants.IS_SINGLE_SIM = true;
        else
        {
            Constants.IS_SINGLE_SIM = false;
        }
        if (serial0 == null && serial1 == null) //Both sim serials are null Sims might not be inserted
        {
            SimModel.dual_type = Constants.TYPE_UNKNOWN;
        }

    }

    private boolean isTypeNexus()
    {
        if (ReflectionHelper.methodCheck(null, "android.telephony.TelephonyManager", "getSimOperator", Long.class))
        {
            return true;
        }
        return false;
    }

    private void getSimListForNexus(boolean sim_details_known)
    {
        if(sim_details_known)
        {

        }

    }

    private void getSimListForSingleSim(boolean sim_details_known)
    {
        String serial0, imei, sim_imsi, network_imsi, carrier, carrier_display_name, circle;
        SimModel mSimModel;
        ArrayList<String> carrier_circle;
        if (mTelephonyManager == null)
            mTelephonyManager = (TelephonyManager) MyApplication.context.getSystemService(Context.TELEPHONY_SERVICE);
        serial0 = mTelephonyManager.getSimSerialNumber();
        Constants.HAS_TWO_SLOTS = false;
        if(sim_details_known)
        {
            if(hasChanged(serial0,0))
                getSimList(0);
            return;
        }
        if (serial0 != null && !serial0.equals(""))
        {
            imei = mTelephonyManager.getDeviceId();
            network_imsi = mTelephonyManager.getNetworkOperator();
            sim_imsi = mTelephonyManager.getSimOperator();
            carrier_display_name = mTelephonyManager.getNetworkOperatorName();
            if (network_imsi == null) //Network IMSi might not be available when the sim state is not ready
                network_imsi = sim_imsi;
            carrier_circle = getCarrierCirclefromIMSI(network_imsi);
            if (carrier_circle == null)
            {
                getCarrierCircleManually();
            }
            carrier = carrier_circle.get(0);
            circle = carrier_circle.get(1);
            mSimModel = new SimModel.Builder(-1, 0, sim_imsi, carrier)
                    .serial(serial0)
                    .imei(imei)
                    .carrier_display_name(carrier_display_name)
                    .circle(circle)
                    .network_imsi(network_imsi)
                    .build();
            sim_list.add(mSimModel);
        } else
        {
            getCarrierCircleManually();
        }
        Constants.IS_SINGLE_SIM = true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private void getSimListForLolipop(boolean sim_details_known)
    {

        Log.d(tag, "Function getSimListForLolipop");
        SubscriptionManager mSubscriptionManager = SubscriptionManager.from(MyApplication.context);
        Log.d(tag, "SimInfo LIst:" + mSubscriptionManager.getActiveSubscriptionInfoList().toString());
        SubscriptionInfo sim1, sim0;
        SimModel.two_slots = true;
        SimModel.uses_subscription = true;
        if(sim_details_known) //already known then just check serials
        {
            sim0 = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0);
            //Serial is known nut now it doesnot exist
            if(sim0 == null && !knownSerial[0].equals("-1"))
            {
                getSimList(0);
            }
            if(!knownSerial[0].equals(sim0.getIccId()))
                getSimList(0);
            sim1 = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(1);
            if(sim1 == null && !knownSerial[0].equals("-1"))
            {
                getSimList(0);
            }
            if(!knownSerial[1].equals(sim1.getIccId()))
                getSimList(0);
            return; //Everyrhing is as it was earlier
        }
        if (mSubscriptionManager.getActiveSubscriptionInfoCount() < 2)
        {
            sim0 = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0);
            if (sim0 == null)
                sim0 = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(1);
            Constants.IS_SINGLE_SIM = true; //Only one Sim so no problem
            String imsi = sim0.getMcc() + "" + sim0.getMnc();
            //network and sim imsi consider it same
            ArrayList<String> carrier_circle = getCarrierCirclefromIMSI(imsi);
            if (carrier_circle == null)
            {
                carrier_circle = getCarrierCircleManually();

            }
            SimModel mSimModel = new SimModel.Builder(sim0.getSubscriptionId(), sim0.getSimSlotIndex(), imsi, carrier_circle.get(0))
                    .network_imsi(imsi)
                    .carrier_display_name(sim0.getCarrierName().toString())
                    .imei("")//skip for 5.1
                    .serial(sim0.getIccId())
                    .build();
            sim_list.add(mSimModel);
        } else
        {
            Constants.IS_SINGLE_SIM = false;
            sim0 = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0);
            sim1 = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(1);
            if (sim0 != null && sim1 != null)
            {
                String imsi0 = "", imsi1 = "";
                imsi0 = sim0.getMcc() + "" + sim0.getMnc();
                imsi1 = sim1.getMcc() + "" + sim1.getMnc();
                Log.d(tag, "IMSI 0 = " + imsi0 + "\nIMSI1 = " + imsi1);
                if (imsi0.equals(imsi1) && !sim0.getCarrierName().equals(sim1.getCarrierName())) // If MNCs are same get carrier name an see if it is different
                {                                                                       //If yes then Qtiyappa happened try for the general approach
                    int subid_0 = 1, subid_1 = 2;
                    subid_0 = sim0.getSubscriptionId();
                    subid_1 = sim1.getSubscriptionId();
                    //if both IMSI are same though carriers are different then do reelection to find out
                    Object[] parameter = new Object[1];
                    parameter[0] = subid_0;
                    imsi0 = (String) ReflectionHelper.getObject(mTelephonyManager, null, "getNetworkOperatorForSubscription", parameter);
                    parameter[0] = subid_1;
                    imsi1 = (String) ReflectionHelper.getObject(mTelephonyManager, null, "getNetworkOperatorForSubscription", parameter);
                } //If everything is Okay.
                ArrayList<String> carrier_circle = getCarrierCirclefromIMSI(imsi0);
                if (carrier_circle == null)
                {
                    carrier_circle = getCarrierCircleManually();
                }
                SimModel mSimModel = new SimModel.Builder(sim0.getSubscriptionId(), sim0.getSimSlotIndex(), imsi0, carrier_circle.get(0))
                        .network_imsi(imsi0)
                        .carrier_display_name(sim0.getCarrierName().toString())
                        .imei("")//skip for 5.1
                        .serial(sim0.getIccId())
                        .circle(carrier_circle.get(1))
                        .build();
                sim_list.add(mSimModel);
                carrier_circle = getCarrierCirclefromIMSI(imsi1);
                if (carrier_circle == null)
                {
                    carrier_circle = getCarrierCircleManually();
                }
                mSimModel = new SimModel.Builder(sim1.getSubscriptionId(), sim1.getSimSlotIndex(), imsi1, carrier_circle.get(0))
                        .network_imsi(imsi1)
                        .carrier_display_name(sim1.getCarrierName().toString())
                        .imei("")//skip for 5.1
                        .serial(sim1.getIccId())
                        .circle(carrier_circle.get(1))
                        .build();
                sim_list.add(mSimModel);


            } else
            {
                SimModel.dual_type = Constants.TYPE_UNKNOWN;
                sim_list.clear();
                Log.d(tag, "Network Not Available");
            }
        }

    }

    private ArrayList<String> getCarrierCircleManually()
    {
        // Todo : Add manual select or some hack
        ArrayList<String> dummy = new ArrayList<String>();
        dummy.add("DummyCarrier");
        dummy.add("DummyCircle");
        return dummy;
    }

    private void getSimListforMediaTek(boolean sim_details_known)
    {
        Log.d(tag, "MediaTek");
        TelephonyManagerEx mediaTekTelephonyManager = new TelephonyManagerEx(MyApplication.context);
        String imsi0 = "", imsi1 = "", serial0 = null, serial1 = null;
        long subId0=-1,subId1=-1;
        if(sim_details_known) //already known then just check serials
        {
            serial0 =mediaTekTelephonyManager.getSimSerialNumber(0);
            if(hasChanged(serial0,0))
                getSimList(0);
            serial1 =mediaTekTelephonyManager.getSimSerialNumber(1);
            if(hasChanged(serial1,1))
                getSimList(0);
            return; // Everything as it was eaelier

        }
        Class[] subIdParametersClasses = new Class[2];
        subIdParametersClasses[0] = Context.class;
        subIdParametersClasses[1] = int.class;
        Object[] subIdParameters = new Object[2];
        subIdParameters[0] = MyApplication.context;
        subIdParameters[1] = 0;
        serial0 = mediaTekTelephonyManager.getSimSerialNumber(0);
        if (serial0 != null && !serial0.equals(""))
        {
            imsi0 = mediaTekTelephonyManager.getNetworkOperator(0);
            ArrayList<String> carrier_circle = getCarrierCirclefromIMSI(imsi0);
            if (carrier_circle == null)
            {
                carrier_circle = getCarrierCircleManually();
            }
            //If Sub Id Exists
            if(ReflectionHelper.methodCheck(null,"android.provider.Telephony$SIMInfo","getIdBySlot",subIdParametersClasses))
            {
                SimModel.uses_subscription = true;
                subIdParameters[1] = 0;
                subId0 = (long)ReflectionHelper.getObject(null,"android.provider.Telephony$SIMInfo","getIdBySlot",subIdParameters);
            }

            SimModel mSimModel = new SimModel.Builder(subId0, 0, imsi0, carrier_circle.get(0))
                    .network_imsi(mediaTekTelephonyManager.getSimOperator(0))
                    .carrier_display_name(mediaTekTelephonyManager.getNetworkOperatorName(0))
                    .imei(mediaTekTelephonyManager.getDeviceId(0))//skip for 5.1
                    .serial(mediaTekTelephonyManager.getSimSerialNumber(0))
                    .circle(carrier_circle.get(1))
                    .build();
            sim_list.add(mSimModel);
        }
        String imei = mediaTekTelephonyManager.getDeviceId(1);
        if (imei == null)
        {
            SimModel.two_slots = false;
        } else
        {
            serial1 = mediaTekTelephonyManager.getSimSerialNumber(1);
            if (serial1 != null && !serial1.equals(""))
            {
                imsi1 = mediaTekTelephonyManager.getSimOperator(1);
                //Manual Selection is implemented in the function itself
                ArrayList<String> carrier_circle = getCarrierCirclefromIMSI(imsi1);
                //Sub Id is in-existent for Mediatek
                if(ReflectionHelper.methodCheck(null,"android.provider.Telephony$SIMInfo","getIdBySlot",subIdParametersClasses))
                {
                    SimModel.uses_subscription = true;
                    subIdParameters[1] = 1;
                    subId1 = (long)ReflectionHelper.getObject(null,"android.provider.Telephony$SIMInfo","getIdBySlot",subIdParameters);
                }
                SimModel mSimModel = new SimModel.Builder(subId1, 1, imsi1, carrier_circle.get(0))
                        .network_imsi(mediaTekTelephonyManager.getNetworkOperator(1))
                        .carrier_display_name(mediaTekTelephonyManager.getNetworkOperatorName(1))
                        .imei(mediaTekTelephonyManager.getDeviceId(1))//skip for 5.1
                        .serial(mediaTekTelephonyManager.getSimSerialNumber(1))
                        .circle(carrier_circle.get(1))
                        .build();
                sim_list.add(mSimModel);
            }
        }
        if (serial0 == null || serial1 == null)
        {
            //Dual Sim only one sim present on slot 0
            Constants.IS_SINGLE_SIM = true;

        } else
        {
            Constants.IS_SINGLE_SIM = false;

        }
        if (serial0 == null && serial1 == null)
        {
            SimModel.dual_type = Constants.TYPE_UNKNOWN;
            //Both sims have some problem
            Log.d(tag, "Error in getting the Sims");
            sim_list.clear();
        }


    }


    String javaReflectHelper(String method_name, String parameterType, int id)//0 or 1
    {
        if (mTelephonyManager == null)
            mTelephonyManager = (TelephonyManager) MyApplication.context.getSystemService(Context.TELEPHONY_SERVICE);
        try
        {
            if (telephonyClass == null)
                telephonyClass = Class.forName(mTelephonyManager.getClass().getName());

            Class<?>[] parameter = new Class[1];
            if (parameterType.equals("int"))
                parameter[0] = int.class;
            else if (parameterType.equals("long"))
                parameter[0] = long.class;
            Method getSimStateMethod = telephonyClass.getMethod(method_name, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = id; //sim 1
            Object returned_object = getSimStateMethod.invoke(mTelephonyManager, obParameter);
            return returned_object.toString();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList getCarrierCirclefromIMSI(String IMSI)
    {
        if (IMSI == null) //IF MCC+MNC doesn't exist
        {
            return getCarrierCircleManually();
            //Launch a Activity to let user select startActivity for Result

        }
        ArrayList<String> carrier_circle = null;
        if (imsiHelper == null)
        {
            imsiHelper = new IMSIHelper();
        }
        if (IMSI.length() > 6) // if the MCC+MNC tupple doesn't exist
        {
            carrier_circle = imsiHelper.getMapping(IMSI.substring(0, 6)); // TRY with 3 digit MNC
            if (carrier_circle == null)
            {
                carrier_circle = imsiHelper.getMapping(IMSI.substring(0, 5));// TRY with 2 digit MNC
            }
        } else //Normal Flow
        {
            carrier_circle = imsiHelper.getMapping(IMSI);
            if(carrier_circle==null && IMSI.length()>=5)
            {
                String temp = IMSI.substring(0,3)+'0'+IMSI.substring(3);
                Log.d(tag,"Adding zero to find CARRIER_CIRCLE "+temp);
                carrier_circle = imsiHelper.getMapping(temp);
            }
        }
        if (carrier_circle == null) //IF MCC+MNC doesn't exist
        {
            carrier_circle = getCarrierCircleManually();
            //Launch a Activity to let user select startActivity for Result

        }
        return carrier_circle;
    }

    public boolean isDualSim()
    {
        boolean isDualSim = true;//Assume its dual SIM
        String[] method_list = new String[]{"getDeviceId", "getDeviceIdDs", "getDeviceIdGemini", "getDeviceIdExt"};
        if (mTelephonyManager == null)
            mTelephonyManager = (TelephonyManager) MyApplication.context.getSystemService(Context.TELEPHONY_SERVICE);
        try
        {
            if (telephonyClass == null)
                telephonyClass = Class.forName(mTelephonyManager.getClass().getName());
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        Class<?>[] parameter = new Class[1];
        parameter[0] = int.class;
        int length = method_list.length;
        for (int i = 0; i < length; i++)
        {
            Method getSimStateMethod = null;
            try
            {
                getSimStateMethod = telephonyClass.getMethod(method_list[i], parameter);


                Object[] obParameter = new Object[1];
                obParameter[0] = 1; //sim 1
                Object returned_object = getSimStateMethod.invoke(mTelephonyManager, obParameter);
                if (returned_object == null) //Everything Went Good and Still I got a Null it means its Single Sim Phone
                {
                    isDualSim = false;
                    break;
                }
            } catch (NoSuchMethodException e)
            {
                e.printStackTrace();
            } catch (InvocationTargetException e)
            {
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }

        }
        if ((!TextUtils.isEmpty(Build.MANUFACTURER) && Build.MANUFACTURER.toUpperCase().contains("XIAOMI")))
        {
            Method getSimStateMethod = null;
            try
            {
                getSimStateMethod = telephonyClass.getMethod("getDefault", parameter);


                Object[] obParameter = new Object[1];
                obParameter[0] = 0; //sim 1
                Object returned_object = getSimStateMethod.invoke(mTelephonyManager, obParameter);
                TelephonyManager tel = (TelephonyManager) returned_object;
                Log.d(tag, "Telphony 0 = " + tel.getNetworkOperatorName());
                obParameter[0] = 2; //sim 1
                returned_object = getSimStateMethod.invoke(mTelephonyManager, obParameter);
                Log.d(tag, "Telphony 1 = " + tel.getNetworkOperatorName());
                if (returned_object == null)
                {
                    isDualSim = false;
                }
            } catch (NoSuchMethodException e)
            {
                e.printStackTrace();
            } catch (InvocationTargetException e)
            {
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
        return isDualSim;
    }


    public final void checkDualSim() //for Debug only
    {
        Log.d(tag, "Testing Dual SIM Support");
        StringBuilder mStringBuilder = new StringBuilder("Debuf Info :");
        String[] arrayOfString1 = {"android.provider.", "android.telephony.", "com.mediatek.telephony."};
        String[] arrayOfString2 = {"TelephonyManager", "TelephonyManagerEx", "MSimTelephonyManager", "MultiSimTelephonyManager", "SubscriptionManager", "SubInfoRecord", "Telephony", "Telephony$SIMInfo", "MSimCardUtils", "SimManager"};
        for (int i = 0; i < 3; i++)
        {
            String str5 = arrayOfString1[i];
            for (int n = 0; n < 10; n++)
            {
                String str6 = arrayOfString2[n];
                if (ReflectionHelper.classExists(str5 + str6))
                {
                    System.out.println("c  Class  found : " + str5 + str6);
                    mStringBuilder.append("c  Class  found : " + str5 + str6);
                }
            }
        }
        String[] arrayOfString3 = {"phone", "phone_msim"};
        for (int j = 0; j < 2; j++)
        {
            String str3 = arrayOfString3[j];
            Object localObject = ReflectionHelper.getObject(MyApplication.context, "android.content.Context", "getSystemService", new Object[]{str3});
            if (localObject != null)
            {
                String str4 = localObject.getClass().getName();
                System.out.println("o  Object found : " + str4 + " by context.getSystemService(\"" + str3 + "\")");
                mStringBuilder.append("o  Object found : " + str4 + " by context.getSystemService(\"" + str3 + "\")");
            }
        }
        String[] arrayOfString4 = {"android.telephony.TelephonyManager", "com.mediatek.telephony.TelephonyManagerEx", "android.telephony.MSimTelephonyManager", "android.telephony.MultiSimTelephonyManager", "android.telephony.SubscriptionManager"};
        for (int k = 0; k < 5; k++)
        {
            String str2 = arrayOfString4[k];
            Method localMethod4 = ReflectionHelper.getMethod(null, str2, "getDefault", new Object[0]);
            if (localMethod4 != null)
            {
                System.out.println("m  static method found : " + localMethod4.toGenericString());
                mStringBuilder.append("m  static method found : " + localMethod4.toGenericString());
            }
            Object[] arrayOfObject3 = new Object[1];
            arrayOfObject3[0] = Integer.valueOf(1);
            Method localMethod5 = ReflectionHelper.getMethod(null, str2, "getDefault", arrayOfObject3);
            if (localMethod5 != null)
            {
                System.out.println("m  static method found : " + localMethod5.toGenericString());
                mStringBuilder.append("m  static method found : " + localMethod5.toGenericString());
            }
            Object[] arrayOfObject4 = new Object[1];
            arrayOfObject4[0] = Long.valueOf(1L);
            Method localMethod6 = ReflectionHelper.getMethod(null, str2, "getDefault", arrayOfObject4);
            if (localMethod6 != null)
            {
                System.out.println("m  static method found : " + localMethod6.toGenericString());
                mStringBuilder.append("m  static method found : " + localMethod6.toGenericString());
            }
            Method localMethod7 = ReflectionHelper.getMethod(null, str2, "getActiveSubInfoList", new Object[0]);
            if (localMethod7 != null)
            {
                System.out.println("m  static method found : " + localMethod7.toGenericString());
                mStringBuilder.append("m  static method found : " + localMethod7.toGenericString());
            }
            Method localMethod8 = ReflectionHelper.getMethod(null, str2, "getActivatedSubInfoList", new Object[0]);
            if (localMethod8 != null)
            {
                System.out.println("m  static method found : " + localMethod8.toGenericString());
                mStringBuilder.append("m  static method found : " + localMethod8.toGenericString());
            }
        }
        String[] arrayOfString5 = {"getDefault", "getSimOperator", "getSimOperatorGemini", "getSimSerialNumber", "getSimSerialNumberDs"};
        TelephonyManager localTelephonyManager = (TelephonyManager) MyApplication.context.getSystemService(Context.TELEPHONY_SERVICE);
        for (int m = 0; m < 5; m++)
        {
            String str1 = arrayOfString5[m];
            Method localMethod1 = ReflectionHelper.getMethod(localTelephonyManager, null, str1, new Object[0]);
            if (localMethod1 != null)
            {
                System.out.println("m  TelMan method found : " + localMethod1.toGenericString());
                mStringBuilder.append("m  TelMan method found : " + localMethod1.toGenericString());
            }
            Object[] arrayOfObject1 = new Object[1];
            arrayOfObject1[0] = Integer.valueOf(1);
            Method localMethod2 = ReflectionHelper.getMethod(localTelephonyManager, null, str1, arrayOfObject1);
            if (localMethod2 != null)
            {
                System.out.println("m  TelMan method found : " + localMethod2.toGenericString());
                mStringBuilder.append("m  TelMan method found : " + localMethod2.toGenericString());
            }
            Object[] arrayOfObject2 = new Object[1];
            arrayOfObject2[0] = Long.valueOf(1L);
            Method localMethod3 = ReflectionHelper.getMethod(localTelephonyManager, null, str1, arrayOfObject2);
            if (localMethod3 != null)
            {
                System.out.println("m  TelMan method found : " + localMethod3.toGenericString());
                mStringBuilder.append("m  TelMan method found : " + localMethod3.toGenericString());
            }
        }
        SimModel.debugInfo = mStringBuilder.toString();
    }

    public ArrayList<String> getDualCallLogColumn()
    {
        Cursor managedCursor = MyApplication.context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI, null, null, null, null);
        SimModel.call_log_columns.clear();
        Helper.toastHelper("Checking Call Logs");
        final String[] colPrefixList = {"subscription", "slot", "icc", "sim", "sub","phone"};
        final String[] colSuffixList = {"", "index", "indx", "idx", "id", "_index", "Slot", "_subscription", "_id", "num", "Num"};
        int prefix_length = colPrefixList.length;
        int suffix_length = colSuffixList.length;
        ArrayList<String> call_log_columns = new ArrayList<String>();
        String dual_sim_column_name = ""; //fall back to logical categorization
        for (int i = 0; i < prefix_length; i++)
            for (int j = 0; j < suffix_length; j++)
            {
                dual_sim_column_name = colPrefixList[i] + colSuffixList[j];
                if (managedCursor.getColumnIndex(dual_sim_column_name) != -1) //if it exists
                {
                    toastHelper("Found "+dual_sim_column_name);
                    call_log_columns.add(dual_sim_column_name);
                }
            }
        toastHelper("found  "+SimModel.call_log_columns.toString());
        return call_log_columns; //fall back to logical categorization
    }
}
