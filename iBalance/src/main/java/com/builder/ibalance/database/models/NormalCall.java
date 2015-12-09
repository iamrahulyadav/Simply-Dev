package com.builder.ibalance.database.models;



public class NormalCall extends DatabaseEntryBase
{

    public Float callCost;
    public Float bal;
    public int callDuration;
    public int slot ;
    public String lastNumber;
	public String message;
    public long id;
    public NormalCall(){}
 //id,slot and last number will be added from the content observer
    public NormalCall(Long time, Float callCost,Float bal,int callDuration,String message) {
        super();
        this.date = time;
        this.callCost = callCost;
        this.bal = bal;
        this.callDuration = callDuration;
        this.message = message;
    }

    public NormalCall(long id,Long time,int slot, Float callCost,Float bal,int callDuration,String lastNumber,String message) {
        super();
        this.id = id;
        this.date = time;
        this.slot = slot;
        this.callCost = callCost;
        this.bal = bal;
        this.callDuration = callDuration;
        this.lastNumber = lastNumber;
        this.message = message;
    }
    public void put(long id ,Long time,int slot, Float callCost,Float bal,int callDuration,String lastNumber,String message) {
        this.id = id;
        this.date = time;
        this.slot = slot;
        this.callCost = callCost;
        this.bal = bal;
        this.callDuration = callDuration;
        this.lastNumber = lastNumber;
        this.message = message;
    }
    //getters & setters
 
    @Override
    public String toString() {
        return "Entry [id = "+id+" , date=" + date+ ", callcost=" + callCost + ", bal=" + bal
                + ", callduration=" + callDuration + "lastNumber = "+lastNumber +" Message = "+message+ "]";
    }
}
