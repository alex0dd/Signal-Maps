package it.unibo.alexpod.lam_project_signal_maps.persistence;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import it.unibo.alexpod.lam_project_signal_maps.enums.SignalType;

public class SignalRepository {

    SignalSampleDao signalSampleDao;

    public SignalRepository(Application application){
        SignalDatabase dbInstance = SignalDatabase.getInstance(application);
        signalSampleDao = dbInstance.getSignalSampleDao();
    }

    public List<SignalSample> getAllSamples(){
        return signalSampleDao.getAllSamples();
    }

    public List<SignalSample> getAllSamplesOfType(SignalType type){
        return signalSampleDao.getAllSamples(type.getValue());
    }

    public List<SignalSample> getAllSamplesInZone( String mgrs, SignalType type){
        return signalSampleDao.getAllSamplesInZone(type.getValue(), mgrs);
    }

    public List<SignalMgrsAvgCount> getAllSamplesAndCountPerZone(SignalType type){
        return signalSampleDao.getAllSamplesAndCountPerZone(type.getValue());
    }

    public List<SignalMgrsAvgCount> getAllSamplesAndCountInZone(String mgrs, SignalType type){
        return signalSampleDao.getAllSamplesAndCountInZone(type.getValue(), mgrs);
    }

    public SignalSample getLastSample(SignalType type){
        return signalSampleDao.getLastSample(type.getValue());
    }

    public void insertSample(SignalSample sample){
        new InsertSampleAsyncTask(signalSampleDao).execute(sample);
    }

    public void insertSamples(SignalSample... samples){
        new InsertSampleAsyncTask(signalSampleDao).execute(samples);
    }

    class InsertSampleAsyncTask extends AsyncTask<SignalSample, Void, Void> {

        private SignalSampleDao signalSampleDao;

        public InsertSampleAsyncTask(SignalSampleDao signalSampleDao){
            this.signalSampleDao = signalSampleDao;
        }

        @Override
        protected Void doInBackground(SignalSample... signalSamples) {
            this.signalSampleDao.insertAll(signalSamples);
            return null;
        }
    }

}
