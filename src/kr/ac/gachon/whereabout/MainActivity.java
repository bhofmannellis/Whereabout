package kr.ac.gachon.whereabout;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

/*
 * Main activity is for sign up, sign in.
 * After sign up or sign in successful, start activity MainPageActivity.java.
 */

/**
 * This is an application that offer service based on location-sharing. Content
 * is a Life style application and people can share their location and protect
 * loved ones. Application name is a 'Whereabout'. The key functions is that
 * making a friend list automatically, sharing own location, and notifying to
 * other users using Service component and Notification. The goal of our
 * application is that share location with friend, family, and sweetheart for
 * safety, fun, and convenience.
 * 
 * 
 * @author Team6 :: Blaise, Ra Hyungjin, Kim Kideok
 * @version 1.0
 * @since 10. June. 2015
 * @email doubleknd26@gmail.com
 *
 */
public class MainActivity extends FragmentActivity implements OnClickListener {

	// Instance variables for main activity.

	// Bottom variables are button.

	private Button signUp;
	private Button signIn;
	// For sign in.

	private String Id;
	private String pw;;
	// For sign up.
	// Bottom variables are take user information inserted by user.

	private String UId;
	private String UPw;
	private String USnum;
	private String UPhone;
	private String UName;
	private String UEmail;

	private boolean check;
	private String result;

	/**
	 * This is a onCreate.
	 * 
	 * @param Bundle
	 *            savedInstanceState.
	 * @return No return.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		RelativeLayout lay = (RelativeLayout) findViewById(R.id.relat);
		lay.setBackgroundResource(R.drawable.whereabout);

		signUp = (Button) findViewById(R.id.signUp);
		signIn = (Button) findViewById(R.id.signIn);

		// set up the listener
		signUp.setOnClickListener(this);
		signIn.setOnClickListener(this);
	}

	/**
	 * This is a onResume.
	 * 
	 * @param No
	 *            parameter.
	 * @return No return.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		// For using location service, check the state of user's device.
		checkLocationServices();
	}

	/**
	 * onClick is called when user click the button signUp or signIn.
	 * 
	 * @param View
	 *            v for widget.
	 * @return No return.
	 */
	public void onClick(View v) {

		if (v.getId() == R.id.signUp) {
			SignUpDialog().show();
		} else if (v.getId() == R.id.signIn) {
			SignInDialog().show();
		}
	}

