package com.example.kj.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    private GoogleMap mMap;
    //GoogleApiClient mMapClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallBack;
    FusedLocationProviderClient mFusedLocationClient;
    Marker marker;
    Location mLastLocation;
    private DatabaseReference mDatabase;
    EditText et;
    TextView tvLocality,tvLat,tvLng;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = getFusedLocationProviderClient(this);
        mDatabase= FirebaseDatabase.getInstance().getReference("Geolocation");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (googleServicesAvailable()) {
            Toast.makeText(this, "perfect! Google map services available", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_maps);
            initMap();
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        } else {
            Toast.makeText(this, "error! Google map services not available", Toast.LENGTH_LONG).show();
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to google play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    maps user = s.getValue(maps.class);
                    setMarker(user.getLocation(), user.getLatitude(), user.getLongitude());
                    }
                }
                 @Override
                 public void onCancelled(@NonNull DatabaseError databaseError) {
                }
        });
         //getRetrieveLocation();
        if (mMap != null) {
            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                    Geocoder gc= new Geocoder(MapsActivity.this);
                    LatLng ll= marker.getPosition();
                    List<Address> list=null;
                    try {
                        list=gc.getFromLocation(ll.latitude,ll.longitude,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address addrs=list.get(0);
                    marker.setTitle(addrs.getLocality());
                    marker.showInfoWindow();
                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {

                }
            });

            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v = getLayoutInflater().inflate(R.layout.gpswindow, null);
                    tvLocality = (TextView) v.findViewById(R.id.tv_locality);
                    tvLat = (TextView) v.findViewById(R.id.tv_lat);
                    tvLng = (TextView) v.findViewById(R.id.tv_lng);
                    TextView tvsnippet = (TextView) v.findViewById(R.id.tv_snippet);
                    LatLng ll = marker.getPosition();
                    tvLocality.setText(marker.getTitle());
                    tvLat.setText("Latitude: " + ll.latitude);
                    tvLng.setText("Longitude: " + ll.longitude);
                    tvsnippet.setText(marker.getSnippet());
                    return v;
                }
            });
        }
    }



            //goToLocationZoom(16.4964308, 80.6516815, 15);

    //buildGoogleApiClient();


    /*protected synchronized void buildGoogleApiClient(){
        mMapClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mMapClient.connect();  }*/

    /*private void goToLocation(double lat, double lng) {
        LatLng l1 = new LatLng(lat, lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(l1));}*/

    private void goToLocationZoom(double lat, double lng, float zoom) {
        LatLng l1 = new LatLng(lat, lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(l1, zoom));
    }

    public void geoLocate(View view) throws IOException {
        et = (EditText) findViewById(R.id.location_search);
        String location = et.getText().toString();
        Geocoder gc = new Geocoder(this);
        try {
            List<Address> list = gc.getFromLocationName(location, 1);
            Address addr = list.get(0);
            String locality = addr.getLocality();
            Toast.makeText(this, locality, Toast.LENGTH_LONG).show();
            double lat = addr.getLatitude();
            double lng = addr.getLongitude();
            goToLocationZoom(lat, lng, 15f);
            setMarker(locality, lat, lng);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void setMarker(String locality, double lat, double lng) {
        MarkerOptions markerOptions = new MarkerOptions()
                .title(locality)
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                .position(new LatLng(lat, lng))
                .snippet("I am here");
        marker = mMap.addMarker(markerOptions);

        //setMarker(locality, lat, lng);
    }


    public void getRetrieveLocation() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null)
                            return;
                        else {
                            List<Location> locationList = locationResult.getLocations();
                            if (locationList.size() > 0) {
                                Location l= locationList.get(locationList.size() - 1);
                                mLastLocation=l;
                                onLocationChanged(mLastLocation);
                                }//end_if
                        }//end_else
                    }//onlocationresult_method
                },
                Looper.myLooper());

    }


   /* @Override
    public void onConnectionSuspended(int i) {}
     @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}*/

   @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Toast.makeText(this, "can't get current location", Toast.LENGTH_LONG).show();
        } else {
            LatLng l1 = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(l1, 15);
            mMap.animateCamera(update);
        }
    }

    public void AddHungerspot(View view) {
        LatLng l2= marker.getPosition();
        double lat=l2.latitude;
        double lng=l2.longitude;
        String s=tvLocality.getText().toString();
         id = mDatabase.push().getKey();
        maps info=new maps(s,lat,lng);
        mDatabase.child(id).setValue(info);
        Toast.makeText(this,"Added",Toast.LENGTH_LONG).show();

   }

    public void deleteHungerSpot(View view) {
       if(marker!=null){


           marker.remove();
       marker=null;
       Toast.makeText(this,"Deleted",Toast.LENGTH_LONG).show();}
    }

   /* public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }*/

}
