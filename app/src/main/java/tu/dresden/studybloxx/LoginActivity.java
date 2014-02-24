package tu.dresden.studybloxx;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import tu.dresden.studybloxx.services.SyncService;
import tu.dresden.studybloxx.utils.Constants;
import tu.dresden.studybloxx.utils.StudyBloxxClient;


/**
 * Activity which displays a login screen to the user, offering registration as well.
 */
public class LoginActivity extends Activity
{

	private static final String TAG = "LoginActivity";

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	public static final String LOGIN_PREFERENCES = "login_preferences";

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	private SharedPreferences mLoginPreferences;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		mLoginPreferences = getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE);

		String storedUsername = mLoginPreferences.getString(Constants.STUDYBLOXX_USERNAME, null);
		String storedPassword = mLoginPreferences.getString(Constants.STUDYBLOXX_PASSWORD, null);

		if (storedUsername != null && storedPassword != null)
		{
			Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show();

			Intent syncService = new Intent(this, SyncService.class);
			startService(syncService);

			Intent lectureListIntent = new Intent(this, NoteListActivity.class);
			startActivity(lectureListIntent);
			finish();
			return;
		}

		// Set up the login form.
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
			{
				if (id == R.id.login || id == EditorInfo.IME_NULL)
				{
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				attemptLogin();
			}
		});
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}


	/**
	 * Attempts to sign in or register the account specified by the login form. If there are form errors (invalid email, missing fields, etc.), the errors are
	 * presented and no actual login attempt is made.
	 */
	public void attemptLogin()
	{

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword))
		{
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}
		else if (mPassword.length() < 4)
		{
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail))
		{
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		}
		else if (!mEmail.contains("@"))
		{
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel)
		{
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		}
		else
		{
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			RequestParams loginParams = new RequestParams();
			loginParams.add("username", mEmail);
			loginParams.add("password", mPassword);
			StudyBloxxClient.get("/bloxxdata/login/", loginParams, loginCheckHandler);
		}
	}


	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show)
	{
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
		{
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});
		}
		else
		{
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	AsyncHttpResponseHandler loginCheckHandler = new AsyncHttpResponseHandler()
	{
		public void onFailure(int statusCode, org.apache.http.Header[] headers, Throwable error, String content)
		{
			Log.e(TAG, "Failed to call the login handler");
			Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
			showProgress(false);
		};


		public void onSuccess(int statusCode, org.apache.http.Header[] headers, String content)
		{
			try
			{
				JSONObject responseJSON = new JSONObject(content);
				boolean loginResult = responseJSON.getBoolean("result");
				if (loginResult)
				{
					Editor loginPrefsEditor = mLoginPreferences.edit();
					loginPrefsEditor.putString(Constants.STUDYBLOXX_USERNAME, mEmail);
					loginPrefsEditor.putString(Constants.STUDYBLOXX_PASSWORD, mPassword);
					loginPrefsEditor.commit();

					Intent syncService = new Intent(LoginActivity.this, SyncService.class);
					startService(syncService);

					Toast.makeText(getApplicationContext(), "Login Succeeded", Toast.LENGTH_SHORT).show();
					Intent lectureActivity = new Intent(getApplicationContext(), NoteListActivity.class);
					startActivity(lectureActivity);
					finish();

					return;
				}
				else
				{
					mEmailView.requestFocus();
				}
			}
			catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			showProgress(false);
		};
	};


	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_registration:
				Intent registrationActivity = new Intent(this, RegistrationActivity.class);
				startActivity(registrationActivity);
		}
		return super.onMenuItemSelected(featureId, item);
	}
}
