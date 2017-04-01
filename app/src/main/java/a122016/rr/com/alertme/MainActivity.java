package a122016.rr.com.alertme;

import android.Manifest;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
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
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Notification.PRIORITY_MAX;
import static android.app.Notification.VISIBILITY_PUBLIC;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final String LOG_TAG = MainActivity.class.getName();

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_MAKE_CALL = 2;


    private static final String PLACES_REQUEST_URL = "https://raw.githubusercontent.com/rachitrawat/AlertMe/master/app/src/debug/res/location_data.json";
    private static final String POLICE_STATION_REQUEST_URL = "https://raw.githubusercontent.com/rachitrawat/AlertMe/master/app/src/debug/res/police_station_data.json";

    /**
     * Constant value for the places loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int PLACES_LOADER_ID = 1;
    private static final int POLICE_STATION_LOADER_ID = 2;

    private static final int REQUEST_CHECK_SETTINGS = 1;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 3;

    public static ArrayList<Place> arrayList;
    public static ArrayList<PoliceStation> arrayListPS;

    private static PoliceStation nearestPS;


    private static int ALERT_ON = 0;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    /**
     * Represents a geographical location.
     */
    protected static Location mCurrentLocation;
    /**
     * The formatted location address.
     */
    protected static String mAddressOutput = "Fetching...";

    private static String destinationTextString = "";

    private TextView progressBarText;
    private TextView nameTextView;
    private EditText destinationText;
    private TextView phoneTextView;
    private ImageView doneImageView;
    private ImageView clearImageView;
    private ProgressBar progressBar;
    private ProgressBar progressBar2;
    private String LOCATION_KEY = "Location_Key";
    private TextView helpText;
    private TextView areaText;
    private TextView speedText;
    private ImageView helpImage;
    private ImageView phoneImage;
    private ImageView smsImage;
    private TextView nearestPSTextView;
    private Timer timer;
    private Uri notification;
    private Ringtone r;
    private Vibrator v;
    private NotificationCompat.Builder mBuilder;
    private FloatingActionButton fab;
    private FloatingActionButton fab_map;

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
    private String phonePref;
    private boolean engine_running = false;
    private NotificationManager mNotificationManager;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private TextToSpeech t1;
    private static String accidentProneAreaJSON;
    private Geocoder geocoder;
    private static LatLng destinationlatLng;

    private LoaderManager.LoaderCallbacks<ArrayList<Place>> placeLoaderListener
            = new LoaderManager.LoaderCallbacks<ArrayList<Place>>() {
        @Override
        public Loader<ArrayList<Place>> onCreateLoader(int id, Bundle args) {
            return new PlacesLoader(getApplicationContext(), PLACES_REQUEST_URL);
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<Place>> loader, ArrayList<Place> data) {
            if (data != null) {
                arrayList = data;
                fab.setVisibility(View.VISIBLE);
                if (!engine_running) {
                    helpText.setText("Press play button to start.");
                    helpText.setTextColor(Color.parseColor("#3949AB"));
                }
                helpText.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                progressBarText.setVisibility(View.GONE);

                //allow opening navigation drawer
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                toggle.syncState();
            } else {
                progressBar.setVisibility(View.GONE);
                progressBarText.setVisibility(View.GONE);
                helpImage.setImageResource(R.drawable.error_icon);
                helpImage.setVisibility(View.VISIBLE);
                helpText.setText("Problem fetching data from server!");
                helpText.setVisibility(View.VISIBLE);
                helpText.setTextColor(Color.BLACK);
            }
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<Place>> loader) {
        }
    };

    private LoaderManager.LoaderCallbacks<ArrayList<PoliceStation>> policeStationLoaderListener
            = new LoaderManager.LoaderCallbacks<ArrayList<PoliceStation>>() {
        @Override
        public Loader<ArrayList<PoliceStation>> onCreateLoader(int id, Bundle args) {
            return new PoliceStationLoader(getApplicationContext(), POLICE_STATION_REQUEST_URL);
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<PoliceStation>> loader, ArrayList<PoliceStation> data) {
            if (data != null) {
                arrayListPS = data;
            }
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<PoliceStation>> loader) {

        }
    };

    public static ArrayList<Place> getArrayList() {

        return arrayList;
    }

    public static ArrayList<PoliceStation> getArrayListPS() {

        return arrayListPS;
    }

    public static Location getCurrentLocation() {

        return mCurrentLocation;
    }

    public static String getCurrentLocationString() {

        return mAddressOutput;
    }

    public static LatLng getDestinationLatLng() {

        return destinationlatLng;
    }

    public static String getDestinationString() {

        return destinationTextString;
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService() {
        Log.i(LOG_TAG, "Fetch address");

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
        Log.i(LOG_TAG, "Create");

        geocoder = new Geocoder(this, Locale.US);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        doneImageView = (ImageView) findViewById(R.id.destination_button_done);
        doneImageView.setVisibility(View.INVISIBLE);
        clearImageView = (ImageView) findViewById(R.id.destination_button_clear);
        clearImageView.setVisibility(View.INVISIBLE);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab_map = (FloatingActionButton) findViewById(R.id.fab_map);
        fab.setVisibility(View.INVISIBLE);
        fab_map.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Alert Engine Started", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startAlertEngine();
            }
        });
        fab_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                execute_it_live();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        nameTextView = (TextView) header.findViewById(R.id.name_text_view);
        phoneTextView = (TextView) header.findViewById(R.id.phone_text_view);

        updateValuesFromBundle(savedInstanceState);

        mResultReceiver = new AddressResultReceiver(new Handler());
        buildGoogleApiClient();

        destinationText = (EditText) findViewById(R.id.destinationText);
        destinationText.setVisibility(View.INVISIBLE);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar2 = (ProgressBar) findViewById(R.id.progress_bar2);
        progressBarText = (TextView) findViewById(R.id.progress_bar_text);
        progressBar.setVisibility(View.INVISIBLE);
        progressBar2.setVisibility(View.INVISIBLE);
        progressBarText.setVisibility(View.INVISIBLE);
        helpText = (TextView) findViewById(R.id.alert_text_view);
        helpText.setVisibility(View.INVISIBLE);
        areaText = (TextView) findViewById(R.id.area_text_view);
        areaText.setVisibility(View.INVISIBLE);
        speedText = (TextView) findViewById(R.id.speed_text_view);
        speedText.setVisibility(View.INVISIBLE);
        helpImage = (ImageView) findViewById(R.id.help_image);
        helpImage.setVisibility(View.INVISIBLE);
        nearestPSTextView = (TextView) findViewById(R.id.nearest_ps_text_view);
        nearestPSTextView.setVisibility(View.INVISIBLE);
        phoneImage = (ImageView) findViewById(R.id.call_nearest_ps_text_view);
        phoneImage.setVisibility(View.INVISIBLE);
        smsImage = (ImageView) findViewById(R.id.sms_nearest_ps_text_view);
        smsImage.setVisibility(View.INVISIBLE);
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        namePref = sharedPrefs.getString(
                "name_text",
                "");

        phonePref = sharedPrefs.getString(
                "phone_text",
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

        if (namePref != null && !namePref.isEmpty())
            nameTextView.setText(namePref);
        if (phonePref != null && !phonePref.isEmpty())
            phoneTextView.setText(phonePref);


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

                loaderManager.initLoader(PLACES_LOADER_ID, null, placeLoaderListener);
                loaderManager.initLoader(POLICE_STATION_LOADER_ID, null, policeStationLoaderListener);
                progressBar.setVisibility(View.VISIBLE);
                progressBarText.setVisibility(View.VISIBLE);

            } else {
                progressBar.setVisibility(View.GONE);
                progressBarText.setVisibility(View.GONE);
                helpImage.setImageResource(R.drawable.error_icon);
                helpImage.setVisibility(View.VISIBLE);
                helpText.setText("Internet Connection Required!");
                helpText.setVisibility(View.VISIBLE);
                helpText.setTextColor(Color.BLACK);
            }
        }
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i(LOG_TAG, "Destroy");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if (engine_running) {
            timer.cancel();
        }
        mNotificationManager.cancelAll();
        if (t1 != null) {
            t1.stop();
            t1.shutdown();
        }
        super.onDestroy();
    }

    private void startAlertEngine() {
        doneImageView.setVisibility(View.VISIBLE);
        clearImageView.setVisibility(View.VISIBLE);
        destinationText.setVisibility(View.VISIBLE);
        engine_running = true;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            afterLoadFinished();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, 0, 12000);
        fab.setImageResource(R.drawable.ic_media_pause);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Alert Engine Stopped!", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                stopAlertEngine();
            }
        });
    }

    private void stopAlertEngine() {
        fab_map.setVisibility(View.INVISIBLE);
        destinationText.setVisibility(View.INVISIBLE);
        doneImageView.setVisibility(View.INVISIBLE);
        clearImageView.setVisibility(View.INVISIBLE);
        engine_running = false;
        timer.cancel();
        mNotificationManager.cancel(1);
        fab.setImageResource(R.drawable.ic_media_play);
        helpText.setText("Press play button to start.");
        helpText.setTextColor(Color.parseColor("#3949AB"));
        helpImage.setVisibility(View.INVISIBLE);
        areaText.setVisibility(View.INVISIBLE);
        speedText.setVisibility(View.INVISIBLE);
        nearestPSTextView.setVisibility(View.INVISIBLE);
        phoneImage.setVisibility(View.INVISIBLE);
        smsImage.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Alert Engine Started!", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                startAlertEngine();

            }
        });
    }

    private void execute_it_live() {
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra("intVariableName", 3);
        startActivity(i);
    }

    private void execute_it() {
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra("intVariableName", 1);
        startActivity(i);
    }

    private void execute_itPS() {
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra("intVariableName", 2);
        startActivity(i);
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
                    .addLocationRequest(createLocationRequest());

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
        super.onStop();
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(8000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }


    protected void startLocationUpdates() {
        //  we already have permission
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, createLocationRequest(), this);
    }

    @Override
    public void onLocationChanged(Location location) {
//        if (mCurrentLocation.getSpeed() * 18 / 5 - location.getSpeed() * 18 / 5 > 100)
//            Toast.makeText(this, "Accident Detected! ", Toast.LENGTH_SHORT).show();
        mCurrentLocation = location;
        Log.i(LOG_TAG, "Location Updated");
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
                    // recreate activity
                    recreate();


                } else {
                    // permission denied

                    progressBar.setVisibility(View.GONE);
                    progressBarText.setVisibility(View.GONE);
                    helpImage.setImageResource(R.drawable.error_icon);
                    helpImage.setVisibility(View.VISIBLE);
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

    public void afterLoadFinished() throws IOException {

        if (mCurrentLocation != null && Geocoder.isPresent()) {
            startIntentService();
        }

        if (destinationTextString != null) {
            if (!destinationTextString.equals("")) {
                progressBar2.setVisibility(View.INVISIBLE);
                fab_map.setVisibility(View.VISIBLE);
                List<Address> listOfAddress = geocoder.getFromLocationName(destinationTextString, 1);
                double latitude = listOfAddress.get(0).getLatitude();
                double longitude = listOfAddress.get(0).getLongitude();
                destinationlatLng = new LatLng(latitude, longitude);
                Log.e(LOG_TAG, latitude + " " + longitude);
            } else {
                Log.e(LOG_TAG, "Empty");
                fab_map.setVisibility(View.INVISIBLE);
                progressBar2.setVisibility(View.INVISIBLE);
            }
        } else {
            Log.e(LOG_TAG, "Empty");
            fab_map.setVisibility(View.INVISIBLE);
            progressBar2.setVisibility(View.INVISIBLE);
        }


        int c = 0;

        if (mCurrentLocation != null) {
            //   Log.i(LOG_TAG, "Current Location: " + mCurrentLocation.getLatitude() + " " + mCurrentLocation.getLongitude());
            for (Place temp : arrayList) {

                float[] result = new float[1];
                if (temp.getLatitude() != 0) {
                    Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                            temp.getLatitude(), temp.getLongitude(), result);

                    //    Log.i(LOG_TAG, "result: " + result[0]);

                    if (result[0] <= 1000) {
                        ALERT_ON = 1;
                        accidentProneAreaJSON = temp.getPlaceOfAccident();
                        break;
                    } else {
                        if (c == arrayList.size() - 1) {
                            ALERT_ON = 0;
                        }
                    }

                }
                c++;
            }

            nearestPS = arrayListPS.get(0);
            //    Log.e(LOG_TAG, "nearesPS init: " + nearestPS.getmName());
            float[] minresult = new float[1];
            Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                    nearestPS.getmLatitude(), nearestPS.getmLongitude(), minresult);
            //      Log.e(LOG_TAG, "minresult init: " + minresult[0]);
            float[] result = new float[1];

            for (PoliceStation temp : arrayListPS) {
                if (temp.getmLatitude() != 0) {
                    Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                            temp.getmLatitude(), temp.getmLongitude(), result);
                }
                //      Log.e(LOG_TAG, "result: " + (int) result[0] / 1000 + " minresult: " + (int) minresult[0] / 1000);
                if (result[0] < minresult[0]) {
                    //           Log.e(LOG_TAG, "true");
                    minresult[0] = result[0];
                    nearestPS = temp;
                }
            }

            //       Log.e(LOG_TAG, "Nearest: " + nearestPS.getmName());

            if (ALERT_ON == 1) {
                helpText.setText("You are in an Accident Prone Area.");
                helpText.setTextColor(Color.RED);
                helpImage.setImageResource(R.drawable.alert_icon);
                areaText.setText("Location: " + accidentProneAreaJSON + ", " + mAddressOutput);
                if (notificationPref)
                    buildNotification(true);
            } else {
                helpImage.setImageResource(R.drawable.safe_icon);
                areaText.setText("Location: " + mAddressOutput);
                helpText.setText("You are in a Safe Area.");
                helpText.setTextColor(Color.parseColor("#388E3C"));
                if (notificationPref)
                    buildNotification(false);
            }

            if (mCurrentLocation.getSpeed() * 18 / 5 >= 45) {
                speedText.setTextColor(Color.RED);
            } else
                speedText.setTextColor(Color.parseColor("#388E3C"));

            nearestPSTextView.setText("Nearest Police Station: " + nearestPS.getmName() + " " + (int) minresult[0] / 1000 + " " + "KMs");
            nearestPSTextView.setVisibility(View.VISIBLE);
            phoneImage.setVisibility(View.VISIBLE);
            smsImage.setVisibility(View.VISIBLE);
            helpImage.setVisibility(View.VISIBLE);
            areaText.setVisibility(View.VISIBLE);
            helpText.setVisibility(View.VISIBLE);
            speedText.setText("Speed: " + (int) (mCurrentLocation.getSpeed() * 18 / 5) + " km/h");

            if (mCurrentLocation.getSpeed() != 0) {
                speedText.setVisibility(View.VISIBLE);
            } else {
                speedText.setVisibility(View.INVISIBLE);
            }

        } else {
            helpImage.setVisibility(View.VISIBLE);
            helpImage.setImageResource(R.drawable.error_icon);
            helpText.setText("Problem getting your location! Check location settings or wait for sometime.");
            helpText.setVisibility(View.VISIBLE);
            helpText.setTextColor(Color.BLACK);
        }

    }

    public void playAlertSound() {
        t1.speak("Alert! Accident Prone Area." + accidentProneAreaJSON, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void buildNotification(boolean is_alert) {

        if (is_alert) {
            mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_warning_white_24dp)
                            .setContentTitle("Accident Prone Area")
                            .setPriority(PRIORITY_MAX);

            if (vibratePref)
                v.vibrate(500);
            if (soundPref)
                playAlertSound();
        } else {
            mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_beenhere_white_24dp)
                            .setContentTitle("Safe Area");
        }

        // Creates an explicit intent for an Activity in your app
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, 0);
        mBuilder.setContentIntent(pIntent);
        mBuilder.setContentText(mAddressOutput);
        mBuilder.setOngoing(true);
        mBuilder.setVisibility(VISIBILITY_PUBLIC);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());

    }

    private void openList() {
        Intent i = new Intent(this, ListActivity.class);
        i.putExtra("intVariableName", 1);
        startActivity(i);
    }

    private void openListPS() {
        Intent i = new Intent(this, ListActivity.class);
        i.putExtra("intVariableName", 2);
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
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            execute_it();
        } else if (id == R.id.nav_list) {
            openList();

        } else if (id == R.id.nav_list2) {
            openListPS();

        } else if (id == R.id.nav_mapPS) {
            execute_itPS();

            //    } else if (id == R.id.nav_slideshow) {

            //  } else if (id == R.id.nav_manage) {
//
        } else if (id == R.id.nav_share) {
            String message = "Name: " + namePref + "\nNumber: " + phonePref + "\nLocation: " + mAddressOutput + "\nLatitude: " + mCurrentLocation.getLatitude() + "\nLongitude: " + mCurrentLocation.getLongitude();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
//        } else if (id == R.id.nav_send) {


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void done_destination(View view) {
        destinationTextString = "";
        // Check if no view has focus:
        // hide keyboard
        View view1 = this.getCurrentFocus();
        if (view1 != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
        }
        destinationTextString = destinationText.getText().toString();
        Toast msg = Toast.makeText(getBaseContext(), "Destination: " + destinationTextString, Toast.LENGTH_LONG);
        msg.show();
        progressBar2.setVisibility(View.VISIBLE);
    }

    public void clear_destination(View view) {
        destinationText.setText("");
    }

    public void make_call(View view) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_MAKE_CALL);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm")
                    .setMessage("Call " + nearestPS.getmName() + "?")
                    .setIcon(R.drawable.ic_info_black_24dp)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setData(Uri.parse("tel:" + nearestPS.getmNumber()));
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }

    public void make_sms(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm")
                    .setMessage("Request help via sms from " + nearestPS.getmName() + "?")
                    .setIcon(R.drawable.ic_info_black_24dp)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            SmsManager smsManager = SmsManager.getDefault();
                            String message = "***Help Required!***\nName: " + namePref + "\nNumber: " + phonePref + "\nLocation: " + mAddressOutput + "\nLatitude: " + mCurrentLocation.getLatitude() + "\nLongitude: " + mCurrentLocation.getLongitude();
                            smsManager.sendTextMessage(nearestPS.getmNumber(), null, message, null, null);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        }
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
}
