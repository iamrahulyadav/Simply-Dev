package com.builder.ibalance.database.models;

public class NormalData extends DatabaseEntryBase
{
    public Long date;
    public Float cost;
    public Float data_consumed;
    public Float bal;
	public String message;
 
   public  NormalData(){}
 
    public NormalData(Long time, Float cost,Float data_consumed,Float bal, String message) {
        super();
        this.date = time;
        this.cost = cost;
        this.data_consumed = data_consumed;
        this.bal = bal;
        this.message = message;
    }
 
    //getters & setters
 
    @Override
    public String toString() {
        return "NormalData [date=" + date + ", Cost = "+cost + " data_consumed = "+data_consumed+", bal=" + bal
               + " Message = "+message+ "]";
    }

}
