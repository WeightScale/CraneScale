package com.kostya.cranescale.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Kostya on 24.04.2015.
 * Таблица для хранения результатов взвешивания
 */
public class WeighingDbAdapter {
    private final Context mContext;
    private final ContentResolver contentResolver;

    public static final String TABLE                = "weighing";
    public static final String KEY_ID               = BaseColumns._ID;
    public static final String KEY_DOC_ID           = "docId";
    public static final String KEY_DATE             = "date";
    public static final String KEY_TIME             = "time";
    public static final String KEY_WEIGHT           = "weight";

    private static final String[] All_COLUMN_TABLE = {KEY_ID, KEY_DOC_ID, KEY_DATE, KEY_TIME, KEY_WEIGHT};

    public static final String CREATE_TABLE = "create table "
            + TABLE + " ("
            + KEY_ID + " integer primary key autoincrement, "
            + KEY_DOC_ID + " integer, "
            + KEY_DATE + " text,"
            + KEY_TIME + " text,"
            + KEY_WEIGHT + " integer );";

    private static final Uri CONTENT_URI = Uri.parse("content://" + CraneScaleProvider.AUTHORITY + '/' + TABLE);


    public WeighingDbAdapter(Context context) {
        mContext = context;
        contentResolver = mContext.getContentResolver();
    }

    public Cursor getAllEntries(int docId) {
        return contentResolver.query(CONTENT_URI, All_COLUMN_TABLE, KEY_DOC_ID + "= " + docId, null, null);
    }

    public Uri insertNewEntry(int weight) {
        ContentValues newTaskValues = new ContentValues();
        Date date = new Date();
        newTaskValues.put(KEY_DATE, new SimpleDateFormat("dd.MM.yyyy").format(date));
        newTaskValues.put(KEY_TIME, new SimpleDateFormat("HH:mm:ss").format(date));
        newTaskValues.put(KEY_WEIGHT, 0);
        return contentResolver.insert(CONTENT_URI, newTaskValues);
    }

    public Uri insertNewEntry(Date date, int weight) {
        ContentValues newTaskValues = new ContentValues();
        newTaskValues.put(KEY_DATE, new SimpleDateFormat("dd.MM.yyyy").format(date));
        newTaskValues.put(KEY_TIME, new SimpleDateFormat("HH:mm:ss").format(date));
        newTaskValues.put(KEY_WEIGHT, 0);
        return contentResolver.insert(CONTENT_URI, newTaskValues);
    }

    public Cursor getEntryItem(int _rowIndex) {
        try {
            Uri uri = ContentUris.withAppendedId(CONTENT_URI, _rowIndex);
            Cursor result = contentResolver.query(uri, All_COLUMN_TABLE, null, null, null);
            if (result.getCount() == 0 || !result.moveToFirst()) {
                return null;
            }
            return result;
        }catch (Exception e){
            return null;
        }
    }

    int removeEntry(int _rowIndex) throws Exception{
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, _rowIndex);
        return contentResolver.delete(uri, null, null);
    }

    int getWeight(int _rowIndex) throws Exception{
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, _rowIndex);
        int weight;
        Cursor result = contentResolver.query(uri, new String[]{KEY_ID, KEY_WEIGHT}, null, null, null);
        result.moveToFirst();
        weight = result.getInt(result.getColumnIndex(KEY_WEIGHT));
        result.close();
        return weight;
    }
}
