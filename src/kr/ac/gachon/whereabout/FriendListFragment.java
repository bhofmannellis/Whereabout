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
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
/*
 * This is a fragment which shows my friends list.
 * To get the friends list, take phone numbers from my phone to server,
 * compare that there are same phone numbers between my numbers(contacts) and 
 * server, user_info (database table) about phone number. If number is same,
 * get their information from server and make list view of their id.
 */
public class FriendListFragment extends Fragment {

	private ArrayAdapter<String> adapter; //array adapter for Ids. 
	private ListView fragment1_list;  
	private EditText search; 
	public static ArrayList<String> checkedFriend; // store friends who checked
	private int Position; 

	public static FriendListFragment newInstance(int page, String name) {
		
		return new FriendListFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// Log.d("<<FriendListFragment>>", "OnCreateView -- Inflating activity_friend_list_fragment");

		// Initialize checkFriend.
		checkedFriend = new ArrayList<String>(); 


		View view = inflater.inflate(R.layout.activity_friend_list_fragment,
				container, false);
		Button button = (Button) view.findViewById(R.id.confirm);
		//This button is to check which ids are checked in friends list.
		//If checked, make a connection with checked ids and chatting room.
		button.setOnClickListener(new OnClickListener() 
				{
			@Override
			public void onClick(View v)
			{
				if (v.getId()==R.id.confirm) {

					// Log.i("wa","checkedUser");

					if (FriendListFragment.checkedFriend.size() > 0) {

						Thread thread = new Thread() {
							@Override
							public void run() {

								ArrayList<String> checkedUser = FriendListFragment.checkedFriend;



								ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();

								String str="";
								for (int i=0 ; i< checkedUser.size() ; i++) {
									str += checkedUser.get(i).toString();
									str+="/";
								}

								post.add(new BasicNameValuePair("CREATOR", MainPageActivity.userId));
								post.add(new BasicNameValuePair("INVITEE", str));


								HttpClient client = new DefaultHttpClient();

								HttpParams params = client.getParams();
								HttpConnectionParams.setConnectionTimeout(params, 5000);
								HttpConnectionParams.setSoTimeout(params, 5000);

								HttpPost httpPost = new HttpPost("http://kideok.linux2.dude.kr"+"/create_chat_room.php?");

								try {
									UrlEncodedFormEntity entity = new UrlEncodedFormEntity(post,"utf-8");
									httpPost.setEntity(entity);

									// return EntityUtils.getContentCharSet(entity);
									HttpResponse res = client.execute(httpPost);

									String txt = EntityUtils.toString((res.getEntity()));


									txt.trim();
									// Log.i("wa", txt + "   " + txt.length());
									
									if ( txt.equals("success") ) {
										//make a chat room.
										// Log.i("wa","SUCCESS MAKE A CHAT ROOM");
										
										MainPageActivity.setChatGroup(str+MainPageActivity.userId);

										startActivity(new Intent(getActivity(),Loading.class));
									}
									else {
										//error
										Log.e("<<FriendListFragment>>","HTTP failure when making chatroom");

									}

								} catch (ClientProtocolException e) {
									e.printStackTrace();
									Log.e("FriendListFragment","Client ProtocolException");
								} catch (IOException e) {
									e.printStackTrace();
									Log.e("FriendListFragment","IOException");
								}



							}
						};
						thread.start();
					}
				}
			} 
		});


		return view;
	}



