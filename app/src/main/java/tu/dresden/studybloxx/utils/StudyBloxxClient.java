package tu.dresden.studybloxx.utils;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class StudyBloxxClient {
    private static final String TAG = "StudyBloxxClient";
    static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params,
                           AsyncHttpResponseHandler handler) {
        client.get(url, params, handler);
    }

    public static void post(String url, RequestParams params,
                            AsyncHttpResponseHandler handler) {
        client.post(url, params, handler);
    }


}
