package com.xysy.ybs.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;

import com.xysy.ybs.tools.Logger;

public class DataProvider extends ContentProvider {
    public static final String AUTHORITY = "com.xysy.ybs.provider";
    public static final String SCHEME = "content://";

    public static final String PATH_JOBS = "/jobs";
    public static final Uri JOBS_URI = Uri.parse(SCHEME + AUTHORITY + PATH_JOBS);

    private static final int JOBS = 0;
    public static final String JOBS_CONTENT_TYPE = "and.android.cursor.dir/com.xysy.ybs.jobs";
    private static final UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final Object DBLock = new Object();

    static {
        sMatcher.addURI(AUTHORITY, "jobs", JOBS);
    }

    private static DataHelper mDBHelper;

    @Override
    public boolean onCreate() {
        mDBHelper = new DataHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (sMatcher.match(uri)) {
            case JOBS:
                return JOBS_CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown Uri " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String where, String[] whereArgs,
                        String sortOrder) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor result;
        switch (sMatcher.match(uri)) {
            case JOBS:
                result = db.query(DataHelper.JobsContract.TABLE_NAME, projection, where,
                        whereArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri " + uri);
        }
        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Uri result = null;
        db.beginTransaction();
        try {
            switch (sMatcher.match(uri)) {
                case JOBS:
                    long rawId = db.insertOrThrow(DataHelper.JobsContract.TABLE_NAME,null, values);
                    db.setTransactionSuccessful();
                    if (rawId > 0) {
                        result = ContentUris.withAppendedId(uri, rawId);
                    }

                    break;
                default:
                    throw new IllegalArgumentException("Unknown Uri " + uri);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int result;
        db.beginTransaction();
        try {
            switch (sMatcher.match(uri)) {
                case JOBS:
                    result = db.delete(DataHelper.JobsContract.TABLE_NAME,
                            where, whereArgs);
                    db.setTransactionSuccessful();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Uri" + uri);
            }
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return result;

    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int result;
        db.beginTransaction();
        try {
            switch (sMatcher.match(uri)) {
                case JOBS:
                    result = db.update(DataHelper.JobsContract.TABLE_NAME,
                            values, where, whereArgs);
                    db.setTransactionSuccessful();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Uri " + uri);
            }
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }
}
