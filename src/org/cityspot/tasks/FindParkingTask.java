package org.cityspot.tasks;

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
import org.cityspot.CitySpotActivity;
import org.cityspot.CitySpotApplication;
import org.cityspot.R;
import org.cityspot.model.GreenParking;
import org.cityspot.model.Parking;
import org.cityspot.model.ParkingResponse;
import org.cityspot.model.ParkingTaskResponse;
import org.cityspot.utilities.Debug;

import android.location.Location;
import android.os.AsyncTask;

import com.google.gson.JsonSyntaxException;

public class FindParkingTask extends AsyncTask<Void, Void, ParkingTaskResponse> {
	private final CitySpotActivity mActivity;
	private final Location mLocation;
	private final int mRadius;

	public static final String UTF8 = "UTF8";
	public static final String BASE_URL = "http://173.255.227.135/CitySpotServer/makedata.php";
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

	private ParkingTaskResponse convert(final ParkingResponse parkingResponse) {
		if (parkingResponse == null || parkingResponse.mGreenParking == null) {
			return null;
		}

		final ParkingTaskResponse parkingTaskResponse = new ParkingTaskResponse();
		parkingTaskResponse.mParking = new ArrayList<Parking>(parkingResponse.mGreenParking.size());
		for (final GreenParking greenParking : parkingResponse.mGreenParking) {
			parkingTaskResponse.mParking.add(greenParking);
		}
		
		parkingTaskResponse.mCity = parkingResponse.mCity;
		parkingTaskResponse.mUrl = parkingResponse.mUrl;
		return parkingTaskResponse;

	}

	@Override
	protected ParkingTaskResponse doInBackground(final Void... params) {
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
		} catch (final JsonSyntaxException jsonSyntaxException){
			Debug.log(jsonSyntaxException.getMessage());
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
	protected void onPostExecute(final ParkingTaskResponse parkingTaskResponse) {
		if (mActivity.mIsDestroyed) {
			return;
		}
		if (parkingTaskResponse == null || parkingTaskResponse.mParking == null || parkingTaskResponse.mParking.isEmpty()) {
			mActivity.setErrorUI(mActivity.getResources().getString(R.string.activity_glass_error_message));
		} else {
			mActivity.updateUI(parkingTaskResponse);
		}
	}
}