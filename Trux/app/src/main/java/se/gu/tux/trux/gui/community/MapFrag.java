package se.gu.tux.trux.gui.community;




import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;


import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.application.FriendFetchListener;
import se.gu.tux.trux.application.SettingsHandler;
import se.gu.tux.trux.application.SocialHandler;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.gui.main_home.HomeActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

public class MapFrag extends Fragment implements OnMapReadyCallback, FriendFetchListener,
        GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private LatLng loc;
    private MapFragment f;
    
    private HashMap<String, Friend> friendMarker;
    
    private ArrayList<Friend> friends;
    private MapFrag thisMapFrag = this;
    
    private Timer t;
    private PopFriends timer;
    private boolean hasMarker = false;
    private boolean mapLoaded = false;


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


    @Override
    public void onMapClick(LatLng point) {
        System.out.println("");
        getActivity().getSupportFragmentManager().popBackStackImmediate("MENU",
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }


    private GoogleMap.OnCameraChangeListener stopFollowing = new GoogleMap.OnCameraChangeListener() {
        public void onCameraChange(CameraPosition position) {
            startFollowing = null;
        }
    };

    private GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            if (!isDriving()) {
                String markerID = marker.getId();
                MapCommunityWindow fragment = new MapCommunityWindow();
                Friend friend = friendMarker.get(markerID);
                Bundle sendToInfoFragment = new Bundle();
                sendToInfoFragment.putSerializable("friend", friend);
                fragment.setArguments(sendToInfoFragment);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                getActivity().getSupportFragmentManager().popBackStackImmediate("MENU",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentTransaction.addToBackStack("MENU");
                fragmentTransaction.replace(R.id.menuContainer, fragment);
                System.out.println("Count on the popStack in mapFrag: " + getFragmentManager().getBackStackEntryCount());
                fragmentTransaction.commit();
            }
       return false;
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Makes it possible for the map to locate the device
        mMap.setMyLocationEnabled(true);
        //Gets the normal view of the map (not satalite)
        if(SettingsHandler.getInstance().isNormalMap()) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        else mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

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
        mMap.setOnMapClickListener(this);

        friendMarker = new HashMap<String, Friend>();

        //Creats a timeTask which will uppdate the posion of the friendUsers
        t = new Timer();
        timer = new PopFriends();
        t.schedule(timer, 0, 10000);

        mapLoaded = true;
    }
    

    @Override
    public void onFriendsFetched(final ArrayList<Friend> friends) {
        this.friends = friends;
        final long selectedFriend = ((HomeActivity) getActivity()).getSelectedFriend();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (hasMarker) {
                    mMap.clear();
                    hasMarker = false;
                }
            }});
        if (friends != null) {

            for (final Friend currentFriend : friends) {

                if (currentFriend != null && currentFriend.getProfilePic() != null &&
                        currentFriend.getCurrentLoc() != null &&
                        currentFriend.getCurrentLoc().getLoc() != null) {

                    final double[] loc = currentFriend.getCurrentLoc().getLoc();

                    final Bitmap pic = Bitmap.createScaledBitmap(
                            SocialHandler.pictureToBitMap(currentFriend.getProfilePic())
                            , 100, 100, false);

                    Canvas canvas = new Canvas(pic);
                    Drawable shape = getResources().getDrawable(R.drawable.marker_layout);
                    shape.setBounds(0, 0, pic.getWidth(), pic.getHeight());
                    shape.draw(canvas);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Marker m = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(loc[0], loc[1]))
                                    .title(currentFriend.getFirstname())
                                    .snippet("DRIVING")
                                    .icon(BitmapDescriptorFactory.fromBitmap(pic)));
                            String mID = m.getId();
                            friendMarker.put(mID, currentFriend);
                            System.out.println("---Picture is now a marker---");
                            hasMarker = true;
                            if(selectedFriend == currentFriend.getFriendId()){
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(loc[0], loc[1])));

                            }

                        }
                    });

                }
            }
        }
    }

    @Override
    public void onFriendRequestsFetched(ArrayList<Friend> friends) {

    }

    /*
     * This method will populate the map with the friend pictures and put
     * them on the currect position.
     */

    class PopFriends extends TimerTask{
        public void run(){
            DataHandler.gI().getSocialHandler().fetchFriends(thisMapFrag,
                    SocialHandler.FriendsUpdateMode.ONLINE);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (SettingsHandler.getInstance().isNormalMap()) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        System.out.println("The mapType is Normal in the UIThread ");
                    } else {
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        System.out.println("The mapType is hybrid in the UIThread ");
                    }
                }
            });
        }
    }

    public void onStop(){
        super.onStop();
        if(t != null) {
            t.cancel();
            t = null;
        }
    }
    public void onResume(){
        super.onResume();
        if(mapLoaded && t == null) {
            t = new Timer();
            timer = new PopFriends();
            t.schedule(timer, 0, 10000);
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
