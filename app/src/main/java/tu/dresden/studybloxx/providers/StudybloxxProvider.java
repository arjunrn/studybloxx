package tu.dresden.studybloxx.providers;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import tu.dresden.studybloxx.database.StudybloxxDBHelper;


public class StudybloxxProvider extends ContentProvider {
    private static final UriMatcher mURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String AUTHORITY = "tu.dresden.studybloxx.courseprovider";
    private static final String COURSES_BASE_PATH = "courses";
    public static final Uri COURSE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + COURSES_BASE_PATH);
    private static final String NOTES_BASE_PATH = "notes";
    public static final Uri NOTE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + NOTES_BASE_PATH);
    private static final int COURSES = 0;
    private static final int COURSE_ID = 1;
    private static final int NOTES = 2;
    private static final int NOTE_ID = 3;
    private static final String TAG = "StudybloxxProvider";
    private static final String INSERT_NOTE_STATEMENT = "INSERT INTO " + StudybloxxDBHelper.NOTE_TABLE_NAME + "()";

    static {
        mURIMatcher.addURI(AUTHORITY, COURSES_BASE_PATH, COURSES);
        mURIMatcher.addURI(AUTHORITY, COURSES_BASE_PATH + "/#", COURSE_ID);
        mURIMatcher.addURI(AUTHORITY, NOTES_BASE_PATH, NOTES);
        mURIMatcher.addURI(AUTHORITY, NOTES_BASE_PATH + "/#", NOTE_ID);
    }

    private StudybloxxDBHelper mDB;
    private String mServerAddress;
    private ContentResolver mContentResolver;


    public StudybloxxProvider() {
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (mURIMatcher.match(uri)) {
            case COURSES: {
                SQLiteDatabase db = mDB.getWritableDatabase();
                final int delCount = db.delete(StudybloxxDBHelper.COURSE_TABLE_NAME, selection, selectionArgs);
                db.close();
                return delCount;
            }
            case NOTES: {
                SQLiteDatabase db = mDB.getWritableDatabase();
                final int delCount = db.delete(StudybloxxDBHelper.NOTE_TABLE_NAME, selection, selectionArgs);
                db.close();
                return delCount;
            }
            case COURSE_ID: {
                if (selection != null) {
                    throw new UnsupportedOperationException("Not yet implemented");
                }
                SQLiteDatabase db = mDB.getWritableDatabase();
                final long courseID = ContentUris.parseId(uri);
                final int delCount = db.delete(StudybloxxDBHelper.COURSE_TABLE_NAME, StudybloxxDBHelper.Contract.Course.ID + "=" + courseID, null);
                db.close();
                return delCount;
            }
            case NOTE_ID: {
                if (selection != null) {
                    throw new UnsupportedOperationException("Not yet implemented");
                }
                SQLiteDatabase db = mDB.getWritableDatabase();
                final long noteID = ContentUris.parseId(uri);
                final int delCount = db.delete(StudybloxxDBHelper.NOTE_TABLE_NAME, StudybloxxDBHelper.Contract.Note.ID + "=" + noteID, null);
                db.close();
                return delCount;
            }
            default: {
                throw new UnsupportedOperationException("Not yet implemented");
            }
        }
    }


    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (mURIMatcher.match(uri)) {
            case COURSES: {
                Log.d(TAG, "INSERT INTO COURSES");
                SQLiteDatabase db = mDB.getWritableDatabase();
                long newCourseId = db.insert(StudybloxxDBHelper.COURSE_TABLE_NAME, null, values);
                mContentResolver.notifyChange(COURSE_CONTENT_URI, null);
                db.close();
                return ContentUris.withAppendedId(COURSE_CONTENT_URI, newCourseId);
            }
            case NOTES: {
                Log.d(TAG, "INSERT INTO NOTES");
                SQLiteDatabase db = mDB.getWritableDatabase();
                long newId = db.insert(StudybloxxDBHelper.NOTE_TABLE_NAME, null, values);
                mContentResolver.notifyChange(NOTE_CONTENT_URI, null);
                db.close();
                return ContentUris.withAppendedId(StudybloxxProvider.NOTE_CONTENT_URI, newId);
            }
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        switch (mURIMatcher.match(uri)) {
            case NOTES:
                SQLiteDatabase db = mDB.getWritableDatabase();
                db.compileStatement(INSERT_NOTE_STATEMENT);
                db.beginTransaction();

                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
        }
        return super.bulkInsert(uri, values);
    }


    @Override
    public boolean onCreate() {
        mDB = new StudybloxxDBHelper(getContext());
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mServerAddress = "http://" + mPrefs.getString("sync_server_address", "127.0.0.1:8000");
        Log.d(TAG, "Server Address: " + mServerAddress);

        mContentResolver = getContext().getContentResolver();
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (mURIMatcher.match(uri)) {
            case COURSES: {
                Log.d(TAG, "QUERY COURSES");
                SQLiteDatabase database = mDB.getReadableDatabase();
                Cursor coursesCursor = database.query(StudybloxxDBHelper.COURSE_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                coursesCursor.setNotificationUri(getContext().getContentResolver(), COURSE_CONTENT_URI);
                return coursesCursor;
            }
            case COURSE_ID:
                Log.d(TAG, "QUERY COURSE ID");
                break;
            case NOTES: {
                Log.d(TAG, "QUERY NOTES");
                SQLiteDatabase database = mDB.getReadableDatabase();
                Cursor notesCursor = database.query(StudybloxxDBHelper.NOTE_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                notesCursor.setNotificationUri(getContext().getContentResolver(), NOTE_CONTENT_URI);
                return notesCursor;
            }
            case NOTE_ID: {
                long noteId = Long.parseLong(uri.getLastPathSegment());
                Log.d(TAG, "Note ID: " + noteId);
                SQLiteDatabase database = mDB.getReadableDatabase();

                Cursor noteCursor = database.query(StudybloxxDBHelper.NOTE_TABLE_NAME, projection, StudybloxxDBHelper.Contract.Note.ID + "=?", new String[]{Long.toString(noteId)}, null, null, null);
                noteCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return noteCursor;
            }
        }
        return null;
        // TODO: Implement this to handle query requests from clients.
        // throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (mURIMatcher.match(uri)) {
            case COURSES: {
                SQLiteDatabase db = mDB.getWritableDatabase();
                final int updateCount = db.update(StudybloxxDBHelper.COURSE_TABLE_NAME, values, selection, selectionArgs);
                Log.d(TAG, "Number of Updated Courses: " + updateCount);
                db.close();
                mContentResolver.notifyChange(uri, null);
                return updateCount;
            }
            case COURSE_ID: {
                SQLiteDatabase db = mDB.getWritableDatabase();
                long courseId = ContentUris.parseId(uri);
                final int updateCount = db.update(StudybloxxDBHelper.COURSE_TABLE_NAME, values, StudybloxxDBHelper.Contract.Course.ID + "=?", new String[]{Long.toString(courseId)});
                db.close();
                mContentResolver.notifyChange(uri, null);
                return updateCount;
            }
            case NOTES: {
                SQLiteDatabase db = mDB.getWritableDatabase();
                final int updateCount = db.update(StudybloxxDBHelper.NOTE_TABLE_NAME, values, selection, selectionArgs);
                db.close();
                mContentResolver.notifyChange(uri, null);
                return updateCount;
            }
            case NOTE_ID: {
                SQLiteDatabase db = mDB.getWritableDatabase();
                long id = Long.parseLong(uri.getLastPathSegment());
                int updateCount = db.update(StudybloxxDBHelper.NOTE_TABLE_NAME, values, StudybloxxDBHelper.Contract.Note.ID + "=?", new String[]{Long.toString(id)});
                db.close();
                mContentResolver.notifyChange(uri, null);
                return updateCount;
            }
            default: {
                // TODO: Implement this to handle requests to update one or more rows.
                throw new UnsupportedOperationException("Not yet implemented");
            }
        }
    }
}
