package gr.hua.dit.it00000.mygeofenceapp.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity( tableName = "sessions" )
public class SessionModel {
    @PrimaryKey( autoGenerate = true )
    private long id;
    @NonNull
    private long createdAt;
    public SessionModel(long createdAt) {
        this.createdAt = createdAt;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
