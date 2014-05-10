package org.cityspot;

import org.cityspot.utilities.Debug;

import android.app.Application;

import com.google.gson.Gson;

public class CitySpotApplication extends Application {

	public static final Gson GSON = new Gson();

	@Override
	public void onCreate() {
		super.onCreate();
		Debug.setIsDebug(true);
	}
}
