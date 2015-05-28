package se.gu.tux.trux.technical_services;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


/**
 * This class is a listener for location updates from the Google API.
 * It stores the last known location so that the DataPoller can ask for it regularly like it does
 * with all the AGA data. The location is returned as a Trux location by the method getLocation().
 *
 * Created by Niklas on 07/05/15.
 */
public class LocationService implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener {

    // The last known location
    private Location currentLocation;
    // Google API client
    private GoogleApiClient googleApiClient;
    // A reference to the activity, needed to initialize the API client
    private Context context;


    /**
     * Constructs a new LocationService.
     * @param context  We need to send a context to the Google API.
     */
    public LocationService(Context context) {
        System.out.println("LocationService created.");
        this.context = context;
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }


    /**
     * Called by the API when connected.
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Update the currentLocation variable
        System.out.println("LocationService connected.");
        Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (lastKnownLocation != null) {
            currentLocation = lastKnownLocation;
        }

        // Start receiving connection updates
        startLocationUpdates();
    }


    /**
     * Provided by the API, currently not used.
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) { }


    /**
     * Requests location updates from the Google API.
     */
    protected void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(6000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    /**
     * Called by the API when the location has changed. We store the new location.
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }


    /**
     * Returns a Trux Location object.
     * @return
     */
    public se.gu.tux.trux.datastructure.Location getLocation() {
        double delta = 0.01;
        se.gu.tux.trux.datastructure.Location truxLocation = null;

        // Check that the location is not 0, 0 - since it is a double comparison we compare with
        // a delta
        if (currentLocation != null && Math.abs(currentLocation.getLongitude()) > delta
                && Math.abs(currentLocation.getLatitude()) > delta) {

            // The location has a reasonable value
            truxLocation = new se.gu.tux.trux.datastructure.Location(currentLocation.getLatitude(),
                    currentLocation.getLongitude());

        } else {

            // Null or not a reasonable value - just create an empty location
            truxLocation = new se.gu.tux.trux.datastructure.Location();

        }
        return truxLocation;
    }


    /**
     * Called by the API if the connection failed.
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("Connecting LocationService failed.");
    }
}