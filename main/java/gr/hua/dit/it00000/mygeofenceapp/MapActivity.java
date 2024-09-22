package gr.hua.dit.it00000.mygeofenceapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gr.hua.dit.it00000.mygeofenceapp.databinding.ActivityMapBinding;
import gr.hua.dit.it00000.mygeofenceapp.db.PlacePayload;
import gr.hua.dit.it00000.mygeofenceapp.db.SessionPayload;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private static final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;

    private GoogleMap mMap;
    private ActivityMapBinding binding;
    private long sessionId;
    private List<PlacePayload> places;
    private Intent serviceIntent;
    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d( "admin", "MapActivity => onCreate()" );
        super.onCreate(savedInstanceState);

        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        places = new ArrayList<>();
        sessionId = -1;

        // to fetch the google map

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // to fetch the places of last session from db and represent on map

        ThreadHelper.restorePlaces(
            getContentResolver(),
            (sessionId, places) -> {
                this.sessionId = sessionId;
                this.places = places;
                MapHelper.addPlacesOnMap( mMap, places, null );
                Log.d("admin", "restorePlaces => " + this.places.size());
            }
        );

        // to set buttons behaviour

        // activate start button in case only of onMapReady()
        // findViewById( R.id.start_button ).setOnClickListener( onStart );
        findViewById( R.id.cancel_button ).setOnClickListener( onCancel );
    }

//    @Override
//    protected void onResume() {
//        Log.d( "admin", "MapActivity => onResume()" );
//        super.onResume();
//
//        // to fetch the places of last session from db and represent on map
//
//        if ( sessionId == -1 ) {
//            ThreadHelper.restorePlaces(
//                getContentResolver(),
//                (sessionId, places) -> {
//                    this.sessionId = sessionId;
//                    this.places = places;
//                    MapHelper.addPlacesOnMap( mMap, places );
//                    Log.d("admin", "restorePlaces => " + this.places.size());
//                }
//            );
//        }
//    }

    @Override
    public void onMapReady( GoogleMap googleMap ) {
        Log.d( "admin", "MapActivity => onMapReady()" );

        SharedPreferences sharedPref = getSharedPreferences( "MyGeoFenceApp", Context.MODE_PRIVATE);

        // to represent the map

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled( true );
        MapHelper.addMapCenter(this, mMap, sharedPref );
        MapHelper.addPlacesOnMap( mMap, places, null );

        // to add/remove places on map

        mMap.setOnMapLongClickListener( new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                Log.d( "admin", "Long Click => " + latLng.toString() );

                if ( placeExists( latLng ) ) {
                    Log.d("admin", "place => remove");
                    int index =  placeExistsAt( latLng );
                    places.remove( index );
                    mMap.clear();
                    MapHelper.addPlacesOnMap( mMap, places, null );

                } else {
                    Log.d("admin", "place => add");
                    places.add( new PlacePayload( latLng ) );
                    MapHelper.addPlace( mMap, latLng, false );
                }
            }

            private boolean placeExists( LatLng latLng ) {
                return placeExistsAt( latLng ) >= 0;
            }

            private int placeExistsAt( LatLng latLng ) {
                // starting from bottom to find the most recently added place
                // supposed that is placed on top of others
                for ( int i = places.size() - 1; i >= 0; i-- ) {
                    if (MapHelper.calcDistance(places.get( i ).getLatLng(), latLng) <= MainActivity.PLACE_RADIUS) {
                        return i;
                    }
                }
                return -1;
            }

        } );

        findViewById( R.id.start_button ).setOnClickListener( onStart );
    }

    // to start tracking service

    private View.OnClickListener onStart = view -> {

        ThreadHelper.setupSession(
            getContentResolver(),
            places,
            () -> {
                Log.d("admin", "TrackService => prepare");

                if (serviceConnection != null) {
                    unbindService(serviceConnection);
                }

                serviceIntent = new Intent(this, TrackService.class);

                serviceConnection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder iBinder) {
                        TrackService.LocalBinder binder = (TrackService.LocalBinder) iBinder;
                        TrackService service = binder.getService();
                        if (service.isTaskRunning()) {
                            service.restartTask();
                        } else {
                            service.startTask();
                        }
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                    }
                };
                bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            }
        );

        Intent intent2 = new Intent( this, MainActivity.class );
        startActivity( intent2 );
    };

    // to ignore changes and return to main screen

    private View.OnClickListener onCancel = view -> {
        Intent intent = new Intent( this, MainActivity.class );
        startActivity( intent );
    };

    @Override
    public void onDestroy() {
        Log.d("admin", "MapActivity => onDestroy()");
        super.onDestroy();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }
}