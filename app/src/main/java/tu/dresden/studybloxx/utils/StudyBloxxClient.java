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
		client.get(addHostToURL(url), params, handler);
	}

	public static void post(String url, RequestParams params,
			AsyncHttpResponseHandler handler) {
		String completeURL = addHostToURL(url);
		Log.d(TAG, "Complete POST Url: " + completeURL);
		client.post(completeURL, params, handler);
	}

	private static String addHostToURL(String url) {
		return Constants.STUDYBLOXX_HOST + url;
	}

}
