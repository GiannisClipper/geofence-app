package gr.hua.dit.it00000.mygeofenceapp.db;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public class TrackPayload {
    private long id;
    private LatLng latLng;

    public TrackPayload(LatLng latLng ) {
        this.latLng = latLng;
    }
    public TrackPayload(long id, double lat, double lon ) {
        LatLng latLng = new LatLng( lat, lon );
        this.id = id;
        this.latLng = latLng;
    }
    public long getId() {
        return id;
    }
    public void setId( long id ) {
        this.id = id;
    }
    public LatLng getLatLng() {
        return latLng;
    }
    public void setLatLng( LatLng latLng ) {
        this.latLng = latLng;
    }
    @NonNull
    public String toString() {
        return "[ id: " + getId() +
            ", lat: " + getLatLng().latitude +
            ", lon: " + getLatLng().longitude + " ]";
    }
}
