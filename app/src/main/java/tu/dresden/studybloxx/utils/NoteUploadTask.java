package tu.dresden.studybloxx.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import tu.dresden.studybloxx.database.StudybloxxDBHelper;
import tu.dresden.studybloxx.database.StudybloxxProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;


public abstract class NoteUploadTask extends AsyncTask<Void, Void, Boolean>
{

	private static final String TAG = "UploadTask";
	private String mUsername;
	private String mPassword;
	private HttpClient mHttpClient;
	private HttpPost mHttpPostNew;
	private HttpPost mHttpPostUpdate;
	private StudybloxxDBHelper mHelper;
	private ContentResolver mResolver;


	public NoteUploadTask(Context context, String username, String password)
	{
		mHelper = new StudybloxxDBHelper(context);
		mResolver = context.getContentResolver();
		mUsername = username;
		mPassword = password;
		mHttpClient = new DefaultHttpClient();
		mHttpPostNew = new HttpPost(Constants.NOTE_UPLOAD_URL);
		mHttpPostUpdate = new HttpPost(Constants.NOTE_UPDATE_URL);

	}


	@Override
	protected Boolean doInBackground(Void... params)
	{
		ArrayList<NameValuePair> reqParams = new ArrayList<NameValuePair>();
		SQLiteDatabase database = mHelper.getWritableDatabase();
		Cursor cursor = database.query(StudybloxxDBHelper.NOTE_TABLE_NAME, new String[] { StudybloxxDBHelper.NOTE_ID, StudybloxxDBHelper.NOTE_TITLE,
			StudybloxxDBHelper.NOTE_CONTENT, StudybloxxDBHelper.NOTE_COURSE }, StudybloxxDBHelper.NOTE_UNSYNCED + "=1", null, null, null, null);

		int unUploadedCount = cursor.getCount();
		Log.d(TAG, "Unsynced New Note Count: " + unUploadedCount);

		if (unUploadedCount > 0)
		{
			reqParams.clear();
			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{
				long firstNoteId = cursor.getLong(0);
				String title = cursor.getString(1);
				String content = cursor.getString(2);
				long courseId = cursor.getLong(3);

				reqParams.add(new BasicNameValuePair("title", title));
				reqParams.add(new BasicNameValuePair("text", content));
				reqParams.add(new BasicNameValuePair("courseid", Long.toString(courseId)));
				reqParams.add(new BasicNameValuePair("username", mUsername));
				reqParams.add(new BasicNameValuePair("password", mPassword));

				try
				{
					mHttpPostNew.setEntity(new UrlEncodedFormEntity(reqParams));
				}
				catch (UnsupportedEncodingException e)
				{
					Log.e(TAG, "Error Encoding Request Parameters");
					e.printStackTrace();
					return false;
				}

				HttpResponse response;
				String responseString;
				try
				{
					response = mHttpClient.execute(mHttpPostNew);
					responseString = Helper.inputStreamToString(response.getEntity().getContent());
					Log.d(TAG, "Response: " + responseString);
				}
				catch (ClientProtocolException e)
				{
					e.printStackTrace();
					return false;
				}
				catch (IOException e)
				{
					e.printStackTrace();
					return false;
				}

				Log.d(TAG, responseString);
				long noteId, updated, created;
				try
				{
					JSONObject responseJSON = new JSONObject(responseString);
					noteId = responseJSON.getLong("note");
					updated = responseJSON.getLong("updated");
					created = responseJSON.getLong("created");
				}
				catch (JSONException e)
				{
					Log.e(TAG, "JSON response if faulty for Add Note");
					return false;
				}
				ContentValues values = new ContentValues();
				values.put(StudybloxxDBHelper.NOTE_ID, noteId);
				values.put(StudybloxxDBHelper.NOTE_CREATED, created);
				values.put(StudybloxxDBHelper.NOTE_UPDATED, updated);
				values.put(StudybloxxDBHelper.NOTE_SYNCED, 1);
				values.put(StudybloxxDBHelper.NOTE_UNSYNCED, 0);
				database.update(StudybloxxDBHelper.NOTE_TABLE_NAME, values, StudybloxxDBHelper.NOTE_ID + "=?", new String[] { Long.toString(firstNoteId) });

				cursor.moveToNext();
			}
		}
		cursor.close();

		cursor = database.query(StudybloxxDBHelper.NOTE_TABLE_NAME, new String[] { StudybloxxDBHelper.NOTE_ID, StudybloxxDBHelper.NOTE_TITLE,
			StudybloxxDBHelper.NOTE_CONTENT, StudybloxxDBHelper.NOTE_COURSE }, StudybloxxDBHelper.NOTE_UNSYNCED + "=2", null, null, null, null);
		unUploadedCount = cursor.getCount();
		Log.d(TAG, "Unsynced Updated Note Count: " + unUploadedCount);

		if (unUploadedCount > 0)
		{
			reqParams.clear();
			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{
				long firstNoteId = cursor.getLong(0);
				String title = cursor.getString(1);
				String content = cursor.getString(2);
				long courseId = cursor.getLong(3);

				reqParams.add(new BasicNameValuePair("title", title));
				reqParams.add(new BasicNameValuePair("text", content));
				reqParams.add(new BasicNameValuePair("courseid", Long.toString(courseId)));
				reqParams.add(new BasicNameValuePair("noteid", Long.toString(firstNoteId)));
				reqParams.add(new BasicNameValuePair("username", mUsername));
				reqParams.add(new BasicNameValuePair("password", mPassword));

				try
				{
					mHttpPostUpdate.setEntity(new UrlEncodedFormEntity(reqParams));
				}
				catch (UnsupportedEncodingException e)
				{
					Log.e(TAG, "Error Encoding Request Parameters");
					e.printStackTrace();
					return false;
				}

				HttpResponse response;
				String responseString;
				try
				{
					response = mHttpClient.execute(mHttpPostUpdate);
					responseString = Helper.inputStreamToString(response.getEntity().getContent());
					Log.d(TAG, "Response: " + responseString);
				}
				catch (ClientProtocolException e)
				{
					e.printStackTrace();
					return false;
				}
				catch (IOException e)
				{
					e.printStackTrace();
					return false;
				}

				Log.d(TAG, responseString);
				long noteId, updated;
				try
				{
					JSONObject responseJSON = new JSONObject(responseString);
					noteId = responseJSON.getLong("note");
					updated = responseJSON.getLong("updated");
				}
				catch (JSONException e)
				{
					Log.e(TAG, "JSON response if faulty for Add Note");
					return false;
				}
				ContentValues values = new ContentValues();
				values.put(StudybloxxDBHelper.NOTE_ID, noteId);
				values.put(StudybloxxDBHelper.NOTE_UPDATED, updated);
				values.put(StudybloxxDBHelper.NOTE_SYNCED, 1);
				values.put(StudybloxxDBHelper.NOTE_UNSYNCED, 0);
				database.update(StudybloxxDBHelper.NOTE_TABLE_NAME, values, StudybloxxDBHelper.NOTE_ID + "=?", new String[] { Long.toString(firstNoteId) });

				cursor.moveToNext();
			}
		}
		cursor.close();
		database.close();
		mResolver.notifyChange(StudybloxxProvider.NOTE_CONTENT_URI, null);
		return null;
	}

}