	@Override
	public void onStart() {

		super.onStart();


		// adapter = new
		// ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1);
		search = (EditText) getActivity().findViewById(R.id.search);
		//this text watcher is to search friend in friends list.
		//this acts like a kakao talk.
		TextWatcher watcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				//Log.d("why", "why");
				//If text changed,
				String check = search.getText().toString(); //get string in the editText to search.
				//if there is no any keyword to search,
				if (check.equalsIgnoreCase("")) {
					adapter.clear();
					((MainPageActivity)getActivity()).insertFriendsList();
					
					//adapter.addAll(((MainPageActivity)getActivity()).friendsList);
					adapter.notifyDataSetChanged();
					
				} 
				//if there is matching keyword between string and friend ids in friends list.
				else {
					ArrayList<String> list = new ArrayList<String>();
					for (String f : ((MainPageActivity) getActivity()).friendsList) {
						if (f.contains(check))
						{
							list.add(f);
							
						}
					}
					// for (String d : list)
					// 	Log.d("d in list", d);
					adapter.clear();
					adapter.addAll(list);
					adapter.notifyDataSetChanged();
					//((MainPageActivity)getActivity()).insertFriendsList();
				}
			}
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		};
		search.addTextChangedListener(watcher);
		adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_checked,
				((MainPageActivity) getActivity()).friendsList);

		//adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1);
		adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_checked,((MainPageActivity)getActivity()).friendsList);

		adapter.setNotifyOnChange(true);
		fragment1_list = (ListView) getActivity().findViewById(R.id.myList);
		fragment1_list.setAdapter(adapter);//set adapter.
		fragment1_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			//make long click listener on friends list to show information about long clicked 
			//friend and dial, sms, email.
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Position = position;
				ShowFriend().show(); //show alert dialog box about friend information.
				return true;
			}
			
		});
		fragment1_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				// Change check
				CheckedTextView checkedView = ((CheckedTextView)view);
				Position = position;
				// When friend is already checked and user want to cancel the check,
				// Here is called.
				if (checkedView.isChecked()) {
					// If this friend is in the checkedFriend List, then remove.
					if (checkedFriend.contains(checkedView.getText().toString()) ) {
						checkedFriend.remove(checkedView.getText().toString());
					}
					//((MainPageActivity)getActivity()).removeFromListById(((MainPageActivity)getActivity()).showFriendList.get(position).getID());
				} else {
					if (!checkedFriend.contains(checkedView.getText().toString()) ) {
						checkedFriend.add(checkedView.getText().toString());
					}
					// ((MainPageActivity)getActivity()).showFriendList.add(((MainPageActivity)getActivity()).allFriendList.get(position));
				}

				// for (int i=0 ; i<checkedFriend.size() ; i++) {
				// 	Log.i("wa", "checkedFriend ::"+checkedFriend.get(i).toString() );
				// }

				checkedView.setChecked(!checkedView.isChecked());	// It represent CHECK!!
				//((MainPageActivity)getActivity()).updateMapFragment();
			}
		});
	}


	public ArrayList<String> getCheckedFriend () {
		return checkedFriend;
	}


	public Dialog ShowFriend() {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		final View dialogView = inflater.inflate(R.layout.dialog_friend, null);
		final TextView frId = (TextView) dialogView
				.findViewById(R.id.frIdField);
		final TextView frName = (TextView) dialogView
				.findViewById(R.id.frNameField);
		final TextView frPhone = (TextView) dialogView
				.findViewById(R.id.frPhoneField);
		final TextView frEmail = (TextView) dialogView
				.findViewById(R.id.frEmailField);
		for(Friend a : ((MainPageActivity)getActivity()).allFriendList)
		{
			if(a.getID().equalsIgnoreCase(adapter.getItem(Position)))
			{
				//get information from friend object.
				frId.setText("ID : "+ a.getID());
				frName.setText("Name : "+a.getName());
				frPhone.setText("Phone : "+a.getPhoneNum());
				frEmail.setText("Email : "+a.getEmail());
				break;
			}
		}
		

		builder.setView(dialogView)
				// Add action buttons
				//To make sms button.
				.setNegativeButton("Message",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) { //
								Uri uri = Uri.parse("smsto:"
										+ frPhone.getText().toString());
								Intent it = new Intent(Intent.ACTION_SENDTO,
										uri);
								startActivity(it);
							}
						})
				//To make Email button.
				.setPositiveButton("Email",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Uri uri = Uri.parse("mailto:"
										+ frEmail.getText().toString());
								Intent it = new Intent(Intent.ACTION_SENDTO,
										uri);
								startActivity(it);
							}
						})
				//To make Dial button.
				.setNeutralButton("Dial",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent i = new Intent(Intent.ACTION_CALL, Uri
										.parse("tel:"
												+ frPhone.getText().toString()));
								startActivity(i);
							}
						});

		return builder.create();
	}


}
