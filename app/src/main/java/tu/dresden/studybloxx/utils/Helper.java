package tu.dresden.studybloxx.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import tu.dresden.studybloxx.LoginActivity;
import android.content.Context;
import android.content.SharedPreferences;


public class Helper
{
	public static void logout(Context c)
	{
		SharedPreferences loginPrefs = c.getSharedPreferences(LoginActivity.LOGIN_PREFERENCES, Context.MODE_PRIVATE);
		loginPrefs.edit().clear().commit();
	}


	public static String getStoredUserName(Context c) throws IllegalStateException
	{
		SharedPreferences loginPrefs = c.getSharedPreferences(LoginActivity.LOGIN_PREFERENCES, Context.MODE_PRIVATE);
		String username = loginPrefs.getString(Constants.STUDYBLOXX_USERNAME, null);
		if (username == null)
			throw new IllegalStateException("The username has not been store in preferences");
		return username;
	}


	public static String getStoredPassword(Context c) throws IllegalStateException
	{
		SharedPreferences loginPrefs = c.getSharedPreferences(LoginActivity.LOGIN_PREFERENCES, Context.MODE_PRIVATE);
		String password = loginPrefs.getString(Constants.STUDYBLOXX_PASSWORD, null);
		if (password == null)
			throw new IllegalStateException("The username has not been store in preferences");
		return password;
	}


	public static String inputStreamToString(InputStream is) throws IOException
	{
		String line = "";
		StringBuilder total = new StringBuilder();

		// Wrap a BufferedReader around the InputStream
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));

		// Read response until the end
		while ((line = rd.readLine()) != null)
		{
			total.append(line);
		}

		// Return full string
		return total.toString();
	}
}
