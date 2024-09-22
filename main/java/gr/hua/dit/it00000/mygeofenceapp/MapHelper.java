package gr.hua.dit.it00000.mygeofenceapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.util.List;

import gr.hua.dit.it00000.mygeofenceapp.db.PlacePayload;
import gr.hua.dit.it00000.mygeofenceapp.db.TrackPayload;

public class MapHelper {

    public static double calcDistance(LatLng latLng1, LatLng latLng2 ) {

        // based on: Calculate Distance Between Two Coordinates in Java
        // https://www.baeldung.com/java-find-distance-between-points
        final double EARTH_RADIUS = 6371e3;
        double lat1Rad = Math.toRadians( latLng1.latitude );
        double lat2Rad = Math.toRadians( latLng2.latitude );
        double lon1Rad = Math.toRadians( latLng1.longitude );
        double lon2Rad = Math.toRadians( latLng2.longitude );

        double x = ( lon2Rad - lon1Rad ) * Math.cos( ( lat1Rad + lat2Rad ) / 2 );
        double y = ( lat2Rad - lat1Rad );
        double distance = Math.sqrt( x * x + y * y ) * EARTH_RADIUS;

        return distance;
    }

    public static void addMapCenter( Activity activity, GoogleMap mMap, SharedPreferences sharedPref ) {
        // based on: Select Current Place and Show Details on a Map
        // https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(activity, task -> {
                if (task.isSuccessful() && task.getResult() != null ) {
                    LatLng latLng = new LatLng(
                            task.getResult().getLatitude(),
                            task.getResult().getLongitude()
                    );
                    Log.d("admin", "currentLocation => " + latLng.toString());
                    focusOnMap( mMap, latLng );

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("currentLat", "" + latLng.latitude );
                    editor.putString("currentLon", "" + latLng.longitude );
                    editor.apply();

                } else {
                    Log.d("admin", "currentLocation => not successful");
                    focusOnMap( mMap );
                }
            } );
        } else {
            Log.d("admin", "currentLocation => no permissions");
        }
    }

    public static void focusOnMap(GoogleMap mMap, LatLng latLng ) {
        addStartMarker( mMap, latLng, MainActivity.START_TITLE );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MainActivity.START_ZOOM));
    }
    public static void focusOnMap(GoogleMap mMap) {
        focusOnMap( mMap, MainActivity.START_POINT );
    }

    public static void addPlacesOnMap(GoogleMap mMap, List<PlacePayload> places, LatLng currentLatLng ) {
        if ( mMap == null ) {
            return;
        }

        double minDistance = -1;

        if ( currentLatLng != null ) {
            for (PlacePayload place : places) {
                double temp = calcDistance(place.getLatLng(), currentLatLng);
                if (minDistance == -1 || minDistance > temp) {
                    minDistance = temp;
                }
                Log.d("admin", "minDistance:" + minDistance);
            }
        }

        for ( PlacePayload place : places ) {
            boolean closer = false;
            if ( currentLatLng != null ) {
                double temp = calcDistance(place.getLatLng(), currentLatLng);
                closer = minDistance == temp ? true : false;
            }
            addPlace( mMap, place.getLatLng(), closer );
        }
    }

    public static void addTracksOnMap(GoogleMap mMap, List<TrackPayload> tracks) {
        if ( mMap == null ) {
            return;
        }
        for ( TrackPayload track : tracks ) {
            addTrack( mMap, track.getLatLng() );
        }
    }

    public static void addPlace( GoogleMap mMap, LatLng latLng, Boolean closer ) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center( latLng );
        circleOptions.radius( MainActivity.PLACE_RADIUS );
        if ( ! closer ) {
            circleOptions.strokeColor(Color.argb(255, 255, 255, 0));
            circleOptions.fillColor(Color.argb(64, 255, 255, 0));
        } else {
            circleOptions.strokeColor(Color.argb(255, 255, 255, 0));
            circleOptions.fillColor(Color.argb(64, 255, 0, 255));
        }
        circleOptions.strokeWidth( 4);
        mMap.addCircle( circleOptions );
    }

    public static void addTrack( GoogleMap mMap, LatLng latLng ) {
        // about marker layouts: https://developers.google.com/maps/documentation/android-sdk/marker#maps_android_markers_custom_marker_color-java
        //MarkerOptions markerOptions = new MarkerOptions().position( latLng );
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon( BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_YELLOW ) );
        markerOptions.position(latLng);
        mMap.addMarker( markerOptions );
    }

    public static void addStartMarker( GoogleMap mMap, LatLng latLng, String title ) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon( BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_RED ) );
        markerOptions.title( title );
        markerOptions.position(latLng);
        markerOptions.rotation( 25.0f );
        mMap.addMarker( markerOptions );
    }
}
