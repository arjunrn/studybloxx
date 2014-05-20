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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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

    public static HttpRequestBase getRequest(RequestType requestType, String csrfToken, String sessionToken, String url, String entity) throws UnsupportedEncodingException {
        HttpRequestBase request = null;
        switch (requestType) {
            case HTTP_POST: {
                final HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new StringEntity(entity));
                request = httpPost;
                break;
            }
            case HTTP_GET: {
                request = new HttpGet(url);
                break;
            }
            case HTTP_DELETE: {
                request = new HttpDelete(url);
                break;
            }
            case HTTP_PATCH: {
                final HttpPatch httpPatch = new HttpPatch(url);
                httpPatch.setEntity(new StringEntity(entity));
                request = httpPatch;
                break;
            }
        }

        request.addHeader("X-CSRFToken", csrfToken);
        request.addHeader("Cookie", "sessionid=" + sessionToken + ";csrftoken=" + csrfToken);
        request.addHeader("Content-Type", "application/json");

        return request;
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
                    HttpRequestBase request = getRequest(RequestType.HTTP_POST, csrfToken, authToken, endPoint, obj.resourceJSON.toString());
                    HttpResponse response = httpclient.execute(request);
                    //getResponseBody(response);
                    final Header[] allHeaders = response.getAllHeaders();
                    URL resourceURL = null;
                    for (Header h : allHeaders) {
                        Log.d(TAG, h.getName() + " : " + h.getValue());
                        if (CREATED_LOCATION_HEADER.equals(h.getName())) {
                            resourceURL = new URL(h.getValue());
                            Log.d(TAG, "Location: " + resourceURL.getPath());
                        }
                    }

                    final int statusCode = response.getStatusLine().getStatusCode();
                    Log.e(TAG, "Status code: " + statusCode);

                    if (statusCode == 201 && resourceURL.getPath() != null) {
                        helper.setUploaded(obj.localId, resourceURL.getPath());
                        Log.d(TAG, "Successfuly informed server");
                        syncResult.stats.numInserts++;
                    } else if (statusCode == 401) {
                        Log.i(TAG, "Server token expired");
                        mAccountMan.invalidateAuthToken(account.type, authToken);
                        syncResult.stats.numAuthExceptions++;
                        continue;

                    } else {
                        Log.e(TAG, "Server upload of course failed");
                        syncResult.stats.numParseExceptions++;
                        continue;
                    }
                }


                final JSONArray modifiedArray = helper.getModifiedResourceObject();
                final JSONArray deletedArray = helper.getDeletedResourceUris();
                final int modifiedItems = modifiedArray.length();
                final int deletedItems = deletedArray.length();
                Log.d(TAG, "Number of Modified Objects: " + modifiedItems);
                Log.d(TAG, "Number of Deleted Objects: " + deletedItems);

                if (modifiedItems != 0 || deletedItems != 0) {
                    JSONObject patchJSON = new JSONObject();
                    patchJSON.put("objects", modifiedArray);
                    patchJSON.put("deleted_objects", deletedArray);
                    final HttpRequestBase request = getRequest(RequestType.HTTP_PATCH, csrfToken, authToken, endPoint, patchJSON.toString());
                    HttpResponse response = httpclient.execute(request);
                    //getResponseBody(response);
                    if (response.getStatusLine().getStatusCode() == 202) {
                        helper.setAllModifiedSynced();
                        syncResult.stats.numUpdates += modifiedItems;
                        syncResult.stats.numDeletes += deletedItems;
                        Log.d(TAG, "Successfuly informed server of modifications and deletions");
                    } else {
                        syncResult.stats.numSkippedEntries = syncResult.stats.numSkippedEntries + modifiedItems + deletedItems;
                        Log.e(TAG, "Server patch of deleted and uploaded courses failed");
                    }
                }

                //TODO: Account for pagination. Should there be a limit to the number of resources requested.
                if (!uploadOnly) {
                    final HttpRequestBase request = getRequest(RequestType.HTTP_GET, csrfToken, authToken, endPoint, null);
                    HttpResponse response = httpclient.execute(request);
                    if (response.getStatusLine().getStatusCode() != 200) {
                        mAccountMan.invalidateAuthToken(account.type, authToken);
                        syncResult.stats.numAuthExceptions++;
                        continue;
                    }
                    final String list = getResponseBody(response);
                    final JSONObject listJSON = new JSONObject(list);
                    final String[] resourceURIs = helper.compareWithServer(listJSON);
                    Log.d(TAG, "Number of missing resources :" + resourceURIs.length);

                    //TODO: Shorten this request so that all resources are request together.
                    for (String uri : resourceURIs) {
                        final HttpRequestBase getRequest = getRequest(RequestType.HTTP_GET, csrfToken, authToken, mServerAddress + uri, null);
                        HttpResponse resourceResponse = httpclient.execute(getRequest);
                        if (resourceResponse.getStatusLine().getStatusCode() != 200) {
                            Log.e(TAG, "Could not fetch missing resources from server.");
                            syncResult.stats.numConflictDetectedExceptions++;
                            syncResult.stats.numAuthExceptions++;
                            continue;
                        }
                        final String objectResponse = getResponseBody(resourceResponse);
                        final boolean addResult = helper.addNewResourceObjects(new JSONObject(objectResponse));
                        Log.d(TAG, "Result of fetching: " + addResult);
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

    enum RequestType {
        HTTP_POST,
        HTTP_GET,
        HTTP_DELETE,
        HTTP_PATCH
    }

    //TODO: Better name for this interface.
    public interface SyncableHelper {
        public String getResourceEndpoint();

        public NewResource[] getNewResourceObjects() throws JSONException, RemoteException;

        public JSONArray getModifiedResourceObject() throws RemoteException, JSONException;

        public JSONArray getDeletedResourceUris() throws RemoteException;

        public boolean setUploaded(long resourceId, String resourceUri) throws RemoteException;

        public boolean setAllModifiedSynced() throws RemoteException;

        public String[] compareWithServer(JSONObject results) throws RemoteException, JSONException;

        public boolean addNewResourceObjects(JSONObject data) throws JSONException, RemoteException;
    }

    public static class NewResource {
        long localId;
        JSONObject resourceJSON;
    }
}
