package kr.ac.gachon.whereabout;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.RelativeLayout;
/*
 * This class is just to show loading(splash).
 * Show background randomly then after 3 seconds, it will disappear and 
 * start another activity.
 */
/**
 * 	Loading class is a like splash class. When application work and take little time, this splash class
 * 	make user feel not stuffy.
 * 
 * 	@author		Team6 :: Blaise, Ra Hyungjin, Kim Kideok
 * 	@version	1.0
 * 	@since		10. June. 2015
 * 	@email		doubleknd26@gmail.com
 *
 */
public class Loading extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);

		setContentView(R.layout.splash);
		RelativeLayout lay = (RelativeLayout) findViewById(R.id.splash);
		int i;
		//This is to show the background randomly.
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


				finish(); //finish after 3 seconds.

			}
		}, 3000);

	}

}
