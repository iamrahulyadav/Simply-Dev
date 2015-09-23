package com.builder.ibalance.database.models;

public class NormalSMS extends Base {
	    public Long date;
	    public Float cost;
	    public Float bal;
	    public String lastNumber;
		public String message;
	 
	   public  NormalSMS(){}
	 
	    public NormalSMS(Long time, Float callCost,Float bal, String lastNumber, String message) {
	        super();
	        this.date = time;
	        this.cost = callCost;
	        this.lastNumber = lastNumber;
	        this.bal = bal;
	        this.message = message;
	    }
	 
	    //getters & setters
	 
	    @Override
	    public String toString() {
	        return "Entry [date=" + date +" ,Cost ="+cost+ ", bal=" + bal
	               + " , lastNumber = "+lastNumber +" Message = "+message+ "]";
	    }
}
