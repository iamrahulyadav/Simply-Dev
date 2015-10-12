package com.builder.ibalance.database.helpers;



/**
 * Created by Shabaz on 17-Aug-15.
 */
public class IbalanceContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String FLOAT_TYPE = " FLOAT";
    private static final String COMMA_SEP = ",";
    private static final String PK = " PRIMARY KEY";
    private static final String AINC = " AUTOINCREMENT";



    public static abstract class CallEntry  {
        public static final String TABLE_NAME = "CALL";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_SLOT = "SLOT";
        public static final String COLUMN_NAME_DATE= "DATE";
        public static final String COLUMN_NAME_COST = "COST";
        public static final String COLUMN_NAME_DURATION = "DURATION";
        public static final String COLUMN_NAME_NUMBER = "NUMBER";
        public static final String COLUMN_NAME_BALANCE = "BALANCE";
        public static final String COLUMN_NAME_MESSAGE = "MESSAGE";
    }

/*    final String CREATE_CALL_TABLE = "CREATE TABLE CALL ( "
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT , " + "DATE INTEGER  , "
            + "COST FLOAT, "  + "DURATION INTEGER, "
            + "NUMBER TEXT, "+ "BALANCE FLOAT, " +"MESSAGE TEXT"+" )";*/

    public static final String DROP_CALL_TABLE = "DROP TABLE IF EXISTS "+ CallEntry.TABLE_NAME;
    public static final String CREATE_CALL_TABLE ="CREATE TABLE IF NOT EXISTS " + CallEntry.TABLE_NAME + " (" +
            CallEntry.COLUMN_NAME_ID + INT_TYPE +PK+ AINC+ COMMA_SEP +
            CallEntry.COLUMN_NAME_SLOT + INT_TYPE + COMMA_SEP +
            CallEntry.COLUMN_NAME_DATE + INT_TYPE + COMMA_SEP +
            CallEntry.COLUMN_NAME_COST + FLOAT_TYPE + COMMA_SEP +
            CallEntry.COLUMN_NAME_DURATION + INT_TYPE + COMMA_SEP +
            CallEntry.COLUMN_NAME_BALANCE + FLOAT_TYPE + COMMA_SEP +
            CallEntry.COLUMN_NAME_MESSAGE + TEXT_TYPE + COMMA_SEP +
            CallEntry.COLUMN_NAME_NUMBER + TEXT_TYPE +
            " )";


   public final static String CREATE_VOICE_PACK_TABLE = "CREATE TABLE VOICE_PACK ( "
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT , " + "DATE INTEGER  , "
            + "DURATION INTEGER, "+ "NUMBER TEXT, "+ "REMAINING INTEGER, " +"VALIDITY TEXT, " +"MESSAGE TEXT"+ " )";

    public static abstract class VoicePackEntry  {
        public static final String TABLE_NAME = "VOICE_PACK";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_SLOT = "SLOT";
        public static final String COLUMN_NAME_DATE= "DATE";
        public static final String COLUMN_NAME_DURATION = "DURATION";
        public static final String COLUMN_NAME_REMAINING = "REMAINING";
        public static final String COLUMN_NAME_VALIDITY = "VALIDITY";
        public static final String COLUMN_NAME_NUMBER = "NUMBER";
        public static final String COLUMN_NAME_MESSAGE = "MESSAGE";
    }

    public final static String CREATE_SMS_TABLE = "CREATE TABLE SMS  ( "
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT , " + "DATE INTEGER  , "
            + "COST FLOAT, "  + "NUMBER TEXT, "+ "BALANCE FLOAT, " +"MESSAGE TEXT "+" )";

    public static abstract class SMSEntry  {
        public static final String TABLE_NAME = "SMS";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_SLOT = "SLOT";
        public static final String COLUMN_NAME_DATE= "DATE";
        public static final String COLUMN_NAME_COST= "COST";
        public static final String COLUMN_NAME_BALANCE = "BALANCE";
        public static final String COLUMN_NAME_NUMBER = "NUMBER";
        public static final String COLUMN_NAME_MESSAGE = "MESSAGE";
    }
    public final static String CREATE_SMS_PACK_TABLE = "CREATE TABLE SMS_PACK ( "
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT , " + "DATE INTEGER  , "
            + "NUMBER TEXT ,  "+ "REMAINING INTEGER, " +" VALIDITY TEXT, " +"MESSAGE TEXT"+ " )";

    public static abstract class SMSPackEntry  {
        public static final String TABLE_NAME = "SMS_PACK";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_SLOT = "SLOT";
        public static final String COLUMN_NAME_DATE= "DATE";
        public static final String COLUMN_NAME_REMAINING = "REMAINING";
        public static final String COLUMN_NAME_VALIDITY = "VALIDITY";
        public static final String COLUMN_NAME_NUMBER = "NUMBER";
        public static final String COLUMN_NAME_MESSAGE = "MESSAGE";
    }
    public final static String CREATE_DATA_TABLE = "CREATE TABLE DATA ( "
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT , " + "DATE INTEGER  , "
            + "COST FLOAT, "  +"DATA_CONSUMED FLOAT , "+ "BALANCE FLOAT, " +"MESSAGE TEXT" + " )";

    public static abstract class DataEntry  {
        public static final String TABLE_NAME = "DATA";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_SLOT = "SLOT";
        public static final String COLUMN_NAME_DATE= "DATE";
        public static final String COLUMN_NAME_COST= "COST";
        public static final String COLUMN_NAME_DATA_CONSUMED = "DATA_CONSUMED";
        public static final String COLUMN_NAME_BALANCE = "BALANCE";
        public static final String COLUMN_NAME_MESSAGE = "MESSAGE";
    }

    //TYPE: 0-2G 1-3G 2-4G
    public final static String CREATE_DATA_PACK_TABLE = "CREATE TABLE DATA_PACK ( "
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT , " + "DATE INTEGER  , "
            + "TYPE INTEGER, "+ "DATA_CONSUMED FLOAT,  "+ "REMAINING INTEGER, " +"VALIDITY TEXT, "+"BALANCE FLOAT , " +"MESSAGE TEXT"+ " )";

    public static abstract class DataPackEntry  {
        public static final String TABLE_NAME = "DATA_PACK";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_SLOT = "SLOT";
        public static final String COLUMN_NAME_DATE= "DATE";
        public static final String COLUMN_NAME_TYPE= "TYPE";
        public static final String COLUMN_NAME_DATA_CONSUMED = "DATA_CONSUMED";
        public static final String COLUMN_NAME_REMAINING = "REMAINING";
        public static final String COLUMN_NAME_VALIDITY = "VALIDITY";
        public static final String COLUMN_NAME_BALANCE = "BALANCE";
        public static final String COLUMN_NAME_MESSAGE = "MESSAGE";
    }

    public final static String CREATE_RECHARGE_TABLE = "CREATE TABLE RECHARGE ( "
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT , "+ "DATE INTEGER  , " + "RECHARGE_AMOUNT FLOAT, "
            + "BALANCE FLOAT, " +"MESSAGE TEXT" + " )";

    public static abstract class RechargeEntry  {
        public static final String TABLE_NAME = "RECHARGE";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_SLOT = "SLOT";
        public static final String COLUMN_NAME_DATE = "DATE";
        public static final String COLUMN_NAME_RECHARGE_AMOUNT = "RECHARGE_AMOUNT";
        public static final String COLUMN_NAME_BALANCE = "BALANCE";
        public static final String COLUMN_NAME_MESSAGE = "MESSAGE";
    }
    public final static String CREATE_SUSPICIOUS_TABLE = "CREATE TABLE SUSPICIOUS ( "
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT , "+ "DATE INTEGER  , " + "AMOUNT FLOAT " + " )";

    public static abstract class SuspiciousEntry  {
        //Amount = the money which was deducted, Balance = what the balance looked like after deduction will help in displaying graph properly
        public static final String TABLE_NAME = "SUSPICIOUS";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_SLOT = "SLOT";
        public static final String COLUMN_NAME_DATE = "DATE";
        public static final String COLUMN_NAME_AMOUNT = "AMOUNT";
        public static final String COLUMN_NAME_BALANCE = "BALANCE";
    }
    public IbalanceContract() {}


    //Number, name,InCount,InDur,OutCount,OutDur,MissCount,Provider,State,imageUri
    public static abstract class ContactDetailEntry  {
        public static final String TABLE_NAME = "CONTACT_DETAIL";
        public static final String COLUMN_NAME_NUMBER = "NUMBER";
        public static final String COLUMN_NAME_NAME= "NAME";
        public static final String COLUMN_NAME_IN_COUNT = "IN_COUNT";
        public static final String COLUMN_NAME_IN_DURATION = "IN_DURATION";
        public static final String COLUMN_NAME_OUT_COUNT= "OUT_COUNT";
        public static final String COLUMN_NAME_OUT_DURATION= "OUT_DURATION";
        public static final String COLUMN_NAME_MISS_COUNT= "MISS_COUNT";
        public static final String COLUMN_NAME_CARRIER= "CARRIER";
        public static final String COLUMN_NAME_CIRCLE= "CIRCLE";
        public static final String COLUMN_NAME_IMAGE_URI= "IMAGE_URI";
    }

    public static final String DROP_CONTACT_DETAIL_TABLE = "DROP TABLE IF EXISTS "+ ContactDetailEntry.TABLE_NAME;
    public static final String CREATE_CONTACT_DETAIL_TABLE = " CREATE TABLE IF NOT EXISTS " + ContactDetailEntry.TABLE_NAME+" ("+
            ContactDetailEntry.COLUMN_NAME_NUMBER + TEXT_TYPE + PK + COMMA_SEP +
            ContactDetailEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
            ContactDetailEntry.COLUMN_NAME_IN_COUNT + INT_TYPE + COMMA_SEP +
            ContactDetailEntry.COLUMN_NAME_IN_DURATION + INT_TYPE + COMMA_SEP +
            ContactDetailEntry.COLUMN_NAME_OUT_COUNT + INT_TYPE + COMMA_SEP +
            ContactDetailEntry.COLUMN_NAME_OUT_DURATION + INT_TYPE + COMMA_SEP +
            ContactDetailEntry.COLUMN_NAME_MISS_COUNT + INT_TYPE + COMMA_SEP +
            ContactDetailEntry.COLUMN_NAME_CARRIER+ TEXT_TYPE + COMMA_SEP +
            ContactDetailEntry.COLUMN_NAME_CIRCLE + TEXT_TYPE + COMMA_SEP +
            ContactDetailEntry.COLUMN_NAME_IMAGE_URI + TEXT_TYPE +

            " )";


    public static abstract class DateDurationEntry  {
        public static final String TABLE_NAME = "DATE_DURATION";
        public static final String COLUMN_NAME_DATE = "DATE";
        public static final String COLUMN_NAME_IN_DURATION = "IN_DURATION";
        public static final String COLUMN_NAME_IN_COUNT = "IN_COUNT";
        public static final String COLUMN_NAME_WEEK_DAY = "WEEK_DAY";
        public static final String COLUMN_NAME_OUT_DURATION= "OUT_DURATION";
        public static final String COLUMN_NAME_OUT_COUNT= "OUT_COUNT";
        public static final String COLUMN_NAME_MISS_COUNT= "MISS_COUNT";

    }

    public static final String DROP_DATE_DURATION_TABLE = "DROP TABLE IF EXISTS "+ DateDurationEntry.TABLE_NAME;
    public static final String CREATE_DATE_DURATION_TABLE = " CREATE TABLE IF NOT EXISTS " + DateDurationEntry.TABLE_NAME+" ("+
            DateDurationEntry.COLUMN_NAME_DATE + INT_TYPE + PK + COMMA_SEP +
            DateDurationEntry.COLUMN_NAME_IN_COUNT + INT_TYPE + COMMA_SEP +
            DateDurationEntry.COLUMN_NAME_IN_DURATION + INT_TYPE + COMMA_SEP +
            DateDurationEntry.COLUMN_NAME_WEEK_DAY+ INT_TYPE + COMMA_SEP +
            DateDurationEntry.COLUMN_NAME_OUT_COUNT + INT_TYPE + COMMA_SEP +
            DateDurationEntry.COLUMN_NAME_OUT_DURATION + INT_TYPE + COMMA_SEP +
            DateDurationEntry.COLUMN_NAME_MISS_COUNT + INT_TYPE +
            " )";

    /* Inner class that defines the table contents */
    public static abstract class IMSIEntry  {
        public static final String TABLE_NAME = "IMSI_MAPPING";
        public static final String COLUMN_NAME_IMSI = "IMSI";
        public static final String COLUMN_NAME_CARRIER = "CARRIER";
        public static final String COLUMN_NAME_CIRCLE = "CIRCLE";

    }
    public static abstract class CallLogEntry  {
        public static final String COLUMN_NAME_ID = "_ID";
        public static final String TABLE_NAME = "CALL_LOGS";
        public static final String COLUMN_NAME_SLOT = "SLOT";
        public static final String COLUMN_NAME_NUMBER= "NUMBER";
        public static final String COLUMN_NAME_DURATION = "DURATION";
        public static final String COLUMN_NAME_DATE = "DATE";
        public static final String COLUMN_NAME_TYPE = "TYPE";

    }


    public static final String DROP_CALL_LOG_TABLE = "DROP TABLE IF EXISTS "+ CallLogEntry.TABLE_NAME;
    public static final String CREATE_CALL_LOG_TABLE ="CREATE TABLE IF NOT EXISTS " + CallLogEntry.TABLE_NAME + " (" +
            CallLogEntry.COLUMN_NAME_ID + INT_TYPE +PK+ COMMA_SEP +
            CallLogEntry.COLUMN_NAME_DATE + INT_TYPE + COMMA_SEP +
            CallLogEntry.COLUMN_NAME_SLOT + INT_TYPE + COMMA_SEP +
            CallLogEntry.COLUMN_NAME_TYPE + INT_TYPE + COMMA_SEP +
            CallLogEntry.COLUMN_NAME_DURATION + INT_TYPE + COMMA_SEP +
            CallLogEntry.COLUMN_NAME_NUMBER + TEXT_TYPE +
            " )";

}
