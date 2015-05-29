package se.gu.tux.trux.gui.community;




import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;


import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;


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

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.application.FriendFetchListener;
import se.gu.tux.trux.application.SettingsHandler;
import se.gu.tux.trux.application.SocialHandler;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Notification;
import se.gu.tux.trux.gui.base.TimerUpdateFragment;
import se.gu.tux.trux.gui.main_home.HomeActivity;
import tux.gu.se.trux.R;

public class MapFrag extends TimerUpdateFragment implements OnMapReadyCallback, FriendFetchListener,
        GoogleMap.OnMapClickListener  {

    private GoogleMap mMap;
    private LatLng loc;
    private MapFragment f;

    private HashMap<String, Friend> friendMarker;

    private ArrayList<Friend> friends;
    private MapFrag thisMapFrag = this;

    private boolean hasMarker = false;
    private boolean mapLoaded = false;
    private boolean stopped = true;
    private boolean followingUser = false;

    private RelativeLayout loadingPanel;
    private HomeActivity homeActivity = null;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Creates the view and initilize the MapFrag to the mapfragment in the layout.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return  returns view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        loadingPanel = (RelativeLayout) view.findViewById(R.id.loadingPanel);

        //Setting a Mapfragment so that it calls to the getMapAsync which is connected to onMapReady
        f = (MapFragment) homeActivity.getFragmentManager().findFragmentById(R.id.map);
        f.getMapAsync(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        homeActivity = (HomeActivity)activity;
    }


    /**
     * Sets a OnClickListener for the myLocation button.
     */
    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener =
            new GoogleMap.OnMyLocationButtonClickListener() {
                /**
                 * When pressed it will go the users location.
                 *
                 * @return  the state of the button
                 */
                @Override
                public boolean onMyLocationButtonClick(){
                    {
                        //Set the value of the selected friend to -1 so it stops following any friend
                        homeActivity.setSelectedFriend(new Long(-1));

                        //Follows the user on the map
                        mMap.setOnMyLocationChangeListener(startFollowing);
                        followingUser = true;
                        //Makes a toast of how to stop following your loaction.
                        Toast.makeText(homeActivity.getApplication(),
                                "Following You.\nPress Map to stop Following.", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            };


    /**
     * A listener which listen to the location of the user
     */
    private GoogleMap.OnMyLocationChangeListener startFollowing = new GoogleMap.OnMyLocationChangeListener() {
        /**
         * This method listens to the location of the user and sets the camera to that position.
         *
         * @param location  the users location from the device.
         */
        @Override
        public void onMyLocationChange(Location location) {
            //Gets the users location
            loc = new LatLng(location.getLatitude(), location.getLongitude());
            if(mMap != null){
                //Starts following the user.
                mMap.animateCamera(CameraUpdateFactory.newLatLng(loc));

            }
        }
    };

    /**
     * A OnClickListener for the map if the map is clicked.
     * It takes away the fragment that is namned "MENU" from the BackStack.
     * And also sets so that the camera stops following the user and the friend.
     *
     * @param point gets the latitude and longitude from the position you clicked.
     */
    @Override
    public void onMapClick(LatLng point) {
        //Close the map menu that appears when pressing a friend (if it is there)
        homeActivity.getSupportFragmentManager().popBackStackImmediate("MENU",
                FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // Stop following user or friend if currently following
        if (homeActivity.getSelectedFriend() != -1 || followingUser) {

            //Set the value of the selected friend to -1 so it stops follow that person
            homeActivity.setSelectedFriend(new Long(-1));
            //Stops following the user
            mMap.setOnMyLocationChangeListener(null);
            homeActivity.showToast("Stopped following.");
        }
    }

    /**
     * A clicklistener for the marker
     */
    private GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        /**
         * This method takes the marker ID and and creates a new MapCommunityWindow Fragment
         * so that it can pass a friend object to the friend profile page or to be able to message
         * the friend on which marker the user clicked on.
         *
         * @param marker The marker that the user clicked on on the map.
         * @return  Returns the state of the marker false.
         */
        @Override
        public boolean onMarkerClick(Marker marker) {
            //Gets the marker ID
            String markerID = marker.getId();
            //Creates a new fragment with the menu for chating or viewing the selected friend
            MapSocialMenu fragment = new MapSocialMenu();
            //Use the markerID to get the friend of that marker from the hashmap
            Friend friend = friendMarker.get(markerID);
            Bundle sendToInfoFragment = new Bundle();
            //Puts the friend in the bundle
            sendToInfoFragment.putSerializable("friend", friend);
            fragment.setArguments(sendToInfoFragment);
            //Make the transaction to the MapSocialMenu fragment
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            //Names the fragment in the backStack
            homeActivity.getSupportFragmentManager().popBackStackImmediate("MENU",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
            //Adds it to the BackStack
            fragmentTransaction.addToBackStack("MENU");
            fragmentTransaction.replace(R.id.menuContainer, fragment);
            fragmentTransaction.commit();

            return false;
        }
    };

    /**
     * This method initialize the map with it's maptype. It gets the last known location from
     * the device and sets the map camera there, and puts that it zooms in.
     * It sets all the listeners and creates a Timer and TimerTask that will be executed every
     * 10th second.
     *
     * @param googleMap this is defined the API.
     */
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

        //These lines will give you the last known position of the device
        LocationManager locationManager = (LocationManager) homeActivity.getSystemService(Context.LOCATION_SERVICE);
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
        //If you dont have a location you get the view of this position on the map
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(0, 0)));
        }
        //Zooms in the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        //Adds zoom controls to the map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //Takes away the toolbar from the map
        mMap.getUiSettings().setMapToolbarEnabled(false);
        //Sets the listener for the Location Button
        mMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        //Starts following the users location
        mMap.setOnMyLocationChangeListener(startFollowing);
        //Sets the listener for pressing the markers
        mMap.setOnMarkerClickListener(markerClickListener);
        //Listens to the clicklistener OnMapClick
        mMap.setOnMapClickListener(this);

        //Sets the HashMap to be format String, Friend
        friendMarker = new HashMap<String, Friend>();

        // Set flag
        mapLoaded = true;
    }


    /**
     * When the friends are fetched this is executed and take the friends from the ArrayList
     * and print them out as markers on the map.
     *
     * @param friends   ArrayList of all the friends that is online.
     */
    @Override
    public void onFriendsFetched(final ArrayList<Friend> friends) {
        this.friends = friends;
        //Sets the value of the ID from the selected friend in the friendWindow to selectedFriend
        final long selectedFriend =  homeActivity.getSelectedFriend();

        homeActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Clears the markers on the map
                if (hasMarker) {
                    mMap.clear();
                    hasMarker = false;
                }
            }});

        // If following a friend, we need to perform a check if the friend is online.
        // We do this by setting the variable to false, and if it is true the next time (we can't
        // check directly below in the code because the loop is in another thread!) the friend
        // is still online. Otherwise the friend is offline, and we set selected friend to -1.
        if (selectedFriend != -1 && homeActivity.getFriendIsOnline() == true) {
            // Assume friend is offline until proven otherwise in the loop below
            homeActivity.setFriendIsOnline(false);
        } else {
            homeActivity.setSelectedFriend(-1L);
        }

        if (friends != null) {
            for (final Friend currentFriend : friends) {

                if (currentFriend != null && currentFriend.getProfilePic() != null &&
                        currentFriend.getCurrentLoc() != null &&
                        currentFriend.getCurrentLoc().getLoc() != null) {
                    //Sets gets the location of the current friend and puts it in the array
                    final double[] loc = currentFriend.getCurrentLoc().getLoc();
                    //Create a scaled Bitmap from the friend profilePic
                    final Bitmap pic = Bitmap.createScaledBitmap(
                            SocialHandler.pictureToBitMap(currentFriend.getProfilePic())
                            , 100, 100, false);

                    homeActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Creates new Marker for the friend and puts it on the friend location
                            Marker m = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(loc[0], loc[1]))
                                    .title(currentFriend.getFirstname())
                                    .icon(BitmapDescriptorFactory.fromBitmap(pic)));

                            String mID = m.getId();
                            //Puts the created markers ID to the hashmap and the currentFriend
                            friendMarker.put(mID, currentFriend);
                            //Set hasMarker to true so that it will erase all the markers when the method is runned again
                            hasMarker = true;
                            //Set the position of the selected friend in the friendlist
                            if(selectedFriend == currentFriend.getFriendId()){
                                // Make sure we are not following the user instead of the friend
                                mMap.setOnMyLocationChangeListener(null);
                                // Move the camera to the friend
                                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(loc[0], loc[1])));
                                // The friend is still online!
                                homeActivity.setFriendIsOnline(true);
                            }
                        }
                    });
                }
            }
            homeActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingPanel.setVisibility(View.GONE);
                }
            });
        }
    }

    /**
     * This mehtod is never used.
     *
     * @param friends   ArrayList of FriendRequests
     */
    @Override
    public void onFriendRequestsFetched(ArrayList<Friend> friends) {}


    @Override
    public void setStatus(final DataHandler.SafetyStatus safetyStatus, Notification notificationStatus) {
        // Update the map
        if(mapLoaded && !stopped) {
            //Fetch the friends from the SocialHandler - will result in calling onFriendsFetched
            DataHandler.gI().getSocialHandler().fetchFriends(thisMapFrag,
                    SocialHandler.FriendsUpdateMode.ONLINE);

            homeActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Sets the map to the map_type which the user has choosen in settings
                    if (SettingsHandler.getInstance().isNormalMap()) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    } else {
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    }

                    //If the user is driving lock the google map screen
                    if(safetyStatus != DataHandler.SafetyStatus.IDLE) {
                        mMap.getUiSettings().setScrollGesturesEnabled(false);
                    } else {
                        //If the user is not driving the map isn't locked
                        mMap.getUiSettings().setScrollGesturesEnabled(true);
                    }
                }
            });
        }
    }




    /**
     * Set the stop flag.
     */
    @Override
    public void onStop() {
        super.onStop();
        stopped = true;
    }


    /**
     * Resumes the fragment. Set the stoppped flag to false.
     * Show the loading panel until map has been refreshed.
     */
    public void onResume(){
        super.onResume();
        loadingPanel.setVisibility(View.VISIBLE);
        stopped = false;

        // Manually call setStatus to force update of the map
        setStatus(DataHandler.gI().getSafetyStatus(), DataHandler.gI().getNotificationStatus());
    }
}
