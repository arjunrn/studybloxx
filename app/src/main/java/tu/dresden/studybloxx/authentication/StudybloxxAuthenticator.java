package tu.dresden.studybloxx.authentication;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import tu.dresden.studybloxx.LoginActivity;
import tu.dresden.studybloxx.R;
import tu.dresden.studybloxx.utils.AuthTokenFetcher;
import tu.dresden.studybloxx.utils.Constants;

/**
 * Created by Arjun Naik<arjun@arjunnaik.in> on 22.03.14.
 * Code is mostly adapted from <a href="https://github.com/Udinic/AccountAuthenticator/blob/master/src/com/udinic/accounts_authenticator_example/authentication/UdinicAuthenticator.java">Udinic Example</a>
 */
public class StudybloxxAuthenticator extends AbstractAccountAuthenticator {
    private final String TAG = this.getClass().getName();
    private final Context mContext;

    public StudybloxxAuthenticator(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        throw new UnsupportedOperationException("Cannot modify properties");
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "Called Add Account");
        Intent loginIntent = new Intent(mContext, LoginActivity.class);
        loginIntent.putExtra(LoginActivity.ARG_ACCOUNT_TYPE, accountType);
        Log.d(TAG, "Account Type:" + accountType);
        loginIntent.putExtra(LoginActivity.ARG_AUTH_TYPE, authTokenType);
        loginIntent.putExtra(LoginActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
        loginIntent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, loginIntent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String authTokenType, Bundle bundle) throws NetworkErrorException {
        final Bundle returnBundle = new Bundle();

        if (!authTokenType.equals(StudybloxxAuthentication.AUTHTOKEN_TYPE_READ_ONLY) && !authTokenType.equals(StudybloxxAuthentication.AUTHTOKEN_TYPE_FULL_ACCESS)) {
            returnBundle.putString(AccountManager.KEY_ERROR_MESSAGE, "Invalid authTokenType");
            return returnBundle;
        }

        final AccountManager accountManager = AccountManager.get(mContext);
        String authToken = accountManager.peekAuthToken(account, authTokenType);

        if (TextUtils.isEmpty(authToken)) {
            final String password = accountManager.getPassword(account);
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            String serverAddress = prefs.getString("sync_server_address", Constants.STUDYBLOXX_DEFAULT_SERVER_ADDRESS);
            if (password != null) {
                AuthTokenFetcher fetcher = new AuthTokenFetcher(serverAddress, account.name, password);
                final AuthTokenFetcher.LoginReply token = fetcher.getToken();
                if(token.result){
                    authToken = token.authToken;
                }
            }
        }

        if (!TextUtils.isEmpty(authToken)) {
            Log.d(TAG, "Auth Token is not empty");
            returnBundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            returnBundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            returnBundle.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return returnBundle;
        }

        Log.e(TAG, "Need to put intent for Login");

        final Intent authenticationIntent = new Intent(mContext, LoginActivity.class);
        authenticationIntent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
        authenticationIntent.putExtra(LoginActivity.ARG_ACCOUNT_TYPE, account.type);
        authenticationIntent.putExtra(LoginActivity.ARG_ACCOUNT_NAME, account.name);
        authenticationIntent.putExtra(LoginActivity.ARG_AUTH_TYPE, authTokenType);
        returnBundle.putParcelable(AccountManager.KEY_INTENT, authenticationIntent);

        return returnBundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (StudybloxxAuthentication.AUTHTOKEN_TYPE_FULL_ACCESS.equals(authTokenType)) {
            return StudybloxxAuthentication.AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
        } else if (StudybloxxAuthentication.AUTHTOKEN_TYPE_READ_ONLY.equals(authTokenType)) {
            return StudybloxxAuthentication.AUTHTOKEN_TYPE_READ_ONLY_LABEL;
        } else {
            throw new UnsupportedOperationException("This Authorization Token is not recognized: " + authTokenType);
        }
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        final Bundle bundle = new Bundle();
        bundle.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return bundle;
    }

    @Override
    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
        if (mContext.getString(R.string.account_authority).equals(account.type)) {
            final Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
            return result;
        }
        return super.getAccountRemovalAllowed(response, account);
    }
}
