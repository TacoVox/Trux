package se.gu.tux.trux.gui.community;




import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;


import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.application.FriendFetchListener;
import se.gu.tux.trux.application.SocialHandler;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

public class MapFrag extends Fragment implements OnMapReadyCallback, FriendFetchListener {

    private GoogleMap mMap;
    private LatLng[] latLng;
    private LatLng loc;
    private MapFragment f;
    String markerID;
    Friend[] friend;
    HashMap<String, Friend> friendMarker;
    Bitmap[] picture;





    private Timer t;
    private PopFriends timer;
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
            if (!isDriving()) {
                markerID = marker.getId();
                System.out.println("This is the friend length: " + friend.length + " ------------------");
                System.out.println("This is the picture length: " + picture.length + " ------------------");
                System.out.println("This is the friendMarkerID: " + markerID  + " ------------------");
                MapCommunityWindow fragment = new MapCommunityWindow();
                Bundle sendToInfoFragment = new Bundle();
                sendToInfoFragment.putSerializable("friendArray", friend);
                sendToInfoFragment.putSerializable("pictureArray", picture);
                sendToInfoFragment.putSerializable("hashmap", friendMarker);
                sendToInfoFragment.putString("markerID", markerID);
                fragment.setArguments(sendToInfoFragment);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                fragmentTransaction.replace(R.id.menuContainer, fragment);
                fragmentTransaction.commit();
            }
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

        friendMarker = new HashMap<String, Friend>();

        //Creats a timeTask which will uppdate the posion of the friendUsers
        t = new Timer();
        timer = new PopFriends();
        t.schedule(timer, 0, 10000);
    }

    @Override
    public void FriendsFetched(ArrayList<Friend> friends) {

    }

    /*
     * This method will populate the map with the friend pictures and put
     * them on the currect position.
     */

    class PopFriends extends TimerTask{
      public  void run(){
    /*        getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        friend = DataHandler.gI().getFriends();
                        picture = new Bitmap[friend.length];
                        if(friend.length > 0){
                            for(int i = 0; i < friend.length; i++){
                                try{
                                    picture[i] = DataHandler.gI().getPicture(friend[i].getProfilePicId());
                                }
                                catch (NotLoggedInException n){
                                    n.printStackTrace();
                                }
                            }
                            final Bitmap[] newPicture = picture;
                            final Friend[] newFriend = friend;
                            if(newFriend != null){
                                if (hasMarker) {
                                    mMap.clear();
                                    hasMarker = false;
                                }

                                for (int i = 0; i < newFriend.length; i++) {

                                    if (newPicture[i] != null && newFriend[i] != null &&
                                            newFriend[i].getCurrentLoc() != null &&
                                            newFriend[i].getCurrentLoc().getLoc() != null  &&
                                                   friend[i].getStatus() == Friend.Status.ONLINE) {

                                        System.out.println("FRIEND: " + i + " picture: " +
                                                newPicture[i] + " pictureid: " + newFriend[i].getProfilePicId()
                                                + " loc: " + newFriend[i].getCurrentLoc().getLoc());

                                        double[] loc = newFriend[i].getCurrentLoc().getLoc();


                                        newPicture[i] = Bitmap.createScaledBitmap(newPicture[i],40,40,false);
                                        Canvas canvas = new Canvas(newPicture[i]);
                                        Drawable shape = getResources().getDrawable(R.drawable.marker_layout);
                                        shape.setBounds(0, 0, newPicture[i].getWidth(), newPicture[i].getHeight());
                                        shape.draw(canvas);


                                        Marker m = mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(loc[0], loc[1]))
                                                .title(newFriend[i].getFirstname())
                                                .snippet("DRIVING")
                                                .icon(BitmapDescriptorFactory.fromBitmap(newPicture[i])));
                                        String mID = m.getId();
                                        friendMarker.put(mID, newFriend[i]);
                                        System.out.println("---Picture is now a marker---");
                                        hasMarker = true;

                                    }
                                }
                            }
                        }
                    }
                    catch (NotLoggedInException n){
                        n.printStackTrace();
                    }
                }
            });
            */
           new AsyncTask(){
               @Override
                protected Object doInBackground(Object[] objects){
                   try{
                       friend = DataHandler.getInstance().getFriends();
                       picture = new Bitmap[friend.length];
                       if(friend.length > 0){
                           for(int i = 0; i < friend.length; i++){
                               try{
                                   picture[i] = DataHandler.getInstance().getPicture(friend[i].getProfilePicId());
                               }
                               catch(NotLoggedInException nLIE){
                                   System.out.println("NotLoggedInException: " + nLIE);
                               }
                           }

                       }
                   }
                   catch (NotLoggedInException nLIE){
                       System.out.println("NotLoggedInException: " + nLIE.getMessage());
                   }
              return null; }
           }.execute();
          final Bitmap[] newPicture = picture;
          final Friend[] newFriend = friend;

          getActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  if (newFriend != null) {

                      if (hasMarker) {
                          mMap.clear();
                          hasMarker = false;
                      }

                      for (int i = 0; i < newFriend.length; i++) {

                          if (newPicture[i] != null && newFriend[i] != null &&
                                  newFriend[i].getCurrentLoc() != null &&
                                  newFriend[i].getCurrentLoc().getLoc() != null/* &&
                                  friend[i].getStatus() == Friend.Status.ONLINE*/) {

                              System.out.println("FRIEND: " + i + " picture: " +
                                      newPicture[i] + " pictureid: " + newFriend[i].getProfilePicId()
                                      + " loc: " + newFriend[i].getCurrentLoc().getLoc());

                              double[] loc = newFriend[i].getCurrentLoc().getLoc();


                              newPicture[i] = Bitmap.createScaledBitmap(newPicture[i], 40, 40, false);
                              Canvas canvas = new Canvas(newPicture[i]);
                              Drawable shape = getResources().getDrawable(R.drawable.marker_layout);
                              shape.setBounds(0, 0, newPicture[i].getWidth(), newPicture[i].getHeight());
                              shape.draw(canvas);


                              Marker m = mMap.addMarker(new MarkerOptions()
                                      .position(new LatLng(loc[0], loc[1]))
                                      .title(newFriend[i].getFirstname())
                                      .snippet("DRIVING")
                                      .icon(BitmapDescriptorFactory.fromBitmap(newPicture[i])));
                              String mID = m.getId();
                              friendMarker.put(mID, newFriend[i]);
                              System.out.println("---Picture is now a marker---");
                              hasMarker = true;

                          }
                      }
                  }
              }
          });
        }
    }

    public void onStop(){
        super.onStop();
        if(t != null) {
            t.cancel();
        }
    }
    private boolean isDriving(){
        try{
            Speed speed = (Speed) DataHandler.getInstance().getData(new Speed(0));
            if(speed.getValue() != null && (double) speed.getValue() > 15){
                return true;
            }
        }
        catch (NotLoggedInException nLIE){
            nLIE.printStackTrace();
        }
        return false;
    }
}
