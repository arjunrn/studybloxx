package tu.dresden.studybloxx.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Arjun Naik on 26.03.14.
 */
public class StudybloxxSyncService extends Service {
    private final static Object mSyncServiceLock = new Object();
    private static final String TAG = "StudybloxxSyncService";
    private StudybloxxSyncAdapter mSyncAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (mSyncServiceLock) {
            if (mSyncAdapter == null) {
                mSyncAdapter = new StudybloxxSyncAdapter(this, true, true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind called");
        return mSyncAdapter.getSyncAdapterBinder();
    }
}
