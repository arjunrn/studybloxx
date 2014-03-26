package tu.dresden.studybloxx.syncadapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import tu.dresden.studybloxx.authentication.StudybloxxAuthentication;

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
    public void onPerformSync(Account account, Bundle bundle, String authority, ContentProviderClient providerClient, SyncResult syncResult) {
        boolean uploadOnly = bundle.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD);
        Log.d(TAG, "Upload Only: " + uploadOnly);
        try {
            final String authToken = mAccountMan.blockingGetAuthToken(account, StudybloxxAuthentication.AUTHTOKEN_TYPE_FULL_ACCESS, true);
            Log.d(TAG, "Auth Token" + authToken);

        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        }
    }
}
