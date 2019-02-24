package com.example.workhours.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.workhours.data.ShiftsContract.ShiftEntry;

/**
 * {@link ContentProvider} for Pets app.
 */
public class ShiftsProvider extends ContentProvider {

    private ShiftsDbHelper mDbHelper;

    private static final int SHIFTS = 100;
    private static final int SHIFT_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(ShiftsContract.CONTENT_AUTHORITY, ShiftsContract.PATH_SHIFTS, SHIFTS);
        sUriMatcher.addURI(ShiftsContract.CONTENT_AUTHORITY, ShiftsContract.PATH_SHIFTS + "/#", SHIFT_ID);
    }
    /** Tag for the log messages */
    public static final String LOG_TAG = ShiftsProvider.class.getSimpleName();

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new ShiftsDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case SHIFTS:
                cursor = database.query(ShiftEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case SHIFT_ID:
                selection = ShiftEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ShiftEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("@string/message_wrong_uri" + uri);
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {                                           // TODO dodělat validaci input dat z contentValues
        final int match = sUriMatcher.match(uri);
        switch (match){
            case SHIFTS:
                return insertShift(uri, contentValues);
            default:
                throw new IllegalArgumentException("@string/message_query_not_supported" + uri);
        }
    }

    private Uri insertShift(Uri uri, ContentValues values){
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(ShiftEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "@string/message_query_failed" + uri);
        }
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }
}