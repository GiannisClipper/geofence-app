package gr.hua.dit.it00000.mygeofenceapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gr.hua.dit.it00000.mygeofenceapp.db.PlacePayload;
import gr.hua.dit.it00000.mygeofenceapp.db.TrackPayload;

public class ThreadHelper {

    // interface used to process the result when setupSession thread finish

    interface SetupSessionOnFinish {
        // base on: Java 8 – How to Pass Lambda as a Parameter
        // https://techndeck.com/java-pass-lambda-as-parameter-to-method-how-to-java8/?utm_content=cmp-true
        void run();
    }

    static void setupSession(
            ContentResolver contentResolver, List<PlacePayload> places, SetupSessionOnFinish setupSessionOnFinish
    ) {

        // setup session
        new Thread(() -> {

            long sessionId;

            ContentValues values = new ContentValues();
            values.put("createdAt", System.currentTimeMillis() + "");
            Uri uri = contentResolver.insert(MapContentProvider.SESSIONS_URI, values);
            sessionId = Long.parseLong(uri.getLastPathSegment());
            Log.d("admin", "session => setup with id " + sessionId);

            for (PlacePayload place : places) {
                values = new ContentValues();
                values.put("lat", place.getLatLng().latitude);
                values.put("lon", place.getLatLng().longitude);
                uri = contentResolver.insert(
                        Uri.withAppendedPath(MapContentProvider.SESSION_PLACES_URI, (sessionId + "")),
                        values
                );
                place.setId(Long.parseLong(uri.getLastPathSegment()));
                Log.d("admin", "place => saved with id " + place.getId());
            }
            setupSessionOnFinish.run();

        }).start();
    }

    // interface used to process the result when restorePlaces thread finish

    public interface RestorePlacesOnFinish {
        // base on: Java 8 – How to Pass Lambda as a Parameter
        // https://techndeck.com/java-pass-lambda-as-parameter-to-method-how-to-java8/?utm_content=cmp-true
        void run( long sessionId, List<PlacePayload> places );
    }

    public static void restorePlaces( ContentResolver contentResolver, RestorePlacesOnFinish restorePlacesOnFinish ) {

        new Thread( () -> {

            long sessionId;
            long createdAt;
            List<PlacePayload> places = new ArrayList<>();

            Cursor cursor = contentResolver.query(
                    MapContentProvider.LAST_SESSION_URI, null, null, null, null
            );
            if (cursor != null && cursor.getCount() > 0 ) {
                Log.d("admin", "Sessions => last");
                cursor.moveToFirst();
                sessionId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("createdAt"));
                Date createdAtAsDate = new Date( createdAt );

                Log.d("admin", "Session => id:" + sessionId );
                Log.d("admin", "Session => createdAt:" + createdAtAsDate );

                cursor.close();

                String[] projection = new String[]{ "id", "lat", "lon" };
                String selection = null;
                String[] selectionArgs = null;
                String sortOrder = "id";

                cursor = contentResolver.query(
                        Uri.withAppendedPath( MapContentProvider.SESSION_PLACES_URI, sessionId + ""),
                        projection, selection, selectionArgs, sortOrder
                );

                if (cursor != null) {
                    Log.d("admin", "Places => count:" + cursor.getCount());

                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        places.add(new PlacePayload(
                                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                                cursor.getDouble(cursor.getColumnIndexOrThrow("lat")),
                                cursor.getDouble(cursor.getColumnIndexOrThrow("lon"))
                        ));
                        cursor.moveToNext();
                    }
                    cursor.close();
                    restorePlacesOnFinish.run(sessionId, places);
                }
            }
        } ).start();
    }

    // interface used to process the result when restoreTracks thread finish

    public interface RestoreTracksOnFinish {
        // base on: Java 8 – How to Pass Lambda as a Parameter
        // https://techndeck.com/java-pass-lambda-as-parameter-to-method-how-to-java8/?utm_content=cmp-true
        void run( long sessionId, List<TrackPayload> tracks );
    }

    public static void restoreTracks( ContentResolver contentResolver, RestoreTracksOnFinish restoreTracksOnFinish ) {

        new Thread( () -> {

            long sessionId;
            List<TrackPayload> tracks = new ArrayList<>();

            Cursor cursor = contentResolver.query(
                    MapContentProvider.LAST_SESSION_URI, null, null, null, null
            );
            if (cursor != null && cursor.getCount() > 0 ) {
                Log.d("admin", "Sessions => last");
                cursor.moveToFirst();
                sessionId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                cursor.close();

                String[] projection = new String[]{ "id", "lat", "lon" };
                String selection = null;
                String[] selectionArgs = null;
                String sortOrder = "id";

                cursor = contentResolver.query(
                        Uri.withAppendedPath( MapContentProvider.SESSION_TRACKS_URI, sessionId + ""),
                        projection, selection, selectionArgs, sortOrder
                );

                if (cursor != null) {
                    Log.d("admin", "Tracks => count:" + cursor.getCount());

                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        tracks.add(new TrackPayload(
                                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                                cursor.getDouble(cursor.getColumnIndexOrThrow("lat")),
                                cursor.getDouble(cursor.getColumnIndexOrThrow("lon"))
                        ));
                        cursor.moveToNext();
                    }
                    cursor.close();
                    restoreTracksOnFinish.run(sessionId, tracks);
                }
            }
        } ).start();
    }

    public static void saveTrack(ContentResolver contentResolver, long sessionId, LatLng latLng ) {
        new Thread( () -> {
            ContentValues values = new ContentValues();
            values.put( "lat", latLng.latitude );
            values.put( "lon", latLng.longitude );
            contentResolver.insert(
                    Uri.withAppendedPath(MapContentProvider.SESSION_TRACKS_URI, (sessionId + "")),
                    values
            );
            Log.d( "admin", "track => saved in session " + sessionId );
        } ).start();
    }
}
