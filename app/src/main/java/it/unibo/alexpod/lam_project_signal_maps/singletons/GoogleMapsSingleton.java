package it.unibo.alexpod.lam_project_signal_maps.singletons;

import com.google.android.gms.maps.GoogleMap;

public class GoogleMapsSingleton {
    private static final GoogleMapsSingleton ourInstance = new GoogleMapsSingleton();
    private static GoogleMap map = null;

    public static GoogleMapsSingleton getInstance() {
        return ourInstance;
    }

    public static GoogleMap getMap() {
        return map;
    }

    public static void setMap(GoogleMap newMap){
        if(map == null){
            map = newMap;
        }
    }

    private GoogleMapsSingleton() {}
}
