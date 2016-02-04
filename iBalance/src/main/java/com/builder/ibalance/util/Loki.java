package com.builder.ibalance.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.builder.ibalance.R;
import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Shabaz on 18-Oct-15.
 */
public class Loki
{
    private static Charset PLAIN_TEXT_ENCODING = Charset.forName("UTF-8");
    private static String CIPHER_TRANSFORMATION = "AES/CTR/NoPadding";
    private static String KEY_TYPE = "AES";
    //TODO Change this if you updated the Miner
    private static final int VERSION = 4;
    // 192 and 256 bits may not be available
    private static int KEY_SIZE_BITS = 128;
    private SharedPreferences parserPreferences;
    private Cipher cipher;
    private SecretKey key;
    private IvParameterSpec iv;

    private String normal_call_costRegex = "0x22D76AD92D1293249DAFE80EA93A893F7E1583FD057D0EC0D593BE4A37E86EAF6E51D8F24248D994DCB1BC64F376D31B3E218396FC3BEA64164A4B067DF69C5CC813B3690A47CB8EBF9485466D63711E6EDC2714D3A6470F690384CA275DD7AA8BC0A9CCC24B69B010631DE29EAE9D62623100B5C55D3A1AB16B380F1653738789414F7169177C1CD042D13EC16E09BBBB69C737D00263DEF2172E4AA2F4FADF0C86DEF7937D35938558B1625257C1192BD1EEE4F2BC8A49E2698CD022710AA101520401A404B79BD9FF50D98A609ADFEB47B4E395FC1443CB9259231B57C7BA006BD23FF787E61D66E78C5DF9C998493DCC12F5C256EE3EA21EC5A1DC";
    private String normal_call_remBalanceRegex =  "0x22F66AD9200093208D81F534BA11AB167E218DF8077611F3E698BC430AEA4C99666FD5FF3C50E0BEE9BE8A2CA076A0222B7FD7BFF836F1270042480929D6B62EE156975C3A59E983B296C640256E661F7FC44407D3BB4F5B50428FC56B3FDFB58BC180E1EF60498A273851E3DFA49C6D540C0FABC5545C0DC4550D1A3B4E5DAAE55D6C475B3F3F4E915DD732D3712489834AC81DE5223FA7D02513589DFEC9F411ABF5D3E100248E8F41AA661F7FDC22209CCDECF0A7E94CD147D89D027B0791175303559A30A8D3E2E906B6E60CE2C6E40D95BD81A6616C92E33F327A79D2B44C32C53FE086970C00BCFC288B87EF5E28F16AF4A1";
    private String normal_call_remBalanceRegex2 = "0x22F66AD9200093208D81F534BA11AB167E218DF8077611F3E698BC430AEA4C99666FD5FF3C50E0BEE9BE8A2CA076A0222B7FD7BFF836F1270042480929D6B62EE156975C3A59E983B296C640256E661F7FC44A13C8A70A77450ED7D8455EFA99BBC687F98A0C61AD1A6C0DCCFF81AD5D530B17CEE4707337827A2D3F0A7E5AADFD221A22700A761ACD72F717E241238E9B35BE35C21F71E1FF2516578FDFCDB84A91F7DAAA4E19828603E4302E698A2236ADDFAFC59CE73CED11EACE6930389A49615929BC7AED";
    private String normal_call_remBalanceRegex3 = "0x22C66ED820079E2C9FA4B43AA73FCA3B641789E6466C1AF3BAB99C63279E2F917B44FA93537DC984EFA7A52CA54785292A49CABEA31CD055270366290B9CE931D140DB7A7948B7B3A0D2FE792B3F5E4657CB2D5AFDAD01690A3E898F2E";
    private String normal_call_remBalanceRegex4 = "0x22D57DD4280291279DA6B415A73A847A400280E8274E33DFD69BBF430E84429D5363B4F15050D999CBA3A121AD4B80226D66CABBF77FFD660E0A1E571BC6F95CB240CE743511D3B3FDC5F81A5971295157DC2C2E8F954E1E0D";
    private String normal_call_callDurationRegex1 = "0x56F0208F1D0ADB7FADA7BF";
    private String normal_call_callDurationRegex2 = "0x22F07EC7201A992A9FBFD02DB4329E336D0D90D0334A33C2D395B3730FF15D926649E1C17F60E1ADEFBEAC38BE55A8323F77E8B3F533C04317514A526E8FEC52FE19D9742210A1B3A0D28D7660617F0A6EDB7B21E48A031D571EBE8D38";
    private String normal_call_callDurationRegex3 = "0x22F07EC7201A992A9FBFD02DB4329E336D0D90D0334A33C2D395B3730FF15D926649E1C17F60E1ADEFBEAC38BE55A8323F77E8B3F533C04317514A526E8FEC52FE19D9742210A1B3A0D28D7660617F0A6EDB7B21E48A031D571EBE8D38";
    private String normal_data_costRegex = "0x51D06AC12012B404A582E83CA7278B073D4BBFF1156B1BF9F4A68E6A38D766B37451C7F6424FECB7F3EB943EE601AF2F2C79CCB7E53CF7661044411424DAA07AF1709E5B3247CBA781BFD9664D43513E4E915B018BF31569575DBF9B7420EAB5DDD89D838D713390002859D2DAE6AE3F4A2968DDD258540BA307";
    private String normal_data_dataConsumedRegex = "0x22D06AC1203B832496A6E81CA7278B0557108DF3036436F7EEBBD05A38C568B9666EFBDD6269C89DD9E2BE22A05C81223148C4BCEA2AF262067C52072BC0BE6BF1709E46354EE58AB7AECA49706F66055DD76B52F4BA4F51583482C8584AC5A393FF97C2DC5961A9535702EBDAB1A47E7A601687EC7533058D12564C094135C3F5660B5E33377B5F986CC564954024B4A277F815C15F";
    private String normal_data_remBalanceRegex = "0x22F66AD9200093208D81F534BA11AB167E218DF8077611F3E698BC430AEA4C99666FD5FF3C50E0BEE9BE8A2CA076A0222B7F828EEA75A03D5D7F574278E7EC7DB26F82021D67A6D28EC7F9562F2A5F1D20E4292EC5E203";
    private String data_pack_dataUsedRegex =  "0x51E178D4260B8C139EAFCB0DB5368E266E029FE0467C13E2FBFA8E6A38D766B3740DC1C0707BC084EB8D84319A46803B3B64C7AECC0CDE40275F711B26D2B672DB5C9D081348ED8BAF8ECA492577701C6FC4513DEDE97F66612691ED696BD3B499E6B5F8C34D6BA90F4610FADF98817071283FB0E8657B06AB4B0D14304E5B8ADD637C2F48187E13D44CF534D37E13A5AA71A42ECC1A6AF0D83839598FCFDDF515A1C9C0AE56058286569D601C69D51D2095FAEAF2B5A464CC78D9D11C4D179B0641211AB424A99F9ECF5FE9BD5D8BF5FB6EEAD781FC5942E3E410371A1BD7D764449E48D992E37E38F38B2F8B90EE5F3AE34BF6C30DED2FCE3EECE8DC";
    private String data_pack_bsnlDataUsedRegex1 = "0x53FB7EC76102913685E3F739AA3FCA356443C4C802335BDBF8A6B04D17D725F44649BF9A5A7ED9B3FF";
    private String data_pack_bsnlDataUsedRegex2 = "0x53FB7EC76102913685E3F739AA3FCA35643F9FBE4E4416BDB3919F7300E6";
    private String data_pack_dataLeftRegex = "0x22D66AD93D2CB1098DA1F534BA178B2E6343A0F1006C0ED2DB8EBC2F07E149886649F5C7703CC99DDBB6B40CBA48852B2C69C7B7B96CD8273242470367F7B660E855985C3A7AFE8EBA94C4476967234B4C985713C2A20A77410C88C26E6BCA8085E6A4CFD94953AE126E0DCADFB9934E5A282580A04D6973C40730007F1A438F825E0E3D410F355DED438916FF";
    private String data_pack_bsnlDataLeftRegex1 = "0x58F166D42800992B96E3D639AA3284396743C4C802335BBED7B88142098D53AF3005C8D73A35EE9AC1898A";
    private String data_pack_bsnlDataLeftRegex2 = "0x58F166D42800992B96E3D639AA328439673F9FBE4E4416BDB3919F7300E6";
    private String data_pack_remBalanceRegex1 = "0x22F66AD9200093208D81F534BA11AB167E218DF8077611F3E698BC430AEA4C99666FD5FF3C50E0BEE9BE8A2CA076A0222B7FD79FF836F1272340470732DBA72ECF529D492858EDC68F8B8F1F3A5E705323EA745B9E95591F7F3EC3F93822899A84A9E9F1D40750E22F665AA7";
    private String data_pack_remBalanceRegex2 = "0x22F66AD9200093208D81F534BA11AB167E218DF8077611F3E698BC430AEA4C99666FD5FF3C50E0BEE9BE8A2CA076A0222B7FD79FF836F1272340470732DBA72ECF529D492858EDC68F8B8F1F3A5E705323EA745BFDBA006E784CB09B3A20EAB5DDAB9DC99B702290172958";
    private String data_pack_INRremBalanceRegex = "0x22F66AD9200093208D81F534BA11AB167E218DF8077611F3E698BC430AEA4C99666FD5FF3C50E0BEE9BE8A2CA076A0222B7F828EEA75A5383E500E3A78C6EC52FE19AA746866B7D2ECA4D60F2D5E675257965B168AE076460E2BA3F6";
    private String data_pack_validityRegex = "0x22C26AD93D1891298D95D514BA038B39693C89EC166411F7F4FA9F6A6BD17CB97E0DE0DA7D70D9AEDCAEE839A545806E117881E8A603EC2D4A7F4034239A8F6AD157DE742267ECB3B7A4C15959665F1D57955B16FDAD76187806B1C05B7BEAA28BDFA5F1D4706890175E5CD2DA91964D3B1127A8ED6D4138D3422D5E0F6F64D8D45E535E79377B28C26CD207D9510294AB3C";
    private String normal_sms_smsCost = "0x22D86AC6354EA308A2BFC71595738935711790C72B4B52D5F5A9897318E95CFC5962C7E76D4FE8AB9DA1A02CBE4E89672B79C4BFB912FE6E0C0366092B9CE931D140DB7A7948B7B3A0D2FE792B3F5E4657CB2D5AFDAD01690A5DB1C02C36";
    private String normal_sms_smsBal1 = "0x22F66AD9200093208D81F534BA11AB167E218DF8077611F3E698BC430AEA4C99666FD5FF3C50E0BEE9BE8A2CA076A0222B7FD7BFF836F1270042480929D6B62EE156975C3A59E983B296C640256E661F7FC45517CCA8435B4D0C8AF74A4CF4A79BFF80CED34379A2072233EFD2AC9C72736D2A87A02B25058D123E4C260D439883597C2C20362028C21A9E07D9263AE6F049E0738A";
    private String normal_sms_smsBal2 = "0x22F66AD9200093208D81F534BA11AB167E218DF8077611F3E698BC430AEA4C99666FD5FF3C50E0BEE9BE8A2CA076A0222B7FD7BFF836F1270042480929D6B62EE156975C3A59E983B296C640256E661F7FC44A13C8A70A77450ED78D3D20EAB5DDD1B2F1C30657905D3F2CB1E2BED8394A2968A8A72E463DD511";

