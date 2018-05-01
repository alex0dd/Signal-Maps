package it.unibo.alexpod.lam_project_signal_maps.fragments;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

import it.unibo.alexpod.lam_project_signal_maps.R;
import it.unibo.alexpod.lam_project_signal_maps.utils.MapsDrawUtilities;
import it.unibo.alexpod.lam_project_signal_maps.singletons.GoogleMapsSingleton;
import it.unibo.alexpod.lam_project_signal_maps.utils.MathUtils;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private double quadrantsDistance = 10.1; //10.1 meters

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.maps_fragment, container, false);
        SupportMapFragment mMapView = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mMapView.getMapAsync(this);
        return view;
    }

    private HashMap<LatLng, Float> getMockData(int type){
        HashMap<LatLng, Float> mapPoints = new HashMap<>();
        // Add mock points
        LatLng initialMockPosition = new LatLng(37.4, -122.1);
        mapPoints.put(initialMockPosition, 94.0f);
        mapPoints.put(new LatLng(initialMockPosition.latitude+quadrantsDistance, initialMockPosition.longitude), 184.5f);
        mapPoints.put(new LatLng(initialMockPosition.latitude, initialMockPosition.longitude+quadrantsDistance), 153.1f);
        mapPoints.put(new LatLng(initialMockPosition.latitude+quadrantsDistance, initialMockPosition.longitude+quadrantsDistance), 66.1f);
        return mapPoints;
    }

    /*
    * type: 0: Wifi, 1: UMTS, 2: LTE
    * */
    public void setSignalType(int type){
        /*
        * TODO: implement proper signal type enum/class
        * */
        GoogleMap mMap = GoogleMapsSingleton.getMap();
        if(mMap != null){
            // reset points on the map
            mMap.clear();
            // get data from a persistent store(mock for now)
            HashMap<LatLng, Float> mapPoints = getMockData(type);
            // Draw squares on map
            for(Map.Entry<LatLng, Float> point : mapPoints.entrySet()){
                LatLng latLngQuadrant = point.getKey();
                Float sampledValue = point.getValue();
                // TODO: do proper rescaling
                float rescaledValue = MathUtils.rescaleInInterval(sampledValue, 0, 100, 0, 1);

                int color = MathUtils.interpolateColors(Color.RED, Color.GREEN, rescaledValue);

                MapsDrawUtilities.drawSquare(GoogleMapsSingleton.getMap(), latLngQuadrant, quadrantsDistance, color);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        /* Perform initialization */
        // Using singleton as there is some bug with maps instance being null outside of this method
        // even after it's been assigned
        GoogleMapsSingleton.setMap(googleMap);
        GoogleMap mMap = GoogleMapsSingleton.getMap();
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setMyLocationEnabled(true);
        this.setSignalType(0);
    }
}
