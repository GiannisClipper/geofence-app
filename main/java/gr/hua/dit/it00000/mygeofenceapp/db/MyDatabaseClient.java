package gr.hua.dit.it00000.mygeofenceapp.db;

import android.content.Context;
import androidx.room.Room;

// using single instance of database, based on:
// Android Room Database Example – Building a Todo app
// https://www.simplifiedcoding.net/android-room-database-example

public class MyDatabaseClient {

    private Context mCtx;
    private static MyDatabaseClient mInstance;

    private MyDatabase myDatabase;

    private MyDatabaseClient(Context mCtx) {
        this.mCtx = mCtx;
        myDatabase = Room.databaseBuilder(mCtx, MyDatabase.class, "my_database" ).build();
    }

    public static synchronized MyDatabaseClient getInstance( Context mCtx ) {
        if ( mInstance == null ) {
            mInstance = new MyDatabaseClient( mCtx );
        }
        return mInstance;
    }

    public MyDatabase getMyDatabase() {
        return myDatabase;
    }
}