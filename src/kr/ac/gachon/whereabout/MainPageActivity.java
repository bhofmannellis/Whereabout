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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 	This is an application that offer service based on location-sharing.
 *	Content is a Life style application and people can share their location and protect loved ones.
 * 	Application name is a 'Whereabout'. The key functions is that making a friend list automatically,
 * 	sharing own location, and notifying to other users using Service component and Notification.
 * 	The goal of our application is that share location with friend, family, and sweetheart 
 * 	for safety, fun, and convenience.
 * 
 *  MainPageActivity.java -- Activity for handling the majority of the App's user-interaction.
 *  This Activity contains a ViewPager for handling Fragments of the user's friend list 
 *  (FriendListFragment.java), a list of shared-map groups (GroupListFragment.java), and the 
 *  Map for viewing locations (FriendMapFragment.java).
 *  This Activity also houses the list of Friends' information which is updated from the server.
 * 
 * 	@author		Team6 :: Blaise, Ra Hyungjin, Kim Kideok
 * 	@version	1.0
 * 	@since		10. June. 2015
 * 	@email		doubleknd26@gmail.com
 *
 */

public class MainPageActivity extends FragmentActivity {

	static final int NUM_ITEMS = 3;
	
	// For different fragment view
	FragPagerAdapter pagesAdapter;
	ViewPager viewPager;
	Bitmap bmImg;
	// Lists
	ArrayList<String> friendsList; // Users downloaded from server
	ArrayList<String> contactList; // Phone numbers

	public static ArrayList<String> chatGroupList; // group chats
	ArrayAdapter<String> chatGroupListAdapter;     // Adapter for groups
	public static ArrayList<Friend> allFriendList; // All Friends
	ArrayList<Friend> showFriendList;              // Friends to show on Map

	// User's location information
	private double latitude;
	private double longitude;

	// Location Manager, Listener, and object
	LocationManager locationMan;
	Location myLocation;
	LocationListener locationListener;

	Button btn;

	ImageView imView;
	String imageUrl = "http://kideok.linux2.dude.kr/";

	public static String userId;

	public MainPageActivity getMainPageActivity() {
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_page);
		startActivity(new Intent(this, Loading.class));

		friendsList = new ArrayList<String>();
		contactList = new ArrayList<String>();
		chatGroupList = new ArrayList<String>();

		allFriendList = new ArrayList<Friend>();
		showFriendList = new ArrayList<Friend>();
		userId = new String();

		// When After User Sign Up or Sign In.
		Intent intent = getIntent();

		if (!intent.getStringExtra("idInfo").toString().isEmpty()) {
			userId = intent.getStringExtra("idInfo").toString();

			Toast.makeText(this,
					"Welcome " + intent.getStringExtra("idInfo").toString(),
					Toast.LENGTH_SHORT).show();
		} else {
		}

		// Set up ViewPager for tabbed interface each fragment.
		viewPager = (ViewPager) findViewById(R.id.pager);
		pagesAdapter = new FragPagerAdapter(getSupportFragmentManager());

		viewPager.setAdapter(pagesAdapter);

		// For connect own device's contact.
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

		//
		String[] projection = new String[] {
				ContactsContract.CommonDataKinds.Phone.NUMBER,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };

		String[] selectionArgs = null;

		// Get list of my contact ordered by current list of my contact of
		// device.
		String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";

		//
		Cursor contactCursor = managedQuery(uri, projection, null,
				selectionArgs, sortOrder);

		if (contactCursor.moveToFirst()) {
			do {
				String phonenumber = contactCursor.getString(0);
				
				if (phonenumber.contains("-"))
					phonenumber = phonenumber.replaceAll("-", "");
				// Log.d("contact: ", phonenumber);
				contactList.add(phonenumber);// Getting phone
				
			} while (contactCursor.moveToNext());
		}
	    new SendPost().execute();
		
