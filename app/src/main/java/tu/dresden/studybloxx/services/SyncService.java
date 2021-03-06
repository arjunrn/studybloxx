package tu.dresden.studybloxx.services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tu.dresden.studybloxx.database.StudybloxxDBHelper;
import tu.dresden.studybloxx.providers.StudybloxxProvider;
import tu.dresden.studybloxx.utils.Helper;
import tu.dresden.studybloxx.utils.StudyBloxxClient;


public class SyncService extends Service {

    protected static final String TAG = "SyncService";
    AsyncHttpResponseHandler noteSyncHandler = new AsyncHttpResponseHandler() {
        public void onSuccess(int arg0, org.apache.http.Header[] arg1, byte[] arg2) {
            Log.d(TAG, "Successfully received notes sync response");
            String jsonResponse = new String(arg2);
            Log.d(TAG, jsonResponse);
            try {
                JSONArray notesResponse = new JSONArray(jsonResponse);
                int length = notesResponse.length();
                Log.d(TAG, "Number of Notes: " + length);
                SQLiteDatabase database = mDBHelper.getWritableDatabase();

                for (int i = 0; i < length; i++) {
                    JSONObject noteObject = notesResponse.getJSONObject(i);
                    ContentValues values = new ContentValues(4);
                    values.put(StudybloxxDBHelper.Contract.Note.ID, noteObject.getInt("id"));
                    values.put(StudybloxxDBHelper.Contract.Note.TITLE, noteObject.getString("title"));
                    values.put(StudybloxxDBHelper.Contract.Note.URL, noteObject.getString("url"));
                    values.put(StudybloxxDBHelper.Contract.Note.COURSE, noteObject.getLong("course"));
                    values.put(StudybloxxDBHelper.Contract.Note.SYNC_STATUS, 0);
                    database.insertWithOnConflict(StudybloxxDBHelper.NOTE_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                }

                database.close();
                getContentResolver().notifyChange(StudybloxxProvider.NOTE_CONTENT_URI, null);
                Log.d(TAG, "Notified the Provider of change.");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        ;


        public void onFailure(int arg0, org.apache.http.Header[] arg1, byte[] arg2, Throwable arg3) {
            Log.e(TAG, "Failed to sync notes sync reponse");
        }

        ;
    };
    private StudybloxxDBHelper mDBHelper;
    private ContentResolver mResolver;
    private String mServerAddress;
    AsyncHttpResponseHandler syncHandler = new AsyncHttpResponseHandler() {
        public void onSuccess(int arg0, org.apache.http.Header[] arg1, byte[] arg2) {
            Log.d(TAG, "Successfully Synced");
            String jsonResponse = new String(arg2);
            try {
                JSONArray courseJSON = new JSONArray(jsonResponse);
                System.out.println(courseJSON.toString());
                SQLiteDatabase db = mDBHelper.getWritableDatabase();

                for (int i = 0; i < courseJSON.length(); i++) {
                    JSONObject courseItem = courseJSON.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put(StudybloxxDBHelper.Contract.Course.ID, courseItem.getInt("id"));
                    values.put(StudybloxxDBHelper.Contract.Course.TITLE, courseItem.getString("title"));
                    values.put(StudybloxxDBHelper.Contract.Course.URL, courseItem.getString("url"));
                    values.put(StudybloxxDBHelper.Contract.Course.SYNC_STATUS, 1);
                    db.insertWithOnConflict(StudybloxxDBHelper.COURSE_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                }
                db.close();
                mResolver.notifyChange(StudybloxxProvider.COURSE_CONTENT_URI, null);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing course JSON");
                e.printStackTrace();
            }

            RequestParams params = new RequestParams();
            params.add("username", Helper.getStoredUserName(SyncService.this));
            params.add("password", Helper.getStoredPassword(SyncService.this));
            StudyBloxxClient.get(mServerAddress + "/bloxxdata/all-notes-gist/", params, noteSyncHandler);
        }

        ;


        public void onFailure(int arg0, org.apache.http.Header[] arg1, byte[] arg2, Throwable arg3) {
            Log.e(TAG, "Course Sync Failed");
        }

        ;
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() called");
        mDBHelper = new StudybloxxDBHelper(this);
        mResolver = getContentResolver();
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mServerAddress = "http://" + mPrefs.getString("sync_server_address", "127.0.0.1:8000");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        RequestParams syncParams = new RequestParams();
        syncParams.add("username", Helper.getStoredUserName(this));
        syncParams.add("password", Helper.getStoredPassword(this));
        getContentResolver().query(StudybloxxProvider.COURSE_CONTENT_URI, new String[]{StudybloxxDBHelper.Contract.Course.ID}, null, null, null);
        StudyBloxxClient.get(mServerAddress + "/bloxxdata/user-courses/", syncParams, syncHandler);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
