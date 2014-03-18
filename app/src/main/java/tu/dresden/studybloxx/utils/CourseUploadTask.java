package tu.dresden.studybloxx.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import tu.dresden.studybloxx.database.StudybloxxDBHelper;
import tu.dresden.studybloxx.providers.StudybloxxProvider;


public abstract class CourseUploadTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "CourseUploadTask";
    private StudybloxxDBHelper mHelper;
    private String mUsername;
    private String mPassword;
    private HttpPost mHttpPostNew;
    private DefaultHttpClient mHttpClient;
    private ContentResolver mResolver;


    public CourseUploadTask(Context context) {
        mHelper = new StudybloxxDBHelper(context);
        mResolver = context.getContentResolver();
        mUsername = Helper.getStoredUserName(context);
        mPassword = Helper.getStoredPassword(context);

        mHttpClient = new DefaultHttpClient();
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String serverAddress = "http://" + mPrefs.getString("sync_server_address", "127.0.0.1:8000");
        Log.d(TAG, "Server Address: " + serverAddress);
        mHttpPostNew = new HttpPost(serverAddress + Constants.COURSE_UPLOAD_URL);
    }


    @Override
    protected Boolean doInBackground(Void... arg0) {
        ArrayList<NameValuePair> reqParams = new ArrayList<NameValuePair>();
        SQLiteDatabase database = mHelper.getWritableDatabase();
        Cursor cursor = database.query(StudybloxxDBHelper.COURSE_TABLE_NAME, new String[]{StudybloxxDBHelper.Contract.Course.ID, StudybloxxDBHelper.Contract.Course.TITLE},
                StudybloxxDBHelper.Contract.Course.SYNC_STATUS + "=0", null, null, null, null);

        int unsyncedCount = cursor.getCount();
        Log.d(TAG, "Number of unsynced courses: " + unsyncedCount);

        if (unsyncedCount > 0) {
            reqParams.clear();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                long initialId = cursor.getLong(0);
                String title = cursor.getString(1);

                reqParams.add(new BasicNameValuePair("title", title));
                reqParams.add(new BasicNameValuePair("username", mUsername));
                reqParams.add(new BasicNameValuePair("password", mPassword));

                try {
                    mHttpPostNew.setEntity(new UrlEncodedFormEntity(reqParams));
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Error Encoding Request Parameters");
                    e.printStackTrace();
                    return false;
                }

                HttpResponse response;
                String responseString;
                try {
                    response = mHttpClient.execute(mHttpPostNew);
                    responseString = Helper.inputStreamToString(response.getEntity().getContent());
                    Log.d(TAG, "Response: " + responseString);
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }

                Log.d(TAG, responseString);
                String courseId, courseUrl;
                try {
                    JSONObject responseJSON = new JSONObject(responseString);
                    courseId = responseJSON.getString("course");
                    courseUrl = responseJSON.getString("url");

                } catch (JSONException e) {
                    Log.e(TAG, "JSON response if faulty for Add Note");
                    return false;
                }
                ContentValues values = new ContentValues();
                values.put(StudybloxxDBHelper.Contract.Course.ID, courseId);
                values.put(StudybloxxDBHelper.Contract.Course.URL, courseUrl);
                values.put(StudybloxxDBHelper.Contract.Course.SYNC_STATUS, 1);
                database.update(StudybloxxDBHelper.COURSE_TABLE_NAME, values, StudybloxxDBHelper.Contract.Course.ID + "=?", new String[]{Long.toString(initialId)});
                cursor.moveToNext();
            }
        }

        cursor.close();
        database.close();
        mResolver.notifyChange(StudybloxxProvider.NOTE_CONTENT_URI, null);
        return true;
    }

}
