package it.unibo.alexpod.lam_project_signal_maps.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import it.unibo.alexpod.lam_project_signal_maps.R;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalMgrsAvgCount;

public class SignalInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{

    private Context context;

    public SignalInfoWindowAdapter(Context context){
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.signal_info_window, null);
        TextView quadrantTxt = view.findViewById(R.id.quadrant);
        TextView samplesTxt = view.findViewById(R.id.samples);
        TextView strengthTxt = view.findViewById(R.id.strength);

        SignalMgrsAvgCount infoWindowData = (SignalMgrsAvgCount) marker.getTag();
        quadrantTxt.setText(infoWindowData.mgrs);
        samplesTxt.setText("Samples: "+infoWindowData.samplesCount);
        strengthTxt.setText("Avg. Strength: "+infoWindowData.avgPower);
        return view;
    }
}
