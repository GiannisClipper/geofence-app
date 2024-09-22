package gr.hua.dit.it00000.mygeofenceapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import gr.hua.dit.it00000.mygeofenceapp.db.PlacePayload;
import gr.hua.dit.it00000.mygeofenceapp.db.TrackPayload;

//public class ResultsMapActivity extends AppCompatActivity {
public class ResultsMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private long sessionId;
    private List<PlacePayload> places;
    private List<TrackPayload> tracks;
    private TrackService service;
    private Intent serviceIntent;
    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("admin", "ResultsMapActivity => onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_results);

        sessionId = -1;
        places = new ArrayList<>();
        tracks = new ArrayList<>();

        // to fetch the google map

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.resultsMap);
        mapFragment.getMapAsync(this);

        // to fetch the places from db and represent on map

        ThreadHelper.restorePlaces(
                getContentResolver(),
                (sessionId, places) -> {
                    this.sessionId = sessionId;
                    this.places = places;
                    MapHelper.addPlacesOnMap(mMap, places, null );
                    Log.d("admin", "restorePlaces => " + this.places.size());
                }
        );

        // to fetch the tracks from db and represent on map

        ThreadHelper.restoreTracks(
                getContentResolver(),
                (sessionId, tracks) -> {
                    this.sessionId = sessionId;
                    this.tracks = tracks;
                    MapHelper.addTracksOnMap(mMap, tracks);
                    Log.d("admin", "restoreTracks => " + this.tracks.size());
                }
        );

        // to connect to tracking service

        serviceIntent = new Intent(this, TrackService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder iBinder) {
                TrackService.LocalBinder binder = (TrackService.LocalBinder) iBinder;
                service = binder.getService();

                setPauseResumeButtonText();
                findViewById(R.id.pause_resume_button).setOnClickListener(pauseResume);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // to set buttons behaviour

        // activate pause/resume button onServiceConnected()
        // findViewById( R.id.pause_resume_button ).setOnClickListener( pauseResume );
        findViewById(R.id.back_button).setOnClickListener(goMain);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("admin", "ResultsMapActivity => onMapReady()");

        SharedPreferences sharedPref = getSharedPreferences( "MyGeoFenceApp", Context.MODE_PRIVATE);

        // to represent results on map

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        MapHelper.addMapCenter(this, mMap, sharedPref );

        String currentLat = sharedPref.getString("currentLat", null );
        String currentLon = sharedPref.getString("currentLon", null );
        Log.d( "admin","currentLat:" + currentLat );
        Log.d( "admin","currentLon:" + currentLon );

        LatLng latLng = new LatLng(
            Float.parseFloat( currentLat ),
            Float.parseFloat( currentLon )
        );
        Log.d( "admin","current*:" + latLng );

        MapHelper.addPlacesOnMap(mMap, places, latLng );
        MapHelper.addTracksOnMap( mMap, tracks );
    }

    // to set the proper pause/resume button label

    private void setPauseResumeButtonText() {
        Button btn = findViewById(R.id.pause_resume_button);
        if (service.isTaskRunning()) {
            btn.setText("Pause");
        } else if (service.isTaskPaused()) {
            btn.setText("Resume");
        } else {
            btn.setText("(stopped)");
        }
    }

    // to pause/resume tracking service

    private View.OnClickListener pauseResume = view -> {

        if ( service.isTaskRunning() ) {
            service.pauseTask();

        } else if ( service.isTaskPaused() ) {
            service.resumeTask();
        }
        setPauseResumeButtonText();

    };

    // to return to main screen

    View.OnClickListener goMain = view -> {
        Intent intent = new Intent( this, MainActivity.class );
        startActivity( intent );
    };

    @Override
    public void onDestroy() {
        Log.d("admin", "ResultsMapActivity => onDestroy()");
        super.onDestroy();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }
}