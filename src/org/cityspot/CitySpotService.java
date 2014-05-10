package org.cityspot;

import org.cityspot.model.GreenParking;
import org.cityspot.model.LawnParking;
import org.cityspot.model.Parking;
import org.cityspot.utilities.Debug;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;

public class CitySpotService extends Service {
	private static final String TYPE = "type";
	private static final String GREEN_PARKING = "greenParking";
	private static final String LAWN_PARKING = "lawnParking";
	private static final String LIVE_CARD_ID = "parking";
	private static final String MENU_LONG = "menuLong";
	private static final String MENU_LAT = "menuLat";
	private LiveCard mLiveCard;
	
	@Override
	public void onCreate() {
		Debug.setIsDebug(true);
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			publishCard(intent);
		}

		return START_STICKY;
	}

	private void publishCard(final Intent intent) {
		final String type = intent.getStringExtra(TYPE);
		if (LAWN_PARKING.equals(type)) {
			final float distance = intent.getFloatExtra(LawnParking.Keys.DISTANCE, 0);
			final String address = intent.getStringExtra(LawnParking.Keys.ADDRESS);
			final float latitude = intent.getFloatExtra(LawnParking.Keys.LATITUDE, 0);
			final float longitude = intent.getFloatExtra(LawnParking.Keys.LONGITUDE, 0);
			final LawnParking lawnParking = new LawnParking();
			lawnParking.mDistance = distance;
			lawnParking.mAddress = address;
			lawnParking.mLatitude = latitude;
			lawnParking.mLongitude = longitude;
			publishCard(getApplicationContext(), lawnParking);
		} else if (GREEN_PARKING.equals(type)) {
			final float distance = intent.getFloatExtra(GreenParking.Keys.DISTANCE, 0);
			final String address = intent.getStringExtra(GreenParking.Keys.ADDRESS);
			final String rateHalfHour = intent.getStringExtra(GreenParking.Keys.RATE_HALF_HOUR);
			final String latitude = intent.getStringExtra(GreenParking.Keys.LAT);
			final String longitude = intent.getStringExtra(GreenParking.Keys.LNG);
			final GreenParking greenParking = new GreenParking();
			greenParking.mDistance = distance;
			greenParking.mAddress = address;
			greenParking.mRateHalfHour = rateHalfHour;
			greenParking.mLat = latitude;
			greenParking.mLong = longitude;
			publishCard(getApplicationContext(), greenParking);
		}
	}

	private void publishCard(final Context context, final GreenParking greenParking) {
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.list_item_green_parking);
		remoteViews.setTextViewText(R.id.list_item_green_parking_address, greenParking.mAddress);
		final String distanceString = String.format(Parking.DISTANCE, greenParking.mDistance);
		final String priceString = String.format(Parking.PRICE, greenParking.mRateHalfHour);
		remoteViews.setTextViewText(R.id.list_item_green_parking_distance, distanceString);
		remoteViews.setTextViewText(R.id.list_item_green_parking_price, priceString);
		
		
		publishCard(context, remoteViews, Float.parseFloat(greenParking.mLat), Float.parseFloat(greenParking.mLong), greenParking.mAddress);
	}

	private void publishCard(final Context context, final LawnParking lawnParking) {
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.list_item_lawn_parking);
		remoteViews.setTextViewText(R.id.list_item_lawn_parking_address, lawnParking.mAddress);
		final String distanceString = String.format(Parking.DISTANCE, lawnParking.mDistance);
		remoteViews.setTextViewText(R.id.list_item_green_parking_distance, distanceString);
		
		publishCard(context, remoteViews, lawnParking.mLatitude, lawnParking.mLongitude, lawnParking.mAddress);
	}

	private void publishCard(final Context context, final RemoteViews remoteViews, float latitude, float longitude, String address) {
		if (mLiveCard != null) {
			mLiveCard.unpublish();
		}
		mLiveCard = new LiveCard(this, LIVE_CARD_ID);
		mLiveCard.setViews(remoteViews);
		Intent intent = MenuActivity.SetUpMenu(this, latitude, longitude, address);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		mLiveCard.setAction(PendingIntent.getActivity(context, 0, intent, 0));
		mLiveCard.publish(LiveCard.PublishMode.SILENT);
	}

	public static void launchCard(final Activity activity, final GreenParking greenParking) {
		final Intent intent = new Intent(activity, CitySpotService.class);
		intent.putExtra(TYPE, GREEN_PARKING);
		intent.putExtra(GreenParking.Keys.ADDRESS, greenParking.mAddress);
		intent.putExtra(GreenParking.Keys.LAT, greenParking.mLat);
		intent.putExtra(GreenParking.Keys.LNG, greenParking.mLong);
		intent.putExtra(GreenParking.Keys.RATE_HALF_HOUR, greenParking.mRateHalfHour);
		intent.putExtra(GreenParking.Keys.DISTANCE, greenParking.mDistance);
		
		activity.startService(intent);
	}

	public static void launchCard(final Activity activity, final LawnParking lawnParking) {
		final Intent intent = new Intent(activity, CitySpotService.class);
		intent.putExtra(TYPE, LAWN_PARKING);
		intent.putExtra(LawnParking.Keys.ADDRESS, lawnParking.mAddress);
		intent.putExtra(LawnParking.Keys.LATITUDE, lawnParking.mLatitude);
		intent.putExtra(LawnParking.Keys.LONGITUDE, lawnParking.mLongitude);
		intent.putExtra(LawnParking.Keys.DISTANCE, lawnParking.mDistance);
		activity.startService(intent);
	}
	
	@Override
	public void onDestroy(){
		unpublishCard(this);
	
	    super.onDestroy();
	}
	
	private void unpublishCard(Context context){
	    if (mLiveCard != null) {
	    	mLiveCard.unpublish();
	    	mLiveCard = null;
	    }
	}
}
