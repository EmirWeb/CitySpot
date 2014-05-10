package org.cityspot.model;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;


public class ParkingResponse  {
	@SerializedName("results")
	public ArrayList<GreenParking> mGreenParking;
	
	@SerializedName("city")
	public String mCity;
	
	@SerializedName("url")
	public String mUrl;
	
}
