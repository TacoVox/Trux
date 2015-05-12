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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.application.LoginService;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.trux.technical_services.LocationService;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

public class MapFrag extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Friend friend[];
    private Picture picture[];
    private double[] loc;
    private LatLng[] latLng;

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
        MapFragment f = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        f.getMapAsync(this);
        return view;

    }
    //A listner which listen to the location of the user
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            if(mMap != null){
                mMap.animateCamera(CameraUpdateFactory.newLatLng(loc));

            }
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Makes it possible for the map to locate the device
        mMap.setMyLocationEnabled(true);
        //Gets the satelite pictures as a map
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);



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

        mMap.moveCamera(CameraUpdateFactory.newLatLng (new LatLng(0, 0)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMyLocationChangeListener(myLocationChangeListener);

        //Creats a timeTask which will uppdate the posion of the friendUsers
        t = new Timer();
        timer = new popFriends();
        t.schedule(timer, 0, 10000);

    }

    class popFriends extends TimerTask{
        public  void run(){
           new AsyncTask(){
               @Override
                protected Object doInBackground(Object[] objects){
                   try{
                       System.out.println("Inne i popFriends------------");
                       friend = DataHandler.getInstance().getFriends();
                       picture = new Picture[friend.length];
                       if(friend.length > 0){
                           System.out.println("Friend.length > 0------------");
                           for(int i = 0; i < friend.length; i++){
                               double temp = friend[i].getFriendId();
                               for(int j = 0; j < friend.length; j++){
                                   if(temp == friend[j].getFriendId()){
                                       try{
                                           picture[j] = DataHandler.getInstance().getPicture(friend[j].getProfilePicId());
                                       }
                                       catch(NotLoggedInException nLIE){
                                           System.out.println("NotLoggedInException: " + nLIE);
                                       }
                                       if(friend[j].getCurrentLoc().getLoc() != null) {
                                           double[] loc = friend[j].getCurrentLoc().getLoc();
                                           LatLng[] latLng = new LatLng[friend.length];
                                           latLng[j] = new LatLng(loc[0], loc[1]);
                                       }
                                   }
                               }
                           }
                           final Picture[] newPicture = picture;
                           final Friend[] newFriend = friend;

                           final LatLng[] newLatLng = latLng;
                           getActivity().runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   System.out.println("In the Run on UI Thread-----------");
                                   if(newPicture != null)
                                   for (int i = 0; i < newPicture.length; i++) {
                                       if(newLatLng != null) {
                                           if (hasMarker) {
                                               mMap.clear();
                                               hasMarker = false;
                                           } else if (newPicture != null && newPicture[i] != null) {
                                               Bitmap bmp;
                                               BitmapFactory.Options options = new BitmapFactory.Options();
                                               bmp = BitmapFactory.decodeByteArray(newPicture[i].getImg(), 0,
                                                       newPicture[i].getImg().length, options);
                                               Bitmap reBmp = Bitmap.createScaledBitmap(bmp, 50, 50, false);
                                               mMap.addMarker(new MarkerOptions().position(new LatLng(46.6872, 11.7564)).title(
                                                       "Here is" + newFriend[i].getFirstname())
                                                       .icon(BitmapDescriptorFactory.fromBitmap(reBmp)));
                                               hasMarker = true;
                                           }
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
  /*
    class popFriends extends TimerTask{
        public void run(){
            new AsyncTask(){
                @Override
                protected Object doInBackground(Object[] objects){
                    try{
                        System.out.println("Inne i popFriends------------------------");
                        friend = DataHandler.getInstance().getFriends();
                        picture = new Picture[friend.length];
                        if(friend.length > 0){
                            System.out.println("Inne i friend > 0 ------------------------");

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    for(int i = 0; i < friend.length; i++){
                                        double temp = friend[i].getFriendId();
                                        for(int j = 0; j < friend.length; j++){
                                            if(temp == friend[j].getFriendId()){
                                                try{
                                                    picture[j] = DataHandler.getInstance().getPicture(friend[j].getProfilePicId());
                                                }
                                                catch (NotLoggedInException nLIE){
                                                    System.out.println("NotLoggedInException: " + nLIE.getMessage());
                                                }
                                                System.out.println("It finds the user id and finds the User----------");
                                                double[] loc = friend[j].getCurrentLoc().getLoc();
                                                LatLng latLng = new LatLng(loc[0], loc[1]);
                                                if(hasMarker){
                                                    mMap.clear();
                                                    hasMarker = false;
                                                }
                                                else
                                                    if(picture != null && picture[j] != null){
                                                        Bitmap bmp;
                                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                                        bmp = BitmapFactory.decodeByteArray(picture[j].getImg(), 0,
                                                                picture[j].getImg().length, options);
                                                        mMap.addMarker(new MarkerOptions().position(latLng).title(
                                                                "Here is" + friend[j].getFirstname())
                                                                .icon(BitmapDescriptorFactory.fromBitmap(bmp)));
                                                        hasMarker = true;
                                                    }
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                    catch (NotLoggedInException nLIE){
                        System.out.println("NotLoggedInException: " + nLIE.getMessage());
                    }


                    return null;}
            }.execute();
        }
    }
*/

    public void onStop(){
        super.onStop();
        if(t != null) {
            t.cancel();
        }
    }
}
