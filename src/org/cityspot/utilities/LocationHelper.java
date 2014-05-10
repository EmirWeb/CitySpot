package org.cityspot.utilities;

import java.util.List;

import org.cityspot.CitySpotActivity;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationHelper implements LocationListener {
	private final LocationManager mLocationManager;
	private final CitySpotActivity mGlassActivity;

	public LocationHelper(final CitySpotActivity glassActivity) {
		mGlassActivity = glassActivity;
		mLocationManager = (LocationManager) glassActivity.getSystemService(Context.LOCATION_SERVICE);
	}

	public void startLocationSearch() {
		final Criteria criteria = new Criteria();
		//criteria.setAccuracy(Criteria.ACCURACY_FINE);
		//criteria.setAltitudeRequired(true);
		final List<String> providers = mLocationManager.getProviders(criteria, true);

		float smallestAccuracy = Float.MAX_VALUE;
		Location smallestLocation = null;
		for (String provider : providers) {
			mLocationManager.requestLocationUpdates(provider, 0, 0, this);
			final Location location = mLocationManager.getLastKnownLocation(provider);
			if (location != null) {
				final float accuracy = location.getAccuracy();
				if (smallestAccuracy < accuracy) {
					smallestLocation = location;
				}
			}
		}
		if (smallestLocation != null) {
			mGlassActivity.setLocation(smallestLocation);
		}
	}

	public void stopLocationSearch() {
		mLocationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(final Location location) {
		mGlassActivity.setLocation(location);
	}

	@Override
	public void onStatusChanged(final String provider, final int status, final Bundle extras) {
	}

	@Override
	public void onProviderEnabled(final String provider) {
	}

	@Override
	public void onProviderDisabled(final String provider) {
	}
}
