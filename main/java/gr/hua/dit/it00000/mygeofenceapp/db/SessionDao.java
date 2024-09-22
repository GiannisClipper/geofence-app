package gr.hua.dit.it00000.mygeofenceapp.db;

import android.database.Cursor;

import androidx.room.Insert;
import androidx.room.Query;

@androidx.room.Dao
public interface SessionDao {
    @Insert
    void insert( SessionModel model );
    @Query( "INSERT INTO sessions (createdAt) VALUES(:createdAt)" )
    long insertOne( long createdAt );
    @Query( "DELETE FROM places WHERE rowid=:id" )
    int deleteOne( long id );
    @Query( "DELETE FROM sessions" )
    void deleteAll();
    @Query( "SELECT * FROM sessions ORDER BY rowid ASC" )
    Cursor getAll();

    @Query( "SELECT * FROM sessions ORDER BY rowid DESC LIMIT 1" )
    Cursor getLast();
}
