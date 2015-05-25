package com.kostya.cranescale.provider;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

/**
 * Created by Kostya on 24.04.2015.
 */
public class CraneScaleProvider extends ContentProvider {
    private static final String DATABASE_NAME           = "craneScale.db";
    private static final int DATABASE_VERSION           = 1;
    static final String AUTHORITY                       = "com.kostya.cranescale.craneScale";
    private static final String DROP_TABLE_IF_EXISTS    = "DROP TABLE IF EXISTS ";

    private static final int ALL_ROWS           = 1;
    private static final int SINGLE_ROWS        = 2;

    private enum TABLE_LIST{
        WEIGHING_LIST,
        WEIGHING_ID,
        WEIGHT_DOC_LIST,
        WEIGHT_DOC_ID
    }

    private static final UriMatcher uriMatcher;
    private SQLiteDatabase db;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, WeighingDbAdapter.TABLE, TABLE_LIST.WEIGHING_LIST.ordinal());
        uriMatcher.addURI(AUTHORITY, WeighingDbAdapter.TABLE + "/#", TABLE_LIST.WEIGHING_ID.ordinal());
        uriMatcher.addURI(AUTHORITY, WeightDocDbAdapter.TABLE, TABLE_LIST.WEIGHT_DOC_LIST.ordinal());
        uriMatcher.addURI(AUTHORITY, WeightDocDbAdapter.TABLE + "/#", TABLE_LIST.WEIGHT_DOC_ID.ordinal());
    }

    private String getTable(Uri uri) {
        switch (TABLE_LIST.values()[uriMatcher.match(uri)]) {
            case WEIGHING_LIST:
            case WEIGHING_ID:
                return WeighingDbAdapter.TABLE; // return
            case WEIGHT_DOC_LIST:
            case WEIGHT_DOC_ID:
                return WeightDocDbAdapter.TABLE; // return
            /** PROVIDE A DEFAULT CASE HERE **/
            default:
                // If the URI doesn't match any of the known patterns, throw an exception.
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        DBHelper dbHelper = new DBHelper(getContext());
        //db = dbHelper.getWritableDatabase();
        db = dbHelper.getReadableDatabase();
        if (db != null) {
            db.setLockingEnabled(false);
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (TABLE_LIST.values()[uriMatcher.match(uri)]) {
            case WEIGHING_LIST: // общий Uri
                queryBuilder.setTables(WeighingDbAdapter.TABLE);
            break;
            case WEIGHING_ID: // Uri с ID
                queryBuilder.setTables(WeighingDbAdapter.TABLE);
                queryBuilder.appendWhere(BaseColumns._ID + '=' + uri.getLastPathSegment());
            break;
            case WEIGHT_DOC_LIST: // общий Uri
                queryBuilder.setTables(WeightDocDbAdapter.TABLE);
            break;
            case WEIGHT_DOC_ID: // Uri с ID
                queryBuilder.setTables(WeightDocDbAdapter.TABLE);
                queryBuilder.appendWhere(BaseColumns._ID + '=' + uri.getLastPathSegment());
            break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor == null) {
            return null;
        }
        Context context = getContext();
        if (context != null) {
            ContentResolver contentResolver = context.getContentResolver();
            if (contentResolver != null) {
                cursor.setNotificationUri(contentResolver, uri);
            }
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALL_ROWS:
                return "vnd.android.cursor.dir/vnd.";
            case SINGLE_ROWS:
                return "vnd.android.cursor.item/vnd.";
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = db.insert(getTable(uri), null, values);
        if (rowID > 0L) {
            Uri resultUri = ContentUris.withAppendedId(uri, rowID);
            Context context = getContext();
            try {
                context.getContentResolver().notifyChange(resultUri, null);
                return resultUri;
            }catch (Exception e){
                throw new SQLiteException(e.getMessage());
            }
        }
        throw new SQLiteException("Ошибка добавления записи " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArg) {
        int rowId;
        String id;
        switch (TABLE_LIST.values()[uriMatcher.match(uri)]) {
            case WEIGHING_LIST: // общий Uri
                rowId = db.delete(WeighingDbAdapter.TABLE, where, whereArg);
            break;
            case WEIGHING_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(where)) {
                    where = BaseColumns._ID + " = " + id;
                } else {
                    where = where + " AND " + BaseColumns._ID + " = " + id;
                }
                rowId = db.delete(WeighingDbAdapter.TABLE, where, whereArg);
            break;
            case WEIGHT_DOC_LIST: // общий Uri
                rowId = db.delete(WeightDocDbAdapter.TABLE, where, whereArg);
            break;
            case WEIGHT_DOC_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(where)) {
                    where = BaseColumns._ID + " = " + id;
                } else {
                    where = where + " AND " + BaseColumns._ID + " = " + id;
                }
                rowId = db.delete(WeightDocDbAdapter.TABLE, where, whereArg);
            break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db.execSQL("VACUUM");
        if (rowId > 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }

        return rowId;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int updateCount;
        String id;
        switch (TABLE_LIST.values()[uriMatcher.match(uri)]) {
            case WEIGHING_LIST: // общий Uri
                updateCount = db.update(WeighingDbAdapter.TABLE, values, selection, selectionArgs);
            break;
            case WEIGHING_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = BaseColumns._ID + " = " + id;
                } else {
                    selection = selection + " AND " + BaseColumns._ID + " = " + id;
                }
                updateCount = db.update(WeighingDbAdapter.TABLE, values, selection, selectionArgs);
            break;
            case WEIGHT_DOC_LIST: // общий Uri
                updateCount = db.update(WeightDocDbAdapter.TABLE, values, selection, selectionArgs);
                break;
            case WEIGHT_DOC_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = BaseColumns._ID + " = " + id;
                } else {
                    selection = selection + " AND " + BaseColumns._ID + " = " + id;
                }
                updateCount = db.update(WeightDocDbAdapter.TABLE, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        if (updateCount > 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }

        return updateCount;
    }

    private class DBHelper extends SQLiteOpenHelper {

        DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(WeighingDbAdapter.CREATE_TABLE);
            db.execSQL(WeightDocDbAdapter.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL(DROP_TABLE_IF_EXISTS + WeighingDbAdapter.TABLE);
            db.execSQL(DROP_TABLE_IF_EXISTS + WeightDocDbAdapter.TABLE);
            onCreate(db);
        }
    }
}
