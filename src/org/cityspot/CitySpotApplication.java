package org.cityspot;

import java.util.ArrayList;

import org.cityspot.model.GreenParking;
import org.cityspot.model.LawnParking;
import org.cityspot.utilities.Debug;

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
