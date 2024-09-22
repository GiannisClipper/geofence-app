package gr.hua.dit.it00000.mygeofenceapp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import gr.hua.dit.it00000.mygeofenceapp.db.PlaceModel;
import gr.hua.dit.it00000.mygeofenceapp.db.TrackModel;
import gr.hua.dit.it00000.mygeofenceapp.db.MyDatabaseClient;

public class MapContentProvider extends ContentProvider {

    public static final String AUTHORITIES = "gr.hua.dit.it00000.mygeofenceapp.contentprovider";

    public static final Uri SESSIONS_URI = Uri.parse("content://" + AUTHORITIES + "/sessions");
    public static final Uri ONE_SESSION_URI = Uri.parse("content://" + AUTHORITIES + "/session/#");
    public static final Uri LAST_SESSION_URI = Uri.parse("content://" + AUTHORITIES + "/session/last");

    public static final Uri SESSION_PLACES_URI = Uri.parse("content://" + AUTHORITIES + "/places/#");
    public static final Uri ONE_PLACE_URI = Uri.parse("content://" + AUTHORITIES + "/place/#");

    public static final Uri SESSION_TRACKS_URI = Uri.parse("content://" + AUTHORITIES + "/tracks/#");

    private static final int SESSIONS_PATH = 11;
    private static final int ONE_SESSION_PATH = 12;
    private static final int LAST_SESSION_PATH = 13;

    private static final int SESSION_PLACES_PATH = 21;
    private static final int ONE_PLACE_PATH = 22;

    private static final int SESSION_TRACKS_PATH = 31;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITIES, "sessions", SESSIONS_PATH);
        uriMatcher.addURI(AUTHORITIES, "session/#", ONE_SESSION_PATH);
        uriMatcher.addURI(AUTHORITIES, "session/last", LAST_SESSION_PATH);

        uriMatcher.addURI(AUTHORITIES, "places/#", SESSION_PLACES_PATH);
        uriMatcher.addURI(AUTHORITIES, "place/#", ONE_PLACE_PATH);

        uriMatcher.addURI(AUTHORITIES, "tracks/#", SESSION_TRACKS_PATH);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public String getType(Uri uri) {

        switch (uriMatcher.match(uri)) {

            case SESSIONS_PATH:
                return "vnd.android.cursor.dir/vnd." + AUTHORITIES + "/sessions";
            case ONE_SESSION_PATH:
                return "vnd.android.cursor.dir/vnd." + AUTHORITIES + "/session/#";

            case SESSION_PLACES_PATH:
                return "vnd.android.cursor.dir/vnd." + AUTHORITIES + "/places/#";
            case ONE_PLACE_PATH:
                return "vnd.android.cursor.dir/vnd." + AUTHORITIES + "/place/#";

            case SESSION_TRACKS_PATH:
                return "vnd.android.cursor.dir/vnd." + AUTHORITIES + "/tracks/#";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        long sessionId, id;
        double lat, lon;

        switch (uriMatcher.match(uri)) {

            case SESSIONS_PATH:
                Long createdAt = Long.parseLong(values.get("createdAt").toString());
                id = MyDatabaseClient
                        .getInstance(getContext())
                        .getMyDatabase()
                        .sessionDao()
                        .insertOne( createdAt );

                return Uri.parse("content://" + AUTHORITIES + "/session/" + id );

            case SESSION_PLACES_PATH:
                sessionId = Long.parseLong( uri.getLastPathSegment() );
                lat = Double.parseDouble(values.get("lat").toString());
                lon = Double.parseDouble(values.get("lon").toString());
                id = MyDatabaseClient
                .getInstance(getContext())
                .getMyDatabase()
                .placeDao()
                .insertOne( sessionId, lat, lon );

                return Uri.parse("content://" + AUTHORITIES + "/place/" + id );

            case SESSION_TRACKS_PATH:
                sessionId = Long.parseLong( uri.getLastPathSegment() );
                lat = Double.parseDouble(values.get("lat").toString());
                lon = Double.parseDouble(values.get("lon").toString());
                TrackModel track = new TrackModel(lat, lon);
                id = MyDatabaseClient
                .getInstance(getContext())
                .getMyDatabase()
                .trackDao()
                .insertOne( sessionId, lat, lon );

                return Uri.parse("content://" + AUTHORITIES + "/track/" + id );

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Cursor query(
            Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder
    ) {

        long sessionId;

        switch (uriMatcher.match(uri)) {

            case LAST_SESSION_PATH:
                return MyDatabaseClient
                    .getInstance(getContext())
                    .getMyDatabase()
                    .sessionDao()
                    .getLast();

            case SESSION_PLACES_PATH:
                sessionId = Long.parseLong( uri.getLastPathSegment() );
                return MyDatabaseClient
                    .getInstance(getContext())
                    .getMyDatabase()
                    .placeDao()
                    .getMany( sessionId );

            case SESSION_TRACKS_PATH:
                sessionId = Long.parseLong( uri.getLastPathSegment() );
                return MyDatabaseClient
                    .getInstance(getContext())
                    .getMyDatabase()
                    .trackDao()
                    .getMany( sessionId );

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        switch (uriMatcher.match(uri)) {

            case ONE_PLACE_PATH:
                Log.d("admin", "getPath:" + uri.getPath() );
                Log.d("admin", "getLastPathSegment:" + uri.getLastPathSegment() );
                int id = Integer.parseInt( uri.getLastPathSegment() );
                int numberOfDeletedRows = MyDatabaseClient
                        .getInstance(getContext())
                        .getMyDatabase()
                        .placeDao()
                        .deleteOne( id );
                return numberOfDeletedRows;

//            case PLACES_PATH:
//                MyDatabaseClient
//                        .getInstance(getContext())
//                        .getMyDatabase()
//                        .placeDao()
//                        .deleteAll();
//                return 0;
//
//            case TRACKS_PATH:
//                MyDatabaseClient
//                        .getInstance(getContext())
//                        .getMyDatabase()
//                        .trackDao()
//                        .deleteAll();
//                return 0;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
}