	/**
	 * checkLocationServices is to check whether or not location service is
	 * possible.
	 * 
	 * @param No
	 *            parameter.
	 * @return No return.
	 */
	private void checkLocationServices() {
		// Check if location is enabled. If not, launch dialog giving option of
		// opening location settings or quitting
		if (!isLocationEnabled(getApplicationContext())) {

			// Alert Dialog code adapted form here:
			// http://stackoverflow.com/questions/2115758/how-to-display-alert-dialog-in-android
			AlertDialog.Builder builder = new AlertDialog.Builder(
					MainActivity.this);
			builder.setMessage("Whereabout requires that location settings are enabled");
			builder.setCancelable(true);
			// Give option of opening location settings
			builder.setPositiveButton("Open Location Settings",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
							startActivity(new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS));
						}
					});
			builder.setNegativeButton("Quit",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
							finish();
						}
					});

			AlertDialog alert11 = builder.create();
			alert11.show();
		}
	}

	/**
	 * Checks if user has enabled location services on the device. Adapted from
	 * here:
	 * http://stackoverflow.com/questions/10311834/how-to-check-if-location
	 * -services-are-enabled
	 *
	 * @param context
	 * @return true or false.
	 */
	public static boolean isLocationEnabled(Context context) {
		int locationMode = 0;
		String locationProviders;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			try {
				locationMode = Settings.Secure.getInt(
						context.getContentResolver(),
						Settings.Secure.LOCATION_MODE);
			} catch (SettingNotFoundException e) {
				Log.e("MainActivity","SettingNotFoundException");
				e.printStackTrace();
				return false;
			}
			return locationMode != Settings.Secure.LOCATION_MODE_OFF;
		} else {
			locationProviders = Settings.Secure.getString(
					context.getContentResolver(),
					Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			return !TextUtils.isEmpty(locationProviders);
		}
	}

	// This method is to check id and pw for sing in.
	/**
	 * SignInDialog make a dialog for sign in.
	 * 
	 * @param No
	 *            parameter.
	 * @return No return.
	 */
	public Dialog SignInDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Get the layout inflater
		LayoutInflater inflater = this.getLayoutInflater();
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		final View dialogView = inflater.inflate(R.layout.dialog_signin, null);
		final EditText inUserId = (EditText) dialogView
				.findViewById(R.id.userIdField);
		final EditText inUserPw = (EditText) dialogView
				.findViewById(R.id.userPwField);
		builder.setView(dialogView)
				// Add action buttons
				.setNegativeButton("SignIn",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// sign in the user ...
								// Log.d("onClick, ", inUserPw + "111");
								UId = inUserId.getText().toString();
								UPw = inUserPw.getText().toString();
								MyThread object = new MyThread();
								Thread thr1 = new Thread(object);
								result = "signIn";
								thr1.start();
							}
						})
				.setPositiveButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		return builder.create();
	}

	/**
	 * SignUpDialog make a dialog for sign up.
	 * 
	 * @param No
	 *            parameter.
	 * @return No return.
	 */
	public Dialog SignUpDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Get the layout inflater
		LayoutInflater inflater = this.getLayoutInflater();
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		final View dialogView = inflater.inflate(R.layout.dialog_signup, null);
		final EditText UserId = (EditText) dialogView
				.findViewById(R.id.suserIdField);
		final EditText UserPw = (EditText) dialogView
				.findViewById(R.id.suserPwField);
		final EditText UserName = (EditText) dialogView
				.findViewById(R.id.userNameField);
		final EditText UserSnum = (EditText) dialogView
				.findViewById(R.id.userSnumField);
		final EditText UserEmail = (EditText) dialogView
				.findViewById(R.id.userEmailField);
		final EditText UserPhone = (EditText) dialogView
				.findViewById(R.id.userPhoneNumField);
		builder.setView(dialogView)
				// Add action buttons
				.setNegativeButton("SignUp",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// sign in the user ...

								UId = UserId.getText().toString();
								UPw = UserPw.getText().toString();
								UName = UserName.getText().toString();
								USnum = UserSnum.getText().toString();
								UEmail = UserEmail.getText().toString();
								UPhone = UserPhone.getText().toString();
								MyThread object = new MyThread();
								Thread thr1 = new Thread(object);
								result = "signUp";
								thr1.start();
							}
						})
				.setPositiveButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		return builder.create();
	}

	/**
	 * checkSignIn is called when user try to sign in.
	 *
	 * @param No
	 *            parameter.
	 * @return No return.
	 */
	protected void checkSignIn() {

		// Log.d(getClass().getName(), " Start name value pair");
		// Log.d("ID", UId);
		// Log.d("PW", UPw);

		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();

		post.add(new BasicNameValuePair("ID", UId));
		post.add(new BasicNameValuePair("PW", UPw));

		// �?��?�옙�?��?�옙 HttpClient �?��?�옙체 �?��?�옙�?��?�옙
		HttpClient client = new DefaultHttpClient();

		// �?��?�옙체 �?��?�옙�?��?�옙 �?��?�옙�?��?�옙 �?�싸�?옙, �?��?�옙�?��?�옙 �?��?�?옙챨�?�� �?��?�옙�?��
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000);

		// Post�?��?�옙체 �?��?�옙�?��?�옙
		HttpPost httpPost = new HttpPost("http://kideok.linux2.dude.kr"
				+ "/signIn.php?");

		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(post,
					"utf-8");
			httpPost.setEntity(entity);

			// return EntityUtils.getContentCharSet(entity);
			HttpResponse res = client.execute(httpPost);

			String txt = EntityUtils.toString((res.getEntity()));
			// Log.d("txt", txt);

			String[] result = txt.split("/");

			result[0] = result[0].trim();

			if (result[0].equalsIgnoreCase("valid")) {
				check = true;
			} else {
				check = false;
			}

		} catch (ClientProtocolException e) {
			Log.e("MainActivity","ClientProtocolException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("MainActivity","IOException");
			e.printStackTrace();
		}
	}

	/**
	 * checkSignUp is called when user try to sign up.
	 * 
	 * @param No
	 *            parameter.
	 * @return No return.
	 */
	protected void checkSignUp() {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();

		post.add(new BasicNameValuePair("ID", UId));
		post.add(new BasicNameValuePair("PW", UPw));
		post.add(new BasicNameValuePair("NAME", UName));
		post.add(new BasicNameValuePair("SNUM", USnum));
		post.add(new BasicNameValuePair("EMAIL", UEmail));
		post.add(new BasicNameValuePair("PHONE", UPhone));
		post.add(new BasicNameValuePair("LONGITUDE", "0.0"));
		post.add(new BasicNameValuePair("LATITUDE", "0.0"));

		// �?��?�옙�?��?�옙 HttpClient �?��?�옙체 �?��?�옙�?��?�옙
		HttpClient client = new DefaultHttpClient();

		// �?��?�옙체 �?��?�옙�?��?�옙 �?��?�옙�?��?�옙 �?�싸�?옙, �?��?�옙�?��?�옙 �?��?�?옙챨�?�� �?��?�옙�?��
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000);

		// Post�?��?�옙체 �?��?�옙�?��?�옙
		HttpPost httpPost = new HttpPost("http://kideok.linux2.dude.kr"
				+ "/signUp.php?");

		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(post,
					"utf-8");
			httpPost.setEntity(entity);

			// return EntityUtils.getContentCharSet(entity);
			HttpResponse res = client.execute(httpPost);

			String txt = EntityUtils.toString((res.getEntity()));

			txt = txt.trim();
			// Log.i("wa", txt);
			// Log.i("wa", Integer.toString(txt.length()));

			if (txt.equals("success")) {
				check = true;
			} else {
				check = false;
			}
		} catch (ClientProtocolException e) {
			Log.e("MainActivity","ClientProtocolException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("MainActivity","IOException");
			e.printStackTrace();
		}
	}

	/**
	 * This is a onCreateOptionsMenu.
	 * 
	 * @param Menu
	 *            menu.
	 * @return No return.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * This is a onOptionsItemSelected.
	 * 
	 * @param MenuItem
	 *            item.
	 * @return No return.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class MyThread implements Runnable {
		public void run() {

			if (result.equalsIgnoreCase("signIn")) {
				checkSignIn();
				if (check) {
					// Go to the MainPageActivitiy
					Intent intent = new Intent(getApplicationContext(),
							MainPageActivity.class);
					intent.putExtra("idInfo", UId);

					startActivity(intent);
					finish();
					

				} else {

					Toast.makeText(getApplicationContext(),
							"Invalid, type again!", Toast.LENGTH_SHORT).show();
				}
			} else if (result.equalsIgnoreCase("signUp")) {
				checkSignUp();
				if (check) {

					// Go to the MainPageActivitiy
					Intent intent = new Intent(getApplicationContext(),
							MainPageActivity.class);
					// Input user ID information, and send it to
					// MainPageActivity.
					intent.putExtra("idInfo", UId);

					startActivity(intent);

					 finish();

					
				} else {
					Toast.makeText(getApplicationContext(),
							"Duplicate Account", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

}
