package kr.ac.gachon.whereabout;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 *	MyService offers a service for the notification to another user.
 *	When application is started, service is started too and keep track database to check whether or not
 *	another friends send me to the invite message.
 * 
 * 
 *  @author		Team6 :: Blaise, Ra Hyungjin, Kim Kideok
 * 	@version	1.0
 * 	@since		10. June. 2015
 * 	@email		doubleknd26@gmail.com
 */
public class MyService extends Service {

	public static ArrayList<String> creator;
	public static ArrayList<String> invitee;
	private int notificationID = 1;
	static final int UPDATE_INTERVAL = 5000; 
	private Timer timer = new Timer();

	/**
	 * 	This is a onBind.
	 * 	@param	Intent arg0.
	 * 	@return	null.
	 */
	@Override
	public IBinder onBind(Intent arg0) { 
		return null;
	}


	/**
	 * 	This is a onStartCommand method and it is called when service is started by
	 * 	startService(new Intent(getBaseContext(), MyService.class)) from MainPageActivity.
	 * 	
	 * 	@param	intent, flags, startId
	 * 	@return	START_STICKY, which means that if service is exited, restart service automatically.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.

		Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

		creator = new ArrayList<String>();
		invitee = new ArrayList<String>();

		doSomethingRepeatedly();
		return START_STICKY;
	}

	/**
	 * 	This method is called in the onStartCommand and do checking id from web server repeatedly
	 * 	for make a location sharing group.
	 * 	
	 * 	@param	No parameter.
	 * 	@return	No return.
	 * 	
	 */
	private void doSomethingRepeatedly() { 
		timer.scheduleAtFixedRate( new TimerTask() {
			public void run() {
				// check database what is there my id
				ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();

				post.add(new BasicNameValuePair("ID", MainPageActivity.userId ));

				HttpClient client = new DefaultHttpClient();

				HttpParams params = client.getParams();
				HttpConnectionParams.setConnectionTimeout(params, 5000);
				HttpConnectionParams.setSoTimeout(params, 5000);

				HttpPost httpPost = new HttpPost("http://kideok.linux2.dude.kr"+"/check_chat_room.php?");

				try {
					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(post,"utf-8");
					httpPost.setEntity(entity);

					// return EntityUtils.getContentCharSet(entity);
					HttpResponse res = client.execute(httpPost);

					String result = EntityUtils.toString((res.getEntity()));

					if (result.equals(":::ERROR::CONNECTION_ERROR")) {
						// There is no invited chat room
						// Log.i("wa","ERROR:NODATA");
					}
					else {
						result.trim();
						if (result.substring(result.indexOf(":::")+3).isEmpty() == false ) {

							result = result.substring(result.indexOf(":::")+3);

							String[] list = result.split("!");

							for (int i=0; i<list.length; i++) {
								creator.add(i, list[i].substring(0, list[i].indexOf("-")) );
								list[i] = list[i].substring(list[i].indexOf("-")+1);


								displayNotification(creator.get(i), list[i]);
							}
						}
						// else {
						// 	 Log.i("wa","Data format is not completed.");
						// }
					}

				} catch (ClientProtocolException e) {
					e.printStackTrace();
					Log.e("wa","Client ProtocolException");
				} catch (IOException e) {
					e.printStackTrace();
					Log.e("wa","IOException");
				}
			}
		}, 0, UPDATE_INTERVAL); 
	}

	/**
	 * 	This method is a displayNotification. And it well be called when user invited from another user for
	 * 	location sharing.
	 * 
	 * 	@param	newCreator who invite this user.
	 * 	@param	newInvitees	who all inviter.
	 * 	@return	No return.
	 * 	
	 */
	protected void displayNotification (String newCreator, String newInvitees) {

		//---PendingIntent to launch activity if the user selects // this notification---
		Intent i = new Intent(this, Notification.class); 
		i.putExtra("notificationID", notificationID); 

		newInvitees = newInvitees.substring(0, newInvitees.length()-1);

		i.putExtra("creator", newCreator);

		i.putExtra("invitees",newInvitees);

		i.setData(Uri.parse(newCreator+":"+newInvitees));


		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, 0);

		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); 

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_launcher) 
		.setContentTitle("Invited by "+newCreator)
		.setContentText("Invited ID :"+newInvitees);

		mBuilder.setAutoCancel(true);
		//notif.setLatestEventInfo(this, from, message, pendingIntent); 
		mBuilder.setContentIntent(pendingIntent);
		//---100ms delay, vibrate for 250ms, pause for 100 ms and
		// then vibrate for 500ms---
		mBuilder.setVibrate(new long[] { 100, 250, 100, 500 } );

		nm.notify(notificationID, mBuilder.build());
		notificationID++;
	}


	/**
	 * 	This is a onDestroy.
	 * 	@param	No parameter.
	 * 	@return	No return.
	 */
	@Override
	public void onDestroy() { 
		super.onDestroy(); 
		// timer is canceled.
		if (timer != null){
			timer.cancel();
		}
		Toast.makeText(this, "Service Destroyed",Toast.LENGTH_LONG).show();
	}
}