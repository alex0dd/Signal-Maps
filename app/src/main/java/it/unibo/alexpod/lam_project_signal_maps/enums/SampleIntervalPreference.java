package it.unibo.alexpod.lam_project_signal_maps.enums;

public enum SampleIntervalPreference {
    ALWAYS(0, 0),
    ONE_MINUTE(1, 60000),
    THIRTY_MINUTES(2, 1800000),
    ONE_HOUR(3, 3600000),
    ONE_DAY(4, 86400000);

    private int value;
    private long intervalInMilliseconds;
    SampleIntervalPreference(int value, long intervalInMilliseconds){
        this.value = value;
        this.intervalInMilliseconds = intervalInMilliseconds;
    }
    public int getValue(){
        return this.value;
    }

    public long getIntervalMs(){
        return this.intervalInMilliseconds;
    }
}
