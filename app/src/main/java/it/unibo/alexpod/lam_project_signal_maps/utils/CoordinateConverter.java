package it.unibo.alexpod.lam_project_signal_maps.utils;

import com.berico.coords.Coordinates;
import com.google.android.gms.maps.model.LatLng;

public class CoordinateConverter {

    public static String LatLngToMgrs(LatLng coordinate){
        return Coordinates.mgrsFromLatLon(coordinate.latitude, coordinate.longitude);
    }

    public static LatLng MgrsToLatLng(String mgrs){
        double[] latlon = Coordinates.latLonFromMgrs(mgrs);
        return new LatLng(latlon[0], latlon[1]);
    }

    public static String LatLngToMgrsQuadrant(LatLng coordinate){
        String position = Coordinates.mgrsFromLatLon(coordinate.latitude, coordinate.longitude);
        StringBuilder outPosition = new StringBuilder();
        String[] components = position.split(" ");
        // Manually join as we don't wanna use a MinSDK 26 method
        outPosition.append(components[0]);
        outPosition.append(" ");
        outPosition.append(components[1].substring(0, 4));
        outPosition.append(" ");
        outPosition.append(components[2].substring(0, 4));
        // Return string
        return outPosition.toString();
    }

}
