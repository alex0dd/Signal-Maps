package it.unibo.alexpod.lam_project_signal_maps.fragments;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import it.unibo.alexpod.lam_project_signal_maps.R;
import it.unibo.alexpod.lam_project_signal_maps.maps.MapsDrawUtilities;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap = null;
    private MapsDrawUtilities mapsDrawUtilities = null;
    private HashMap<LatLng, Float> mapPoints;
    private double quadrantsDistance = 1.0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.maps_fragment, container, false);
        SupportMapFragment mMapView = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mMapView.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        /* Perform initialization */
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mapsDrawUtilities = new MapsDrawUtilities(mMap);
        mapPoints = new HashMap<>();

        // Add mock points
        LatLng initialMockPosition = new LatLng(37.4, -122.1);
        mapPoints.put(initialMockPosition, 94.0f);
        mapPoints.put(new LatLng(initialMockPosition.latitude+quadrantsDistance, initialMockPosition.longitude), 184.5f);
        mapPoints.put(new LatLng(initialMockPosition.latitude, initialMockPosition.longitude+quadrantsDistance), 153.1f);
        mapPoints.put(new LatLng(initialMockPosition.latitude+quadrantsDistance, initialMockPosition.longitude+quadrantsDistance), 66.1f);

        // Draw squares on map
        for(Map.Entry<LatLng, Float> point : mapPoints.entrySet()){
            mapsDrawUtilities.drawSquare(point.getKey(), quadrantsDistance, Color.argb(point.getValue().intValue(), 255, 0, 0));
        }
        // Move camera towards the first point
        LatLng cameraPoint = mapPoints.keySet().iterator().next();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraPoint, 0.1f));
    }
}
