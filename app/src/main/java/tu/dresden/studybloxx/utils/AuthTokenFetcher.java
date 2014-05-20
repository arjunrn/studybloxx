package tu.dresden.studybloxx.utils;

import android.util.Log;

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

/**
 * Created by arjun on 15.05.14.
 */
public class AuthTokenFetcher {

    private static final String TAG = "AuthTokenFetcher";
    private final String mServerAddress;
    private final String mPassword;
    private final String mEmail;

    public AuthTokenFetcher(String serverAddress, String email, String password) {
        mServerAddress = serverAddress;
        mEmail = email;
        mPassword = password;
    }

    public LoginReply getToken() {
        HttpClient httpclient = new DefaultHttpClient();
        String loginURL = String.format(Constants.LOGIN_URL, mServerAddress);
        Log.d(TAG, "Login URL: " + loginURL);
        HttpPost httppost = new HttpPost(loginURL);
        LoginReply reply = new LoginReply();
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
            nameValuePairs.add(new BasicNameValuePair("email", mEmail));
            nameValuePairs.add(new BasicNameValuePair("password", mPassword));
            nameValuePairs.add(new BasicNameValuePair("keep_logged_in", "true"));
            nameValuePairs.add(new BasicNameValuePair("mobile", "true"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpclient.execute(httppost);

            StringBuilder sb = new StringBuilder();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 65728);
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            final Header[] allHeaders = response.getAllHeaders();
            for (Header h : allHeaders) {
                Log.d(TAG, h.getName() + " : " + h.getValue());
            }

            final Header[] cookieHeaders = response.getHeaders("Set-Cookie");
            Log.d(TAG, "Number of Cookie Headers: " + cookieHeaders.length);

            String token = null, sessionID = null;

            for (Header h : cookieHeaders) {
                Log.d(TAG, h.getName() + " : " + h.getValue());
                if (h.getName().equals("Set-Cookie")) {
                    String[] cookiePairs = h.getValue().split(";");
                    String[] firstPair = cookiePairs[0].split("=");
                    if ("csrftoken".equals(firstPair[0])) {
                        token = firstPair[1];
                        Log.d(TAG, "CSRF Token: " + token);
                    }
                    if ("sessionid".equals(firstPair[0])) {
                        sessionID = firstPair[1];
                        Log.d(TAG, "Session ID: " + sessionID);
                    }

                }
            }

            if (token == null || sessionID == null) {
                reply.authToken = "";
                reply.result = false;
            } else {
                reply.result = true;
                reply.authToken = sessionID;
                reply.csrfToken = token;
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
            reply.result = false;
            reply.exception = e;
        } catch (IOException e) {
            reply.result = false;
            reply.exception = e;
        }
        return reply;
    }

    public class LoginReply {
        public boolean result;
        public String authToken;
        public String csrfToken;
        public Exception exception;
    }
}
