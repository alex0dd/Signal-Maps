package it.unibo.alexpod.lam_project_signal_maps.maps;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class MapsDrawUtilities {

    public static void drawSquare(GoogleMap map, LatLng initialPoint, double l, int color){
        drawSquare(map, initialPoint.latitude, initialPoint.longitude, l, color);
    }

    public static void drawSquare(GoogleMap map, double lat, double lon, double l, int color){
        // 1m in degree = 0.0089 / 1000 = 0.0000089
        double coef = l * 0.0000089;

        double new_lat = lat + coef;
        // pi / 180 = 0.018
        double new_lon = lon + coef / Math.cos(new_lat * 0.018);

        // Instantiates a new Polygon object and adds points to define a rectangle
        PolygonOptions rectOptions = new PolygonOptions()
                .add(new LatLng(lat, lon),
                        new LatLng(new_lat, lon),
                        new LatLng(new_lat, new_lon),
                        new LatLng(lat, new_lon),
                        new LatLng(lat, lon))
                .strokeColor(Color.TRANSPARENT)
                .fillColor(color);
        // Get back the mutable Polygon
        Polygon polygon = map.addPolygon(rectOptions);

    }

}
