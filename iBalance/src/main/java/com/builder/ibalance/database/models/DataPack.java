package com.builder.ibalance.database.models;

public class DataPack extends DatabaseEntryBase
{
    public Long date;
    public Float data_left;
    public Float data_consumed;
    public Float bal;
	public String message;
	public String validity;
 
   public  DataPack(){}
 
    public DataPack(Long time, Float data_consumed,Float data_left,Float bal,String validity, String message) {
        super();
        this.date = time;
        this.data_left = data_left;
        this.data_consumed = data_consumed;
        this.bal = bal;
        this.validity = validity;
        this.message = message;
    }
 
    //getters & setters
 
    @Override
    public String toString() {
        return "NormalData [date=" + date + ", data_consumed = "+data_consumed + " data_left = "+data_left+", bal=" + bal +"  , validity = "+validity
               + " Message = "+message+ "]";
    }

}
