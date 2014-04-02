package tu.dresden.studybloxx.syncadapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import tu.dresden.studybloxx.authentication.StudybloxxAuthentication;
import tu.dresden.studybloxx.utils.Constants;
import tu.dresden.studybloxx.utils.HttpPatch;

/**
 * Created by Arjun Naik on 26.03.14.
 * Desciption of the class.
 */
public class StudybloxxSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String CREATED_LOCATION_HEADER = "Location";
    private final String TAG = this.getClass().getName();
    private final AccountManager mAccountMan;
    private final Context mContext;
    private String mServerAddress;

    public StudybloxxSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mAccountMan = AccountManager.get(context);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mServerAddress = prefs.getString("sync_server_address", Constants.STUDYBLOXX_DEFAULT_SERVER_ADDRESS);
        mContext = context;
    }

    public static String getResponseBody(HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 65728);
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String authority, ContentProviderClient client, SyncResult syncResult) {
        SyncableHelper[] helpers = new SyncableHelper[]{new CourseSyncHelper(mContext, client), new NoteSyncHelper(mContext, client)};
        boolean uploadOnly = bundle.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD);
        Log.d(TAG, "Upload Only: " + uploadOnly);

        HttpClient httpclient = new DefaultHttpClient();


        try {

            final String authToken = mAccountMan.blockingGetAuthToken(account, StudybloxxAuthentication.AUTHTOKEN_TYPE_FULL_ACCESS, true);
            Log.d(TAG, "Auth Token: " + authToken);

            final String csrfToken = mAccountMan.getUserData(account, StudybloxxAuthentication.CSRF_TOKEN);
            Log.d(TAG, "CSRF TOKEN: " + csrfToken);

            for (SyncableHelper helper : helpers) {
                final String endPoint = helper.getResourceEndpoint();
                final NewResource[] newObjs = helper.getNewResourceObjects();

                for (NewResource obj : newObjs) {
                    HttpPost httppost = new HttpPost(endPoint);
                    Log.d(TAG, obj.resourceJSON.toString());
                    StringEntity entity = new StringEntity(obj.resourceJSON.toString());
                    httppost.setEntity(entity);
                    httppost.addHeader("X-CSRFToken", csrfToken);
                    httppost.addHeader("Cookie", "sessionid=" + authToken + ";csrftoken=" + csrfToken);
                    httppost.addHeader("Content-Type", "application/json");
                    HttpResponse response = httpclient.execute(httppost);

                    getResponseBody(response);

                    final Header[] allHeaders = response.getAllHeaders();
                    URL resourceURL = null;
                    for (Header h : allHeaders) {
                        Log.d(TAG, h.getName() + " : " + h.getValue());
                        if (CREATED_LOCATION_HEADER.equals(h.getName())) {
                            resourceURL = new URL(h.getValue());
                            Log.d(TAG, "Location: " + resourceURL.getPath());
                        }
                    }

                    if (response.getStatusLine().getStatusCode() == 201 && resourceURL.getPath() != null) {
                        helper.setUploaded(obj.localId, resourceURL.getPath());

                        Log.d(TAG, "Successfuly informed server");
                    } else {
                        Log.e(TAG, "Server upload of course failed");
                    }
                }


                final JSONArray modifiedArray = helper.getModifiedResourceObject();
                final JSONArray deletedArray = helper.getDeletedResourceUris();
                if (modifiedArray.length() != 0 || deletedArray.length() != 0) {
                    JSONObject patchJSON = new JSONObject();
                    patchJSON.put("objects", modifiedArray);
                    patchJSON.put("deleted_objects", deletedArray);
                    HttpPatch httpPatch = new HttpPatch(endPoint);
                    StringEntity entity = new StringEntity(patchJSON.toString());
                    httpPatch.setEntity(entity);
                    httpPatch.addHeader("X-CSRFToken", csrfToken);
                    httpPatch.addHeader("Cookie", "sessionid=" + authToken + ";csrftoken=" + csrfToken);
                    httpPatch.addHeader("Content-Type", "application/json");
                    HttpResponse response = httpclient.execute(httpPatch);


                    getResponseBody(response);

                    final Header[] allHeaders = response.getAllHeaders();
                    for (Header h : allHeaders) {
                        Log.d(TAG, h.getName() + " : " + h.getValue());
                    }

                    if (response.getStatusLine().getStatusCode() == 202) {
                        helper.setAllModifiedSynced();
                        Log.d(TAG, "Successfuly informed server of modifications and deletions");
                    } else {
                        Log.e(TAG, "Server patch of deleted and uploaded courses failed");
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        }

    }

    //TODO: Better name for this interface.
    public interface SyncableHelper {
        public String getResourceEndpoint();

        public NewResource[] getNewResourceObjects() throws JSONException, RemoteException;

        public JSONArray getModifiedResourceObject() throws RemoteException, JSONException;

        public JSONArray getDeletedResourceUris() throws RemoteException;

        public boolean setUploaded(long resourceId, String resourceUri) throws RemoteException;

        public boolean setAllModifiedSynced() throws RemoteException;

        public String[] compareWithServer(JSONObject results);

        public boolean addNewResourceObjects(JSONObject data);
    }

    public static class NewResource {
        long localId;
        JSONObject resourceJSON;
    }
}
