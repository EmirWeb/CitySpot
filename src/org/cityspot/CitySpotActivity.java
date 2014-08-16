package org.cityspot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardScrollView;

import org.cityspot.adapters.ParkingAdapter;
import org.cityspot.model.GreenParking;
import org.cityspot.model.LawnParking;
import org.cityspot.model.Parking;
import org.cityspot.model.ParkingTaskResponse;
import org.cityspot.tasks.FindParkingTask;
import org.cityspot.utilities.Debug;
import org.cityspot.utilities.LocationHelper;
import org.cityspot.utilities.SliderView;

import java.util.ArrayList;

public class CitySpotActivity extends Activity implements OnItemClickListener {
    private static final int RADIUS = 1000;
    private static final String FAKE_LOCATION = "Mars Location";
    private static final float MARS_LATITUDE = 43.659968f;
    private static final float MARS_LONGITUDE = -79.388934f;
    private static final float SAN_FRAN_LATITUDE = 37.7833f;
    private static final float SAN_FRAN_LONGITUDE = -122.4167f;
    private static final float OTTAWA_LATITUDE = 45.4214f;
    private static final float OTTAWA_LONGITUDE = -75.6919f;

    private static final long GPS_TIMEOUT = 1000 * 10; // 10 seconds

    public boolean mIsDestroyed;
    public boolean mFindingParking;

    private boolean mHaveWakeLock;
    private boolean mIsError;
    private ParkingTaskResponse mParkingTaskResponse;
    private Location mLocation;
    private Handler mHandler;
    private TextView mProgressTextView;
    private View mProgressContainer;
    private SliderView mProgressBar;
    private View mResultsContainer;
    private CardScrollView mCardScrollView;
    private LocationHelper mLocationHelper;
    private WakeLock mWakeLock;
    private View mErrorContainer;
    private TextView mErrorMessage;
    private final Runnable mTimeout = new Runnable() {

        @Override
        public void run() {
            if (mIsDestroyed) {
                return;
            }
            releaseLock();
            mLocationHelper.stopLocationSearch();
            if (mLocation == null) {
                // setLocation(getFakeLocation());
                setErrorUI(getResources().getString(R.string.activity_glass_error_nolocation));
            }
        }
    };

    private synchronized void releaseLock() {
        if (!mHaveWakeLock) {
            return;
        }
        mHaveWakeLock = false;
        mWakeLock.release();
    }

    private synchronized void acquireLock() {
        if (mHaveWakeLock) {
            return;
        }
        mWakeLock.acquire();
        mHaveWakeLock = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mHandler = new Handler();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_city_spot);
        mProgressTextView = (TextView) findViewById(R.id.activity_glass_progress_text);
        mProgressContainer = findViewById(R.id.activity_glass_progress_container);
        mProgressBar = (SliderView)findViewById(R.id.activity_glass_progress_bar);
        mResultsContainer = findViewById(R.id.activity_glass_results_container);
        mErrorContainer = findViewById(R.id.activity_glass_error_container);
        mErrorMessage = (TextView) findViewById(R.id.activity_glass_error_message);

        mCardScrollView = (CardScrollView) findViewById(R.id.activity_glass_results);
        mCardScrollView.setOnItemClickListener(this);

