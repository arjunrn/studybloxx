package tu.dresden.studybloxx.authentication;

import android.content.Context;

import tu.dresden.studybloxx.R;

/**
 * Created by Arjun Naik on 23.03.14.
 */
public class StudybloxxAuthentication {

    /**
     * Account name
     */
    public static final String ACCOUNT_NAME = "Studybloxx";
    /**
     * Auth token types
     */
    public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an Studybloxx account";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an Studybloxx account";

    public static String getAuthority(Context context) {
        return context.getString(R.string.account_authority);
    }
}