    public Loki() throws NoSuchAlgorithmException, NoSuchPaddingException,
            NoSuchProviderException
    {


        cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        String[] key_iv = getKeyIv();
        this.setKeyHex(key_iv[0]);
        this.setIVHex(key_iv[1]);
        //GOOGLE PREFS are the Pasrser Prefs
        parserPreferences = MyApplication.context.getSharedPreferences("GOOGLE_PREFS", Context.MODE_PRIVATE);
        boolean fisrtTime = parserPreferences.getBoolean("FISRT_TIME",true);
        int existingVersion = parserPreferences.getInt("EXISTING_VERSION",0);
        if(fisrtTime)
        {
            SharedPreferences.Editor editor = parserPreferences.edit();

            editor.putString("normal_call_costRegex", normal_call_costRegex);
            editor.putString("normal_call_remBalanceRegex", normal_call_remBalanceRegex);
            editor.putString("normal_call_remBalanceRegex2", normal_call_remBalanceRegex2);
            editor.putString("normal_call_remBalanceRegex3", normal_call_remBalanceRegex3);
            editor.putString("normal_call_remBalanceRegex4", normal_call_remBalanceRegex4);
            editor.putString("normal_call_callDurationRegex1", normal_call_callDurationRegex1);
            editor.putString("normal_call_callDurationRegex2", normal_call_callDurationRegex2);
            editor.putString("normal_call_callDurationRegex3", normal_call_callDurationRegex3);
            editor.putString("normal_data_costRegex", normal_data_costRegex);
            editor.putString("normal_data_dataConsumedRegex", normal_data_dataConsumedRegex);
            editor.putString("normal_data_remBalanceRegex", normal_data_remBalanceRegex);
            editor.putString("data_pack_dataUsedRegex",data_pack_dataUsedRegex );
            editor.putString("data_pack_bsnlDataUsedRegex1", data_pack_bsnlDataUsedRegex1);
            editor.putString("data_pack_bsnlDataUsedRegex2",data_pack_bsnlDataUsedRegex2 );
            editor.putString("data_pack_dataLeftRegex", data_pack_dataLeftRegex);
            editor.putString("data_pack_bsnlDataLeftRegex1", data_pack_bsnlDataLeftRegex1);
            editor.putString("data_pack_bsnlDataLeftRegex2", data_pack_bsnlDataLeftRegex2);
            editor.putString("data_pack_remBalanceRegex1",data_pack_remBalanceRegex1 );
            editor.putString("data_pack_remBalanceRegex2", data_pack_remBalanceRegex2);
            editor.putString("data_pack_INRremBalanceRegex", data_pack_INRremBalanceRegex);
            editor.putString("data_pack_validityRegex", data_pack_validityRegex);
            editor.putString("normal_sms_smsCost",normal_sms_smsCost );
            editor.putString("normal_sms_smsBal1",normal_sms_smsBal1 );
            editor.putString("normal_sms_smsBal2", normal_sms_smsBal2);
            editor.putBoolean("FISRT_TIME",false);
            editor.commit();
        }
        if(existingVersion<VERSION)
        {

            try
            {
            JSONObject assetJson = new JSONObject(loadJSONFromAsset());
            if(assetJson!=null)
            {
                SharedPreferences.Editor editor = parserPreferences.edit();


                    editor.putString("NORMAL_CALL", assetJson.getString("NORMAL_CALL"));
                    editor.putString("PACK_CALL", assetJson.getString("PACK_CALL"));
                    editor.putString("NORMAL_DATA", assetJson.getString("NORMAL_DATA"));
                    editor.putString("PACK_DATA", assetJson.getString("PACK_DATA"));
                    editor.putString("NORMAL_SMS", assetJson.getString("NORMAL_SMS"));
                    editor.putString("PACK_SMS", assetJson.getString("PACK_SMS"));
                    editor.putString("MAIN_BALANCE", assetJson.getString("MAIN_BALANCE"));
                    editor.putString("DATA_BALANCE", assetJson.getString("DATA_BALANCE"));
                    editor.putString("SMS_BALANCE", assetJson.getString("SMS_BALANCE"));
                    editor.putString("CALL_PACK_BALANCE", assetJson.getString("CALL_PACK_BALANCE"));
                editor.putInt("EXISTING_VERSION",VERSION);
                editor.commit();
            }
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }
    public JSONArray getMainBalRegex() throws JSONException
    {
        JSONArray mJsonArray = null;
        //New object is just for fall back, it should not happen
        //Dont mess up the upgardes, of you do it at some time then the ones in the Assets folder will be used
        try
        {
            mJsonArray = new JSONArray(parserPreferences.getString("MAIN_BALANCE",(new JSONObject(loadJSONFromAsset())).getString("MAIN_BALANCE")));
            mJsonArray = decryptJsonArray(mJsonArray);
        } catch (JSONException e)
        {
            mJsonArray = new JSONArray((new JSONObject(loadJSONFromAsset())).getString("MAIN_BALANCE"));
            mJsonArray = decryptJsonArray(mJsonArray);
            Crashlytics.logException(e);
        }

        return mJsonArray;
    }
    public JSONArray getNormalCallRegex() throws JSONException
    {
        JSONArray mJsonArray = null;
        //New object is just for fall back, it should not happen
        try{
        mJsonArray = new JSONArray(parserPreferences.getString("NORMAL_CALL",(new JSONObject(loadJSONFromAsset())).getString("NORMAL_CALL")));
        mJsonArray = decryptJsonArray(mJsonArray);
        } catch (JSONException e)
        {
            mJsonArray = new JSONArray((new JSONObject(loadJSONFromAsset())).getString("NORMAL_CALL"));
            mJsonArray = decryptJsonArray(mJsonArray);
            Crashlytics.logException(e);
        }
        return mJsonArray;
    }
    public JSONArray getPackCallRegex() throws JSONException
    {
        JSONArray mJsonArray = null;
        //New object is just for fall back, it should not happen
        try
        {
        mJsonArray = new JSONArray(parserPreferences.getString("PACK_CALL",(new JSONObject(loadJSONFromAsset())).getString("PACK_CALL")));
        mJsonArray = decryptJsonArray(mJsonArray);
        } catch (JSONException e)
        {
            mJsonArray = new JSONArray((new JSONObject(loadJSONFromAsset())).getString("PACK_CALL"));
            mJsonArray = decryptJsonArray(mJsonArray);
            Crashlytics.logException(e);
        }
        return mJsonArray;
    }
    public JSONArray getNormalSMSRegex() throws JSONException
    {
        JSONArray mJsonArray = null;
        //New object is just for fall back, it should not happen
        try
        {
        mJsonArray = new JSONArray(parserPreferences.getString("NORMAL_SMS",(new JSONObject(loadJSONFromAsset())).getString("NORMAL_SMS")));
        mJsonArray = decryptJsonArray(mJsonArray);
        } catch (JSONException e)
        {
            mJsonArray = new JSONArray((new JSONObject(loadJSONFromAsset())).getString("NORMAL_SMS"));
            mJsonArray = decryptJsonArray(mJsonArray);
            Crashlytics.logException(e);
        }
        return mJsonArray;
    }
    public JSONArray getPackSMSRegex() throws JSONException
    {
        JSONArray mJsonArray = null;
        //New object is just for fall back, it should not happen
        try
        {
        mJsonArray = new JSONArray(parserPreferences.getString("PACK_SMS",(new JSONObject(loadJSONFromAsset())).getString("PACK_SMS")));
        mJsonArray = decryptJsonArray(mJsonArray);
        } catch (JSONException e)
        {
            mJsonArray = new JSONArray((new JSONObject(loadJSONFromAsset())).getString("PACK_SMS"));
            mJsonArray = decryptJsonArray(mJsonArray);
            Crashlytics.logException(e);
        }
        return mJsonArray;
    }
    public JSONArray getNormalDataRegex() throws JSONException
    {
        JSONArray mJsonArray = null;
        //New object is just for fall back, it should not happen
        try
        {
        mJsonArray = new JSONArray(parserPreferences.getString("NORMAL_DATA",(new JSONObject(loadJSONFromAsset())).getString("NORMAL_DATA")));
        mJsonArray = decryptJsonArray(mJsonArray);
        } catch (JSONException e)
        {
            mJsonArray = new JSONArray((new JSONObject(loadJSONFromAsset())).getString("NORMAL_DATA"));
            mJsonArray = decryptJsonArray(mJsonArray);
            Crashlytics.logException(e);
        }
        return mJsonArray;
    }
    public JSONArray getPackDataRegex() throws JSONException
    {
        JSONArray mJsonArray = null;
        try
        {
        mJsonArray = new JSONArray(parserPreferences.getString("PACK_DATA",(new JSONObject(loadJSONFromAsset())).getString("PACK_DATA")));
        mJsonArray = decryptJsonArray(mJsonArray);
        } catch (JSONException e)
        {
            mJsonArray = new JSONArray((new JSONObject(loadJSONFromAsset())).getString("PACK_DATA"));
            mJsonArray = decryptJsonArray(mJsonArray);
            Crashlytics.logException(e);
        }
        return mJsonArray;
    }


    public String getNormal_call_costRegex()
    {
        return getRegex(parserPreferences.getString("normal_call_costRegex",normal_call_costRegex));
    }

    public String getNormal_call_remBalanceRegex()
    {
        return getRegex(parserPreferences.getString("normal_call_remBalanceRegex",normal_call_remBalanceRegex));

    }

    public String getNormal_call_remBalanceRegex2()
    {
        return getRegex(parserPreferences.getString("normal_call_remBalanceRegex2",normal_call_remBalanceRegex2));
    }

    public String getNormal_call_remBalanceRegex3()
    {
        return getRegex(parserPreferences.getString("normal_call_remBalanceRegex3",normal_call_remBalanceRegex3));
    }

    public String getNormal_call_remBalanceRegex4()
    {
        return getRegex(parserPreferences.getString("normal_call_remBalanceRegex4",normal_call_remBalanceRegex4));
    }

    public String getNormal_call_callDurationRegex1()
    {
        return getRegex(parserPreferences.getString("normal_call_callDurationRegex1",normal_call_callDurationRegex1));
    }

    public String getNormal_call_callDurationRegex2()
    {
        return getRegex(parserPreferences.getString("normal_call_callDurationRegex2",normal_call_callDurationRegex2));
    }

    public String getNormal_call_callDurationRegex3()
    {
        return getRegex(parserPreferences.getString("normal_call_callDurationRegex3",normal_call_callDurationRegex3));
    }

    public String getNormal_data_costRegex()
    {
        return getRegex(parserPreferences.getString("normal_data_costRegex",normal_data_costRegex));
    }

    public String getNormal_data_dataConsumedRegex()
    {
        return getRegex(parserPreferences.getString("normal_data_dataConsumedRegex",normal_data_dataConsumedRegex));

    }

    public String getNormal_data_remBalanceRegex()
    {
        return getRegex(parserPreferences.getString("normal_data_remBalanceRegex",normal_data_remBalanceRegex));
    }

    public String getData_pack_dataUsedRegex()
    {
        return getRegex(parserPreferences.getString("data_pack_dataUsedRegex",data_pack_dataUsedRegex));
    }

    public String getData_pack_bsnlDataUsedRegex1()
    {
        return getRegex(parserPreferences.getString("data_pack_bsnlDataUsedRegex1",data_pack_bsnlDataUsedRegex1));
    }

    public String getData_pack_bsnlDataUsedRegex2()
    {
        return getRegex(parserPreferences.getString("data_pack_bsnlDataUsedRegex2",data_pack_bsnlDataUsedRegex2));

    }

    public String getData_pack_dataLeftRegex()
    {
        return getRegex(parserPreferences.getString("data_pack_dataLeftRegex",data_pack_dataLeftRegex));
    }

    public String getData_pack_bsnlDataLeftRegex1()
    {
        return getRegex(parserPreferences.getString("data_pack_bsnlDataLeftRegex1",data_pack_bsnlDataLeftRegex1));
    }

    public String getData_pack_bsnlDataLeftRegex2()
    {
        return getRegex(parserPreferences.getString("data_pack_bsnlDataLeftRegex2",data_pack_bsnlDataLeftRegex2));
    }

    public String getData_pack_remBalanceRegex1()
    {
        return getRegex(parserPreferences.getString("data_pack_remBalanceRegex1",data_pack_remBalanceRegex1));
    }

    public String getData_pack_remBalanceRegex2()
    {
        return getRegex(parserPreferences.getString("data_pack_remBalanceRegex2",data_pack_remBalanceRegex2));
    }

    public String getData_pack_INRremBalanceRegex()
    {
        return getRegex(parserPreferences.getString("data_pack_INRremBalanceRegex",data_pack_INRremBalanceRegex));
    }

    public String getData_pack_validityRegex()
    {
        return getRegex(parserPreferences.getString("data_pack_validityRegex",data_pack_validityRegex));

    }

    public String getNormal_sms_smsCost()
    {
        return getRegex(parserPreferences.getString("normal_sms_smsCost",normal_sms_smsCost));

    }

    public String getNormal_sms_smsBal1()
    {
        return getRegex(parserPreferences.getString("normal_sms_smsBal1",normal_sms_smsBal1));

    }

    public String getNormal_sms_smsBal2()
    {
        return getRegex(parserPreferences.getString("normal_sms_smsBal2",normal_sms_smsBal2));
    }


    private String[] getKeyIv()
    {
         String ret[] = {MyApplication.context.getString(R.string.analytics_key),
            MyApplication.context.getString(R.string.flurry_key)};
        return ret;
    }

    public void setKeyHex(String keyText) {

        byte[] bText = hexStringToByteArray(keyText);
        if (bText.length * Byte.SIZE != KEY_SIZE_BITS) {
            throw new IllegalArgumentException(
                    "Wrong key size, expecting " + KEY_SIZE_BITS / Byte.SIZE + " bytes in hex");
        }
        key = new SecretKeySpec(bText, KEY_TYPE);
    }

    public void setIVHex(String ivText)
    {
        byte[] bText = hexStringToByteArray(ivText);
        if (bText.length != cipher.getBlockSize()) {
            throw new IllegalArgumentException(
                    "Wrong IV size, expecting " + cipher.getBlockSize() + " bytes in hex");
        }
        iv = new IvParameterSpec(bText);
    }

    public String encrypt(String message) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException
    {
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encrypted = cipher.doFinal(message.getBytes(PLAIN_TEXT_ENCODING));
        return byteArrayToHexString(encrypted);
    }

    public String decrypt(String hexCiphertext) throws IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException,
            UnsupportedEncodingException
    {
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] dec = hexStringToByteArray(hexCiphertext);
        byte[] decrypted = cipher.doFinal(dec);
        return new String(decrypted, PLAIN_TEXT_ENCODING);
    }

    private static String byteArrayToHexString(byte[] raw) {
        StringBuilder sb = new StringBuilder(2 + raw.length * 2);
        sb.append("0x");
        for (int i = 0; i < raw.length; i++) {
            sb.append(String.format("%02X", Integer.valueOf(raw[i] & 0xFF)));
        }
        return sb.toString();
    }

    // better add some input validation
    private static byte[] hexStringToByteArray(String hex) {
        Pattern replace = Pattern.compile("^0x");
        String s = replace.matcher(hex).replaceAll("");

        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }
/*
    public static String normal_call_costRegex = "(Call|call|Voice|voice|VOICE|Last|LAST|last)?_?\\s*(Deduction:CORE BAL|Call charged from:Main Bal|Charge|call cost|CALL COST|Call Cost|charge|cost|Cost|COST|CHRG|CHARGE|from Main Bal|CHRG:main_cost|Usage|USAGE|usage)\\s*:?\\s*R?s?\\s*-?:?[\\.=]?\\s*(\\d+\\.\\d+)";
    public static String normal_call_remBalanceRegex =  "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|main balance left|balance left|Current bal is|BAL_LEFT: main|BAL_LEFT:main|BAL_LEFT : main|BAL_LEFT :main|Balance :Talktime|Remaing Main Account Bal)\\s*:?-?\\s?R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)\\s*(INR)?";
    public static String normal_call_remBalanceRegex2 = "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|main balance left|balance left|Main Bal:|BAL_LEFT: main|BAL_LEFT:main|BAL_LEFT : main|BAL_LEFT :main|Balance :Talktime):?\\s*Rs\\s*[\\.=]?\\s*(\\d+\\.\\d+)";
    public static String normal_call_remBalanceRegex3 =  "(Remaining bal after the call: Main Bal|RemainingBal:CORE BAL):?\\s*R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)";
    public static String normal_call_remBalanceRegex4 =  "(Available Main Bal|AVAILABLE MAIN BAL|available main bal):?\\s*R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)";
    public static String normal_call_callDurationRegex1 = "\\d+:\\d+:\\d+";
    public static String normal_call_callDurationRegex2 = "(duration|Duration|DURATION|DURN|durn|DUR|dur|Dur|Call_Durn:):?\\s*(\\d+)\\s*(Sec|sec|SEC)(s|S)?";
    public static String normal_call_callDurationRegex3 = "(duration|Duration|DURATION|DURN|durn|DUR|dur|Dur|Call_Durn:):?\\s*(\\d+)\\s*(Sec|sec|SEC)(s|S)?";
    public static String normal_data_costRegex = "[Data|DATA|data]?(Session|session|SESSION)\\s*(Charge|charge|cost|Cost|CHRG|CHARGE)\\s*:?\\s?R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)[INR]?";
    public static String normal_data_dataConsumedRegex = "(DataUsage|Data_Usage|Data-Usage|Consumed volume|Consumed_volume|ConsumedVolume|Vol Used|Vol_Used|Volume Used|Vol-Used)\\s*:?\\s*(\\d+\\.\\d+)\\s?(MB|mb|Mb)";
    public static String normal_data_remBalanceRegex = "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left)\\s*?:?\\s*?R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)";
    public static String data_pack_dataUsedRegex =  "[usage|Vol_Used|last data session Usage|VOL|Vol|vol|USAGE|Usage|Vol Used|vol used|VOL USED|InternetUsage|DataUsage|Data_Usage|Data\\-Usage|Consumed volume|Consumed_volume|ConsumedVolume|Vol_Used|Volume Used|Vol\\-Used][\\s+]?:?[\\s+]?(\\d+\\.\\d+)[\\s+]?(MB|Mb)";
    public static String data_pack_bsnlDataUsedRegex1 = "Your last call of (\\d+)Mb|MB\\s*(\\d+)Kb|KB";
    public static String data_pack_bsnlDataUsedRegex2 = "Your last call of\\s*(\\d+)Kb|KB";
    public static String data_pack_dataLeftRegex = "(Bal|BAL|bal|Data Left|DATA LEFT|data left|Available 3G Pack Benefit|Available 2G Pack Benefit|Freebie_bal|Data_Left)\\s*:?\\s*(\\d+\\.?\\d*)\\s?MB";
    public static String data_pack_bsnlDataLeftRegex1 = "Remaining Balance (\\d+)(Mb|MB)\\s*(\\d+)Kb|KB";
    public static String data_pack_bsnlDataLeftRegex2 = "Remaining Balance\\s*(\\d+)Kb|KB";
    public static String data_pack_remBalanceRegex1 = "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|Main Account Balance)\\s*:?\\s*(Rs)?\\s*[\\.]?=?\\s*(\\d+\\.\\d+)";
    public static String data_pack_remBalanceRegex2 = "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|Main Account Balance)\\s*:?\\s*(Rs)\\s*[\\.]?=?\\s*(\\d+\\.\\d+)";
    public static String data_pack_INRremBalanceRegex = "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left)\\s*:?\\s*R?s?\\s*[\\.]?=?\\s*(\\d+\\.\\d+)\\s*INR";
    public static String data_pack_validityRegex = "(Val|val|VAL|Pack_exp|can be used till|Val till)\\s*:?\\s*(\\d\\d/\\d\\d/\\d\\d\\d\\d|\\d\\d\\-\\d\\d\\-\\d\\d\\d\\d|\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d|[a-zA-Z]{3}\\s\\d\\d\\s\\d\\d\\d\\d)";
    public static String normal_sms_smsCost = "(Last SMS|SMS cost|SMS Cost|SMS COST|SMS charge from Main Bal):?\\s*R?s?\\s*[\\.=]?\\s*(\\d+\\.?\\d+)";
    public static String normal_sms_smsBal1 = "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|main balance left|balance left|RemainingSMSBal|Account Balance is):?\\s*R?s?\\s*[\\.=]?\\s*(\\d+\\.?\\d+)";
    public static String normal_sms_smsBal2 = "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|main balance left|balance left|Main Bal:):?\\s*Rs\\s*[\\.=]?\\s*(\\d+\\.?\\d+)";
*/




    private String getRegex(String cipher)
    {
        String deciphered = "";
        try
        {
            deciphered = decrypt(cipher);
        } catch (IllegalBlockSizeException e)
        {
           //V10e.printStackTrace();
        } catch (BadPaddingException e)
        {
           //V10e.printStackTrace();
        } catch (InvalidKeyException e)
        {
           //V10e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e)
        {
           //V10e.printStackTrace();
        } catch (UnsupportedEncodingException e)
        {
           //V10e.printStackTrace();
        }
        return deciphered;
    }
    String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = MyApplication.context.getAssets().open("Miner.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
    }

    private JSONArray decryptJsonArray(JSONArray regexArray) throws JSONException
    {
        JSONArray decryptedRegexArray = new JSONArray();
        int length = regexArray.length();
        for (int i = 0; i <length; i++)
        {
            JSONObject mRegexObj = regexArray.getJSONObject(i);
            String regex = mRegexObj.getString("REGEX");
            regex = getRegex(regex);
            mRegexObj.put("REGEX",regex);
            decryptedRegexArray.put(mRegexObj);
        }
        return decryptedRegexArray;
    }



}