	    startService(new Intent(getBaseContext(), MyService.class));

	}

	public static void setChatGroup(String ids) {
		chatGroupList.add(ids);
	}

	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();

		locationMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// ï§�ë�‰ï¿½ï§�ë�±ì‘�æ¿¡ï¿½ ï¿½ì�žç§»ì„�ï¿½ ï§£ëŒ„ê²•ï¿½ë§‚ æ€¨ë…¹ì“£ ï§¡ë�¿ë¼±ä»¥ï¿½ï¿½ë–Ž.
		myLocation = locationMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		// ï¿½ì�žç§»ì„�ï¿½ è«›ë¶¾ï¿½ëš®ë’— å¯ƒê»‹ì“£ ï§£ëŒ„ê²•ï¿½ë¸¯ï¿½ë’— ç�?±ÑŠë’ªï¿½ê¼«.

		locationListener = new LocationListener() {
			@Override
			public void onLocationChanged( Location location ) {

				// Update location information when it changes
				myLocation = location;
				latitude = location.getLatitude();
				longitude = location.getLongitude();
			}

			@Override
			public void onStatusChanged( String provider, int status, Bundle extras ) {}

			@Override
			public void onProviderEnabled( String provider ) {}

			@Override
			public void onProviderDisabled( String provider ) {
				// Whereabout requires users' locations to function, so if the Location Provider is disabled,
				// the user must be prompted to re-enable the provider or close the app.
				checkLocationServices();
			}
		};

		locationMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

		// If location != null, location is being provided by the GPS service.
		// If location == null, try checking the network provider
		if (myLocation == null) {
			myLocation = locationMan
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			locationMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
		} else {
			myLocation = locationMan
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}

		// If location is still null after checking both the network and GPS
		// providers, show a Toast saying so.
		// If location is not null, get the location so it can be sent to the
		// server and displayed on the map.
		if (myLocation != null) {

			latitude = myLocation.getLatitude();
			longitude = myLocation.getLongitude();

		} else
			Toast.makeText(getApplicationContext(), "Cannot access location",
					Toast.LENGTH_SHORT).show();

		//if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
		//	new KeepTraceLocation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		//else
		new KeepTraceLocation().execute();
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}

	// Get currentlySelectedContacts list
	public ArrayList<Friend> getCurrentFriends() {
		return showFriendList;
	}

	public ArrayList<Friend> getAllFriends() {
		return allFriendList;
	}

	public void addFriendToSelected(Friend friend) {
		showFriendList.add(friend);
	}

	// FragmentPagerAdapter for displaying interface of three fragments side-by-side
	public static class FragPagerAdapter extends FragmentPagerAdapter {

		public FragPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		// Number of tabs (3)
		@Override
		public int getCount() {
			return NUM_ITEMS;
		}

		// Selector for three different tabs
		@Override
		public Fragment getItem(int position) {

			switch (position) {
			default:
				return null;
			case 0:
				return FriendListFragment.newInstance(position, "Friends");
			case 1:
				return GroupListFragment.newInstance(position, "Groups");
			case 2: 
				return FriendMapFragment.newInstance(position, "Map");
			}
		}
			// Titles of each tab
		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return "Friends";
			case 1:
				return "Groups";
			case 2:
				return "Map";
			default:
				return "ERROR";
			}
		}
	}

	// Return the current user's latitude
	public double getLatitude() {
		return latitude;
	}

	// Return the current user's longitude
	public double getLongitude() {
		return longitude;
	}

	// Set the current user's latitude
	public void setLatitude(Double lat) {
		latitude = lat;
	}

	// Set the current user's longitude
	public void setLongitude(Double lon) {
		longitude = lon;
	}

	// Get current user's username
	public String getUsername() {
		if (userId == null)
			return "Anonymous";
		return userId;
	}

	private void checkLocationServices() {
		// Check if location is enabled. If not, launch dialog giving option of
		// opening location settings or quitting
		if (!isLocationEnabled(getApplicationContext())) {

			// Alert Dialog code adapted form here:
			// http://stackoverflow.com/questions/2115758/how-to-display-alert-dialog-in-android
			AlertDialog.Builder builder = new AlertDialog.Builder(
					MainPageActivity.this);
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

	// Checks if user has enabled location services on the device
	// Returns true if location services are already on, false otherwise
	// Adapted from here: http://stackoverflow.com/questions/10311834/how-to-check-if-location-services-are-enabled
	public static boolean isLocationEnabled(Context context) {
		int locationMode = 0;
		String locationProviders;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			try {
				locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
			} catch (SettingNotFoundException e) {
				Log.e("MainPageActivity","SettingNotFoundException when getting LocationMode in isLocationEnabled");
				e.printStackTrace();
				return false;
			}
				return locationMode != Settings.Secure.LOCATION_MODE_OFF;
		}else{
			locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			return !TextUtils.isEmpty(locationProviders);
		}
	}

	class KeepTraceLocation extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... unused) {
			while (true) {

				ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();

				post.add(new BasicNameValuePair("LATITUDE", Double
						.toString(latitude)));
				post.add(new BasicNameValuePair("LONGITUDE", Double
						.toString(longitude)));
				post.add(new BasicNameValuePair("ID", userId));

				int i = 0;
				for (i = 0; i < allFriendList.size(); i++) {
					post.add(new BasicNameValuePair(Integer.toString(i),
							allFriendList.get(i).getID().toString()));
				}
				post.add(new BasicNameValuePair("COUNT", Integer
						.toString(i)));

				HttpClient client = new DefaultHttpClient();

				// set client's connection timeout and so timeout.
				HttpParams params = client.getParams();
				HttpConnectionParams.setConnectionTimeout(params, 5000);
				HttpConnectionParams.setSoTimeout(params, 5000);

				// set web server domain name.
				HttpPost httpPost = new HttpPost(
						"http://kideok.linux2.dude.kr" + "/trace.php?");

				String text = null;

				try {
					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
							post, "utf-8");
					httpPost.setEntity(entity);

					// return EntityUtils.getContentCharSet(entity);
					HttpResponse res = client.execute(httpPost);

					text = EntityUtils.toString(res.getEntity());
					text = text.trim();

					String list[] = text.split("!");
					String info[][] = new String[100][];

					for (int k = 0; k < list.length; k++) {
						info[k] = list[k].split("/");
						// Log.d("LOCATION", " USER ID::" + info[k][0]
						// 		+ " LATITUDE :" + info[k][1]
						// 		+ "LONGITUDE :" + info[k][2]);
					}

					for (int j = 0; j < allFriendList.size(); j++) {

						for (int l = 0; l < info.length; l++) {
							// Update all friend's location information in
							// allFriendList.
							if (allFriendList.get(j).getID()
										.equals(info[l][0])) {

								allFriendList.get(j).setLongitude(
										Double.parseDouble(info[l][1]));
									allFriendList.get(j).setLatitude(
											Double.parseDouble(info[l][2]));
								break;
							}
						}
					}

				} catch (ClientProtocolException e) {
					Log.e("MainPageActivity","ClientProtocolException in keepTraceLocation");
					e.printStackTrace();
				} catch (IOException e) {
					Log.e("MainPageActivity","IOException in keepTraceLocation");
					e.printStackTrace();
				}

				// Keep trace location everytime 20 sec.
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					Log.e("MainPageActivity","InterruptedException while trying to have thrread sleep for 20sec");
					e.printStackTrace();
				}
			}

		}

		@Override
		protected void onPostExecute(Void result) {
			
			super.onPostExecute(result);

		}
	}; // End of class KeepTraceLocation;
		
	class SendPost  extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... unused) {
			// Log.d("Before", "he");
			executeClient();
			// Log.d("ha", "ge");
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
			// Log.d("friendlistsize", allFriendList.size() + "");
			insertFriendsList();

			super.onPostExecute(result);

		}

		@SuppressWarnings("deprecation")
		public void executeClient() {
			int i;
			// Log.d(getClass().getName(), " Start name value pair");

			ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();

			for (i = 0; i < contactList.size(); i++) {
				post.add(new BasicNameValuePair(Integer.toString(i),
						contactList.get(i).toString()));
				// Log.d("i",""+i+""+" "+contactList.get(i).toString());
			}
			// Log.d(getClass().getName(), " end name value pair");

			i--;
			post.add(new BasicNameValuePair("Count", Integer.toString(i)));

			HttpClient client = new DefaultHttpClient();

			// set client's connection timeout and so timeout.
			HttpParams params = client.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 5000);
			HttpConnectionParams.setSoTimeout(params, 5000);

			// set web server domain name.
			HttpPost httpPost = new HttpPost("http://kideok.linux2.dude.kr"
					+ "/getNumber.php?");

			String text = null;
			try {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
						post, "utf-8");
				httpPost.setEntity(entity);

				// return EntityUtils.getContentCharSet(entity);

				HttpResponse res = client.execute(httpPost);
				//
				text = EntityUtils.toString(res.getEntity());
				text = text.trim();
				// Log.d("text", "TTEST::" + text);
				if (text.equals("")) {
					// Log.d("invalid", "There is no data");
				} else {
					String[] list = text.split(" ");
					for (int i1 = 0; i1 < list.length; i1++) {
						String[] temp = list[i1].split("/");
						// Log.d("MainPageActivity -- Download", "temp[0]: " + temp[0] + " temp[1]: " + temp[1] + 
						//       " temp[2]: " + temp[2] + " temp[3]: " + temp[3] + 
						//       " temp[4]: " + temp[4] + " temp[5]: " + temp[5]);
						allFriendList.add(new Friend(temp[0], temp[1],
								Double.parseDouble(temp[2]), Double
										.parseDouble(temp[3]), temp[4],
								temp[5]));
					}
				}

			} catch (ClientProtocolException e) {
				Log.e("MainPageActivity","ClientProtocolException in SendPost");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e("MainPageActivity","IOException in SendPost");
				e.printStackTrace();
			}

		}

	};

	// Parse a list of Strings showing Friends' IDs
	public void insertFriendsList() {
		for (int i = 0; i < allFriendList.size(); i++) {
			friendsList.add(allFriendList.get(i).getID());
			// Log.d("friendsList", friendsList.get(i).toString());
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// ë¹½(ì·¨ì†Œ)í‚¤ê°€ ëˆŒë ¸ì�„ë•Œ ì¢…ë£Œì—¬ë¶€ë¥¼ ë¬»ëŠ�? ë‹¤ì�´ì–¼ë¡œê·¸ ë�„ì›€
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			AlertDialog.Builder d = new AlertDialog.Builder(
					MainPageActivity.this);
			d.setTitle(" ");
			d.setMessage("Do you want to Exit the application?");
			d.setIcon(R.drawable.logo);

			d.setPositiveButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Close the dialog
					dialog.cancel();
				}
			});

			d.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Close the application
					finish();
				}
			});
			d.show();

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// Iterate through allFriendList to find Friend object with same ID as
	// parameter Friend object.
	// Return that friend's updated Latitude
	public double getFriendLatitudeFromAll(Friend friend) {
		for (Friend f : allFriendList) {
			if (f.getID().equals(friend.getID())) {
				return f.getLatitude();
			}
		}
		return 0.0;
	}

	// Iterate through allFriendList to find Friend object with same ID as
	// parameter Friend object.
	// Return that friend's updated Longitude
	public double getFriendLongitudeFromAll(Friend friend) {
		for (Friend f : allFriendList) {
			if (f.getID().equals(friend.getID())) {
				return f.getLongitude();
			}
		}
		return 0.0;
	}

	public void notifyGroupList() {
		chatGroupListAdapter.notifyDataSetChanged();
	}
}