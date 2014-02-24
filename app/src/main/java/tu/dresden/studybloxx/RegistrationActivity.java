package tu.dresden.studybloxx;

import org.json.JSONException;
import org.json.JSONObject;

import tu.dresden.studybloxx.utils.StudyBloxxClient;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class RegistrationActivity extends Activity {

	private static final String TAG = "RegistrationActivity";

	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[] {
			"foo@example.com:hello", "bar@example.com:world" };

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;
	private String mRePass;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private EditText mRePassView;
	private View mRegistrationFormView;
	private View mRegistrationStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_registration);

		// Set up the login form.
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptRegistration();
							return true;
						}
						return false;
					}
				});

		mRePassView = (EditText) findViewById(R.id.reenter_password);

		mRegistrationFormView = findViewById(R.id.registration_form);
		mRegistrationStatusView = findViewById(R.id.registration_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.registration_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptRegistration();
					}
				});
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptRegistration() {

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mRePass = mRePassView.getText().toString();
		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			doRegistration();
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mRegistrationStatusView.setVisibility(View.VISIBLE);
			mRegistrationStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mRegistrationStatusView
									.setVisibility(show ? View.VISIBLE
											: View.GONE);
						}
					});

			mRegistrationFormView.setVisibility(View.VISIBLE);
			mRegistrationFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mRegistrationFormView
									.setVisibility(show ? View.GONE
											: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mRegistrationStatusView.setVisibility(show ? View.VISIBLE
					: View.GONE);
			mRegistrationFormView
					.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	AsyncHttpResponseHandler registrationHandler = new AsyncHttpResponseHandler() {
		public void onFailure(int statusCode, org.apache.http.Header[] headers,
				Throwable error, String content) {
			if (error != null) {
				Log.d(TAG, error.getMessage());
			}
			if (content != null) {
				Log.d(TAG, content);
			}
			Log.d(TAG, "Registration Call Failed");
		};

		public void onSuccess(int statusCode, org.apache.http.Header[] headers,
				String content) {
			Log.d(TAG, "Registration Call Succeeded");
			try {
				JSONObject registrationResponse = new JSONObject(content);
				boolean registrationResult = registrationResponse
						.getBoolean("result");
				if (registrationResult) {
					Toast.makeText(getApplicationContext(),
							"Registration Succeeeded", Toast.LENGTH_SHORT)
							.show();
					finish();
					return;
				} else {
					String failureReason = registrationResponse
							.getString("reason");
					Toast.makeText(getApplicationContext(), failureReason,
							Toast.LENGTH_SHORT).show();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			showProgress(false);
		};
	};

	void doRegistration() {

		RequestParams registrationParams = new RequestParams();
		registrationParams.add("username", mEmail);
		registrationParams.add("password", mPassword);
		registrationParams.add("repass", mRePass);
		showProgress(true);
		StudyBloxxClient.post("/bloxxdata/register-user/", registrationParams,
				registrationHandler);
	}
}
