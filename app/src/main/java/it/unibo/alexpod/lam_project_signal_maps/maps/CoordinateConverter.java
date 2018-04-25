package it.unibo.alexpod.lam_project_signal_maps.maps;

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

}
