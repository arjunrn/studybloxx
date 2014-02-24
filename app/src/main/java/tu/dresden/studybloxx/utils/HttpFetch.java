package tu.dresden.studybloxx.utils;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpFetch
{
	private static final String TAG = "HttpsFetch";

	private final static int CONNECT_TIMEOUT = 5000;
	private final static int SOCKET_TIMEOUT = 120000;


	public static String get(String url)
	{
		long time;
		long bgCounter = System.currentTimeMillis();
		long startMoment = bgCounter;

		time = System.currentTimeMillis();
		Log.d(TAG, "Time to setup socket factory: " + (time - bgCounter) / 1000.0);
		bgCounter = time;

		try
		{
			URL downloadUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
			connection.setConnectTimeout(CONNECT_TIMEOUT);
			connection.setReadTimeout(SOCKET_TIMEOUT);

			time = System.currentTimeMillis();
			Log.d(TAG, "Time to open connection: " + (time - bgCounter) / 1000.0);
			bgCounter = time;

			// connection.addRequestProperty("accept-encoding", "gzip");
			connection.connect();

			time = System.currentTimeMillis();
			Log.d(TAG, "Time to connect: " + (time - bgCounter) / 1000.0);
			bgCounter = time;

			StringBuilder responseBuilder = new StringBuilder(64 * 1024);
			InputStream input = connection.getInputStream();

			InputStreamReader reader = new InputStreamReader(input);
			char[] buffer = new char[128 * 1024];
			int count;
			while ((count = reader.read(buffer)) != -1)
			{
				responseBuilder.append(buffer, 0, count);
			}

			time = System.currentTimeMillis();
			Log.d(TAG, "Time to read from input stream: " + (time - bgCounter) / 1000.0);
			bgCounter = time;

			String responseString = responseBuilder.toString();
			Log.d(TAG, "File Size: " + responseString.length());

			time = System.currentTimeMillis();
			Log.d(TAG, "Time to build response String: " + (time - bgCounter) / 1000.0);
			bgCounter = time;

			Log.d(TAG, "Total Time to Fetch and Create Response: " + (bgCounter - startMoment) / 1000.0);
			return responseString;
		}
		catch (IOException e)
		{
			Log.d(TAG, "IO Exception Occurred");
			e.printStackTrace();
		}
		return null;
	}

}
