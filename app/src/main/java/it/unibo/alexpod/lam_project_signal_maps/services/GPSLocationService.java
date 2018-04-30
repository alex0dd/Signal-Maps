package it.unibo.alexpod.lam_project_signal_maps.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import it.unibo.alexpod.lam_project_signal_maps.maps.CoordinateConverter;
import it.unibo.alexpod.lam_project_signal_maps.maps.MapsDrawUtilities;
import it.unibo.alexpod.lam_project_signal_maps.singletons.GoogleMapsSingleton;

public class GPSLocationService extends Service{

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 9; // 9 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5; // 5 seconds

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        mLocationRequest = new LocationRequest();
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    LatLng latLngLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    String locationQuadrant = CoordinateConverter.LatLngToMgrsQuadrant(latLngLocation);
                    LatLng latLngQuadrant = CoordinateConverter.MgrsToLatLng(locationQuadrant);

                    MapsDrawUtilities.drawSquare(GoogleMapsSingleton.getMap(), latLngQuadrant, 11, Color.RED);
                    System.out.println(location.getTime()+" "+locationQuadrant);
                }

            }
        };
        mLocationRequest.setInterval(MIN_TIME_BW_UPDATES);
        mLocationRequest.setSmallestDisplacement(MIN_DISTANCE_CHANGE_FOR_UPDATES);
        mFusedLocationClient =  LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null );
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
