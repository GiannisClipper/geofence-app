package gr.hua.dit.it00000.mygeofenceapp;

import static android.app.PendingIntent.getActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity {

    static final LatLng START_POINT = new LatLng( 37.9622, 23.7008 );
    // start point = hua-dit
    static final String START_TITLE = "HUA-DIT";
    static final int START_ZOOM = 16;
    static final int PLACE_RADIUS = 100; //meters
    static final int TRACK_DELAY = 1500; //milliseconds
    static final int TRACK_DISTANCE = 50; //meters
    static final int REQUEST_FINE_LOCATION = 16; // random value

    static boolean hasPermission( Context context ) {
        return ActivityCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }
    static void askPermission( Activity activity ) {
        if ( hasPermission( activity ) ) {
            Log.d( "admin", "permission => HAS" );
            return;
        }
        Log.d( "admin", "permission => HAS NOT" );
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_FINE_LOCATION);
    }

    private Intent serviceIntent;
    private ServiceConnection serviceConnection;
    private BroadcastReceiver statusBroadcastReceiver;

    private String serviceStatus;

    private void setStopButtonLabel( String status ) {
        Button btn = findViewById(R.id.stop_button);
        if (status == null || status.equals("STOPPED")) {
            btn.setText("(no active tracking)");
        } else {
            btn.setText("Stop tracking");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("admin", "MainActivity => onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // to ask for permissions
        MainActivity.askPermission( this );

        // to setup the track service
        serviceIntent = new Intent( this, TrackService.class);
        //this.startService( serviceIntent );
        this.startForegroundService( serviceIntent );

        // to setup the track-service-status broadcastReceiver
        // based on: https://www.geeksforgeeks.org/how-to-use-localbroadcastmanager-in-android/
        statusBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String status = intent.getStringExtra("message");
                Log.d("admin", "onReceive status:" + status);
                setStopButtonLabel( status );
                SharedPreferences sharedPref = getSharedPreferences( "MyGeoFenceApp", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("serviceStatus", status);
                editor.apply();
            }
        };

        // to register the broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            statusBroadcastReceiver,
            new IntentFilter("local-broadcast-track-service-status" )
        );

        // option to set places and start

        findViewById( R.id.set_button ).setOnClickListener( view -> {
            // explicitly call of other activity:
            // Intent intent = new Intent( this, MapActivity.class );
            // implicitly call of other activity:
            Intent intent = new Intent();
            intent.setAction( "gr.hua.dit.it00000.mygeofenceapp.MAP_ACTIVITY" );
            startActivity( intent );
            //finish();
        } );

        // option to stop running

        findViewById( R.id.stop_button ).setOnClickListener( view -> {

            if (serviceConnection != null) {
                unbindService(serviceConnection);
            }
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder iBinder ) {
                    TrackService.LocalBinder binder = ( TrackService.LocalBinder ) iBinder;
                    TrackService service = binder.getService();
                    if ( service.isTaskRunning() || service.isTaskPaused() ) {
                        service.stopTask();
                        return;
                    }
                    Log.d("admin", "Service => not running...");
                }
                @Override
                public void onServiceDisconnected(ComponentName name) {}
            };
            bindService( serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE );

        } );

        // option to pause/resume and view results

        findViewById( R.id.view_button ).setOnClickListener( view -> {
            // explicitly call of other activity:
            // Intent intent = new Intent( this, ResultsMapActivity.class );
            // implicitly call of other activity:
            Intent intent = new Intent();
            intent.setAction( "gr.hua.dit.it00000.mygeofenceapp.RESULTS_MAP_ACTIVITY" );
            startActivity( intent );
        } );
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = getSharedPreferences( "MyGeoFenceApp", Context.MODE_PRIVATE);
        String status = sharedPref.getString("serviceStatus", null );
        setStopButtonLabel( status );
    }

    @Override
    public void onDestroy() {
        Log.d("admin", "MainActivity => onDestroy()");
        this.stopService( serviceIntent );
        super.onDestroy();
        // commented here due to possible exception
        // android.app.ServiceConnectionLeaked... MapActivity has leaked ServiceConnection
        // if ( serviceConnection != null ) {
        //     unbindService( serviceConnection );
        // }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ( requestCode == this.REQUEST_FINE_LOCATION ) {
            for ( int i = 0; i< permissions.length; i++ ) {
                if ( permissions[ i ] == Manifest.permission.ACCESS_FINE_LOCATION ) {
                    if ( grantResults[ i ] == PackageManager.PERMISSION_GRANTED ) {
                        if ( MainActivity.hasPermission( this ) ) {
                            //locationManager.request... //get location
                            Log.d( "admin", "permission => ok" );
                        }
                    }
                }
            }
        }
    }
}