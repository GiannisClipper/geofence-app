package gr.hua.dit.it00000.mygeofenceapp.db;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public class SessionPayload {
    private long id;
    private long createdAt;
    public SessionPayload() {}
    public SessionPayload( long id) {
        this.id = id;
    }
    public SessionPayload(long id, long createdAth ) {
        this.id = id;
        this.createdAt = createdAt;
    }
    public long getId() {
        return id;
    }
    public void setId( long id ) {
        this.id = id;
    }
    public long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt( long createdAt ) {
        this.createdAt = createdAt;
    }
    @NonNull
    public String toString() {
        return "[ id: " + getId() + ", createdAt: " + getCreatedAt() + " ]";
    }
}
