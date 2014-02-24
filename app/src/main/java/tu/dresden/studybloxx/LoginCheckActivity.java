package tu.dresden.studybloxx;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;

import tu.dresden.studybloxx.services.SyncService;


public class LoginCheckActivity extends Activity
{

	private static final String USER_SESSION_TOKEN = "tu.dresden.studybloxx.USER_SESSION_TOKEN";
	private static final String USER_SESSION_EXPIRY_DATE = "tu.dresden.studybloxx.USER_SESSION_EXPIRY_DATE";


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login_check, menu);
		return true;
	}


	@Override
	protected void onStart()
	{
		SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
		String userSessionToken = preferences.getString(USER_SESSION_TOKEN, null);
		long expiryTime = preferences.getLong(USER_SESSION_EXPIRY_DATE, System.currentTimeMillis() - 20);
		if (userSessionToken != null && expiryTime > System.currentTimeMillis())
		{
			Intent syncService = new Intent(this, SyncService.class);
			startService(syncService);

			Intent continueIntent = new Intent(this, NoteListActivity.class);
			startActivity(continueIntent);
			finish();
		}
		else
		{
			Intent loginIntent = new Intent(this, LoginActivity.class);
			startActivity(loginIntent);
			finish();
		}
		super.onStart();
	}

}
