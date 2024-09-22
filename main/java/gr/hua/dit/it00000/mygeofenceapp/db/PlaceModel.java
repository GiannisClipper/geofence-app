package gr.hua.dit.it00000.mygeofenceapp.db;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity( tableName = "places",
    foreignKeys = @ForeignKey(
        entity = SessionModel.class,
        parentColumns = "id",
        childColumns = "sessionId",
        onDelete = CASCADE
    )
)
public class PlaceModel {
    @PrimaryKey( autoGenerate = true )
    private long id;
    @NonNull
    private long sessionId;
    @NonNull
    private double lat;
    @NonNull
    private double lon;
    public PlaceModel( float lat, float lon) {
        this.lat = lat;
        this.lon = lon;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getSessionId() {
        return sessionId;
    }
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }
    public double getLat() {
        return lat;
    }
    public void setLat(float lat) {
        this.lat = lat;
    }
    public double getLon() {
        return lon;
    }
    public void setLon(float lon) {
        this.lon = lon;
    }
}
