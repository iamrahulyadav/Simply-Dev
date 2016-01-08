package com.builder.ibalance.parsers;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.builder.ibalance.models.USSDModels.NormalCall;
import com.builder.ibalance.models.USSDModels.NormalData;
import com.builder.ibalance.models.USSDModels.NormalSMS;
import com.builder.ibalance.models.USSDModels.PackCall;
import com.builder.ibalance.models.USSDModels.PackData;
import com.builder.ibalance.models.USSDModels.PackSMS;
import com.builder.ibalance.models.USSDModels.USSDBase;
import com.builder.ibalance.util.ConstantsAndStatics;
import com.builder.ibalance.util.Loki;
import com.builder.ibalance.util.MyApplication;
import com.google.code.regexp.Matcher;
import com.google.code.regexp.Pattern;

import org.json.JSONArray;
import org.json.JSONException;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Date;

import javax.crypto.NoSuchPaddingException;

public class USSDParser
{

    final String TAG = USSDParser.class.getSimpleName();
    USSDBase ussdDetails;
    Loki mLoki;

    public USSDParser()
    {
        try
        {
            mLoki = new Loki();
        } catch (NoSuchAlgorithmException e)
        {
            //V10e.printStackTrace();
        } catch (NoSuchPaddingException e)
        {
            //V10e.printStackTrace();
        } catch (NoSuchProviderException e)
        {
            //V10e.printStackTrace();
        }
    }

    public USSDBase getDetails()
    {
        return ussdDetails;
    }


