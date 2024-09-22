package gr.hua.dit.it00000.mygeofenceapp.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// based on https://www.simplifiedcoding.net/android-room-database-example/

@Database( entities =
    {
        SessionModel.class,
        PlaceModel.class,
        TrackModel.class
    },
    version = 1
)
public abstract class MyDatabase extends RoomDatabase {
    public abstract SessionDao sessionDao();
    public abstract PlaceDao placeDao();
    public abstract TrackDao trackDao();
}
