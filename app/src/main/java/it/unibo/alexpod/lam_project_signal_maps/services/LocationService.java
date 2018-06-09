package it.unibo.alexpod.lam_project_signal_maps.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import it.unibo.alexpod.lam_project_signal_maps.R;
import it.unibo.alexpod.lam_project_signal_maps.enums.SampleIntervalPreference;
import it.unibo.alexpod.lam_project_signal_maps.enums.SignalType;
import it.unibo.alexpod.lam_project_signal_maps.fragments.SettingsFragment;
import it.unibo.alexpod.lam_project_signal_maps.utils.CoordinateConverter;
import it.unibo.alexpod.lam_project_signal_maps.utils.PermissionsRequester;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalRepository;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalSample;

public class LocationService extends Service{

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meter
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 second

    private FusedLocationProviderClient mFusedLocationClient;

    private WifiScanReceiver wifiReceiver;

    private WifiManager wifiManager;
    private TelephonyManager telephonyManager;
    private LocationManager locationManager;

    private SharedPreferences preferences;
    private PermissionsRequester permissionsRequester;
    private SignalRepository signalRepository;

    // Executor which is used by getLastLocation's event handler to run on another thread
    private final Executor locationExecutor = new Executor() {
        @Override
        public void execute(@NonNull Runnable command) {
            command.run();
        }
    };


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        // Get Telephony manager
        telephonyManager = ((TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE));
        // Get Wifi manager
        wifiManager = ((WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE));
        // Get Location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Get shared preferences
        preferences = getSharedPreferences(SettingsFragment.PREFERENCES_NAME, Context.MODE_PRIVATE);
        // Get signal repository
        signalRepository = new SignalRepository(getApplication());

        // Get permissions requester
        permissionsRequester = new PermissionsRequester(null, getApplicationContext());

        // Declare location handler thread
        HandlerThread locationHandlerThread = new HandlerThread("LocationHandlerThread");
        locationHandlerThread.start();

