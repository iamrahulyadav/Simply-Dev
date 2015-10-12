package com.builder.ibalance.database.models;



public class NormalCall extends DatabaseEntryBase
{

    public Float callCost;
    public Float bal;
    public int callDuration;
    public int slot ;
    public String lastNumber;
	public String message;

    public NormalCall(){}
 
    public NormalCall(Long time, Float callCost,Float bal,int callDuration,String message) {
        super();
        this.date = time;
        this.callCost = callCost;
        this.bal = bal;
        this.callDuration = callDuration;
        this.message = message;
    }

    public NormalCall(Long time,int slot, Float callCost,Float bal,int callDuration,String lastNumber,String message) {
        super();
        this.date = time;
        this.slot = slot;
        this.callCost = callCost;
        this.bal = bal;
        this.callDuration = callDuration;
        this.lastNumber = lastNumber;
        this.message = message;
    }
    public void put(Long time,int slot, Float callCost,Float bal,int callDuration,String lastNumber,String message) {
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
        return "Entry [date=" + date+ ", callcost=" + callCost + ", bal=" + bal
                + ", callduration=" + callDuration + "lastNumber = "+lastNumber +" Message = "+message+ "]";
    }
}
