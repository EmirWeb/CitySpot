package org.cityspot;

import org.cityspot.utilities.Debug;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MenuActivity extends Activity {
	private static final String MENU_LONG = "menuLong";
	private static final String MENU_LAT = "menuLat";
	private static final String ADDRESS = "address";

	private float lon;
	private float lat;
	private String address;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent != null) {
			lon = intent.getFloatExtra(MENU_LONG, 0);
			lat = intent.getFloatExtra(MENU_LAT, 0);
			address = intent.getStringExtra(ADDRESS);
		}
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		openOptionsMenu();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_items, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.stop:
			stopService(new Intent(this, CitySpotService.class));
			return true;
		case R.id.go:
			Intent intent = new Intent(Intent.ACTION_VIEW);
			
			final String uri = "google.navigation:q=" + lat + "," + lon +"(" + address.replaceAll("\\s", "+") + ")" + "&mode=w&title=" + address.replaceAll("\\s", "+");
			
			Debug.log("uri: " + uri);
			intent.setData(Uri.parse(uri));
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public static Intent SetUpMenu(final Service service, final float lat, final float lon, String address) {
		final Intent intent = new Intent(service, MenuActivity.class);
		intent.putExtra(ADDRESS, address);
		intent.putExtra(MENU_LONG, lon);
		intent.putExtra(MENU_LAT, lat);
		return intent;
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// Nothing else to do, closing the Activity.
		finish();
	}
}
