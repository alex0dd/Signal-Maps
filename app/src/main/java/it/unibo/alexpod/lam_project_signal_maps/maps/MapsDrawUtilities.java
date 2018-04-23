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

    public static void drawSquare(GoogleMap map, double x, double y, double l, int color){
        // Instantiates a new Polygon object and adds points to define a rectangle
        PolygonOptions rectOptions = new PolygonOptions()
                .add(new LatLng(x, y),
                        new LatLng(x+l, y),
                        new LatLng(x+l, y+l),
                        new LatLng(x, y+l),
                        new LatLng(x, y))
                .strokeColor(Color.TRANSPARENT)
                .fillColor(color);

        // Get back the mutable Polygon
        Polygon polygon = map.addPolygon(rectOptions);

    }

}
