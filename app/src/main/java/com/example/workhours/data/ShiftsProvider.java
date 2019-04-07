package com.example.workhours.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import com.example.workhours.data.ShiftsContract.ShiftEntry;


@SuppressWarnings({"WeakerAccess", "unused"})
public class ShiftsProvider extends ContentProvider {

    private ShiftsDbHelper mDbHelper;

    private static final int SHIFTS = 100;
    private static final int SHIFT_ID = 101;
    private static final int MONTHS = 200;
    private static final int MONTHS_ID = 201;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(ShiftsContract.CONTENT_AUTHORITY, ShiftsContract.PATH_SHIFTS, SHIFTS);
        sUriMatcher.addURI(ShiftsContract.CONTENT_AUTHORITY, ShiftsContract.PATH_SHIFTS + "/#", SHIFT_ID);
        sUriMatcher.addURI(ShiftsContract.CONTENT_AUTHORITY, ShiftsContract.PATH_MONTHS, MONTHS);
        sUriMatcher.addURI(ShiftsContract.CONTENT_AUTHORITY, ShiftsContract.PATH_MONTHS + "/#", MONTHS_ID);
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
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
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
            case MONTHS:
                cursor = database.query(ShiftEntry.TABLE_MONTHS_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MONTHS_ID:
                selection = ShiftEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ShiftEntry.TABLE_MONTHS_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException( "Cannot query unknown URI " + uri);
        }
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {                                           // TODO dodělat validaci input dat z contentValues

        final int match = sUriMatcher.match(uri);
        switch (match){
            case SHIFTS:
                return insertShift(uri, contentValues);
            case MONTHS:
                return insertMonth(uri, contentValues);
            default:
                throw new IllegalArgumentException( "Insertion is not supported for " + uri);
        }
    }

    private Uri insertShift(Uri uri, ContentValues values){
        // data check
        int date =  values.getAsInteger(ShiftEntry.COLUMN_DATE);
        if (date == 0){
            throw new IllegalArgumentException( "Date needed.");
        }
        int arrival = values.getAsInteger(ShiftEntry.COLUMN_ARRIVAL);
        if ((arrival > 1439) || (arrival == 0)){
            throw new IllegalArgumentException( "Arrival time invalid.");
        }
        int departure = values.getAsInteger(ShiftEntry.COLUMN_DEPARTURE);
        if (departure > 1439){
            throw new IllegalArgumentException( "Departure time invalid.");
        }

        int holiday = values.getAsInteger(ShiftEntry.COLUMN_HOLIDAY);
        if (!ShiftEntry.isValidHoliday(holiday)){
            throw new IllegalArgumentException( "Holiday selection invalid.");
        }

        if (arrival > departure && holiday != 4){
            throw new IllegalArgumentException( "Arrival time must be before departure.");
        }
        int breakLength = values.getAsInteger(ShiftEntry.COLUMN_BREAK_LENGTH);
        if ((breakLength > 1439) || (breakLength == 0)){
            throw new IllegalArgumentException( "Break length invalid.");
        }

        //data insertion into the database

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(ShiftEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
        }
        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertMonth(Uri uri, ContentValues values){
        //TODO data check

        int date =  values.getAsInteger(ShiftEntry.COLUMN_DATE);
        if (date == 0){
            throw new IllegalArgumentException( "Date needed.");
        }

        //data insertion into the database

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(ShiftEntry.TABLE_MONTHS_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
        }
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SHIFTS:
                return updateShift(uri, contentValues, selection, selectionArgs);
            case SHIFT_ID:
                selection = ShiftEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateShift(uri, contentValues, selection, selectionArgs);
            case MONTHS:
                return updateMonth(uri, contentValues, selection, selectionArgs);
            case MONTHS_ID:
                selection = ShiftEntry._ID_MONTHS + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateMonth(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }


    public int updateShift (@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs ){
        // data check
        int date =  values.getAsInteger(ShiftEntry.COLUMN_DATE);
        if (date == 0){
            throw new IllegalArgumentException( "Date needed.");
        }
        int arrival = values.getAsInteger(ShiftEntry.COLUMN_ARRIVAL);
        if ((arrival > 1439) || (arrival == 0)){
            throw new IllegalArgumentException( "Arrival time invalid.");
        }
        int departure = values.getAsInteger(ShiftEntry.COLUMN_DEPARTURE);
        if ((departure > 1439) || (departure == 0)){
            throw new IllegalArgumentException( "Departure time invalid.");
        }
        if (arrival > departure){
            throw new IllegalArgumentException( "Arrival time must be before departure.");
        }
        int breakLength = values.getAsInteger(ShiftEntry.COLUMN_BREAK_LENGTH);
        if ((breakLength > 1439) || (breakLength == 0)){
            throw new IllegalArgumentException( "Break length invalid.");
        }
        int holiday = values.getAsInteger(ShiftEntry.COLUMN_HOLIDAY);
        if (!ShiftEntry.isValidHoliday(holiday)){
            throw new IllegalArgumentException( "Holiday selection invalid.");
        }
        if (values.size() == 0){
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        return database.update(ShiftEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    public int updateMonth (@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs ){
        // data check
        /*int date =  values.getAsInteger(ShiftEntry.COLUMN_DATE);
        if (date == 0){
            throw new IllegalArgumentException( "Date needed.");
        }*/
        if (values.size() == 0){
            return 0;
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        return database.update(ShiftEntry.TABLE_MONTHS_NAME, values, selection, selectionArgs);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SHIFTS:
                return database.delete(ShiftEntry.TABLE_NAME, selection, selectionArgs);
            case SHIFT_ID:
                selection = ShiftEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return database.delete(ShiftEntry.TABLE_NAME, selection, selectionArgs);
            case MONTHS:
                return database.delete(ShiftEntry.TABLE_MONTHS_NAME, selection, selectionArgs);
            case MONTHS_ID:
                selection = ShiftEntry._ID_MONTHS + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return database.delete(ShiftEntry.TABLE_MONTHS_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SHIFTS:
                return ShiftEntry.CONTENT_LIST_TYPE;
            case SHIFT_ID:
                return ShiftEntry.CONTENT_ITEM_TYPE;
            case MONTHS:
                return ShiftEntry.CONTENT_LIST_TYPE_MONTHS;
            case MONTHS_ID:
                return ShiftEntry.CONTENT_ITEM_TYPE_MONTHS;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}