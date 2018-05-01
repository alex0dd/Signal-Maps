package it.unibo.alexpod.lam_project_signal_maps.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "signal_samples")
public class SignalSample {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String mgrs;
    public long datetime;
    public float signal;
    @ColumnInfo(name = "signal_type")
    public int signalType; // type: 0: Wifi, 1: UMTS, 2: LTE

    public SignalSample(@NonNull String mgrs, long datetime, float signal, int signalType){
        this.mgrs = mgrs;
        this.datetime = datetime;
        this.signal = signal;
        this.signalType = signalType;
    }
}
