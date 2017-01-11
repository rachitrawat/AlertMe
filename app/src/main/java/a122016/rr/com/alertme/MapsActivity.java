package a122016.rr.com.alertme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLngBounds sonipat = new LatLngBounds(
            new LatLng(28.95, 76.90), new LatLng(29.0, 77.20));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent mIntent = getIntent();
        int intValue = mIntent.getIntExtra("intVariableName", 0);

        if (intValue == 1) {
            ArrayList<Place> arrayList = MainActivity.getArrayList();

            for (Place place : arrayList) {
                if (place.getLatitude() != 0) {
                    LatLng latLong = new LatLng(place.getLatitude(), place.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLong).title(place.getPlaceOfAccident()));
                }
            }
        } else {
            ArrayList<PoliceStation> arrayList = MainActivity.getArrayListPS();

            for (PoliceStation place : arrayList) {
                if (place.getmLatitude() != 0) {
                    LatLng latLong = new LatLng(place.getmLatitude(), place.getmLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLong).title(place.getmName()));
                }
            }
        }

        // Set the camera to the greatest possible zoom level that includes the
        // bounds
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(sonipat, 0));

    }
}
