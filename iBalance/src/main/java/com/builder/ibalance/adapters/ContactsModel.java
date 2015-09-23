package com.builder.ibalance.adapters;

public class ContactsModel {
	private String image_uri,contact_name,contact_number,contact_circle,contact_state,total_duration;
	public int total_secs;
	public float callCost;
	
	public ContactsModel(String contact_name,String contact_number,String contact_circle,String contact_state,String total_duration,int total_secs, float callCost,String image_uri){
		this.contact_name=contact_name;
		this.contact_number=contact_number;
		this.contact_circle=contact_circle;
		this.contact_state=contact_state;
		this.total_duration=total_duration;
		this.total_secs=total_secs;
		this.callCost=callCost;
		this.image_uri=image_uri;
	}
	
	public String getContact_name() {
		return contact_name;
	}

	public String getImage_uri() {
		return image_uri;
	}

	public String getContact_number() {
		return contact_number;
	}

	public String getContact_circle() {
		return contact_circle;
	}

	public String getContact_state() {
		return contact_state;
	}

	public String getTotal_duration() {
		return total_duration;
	}

}
