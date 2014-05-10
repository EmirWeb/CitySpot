package com.cityspot;

import java.util.ArrayList;

import com.cityspot.model.GreenParking;
import com.cityspot.model.LawnParking;
import com.cityspot.utilities.Debug;
import com.google.gson.Gson;

import android.app.Application;

public class CitySpotApplication extends Application {

	public static final Gson GSON = new Gson();

	@Override
	public void onCreate() {
		super.onCreate();
		Debug.setIsDebug(true);
	}
}
