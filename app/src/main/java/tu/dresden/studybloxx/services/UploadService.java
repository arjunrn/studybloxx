package tu.dresden.studybloxx.services;

import tu.dresden.studybloxx.utils.CourseUploadTask;
import tu.dresden.studybloxx.utils.Helper;
import tu.dresden.studybloxx.utils.NoteUploadTask;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class UploadService extends Service
{
	private static final String TAG = "UploadService";
	private ContentResolver mResolver;
	private Context mContext;


	public UploadService()
	{
	}


	@Override
	public void onCreate()
	{
		super.onCreate();
		mResolver = getApplicationContext().getContentResolver();
		mContext = getApplicationContext();
	}


	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.d(TAG, "Received on Start Command");
		new ServiceCourseUploadTask(mContext).execute();
		return START_NOT_STICKY;
	};


	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	class ServiceCourseUploadTask extends CourseUploadTask
	{

		public ServiceCourseUploadTask(Context context)
		{
			super(context);
		}


		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			if (result)
			{
				new ServiceNoteUploadTask(mContext, Helper.getStoredUserName(mContext), Helper.getStoredPassword(mContext)).execute();
			}
		}

	}

	class ServiceNoteUploadTask extends NoteUploadTask
	{

		public ServiceNoteUploadTask(Context context, String username, String password)
		{
			super(context, username, password);
		}

	}
}
