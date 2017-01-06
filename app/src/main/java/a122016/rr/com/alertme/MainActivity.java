package a122016.rr.com.alertme;


import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class MainActivity extends AppCompatActivity implements OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LoaderManager.LoaderCallbacks<ArrayList<Place>> {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;
    private static final String PLACES_REQUEST_URL = "https://raw.githubusercontent.com/rachitrawat/AlertMe/master/app/src/debug/res/data.json";
    private static final String LOG_TAG = MainActivity.class.getName();
    /**
     * Constant value for the places loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int PLACES_LOADER_ID = 1;
    public static ArrayList<Place> arrayList;
    private Button listButton;
    private Button mapButton;
    private TextView progressBarText;
    private ProgressBar progessBar;
    private Location mLastLocation;
    private TextView helpText;
    private ImageView helpImage;
    private Timer timer;
    private GoogleApiClient mGoogleApiClient;


    public static ArrayList<Place> getArrayList() {

        return arrayList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listButton = (Button) findViewById(R.id.list_button);
        listButton.setVisibility(View.INVISIBLE);
        mapButton = (Button) findViewById(R.id.map_button);
        mapButton.setVisibility(View.INVISIBLE);
        progessBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBarText = (TextView) findViewById(R.id.progress_bar_text);
        helpText = (TextView) findViewById(R.id.alert_text_view);
        helpText.setVisibility(View.INVISIBLE);
        helpImage = (ImageView) findViewById(R.id.help_image);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

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

            } else {
                progessBar.setVisibility(View.GONE);
                progressBarText.setVisibility(View.GONE);
                helpImage.setVisibility(View.VISIBLE);
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

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
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
                    helpImage.setVisibility(View.VISIBLE);
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
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        afterLoadFinished(DATA);
                    }
                });
            }
        }, 0, 10000);
    }

    public void afterLoadFinished(ArrayList<Place> data) {

        Log.e(LOG_TAG, "location access " + (mLastLocation != null));

        if (mLastLocation != null) {
            for (Place temp : data) {

                float[] result = new float[1];
                if (temp.getLatitude() != 0) {
                    Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                            temp.getLatitude(), temp.getLongitude(), result);
                    Log.i(LOG_TAG, "result: " + result[0]);

                    if (result[0] <= 5000) {

                        //   Toast.makeText(this, "Accident Prone Area: " + temp.getPlaceOfAccident(), Toast.LENGTH_SHORT).show();
                        helpText.setText("Accident Prone Area: " + temp.getPlaceOfAccident());
                        helpText.setTextColor(Color.RED);
                        helpText.setVisibility(View.VISIBLE);
                        helpImage.setImageResource(R.drawable.alert_icon);
                        //Play alert sound
                        try {
                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                            r.play();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        helpImage.setImageResource(R.drawable.safe_icon);
                        helpText.setText("You are in a safe area.");
                        helpText.setTextColor(Color.parseColor("#388E3C"));
                        helpText.setVisibility(View.VISIBLE);
                    }

                }
            }

            arrayList = data;
            // listButton.setVisibility(View.VISIBLE);
            mapButton.setVisibility(View.VISIBLE);
        } else {
            helpImage.setImageResource(R.drawable.error_icon);
            helpText.setText("Error getting your location!");
            helpText.setVisibility(View.VISIBLE);
            helpText.setTextColor(Color.BLACK);
        }
        progessBar.setVisibility(View.GONE);
        progressBarText.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Place>> loader) {

    }

    public void openList(View view) {

        Intent i = new Intent(this, a122016.rr.com.alertme.ListActivity.class);
        startActivity(i);
    }
}
