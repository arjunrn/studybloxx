package tu.dresden.studybloxx.syncadapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import tu.dresden.studybloxx.authentication.StudybloxxAuthentication;
import tu.dresden.studybloxx.database.StudybloxxDBHelper;
import tu.dresden.studybloxx.providers.StudybloxxProvider;

/**
 * Created by Arjun Naik on 26.03.14.
 * Desciption of the class.
 */
public class StudybloxxSyncAdapter extends AbstractThreadedSyncAdapter {
    private final String TAG = this.getClass().getName();
    private final AccountManager mAccountMan;

    public StudybloxxSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mAccountMan = AccountManager.get(context);

    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String authority, ContentProviderClient client, SyncResult syncResult) {
        boolean uploadOnly = bundle.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD);
        Log.d(TAG, "Upload Only: " + uploadOnly);
        try {
            final String authToken = mAccountMan.blockingGetAuthToken(account, StudybloxxAuthentication.AUTHTOKEN_TYPE_FULL_ACCESS, true);
            Log.d(TAG, "Auth Token" + authToken);

            final Cursor newCourseCursor = client.query(StudybloxxProvider.COURSE_CONTENT_URI, StudybloxxDBHelper.Contract.Course.COLUMNS_NEW_COURSE, StudybloxxDBHelper.Contract.Course.SYNC_STATUS + "=?", new String[]{Integer.toString(StudybloxxDBHelper.Contract.SyncStatus.CREATED)}, null);

            int newCourseCount = newCourseCursor.getCount();
            if (newCourseCount > 0) {
                ArrayList<Integer> newCourseIds = new ArrayList<Integer>();

                Log.d(TAG, "Number of new courses: " + newCourseCount);
                newCourseCursor.moveToFirst();
                while (!newCourseCursor.isAfterLast()) {
                    //TODO: Upload course here
                    newCourseIds.add(newCourseCursor.getInt(0));
                    newCourseCursor.moveToNext();
                }
                final String courseIdsJoined = TextUtils.join(",", newCourseIds);
                Log.d(TAG, "Updated Course IDs: " + courseIdsJoined);
                ContentValues newCourseUpdate = new ContentValues(1);
                newCourseUpdate.put(StudybloxxDBHelper.Contract.Course.SYNC_STATUS, StudybloxxDBHelper.Contract.SyncStatus.SYNCED);
                client.update(StudybloxxProvider.COURSE_CONTENT_URI, newCourseUpdate, StudybloxxDBHelper.Contract.Course.ID + " IN (" + courseIdsJoined + ")", null);
            } else {
                Log.d(TAG, "No new Course available for upload");
            }

        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
