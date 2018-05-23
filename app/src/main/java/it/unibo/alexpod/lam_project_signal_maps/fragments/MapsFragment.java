package it.unibo.alexpod.lam_project_signal_maps.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unibo.alexpod.lam_project_signal_maps.R;
import it.unibo.alexpod.lam_project_signal_maps.adapters.SignalInfoWindowAdapter;
import it.unibo.alexpod.lam_project_signal_maps.enums.SignalType;
import it.unibo.alexpod.lam_project_signal_maps.maps.CoordinateConverter;
import it.unibo.alexpod.lam_project_signal_maps.permissions.PermissionsRequester;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalMgrsAvgCount;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalRepository;
import it.unibo.alexpod.lam_project_signal_maps.utils.MapsDrawUtilities;
import it.unibo.alexpod.lam_project_signal_maps.utils.MathUtils;

public class MapsFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private static int SAMPLES_FOR_MAX_INTENSITY = 10;

    private static double quadrantsDistance = 10.1; //10.1 meters
    private SupportMapFragment mMapView;

    private CameraPosition lastCameraPosition = null;
    private GoogleMap currentMap = null;
    private PermissionsRequester permissionsRequester = null;
    private SharedPreferences preferences;

    private SignalRepository signalRepository;

    private Marker currentPointMarker = null;

    private HashMap<String, SignalMgrsAvgCount> lastFetchedSamples = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionsRequester = new PermissionsRequester(
                this.getActivity(),
                this.getContext()
        );
        // Get shared preferences
        preferences = getContext().getSharedPreferences(SettingsFragment.PREFERENCES_NAME, Context.MODE_PRIVATE);
        // Get signal repository
        signalRepository = new SignalRepository(getActivity().getApplication());
        lastFetchedSamples = new HashMap<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.maps_fragment, container, false);
        mMapView = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mMapView.getMapAsync(this);
        return view;
    }

    @Override
    public void onPause() {
        // save the last position
        if (this.currentMap != null) {
            lastCameraPosition = this.currentMap.getCameraPosition();
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
            // if a map is set and last position is known
            if (lastCameraPosition != null && this.currentMap != null) {
                // restore the last position
                this.currentMap.moveCamera(CameraUpdateFactory.newCameraPosition(lastCameraPosition));
            }
        }
    }

    /*
    * type: 0: Wifi, 1: UMTS, 2: LTE
    * */
    public void setSignalType(final SignalType type) {
        final GoogleMap mMap = this.currentMap;
        if (mMap != null) {
            LoadSamplesAsyncTask loadSamplesAsyncTask = new LoadSamplesAsyncTask(signalRepository, type, mMap, lastFetchedSamples);
            loadSamplesAsyncTask.execute();

        }
    }

    private static void renderSamplesOnMap(SignalType type, GoogleMap mMap, HashMap<LatLng, SignalMgrsAvgCount> mapPoints) {
        // Reset points on the map
        mMap.clear();
        // Draw squares on map
        for (Map.Entry<LatLng, SignalMgrsAvgCount> point : mapPoints.entrySet()) {
            LatLng latLngQuadrant = point.getKey();
            float sampledValue = point.getValue().avgPower;
            long samplesCount = point.getValue().samplesCount;

            float rescaledValue = sampledValue;
            // Rescale differently according to signal type
            if (type == SignalType.Wifi)
                rescaledValue = MathUtils.rescaleInInterval(sampledValue, 0, 100, 0, 1);
            else if (type == SignalType.UMTS)
                rescaledValue = MathUtils.rescaleInInterval(sampledValue, 0, 31, 0, 1);
            else if (type == SignalType.LTE)
                rescaledValue = MathUtils.rescaleInInterval(sampledValue, 0, 97, 0, 1);
            // Scale alpha according do number of samples
            int alpha = 100 + Math.min(SAMPLES_FOR_MAX_INTENSITY * 10, (int) samplesCount * 10);
            int color = MathUtils.interpolateColors(Color.RED, Color.GREEN, alpha, rescaledValue);

            MapsDrawUtilities.drawSquare(mMap, latLngQuadrant, quadrantsDistance, color);
        }
    }

    public void setUserLocationEnabled(){
        // if a map instance and permissions instance is available
        if(this.currentMap != null && this.permissionsRequester != null) {
            // if there are permissions and user has enabled location on map
            if ((permissionsRequester.isGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    permissionsRequester.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) &&
                    preferences.getBoolean(SettingsFragment.DISPLAY_USER_LOCATION_PREFERENCE_KEY, true)) {
                this.currentMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        /* Perform initialization */
        this.currentMap = googleMap;
        this.currentMap.getUiSettings().setZoomControlsEnabled(true);
        this.currentMap.getUiSettings().setZoomGesturesEnabled(true);
        this.setUserLocationEnabled();
        this.setSignalType(SignalType.Wifi);
        // set custom InfoWindow adapter
        this.currentMap.setInfoWindowAdapter(new SignalInfoWindowAdapter(this.getActivity()));
        // Set onclick listener
        this.currentMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng clickedLatLng) {
                /*
                * We have three conditions
                * if not null then remove and add new
                * if not null but same zone then keep old
                * if null then add
                */
                // if current marker is present and the click's coordinates are in different quadrant
                if(currentPointMarker != null &&
                        !CoordinateConverter.LatLngToMgrsQuadrant(currentPointMarker.getPosition()).equals(CoordinateConverter.LatLngToMgrsQuadrant(clickedLatLng))) {
                    // remove the marker
                    currentPointMarker.remove();
                    // reset the marker
                    currentPointMarker = null;
                }
                // if no marker is set
                if(currentPointMarker == null) {
                    String currentMgrs = CoordinateConverter.LatLngToMgrsQuadrant(clickedLatLng);
                    // if there is an entry for that zone
                    if(lastFetchedSamples != null && lastFetchedSamples.containsKey(currentMgrs)) {
                        SignalMgrsAvgCount signalMgrsAvgCountEntry = lastFetchedSamples.get(currentMgrs);
                        Marker zoneMarker = currentMap.addMarker(new MarkerOptions()
                                .position(clickedLatLng)
                                .alpha(0.0f)
                                .infoWindowAnchor(.5f, 1.0f));
                        zoneMarker.setTag(signalMgrsAvgCountEntry);
                        zoneMarker.showInfoWindow();

                        currentPointMarker = zoneMarker;
                    }
                }
            }
        });
    }

    private static class LoadSamplesAsyncTask extends AsyncTask<Void, Void, HashMap<LatLng, SignalMgrsAvgCount>>{

        private SignalType type;
        private GoogleMap mMap;
        private SignalRepository signalRepository;
        // used as a buffer for the upper class which won't need to query once this dict is filled
        private HashMap<String, SignalMgrsAvgCount> lastFetchedData;

        private LoadSamplesAsyncTask(SignalRepository signalRepository, SignalType type, GoogleMap mMap, HashMap<String, SignalMgrsAvgCount> lastFetchedDataBuffer){
            this.signalRepository = signalRepository;
            this.type = type;
            this.mMap = mMap;
            this.lastFetchedData = lastFetchedDataBuffer;
        }

        private HashMap<LatLng, SignalMgrsAvgCount> getData(SignalType type) {
            HashMap<LatLng, SignalMgrsAvgCount> mapPoints = new HashMap<>();
            List<SignalMgrsAvgCount> samplesMgrsAvgCount = signalRepository.getAllSamplesAndCountPerZone(type);
            // TODO: can optimize here and get a reference instead of using another structure
            this.lastFetchedData.clear();
            for (SignalMgrsAvgCount sample : samplesMgrsAvgCount) {
                mapPoints.put(CoordinateConverter.MgrsToLatLng(sample.mgrs), sample);
                this.lastFetchedData.put(sample.mgrs, sample);
            }
            return mapPoints;
        }

        @Override
        protected HashMap<LatLng, SignalMgrsAvgCount> doInBackground(Void... voids) {
            return getData(type);
        }

        @Override
        protected void onPostExecute(HashMap<LatLng, SignalMgrsAvgCount> mapPoints) {
            super.onPostExecute(mapPoints);
            // Render the loaded data
            renderSamplesOnMap(this.type, this.mMap, mapPoints);
        }
    }
}
