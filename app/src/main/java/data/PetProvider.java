package data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import data.PetContract.PetEntry;

public class PetProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();
    private PetDBHelper mPetDBHelper;

    private static final int PETS = 100;
    private static final int PET_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    @Override
    public boolean onCreate() {
        mPetDBHelper = new PetDBHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mPetDBHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);

        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {
        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name==null || name.isEmpty()){
            throw new IllegalArgumentException("Pet requires a name");
        }
        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !isAValidGender(gender)){
            throw  new IllegalArgumentException("Pet requires a valid gender");
        }
        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if(weight != null && weight<0){
            throw new IllegalArgumentException("Pet requires a valid weight");
        }

        SQLiteDatabase database = mPetDBHelper.getWritableDatabase();
        long id = database.insert(PetEntry.TABLE_NAME, null , values);

        if(id == -1)
            return null;

        getContext().getContentResolver().notifyChange(uri, null);
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    public static boolean isAValidGender(int gender){
        if(gender == PetEntry.GENDER_UNKNOWN || gender == PetEntry.GENDER_FEMALE || gender == PetEntry.GENDER_MALE)
            return true;
        else
            return false;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, values, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
    }
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        if(values.containsKey(PetEntry.COLUMN_PET_NAME)){
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name==null || name.isEmpty()){
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        if(values.containsKey(PetEntry.COLUMN_PET_GENDER)){
            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !isAValidGender(gender)){
                throw  new IllegalArgumentException("Pet requires a valid gender");
            }
        }

        if(values.containsKey(PetEntry.COLUMN_PET_WEIGHT)){
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if(weight != null && weight<0){
                throw new IllegalArgumentException("Pet requires a valid weight");
            }
        }

        // Updates the selected pets in the pets database table with the given ContentValues
        SQLiteDatabase database = mPetDBHelper.getWritableDatabase();
        int modifiedRows = database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);

        getContext().getContentResolver().notifyChange(uri, null);

        // Return the number of rows that were affected
        return modifiedRows;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mPetDBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int deleted;
        switch (match){
            case PETS:
                deleted = database.delete(PetEntry.TABLE_NAME, selection,selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return deleted;
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                deleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                return deleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_iTEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    }


