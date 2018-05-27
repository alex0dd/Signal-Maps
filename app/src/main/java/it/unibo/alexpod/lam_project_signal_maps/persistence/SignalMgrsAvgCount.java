package it.unibo.alexpod.lam_project_signal_maps.persistence;

import android.arch.persistence.room.ColumnInfo;

public class SignalMgrsAvgCount {

    public String mgrs;
    public float avgPower;
    public long samplesCount;
    @ColumnInfo(name = "signal_type")
    public int signalType;

    public SignalMgrsAvgCount(String mgrs, float avgPower, long samplesCount, int signalType){
        this.mgrs = mgrs;
        this.avgPower = avgPower;
        this.samplesCount = samplesCount;
        this.signalType = signalType;
    }

}
