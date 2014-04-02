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
 * Created by arjun on 30.03.14.
 */
public class CourseSyncHelper implements StudybloxxSyncAdapter.SyncableHelper {

    private final String mServerAddress;
    private final ContentProviderClient mProviderClient;

    public CourseSyncHelper(Context context, ContentProviderClient client) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mServerAddress = prefs.getString("sync_server_address", Constants.STUDYBLOXX_DEFAULT_SERVER_ADDRESS);
        mProviderClient = client;
    }

    @Override
    public String getResourceEndpoint() {
        return mServerAddress + "/bloxxdata/api/v1/course/";
    }

    @Override
    public StudybloxxSyncAdapter.NewResource[] getNewResourceObjects() throws JSONException, RemoteException {
        final Cursor newCourseCursor = mProviderClient.query(StudybloxxProvider.COURSE_CONTENT_URI, StudybloxxDBHelper.Contract.Course.COLUMNS_NEW_COURSE, StudybloxxDBHelper.Contract.Course.SYNC_STATUS + "=?", new String[]{Integer.toString(StudybloxxDBHelper.Contract.SyncStatus.CREATED)}, null);
        int newCourseCount = newCourseCursor.getCount();
        if (newCourseCount > 0) {
            newCourseCursor.moveToFirst();
            StudybloxxSyncAdapter.NewResource[] newCourses = new StudybloxxSyncAdapter.NewResource[newCourseCount];
            int counter = 0;
            while (!newCourseCursor.isAfterLast()) {
                newCourses[counter] = new StudybloxxSyncAdapter.NewResource();
                JSONObject json = new JSONObject();
                String title = newCourseCursor.getString(1);
                json.put(Constants.JSON.Course.TITLE, title);
                newCourses[counter].resourceJSON = json;
                newCourses[counter].localId = newCourseCursor.getLong(0);

                counter++;
                newCourseCursor.moveToNext();
            }
            return newCourses;
        } else {
            return new StudybloxxSyncAdapter.NewResource[0];
        }
    }

    @Override
    public JSONArray getModifiedResourceObject() throws RemoteException, JSONException {
        final Cursor modifiedCourseCursor = mProviderClient.query(StudybloxxProvider.COURSE_CONTENT_URI, StudybloxxDBHelper.Contract.Course.COLUMNS_MODIFIED_COURSE, StudybloxxDBHelper.Contract.Course.SYNC_STATUS + "=?", new String[]{Integer.toString(StudybloxxDBHelper.Contract.SyncStatus.MODIFIED)}, null);
        final int modifiedCount = modifiedCourseCursor.getCount();
        JSONArray jArray = new JSONArray();
        if (modifiedCount > 0) {
            modifiedCourseCursor.moveToFirst();

            while (!modifiedCourseCursor.isAfterLast()) {
                JSONObject json = new JSONObject();
                String title = modifiedCourseCursor.getString(1);
                String uri = modifiedCourseCursor.getString(3);
                json.put(Constants.JSON.Course.TITLE, title);
                json.put(Constants.JSON.Course.URI, uri);
                jArray.put(json);

                modifiedCourseCursor.moveToNext();
            }

        }
        return jArray;
    }

    @Override
    public JSONArray getDeletedResourceUris() throws RemoteException {
        final Cursor deletedCourseCursor = mProviderClient.query(StudybloxxProvider.COURSE_CONTENT_URI, StudybloxxDBHelper.Contract.Course.COLUMNS_DELETED_COURSE, StudybloxxDBHelper.Contract.Course.SYNC_STATUS + "=" + StudybloxxDBHelper.Contract.SyncStatus.CLIENT_DELETED, null, null);
        final int deletedCount = deletedCourseCursor.getCount();
        JSONArray jsonArray = new JSONArray();
        if (deletedCount > 0) {
            deletedCourseCursor.moveToFirst();
            for (int i = 0; i < deletedCount; i++) {
                jsonArray.put(deletedCourseCursor.getString(1));
                deletedCourseCursor.moveToNext();
            }
        }
        return jsonArray;
    }

    @Override
    public boolean setUploaded(long resourceId, String resourceUri) throws RemoteException {
        final Uri uri = ContentUris.withAppendedId(StudybloxxProvider.COURSE_CONTENT_URI, resourceId);
        ContentValues values = new ContentValues(2);
        values.put(StudybloxxDBHelper.Contract.Course.SYNC_STATUS, StudybloxxDBHelper.Contract.SyncStatus.SYNCED);
        values.put(StudybloxxDBHelper.Contract.Course.URL, resourceUri);
        final int updateCount = mProviderClient.update(uri, values, null, null);
        return updateCount > 0;
    }

    @Override
    public boolean setAllModifiedSynced() throws RemoteException {
        final int deleteCount = mProviderClient.delete(StudybloxxProvider.COURSE_CONTENT_URI, StudybloxxDBHelper.Contract.Course.SYNC_STATUS + "=" + StudybloxxDBHelper.Contract.SyncStatus.CLIENT_DELETED, null);
        ContentValues values = new ContentValues(1);
        values.put(StudybloxxDBHelper.Contract.Course.SYNC_STATUS, StudybloxxDBHelper.Contract.SyncStatus.SYNCED);
        final int updateCount = mProviderClient.update(StudybloxxProvider.COURSE_CONTENT_URI, values, StudybloxxDBHelper.Contract.Course.SYNC_STATUS + "=" + StudybloxxDBHelper.Contract.SyncStatus.MODIFIED, null);
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
