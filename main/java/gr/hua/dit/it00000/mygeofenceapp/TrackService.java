package gr.hua.dit.it00000.mygeofenceapp;

import static android.os.SystemClock.sleep;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import gr.hua.dit.it00000.mygeofenceapp.db.PlacePayload;

public class TrackService extends Service {

    class LocalBinder extends Binder {
        TrackService getService() {
            return TrackService.this;
        }
    }

    private IBinder binder = new LocalBinder();

    private LocationManager locationManager;
    private LocationListener locationListener;

    private BroadcastReceiver receiver;
    private IntentFilter filter;

    public enum Status {STARTED, PAUSED, RESUMED, STOPPED}
    private Status status;
    private long sessionId;
    private List<PlacePayload> places;
    private boolean alreadyInsidePlace;

    public TrackService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("admin", "TrackService => onBind()");

        return binder;
    }

    @Override
    public void onCreate() {
        Log.d("admin", "TrackService => onCreate()");
        super.onCreate();

        // to listen location changing

        // locationManager provides access to the systems location services
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        this.locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.d("admin", "locationListener => " + location.toString());
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                // to check the entry in places

                if (!alreadyInsidePlace) {
                    for (int i = places.size() - 1; i >= 0; i--) {
                        if (MapHelper.calcDistance(places.get(i).getLatLng(), latLng) <= MainActivity.PLACE_RADIUS) {
                            ThreadHelper.saveTrack(getContentResolver(), sessionId, latLng);
                            alreadyInsidePlace = true;
                            Log.d("admin", "track => inside place");
                            return;
                        }
                    }
                    Log.d("admin", "track => out of places");

                    // to check the exit of places

                } else {
                    for (int i = places.size() - 1; i >= 0; i--) {
                        if (MapHelper.calcDistance(places.get(i).getLatLng(), latLng) <= MainActivity.PLACE_RADIUS) {
                            Log.d("admin", "track => inside place");
                            return;
                        }
                    }
                    ThreadHelper.saveTrack(getContentResolver(), sessionId, latLng);
                    alreadyInsidePlace = false;
                    Log.d("admin", "track => out of places");
                }
            }
        };

        // to check the connection to internet and pause/resume the service accordingly

        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (TrackService.this.isNetworkAvailable(context)) {
                    Log.d("admin", "TrackService => connection exists...");
                    if (TrackService.this.status == Status.PAUSED) {
                        TrackService.this.resumeTask();
                    }

                } else {
                    Log.d("admin", "TrackService => connection not exists...");
                    if (TrackService.this.status == Status.STARTED || TrackService.this.status == Status.RESUMED) {
                        TrackService.this.pauseTask();
                    }
                }
            }

        };

        this.filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        // filter.setPriority( 999 );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("admin", "TrackService => onStartCommand()");

        // notification is based on: How to implementing start Foreground for a service?
        // https://www.tutorialspoint.com/how-to-implementing-start-foreground-for-a-service
        String NOTIFICATION_CHANNEL_ID = "gr.hua.dit.it00000.mygeofenceapp";
        String channelName = "Track Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.YELLOW);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new Notification.Builder(this,NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("My geofence app")
                .setContentIntent(pendingIntent).build();
        startForeground(1337, notification);
        return START_STICKY;
    }

    // isNetworkAvailable() is based on: NetworkInfo has been deprecated by API 29 [duplicate]
    // https://stackoverflow.com/questions/57284582/networkinfo-has-been-deprecated-by-api-29
    private Boolean isNetworkAvailable( Context context ) {

        ConnectivityManager connectivityManager = ( ConnectivityManager ) context.getSystemService( Context.CONNECTIVITY_SERVICE );

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {

            Network net = connectivityManager.getActiveNetwork();
            if ( net == null ) {
                return false;
            }
            NetworkCapabilities netCapabilities = connectivityManager.getNetworkCapabilities( net );

            return netCapabilities != null && (
                    netCapabilities.hasTransport( NetworkCapabilities.TRANSPORT_WIFI ) ||
                    netCapabilities.hasTransport( NetworkCapabilities.TRANSPORT_CELLULAR ) ||
                    netCapabilities.hasTransport( NetworkCapabilities.TRANSPORT_ETHERNET ) ||
                    netCapabilities.hasTransport( NetworkCapabilities.TRANSPORT_BLUETOOTH )
            );

        } else {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        }
    }

    private void setStatus( Status status ) {
        Log.d( "admin", "status=" + status );
        this.status = status;
        Intent intent = new Intent("local-broadcast-track-service-status" );
        intent.putExtra("message", status.name() );
        LocalBroadcastManager.getInstance(TrackService.this ).sendBroadcast( intent );
        Log.d( "admin", "sendBroadcast=" + status );
    }

    // to enable (and config) the location requesting

    private void requestLocationUpdates() {
        if ( ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MainActivity.TRACK_DELAY,
                MainActivity.TRACK_DISTANCE,
                locationListener
            );
        } else {
            Log.d( "admin", "no permission to allow trackService.requestLocationUpdates()" );
        }
    }

    // to handle the service task (start,pause,resume,stop,restart)

    public void startTask() {

        this.setStatus( Status.STARTED );
        ThreadHelper.restorePlaces(
            getContentResolver(),
            (sessionId, places) -> {
                this.sessionId = sessionId;
                this.places = places;
                this.alreadyInsidePlace = false;
                Log.d( "admin", "restorePlaces => " + this.places.size() );
            }
        );

        this.registerReceiver( this.receiver, filter );

        if ( ! this.isNetworkAvailable( this ) ) {
            this.setStatus( Status.PAUSED );
            return;
        }
        this.requestLocationUpdates();
    }

    public void pauseTask() {

        this.setStatus( Status.PAUSED );
        this.locationManager.removeUpdates( locationListener );
    }

    public void resumeTask() {

        this.setStatus( Status.RESUMED );
        this.requestLocationUpdates();
    }

    public void stopTask() {
        this.setStatus( Status.STOPPED );
        this.locationManager.removeUpdates(this.locationListener);
        try {
            this.unregisterReceiver(this.receiver);
        } catch(IllegalArgumentException e) {
            // unregistered in case of closing app without start tracking
            Log.d("admin", "TrackService => receiver not registered");
        }
    }

    public void restartTask() {
        this.stopTask();
        this.startTask();
    }

    public boolean isTaskRunning() {
        return
            this.status == Status.STARTED ||
            this.status == Status.RESUMED;
    }

    public boolean isTaskPaused() {
        return this.status == Status.PAUSED;
    }

    @Override
    public void onDestroy() {
        Log.d("admin", "TrackService => onDestroy()");
        this.stopTask();
        super.onDestroy();
    }
}