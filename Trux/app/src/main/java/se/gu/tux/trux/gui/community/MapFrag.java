package se.gu.tux.trux.gui.community;



import android.app.FragmentManager;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;


import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import org.slf4j.Marker;

import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.User;
import tux.gu.se.trux.R;

public class MapFrag extends Fragment implements OnMapReadyCallback {

    GoogleMap mMap;
    User user;
    Friend friend;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        MapFragment f = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        f.getMapAsync(this);

        return view;

    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            System.out.println("Inside onMyLocationChange");
            if(mMap == null) {
                System.out.println("Map is null ");
            }
            if(mMap != null){
                System.out.println("Position is changed");
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 13));
                mMap.addMarker(new MarkerOptions().position(loc).title("Here You Are"));
            }
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
        System.out.println("Adding on location change listener...");


    }
/*
    private void setUpMap(){
        mMap.setMyLocationEnabled(true);

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();

        String provider = locationManager.getBestProvider(criteria, true);

        Location mylocation = locationManager.getLastKnownLocation(provider);

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        double latitude = mylocation.getLatitude();
        double longitude = mylocation.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You Are Here"));
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, getActivity(), 0).show();
            return false;
        }
    }*/
}
