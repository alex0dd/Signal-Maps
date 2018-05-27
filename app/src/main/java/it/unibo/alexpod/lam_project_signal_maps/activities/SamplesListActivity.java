package it.unibo.alexpod.lam_project_signal_maps.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.unibo.alexpod.lam_project_signal_maps.R;
import it.unibo.alexpod.lam_project_signal_maps.adapters.SamplesListAdapter;
import it.unibo.alexpod.lam_project_signal_maps.custom.RecyclerViewEmptySupport;
import it.unibo.alexpod.lam_project_signal_maps.enums.SignalType;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalRepository;
import it.unibo.alexpod.lam_project_signal_maps.persistence.SignalSample;

public class SamplesListActivity extends AppCompatActivity {

    private RecyclerViewEmptySupport mRecyclerView;
    private SamplesListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<SignalSample> signalSamples = new ArrayList<>();
    private SignalRepository signalRepository;
    private SignalType signalType = SignalType.ANY;
    private String zone = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_samples_list);
        // get extras
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            if(extras.containsKey("signalType")) signalType = SignalType.values()[extras.getInt("signalType")];
            if(extras.containsKey("zoneMgrs")) zone = extras.getString("zoneMgrs");
        }

        mRecyclerView = findViewById(R.id.itemsRecyclerView);
        signalRepository = new SignalRepository(getApplication());

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new SamplesListAdapter(signalSamples, getApplicationContext());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setEmptyView(findViewById(R.id.listEmptyTxt));

        // initialize the data
        initializeData();

    }

    private void initializeData(){
        LoadSamplesAsyncTask loadSamplesAsyncTask = new LoadSamplesAsyncTask(signalRepository, mAdapter, signalType);
        loadSamplesAsyncTask.execute(zone);
    }

    private static class LoadSamplesAsyncTask extends AsyncTask<String, Void, List<SignalSample>>{

        private SignalRepository repository;
        private SamplesListAdapter adapter;
        private SignalType signalType;

        public LoadSamplesAsyncTask(SignalRepository repository, SamplesListAdapter adapter, SignalType signalType){
            this.repository = repository;
            this.adapter = adapter;
            this.signalType = signalType;
        }

        @Override
        protected List<SignalSample> doInBackground(String... zones) {
            List<SignalSample> result;
            if (this.signalType != SignalType.ANY) {
                // if the search was done for a specific zone
                if(zones != null) result = repository.getAllSamplesInZone(zones[0], this.signalType);
                else result = repository.getAllSamplesOfType(this.signalType);

            }
            // if type is ANY(then get all the samples of any type)
            else result = repository.getAllSamples();
            return result;
        }

        @Override
        protected void onPostExecute(List<SignalSample> signalSamples) {
            super.onPostExecute(signalSamples);
            this.adapter.setData(signalSamples);
            this.adapter.notifyDataSetChanged();
        }
    }

}
