package gr.hua.dit.it00000.mygeofenceapp.db;

import static androidx.room.ForeignKey.CASCADE;

import android.database.Cursor;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@androidx.room.Dao
public interface PlaceDao {
    @Insert
    void insert( PlaceModel model );

    @Query( "INSERT INTO places (sessionId, lat, lon) VALUES(:sessionId, :lat, :lon)" )
    long insertOne( long sessionId, double lat, double lon );

    @Query( "DELETE FROM places WHERE rowid=:id" )
    int deleteOne( long id );

    @Query( "DELETE FROM places" )
    void deleteAll();
    @Query( "SELECT * FROM places ORDER BY rowid ASC" )
    Cursor getAll();

    @Query( "SELECT * FROM places WHERE sessionId=:sessionId ORDER BY rowid ASC" )
    Cursor getMany( long sessionId );
}
