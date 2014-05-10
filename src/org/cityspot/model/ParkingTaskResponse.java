package org.cityspot.model;

import java.util.ArrayList;

public class ParkingTaskResponse {
	public ArrayList<Parking> mParking;

	public String mCity;

	public String mUrl;

	public static class Cities {
		public static final String TORONTO = "Toronto";
		public static final String OTTAWA = "Ottawa";
		public static final String SAN_FRANCISCO = "San Francisco";
	}

}
