package com.cityspot.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.location.Location;
import android.os.AsyncTask;

import com.cityspot.CitySpotActivity;
import com.cityspot.CitySpotApplication;
import com.cityspot.R;
import com.cityspot.model.GreenParking;
import com.cityspot.model.Parking;
import com.cityspot.model.ParkingResponse;
import com.cityspot.utilities.Debug;

public class FindParkingTask extends AsyncTask<Void, Void, ArrayList<Parking>> {
	private final CitySpotActivity mActivity;
	private final Location mLocation;
	private final int mRadius;

	public static final String UTF8 = "UTF8";
	public static final String BASE_URL = "http://cityspot.org/makedata.php";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String RADIUS = "radius";
	public static final String PARKING_URL = BASE_URL + "?" + LATITUDE + "=%f&" + LONGITUDE + "=%f&" + RADIUS + "=%d";

	public FindParkingTask(final CitySpotActivity activity, final int radius, final Location location) {
		mActivity = activity;
		mLocation = location;
		mRadius = radius;
	}

	private String getUrl() {
		if (mLocation == null) {
			return null;
		}
		final double longitude = mLocation.getLongitude();
		final double latitude = mLocation.getLatitude();
		return String.format(PARKING_URL, latitude, longitude, mRadius);

	}

	private ArrayList<Parking> convert(final ParkingResponse parkingResponse) {
		if (parkingResponse == null) {
			return null;
		}

		final ArrayList<Parking> parkingList = new ArrayList<Parking>(parkingResponse.size());
		for (final GreenParking greenParking : parkingResponse) {
			parkingList.add(greenParking);
		}
		return parkingList;

	}

	@Override
	protected ArrayList<Parking> doInBackground(final Void... params) {
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		try {

			final HttpClient httpClient = new DefaultHttpClient();
			final String url = getUrl();
			final HttpGet httpGet = new HttpGet(url);
			final HttpResponse httpResponse = httpClient.execute(httpGet);

			inputStream = httpResponse.getEntity().getContent();
			inputStreamReader = new InputStreamReader(inputStream, UTF8);
			final ParkingResponse parkingResponse = CitySpotApplication.GSON.fromJson(inputStreamReader, ParkingResponse.class);
			return convert(parkingResponse);
		} catch (final UnsupportedEncodingException unsupportedEncodingException) {
			Debug.log(unsupportedEncodingException.getMessage());
		} catch (final ClientProtocolException clientProtocolException) {
			Debug.log(clientProtocolException.getMessage());
		} catch (final IOException ioException) {
			Debug.log(ioException.getMessage());
		} finally {
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (final IOException ioException) {
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (final IOException ioException) {
				}
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(final ArrayList<Parking> parkingList) {
		if (mActivity.mIsDestroyed) {
			return;
		}
		if (parkingList == null || parkingList.isEmpty()) {
			mActivity.setErrorUI(mActivity.getResources().getString(R.string.activity_glass_error_message));
		} else {
			mActivity.updateUI(parkingList);
		}
	}
}