package it.unibo.alexpod.lam_project_signal_maps.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Random;

import it.unibo.alexpod.lam_project_signal_maps.maps.CoordinateConverter;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalDatabase;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalSample;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalSampleDao;

public class GPSLocationService extends Service{

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meter
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 second

    public HashMap<String, Integer> buildMockSignalData(){
        HashMap<String, Integer> signals = new HashMap<>();
        Random r = new Random();
        int delta = 10;
        for(int i = 2746-delta; i < 2752+delta; i++){
            for(int j = 342-delta; j < 352+delta; j++){
                signals.put("31UFU "+i+" 0"+j, (r.nextInt(100)%20)+80);
            }
        }
        return signals;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        final HashMap<String, Integer> signals = buildMockSignalData();
        LocationRequest mLocationRequest = new LocationRequest();
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    LatLng latLngLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    String locationQuadrant = CoordinateConverter.LatLngToMgrsQuadrant(latLngLocation);
                    LatLng latLngQuadrant = CoordinateConverter.MgrsToLatLng(locationQuadrant);

                    // TODO: measure this value instead of mocking it
                    int sampledValue = signals.get(locationQuadrant);
                    System.out.println(location.getTime() + " " + locationQuadrant + " " + sampledValue);
                    // TODO: persist to database on another thread
                    SignalDatabase dbInstance = SignalDatabase.getInstance(getApplicationContext());
                    SignalSampleDao signalSampleDao = dbInstance.getSignalSampleDao();
                    signalSampleDao.insert(new SignalSample(locationQuadrant, location.getTime(), sampledValue, 0));
                }

            }
        };
        mLocationRequest.setInterval(MIN_TIME_BW_UPDATES);
        mLocationRequest.setSmallestDisplacement(MIN_DISTANCE_CHANGE_FOR_UPDATES);
        // Create client instance and request for updates
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
