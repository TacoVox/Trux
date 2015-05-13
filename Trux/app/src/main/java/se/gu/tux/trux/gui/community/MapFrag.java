package se.gu.tux.trux.gui.community;




import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;


import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.Timer;
import java.util.TimerTask;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

public class MapFrag extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng[] latLng;
    private LatLng loc;
    private MapFragment f;

    private Timer t;
    private popFriends timer;
    private boolean hasMarker = false;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        //Setting a Mapfragment so that it calls to the getMapAsync which is connected to onMapReady
        f = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        f.getMapAsync(this);
        return view;

    }

    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener =
            new GoogleMap.OnMyLocationButtonClickListener() {

        @Override
        public boolean onMyLocationButtonClick(){
            {
                mMap.setOnMyLocationChangeListener(startFollowing);
            }
            return false;
        }
    };
    //A listner which listen to the location of the user
    private GoogleMap.OnMyLocationChangeListener startFollowing = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            loc = new LatLng(location.getLatitude(), location.getLongitude());
            if(mMap != null){
                mMap.animateCamera(CameraUpdateFactory.newLatLng(loc));

            }
        }
    };

    private GoogleMap.OnCameraChangeListener stopFollowing = new GoogleMap.OnCameraChangeListener() {
        public void onCameraChange(CameraPosition position) {
            startFollowing = null;
        }
    };

    private GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {

       return false;
        }
    };

    private GoogleMap.OnInfoWindowClickListener markerMenu = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.menuContainer, new MapCommunityWindow());
            fragmentTransaction.commit();
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Makes it possible for the map to locate the device
        mMap.setMyLocationEnabled(true);
        //Gets the satelite pictures as a map
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        System.out.println("------MAP READY-----");



        //These lines will give you the last known position of the device
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location mylocation = locationManager.getLastKnownLocation(provider);
        if(mylocation != null) {
            double latitude = mylocation.getLatitude();
            double longitude = mylocation.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
        else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(0, 0)));
        }
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        mMap.setOnCameraChangeListener(stopFollowing);
        mMap.setOnMarkerClickListener(markerClickListener);
        mMap.setOnInfoWindowClickListener(markerMenu);


        //Creats a timeTask which will uppdate the posion of the friendUsers
        t = new Timer();
        timer = new popFriends();
        t.schedule(timer, 0, 10000);

    }

    /*
     * This method will populate the map with the friend pictures and put
     * them on the currect position.
     */
    class popFriends extends TimerTask{
        public  void run(){
           new AsyncTask(){
               @Override
                protected Object doInBackground(Object[] objects){
                   try{
                       Friend[] friend = DataHandler.getInstance().getFriends();
                       final Bitmap[] picture = new Bitmap[friend.length];
                       if(friend.length > 0){
                           for(int i = 0; i < friend.length; i++){
                               try{
                                   picture[i] = DataHandler.getInstance().getPicture(friend[i].getProfilePicId());
                               }
                               catch(NotLoggedInException nLIE){
                                   System.out.println("NotLoggedInException: " + nLIE);
                               }
                           }
                           final Bitmap[] newPicture = picture;
                           final Friend[] newFriend = friend;

                           getActivity().runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   if(newFriend != null)
                                   for (int i = 0; i < newFriend.length; i++) {
                                       System.out.println("FRIEND: " + i + " picture: " +
                                            newPicture[i] + " pictureid: " + newFriend[i].getProfilePicId()
                                            + " loc: " + newFriend[i].getCurrentLoc().getLoc());

                                       if (hasMarker) {
                                           mMap.clear();
                                           hasMarker = false;
                                       } else if (newPicture[i] != null && newFriend[i] != null /*&&
                                               newFriend[i].getCurrentLoc() != null &&
                                               newFriend[i].getCurrentLoc().getLoc() != null*/) {

                                           //double[] loc = newFriend[i].getCurrentLoc().getLoc();
                                           double[] loc = {46, 11};
                                           mMap.addMarker(new MarkerOptions()
                                                   .position(new LatLng(loc[0], loc[1]))
                                                   .title(newFriend[i].getFirstname())
                                                   .snippet("DRIVING")
                                                   .icon(BitmapDescriptorFactory.fromBitmap(newPicture[i])));
                                           System.out.println("---Picture is now a marker---");
                                           hasMarker = true;

                                       }
                                   }

                               }
                           });
                       }
                   }
                   catch (NotLoggedInException nLIE){
                       System.out.println("NotLoggedInException: " + nLIE.getMessage());
                   }
              return null; }
           }.execute();
        }
    }

    public void onStop(){
        super.onStop();
        if(t != null) {
            t.cancel();
        }
    }
}
