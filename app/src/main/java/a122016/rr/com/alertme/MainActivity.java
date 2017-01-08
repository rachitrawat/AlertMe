package a122016.rr.com.alertme;

import android.app.LoaderManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Notification.PRIORITY_MAX;
import static android.app.Notification.VISIBILITY_PUBLIC;

public class MainActivity extends AppCompatActivity implements OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LoaderManager.LoaderCallbacks<ArrayList<Place>>, LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;
    private static final String PLACES_REQUEST_URL = "https://raw.githubusercontent.com/rachitrawat/AlertMe/master/app/src/debug/res/data.json";
    private static final String LOG_TAG = MainActivity.class.getName();
    /**
     * Constant value for the places loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int PLACES_LOADER_ID = 1;
    private static final int REQUEST_CHECK_SETTINGS = 1;

    public static ArrayList<Place> arrayList;
    private static LocationRequest mLocationRequest;
    private static int ALERT_ON = 0;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;
    /**
     * The formatted location address.
     */
    protected String mAddressOutput = "Fetching...";

    private Button listButton;
    private Button mapButton;
    private TextView progressBarText;
    private ProgressBar progessBar;
    private String LOCATION_KEY;
    private TextView helpText;
    private TextView areaText;
    private TextView speedText;
    private ImageView helpImage;
    private Timer timer;
    private Uri notification;
    private Ringtone r;
    private Vibrator v;
    private boolean doubleBackToExitPressedOnce = false;
    private NotificationCompat.Builder mBuilder;


    /**
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */
    private AddressResultReceiver mResultReceiver;
    private String namePref;
    private String emerNo1Pref;
    private String emerNo2Pref;
    private String emerNo3Pref;
    private boolean notificationPref;
    private boolean soundPref;
    private boolean vibratePref;

    public static ArrayList<Place> getArrayList() {

        return arrayList;
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService() {
        Log.e(LOG_TAG, "intent started");
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mCurrentLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }

    /**
     * Builds a GoogleApiClient. Uses {@code #addApi} to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateValuesFromBundle(savedInstanceState);

        mResultReceiver = new AddressResultReceiver(new Handler());
        buildGoogleApiClient();

        listButton = (Button) findViewById(R.id.list_button);
        listButton.setVisibility(View.INVISIBLE);
        mapButton = (Button) findViewById(R.id.map_button);
        mapButton.setVisibility(View.INVISIBLE);
        progessBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBarText = (TextView) findViewById(R.id.progress_bar_text);
        progessBar.setVisibility(View.INVISIBLE);
        progressBarText.setVisibility(View.INVISIBLE);
        helpText = (TextView) findViewById(R.id.alert_text_view);
        helpText.setVisibility(View.INVISIBLE);
        areaText = (TextView) findViewById(R.id.area_text_view);
        areaText.setVisibility(View.INVISIBLE);
        speedText = (TextView) findViewById(R.id.speed_text_view);
        speedText.setVisibility(View.INVISIBLE);
        helpImage = (ImageView) findViewById(R.id.help_image);
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        //create location request object
        mLocationRequest = createLocationRequest();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        namePref = sharedPrefs.getString(
                "name_text",
                "");

        emerNo1Pref = sharedPrefs.getString(
                "emergency_number1",
                "");
        emerNo2Pref = sharedPrefs.getString(
                "emergency_number2",
                "");

        emerNo3Pref = sharedPrefs.getString(
                "emergency_number3",
                "");

        notificationPref = sharedPrefs.getBoolean("notifications_new_message", true);

        soundPref = sharedPrefs.getBoolean(
                "notifications_new_message_sound",
                true);

        vibratePref = sharedPrefs.getBoolean(
                "notifications_new_message_vibrate",
                true);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Get a reference to the ConnectivityManager to check state of network connectivity
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);

            // Get details on the currently active default data network
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            // If there is a network connection, fetch data
            if (networkInfo != null && networkInfo.isConnected()) {

                // Get a reference to the LoaderManager, in order to interact with loaders.
                final LoaderManager loaderManager = getLoaderManager();

                // Initialize the loader. Pass in the int ID constant defined above and pass in null for
                // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
                // because this activity implements the LoaderCallbacks interface).

                loaderManager.initLoader(PLACES_LOADER_ID, null, this);

                progessBar.setVisibility(View.VISIBLE);
                progressBarText.setVisibility(View.VISIBLE);

            } else {
                progessBar.setVisibility(View.GONE);
                progressBarText.setVisibility(View.GONE);
                helpImage.setImageResource(R.drawable.error_icon);
                helpText.setText("Internet Connection Required!");
                helpText.setVisibility(View.VISIBLE);
                helpText.setTextColor(Color.BLACK);
            }
        }
    }

    public void execute_it(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(LOG_TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // create a LocationSettingsRequest.Builder
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);

            //check whether the current location settings are satisfied
            final PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                            builder.build());

            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {

                    final Status status = locationSettingsResult.getStatus();
                    // final LocationSettingsStates x = locationSettingsResult.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can
                            // initialize location requests here.
                            // ...
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied, but this can be fixed
                            // by showing the user a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        MainActivity.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way
                            // to fix the settings so we won't show the dialog.
                            //    ...
                            break;
                    }
                }
            });

            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            //start getting location updates
            startLocationUpdates();

            if (mCurrentLocation != null && Geocoder.isPresent()) {
                startIntentService();
            }

        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(LOG_TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            //  mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }


    protected void startLocationUpdates() {
        //  we already have permission
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mCurrentLocation.getSpeed() * 18 / 5 - location.getSpeed() * 18 / 5 > 100)
            Toast.makeText(this, "Accident Detected! ", Toast.LENGTH_SHORT).show();
        mCurrentLocation = location;

        Log.e(LOG_TAG, "change location");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted

                    // restart the activity
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);


                } else {
                    // permission denied

                    progessBar.setVisibility(View.GONE);
                    progressBarText.setVisibility(View.GONE);
                    helpImage.setImageResource(R.drawable.error_icon);
                    helpText.setText("Location permission required!");
                    helpText.setVisibility(View.VISIBLE);
                    helpText.setTextColor(Color.BLACK);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public Loader<ArrayList<Place>> onCreateLoader(int id, Bundle args) {
        return new PlacesLoader(this, PLACES_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Place>> loader, ArrayList<Place> data) {

        final ArrayList<Place> DATA = data;

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        afterLoadFinished(DATA);
                    }
                });
            }
        }, 0, 15000);
    }

    public void afterLoadFinished(ArrayList<Place> data) {

        if (mCurrentLocation != null && Geocoder.isPresent()) {
            startIntentService();
        }

        int c = 0;

        if (mCurrentLocation != null) {
            //   Log.e(LOG_TAG, "Current Location: " + mCurrentLocation.getLatitude() + " " + mCurrentLocation.getLongitude());
            for (Place temp : data) {

                float[] result = new float[1];
                if (temp.getLatitude() != 0) {
                    Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                            temp.getLatitude(), temp.getLongitude(), result);

                    //Log.e(LOG_TAG, "result: " + result[0]);

                    if (result[0] <= 1000) {
                        helpText.setText("You are in an Accident Prone Area.");
                        helpText.setTextColor(Color.RED);
                        helpImage.setImageResource(R.drawable.alert_icon);
                        areaText.setText("Location: " + mAddressOutput);
                        speedText.setText("Speed: " + mCurrentLocation.getSpeed());
                        if (notificationPref)
                            buildNotification(true);
                        ALERT_ON = 1;
                        break;
                    } else {
                        helpImage.setImageResource(R.drawable.safe_icon);
                        helpText.setText("You are in a Safe Area.");
                        helpText.setTextColor(Color.parseColor("#388E3C"));
                        areaText.setText("Location: " + mAddressOutput);
                        if (notificationPref)
                            buildNotification(false);
                        if (c == data.size() - 1) {
                            ALERT_ON = 0;
                        }
                    }

                }
                c++;
            }

            if (mCurrentLocation.getSpeed() * 18 / 5 >= 45) {
                speedText.setTextColor(Color.RED);
            } else
                speedText.setTextColor(Color.parseColor("#388E3C"));


            arrayList = data;
            // listButton.setVisibility(View.VISIBLE);
            mapButton.setVisibility(View.VISIBLE);
            areaText.setVisibility(View.VISIBLE);
            helpText.setVisibility(View.VISIBLE);
            speedText.setText("Speed: " + (int) (mCurrentLocation.getSpeed() * 18 / 5) + " km/h");

            if (mCurrentLocation.getSpeed() != 0) {
                speedText.setVisibility(View.VISIBLE);
            } else {
                speedText.setVisibility(View.INVISIBLE);
            }

        } else {
            helpImage.setImageResource(R.drawable.error_icon);
            helpText.setText("Problem getting your location!");
            helpText.setVisibility(View.VISIBLE);
            helpText.setTextColor(Color.BLACK);
        }
        progessBar.setVisibility(View.GONE);
        progressBarText.setVisibility(View.GONE);
    }

    public void playAlertSound() {
        //Play alert sound
        try {
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void buildNotification(boolean is_alert) {

        if (is_alert) {
            mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_warning_white_24dp)
                            .setContentTitle("Accident Prone Area")
                            .setContentText(mAddressOutput)
                            .setVisibility(VISIBILITY_PUBLIC)
                            .setPriority(PRIORITY_MAX);

            if (vibratePref)
                v.vibrate(500);
            if (soundPref)
                playAlertSound();
        } else {
            mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_beenhere_white_24dp)
                            .setContentTitle("Safe Area")
                            .setContentText(mAddressOutput)
                            .setVisibility(VISIBILITY_PUBLIC);
        }

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());

    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Place>> loader) {

    }

    public void openList(View view) {

        Intent i = new Intent(this, a122016.rr.com.alertme.ListActivity.class);
        startActivity(i);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {

            // Update the value of mCurrentLocation from the Bundle
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // Store the address stringm
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
