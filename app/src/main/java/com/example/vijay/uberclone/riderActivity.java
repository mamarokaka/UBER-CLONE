package com.example.vijay.uberclone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class riderActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    boolean isFirstTime=true;
    boolean isFirstTime2=true;
    Button button;
    boolean isUberRequested=true;
    TextView textView;
    Handler handler=new Handler();
    boolean driverActive=false;

    ArrayList<Marker> markers=new ArrayList<Marker>();


    public void checkForUpdates(){
        ParseQuery<ParseObject> query=new ParseQuery<ParseObject>("request");
        query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        query.whereExists("isDriverAlloted");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (objects.size()>0){
                        driverActive=true;
                        textView.setText("Your driver is on the way!");
                        ParseQuery<ParseUser> query1=ParseUser.getQuery();
                        query1.whereEqualTo("username",objects.get(0).getString("isDriverAlloted"));
                        query1.findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> objects, ParseException e) {
                                if (e==null && objects.size()>0){
                                    ParseGeoPoint userLocation=objects.get(0).getParseGeoPoint("location");
                                    if (ContextCompat.checkSelfPermission(riderActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                                        Location location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                        if (location!=null){
                                            ParseGeoPoint driverLocation=new ParseGeoPoint(location.getLatitude(),location.getLongitude());
                                            Double distance=driverLocation.distanceInKilometersTo(userLocation);
                                            Double roundedValue=(double) Math.round(distance*10)/10;
                                            if (roundedValue<0.01) {
                                            textView.setText("Your Uber is here !");
                                            isUberRequested=true;
                                            driverActive=false;
                                            button.setText("CALL UBER");
                                                ParseQuery<ParseObject> query=new ParseQuery<ParseObject>("request");
                                                query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
                                                query.findInBackground(new FindCallback<ParseObject>() {
                                                    @Override
                                                    public void done(List<ParseObject> objects, ParseException e) {
                                                        if (e==null && objects.size()>0){
                                                            for (ParseObject object:objects){
                                                                object.deleteInBackground();
                                                            }
                                                        }
                                                    }
                                                });
                                            }else{
                                            textView.setText("Your driver is "+Double.toString(roundedValue)+" kms away!");



                                            LatLng driverLocationLatLng = new LatLng(driverLocation.getLatitude(),driverLocation.getLongitude());
                                            LatLng requestLocationLatLng = new LatLng(userLocation.getLatitude(),userLocation.getLongitude());
                                            markers.clear();
                                            mMap.clear();
                                            markers.add(mMap.addMarker(new MarkerOptions().position(driverLocationLatLng).title("Your Location")));
                                            markers.add(mMap.addMarker(new MarkerOptions().position(requestLocationLatLng).title("Request Location")  .snippet("and snippet")
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));

                                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                            for (Marker marker : markers) {
                                                builder.include(marker.getPosition());
                                            }
                                            LatLngBounds bounds = builder.build();

                                            int padding = 60; // offset from edges of the map in pixels
                                            if (isFirstTime2){
                                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                                            mMap.moveCamera(cu);

                                            mMap.animateCamera(cu);
                                            isFirstTime2=false;
                                            }}

                                        }
                                    }
                                }
                            }
                        });
                    }
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkForUpdates();
                    }
                },2000);
            }
        });
    }

    public void requestUber(View view){
        if (isUberRequested){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            final Location lastKnownLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation!=null){
                ParseObject object=new ParseObject("request");
                object.put("username", ParseUser.getCurrentUser().getUsername());
                ParseGeoPoint parseGeoPoint=new ParseGeoPoint(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
                object.put("location",parseGeoPoint);
                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e==null){
                            mMap.clear();
                            driverActive=false;
                            zoomOnUserLocation(lastKnownLocation);
                            isUberRequested=false;
                            Toast.makeText(getApplicationContext(),"Uber on the way!",Toast.LENGTH_SHORT).show();
                            button.setText("CANCEL UBER");
                            checkForUpdates();

                        }
                    }
                });
            }
        }}else {
            ParseQuery<ParseObject> query=new ParseQuery<ParseObject>("request");
            query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e==null){
                        if (objects.size()>0){
                            isUberRequested=true;
                            button.setText("CALL UBER");
                            for (ParseObject object:objects){
                                object.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e==null){
                                            textView.setText("");
                                            Toast.makeText(getApplicationContext(),"Uber request has been canceled!",Toast.LENGTH_SHORT).show();
                                            markers.clear();
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,1,locationListener);
            Location lastKnownLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            zoomOnUserLocation(lastKnownLocation);
        }
    }}

    public void zoomOnUserLocation(Location location){
        if (driverActive==false){
        LatLng userLocation=new LatLng(location.getLatitude(),location.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
        if (isFirstTime){
        CameraUpdate cameraUpdate=CameraUpdateFactory.newLatLngZoom(userLocation,15);
        mMap.animateCamera(cameraUpdate);
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
        isFirstTime=false;
        }}
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        textView=findViewById(R.id.infoText);

        button=findViewById(R.id.button2);
        ParseQuery<ParseObject> query=new ParseQuery<ParseObject>("request");
        query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (objects.size()>0){
                            isUberRequested=false;
                            button.setText("CANCEL UBER");
                            checkForUpdates();
                    }
                }
            }
        });
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

        locationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                zoomOnUserLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,1,locationListener);
            Location location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location!=null){
                zoomOnUserLocation(location);
            }
        }

    }
}
