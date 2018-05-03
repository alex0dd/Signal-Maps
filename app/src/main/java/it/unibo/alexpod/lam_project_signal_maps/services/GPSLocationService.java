package it.unibo.alexpod.lam_project_signal_maps.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Random;

import it.unibo.alexpod.lam_project_signal_maps.maps.CoordinateConverter;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalDatabase;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalSample;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalSampleDao;

public class GPSLocationService extends Service{

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meter
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 second

    private FusedLocationProviderClient mFusedLocationClient;

    private WifiScanReceiver wifiReceiver;

    private WifiManager wifiManager;

    public int mockSignal(){
        Random r = new Random();
        return (r.nextInt(100) % 20) + 80;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread("GPSServiceThread");
        LocationRequest mLocationRequest = new LocationRequest();
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    // Perform Wifi scan
                    ((WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE)).startScan();
                }
            }
        };
        mLocationRequest.setInterval(MIN_TIME_BW_UPDATES);
        mLocationRequest.setSmallestDisplacement(MIN_DISTANCE_CHANGE_FOR_UPDATES);

        // Create client instance and request for updates
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, handlerThread.getLooper());

        // Get Wifi manager
        wifiManager = ((WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE));
        // Start Wifi broadcast receiver on another thread
        wifiReceiver = new WifiScanReceiver();
        HandlerThread wifiHandlerThread = new HandlerThread("WifiHandlerThread");
        wifiHandlerThread.start();
        // Get the new thread's handler
        Handler wifiHandler = new Handler(wifiHandlerThread.getLooper());
        registerReceiver(
                wifiReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
                null,
                wifiHandler
        );
    }

    private class WifiScanReceiver extends BroadcastReceiver {
        @SuppressLint("MissingPermission")
        public void onReceive(Context c, Intent intent) {
            // Attach callback for last known location
            mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    // Get last known location
                    Location location = task.getResult();
                    LatLng latLngLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    String locationQuadrant = CoordinateConverter.LatLngToMgrsQuadrant(latLngLocation);

                    // Get the most recent scan results
                    List<ScanResult> wifiScanList = wifiManager.getScanResults();
                    for(int i = 0; i < wifiScanList.size(); i++){
                        String info = wifiScanList.get(i).toString();
                        System.out.println("quadrant: "+locationQuadrant+", info: "+info);
                    }

                    System.out.println("rssi:" + wifiManager.getConnectionInfo().getRssi());

                    // TODO: measure this value instead of mocking it
                    int sampledValue = mockSignal();//signals.get(locationQuadrant);
                    System.out.println(location.getTime() + " " + locationQuadrant + " " + sampledValue);

                    // TODO: persist to database on another thread
                    SignalDatabase dbInstance = SignalDatabase.getInstance(getApplicationContext());
                    SignalSampleDao signalSampleDao = dbInstance.getSignalSampleDao();
                    signalSampleDao.insert(new SignalSample(locationQuadrant, location.getTime(), sampledValue, 0));
                }
            });

            /*
            //print cell info
            List<CellInfo> cInfoList = ((TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).getAllCellInfo();
            //System.out.println(cInfoList);
            for (CellInfo info : cInfoList){
                // if registred to that cell
                if(info.isRegistered()) {
                    if (info instanceof CellInfoLte) {
                        CellSignalStrengthLte signalStrength = ((CellInfoLte) info).getCellSignalStrength();
                        System.out.println("[LTE]dbm: " + signalStrength.getDbm() + ", asu level: " +
                                signalStrength.getAsuLevel() + ", level: " +
                                signalStrength.getLevel());
                    } else if (info instanceof CellInfoWcdma) {
                        CellSignalStrengthWcdma signalStrength = ((CellInfoWcdma) info).getCellSignalStrength();
                        System.out.println("[UMTS] dbm: " + signalStrength.getDbm() + ", asu level: " +
                                signalStrength.getAsuLevel() + ", level: " +
                                signalStrength.getLevel());
                    } else if (info instanceof CellInfoGsm) {
                        CellSignalStrengthGsm signalStrength = ((CellInfoGsm) info).getCellSignalStrength();
                        System.out.println("[GSM] dbm: " + signalStrength.getDbm() + ", asu level: " +
                                signalStrength.getAsuLevel() + ", level: " +
                                signalStrength.getLevel());
                    }
                }
            }*/
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(wifiReceiver);
        super.onDestroy();
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
