package it.unibo.alexpod.lam_project_signal_maps.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.Button;
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

        TextView quadrantTxt = view.findViewById(R.id.quadrantTxt);
        TextView samplesTxt = view.findViewById(R.id.samplesTxt);
        TextView strengthTxt = view.findViewById(R.id.strengthTxt);
        Button viewAllSamplesBtn = view.findViewById(R.id.viewSamplesBtn);
        final SignalMgrsAvgCount infoWindowData = (SignalMgrsAvgCount) marker.getTag();

        String samplesText = this.context.getString(R.string.samples_number_samples_infowindow_text, infoWindowData.samplesCount);
        String avgPowerText = this.context.getString(R.string.average_power_samples_infowindow_text, infoWindowData.avgPower);

        quadrantTxt.setText(infoWindowData.mgrs);
        samplesTxt.setText(Html.fromHtml(samplesText));
        strengthTxt.setText(Html.fromHtml(avgPowerText));
        viewAllSamplesBtn.setText(R.string.view_samples_infowindow_text);
        return view;
    }
}
