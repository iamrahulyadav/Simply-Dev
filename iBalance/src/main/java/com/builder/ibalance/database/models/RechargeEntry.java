package com.builder.ibalance.database.models;

public class RechargeEntry extends Base {
	    public Long date;
	    public Float RechargeAmount;
	    public Float Balance;
	 
	    public RechargeEntry(){}
	 
	    public RechargeEntry(Long time, Float RechargeAmount,Float Balance) {
	        super();
	        this.date = time;
	        this.RechargeAmount = RechargeAmount;
	        this.Balance = Balance;
	    }
	 
	    //getters & setters
	 
	    @Override
	    public String toString() {
	        return "RechargeEntry [date=" + date+ ", RechargeAmount=" + RechargeAmount + ", Balance=" + Balance
	                +  "]";
	    }
}
