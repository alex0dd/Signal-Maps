package it.unibo.alexpod.lam_project_signal_maps.enums;

/**
 * Created by alexpod on 08/05/18.
 */

public enum SignalType {
    Wifi(0),
    UMTS(1),
    LTE(2);

    private int value;

    SignalType(int value){
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }
}
