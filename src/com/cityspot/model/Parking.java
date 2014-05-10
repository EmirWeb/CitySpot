package com.cityspot.model;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

public abstract class Parking {

	public static final String PRICE = "$ %s / 30 mins";
	public static final String DISTANCE = "%.1f km";

	public static class Keys {
		public static final String DISTANCE = "distance";
	}

	@SerializedName(Keys.DISTANCE)
	public Float mDistance;

	public abstract Location getLocation();

}
