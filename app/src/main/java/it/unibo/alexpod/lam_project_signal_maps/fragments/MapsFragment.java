package it.unibo.alexpod.lam_project_signal_maps.fragments;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unibo.alexpod.lam_project_signal_maps.R;
import it.unibo.alexpod.lam_project_signal_maps.enums.SignalType;
import it.unibo.alexpod.lam_project_signal_maps.maps.CoordinateConverter;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalDatabase;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalMgrsAvgCount;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalSampleDao;
import it.unibo.alexpod.lam_project_signal_maps.utils.MapsDrawUtilities;
import it.unibo.alexpod.lam_project_signal_maps.utils.MathUtils;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private int SAMPLES_FOR_MAX_INTENSITY = 10;

    private double quadrantsDistance = 10.1; //10.1 meters
    private SupportMapFragment mMapView;

    CameraPosition lastCameraPosition = null;
    GoogleMap currentMap = null;

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
        lastCameraPosition = this.currentMap.getCameraPosition();
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mMapView != null) {
            mMapView.onResume();
            // if a map is set and last position is known
            if(lastCameraPosition != null && this.currentMap != null){
                // restore the last position
                this.currentMap.moveCamera(CameraUpdateFactory.newCameraPosition(lastCameraPosition));
            }
        }
    }

    private HashMap<LatLng, SignalMgrsAvgCount> getData(int type){
        HashMap<LatLng, SignalMgrsAvgCount> mapPoints = new HashMap<>();
        SignalDatabase dbInstance = SignalDatabase.getInstance(getContext());
        SignalSampleDao signalSampleDao = dbInstance.getSignalSampleDao();
        List<SignalMgrsAvgCount> samplesMgrsAvgCount = signalSampleDao.getAllSamplesAndCountPerZone(type);
        for(SignalMgrsAvgCount sample : samplesMgrsAvgCount){
            System.out.println("mgrs: "+sample.mgrs+" power: "+sample.avgPower+" count: "+sample.samplesCount);
            mapPoints.put(CoordinateConverter.MgrsToLatLng(sample.mgrs), sample);
        }
        return mapPoints;
    }

    /*
    * type: 0: Wifi, 1: UMTS, 2: LTE
    * */
    public void setSignalType(SignalType type){
        GoogleMap mMap = this.currentMap;
        if(mMap != null){
            // Reset points on the map
            mMap.clear();
            // Get data from a persistent store
            HashMap<LatLng, SignalMgrsAvgCount> mapPoints = getData(type.getValue());
            // Draw squares on map
            for(Map.Entry<LatLng, SignalMgrsAvgCount> point : mapPoints.entrySet()){
                LatLng latLngQuadrant = point.getKey();
                float sampledValue = point.getValue().avgPower;
                long samplesCount = point.getValue().samplesCount;

                float rescaledValue = sampledValue;
                // Rescale differently according to signal type
                if(type == SignalType.Wifi) rescaledValue = MathUtils.rescaleInInterval(sampledValue, -100, 0, 0, 1);
                else if(type == SignalType.UMTS) rescaledValue = MathUtils.rescaleInInterval(sampledValue, 0, 31, 0, 1);
                else if(type == SignalType.LTE) rescaledValue = MathUtils.rescaleInInterval(sampledValue, 0, 97, 0, 1);
                // Scale alpha according do number of samples
                int alpha = 100 + Math.min(SAMPLES_FOR_MAX_INTENSITY*10, (int)samplesCount * 10);
                int color = MathUtils.interpolateColors(Color.RED, Color.GREEN, alpha, rescaledValue);

                MapsDrawUtilities.drawSquare(mMap, latLngQuadrant, quadrantsDistance, color);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        /* Perform initialization */
        this.currentMap = googleMap;
        this.currentMap.getUiSettings().setZoomControlsEnabled(true);
        this.currentMap.getUiSettings().setZoomGesturesEnabled(true);
        this.currentMap.setMyLocationEnabled(true);
        this.setSignalType(SignalType.Wifi);
    }
}
