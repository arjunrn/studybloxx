package tu.dresden.studybloxx;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import tu.dresden.studybloxx.authentication.StudybloxxAuthentication;
import tu.dresden.studybloxx.utils.AuthTokenFetcher;
import tu.dresden.studybloxx.utils.Constants;


/**
 * Activity which displays a login screen to the user, offering registration as well.
 * Code adapted from <a href="https://github.com/Udinic/AccountAuthenticator/blob/master/src/com/udinic/accounts_authenticator_example/authentication/AuthenticatorActivity.java">Udinic Authenticator Activity</a>
 */
public class LoginActivity extends AccountAuthenticatorActivity {

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";
    /**
     * The default email to populate the email field with.
     */
    public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
    public static final String LOGIN_PREFERENCES = "login_preferences";
    private static final String PARAM_CSRF_TOKEN = "CSRF_TOKEN";
    private static final String TAG = "LoginActivity";
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
    private String mServerAddress;
    private AccountManager mAccountManager;
    private String mAuthTokenType;
    private String mAccountType;
    private EditText mServerAddressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
        mEmailView = (EditText) findViewById(R.id.email);
        mEmailView.setText(mEmail);

        mPasswordView = (EditText) findViewById(R.id.password);

        mServerAddressView = (EditText) findViewById(R.id.server_address);
        mServerAddressView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mAccountManager = AccountManager.get(getBaseContext());

        Intent intent = getIntent();
        String accountName = intent.getStringExtra(ARG_ACCOUNT_NAME);
        mAuthTokenType = intent.getStringExtra(ARG_AUTH_TYPE);
        mAccountType = intent.getStringExtra(ARG_ACCOUNT_TYPE);
        if (mAuthTokenType == null)
            mAuthTokenType = StudybloxxAuthentication.AUTHTOKEN_TYPE_FULL_ACCESS;

        if (accountName != null) {
            mEmailView.setText(accountName);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form. If there are form errors (invalid email, missing fields, etc.), the errors are
     * presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mServerAddressView.setError(null);

        // Store values at the time of the login attempt.
        mEmail = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();
        mServerAddress = mServerAddressView.getText().toString();

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

        //TODO: Implemented better checks for valid server address
        if (TextUtils.isEmpty(mServerAddress)) {
            mServerAddressView.setError("This field is required");
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

            new LoginTask().execute();

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
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void finishLogin(Intent intent) {
        if (!intent.hasExtra(KEY_ERROR_MESSAGE)) {
            String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            Log.d(TAG, "Account NAME" + accountName);
            String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
            String accountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
            Log.d(TAG, "Account Type: " + accountType);
            final Account account = new Account(accountName, accountType);
            Log.d(TAG, "Account Name:" + account.name);
            if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
                String authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
                String authTokenType = mAuthTokenType;

                // Creating the account on the device and setting the auth token we got
                // (Not setting the auth token will cause another call to the server to authenticate the user)
                final Bundle accountBundle = new Bundle();
                accountBundle.putString(StudybloxxAuthentication.CSRF_TOKEN, intent.getStringExtra(PARAM_CSRF_TOKEN));

                mAccountManager.addAccountExplicitly(account, accountPassword, accountBundle);
                mAccountManager.setAuthToken(account, authTokenType, authToken);
                ContentResolver.setSyncAutomatically(account, getString(R.string.provider_authority), true);
            } else {
                mAccountManager.setPassword(account, accountPassword);
            }

            setAccountAuthenticatorResult(intent.getExtras());
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(this, intent.getStringExtra(KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
            mEmailView.requestFocus();
        }
    }



    class LoginTask extends AsyncTask<Void, Void, AuthTokenFetcher.LoginReply> {

        @Override
        protected AuthTokenFetcher.LoginReply doInBackground(Void... voids) {
            AuthTokenFetcher fetcher = new AuthTokenFetcher(mServerAddress,mEmail,mPassword);
            return fetcher.getToken();
        }


        @Override
        protected void onPostExecute(AuthTokenFetcher.LoginReply reply) {
            super.onPostExecute(reply);
            final Bundle data = new Bundle();

            if (!reply.result) {
                if (reply.exception != null) {
                    Log.e(TAG, "Login Task Failed");
                    reply.exception.printStackTrace();
                    Toast.makeText(getApplicationContext(), reply.exception.getMessage(), Toast.LENGTH_SHORT).show();
                    data.putString(KEY_ERROR_MESSAGE, reply.exception.getMessage());
                } else {
                    data.putString(KEY_ERROR_MESSAGE, "Login Successful but no authentication data.");
                }
            } else {
                data.putString(AccountManager.KEY_ACCOUNT_NAME, mEmail);
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, mAccountType);
                data.putString(AccountManager.KEY_AUTHTOKEN, reply.authToken);
                data.putString(PARAM_CSRF_TOKEN, reply.csrfToken);
                data.putString(PARAM_USER_PASS, mPassword);
            }

            final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
            final SharedPreferences.Editor sharedEdit = sharedPrefs.edit();
            sharedEdit.putString("sync_server_address", mServerAddress);
            sharedEdit.commit();

            final Intent res = new Intent();
            res.putExtras(data);
            finishLogin(res);
            showProgress(false);
        }
    }
}
