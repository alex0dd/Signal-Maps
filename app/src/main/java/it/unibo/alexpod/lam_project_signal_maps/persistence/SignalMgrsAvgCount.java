package it.unibo.alexpod.lam_project_signal_maps.persistence;

public class SignalMgrsAvgCount {

    public String mgrs;
    public float avgPower;
    public long samplesCount;

    public SignalMgrsAvgCount(String mgrs, float avgPower, long samplesCount){
        this.mgrs = mgrs;
        this.avgPower = avgPower;
        this.samplesCount = samplesCount;
    }

}
