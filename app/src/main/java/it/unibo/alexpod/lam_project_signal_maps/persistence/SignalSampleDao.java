package it.unibo.alexpod.lam_project_signal_maps.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SignalSampleDao {

    @Query("SELECT * FROM signal_samples WHERE signal_type = :signalType")
    List<SignalSample> getAllSamples(int signalType);

    @Insert
    void insertAll(SignalSample... signalSamples);

    @Insert
    void insert(SignalSample signalSample);

    @Delete
    void delete(SignalSample signalSample);

}
