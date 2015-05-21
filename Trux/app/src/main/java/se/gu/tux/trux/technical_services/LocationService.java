package se.gu.tux.trux.technical_services;

import android.app.Activity;
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
 * Created by Niklas on 07/05/15.
 */
public class LocationService implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener {

    private Location currentLocation;
    private Long locationTimeStamp;
    private double latitude;
    private double longitude;
    private double[] latLng = new double[2];

    private GoogleApiClient googleApiClient;

    private Activity activity;

    public LocationService(Activity activity) {
        System.out.println("LocationService created.");
        this.activity = activity;
        buildGoogleApiClient();
        googleApiClient.connect();
    }



    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        System.out.println("LocationService connected.");
        Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (lastKnownLocation != null) {
            currentLocation = lastKnownLocation;
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(6000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        locationTimeStamp = System.currentTimeMillis();
        //System.out.println("New location: " + location.getLongitude());
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public double[] getLatLng(){
        latLng[0] = latitude;
        latLng[1] = longitude;
        return latLng;
    }

    /**
     * Returns a Trux Location object.
     * @return
     */
    public se.gu.tux.trux.datastructure.Location getLocation() {
        double delta = 0.01;
        se.gu.tux.trux.datastructure.Location truxLocation = null;

        if (currentLocation != null && Math.abs(currentLocation.getLongitude()) > delta
                && Math.abs(currentLocation.getLatitude()) > delta) {
            truxLocation = new se.gu.tux.trux.datastructure.Location(currentLocation.getLatitude(),
                    currentLocation.getLongitude());
            //System.out.println("Returning a Location with values.");
        } else {
            truxLocation = new se.gu.tux.trux.datastructure.Location();
            //System.out.println("Returning a Location with no values.");
        }
        return truxLocation;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("Connecting LocationService failed.");
    }
}