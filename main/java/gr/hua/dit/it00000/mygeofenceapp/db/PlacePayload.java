package gr.hua.dit.it00000.mygeofenceapp.db;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.android.gms.maps.model.LatLng;

public class PlacePayload {
    private long id;
    private LatLng latLng;

    public PlacePayload( LatLng latLng ) {
        this.latLng = latLng;
    }
    public PlacePayload( long id, double lat, double lon ) {
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
