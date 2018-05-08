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
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
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

import it.unibo.alexpod.lam_project_signal_maps.enums.SignalType;
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
    private TelephonyManager telephonyManager;

    public int mockSignal(){
        Random r = new Random();
        return (r.nextInt(100) % 20) + 80;
    }

    public Integer getBestValueOfSignal(List<CellInfo> cInfoList, Class<?> cellClass){
        Integer bestValue = null;
        Integer comparisonValue = 99;
        if(cInfoList.size() > 0 && (cellClass.equals(CellInfoWcdma.class) || cellClass.equals(CellInfoLte.class))){
            for(CellInfo cellInfo : cInfoList){
                // if the cell is of the right class and is registred
                if(cellClass.isInstance(cellInfo) && cellInfo.isRegistered()) {
                    // the cellClass is either CellInfoLte or CellInfoWcdma so one of the two branches will be executed
                    if (cellClass.equals(CellInfoLte.class)) {
                        comparisonValue = ((CellInfoLte) cellInfo).getCellSignalStrength().getAsuLevel();
                    } else if (cellClass.equals(CellInfoWcdma.class)) {
                        comparisonValue = ((CellInfoWcdma) cellInfo).getCellSignalStrength().getAsuLevel();
                    }
                    // if comparisonValue is not unknown
                    if(comparisonValue != 99) {
                        bestValue = bestValue != null ? Math.max(bestValue, comparisonValue) : comparisonValue;
                    }
                }
            }
        }
        return bestValue;
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

        // Get Telephony manager
        telephonyManager = ((TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE));
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

        // TODO: add permissions check(permissions might've been removed at runtime)
        // TODO: add gps turned on check
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
                    Integer bestWifiSignalLevel = null;
                    Integer bestUMTSSignal, bestLteSignal = null;

                    // TODO: wrap the database calls into a repository pattern
                    // Get the database instance
                    SignalDatabase dbInstance = SignalDatabase.getInstance(getApplicationContext());
                    SignalSampleDao signalSampleDao = dbInstance.getSignalSampleDao();

                    // Get the most recent Wifi scan results
                    List<ScanResult> wifiScanList = wifiManager.getScanResults();
                    if(wifiScanList.size() > 0) {
                        bestWifiSignalLevel = wifiScanList.get(0).level;
                        for (int i = 1; i < wifiScanList.size(); i++) {
                            // if better signal level
                            if(wifiScanList.get(i).level > bestWifiSignalLevel) {
                                bestWifiSignalLevel = wifiScanList.get(i).level;
                            }
                        }
                    }

                    // Get the Cell results
                    List<CellInfo> cInfoList = telephonyManager.getAllCellInfo();
                    bestUMTSSignal = getBestValueOfSignal(cInfoList, CellInfoWcdma.class);
                    bestLteSignal = getBestValueOfSignal(cInfoList, CellInfoLte.class);

                    System.out.println("LTE: "+bestLteSignal+" UMTS: "+bestUMTSSignal+" Wifi: "+bestWifiSignalLevel);
                    // TODO: add settings check to decide if enough time has passed to push to database or not
                    // TODO: abstract these calls into a repository
                    if(bestWifiSignalLevel != null) signalSampleDao.insert(new SignalSample(locationQuadrant, location.getTime(), bestWifiSignalLevel, SignalType.Wifi));
                    if(bestUMTSSignal != null) signalSampleDao.insert(new SignalSample(locationQuadrant, location.getTime(), bestUMTSSignal, SignalType.UMTS));
                    if(bestLteSignal != null) signalSampleDao.insert(new SignalSample(locationQuadrant, location.getTime(), bestLteSignal, SignalType.LTE));
                }
            });
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
