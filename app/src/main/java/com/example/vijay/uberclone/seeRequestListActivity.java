package com.example.vijay.uberclone;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class seeRequestListActivity extends AppCompatActivity {
    ListView listView;


    ArrayList<String> requests=new ArrayList<String>();
    ArrayList<Double> requestLatitudes=new ArrayList<Double>();
    ArrayList<Double> requestLongitudes=new ArrayList<Double>();
    ArrayList<String> usernames=new ArrayList<String>();


    ArrayAdapter arrayAdapter;
    LocationManager locationManager;
    LocationListener locationListener;




    public void updateListView(Location location){
        if (location!=null){
        ParseQuery<ParseObject> query=new ParseQuery<ParseObject>("request");
        final ParseGeoPoint parseGeoPoint=new ParseGeoPoint(location.getLatitude(),location.getLongitude());
        query.whereDoesNotExist("isDriverAlloted");
        query.whereNear("location",parseGeoPoint);
        query.setLimit(10);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                requests.clear();
                requestLongitudes.clear();
                requestLatitudes.clear();
                usernames.clear();
                if (e==null){
                    if (objects.size()>0){
                        for(ParseObject object:objects){
                            ParseGeoPoint requestLocations=object.getParseGeoPoint("location");
                            Double distance=parseGeoPoint.distanceInKilometersTo(requestLocations);
                            Double roundedValue=(double) Math.round(distance*10)/10;

                            requests.add(roundedValue+" kms");

                            requestLatitudes.add(requestLocations.getLatitude());
                            requestLongitudes.add(requestLocations.getLongitude());

                            usernames.add(object.getString("username"));
                        }

                    }else {
                        requests.add("No nearby requests found!");
                    }
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        });

    }}




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,1,locationListener);
                Location lastKnownLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateListView(lastKnownLocation);
            }
        }}


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_request_list);

        listView=findViewById(R.id.listView);
        requests.clear();
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,requests);
        requests.clear();
        requests.add("Getting nearby requests...");
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (ContextCompat.checkSelfPermission(seeRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                Location location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (requestLatitudes.size()>position &&requestLongitudes.size()>position && usernames.size()>position && location!=null){

                    Intent intent=new Intent(getApplicationContext(),driverLocationActivity.class);

                    intent.putExtra("requestLatitude",requestLatitudes.get(position));
                    intent.putExtra("requestLongitude",requestLongitudes.get(position));
                    intent.putExtra("driverLocationLatitude",location.getLatitude());
                    intent.putExtra("driverLocationLongitude",location.getLongitude());
                    intent.putExtra("username",usernames.get(position));

                    startActivity(intent);
                }}
            }
        });




        locationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateListView(location);
                ParseUser.getCurrentUser().put("location",new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
                ParseUser.getCurrentUser().saveInBackground();

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



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,1,locationListener);
            Location location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location!=null){
                updateListView(location);
            }
        }
    }
}
