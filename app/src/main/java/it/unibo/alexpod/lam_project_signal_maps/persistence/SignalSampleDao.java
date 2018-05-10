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

    @Query("SELECT signal_samples.mgrs, AVG(signal_samples.signal) as avgPower, COUNT(signal_samples.id) as samplesCount " +
            "FROM signal_samples " +
            "WHERE signal_type = :signalType " +
            "GROUP BY signal_samples.mgrs")
    List<SignalMgrsAvgCount> getAllSamplesAndCountPerZone(int signalType);

    @Query("SELECT * FROM signal_samples WHERE signal_type = :signalType ORDER BY datetime DESC LIMIT 1")
    SignalSample getLastSample(int signalType);

    @Insert
    void insertAll(SignalSample... signalSamples);

    @Insert
    void insert(SignalSample signalSample);

    @Delete
    void delete(SignalSample signalSample);
}
