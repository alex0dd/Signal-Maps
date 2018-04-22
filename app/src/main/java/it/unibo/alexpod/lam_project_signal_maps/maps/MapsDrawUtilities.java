package it.unibo.alexpod.lam_project_signal_maps.maps;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsDrawUtilities {

    private GoogleMap map;

    public MapsDrawUtilities(GoogleMap map){
        this.map = map;
    }

    public void drawSquare(LatLng initialPoint, double l, int color){
        this.drawSquare(initialPoint.latitude, initialPoint.longitude, l, color);
    }

    public void drawSquare(double x, double y, double l, int color){
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
        Polygon polygon = this.map.addPolygon(rectOptions);

    }

}
