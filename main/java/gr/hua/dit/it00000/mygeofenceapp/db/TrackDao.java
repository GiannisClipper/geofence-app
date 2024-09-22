package gr.hua.dit.it00000.mygeofenceapp.db;

import android.database.Cursor;

import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@androidx.room.Dao
public interface TrackDao {
    @Insert
    void insert( TrackModel model );

    @Query( "INSERT INTO tracks (sessionId, lat, lon) VALUES(:sessionId, :lat, :lon)" )
    long insertOne( long sessionId, double lat, double lon );

    @Query( "DELETE FROM tracks" )
    void deleteAll();
    @Query( "SELECT * FROM tracks ORDER BY rowid ASC" )
    Cursor getAll();

    @Query( "SELECT * FROM tracks WHERE sessionId=:sessionId ORDER BY rowid ASC" )
    Cursor getMany( long sessionId );
}
