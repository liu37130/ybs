package com.xysy.ybs.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ybs.db";

    public DataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(JobsContract.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(JobsContract.DROP_TABLE);
        onCreate(db);
    }

    public final class JobsContract {
        public static final String TABLE_NAME = "jobs";

        public static final String FAVORITE_JOB = "favorite";
        public static final String NORMAL_JOB   = "normal";

        public static final String ID         = "_id";
        public static final String JOB_TITLE  = "job_title";
        public static final String COMPANY    = "company";
        public static final String CITY       = "city";
        public static final String SALARY     = "salary";
        public static final String URL        = "url";
        public static final String AVATAR_URL = "avatar_url";
        public static final String DATE       = "date";
        public static final String STAR_STATE = "isfavorite";

        private static final String CREATE_TABLE = "create table " + TABLE_NAME + "( "
                + ID + " integer primary key autoincrement,"
                + JOB_TITLE + " text,"
                + COMPANY + " text,"
                + CITY + " text,"
                + SALARY + " text,"
                + URL + " text not null unique,"
                + AVATAR_URL + " text,"
                + DATE + " text,"
                + STAR_STATE + " text);";

        private static final String DROP_TABLE = "drop table if exists " + TABLE_NAME;
    }
}