        LocationRequest mLocationRequest = new LocationRequest();
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    // Perform Wifi scan
                    wifiManager.startScan();
                }
            }
        };
        mLocationRequest.setInterval(MIN_TIME_BW_UPDATES);
        mLocationRequest.setSmallestDisplacement(MIN_DISTANCE_CHANGE_FOR_UPDATES);
        // if background sampling is enabled
        if(preferences.getBoolean(SettingsFragment.ENABLE_BACKGROUND_SAMPLING_PREFERENCE_KEY, true)){
            // perform sampling with higher accuracy
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        // if any location permission is enabled and gps is enabled
        if(permissionsRequester.isGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
                permissionsRequester.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

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
            // Create client instance and request for updates
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, locationHandlerThread.getLooper());
        }

        createNotificationChannel();
    }


    private class WifiScanReceiver extends BroadcastReceiver {

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
        public void onReceive(Context c, Intent intent) {
            // Attach callback for last known location
            mFusedLocationClient.getLastLocation().addOnCompleteListener(locationExecutor, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    // Get last known location
                    Location location = task.getResult();
                    // location can get null
                    if(location != null) {
                        LatLng latLngLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        String locationQuadrant = CoordinateConverter.LatLngToMgrsQuadrant(latLngLocation);
                        Integer bestWifiSignalLevel = null;
                        Integer bestUMTSSignal = null;
                        Integer bestLteSignal = null;
                        long lastScanTime = location.getTime();

                        // Get the most recent Wifi scan results
                        List<ScanResult> wifiScanList = wifiManager.getScanResults();
                        if (wifiScanList.size() > 0) {
                            bestWifiSignalLevel = WifiManager.calculateSignalLevel(wifiScanList.get(0).level, 100);
                            for (int i = 1; i < wifiScanList.size(); i++) {
                                int convertedSignalLevel = WifiManager.calculateSignalLevel(wifiScanList.get(i).level, 100);
                                // if better signal level
                                if (convertedSignalLevel > bestWifiSignalLevel) {
                                    bestWifiSignalLevel = convertedSignalLevel;
                                }
                            }
                        }

                        // Get the Cell results
                        List<CellInfo> cInfoList = telephonyManager.getAllCellInfo();
                        bestUMTSSignal = getBestValueOfSignal(cInfoList, CellInfoWcdma.class);
                        bestLteSignal = getBestValueOfSignal(cInfoList, CellInfoLte.class);
                        // Attempt to the samples
                        saveSamples(locationQuadrant, lastScanTime, bestWifiSignalLevel, bestUMTSSignal, bestLteSignal);
                    }
                }
            });
        }
    }

    private void saveSamples(String locationQuadrant, long lastScanTime, Integer bestWifiSignalLevel, Integer bestUMTSSignal, Integer bestLteSignal) {
        // Get interval preference
        String sampleIntervalPreference = preferences.getString(SettingsFragment.SAMPLE_INTERVAL_PREFERENCE_KEY, "0");
        // Transform it into SampleIntervalPreference
        SampleIntervalPreference sampleInterval = SampleIntervalPreference.values()[Integer.parseInt(sampleIntervalPreference)];
        // Last samples of each signal type
        SignalSample lastSavedWifiSample = signalRepository.getLastSample(SignalType.Wifi);
        SignalSample lastSavedUMTSSample = signalRepository.getLastSample(SignalType.UMTS);
        SignalSample lastSavedLTESample = signalRepository.getLastSample(SignalType.LTE);
        // Datetimes which indicate when the last sample of each signal got saved
        long lastSavedWifiDatetime = lastSavedWifiSample != null ? lastSavedWifiSample.datetime : 0;
        long lastSavedUMTSDatetime = lastSavedUMTSSample != null ? lastSavedUMTSSample.datetime : 0;
        long lastSavedLTEDatetime = lastSavedLTESample != null ? lastSavedLTESample.datetime : 0;
        // flags which indicate if a particular signal type sample should be saved
        boolean shouldSaveWifi = false;
        boolean shouldSaveUMTS = false;
        boolean shouldSaveLTE = false;
        if (lastScanTime - lastSavedWifiDatetime >= sampleInterval.getIntervalMs())
            shouldSaveWifi = true;
        if (lastScanTime - lastSavedUMTSDatetime >= sampleInterval.getIntervalMs())
            shouldSaveUMTS = true;
        if (lastScanTime - lastSavedLTEDatetime >= sampleInterval.getIntervalMs())
            shouldSaveLTE = true;

        LinkedList<SignalSample> samplesToInsert = new LinkedList<>();
        if (bestWifiSignalLevel != null && shouldSaveWifi)
            samplesToInsert.add(new SignalSample(locationQuadrant, lastScanTime, bestWifiSignalLevel, SignalType.Wifi));
        if (bestUMTSSignal != null && shouldSaveUMTS)
            samplesToInsert.add(new SignalSample(locationQuadrant, lastScanTime, bestUMTSSignal, SignalType.UMTS));
        if (bestLteSignal != null && shouldSaveLTE)
            samplesToInsert.add(new SignalSample(locationQuadrant, lastScanTime, bestLteSignal, SignalType.LTE));
        signalRepository.insertSamples(samplesToInsert.toArray(new SignalSample[samplesToInsert.size()]));
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            //int importance = NotificationManager.IMPORTANCE_LOW;//.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NotificationCompat.CATEGORY_SERVICE, name, importance);
            channel.setSound(null, null);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public void sendNotification(String title, String message){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, NotificationCompat.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        // if background sampling is not enabled
        if(!preferences.getBoolean(SettingsFragment.ENABLE_BACKGROUND_SAMPLING_PREFERENCE_KEY, false)){
            unregisterReceiver(wifiReceiver);
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // if background sampling is not enabled
        if(!preferences.getBoolean(SettingsFragment.ENABLE_BACKGROUND_SAMPLING_PREFERENCE_KEY, false)){
            return START_NOT_STICKY;
        }
        else{
            return START_STICKY;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
