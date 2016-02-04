package com.example;

/**
 * Created by Shabaz on 09-Dec-15.
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.example.Choice.ptln;


/*
 * Add state handling! Don't allow same key/iv for encrypting different cipher text!
 */
public class Encdec {

    private static Charset PLAIN_TEXT_ENCODING = Charset.forName("UTF-8");
    private static String CIPHER_TRANSFORMATION = "AES/CTR/NoPadding";
    private static String KEY_TYPE = "AES";
    // 192 and 256 bits may not be available
    private static int KEY_SIZE_BITS = 128;

    private Cipher cipher;
    private SecretKey key;
    private IvParameterSpec iv;
    String type = "MAIN_BALANCE_PATTERNS";//No need
    File folder = new File("G:/SimplyV2/TextMining/json/V4");
    //File regexFile = new File("G:/SimplyV2/TextMining/json/"+type+".json");
    File encryptedRegexFile; //= new File("G:/SimplyV2/TextMining/json/"+type+"_ENCRYPTED.json");
    File decryptedRegexFile;// = new File("G:/SimplyV2/TextMining/json/"+type+"_DECRYPTED.json");
    FileInputStream mFileReader ;
    byte[] data;
    public Encdec() throws JSONException, IOException
    {
        // not much use without a getter
//      final KeyGenerator kgen = KeyGenerator.getInstance(KEY_TYPE);
//      kgen.init(KEY_SIZE_BITS);
//      key = kgen.generateKey();
        ptln("Dir = "+folder.getName());
        try
        {
            cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (NoSuchPaddingException e)
        {
            e.printStackTrace();
        }
        ptln("1-Encrpyt\n2-Decrypt");
        switch (readChoice())
        {
            case 1:
            encryptJSON();
                break;
            case 2:
                decryptJSON();
        }
    }

    void encryptJSON() throws JSONException, IOException
    {
        JSONObject miner = new JSONObject();
        miner.put("DATA_BALANCE","");
        miner.put("SMS_BALANCE","");
        miner.put("CALL_PACK_BALANCE","");
        for (final File regexFile : folder.listFiles())
        {
            if(regexFile.isDirectory())
                continue;
            File encryptedRegexFile = new File(folder.getPath()+"/ENCRYPTED/"+regexFile.getName().replaceFirst("[.][^.]+$", "")+"_ENCRYPTED.json");
            try
            {
                String key = "0x000102030405060708090A0B0C0D0E0F";
                String iv = "0x000102030405060708090A0B0C0D0E0F";
                Encdec aes = this;
                if (!encryptedRegexFile.exists())
                {
                    encryptedRegexFile.createNewFile();
                }
                aes.setKeyHex(key);
                aes.setIVHex(iv);
                mFileReader = new FileInputStream(regexFile);


                data = new byte[(int) regexFile.length()];
                mFileReader.read(data);
                mFileReader.close();
                String str = new String(data, "UTF-8");
                JSONArray regexArray = null, encrypedRegexArray = new JSONArray(), callJson;
                regexArray = new JSONArray(str);
                int length = regexArray.length();
                for (int i = 0; i < length; i++)
                {
                    JSONObject mRegexObj = regexArray.getJSONObject(i);
                    String regex = mRegexObj.getString("REGEX");
                    com.google.code.regexp.Pattern.compile(regex);
                    regex = aes.encrypt(regex);
                    mRegexObj.put("REGEX", regex);
                    encrypedRegexArray.put(mRegexObj);
                }
                FileOutputStream mFileWriter = new FileOutputStream(encryptedRegexFile);
                mFileWriter.write(encrypedRegexArray.toString().getBytes());
                mFileWriter.close();
                if(!regexFile.getName().equals("All.txt"))
                 miner.put(regexFile.getName().replaceFirst("[.][^.]+$", ""),encrypedRegexArray.toString());

            } catch (GeneralSecurityException e)
            {
                throw new IllegalStateException("Poison Ivy ", e);
            } catch (java.io.IOException e)
            {
                // not always thrown even if decryption fails, add integrity check such as MAC
                throw new IllegalStateException("Decryption and/or decoding plain text message failed", e);
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
            {
            /*int l = array.length;
            for(int i=0;i<l;i++ )

            {
                System.out.println("O: "+array[i]);
           *//* String cipherHex = aes.encrypt(array[i]);
            System.out.println("\""+cipherHex+"\",");*//*
                String deciphered = aes.decrypt(enc[i]);
                System.out.println("D: "+deciphered);
                if(array[i].equals(deciphered))
                    System.out.println("Match");
                else
                {
                    System.out.println("No-Match");
                }
            }*/
            }
        }

        File finalMinerFile = new File(folder.getPath()+"/ENCRYPTED/"+"MINER"+"_ENCRYPTED.json");
        if(!finalMinerFile.exists())
            finalMinerFile.createNewFile();
        FileOutputStream mFileWriter = new FileOutputStream(finalMinerFile);
        mFileWriter.write(miner.toString().getBytes());
        mFileWriter.close();
    }

    private void decryptJSON()
    {
        try {
            String key = "0x000102030405060708090A0B0C0D0E0F";
            String iv = "0x000102030405060708090A0B0C0D0E0F";
            Encdec aes = this;
            if(!decryptedRegexFile.exists())
            {
                decryptedRegexFile.createNewFile();
            }
            aes.setKeyHex(key);
            aes.setIVHex(iv);
            mFileReader = new FileInputStream(encryptedRegexFile);


            data = new byte[(int) encryptedRegexFile.length()];
            mFileReader.read(data);
            mFileReader.close();
            String str = new String(data, "UTF-8");
            JSONArray regexArray = null,decryptedRegexArray = new JSONArray(), callJson;
            regexArray = new JSONArray(str);
            int length = regexArray.length();
            for (int i = 0; i <length; i++)
            {
                JSONObject mRegexObj = regexArray.getJSONObject(i);
                String regex = mRegexObj.getString("REGEX");
                regex = aes.decrypt(regex);
                mRegexObj.put("REGEX",regex);
                decryptedRegexArray.put(mRegexObj);
            }
            FileOutputStream mFileWriter = new FileOutputStream(decryptedRegexFile);
            mFileWriter.write(decryptedRegexArray.toString().getBytes());

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Poison Ivy ", e);
        } catch (java.io.IOException e) {
            // not always thrown even if decryption fails, add integrity check such as MAC
            throw new IllegalStateException("Decryption and/or decoding plain text message failed", e);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private int readChoice()
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try{
            return Integer.parseInt(br.readLine());
        }
        catch (Exception e)
        {
            ptln("Wrong Choice\n");
        }
        return 100;
    }

    public void setKeyHex(String keyText) {

        byte[] bText = hexStringToByteArray(keyText);
        if (bText.length * Byte.SIZE != KEY_SIZE_BITS) {
            throw new IllegalArgumentException(
                    "Wrong key size, expecting " + KEY_SIZE_BITS / Byte.SIZE + " bytes in hex");
        }
        key = new SecretKeySpec(bText, KEY_TYPE);
    }

    public void setIVHex(String ivText) {
        byte[] bText = hexStringToByteArray(ivText);
        if (bText.length != cipher.getBlockSize()) {
            throw new IllegalArgumentException(
                    "Wrong IV size, expecting " + cipher.getBlockSize() + " bytes in hex");
        }
        iv = new IvParameterSpec(bText);
    }

    public String encrypt(String message) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException {
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encrypted = cipher.doFinal(message.getBytes(PLAIN_TEXT_ENCODING));
        return byteArrayToHexString(encrypted);
    }

    public String decrypt(String hexCiphertext)
            throws IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException,
            UnsupportedEncodingException {
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


}

/*String array[]= {
                    "(Call|call|Voice|voice|VOICE|Last|LAST|last)?_?\\s*(Deduction:CORE BAL|Call charged from:Main Bal|Charge|call cost|CALL COST|Call Cost|charge|cost|Cost|COST|CHRG|CHARGE|from Main Bal|CHRG:main_cost|Usage|USAGE|usage)\\s*:?\\s*R?s?\\s*-?:?[\\.=]?\\s*(\\d+\\.\\d+)",
                    "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|main balance left|balance left|Current bal is|BAL_LEFT: main|BAL_LEFT:main|BAL_LEFT : main|BAL_LEFT :main|Balance :Talktime|Remaing Main Account Bal)\\s*:?-?\\s?R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)\\s*(INR)?",
                    "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|main balance left|balance left|Main Bal:|BAL_LEFT: main|BAL_LEFT:main|BAL_LEFT : main|BAL_LEFT :main|Balance :Talktime):?\\s*Rs\\s*[\\.=]?\\s*(\\d+\\.\\d+)",
                    "(Remaining bal after the call: Main Bal|RemainingBal:CORE BAL):?\\s*R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)",
                    "(Available Main Bal|AVAILABLE MAIN BAL|available main bal):?\\s*R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)",
                    "\\d+:\\d+:\\d+",
                    "(duration|Duration|DURATION|DURN|durn|DUR|dur|Dur|Call_Durn:):?\\s*(\\d+)\\s*(Sec|sec|SEC)(s|S)?",
                    "(duration|Duration|DURATION|DURN|durn|DUR|dur|Dur|Call_Durn:):?\\s*(\\d+)\\s*(Sec|sec|SEC)(s|S)?",
                    "[Data|DATA|data]?(Session|session|SESSION)\\s*(Charge|charge|cost|Cost|CHRG|CHARGE)\\s*:?\\s?R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)[INR]?",
                    "(DataUsage|Data_Usage|Data-Usage|Consumed volume|Consumed_volume|ConsumedVolume|Vol Used|Vol_Used|Volume Used|Vol-Used)\\s*:?\\s*(\\d+\\.\\d+)\\s?(MB|mb|Mb)",
                    "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left)\\s*?:?\\s*?R?s?\\s*[\\.=]?\\s*(\\d+\\.\\d+)",
                    "[usage|Vol_Used|last data session Usage|VOL|Vol|vol|USAGE|Usage|Vol Used|vol used|VOL USED|InternetUsage|DataUsage|Data_Usage|Data\\-Usage|Consumed volume|Consumed_volume|ConsumedVolume|Vol_Used|Volume Used|Vol\\-Used][\\s+]?:?[\\s+]?(\\d+\\.\\d+)[\\s+]?(MB|Mb)",
                    "Your last call of (\\d+)Mb|MB\\s*(\\d+)Kb|KB",
                    "Your last call of\\s*(\\d+)Kb|KB",
                    "(Bal|BAL|bal|Data Left|DATA LEFT|data left|Available 3G Pack Benefit|Available 2G Pack Benefit|Freebie_bal|Data_Left)\\s*:?\\s*(\\d+\\.?\\d*)\\s?MB",
                    "Remaining Balance (\\d+)(Mb|MB)\\s*(\\d+)Kb|KB",
                    "Remaining Balance\\s*(\\d+)Kb|KB",
                    "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|Main Account Balance)\\s*:?\\s*(Rs)?\\s*[\\.]?=?\\s*(\\d+\\.\\d+)",
                    "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|Main Account Balance)\\s*:?\\s*(Rs)\\s*[\\.]?=?\\s*(\\d+\\.\\d+)",
                    "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left)\\s*:?\\s*R?s?\\s*[\\.]?=?\\s*(\\d+\\.\\d+)\\s*INR",
                    "(Val|val|VAL|Pack_exp|can be used till|Val till)\\s*:?\\s*(\\d\\d/\\d\\d/\\d\\d\\d\\d|\\d\\d\\-\\d\\d\\-\\d\\d\\d\\d|\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d|[a-zA-Z]{3}\\s\\d\\d\\s\\d\\d\\d\\d)",
                    "(Last SMS|SMS cost|SMS Cost|SMS COST|SMS charge from Main Bal):?\\s*R?s?\\s*[\\.=]?\\s*(\\d+\\.?\\d+)",
                    "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|main balance left|balance left|RemainingSMSBal|Account Balance is):?\\s*R?s?\\s*[\\.=]?\\s*(\\d+\\.?\\d+)",
                    "(balance|Bal|BAL|Balance|BALANCE|BAL-LEFT|Bal_Left|main balance left|balance left|Main Bal:):?\\s*Rs\\s*[\\.=]?\\s*(\\d+\\.?\\d+)"};
*/

           /* String enc[]={

                    "0x22D76AD92D1293249DAFE80EA93A893F7E1583FD057D0EC0D593BE4A37E86EAF6E51D8F24248D994DCB1BC64F376D31B3E218396FC3BEA64164A4B067DF69C5CC813B3690A47CB8EBF9485466D63711E6EDC2714D3A6470F690384CA275DD7AA8BC0A9CCC24B69B010631DE29EAE9D62623100B5C55D3A1AB16B380F1653738789414F7169177C1CD042D13EC16E09BBBB69C737D00263DEF2172E4AA2F4FADF0C86DEF7937D35938558B1625257C1192BD1EEE4F2BC8A49E2698CD022710AA101520401A404B79BD9FF50D98A609ADFEB47B4E395FC1443CB9259231B57C7BA006BD23FF787E61D66E78C5DF9C998493DCC12F5C256EE3EA21EC5A1DC",
                    "0x22F66AD9200093208D81F534BA11AB167E218DF8077611F3E698BC430AEA4C99666FD5FF3C50E0BEE9BE8A2CA076A0222B7FD7BFF836F1270042480929D6B62EE156975C3A59E983B296C640256E661F7FC44407D3BB4F5B50428FC56B3FDFB58BC180E1EF60498A273851E3DFA49C6D540C0FABC5545C0DC4550D1A3B4E5DAAE55D6C475B3F3F4E915DD732D3712489834AC81DE5223FA7D02513589DFEC9F411ABF5D3E100248E8F41AA661F7FDC22209CCDECF0A7E94CD147D89D027B0791175303559A30A8D3E2E906B6E60CE2C6E40D95BD81A6616C92E33F327A79D2B44C32C53FE086970C00BCFC288B87EF5E28F16AF4A1",
                    "0x22F66AD9200093208D81F534BA11AB167E218DF8077611F3E698BC430AEA4C99666FD5FF3C50E0BEE9BE8A2CA076A0222B7FD7BFF836F1270042480929D6B62EE156975C3A59E983B296C640256E661F7FC44A13C8A70A77450ED7D8455EFA99BBC687F98A0C61AD1A6C0DCCFF81AD5D530B17CEE4707337827A2D3F0A7E5AADFD221A22700A761ACD72F717E241238E9B35BE35C21F71E1FF2516578FDFCDB84A91F7DAAA4E19828603E4302E698A2236ADDFAFC59CE73CED11EACE6930389A49615929BC7AED",
                    "0x22C66ED820079E2C9FA4B43AA73FCA3B641789E6466C1AF3BAB99C63279E2F917B44FA93537DC984EFA7A52CA54785292A49CABEA31CD055270366290B9CE931D140DB7A7948B7B3A0D2FE792B3F5E4657CB2D5AFDAD01690A3E898F2E",
                    "0x22D57DD4280291279DA6B415A73A847A400280E8274E33DFD69BBF430E84429D5363B4F15050D999CBA3A121AD4B80226D66CABBF77FFD660E0A1E571BC6F95CB240CE743511D3B3FDC5F81A5971295157DC2C2E8F954E1E0D",
                    "0x56F0208F1D0ADB7FADA7BF",
                    "0x22F07EC7201A992A9FBFD02DB4329E336D0D90D0334A33C2D395B3730FF15D926649E1C17F60E1ADEFBEAC38BE55A8323F77E8B3F533C04317514A526E8FEC52FE19D9742210A1B3A0D28D7660617F0A6EDB7B21E48A031D571EBE8D38",
                    "0x22F07EC7201A992A9FBFD02DB4329E336D0D90D0334A33C2D395B3730FF15D926649E1C17F60E1ADEFBEAC38BE55A8323F77E8B3F533C04317514A526E8FEC52FE19D9742210A1B3A0D28D7660617F0A6EDB7B21E48A031D571EBE8D38",
                    "0x51D06AC12012B404A582E83CA7278B073D4BBFF1156B1BF9F4A68E6A38D766B37451C7F6424FECB7F3EB943EE601AF2F2C79CCB7E53CF7661044411424DAA07AF1709E5B3247CBA781BFD9664D43513E4E915B018BF31569575DBF9B7420EAB5DDD89D838D713390002859D2DAE6AE3F4A2968DDD258540BA307",
                    "0x22D06AC1203B832496A6E81CA7278B0557108DF3036436F7EEBBD05A38C568B9666EFBDD6269C89DD9E2BE22A05C81223148C4BCEA2AF262067C52072BC0BE6BF1709E46354EE58AB7AECA49706F66055DD76B52F4BA4F51583482C8584AC5A393FF97C2DC5961A9535702EBDAB1A47E7A601687EC7533058D12564C094135C3F5660B5E33377B5F986CC564954024B4A277F815C15F",
                    "0x22F66AD9200093208D81F534BA11AB167E218DF8077611F3E698BC430AEA4C99666FD5FF3C50E0BEE9BE8A2CA076A0222B7F828EEA75A03D5D7F574278E7EC7DB26F82021D67A6D28EC7F9562F2A5F1D20E4292EC5E203",
                    "0x51E178D4260B8C139EAFCB0DB5368E266E029FE0467C13E2FBFA8E6A38D766B3740DC1C0707BC084EB8D84319A46803B3B64C7AECC0CDE40275F711B26D2B672DB5C9D081348ED8BAF8ECA492577701C6FC4513DEDE97F66612691ED696BD3B499E6B5F8C34D6BA90F4610FADF98817071283FB0E8657B06AB4B0D14304E5B8ADD637C2F48187E13D44CF534D37E13A5AA71A42ECC1A6AF0D83839598FCFDDF515A1C9C0AE56058286569D601C69D51D2095FAEAF2B5A464CC78D9D11C4D179B0641211AB424A99F9ECF5FE9BD5D8BF5FB6EEAD781FC5942E3E410371A1BD7D764449E48D992E37E38F38B2F8B90EE5F3AE34BF6C30DED2FCE3EECE8DC",
                    "0x53FB7EC76102913685E3F739AA3FCA356443C4C802335BDBF8A6B04D17D725F44649BF9A5A7ED9B3FF",
                    "0x53FB7EC76102913685E3F739AA3FCA35643F9FBE4E4416BDB3919F7300E6",
                    "0x22D66AD93D2CB1098DA1F534BA178B2E6343A0F1006C0ED2DB8EBC2F07E149886649F5C7703CC99DDBB6B40CBA48852B2C69C7B7B96CD8273242470367F7B660E855985C3A7AFE8EBA94C4476967234B4C985713C2A20A77410C88C26E6BCA8085E6A4CFD94953AE126E0DCADFB9934E5A282580A04D6973C40730007F1A438F825E0E3D410F355DED438916FF",
                    "0x58F166D42800992B96E3D639AA3284396743C4C802335BBED7B88142098D53AF3005C8D73A35EE9AC1898A",
                    "0x58F166D42800992B96E3D639AA328439673F9FBE4E4416BDB3919F7300E6",
                    "0x22F66AD9200093208D81F534BA11AB167E218DF8077611F3E698BC430AEA4C99666FD5FF3C50E0BEE9BE8A2CA076A0222B7FD79FF836F1272340470732DBA72ECF529D492858EDC68F8B8F1F3A5E705323EA745B9E95591F7F3EC3F93822899A84A9E9F1D40750E22F665AA7",
                    "0x22F66AD9200093208D81F534BA11AB167E218DF8077611F3E698BC430AEA4C99666FD5FF3C50E0BEE9BE8A2CA076A0222B7FD79FF836F1272340470732DBA72ECF529D492858EDC68F8B8F1F3A5E705323EA745BFDBA006E784CB09B3A20EAB5DDAB9DC99B702290172958",
                    "0x22F66AD9200093208D81F534BA11AB167E218DF8077611F3E698BC430AEA4C99666FD5FF3C50E0BEE9BE8A2CA076A0222B7F828EEA75A5383E500E3A78C6EC52FE19AA746866B7D2ECA4D60F2D5E675257965B168AE076460E2BA3F6",
                    "0x22C26AD93D1891298D95D514BA038B39693C89EC166411F7F4FA9F6A6BD17CB97E0DE0DA7D70D9AEDCAEE839A545806E117881E8A603EC2D4A7F4034239A8F6AD157DE742267ECB3B7A4C15959665F1D57955B16FDAD76187806B1C05B7BEAA28BDFA5F1D4706890175E5CD2DA91964D3B1127A8ED6D4138D3422D5E0F6F64D8D45E535E79377B28C26CD207D9510294AB3C",
                    "0x22D86AC6354EA308A2BFC71595738935711790C72B4B52D5F5A9897318E95CFC5962C7E76D4FE8AB9DA1A02CBE4E89672B79C4BFB912FE6E0C0366092B9CE931D140DB7A7948B7B3A0D2FE792B3F5E4657CB2D5AFDAD01690A5DB1C02C36",
                    "0x22F66AD9200093208D81F534BA11AB167E218DF8077611F3E698BC430AEA4C99666FD5FF3C50E0BEE9BE8A2CA076A0222B7FD7BFF836F1270042480929D6B62EE156975C3A59E983B296C640256E661F7FC45517CCA8435B4D0C8AF74A4CF4A79BFF80CED34379A2072233EFD2AC9C72736D2A87A02B25058D123E4C260D439883597C2C20362028C21A9E07D9263AE6F049E0738A",
                    "0x22F66AD9200093208D81F534BA11AB167E218DF8077611F3E698BC430AEA4C99666FD5FF3C50E0BEE9BE8A2CA076A0222B7FD7BFF836F1270042480929D6B62EE156975C3A59E983B296C640256E661F7FC44A13C8A70A77450ED78D3D20EAB5DDD1B2F1C30657905D3F2CB1E2BED8394A2968A8A72E463DD511"};
*/
/*public class EncDec {

	public static void main(String arg[]) {

		 String en[] = {"5Pnyy?pnyy?c|vpr??|vpr?c\\VPR?Yn???YN`a?yn??6LlLi?75Qrq?p?v|{GP\\_R-ONY?Pnyy-puntrq-s|zGZnv{-Ony?Puntr?pnyy-p|???PNYY-P\\`a?Pnyy-P|???puntr?p|???P|???P\\`a?PU_T?PUN_TR?s|z-Znv{-Ony?PU_TGznv{lp|???b?ntr?b`NTR???ntr6i?7GLi?7_L?Li?7:LGLhi;JjLi?75iq8i;iq86",
		     "5onyn{pr?Ony?ONY?Onyn{pr?ONYN[PR?ONY:YRSa?OnylYrs??znv{-onyn{pr-yrs??onyn{pr-yrs??P?r{?-ony-v??ONYlYRSaG-znv{?ONYlYRSaGznv{?ONYlYRSa-G-znv{?ONYlYRSa-Gznv{?Onyn{pr-Ganyx?vzr?_rznv{t-Znv{-Npp|?{?-Ony6i?7GL:Li?L_L?Li?7hi;JjLi?75iq8i;iq86i?75V[_6L",
		    "5onyn{pr?Ony?ONY?Onyn{pr?ONYN[PR?ONY:YRSa?OnylYrs??znv{-onyn{pr-yrs??onyn{pr-yrs??Znv{-OnyG?ONYlYRSaG-znv{?ONYlYRSaGznv{?ONYlYRSa-G-znv{?ONYlYRSa-Gznv{?Onyn{pr-Ganyx?vzr6GLi?7_?i?7hi;JjLi?75iq8i;iq86",
		     "5_rznv{v{t-ony-ns?r-?ur-pnyyG-Znv{-Ony?_rznv{v{tOnyGP\\_R-ONY6GLi?7_L?Li?7hi;JjLi?75iq8i;iq86",
		     "5N?nvynoyr-Znv{-Ony?NcNVYNOYR-ZNV[-ONY?n?nvynoyr-znv{-ony6GLi?7_L?Li?7hi;JjLi?75iq8i;iq86",
		    "iq8Giq8Giq8",
		    "5q?n?v|{?Q?n?v|{?Qb_NaV\\[?Qb_[?q?{?Qb_?q??Q??PnyylQ?{G6GLi?75iq86i?75`rp??rp?`RP65??`6L",
		    "5q?n?v|{?Q?n?v|{?Qb_NaV\\[?Qb_[?q?{?Qb_?q??Q??PnyylQ?{G6GLi?75iq86i?75`rp??rp?`RP65??`6L",
		    "hQn?n?QNaN?qn?njL5`r??v|{??r??v|{?`R``V\\[6i?75Puntr?puntr?p|???P|???PU_T?PUN_TR6i?7GLi?L_L?Li?7hi;JjLi?75iq8i;iq86hV[_jL",
		    "5Qn?nb?ntr?Qn?nlb?ntr?Qn?n:b?ntr?P|{??zrq-?|y?zr?P|{??zrql?|y?zr?P|{??zrqc|y?zr?c|y-b?rq?c|ylb?rq?c|y?zr-b?rq?c|y:b?rq6i?7GLi?75iq8i;iq86i?L5ZO?zo?Zo6",
		    "5onyn{pr?Ony?ONY?Onyn{pr?ONYN[PR?ONY:YRSa?OnylYrs?6i?7LGLi?7L_L?Li?7hi;JjLi?75iq8i;iq86",
		     "h??ntr?c|ylb?rq?yn??-qn?n-?r??v|{-b?ntr?c\\Y?c|y??|y?b`NTR?b?ntr?c|y-b?rq??|y-??rq?c\\Y-b`RQ?V{?r{r?b?ntr?Qn?nb?ntr?Qn?nlb?ntr?Qn?ni:b?ntr?P|{??zrq-?|y?zr?P|{??zrql?|y?zr?P|{??zrqc|y?zr?c|ylb?rq?c|y?zr-b?rq?c|yi:b?rqjhi?8jLGLhi?8jL5iq8i;iq86hi?8jL5ZO?Zo6",
		    "f|?-yn??-pnyy-|s-5iq86Zo?ZOi?75iq86Xo?XO",
		    "f|?-yn??-pnyy-|si?75iq86Xo?XO",
		    "5Ony?ONY?ony?Qn?n-Yrs??QNaN-YRSa?qn?n-yrs??N?nvynoyr-@T-]npx-Or{rsv??N?nvynoyr-?T-]npx-Or{rsv??Srrovrlony?Qn?nlYrs?6i?7GLi?75iq8i;Liq76i?LZO",
		    "_rznv{v{t-Onyn{pr-5iq865Zo?ZO6i?75iq86Xo?XO",
		    "_rznv{v{t-Onyn{pri?75iq86Xo?XO",
		    "5onyn{pr?Ony?ONY?Onyn{pr?ONYN[PR?ONY:YRSa?OnylYrs??Znv{-Npp|?{?-Onyn{pr6i?7GLi?75_?6Li?7hi;jLJLi?75iq8i;iq86",
		    "5onyn{pr?Ony?ONY?Onyn{pr?ONYN[PR?ONY:YRSa?OnylYrs??Znv{-Npp|?{?-Onyn{pr6i?7GLi?75_?6i?7hi;jLJLi?75iq8i;iq86",
		    "5onyn{pr?Ony?ONY?Onyn{pr?ONYN[PR?ONY:YRSa?OnylYrs?6i?7GLi?7_L?Li?7hi;jLJLi?75iq8i;iq86i?7V[_",
		    "5cny??ny?cNY?]npxlr?}?pn{-or-??rq-?vyy?cny-?vyy6i?7GLi?75iqiq<iqiq<iqiqiqiq?iqiqi:iqiqi:iqiqiqiq?iqiqiqiqi:iqiqi:iqiq?hn:?N:gj?@?i?iqiqi?iqiqiqiq6",
		    "5Yn??-`Z`?`Z`-p|???`Z`-P|???`Z`-P\\`a?`Z`-puntr-s|z-Znv{-Ony6GLi?7_L?Li?7hi;JjLi?75iq8i;Liq86",
		    "5onyn{pr?Ony?ONY?Onyn{pr?ONYN[PR?ONY:YRSa?OnylYrs??znv{-onyn{pr-yrs??onyn{pr-yrs??_rznv{v{t`Z`Ony?Npp|?{?-Onyn{pr-v?6GLi?7_L?Li?7hi;JjLi?75iq8i;Liq86",
		    "5onyn{pr?Ony?ONY?Onyn{pr?ONYN[PR?ONY:YRSa?OnylYrs??znv{-onyn{pr-yrs??onyn{pr-yrs??Znv{-OnyG6GLi?7_?i?7hi;JjLi?75iq8i;Liq86"};
		 int len = array.length;
		 String temp = "",dec="";
		 for(int i=0;i<len;i++)
		 {
			 StringBuilder sb = new StringBuilder();
			 temp = en[i];
			 for(int j=0;j<temp.length();j++)
			 {
				 sb.append((char)(temp.charAt(j)-13));
			 }
			 dec = sb.toString();
			 System.out.println("O: "+ array[i]);
			 System.out.println("E: "+ temp);
			 System.out.println("D: "+ dec);
			 if(array[i].equals(dec))
			 {
					System.out.println("Match");
				}
				else
				{
					System.out.println("Un-Match");
				}
		 }

//	int len = array.length;
//	String temp = "",enStr="",deStr="";
//
//	for(int i=0;i<len;i++)
//	{
//		StringBuilder en = new StringBuilder();
//		//StringBuilder de = new StringBuilder();
//		temp = array[i];
//		//System.out.println("0: "+temp);
//		for(int j=0;j<temp.length();j++)
//		{
//			en.append((char)(temp.charAt(j)+13));
//		}
//		enStr = en.toString();
//		System.out.println("\""+enStr+"\"");
//	/*	for(int j=0;j<enStr.length();j++)
//		{
//			de.append((char)(enStr.charAt(j)-13));
//		}
//		deStr = de.toString();
//		System.out.println("D: "+deStr);
//		if(temp.equals(deStr))
//		{
//			System.out.println("Match");
//		}
//		else
//		{
//			System.out.println("Un-Match");
//		}
//
//	}

}
}
*/