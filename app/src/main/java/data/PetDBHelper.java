package data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import data.PetContract.PetEntry;

public class PetDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "shelter.db";

    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + PetContract.PetEntry.TABLE_NAME + " ("
            + PetContract.PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PetContract.PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "
            + PetContract.PetEntry.COLUMN_PET_BREED + " TEXT, "
            + PetContract.PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, "
            + PetContract.PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";

    public PetDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("catalog.class: ",SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
