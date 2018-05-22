package it.unibo.alexpod.lam_project_signal_maps.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(version = 1, entities = {SignalSample.class})
public abstract class SignalDatabase extends RoomDatabase {

    public abstract SignalSampleDao getSignalSampleDao();

    private static SignalDatabase instance;

    public static SignalDatabase getInstance(Context context){
        if (instance == null) {
            synchronized (SignalDatabase.class) {
                if (instance == null) {
                    //instance = Room.databaseBuilder(context,
                    //        SignalDatabase.class, "signal_database")
                    //        .build();
                    // TODO: remove this, use real database
                    instance = Room.inMemoryDatabaseBuilder(context, SignalDatabase.class).build();
                }
            }
        }
        return instance;
    }

}
