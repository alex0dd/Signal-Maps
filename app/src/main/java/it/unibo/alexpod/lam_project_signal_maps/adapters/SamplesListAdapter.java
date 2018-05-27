package it.unibo.alexpod.lam_project_signal_maps.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import it.unibo.alexpod.lam_project_signal_maps.R;
import it.unibo.alexpod.lam_project_signal_maps.enums.SignalType;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalSample;

public class SamplesListAdapter extends RecyclerView.Adapter<SamplesListAdapter.GenericSampleViewHolder> {

    private List<SignalSample> signalSamples = new ArrayList<>();
    private Context context;


    public SamplesListAdapter(List<SignalSample> samples, Context context){
        this.signalSamples = samples;
        this.context = context;
    }

    public void setData(List<SignalSample> samples){
        this.signalSamples = samples;
    }

    @Override
    public SamplesListAdapter.GenericSampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View genericEntryView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.samples_list_generic_entry_view, parent, false);
        return new GenericSampleViewHolder(genericEntryView);
    }

    @Override
    public void onBindViewHolder(SamplesListAdapter.GenericSampleViewHolder holder, int position) {
        SignalSample sample=signalSamples.get(position);
        showGenericSampleDetails(holder, sample);

    }

    @Override
    public int getItemCount() {
        return signalSamples.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    private void showGenericSampleDetails(GenericSampleViewHolder holder, SignalSample sample){

        String signalTypeString;
        String formattedDatetime = DateFormat.getDateTimeInstance().format(sample.datetime);
        if(sample.signalType == SignalType.Wifi.getValue()){
            signalTypeString = context.getString(R.string.wifi_title);
        }
        else if(sample.signalType == SignalType.UMTS.getValue()){
            signalTypeString = context.getString(R.string.umts_title);
        }
        else if(sample.signalType == SignalType.LTE.getValue()){
            signalTypeString = context.getString(R.string.lte_title);
        }
        else{
            signalTypeString = context.getString(R.string.unknown_title);
        }
        /*
        * Fill the TextViews
        * */
        holder.zoneTxt.setText(Html.fromHtml(context.getString(R.string.zone_sample_list_entry_text, sample.mgrs)));
        holder.powerTxt.setText(Html.fromHtml(context.getString(R.string.power_sample_list_entry_text, sample.signal)));
        holder.signalTypeTxt.setText(Html.fromHtml(context.getString(R.string.type_sample_list_entry_text, signalTypeString)));
        holder.datetimeTxt.setText(Html.fromHtml(context.getString(R.string.datetime_sample_list_entry_text, formattedDatetime)));
    }


    static class GenericSampleViewHolder extends RecyclerView.ViewHolder{

        TextView zoneTxt;
        TextView powerTxt;
        TextView signalTypeTxt;
        TextView datetimeTxt;

        GenericSampleViewHolder(View itemView) {
            super(itemView);
            this.zoneTxt = itemView.findViewById(R.id.zoneTxt);
            this.powerTxt = itemView.findViewById(R.id.powerTxt);
            this.signalTypeTxt = itemView.findViewById(R.id.signalTypeTxt);
            this.datetimeTxt = itemView.findViewById(R.id.datetimeTxt);
        }
    }

}