    public boolean parseMessage(String message)
    {
        Log.d(TAG, "Recent event = " + ConstantsAndStatics.RECENT_EVENT);
        try
        {
            switch (ConstantsAndStatics.RECENT_EVENT)
            {
                case Intent.ACTION_NEW_OUTGOING_CALL:
                    ConstantsAndStatics.RECENT_EVENT = "UNKNOWN";
                    if (normalCall(message))
                    {
                        Log.d(TAG, "Was A Normal Call");
                        return true;
                    }
                    if (packCall(message))
                    {
                        Log.d(TAG, "Was A Normal Call");
                        return true;
                    }
                case Intent.ACTION_DATE_CHANGED:
                    ConstantsAndStatics.RECENT_EVENT = "UNKNOWN";
                    if (packData(message))
                    {
                        Log.d(TAG, "Was A Pack Data");
                        return true;
                    }
                    if (normalData(message))
                    {
                        Log.d(TAG, "Was A Normal Data");
                        return true;
                    }
                case "UNKNOWN":
                    if (tryAllTypes(message))
                    {
                        Log.d(TAG, "Got from new Version");
                        return true;
                    }
                    Log.d(TAG, "Trying old school methods");
                    return tryOldSchoolMethod(message);
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
            return tryOldSchoolMethod(message);
        }
        return false;
    }

    private boolean tryAllTypes(String message)
    {
        Log.d(TAG, "Trying all Types");
        try
        {
            if (normalCall(message))
            {
                Log.d(TAG, "Normal Call");
                return true;
            }
            if (normalSMS(message))
            {
                Log.d(TAG, "Normal SMS");
                return true;
            }
            if (packSMS(message))
            {
                Log.d(TAG, "Pack SMS");
                return true;
            }
            if (packCall(message))
            {
                Log.d(TAG, "Pack Call");
                return true;
            }
            if (packData(message))
            {
                Log.d(TAG, "Pack data");
                return true;
            }
            if (normalData(message))
            {
                Log.d(TAG, "Normal DATA");
                return true;
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return false;

    }


    private boolean normalCall(String message) throws JSONException
    {
        Log.d(TAG, "Trying Normal Call");
        Matcher result = findDetails(mLoki.getNormalCallRegex(), message);
        if (result != null)
        {
            Log.d(TAG, "Matched!");
            String bal = result.group("MBAL");
            //to take care of number like 1,208.990
            bal = bal.replace(",", "").trim();
            //to take care of number like "20."
            if (bal.charAt(bal.length() - 1) == '.') bal = bal.substring(0, bal.length() - 2);
            float mainBal = Float.parseFloat(bal);
            //ptln("Cost = " + mMatcher.group("COST"));
            // ptln("Balance = " +);
            int duration = -1;
            float cost = -1.0f;
            try
            {
                cost = Float.parseFloat(result.group("COST"));
            } catch (Exception e)
            {
                cost = -1.0f;
            }
            try
            {
                duration = Integer.parseInt(result.group("DURS"));
                //ptln("Duration Secs = " + duration);
            } catch (IndexOutOfBoundsException e0)
            {
                try
                {
                    String clock = result.group("DURC");
                    if(clock==null)
                        throw new IndexOutOfBoundsException();
                    String sub[] = clock.split(":");
                    duration = Integer.parseInt(sub[0]) * 60 * 60;
                    duration += Integer.parseInt(sub[1]) * 60;
                    duration += Integer.parseInt(sub[2]);
                    //ptln("Duration Clock = " + duration);
                } catch (IndexOutOfBoundsException e1)
                {
                    try
                    {
                        duration = Integer.parseInt(result.group("DURMIN")) * 60;
                        duration += Integer.parseInt(result.group("DURSEC"));
                        //ptln("Duration Min:Secs = " + duration);
                    } catch (IndexOutOfBoundsException e2)
                    {
                        duration = -1;
                        //ptln("No Duration Found A-Hole Telecos");
                    }
                }
            }
            ussdDetails = new com.builder.ibalance.models.USSDModels.NormalCall();
            ((com.builder.ibalance.models.USSDModels.NormalCall) ussdDetails).
                    USSDDetails((new Date()).getTime(), ConstantsAndStatics.USSD_TYPES.NORMAL_CALL, mainBal, cost, duration, message);
            //this.type = USSDMessageType.NORMAL_CALL;
            //details = new NormalCall((new Date()).getTime(), cost, mainBal, duration, message);
            Log.d(TAG, "details = " + ussdDetails.toString());
            return true;
        }
        Log.d(TAG, "Normal Call Didn't Match");
        return false;
    }

    private boolean packData(String message) throws JSONException
    {
        Log.d(TAG, "Trying Pack Data");
        Matcher result = findDetails(mLoki.getPackDataRegex(), message);
        if (result != null)
        {
          /*type(String/Unknown)- TYPE [types (3G,2G,-1,GPRS) (will be empty)or(Exception might be thrown assume 2G) ]
            data used(Mb/-1.0f)	- DUSED
            MB-KB split(Mb/-1.0f)  - DMBUSED , DKBUSED
            data used Metric- DUSEDM[B KB MB GB  (KB,MB,K,empty (assume K))]
            data left		- DLEFT
            GB-MB-KB split - DGBLEFT, DMBLEFT, DKBLEFT
            data used Metric- DLEFTM [B KB MB GB  (KB,MB,K,empty (assume K))]
            validity 		- VAL [(will be empty)or(exception thrown)] , might be incompelete like 31/07/ or 31/07/20
            balance 		- MBAL ? (optional -1)]*/
            String packType, usedMetric, leftMetric, validity;
            float dataUsed, dataLeft, mainBal;
            try
            {
                packType = result.group("TYPE");
            } catch (IndexOutOfBoundsException e)
            {
                packType = "Unknown";
            }
            try
            {
                usedMetric = result.group("DUSEDM");
            } catch (IndexOutOfBoundsException e)
            {
                usedMetric = "KB";
            }
            try
            {
                dataUsed = Float.parseFloat(result.group("DUSED"));
                switch (usedMetric)
                {
                    case "B":
                        dataUsed = dataUsed / 1000000f;
                        break;
                    case "K":
                    case "KB":
                        dataUsed = dataUsed / 1000f;
                        break;
                    case "M":
                    case "MB":
                        break;
                    case "GB":
                        dataUsed = dataUsed * 1000;
                        break;
                }
            } catch (IndexOutOfBoundsException e)
            {
                //Can be a MB Kb Split
                boolean found = false;
                float mb, kb;
                try
                {
                    mb = Float.parseFloat(result.group("DMBUSED"));
                    found = true;
                } catch (IndexOutOfBoundsException e1)
                {
                    mb = 0.0f;
                }
                try
                {
                    kb = Float.parseFloat(result.group("DKBUSED"));
                    found = true;
                } catch (IndexOutOfBoundsException e1)
                {
                    kb = 0.0f;
                }
                if (found)
                {
                    dataUsed = mb + (kb * 1000f);
                } else
                {
                    dataUsed = -1.0f;
                }
            }
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //Data Left
            try
            {
                leftMetric = result.group("DLEFTM");
            } catch (IndexOutOfBoundsException e)
            {
                leftMetric = "KB";
            }
            try
            {
                dataLeft = Float.parseFloat(result.group("DLEFT"));
                switch (leftMetric)
                {
                    case "B":
                        dataLeft = dataLeft / 1000000f;
                        break;
                    case "K":
                    case "KB":
                        dataLeft = dataLeft / 1000f;
                        break;
                    case "MB":
                        break;
                    case "GB":
                        dataLeft = dataLeft * 1000;
                        break;
                }
            } catch (IndexOutOfBoundsException e)
            {
                //Can be a MB Kb Split
                boolean found = false;
                float mb, kb;
                try
                {
                    mb = Float.parseFloat(result.group("DMBLEFT"));
                    found = true;
                } catch (IndexOutOfBoundsException e1)
                {
                    mb = 0.0f;
                }
                try
                {
                    kb = Float.parseFloat(result.group("DKBLEFT"));
                    found = true;
                } catch (IndexOutOfBoundsException e1)
                {
                    kb = 0.0f;
                }
                if (found)
                {
                    dataLeft = mb + (kb * 1000f);
                } else
                {
                    dataLeft = -1.0f;
                }
            }
            try
            {
                validity = result.group("VAL");
                //Have to convert it to machine understandable
            } catch (IndexOutOfBoundsException e)
            {
                validity = "N/A";
            }
            try
            {
                String bal = result.group("MBAL");
                //to take care of number like 1,208.990
                bal = bal.replace(",", "").trim();
                //to take care of number like "20."
                if (bal.charAt(bal.length() - 1) == '.') bal = bal.substring(0, bal.length() - 2);
                mainBal = Float.parseFloat(bal);
            } catch (IndexOutOfBoundsException e)
            {
                mainBal = -1.0f;
            }
            ussdDetails = new PackData();
            ((PackData) ussdDetails).USSDDetails((new Date()).getTime(), ConstantsAndStatics.USSD_TYPES.PACK_DATA, mainBal, packType, validity, dataUsed, dataLeft, message);
            //this.type = USSDMessageType.DATA_PACK;
            //Long time, Float data_consumed,Float data_left,Float bal,String validity, String message
            //details = new DataPack((new Date()).getTime(), dataUsed, dataLeft, mainBal, validity, message);
            //Log.d(TAG, "Pack details : " + details);
            return true;
        }
        return false;
    }

    private boolean packSMS(String message) throws JSONException
    {
        Log.d(TAG, "Trying Pack SMS");
        //Log.d(TAG, "Trying Pack SMS");
        //TYPE - optional Type (?<TYPE>NAT|LOC|NIGHT|STD|NAT&LCL|L(?:OC)?\+STDSTD|LOCAL\s*AIRTEL|LOCAL|LOCAL&STD\s*AIRTEL|LOC&NAT|L(?:OC)?\+STDN|LOCAL\s*AND\s*NATIONAL|VF\s*2\s*VF\s*LOCAL)\s*
        //DEDT - optional SMS deducted
        //REM - SMS Left
        //MBAL -optional Main Balance
        //Validity - optional sometimes may be incomplete
        //anything negative or null means its missing.
        Matcher result = findDetails(mLoki.getPackSMSRegex(), message);
        if (result != null)
        {
            //ptln(message);
            int remSMS, deductedSMS;
            String validity, packType;
            float mainBal;
            remSMS = Integer.parseInt(result.group("REM"));
            try
            {
                deductedSMS = Integer.parseInt(result.group("DEDT"));
            } catch (IndexOutOfBoundsException e)
            {
                deductedSMS = -1;
            }
            try
            {
                validity = result.group("VAL");
            } catch (IndexOutOfBoundsException e)
            {
                validity = null;
            }
            try
            {
                packType = result.group("TYPE");
            } catch (IndexOutOfBoundsException e)
            {
                packType = null;
            }
            try
            {
                String bal = result.group("MBAL");
                //to take care of number like 1,208.990
                bal = bal.replace(",", "").trim();
                //to take care of number like "20."
                if (bal.charAt(bal.length() - 1) == '.') bal = bal.substring(0, bal.length() - 2);
                mainBal = Float.parseFloat(bal);
            } catch (IndexOutOfBoundsException e)
            {
                mainBal = -1.0f;
            } catch (NullPointerException e)
            {
                mainBal = -1.0f;
            }
            //ptln((i++)+")Deducted = "+deductedSMS+" Rem = "+remSMS+" main Bal = "+mainBal+" Val = "+validity+" Type = "+packType);
            ussdDetails = new PackSMS();
            ((PackSMS) ussdDetails).
                    USSDDetails((new Date()).getTime(), ConstantsAndStatics.USSD_TYPES.PACK_SMS, mainBal, packType, validity, deductedSMS, remSMS, message);
            return true;
        }
        return false;
    }

    private boolean normalSMS(String message) throws JSONException
    {
        Log.d(TAG, "Trying Normal SMS");
        //Log.d(TAG, "Trying Normal SMS");
        //Normal SMS has cost and balance left
        //Named Group COST and MBAL
        Matcher result = findDetails(mLoki.getNormalSMSRegex(), message);
        if (result != null)
        {
            String bal = result.group("MBAL");
            //to take care of number like 1,208.990
            bal = bal.replace(",", "").trim();
            //to take care of number like "20."
            if (bal.charAt(bal.length() - 1) == '.') bal = bal.substring(0, bal.length() - 2);
            float mainBal = Float.parseFloat(bal);
            float cost = Float.parseFloat(result.group("COST"));
            //ptln(message);
            //ptln("Cost = "+cost+"  Main Bal = "+mainBal);
            /*type = USSDMessageType.NORMAL_SMS;
            details = new NormalSMS(new Date().getTime(), cost, mainBal, "9972115447", message);
            Log.d(TAG+" SMS", "details = "+details.toString());*/
            ussdDetails = new com.builder.ibalance.models.USSDModels.NormalSMS();
            ((com.builder.ibalance.models.USSDModels.NormalSMS) ussdDetails).
                    USSDDetails(new Date().getTime(), ConstantsAndStatics.USSD_TYPES.NORMAL_SMS, mainBal, cost, message);
            ussdDetails.toString();
            return true;
        }
        return false;
    }

    private boolean normalData(String message) throws JSONException
    {
        Log.d(TAG, "Trying Normal Data");
        //Log.d(TAG, "Trying Normal Data");
        //MBAL - Main Balance
        //DCOST - optional cost
        //DUSED - optional used Data
        //DUSEDM - optional used Metric B|KB|MB|GB
        //DMBUSED- optional MB Used
        //DKBUSED - optional KB used
        //DBUSED - optional Bytes used
        Matcher result = findDetails(mLoki.getNormalDataRegex(), message);
        if (result != null)
        {
            //ptln(message);
            float mainBal, dataUsed = 0.0f, cost, bUsed = 0.0f, kUsed = 0.0f, mUsed = 0.0f;
            String usedMetric;
            boolean normalTypeFound = false;
            String bal = result.group("MBAL");
            //to take care of number like 1,208.990
            bal = bal.replace(",", "").trim();
            //to take care of number like "20."
            if (bal.charAt(bal.length() - 1) == '.') bal = bal.substring(0, bal.length() - 2);
            mainBal = Float.parseFloat(bal);
            try
            {
                cost = Float.parseFloat(result.group("DCOST"));
            } catch (RuntimeException e)
            {
                cost = -1.0f;
            }
            try
            {
                usedMetric = result.group("DUSEDM");
            } catch (IndexOutOfBoundsException e)
            {
                usedMetric = "KB";
            }
            try
            {
                dataUsed = Float.parseFloat(result.group("DUSED"));
                switch (usedMetric)
                {
                    case "B":
                        dataUsed = (float) (dataUsed / 1000000f);
                        break;
                    case "K":
                    case "KB":
                        dataUsed = dataUsed / 1000f;
                        break;
                    case "MB":
                        break;
                    case "GB":
                        dataUsed = dataUsed * 1000;
                        break;
                }
                normalTypeFound = true;
            } catch (RuntimeException e)
            {
                dataUsed = -1.0f;
            }
            if (!normalTypeFound)
            {
                try
                {
                    mUsed = Float.parseFloat(result.group("DMBUSED"));
                } catch (RuntimeException e)
                {
                    mUsed = 0;
                }
                try
                {
                    kUsed = Float.parseFloat(result.group("DKBUSED"));
                } catch (RuntimeException e)
                {
                    kUsed = 0;
                }
                try
                {
                    bUsed = Float.parseFloat(result.group("DBUSED"));
                } catch (RuntimeException e)
                {
                    bUsed = 0;
                }
                dataUsed = mUsed + (kUsed / 1000f) + (bUsed / 1000000f);

            }
            //TODO details
            //ptln((i++)+")Data used = "+dataUsed+"MB Cost = "+cost+" Main Bal = "+mainBal+"\n");
            ussdDetails = new com.builder.ibalance.models.USSDModels.NormalData();
            ((com.builder.ibalance.models.USSDModels.NormalData) ussdDetails).
                    USSDDetails((new Date()).getTime(), ConstantsAndStatics.USSD_TYPES.NORMAL_DATA, mainBal, cost, dataUsed, message);
            ussdDetails.toString();

            return true;
        }
        return false;
    }

    private boolean packCall(String message) throws JSONException
    {
        Log.d(TAG, "Trying Pack Call");
        Matcher result = findDetails(mLoki.getPackCallRegex(), message);
        if (result != null)
        {
            //ptln(message);
            /*
            TYPE : optional Pack Type LOCALA2A loc+nat etc etc
            CUSED : Call Pack Used duration
            CUSEDM: Call Pack  Used Metric SEC?|MINS?
            CLEFT : Call Pack duration left used the CUSED metric to infer sec or mins
            MBAL : mainBal
            PBUSED:(opt) Pack balance used if pack is in the form of amount
            PBAL: (opt)Pack balance left if pack is in the form of amount
            //Skipping cost COST :(opt) call cost in case pack was just a rate cutter
            CUMIN : (opt) Call Pack Used Min
            CUSEC : (opt) Call Pack Used Sec
            CLHR : (opt) Call Pack Left Hour
            CLMIN : (opt) Call Pack Left Min
            CLSEC : (opt) Call Pack Left Sec
            DURC : (opt)Actual Call Duration clock 00:00:00
            DURS : (opt)Actual Call Duration only secs
            DURHR : (opt)Actual Call Duration HR
            DURMIN :(opt) Actual Call Duration MIN
            DURSEC :(opt) Actual Call Duration SEC
            VAL : Validity
            */
            /*
            Story Time:
            There are two types of packs one giving free mins/secs and other giving a special balance with expiry data
            both are mutually exclusive
            */
            float mainBal, packBal = -1.0f, packCost = -1.0f;
            int usedSecs, leftSecs, durSec;
            String usedMetric = "SECS", leftMetric = null, validity, packType;
            boolean freeMinType = false;
            try
            {
                usedSecs = Integer.parseInt(result.group("CUSED"));

                freeMinType = true;
            } catch (RuntimeException e)
            {
                int min = 0, sec = 0;
                int err = 0;
                try
                {
                    min = Integer.parseInt(result.group("CUMIN"));
                    freeMinType = true;

                } catch (RuntimeException e1)
                {
                    min = 0;
                    err--;
                }
                try
                {
                    sec = Integer.parseInt(result.group("CUSEC"));
                    freeMinType = true;
                } catch (RuntimeException e1)
                {
                    sec = 0;
                    err--;
                }
                if (err <= -2) usedSecs = -1;
                else usedSecs = min * 60 + sec;
            }
            try
            {
                leftSecs = Integer.parseInt(result.group("CLEFT"));
                freeMinType = true;
            } catch (RuntimeException e)
            {
                int hr = 0, min = 0, sec = 0;
                int err = 0;
                try
                {
                    hr = Integer.parseInt(result.group("CLHR"));
                    freeMinType = true;
                } catch (RuntimeException e1)
                {
                    hr = 0;
                    err--;
                }
                try
                {
                    min = Integer.parseInt(result.group("CLMIN"));
                    freeMinType = true;

                } catch (RuntimeException e1)
                {
                    min = 0;
                    err--;
                }
                try
                {
                    sec = Integer.parseInt(result.group("CLSEC"));
                    freeMinType = true;
                } catch (RuntimeException e1)
                {
                    sec = 0;
                    err--;
                }
                if (err <= -3) leftSecs = -1;
                else
                {
                    leftMetric = "SECS";
                    leftSecs = (hr * 60 * 60) + (min * 60) + sec;
                }
            }
            try
            {
                //OnlySecs
                durSec = Integer.parseInt(result.group("DURS"));
            } catch (RuntimeException e)
            {
                try
                {
                    //Clock Timing
                    String clock = result.group("DURC");
                    if(clock==null)
                        throw new IndexOutOfBoundsException();
                    String sub[] = clock.split(":");
                    durSec = Integer.parseInt(sub[0]) * 60 * 60;
                    durSec += Integer.parseInt(sub[1]) * 60;
                    durSec += Integer.parseInt(sub[2]);
                } catch (RuntimeException e1)
                {
                    //Hr mim sec split
                    int hr = 0, min = 0, sec = 0;
                    int err = 0;
                    try
                    {
                        hr = Integer.parseInt(result.group("DURHR"));
                    } catch (RuntimeException e2)
                    {
                        hr = 0;
                        err--;
                    }
                    try
                    {
                        min = Integer.parseInt(result.group("DURMIN"));

                    } catch (RuntimeException e2)
                    {
                        min = 0;
                        err--;
                    }
                    try
                    {
                        sec = Integer.parseInt(result.group("DURSEC"));
                        freeMinType = true;
                    } catch (RuntimeException e2)
                    {
                        sec = 0;
                        err--;
                    }
                    if (err <= -3) durSec = -1;
                    else
                    {
                        durSec = (hr * 60 * 60) + (min * 60) + sec;
                    }

                }
            }
            if (durSec != -1 && usedSecs != -1)
            {
                if (usedSecs == durSec)
                {
                    usedMetric = "SECS";
                }
                //ceil of secs to get upper bound mins
                else if (usedSecs == ((durSec + 60 - 1) / 60))
                {
                    usedMetric = "MINS";
                } else if (usedSecs % 60 == 0)
                {
                    //Per minute plan but measured in secs
                    usedMetric = "SECS";

                } else
                {
                    try
                    {
                        usedMetric = result.group("CUSEDM");
                    } catch (RuntimeException e)
                    {
                        usedMetric = "SECS";
                    }
                }

            } else
            {
                try
                {
                    usedMetric = result.group("CUSEDM");
                } catch (RuntimeException e)
                {
                    usedMetric = "SECS";
                }
            }
            if (leftMetric == null) leftMetric = usedMetric;
            try
            {
                validity = result.group("VAL");
            } catch (RuntimeException e)
            {
                validity = null;
            }
            try
            {
                packType = result.group("TYPE");
            } catch (RuntimeException e)
            {
                packType = null;
            }
            try
            {
                mainBal = Float.parseFloat(result.group("MBAL"));
            } catch (RuntimeException e)
            {
                mainBal = -1.0f;
            }
            try
            {
                packCost = Float.parseFloat(result.group("PBUSED"));
            } catch (RuntimeException e)
            {
                packCost = -1.0f;
            }
            try
            {
                packBal = Float.parseFloat(result.group("PBAL"));
            } catch (RuntimeException e)
            {
                packBal = -1.0f;
            }


            //ptln((i++)+")Type = "+type+" Used "+usedSecs+" metric = "+usedMetric+" Duration = "+durSec+" Left = "+leftSecs+" Metric= "+leftMetric+" Validity = "+validity+" Main Bal ="+mainBal+"\n");


            //ptln((i++)+")Type = "+type+" Duration = "+durSec+" Pcost = "+packCost+" Pbal = "+packBal+" Validity = "+validity+" Main Bal ="+mainBal+"\n");
            ussdDetails = new PackCall();
            ((PackCall) ussdDetails).USSDDetails((new Date()).getTime(), ConstantsAndStatics.USSD_TYPES.PACK_CALL, mainBal, durSec, packType, validity, usedSecs, usedMetric, leftSecs, leftMetric, packCost, packBal, message);
            ussdDetails.toString();
            return true;
        }
        return false;
    }


    boolean tryOldSchoolMethod(String message)
    {
        Log.d(TAG, "Trying Old School Method");
        if (parseForNormalCall(message))
        {
            return true;
        } else if (parseForNormalData(message))
        {
            return true;
        } else if (parseForDataPack(message))
        {
            return true;
        } else if (parseForNormalSMS(message))
        {
            return true;
        }


        return false;//Invalid
    }


    private boolean parseForNormalSMS(String message)
    {
        Float balance = (float) 0.0, cost = (float) 0.0;
        Long time = (new Date()).getTime();
        String lastNumber = "";
        int count = 0;

        String smsCost = mLoki.getNormal_sms_smsCost();//"(Last SMS|SMS cost|SMS Cost|SMS COST|SMS charge from Main Bal):?\\s*R?s?\\s*[\\.=]?\\s*(\\d+\\.?\\d+)";
        Pattern pcc;
        Matcher mcc;
        pcc = Pattern.compile(smsCost);
        mcc = pcc.matcher(message);
        if (mcc.find())
        {
            cost = Float.parseFloat(mcc.group(2));
            count++;
            System.out.println(TAG + " SMS " + "Found SMS Cost " + cost);
        }

        boolean flag = false;
        // get the remaining balance
        String smsBal = mLoki.getNormal_sms_smsBal1();//"(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|main balance left|balance left|RemainingSMSBal|Account Balance is):?\\s*R?s?\\s*[\\.=]?\\s*(\\d+\\.?\\d+)";
        pcc = Pattern.compile(smsBal);
        mcc = pcc.matcher(message);
        if (mcc.find())
        {
            flag = true;
            count++;
            balance = Float.parseFloat(mcc.group(2));
            System.out.println(TAG + " SMS" + "Found bal " + balance.toString());
        }

        {
            //Rs not optional overide the previous
            smsBal = mLoki.getNormal_sms_smsBal2();//"(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|main balance left|balance left|Main Bal:):?\\s*Rs\\s*[\\.=]?\\s*(\\d+\\.?\\d+)";
            pcc = Pattern.compile(smsBal);
            mcc = pcc.matcher(message);
            if (mcc.find())
            {
                if (!flag) count++;
                balance = Float.parseFloat(mcc.group(2));
                System.out.println(TAG + " SMS" + "Found bal " + balance.toString());
            }
        }
        if (count == 2)
        {
            System.out.println("Found SMS Pattern");
            // Create Sent box URI
            Uri sentURI = Uri.parse("content://sms/sent");

            // List required columns
            String[] reqCols = new String[]{"address"};

            // Get Content Resolver object, which will deal with Content Provider
            ContentResolver cr = MyApplication.context.getContentResolver();

            // Fetch Sent SMS Message from Built-in Content Provider
            Cursor c = cr.query(sentURI, reqCols, null, null, "date " + "DESC  , address" + " LIMIT 1");
            if (c.moveToFirst())
            {
                lastNumber = c.getString(c.getColumnIndex("address"));
            }
            //type = USSDMessageType.NORMAL_SMS;
            //time, cost, balance, lastNumber, message
            ussdDetails = new NormalSMS();
            ((NormalSMS)ussdDetails)
                    .USSDDetails(
                            time,
                            ConstantsAndStatics.USSD_TYPES.NORMAL_SMS,
                            balance,
                            cost,
                            message);
            Log.d(TAG + " SMS", "details = " + ussdDetails.toString());
            return true;
        }
        return false;
    }

    private boolean parseForDataPack(String message)
    {
        Float data_consumed = (float) 0.0, bal = (float) 0.0, data_left = (float) 0.0;
        int count = 0;
        //3G usage:0.04MB 3G Bal:1048.45MB Val:Jun 02 2015 Bal:Rs.93.35
        //USSD2G usage:9.91MB 2G Bal:163.65MB Val:May 13 2015 Bal:Rs.60.44
        //Data_CHRG:0.00 INR, Bal_Left=71.85 INR, Vol_Used:0.027343 MB,Freebie_bal:492.76 MB, Pack_exp: May 13 2015
        //Ur last data session Usage was: 0.018 MB, Ur Data pack is 3G 1 GB Pack, 557.98 MB can be used till 2015-05-24, Unbilled amount for data usage is Rs 0.00
        //Data Session Charge:Rs 0. Bal Rs.0.01. Vol Used :0.06 MB. Data Left: 748 MB. Val :24/05/2015.
        //CHRG:0.00INR,VOL:0.064MB, Available 3G Pack Benefit 212.720MB,Val till 24-05-2015,BAL-LEFT:99.560INR.
        //Data Session Charge:Rs 0. Bal Rs.0.01. Vol Used :0.00 MB. Data Left: 749 MB. Val :24/05/2015.
        //Your last call of 10Kb was charged from 50MBFREE.Remaining Balance 46Mb 806Kb. Your Main Account Balance: 15.155
        try
        {
            String dataUsedRegex = mLoki.getData_pack_dataUsedRegex();//"[usage|Vol_Used|last data session Usage|VOL|Vol|vol|USAGE|Usage|Vol Used|vol used|VOL USED|InternetUsage|DataUsage|Data_Usage|Data\\-Usage|Consumed volume|Consumed_volume|ConsumedVolume|Vol_Used|Volume Used|Vol\\-Used][\\s+]?:?[\\s+]?(\\d+\\.\\d+)[\\s+]?(MB|Mb)";
            Pattern pcc;
            Matcher mcc;
            pcc = Pattern.compile(dataUsedRegex);
            mcc = pcc.matcher(message);
            if (mcc.find())
            {
                data_consumed = Float.parseFloat(mcc.group(1));
                count++;
                //Log.d(TAG+" DATA ", "Found Data Pack Consumed " + data_consumed);
            }
            String bsnlDataUsedRegex = mLoki.getData_pack_bsnlDataUsedRegex1();//"Your last call of (\\d+)Mb|MB\\s*(\\d+)Kb|KB";
            pcc = Pattern.compile(bsnlDataUsedRegex);
            mcc = pcc.matcher(message);
            if (mcc.find())
            {
                //V10System.out.println("Group 2 "+mcc.group(2));
                data_consumed = Float.parseFloat(mcc.group(1)) + (Float.parseFloat(mcc.group(2)) / 1000);
                count++;
                System.out.println(TAG + " DATA BSNL" + "Found Data Pack Consumed " + data_consumed);
            } else
            {
                bsnlDataUsedRegex = mLoki.getData_pack_bsnlDataUsedRegex2();//"Your last call of\\s*(\\d+)Kb|KB";
                pcc = Pattern.compile(bsnlDataUsedRegex);
                mcc = pcc.matcher(message);
                if (mcc.find())
                {

                    data_consumed = Float.parseFloat(mcc.group(1)) / 1000;
                    count++;
                    System.out.println(TAG + " DATA BSNL" + "Found Data Pack Consumed " + data_consumed);
                }
            }
            String dataLeftRegex = mLoki.getData_pack_dataLeftRegex();//"(Bal|BAL|bal|Data Left|DATA LEFT|data left|Available 3G Pack Benefit|Available 2G Pack Benefit|Freebie_bal|Data_Left)\\s*:?\\s*(\\d+\\.?\\d*)\\s?MB";
            pcc = Pattern.compile(dataLeftRegex);
            mcc = pcc.matcher(message);
            if (mcc.find())
            {
                data_left = Float.parseFloat(mcc.group(2));
                count++;
                //Log.d(TAG+" DATA ", "Found Data Pack Left " + data_left);
            }
            //Your last call of 10Kb was charged from 50MBFREE.Remaining Balance 46Mb 806Kb. Your Main Account Balance: 15.155
            String bsnlDataLeftRegex = mLoki.getData_pack_bsnlDataLeftRegex1();//"Remaining Balance (\\d+)(Mb|MB)\\s*(\\d+)Kb|KB";
            pcc = Pattern.compile(bsnlDataLeftRegex);
            mcc = pcc.matcher(message);
            if (mcc.find())
            {

                //V10System.out.println("Group 1 "+mcc.group(1));
                //V10System.out.println("Group 2 "+mcc.group(3));
                data_left = Float.parseFloat(mcc.group(1)) + (Float.parseFloat(mcc.group(3)) / 1000);
                count++;
                System.out.println(TAG + " DATA BSNL" + "Found Data Pack data_left " + data_left);
            } else
            {
                bsnlDataLeftRegex = mLoki.getData_pack_bsnlDataLeftRegex2();//"Remaining Balance\\s*(\\d+)Kb|KB";
                pcc = Pattern.compile(bsnlDataLeftRegex);
                mcc = pcc.matcher(message);
                if (mcc.find())
                {

                    data_left = Float.parseFloat(mcc.group(1)) / 1000;
                    count++;
                    //Log.d(TAG+" DATA BSNL", "Found Data Pack data_left " + data_consumed);
                }
            }

            // get the remaining balance there are two different types to get main balance
            boolean flag = false;
            String remBalanceRegex = mLoki.getData_pack_remBalanceRegex1();//"(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|Main Account Balance)\\s*:?\\s*(Rs)?\\s*[\\.]?=?\\s*(\\d+\\.\\d+)";
            pcc = Pattern.compile(remBalanceRegex);
            mcc = pcc.matcher(message);
            if (mcc.find())
            {
                count++;
                flag = true;
                bal = Float.parseFloat(mcc.group(3));
                System.out.println(TAG + " DATA " + "Found DataPack Rs bal " + bal.toString());
            }
            //hack to the problem posed by BSNL
            remBalanceRegex = mLoki.getData_pack_remBalanceRegex2();//"(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|Main Account Balance)\\s*:?\\s*(Rs)\\s*[\\.]?=?\\s*(\\d+\\.\\d+)";
            pcc = Pattern.compile(remBalanceRegex);
            mcc = pcc.matcher(message);
            if (mcc.find())
            {
                if (!flag) count++;

                bal = Float.parseFloat(mcc.group(3));
                System.out.println(TAG + " DATA " + "Found Hack DataPack Rs bal " + bal.toString());
            } else
            {
                String INRremBalanceRegex = mLoki.getData_pack_INRremBalanceRegex();//"(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left)\\s*:?\\s*R?s?\\s*[\\.]?=?\\s*(\\d+\\.\\d+)\\s*INR";
                pcc = Pattern.compile(INRremBalanceRegex);
                mcc = pcc.matcher(message);
                if (mcc.find())
                {
                    count++;
                    bal = Float.parseFloat(mcc.group(2));
                    //Log.d(TAG +" DATA ", "Found DataPack INR bal " + bal.toString());
                }
            }
            String validityRegex = mLoki.getData_pack_validityRegex();//"(Val|val|VAL|Pack_exp|can be used till|Val till)\\s*:?\\s*(\\d\\d/\\d\\d/\\d\\d\\d\\d|\\d\\d\\-\\d\\d\\-\\d\\d\\d\\d|\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d|[a-zA-Z]{3}\\s\\d\\d\\s\\d\\d\\d\\d)";
            pcc = Pattern.compile(validityRegex);
            mcc = pcc.matcher(message);
            String validity = "Unknown";
            if (mcc.find())
            {
                count++;
                validity = mcc.group(2);
                System.out.println(TAG + " DATA " + "Found DataPack Validity " + mcc.group(2));
            }
            if (count >= 3)
            {
                //Log.d(TAG+" DATA ","Found Data pack Message");
                //type = USSDMessageType.DATA_PACK;
                //details = new DataPack((new Date()).getTime(), data_consumed, data_left, bal, validity, message);
                ussdDetails = new PackData();
                ((PackData)ussdDetails).USSDDetails(
                        (new Date()).getTime(),
                        ConstantsAndStatics.USSD_TYPES.PACK_DATA,
                        bal,
                        null,
                        validity,
                        data_consumed,
                        data_left,
                        message
                );
                return true;
            }

            //Log.d(TAG+" DATA ","NOT a Data pack Message");
        } catch (Exception e)
        {
            e.printStackTrace();
            //Log.d(TAG, "Failed to parse");
        }
        return false;
    }

    private Matcher findDetails(JSONArray regexArray, String message) throws JSONException
    {
        int length = regexArray.length();
        Log.d(TAG, "Trying with " + length + "Regexes");
        Pattern mPattern;
        Matcher mMatcher;
        String regex;
        for (int i = 0; i < length; i++)
        {
            regex = regexArray.getJSONObject(i).getString("REGEX");
            Log.d(TAG, "Trying with : " + regex);
            mPattern = Pattern.compile(regex);
            mMatcher = mPattern.matcher(message);
            if (mMatcher.find())
            {
                Log.d(TAG, "Found a Match");
                return mMatcher;

            }
        }
        Log.d(TAG, "No Match Found");
        return null;
    }


    private boolean parseForNormalCall(String message)
    {
        int secs = 0, count = 0;
        Float bal = (float) 0.0, callCost = (float) 0.0;
        Long time = (new Date()).getTime() + 19800l;//to ist
        Log.d(TAG+" CALL", "parseForNormalCall");
        Log.d(TAG+" CALL" + "from service", message);
        Log.d(TAG+" CALL" + "time from service", time.toString());

        Pattern pcc;
        Matcher mcc;
        // get the call cost
        //Call_Durn : 00:00:51, Call_CHRG: 0.849, Bal_Left= 70.997
        //Last call charge for 00:01:50 is from Main Bal:0.500.Main Bal:Rs.24.460,28-07-2027.RCH91 All India calls at 25p for 84days
        //Your last call cost Rs.0.225,talk time used 0:0:15,main balance left 15.365.
        //Your last call cost Rs.0.660,talk time used 0:0:44,main balance left 25.258.null
        try
        {

            String callCostRegex = mLoki.getNormal_call_costRegex();//"(Call|call|Voice|voice|VOICE|Last|LAST|last)?_?\\s*(Deduction:CORE BAL|Call charged from:Main Bal|Charge|call cost|CALL COST|Call Cost|charge|cost|Cost|COST|CHRG|CHARGE|from Main Bal|CHRG:main_cost|Usage|USAGE|usage)\\s*:?\\s*R?s?\\s*-?:?[\\.=]?\\s*(\\d+\\.\\d+)";
            pcc = Pattern.compile(callCostRegex);
            mcc = pcc.matcher(message);

            if (mcc.find())
            {
                callCost = Float.parseFloat(mcc.group(3));
                count++;
                //Log.d(TAG+" CALL", "Found callCost " + callCost.toString());
                // System.out.println("Airtel call cost: " + mcc.group(1));
            }
            boolean flag = false;
            // get the remaining balance
            String remBalanceRegex = mLoki.getNormal_call_remBalanceRegex(); //"(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|main balance left|balance left|Current bal is|BAL_LEFT: main|BAL_LEFT:main|BAL_LEFT : main|BAL_LEFT :main|Balance :Talktime|Remaing Main Account Bal)\\s*:?-?\\s?R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)\\s*(INR)?";
            pcc = Pattern.compile(remBalanceRegex);
            mcc = pcc.matcher(message);
            if (mcc.find())
            {
                flag = true;
                count++;
                bal = Float.parseFloat(mcc.group(2));
                //Log.d(TAG +" CALL", "Found bal " + bal.toString());
            }

            {
                //Rs not optional overide the previous
                remBalanceRegex = mLoki.getNormal_call_remBalanceRegex2();//"(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|main balance left|balance left|Main Bal:|BAL_LEFT: main|BAL_LEFT:main|BAL_LEFT : main|BAL_LEFT :main|Balance :Talktime):?\\s*Rs\\s*[\\.=]?\\s*(\\d+\\.\\d+)";
                pcc = Pattern.compile(remBalanceRegex);
                mcc = pcc.matcher(message);
                if (mcc.find())
                {
                    if (!flag) count++;
                    bal = Float.parseFloat(mcc.group(2));
                    System.out.println(TAG + " CALL" + "Found bal  " + bal.toString());
                }
            }
            if (message.contains("Remaining bal after the call: Main Bal") || message.contains("RemainingBal:CORE BAL"))
            {
                remBalanceRegex = mLoki.getNormal_call_remBalanceRegex3();//"(Remaining bal after the call: Main Bal|RemainingBal:CORE BAL):?\\s*R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)";
                pcc = Pattern.compile(remBalanceRegex);
                mcc = pcc.matcher(message);
                if (mcc.find())
                {

                    count++;
                    bal = Float.parseFloat(mcc.group(2));
                    System.out.println(TAG + " CALL" + "Found IDEA bal " + bal.toString());
                }

            }
            if (message.contains("Available Main Bal"))
            {
                remBalanceRegex = mLoki.getNormal_call_remBalanceRegex4(); //"(Available Main Bal|AVAILABLE MAIN BAL|available main bal):?\\s*R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)";
                pcc = Pattern.compile(remBalanceRegex);
                mcc = pcc.matcher(message);
                if (mcc.find())
                {

                    count++;
                    callCost = bal;
                    bal = Float.parseFloat(mcc.group(2));
                    System.out.println(TAG + " CALL" + "Found Reliance bal " + bal.toString());
                }
            }
            //Call_Durn : 00:00:51,
            // get the call duration

            String callDurationRegex = mLoki.getNormal_call_callDurationRegex1();//"\\d+:\\d+:\\d+";

            pcc = Pattern.compile(callDurationRegex);
            mcc = pcc.matcher(message);

            if (mcc.find())
            {
                String callDuration = mcc.group();
                //Log.d(TAG +" CALL"+ "call Duration", callDuration);
                String sub[] = callDuration.split(":");
                //Log.d(TAG + "call sec 0 ", sub[0]);
                //Log.d(TAG + "call sec 1 ", sub[1]);
                //Log.d(TAG + "call sec 2 ", sub[2]);
                secs = Integer.parseInt(sub[0]) * 60 * 60;
                secs += Integer.parseInt(sub[1]) * 60;
                secs += Integer.parseInt(sub[2]);
                //Log.d(TAG +" CALL"+ "found airtel/vodafone call Duration ",secs + " ");
                count++;
            } else
            {

                callDurationRegex = mLoki.getNormal_call_callDurationRegex2();//"(duration|Duration|DURATION|DURN|durn|DUR|dur|Dur|Call_Durn:):?\\s*(\\d+)\\s*(Sec|sec|SEC)(s|S)?";
                pcc = Pattern.compile(callDurationRegex);
                mcc = pcc.matcher(message);
                if (mcc.find())
                {

                    secs = Integer.parseInt(mcc.group(2));
                    //Log.d(TAG+" CALL", "Found Docomo/idea/aircel" + secs);
                    count++;
                }
            }
            if (secs > 36000)
            {
                callDurationRegex = mLoki.getNormal_call_callDurationRegex3();//"(duration|Duration|DURATION|DURN|durn|DUR|dur|Dur|Call_Durn:):?\\s*(\\d+)\\s*(Sec|sec|SEC)(s|S)?";
                pcc = Pattern.compile(callDurationRegex);
                mcc = pcc.matcher(message);
                if (mcc.find())
                {

                    secs = Integer.parseInt(mcc.group(2));
                    //Log.d(TAG+" CALL", "Found Docomo/idea/aircel" + secs);
                    count++;
                }
            }
            if (count >= 3)
            {
                //Log.d(TAG +" CALL", "message was of NormalCall");
                //type = USSDMessageType.NORMAL_CALL;
                //details = new NormalCall(time, callCost, bal, secs, message);
                ussdDetails = new NormalCall();
                ((NormalCall)ussdDetails).USSDDetails(time,
                        ConstantsAndStatics.USSD_TYPES.NORMAL_CALL,
                        bal,
                        callCost,
                        secs,
                        message);
                return true;
            }

            //Log.d(TAG+" CALL", "message was of NOT OF NormalCall");
        } catch (Exception e)
        {
            e.printStackTrace();
            //Log.d(TAG, "Failed to parse");
        }

        return false;
    }

    private boolean parseForNormalData(String message)
    {
        //Log.d(TAG+" DATA", "parseForNormalData");
        int count = 0;
        Float bal = (float) 0.0, cost = (float) 0.0, data_consumed = (float) 0.0;
        Long time = (new Date()).getTime();
        //Log.d(TAG+" DATA" + "from service", message);
        //Log.d(TAG+" DATA" + "time from service", time.toString());

        Pattern pcc;
        Matcher mcc;
        // get the call cost
        //docomo Data Session Charge:Rs 0.10. Bal Rs.6.23. Vol Used :0.00 MB. .
        //Session date:19.05.2015, Session cost: 0.96INR, Consumed volume: 0.232MB, Current Balance: 27.10INR. Dial *121# for Internet Packs.
        try
        {
            String costRegex = mLoki.getNormal_data_costRegex();//"[Data|DATA|data]?(Session|session|SESSION)\\s*(Charge|charge|cost|Cost|CHRG|CHARGE)\\s*:?\\s?R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)[INR]?";
            pcc = Pattern.compile(costRegex);
            mcc = pcc.matcher(message);

            if (mcc.find())
            {
                cost = Float.parseFloat(mcc.group(3));
                count++;
                //Log.d(TAG+" DATA", "Found Normal Data Cost " + cost.toString());
                // System.out.println("Airtel call cost: " + mcc.group(1));
            }

            String dataConsumedRegex = mLoki.getNormal_data_dataConsumedRegex();//"(DataUsage|Data_Usage|Data-Usage|Consumed volume|Consumed_volume|ConsumedVolume|Vol Used|Vol_Used|Volume Used|Vol-Used)\\s*:?\\s*(\\d+\\.\\d+)\\s?(MB|mb|Mb)";
            pcc = Pattern.compile(dataConsumedRegex);
            mcc = pcc.matcher(message);

            if (mcc.find())
            {
                data_consumed = Float.parseFloat(mcc.group(2));
                count++;
                //System.out.println(TAG+" DATA "+ "Found Normal Data Consumed " + data_consumed);

                //Log.d(TAG+" DATA", "Found Normal Data Consumed " + data_consumed);
                // System.out.println("Airtel call cost: " + mcc.group(1));
            }


            // get the remaining balance
            String remBalanceRegex = mLoki.getNormal_data_remBalanceRegex();//"(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left)\\s*?:?\\s*?R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)";
            pcc = Pattern.compile(remBalanceRegex);
            mcc = pcc.matcher(message);
            if (mcc.find())
            {
                count++;
                bal = Float.parseFloat(mcc.group(2));
                //Log.d(TAG +" CALL", "Found bal " + bal.toString());
            }
            if (count >= 2)

            {
                //type = USSDMessageType.NORMAL_DATA;
                //details = new NormalData(time, cost, data_consumed, bal, message);
                ussdDetails = new NormalData();
                ((NormalData)ussdDetails).USSDDetails(time,
                        ConstantsAndStatics.USSD_TYPES.NORMAL_DATA,
                        bal,
                        cost,
                        data_consumed,
                        message);
                //Log.d(TAG+" DATA", "message was of NOT OF NormalDATA");
                return true;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            //Log.d(TAG, "Failed to parse");
        }
        return false;
    }


}
