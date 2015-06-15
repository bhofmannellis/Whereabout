package kr.ac.gachon.whereabout;

import android.app.NotificationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * 	Notification offers notify message(push message) from who want to location sharing to who invited.
 * 
 * 
 * 	@author		Team6 :: Blaise, Ra Hyungjin, Kim Kideok
 * 	@version	1.0
 * 	@since		10. June. 2015
 * 	@email		doubleknd26@gmail.com
 *
 */
public class Notification extends ActionBarActivity {

   private int notificationID = 1;
   private TextView txt1, txt2, txt3;
   private String creator, invitees;

   /**
    * 	This is a onCreate.
    * 	@param	Bundle savedInstanceState.
    * 	@return	No return.
    */
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_notification);
      //---look up the notification manager service---
      NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

      //---cancel the notification that we started---
      nm.cancel(getIntent().getExtras().getInt("notificationID"));

      // get data and split it.
      String temp = getIntent().getData().toString();
      String info[] = temp.split(":");
      creator = info[0];
      invitees = info[1];

      MainPageActivity.setChatGroup(creator+"/"+invitees);


      txt1 = (TextView)findViewById(R.id.txt1);
      txt2 = (TextView)findViewById(R.id.txt2);
      txt3 = (TextView)findViewById(R.id.txt3);

      txt1.setText("Invitation");
      txt2.setText("CREATOR:"+info[0]);
      txt3.setText("INVITEES :"+info[1]);
      
      RelativeLayout lay = (RelativeLayout) findViewById(R.id.notifyLayout);
         
      int i;
      i = (int)(Math.random()*5)+1;
      if(i==1)
         lay.setBackgroundResource(R.drawable.waiting1);
      else if(i==2)
         lay.setBackgroundResource(R.drawable.waiting2);
      else if(i==3)
         lay.setBackgroundResource(R.drawable.waiting3);
      else if(i==4)
         lay.setBackgroundResource(R.drawable.waiting4);
      else
         lay.setBackgroundResource(R.drawable.waiting5);
      // Log.d("i",i+"");
      Handler hd = new Handler();
      
      hd.postDelayed(new Runnable() {

         @Override
         public void run() {
            finish();
         }
      }, 3000);
   }
}