package tu.dresden.studybloxx.syncadapter;

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tu.dresden.studybloxx.database.StudybloxxDBHelper;
import tu.dresden.studybloxx.providers.StudybloxxProvider;
import tu.dresden.studybloxx.utils.Constants;

/**
 * Created by arjun on 02.04.14.
 */
public class NoteSyncHelper implements StudybloxxSyncAdapter.SyncableHelper {

    private final String mServerAddress;
    private final ContentProviderClient mProviderClient;

    NoteSyncHelper(Context context, ContentProviderClient client) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mServerAddress = prefs.getString("sync_server_address", Constants.STUDYBLOXX_DEFAULT_SERVER_ADDRESS);
        mProviderClient = client;
    }

    @Override
    public String getResourceEndpoint() {
        return mServerAddress + "/bloxxdata/api/v1/note/";
    }

    @Override
    public StudybloxxSyncAdapter.NewResource[] getNewResourceObjects() throws JSONException, RemoteException {
        final Cursor newNoteCursor = mProviderClient.query(StudybloxxProvider.NOTE_CONTENT_URI, StudybloxxDBHelper.Contract.Note.COLUMNS_NEW_NOTE, StudybloxxDBHelper.Contract.Note.SYNC_STATUS + "=?", new String[]{Integer.toString(StudybloxxDBHelper.Contract.SyncStatus.CREATED)}, null);
        int newNoteCount = newNoteCursor.getCount();
        if (newNoteCount > 0) {
            newNoteCursor.moveToFirst();
            StudybloxxSyncAdapter.NewResource[] newCourses = new StudybloxxSyncAdapter.NewResource[newNoteCount];
            int counter = 0;
            while (!newNoteCursor.isAfterLast()) {
                long courseID = newNoteCursor.getLong(3);
                final Cursor courseCursor = mProviderClient.query(StudybloxxProvider.COURSE_CONTENT_URI, new String[]{StudybloxxDBHelper.Contract.Course.URL}, StudybloxxDBHelper.Contract.Course.ID + "=" + courseID, null, null);
                if (courseCursor.getCount() > 0) {
                    courseCursor.moveToFirst();
                    final String courseURI = courseCursor.getString(0);
                    newCourses[counter] = new StudybloxxSyncAdapter.NewResource();
                    JSONObject json = new JSONObject();
                    String title = newNoteCursor.getString(1);
                    String content = newNoteCursor.getString(2);
                    json.put(Constants.JSON.Note.TITLE, title);
                    json.put(Constants.JSON.Note.CONTENT, content);
                    json.put(Constants.JSON.Note.COURSE, courseURI);
                    newCourses[counter].resourceJSON = json;
                    newCourses[counter].localId = newNoteCursor.getLong(0);
                } else {
                    //TODO: Handle this exception case better. When the resource URI of the course is not present.
                }

                counter++;
                newNoteCursor.moveToNext();
            }
            return newCourses;
        } else {
            return new StudybloxxSyncAdapter.NewResource[0];
        }
    }

    @Override
    public JSONArray getModifiedResourceObject() throws RemoteException, JSONException {
        final Cursor modifiedNoteCusor = mProviderClient.query(StudybloxxProvider.NOTE_CONTENT_URI, StudybloxxDBHelper.Contract.Note.COLUMNS_MODIFIED_NOTE, StudybloxxDBHelper.Contract.Note.SYNC_STATUS + "=" + StudybloxxDBHelper.Contract.SyncStatus.MODIFIED, null, null);
        JSONArray modifiedArray = new JSONArray();
        final int modifiedCount = modifiedNoteCusor.getCount();
        if (modifiedCount > 0) {
            modifiedNoteCusor.moveToFirst();
            while (!modifiedNoteCusor.isAfterLast()) {
                modifiedNoteCusor.moveToNext();
                final String title = modifiedNoteCusor.getString(1);
                final String content = modifiedNoteCusor.getString(2);
                final String resourceURI = modifiedNoteCusor.getString(3);
                JSONObject json = new JSONObject();
                json.put(Constants.JSON.Note.TITLE, title);
                json.put(Constants.JSON.Note.CONTENT, content);
                json.put(Constants.JSON.Note.URI, resourceURI);
                modifiedArray.put(json);
            }
        }
        return modifiedArray;
    }

    @Override
    public JSONArray getDeletedResourceUris() throws RemoteException {
        final Cursor modifiedNoteCusor = mProviderClient.query(StudybloxxProvider.NOTE_CONTENT_URI, StudybloxxDBHelper.Contract.Note.COLUMNS_DELETED_NOTE, StudybloxxDBHelper.Contract.Note.SYNC_STATUS + "=" + StudybloxxDBHelper.Contract.SyncStatus.CLIENT_DELETED, null, null);
        JSONArray modifiedArray = new JSONArray();
        final int modifiedCount = modifiedNoteCusor.getCount();
        if (modifiedCount > 0) {
            modifiedNoteCusor.moveToFirst();
            while (!modifiedNoteCusor.isAfterLast()) {
                modifiedNoteCusor.moveToNext();
                final String uri = modifiedNoteCusor.getString(1);
                modifiedArray.put(uri);
            }
        }
        return modifiedArray;
    }

    @Override
    public boolean setUploaded(long resourceId, String resourceUri) throws RemoteException {
        final Uri uri = ContentUris.withAppendedId(StudybloxxProvider.NOTE_CONTENT_URI, resourceId);
        ContentValues values = new ContentValues(2);
        values.put(StudybloxxDBHelper.Contract.Note.SYNC_STATUS, StudybloxxDBHelper.Contract.SyncStatus.SYNCED);
        values.put(StudybloxxDBHelper.Contract.Note.URL, resourceUri);
        final int updateCount = mProviderClient.update(uri, values, null, null);
        return updateCount > 0;
    }

    @Override
    public boolean setAllModifiedSynced() throws RemoteException {
        final int deleteCount = mProviderClient.delete(StudybloxxProvider.NOTE_CONTENT_URI, StudybloxxDBHelper.Contract.Note.SYNC_STATUS + "=" + StudybloxxDBHelper.Contract.SyncStatus.CLIENT_DELETED, null);
        ContentValues values = new ContentValues(1);
        values.put(StudybloxxDBHelper.Contract.Course.SYNC_STATUS, StudybloxxDBHelper.Contract.SyncStatus.SYNCED);
        final int updateCount = mProviderClient.update(StudybloxxProvider.NOTE_CONTENT_URI, values, StudybloxxDBHelper.Contract.Note.SYNC_STATUS + "=" + StudybloxxDBHelper.Contract.SyncStatus.MODIFIED, null);
        return updateCount > 0 || deleteCount > 0;
    }

    @Override
    public String[] compareWithServer(JSONObject results) {
        return new String[0];
    }

    @Override
    public boolean addNewResourceObjects(JSONObject data) {
        return false;
    }
}
