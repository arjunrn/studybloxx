package tu.dresden.studybloxx.services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tu.dresden.studybloxx.database.StudybloxxDBHelper;
import tu.dresden.studybloxx.database.StudybloxxProvider;
import tu.dresden.studybloxx.utils.Helper;
import tu.dresden.studybloxx.utils.StudyBloxxClient;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


public class SyncService extends Service
{

	protected static final String TAG = "SyncService";
	private StudybloxxDBHelper mDBHelper;
	private ContentResolver mResolver;


	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG, "onCreate() called");
		mDBHelper = new StudybloxxDBHelper(this);
		mResolver = getContentResolver();
	}


	public int onStartCommand(Intent intent, int flags, int startId)
	{
		RequestParams syncParams = new RequestParams();
		syncParams.add("username", Helper.getStoredUserName(this));
		syncParams.add("password", Helper.getStoredPassword(this));
		getContentResolver().query(StudybloxxProvider.COURSE_CONTENT_URI, new String[] { StudybloxxDBHelper.COURSE_ID }, null, null, null);
		StudyBloxxClient.get("/bloxxdata/user-courses/", syncParams, syncHandler);
		return START_NOT_STICKY;
	}

	AsyncHttpResponseHandler syncHandler = new AsyncHttpResponseHandler()
	{
		public void onSuccess(int arg0, org.apache.http.Header[] arg1, byte[] arg2)
		{
			Log.d(TAG, "Successfully Synced");
			String jsonResponse = new String(arg2);
			try
			{
				JSONArray courseJSON = new JSONArray(jsonResponse);
				System.out.println(courseJSON.toString());
				SQLiteDatabase db = mDBHelper.getWritableDatabase();

				for (int i = 0; i < courseJSON.length(); i++)
				{
					JSONObject courseItem = courseJSON.getJSONObject(i);
					ContentValues values = new ContentValues();
					values.put(StudybloxxDBHelper.COURSE_ID, courseItem.getInt("id"));
					values.put(StudybloxxDBHelper.COURSE_TITLE, courseItem.getString("title"));
					values.put(StudybloxxDBHelper.COURSE_URL, courseItem.getString("url"));
					values.put(StudybloxxDBHelper.COURSE_SYNCED, 1);
					db.insertWithOnConflict(StudybloxxDBHelper.COURSE_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
				}
				db.close();
				mResolver.notifyChange(StudybloxxProvider.COURSE_CONTENT_URI, null);
			}
			catch (JSONException e)
			{
				Log.e(TAG, "Error parsing course JSON");
				e.printStackTrace();
			}

			RequestParams params = new RequestParams();
			params.add("username", Helper.getStoredUserName(SyncService.this));
			params.add("password", Helper.getStoredPassword(SyncService.this));
			StudyBloxxClient.get("/bloxxdata/all-notes-gist/", params, noteSyncHandler);
		};


		public void onFailure(int arg0, org.apache.http.Header[] arg1, byte[] arg2, Throwable arg3)
		{
			Log.e(TAG, "Course Sync Failed");
		};
	};

	AsyncHttpResponseHandler noteSyncHandler = new AsyncHttpResponseHandler()
	{
		public void onSuccess(int arg0, org.apache.http.Header[] arg1, byte[] arg2)
		{
			Log.d(TAG, "Successfully received notes sync response");
			String jsonResponse = new String(arg2);
			Log.d(TAG, jsonResponse);
			try
			{
				JSONArray notesResponse = new JSONArray(jsonResponse);
				int length = notesResponse.length();
				Log.d(TAG, "Number of Notes: " + length);
				SQLiteDatabase database = mDBHelper.getWritableDatabase();

				for (int i = 0; i < length; i++)
				{
					JSONObject noteObject = notesResponse.getJSONObject(i);
					ContentValues values = new ContentValues(4);
					values.put(StudybloxxDBHelper.NOTE_ID, noteObject.getInt("id"));
					values.put(StudybloxxDBHelper.NOTE_TITLE, noteObject.getString("title"));
					values.put(StudybloxxDBHelper.NOTE_URL, noteObject.getString("url"));
					values.put(StudybloxxDBHelper.NOTE_COURSE, noteObject.getLong("course"));
					values.put(StudybloxxDBHelper.NOTE_SYNCED, 0);
					database.insertWithOnConflict(StudybloxxDBHelper.NOTE_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
				}

				database.close();
				getContentResolver().notifyChange(StudybloxxProvider.NOTE_CONTENT_URI, null);
				Log.d(TAG, "Notified the Provider of change.");
			}
			catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};


		public void onFailure(int arg0, org.apache.http.Header[] arg1, byte[] arg2, Throwable arg3)
		{
			Log.e(TAG, "Failed to sync notes sync reponse");
		};
	};


	@Override
	public IBinder onBind(Intent arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
