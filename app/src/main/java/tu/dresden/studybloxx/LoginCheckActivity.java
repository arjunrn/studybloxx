package tu.dresden.studybloxx;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import java.io.IOException;

import tu.dresden.studybloxx.authentication.StudybloxxAuthentication;


public class LoginCheckActivity extends Activity {

    private static final String USER_SESSION_TOKEN = "tu.dresden.studybloxx.USER_SESSION_TOKEN";
    private static final String USER_SESSION_EXPIRY_DATE = "tu.dresden.studybloxx.USER_SESSION_EXPIRY_DATE";
    private AccountManager mAccountMan;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_check, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountMan = AccountManager.get(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Account[] accounts = mAccountMan.getAccountsByType(StudybloxxAuthentication.ACCOUNT_TYPE);

        if (accounts.length == 0) {

            final AccountManagerFuture<Bundle> addResult = mAccountMan.addAccount(StudybloxxAuthentication.ACCOUNT_TYPE, StudybloxxAuthentication.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, null, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    Bundle result;
                    try {
                        result = future.getResult();
                    } catch (OperationCanceledException e) {
                        e.printStackTrace();
                        finish();
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                        finish();
                        return;
                    } catch (AuthenticatorException e) {
                        e.printStackTrace();
                        finish();
                        return;
                    }
                    if (result.containsKey(AccountManager.KEY_INTENT)) {
                        startActivity(result.<Intent>getParcelable(AccountManager.KEY_INTENT));
                    } else {
                        String accName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                        String accType = result.getString(AccountManager.KEY_ACCOUNT_TYPE);
                        getAuthToken(new Account(accName, accType));
                    }
                }
            }, null);

        } else {
            getAuthToken(accounts[0]);
        }


    }


    private void getAuthToken(Account account) {
        final AccountManagerFuture<Bundle> authTokenBundle = mAccountMan.getAuthToken(account, StudybloxxAuthentication.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                Bundle tokenResult = null;
                try {
                    tokenResult = future.getResult();
                } catch (OperationCanceledException e) {
                    e.printStackTrace();
                    finish();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    finish();
                    return;
                } catch (AuthenticatorException e) {
                    e.printStackTrace();
                    finish();
                    return;
                }

                String authToken = tokenResult.getString(AccountManager.KEY_AUTHTOKEN, null);

                finishLogin(authToken);
            }
        }, null);

    }

    ;

    private void finishLogin(String authToken) {
        if (authToken == null) {
            Toast.makeText(this, "Could not get Auth Token", Toast.LENGTH_SHORT).show();
        } else {
            Intent lectureListIntent = new Intent(this, NoteListActivity.class);
            startActivity(lectureListIntent);
        }

        finish();
        return;

    }
}
