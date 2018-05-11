package it.unibo.alexpod.lam_project_signal_maps.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import it.unibo.alexpod.lam_project_signal_maps.enums.SampleIntervalPreference;
import it.unibo.alexpod.lam_project_signal_maps.enums.SignalType;
import it.unibo.alexpod.lam_project_signal_maps.fragments.SettingsFragment;
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

    private SharedPreferences preferences;

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
        System.out.println("On create service");
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
        // Get shared preferences
        preferences = getSharedPreferences(SettingsFragment.PREFERENCES_NAME, Context.MODE_PRIVATE);
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
                    // TODO: wrap the database calls into a repository pattern
                    // Get the database instance
                    SignalDatabase dbInstance = SignalDatabase.getInstance(getApplicationContext());
                    SignalSampleDao signalSampleDao = dbInstance.getSignalSampleDao();
                    // Get interval preference
                    String sampleIntervalPreference = preferences.getString("sample_interval_preference", "0");
                    // Transform it into SampleIntervalPreference
                    SampleIntervalPreference sampleInterval = SampleIntervalPreference.values()[Integer.parseInt(sampleIntervalPreference)];
                    // Get last known location
                    Location location = task.getResult();
                    // location can get null
                    if(location != null) {
                        LatLng latLngLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        String locationQuadrant = CoordinateConverter.LatLngToMgrsQuadrant(latLngLocation);
                        Integer bestWifiSignalLevel = null;
                        Integer bestUMTSSignal, bestLteSignal = null;
                        boolean shouldSaveWifi = false;
                        boolean shouldSaveUMTS = false;
                        boolean shouldSaveLTE = false;
                        // Last samples of each signal type
                        SignalSample lastSavedWifiSample = signalSampleDao.getLastSample(SignalType.Wifi.getValue());
                        SignalSample lastSavedUMTSSample = signalSampleDao.getLastSample(SignalType.UMTS.getValue());
                        SignalSample lastSavedLTESample = signalSampleDao.getLastSample(SignalType.LTE.getValue());
                        // Datetimes which indicate when the last sample of each signal got saved
                        long lastSavedWifiDatetime = lastSavedWifiSample != null ? lastSavedWifiSample.datetime : 0;
                        long lastSavedUMTSDatetime = lastSavedUMTSSample != null ? lastSavedUMTSSample.datetime : 0;
                        long lastSavedLTEDatetime = lastSavedLTESample != null ? lastSavedLTESample.datetime : 0;
                        long lastScanTime = location.getTime();

                        // Get the most recent Wifi scan results
                        List<ScanResult> wifiScanList = wifiManager.getScanResults();
                        if (wifiScanList.size() > 0) {
                            bestWifiSignalLevel = wifiScanList.get(0).level;
                            for (int i = 1; i < wifiScanList.size(); i++) {
                                // if better signal level
                                if (wifiScanList.get(i).level > bestWifiSignalLevel) {
                                    bestWifiSignalLevel = wifiScanList.get(i).level;
                                }
                            }
                        }

                        // Get the Cell results
                        List<CellInfo> cInfoList = telephonyManager.getAllCellInfo();
                        bestUMTSSignal = getBestValueOfSignal(cInfoList, CellInfoWcdma.class);
                        bestLteSignal = getBestValueOfSignal(cInfoList, CellInfoLte.class);

                        if (lastScanTime - lastSavedWifiDatetime >= sampleInterval.getIntervalMs())
                            shouldSaveWifi = true;
                        if (lastScanTime - lastSavedUMTSDatetime >= sampleInterval.getIntervalMs())
                            shouldSaveUMTS = true;
                        if (lastScanTime - lastSavedLTEDatetime >= sampleInterval.getIntervalMs())
                            shouldSaveLTE = true;

                        System.out.println("LTE: " + bestLteSignal + " UMTS: " + bestUMTSSignal + " Wifi: " + bestWifiSignalLevel);
                        // TODO: abstract these calls into a repository
                        if (bestWifiSignalLevel != null && shouldSaveWifi)
                            signalSampleDao.insert(new SignalSample(locationQuadrant, lastScanTime, bestWifiSignalLevel, SignalType.Wifi));
                        if (bestUMTSSignal != null && shouldSaveUMTS)
                            signalSampleDao.insert(new SignalSample(locationQuadrant, lastScanTime, bestUMTSSignal, SignalType.UMTS));
                        if (bestLteSignal != null && shouldSaveLTE)
                            signalSampleDao.insert(new SignalSample(locationQuadrant, lastScanTime, bestLteSignal, SignalType.LTE));
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(wifiReceiver);
        System.out.println("On destroy service");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("On unbind");
        return super.onUnbind(intent);
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
