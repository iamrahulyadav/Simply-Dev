package com.builder.ibalance.database.models;



public class NormalCall extends Base {
    public int id;
    public Long date;
    public Float callCost;
    public Float bal;
    public int callDuration;
    public String lastNumber;
	public String message;
 
    public NormalCall(){}
 
    public NormalCall(Long time, Float callCost,Float bal,int callDuration,String lastNumber,String message) {
        super();
        this.date = time;
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