        mLocationHelper = new LocationHelper(this);

        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GoogleGlassParking");
    }

    @Override
    protected void onStart() {
        super.onStart();
        start();
    }

    private void start() {
        Debug.log("mHaveWakeLock: " + mHaveWakeLock);
        mIsError = false;
        acquireLock();
        mLocationHelper.startLocationSearch();
        updateUI(null);
        mHandler.postDelayed(mTimeout, GPS_TIMEOUT);
    }

    private void stop() {
        Debug.log("mHaveWakeLock: " + mHaveWakeLock);
        releaseLock();
        mLocationHelper.stopLocationSearch();
        updateUI(null);
        mHandler.removeCallbacks(mTimeout);
        mFindingParking = false;
        mParkingTaskResponse = null;
        mLocation = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsDestroyed = true;
    }

    public void setErrorUI(final String error) {
        mErrorMessage.setText(error);
        mIsError = true;
        stop();
    }

    public void updateUI(final ParkingTaskResponse parkingTaskResponse) {

        if (mIsError) {
            mErrorContainer.setVisibility(View.VISIBLE);
            mProgressContainer.setVisibility(View.GONE);
            mProgressBar.stopIndeterminate();
            mResultsContainer.setVisibility(View.GONE);
            return;
        }
        mErrorContainer.setVisibility(View.GONE);
        if (mParkingTaskResponse == null) {
            mParkingTaskResponse = parkingTaskResponse;
        }
        final boolean foundParking = mParkingTaskResponse != null && mParkingTaskResponse.mParking != null;
        if (foundParking) {
            final ArrayList<Parking> parkingList = mParkingTaskResponse.mParking;
            mFindingParking = false;
            mProgressContainer.setVisibility(View.GONE);
            mProgressBar.stopIndeterminate();
            mResultsContainer.setVisibility(View.VISIBLE);
            final ParkingAdapter parkingAdapter = new ParkingAdapter(getApplicationContext());
            parkingAdapter.setParkingList(parkingList);
            mCardScrollView.setAdapter(parkingAdapter);
            mCardScrollView.activate();
            if (parkingList != null) {
                final AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audio.playSoundEffect(Sounds.SUCCESS);
            } else {
                Debug.log("No Result");
                setErrorUI(getResources().getString(R.string.activity_glass_error_message));
            }
            return;
        }

        mProgressContainer.setVisibility(View.VISIBLE);
        mProgressBar.startIndeterminate();
        mResultsContainer.setVisibility(View.GONE);
        final boolean hasLocation = mLocation != null;
        if (!hasLocation) {
            mProgressTextView.setText(R.string.activity_glass_progress_finding_location);
        } else {
            mProgressTextView.setText(R.string.activity_glass_progress_finding_parking);
        }
    }

    public void setLocation(final Location location) {
        mLocationHelper.stopLocationSearch();
        // mLocation = getFakeLocation();
        mLocation = location;
        findParking();
    }

    private Location getFakeLocation() {
        final Location location = new Location(FAKE_LOCATION);
        location.setLatitude(SAN_FRAN_LATITUDE);
        location.setLongitude(SAN_FRAN_LONGITUDE);
        return location;
    }

    public void findParking() {
        updateUI(null);
        final boolean foundParking = mParkingTaskResponse != null;
        if (foundParking || mFindingParking || mLocation == null) {
            return;
        }
        mFindingParking = true;
        final FindParkingTask findParkingTask = new FindParkingTask(this, RADIUS, mLocation);
        findParkingTask.execute((Void) null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.TAP);
        final Parking parking = (Parking) parent.getItemAtPosition(position);
        if (parking instanceof GreenParking) {
            final GreenParking greenParking = (GreenParking) parking;
            CitySpotService.launchCard(this, greenParking);
            navigateTo(greenParking.getLocation(), greenParking.mAddress);
            finish();
        } else {
            final LawnParking lawnParking = (LawnParking) parking;
            CitySpotService.launchCard(this, lawnParking);
            navigateTo(lawnParking.getLocation(), lawnParking.mAddress);
            finish();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
        final boolean consumed = super.onKeyUp(keyCode, keyEvent);
        if (!consumed && keyCode == KeyEvent.KEYCODE_DPAD_CENTER && mIsError) {
            final AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audio.playSoundEffect(Sounds.TAP);
            retry();
            return true;
        }
        return consumed;
    }

    private synchronized void retry() {
        mIsError = false;
        start();
    }

    public void navigateTo(final Location location, final String address) {
        if (location == null) {
            return;
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        final String uri = "google.navigation:q=" + latitude + "," + longitude + "(" + address.replaceAll("\\s", "+") + ")" + "&mode=d&title=" + address.replaceAll("\\s", "+");
        Debug.log("uri: " + uri);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }
}
