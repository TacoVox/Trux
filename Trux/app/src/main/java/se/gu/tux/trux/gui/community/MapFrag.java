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
import se.gu.tux.trux.application.SocialHandler;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

public class MapFrag extends Fragment implements OnMapReadyCallback, FriendFetchListener {

    private GoogleMap mMap;
    private LatLng loc;
    private MapFragment f;
    
    private HashMap<String, Friend> friendMarker;
    
    private ArrayList<Friend> friends;
    private MapFrag thisMapFrag = this;
    
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
            if (!isDriving()) {
                String markerID = marker.getId();
                MapCommunityWindow fragment = new MapCommunityWindow();
                Bundle sendToInfoFragment = new Bundle();
                sendToInfoFragment.putSerializable("friendHashmap", friendMarker);
                sendToInfoFragment.putString("markerID", markerID);
                fragment.setArguments(sendToInfoFragment);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                fragmentTransaction.replace(R.id.menuContainer, fragment);
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

        friendMarker = new HashMap<String, Friend>();

        //Creats a timeTask which will uppdate the posion of the friendUsers
        t = new Timer();
        timer = new PopFriends();
        t.schedule(timer, 0, 10000);
    }
    

    @Override
    public void onFriendsFetched(final ArrayList<Friend> friends) {
        this.friends = friends;

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
                            , 40, 40, false);

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
