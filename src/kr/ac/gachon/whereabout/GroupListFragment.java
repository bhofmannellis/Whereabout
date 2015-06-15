package kr.ac.gachon.whereabout;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 	This is an application that offer service based on location-sharing.
 *	Content is a Life style application and people can share their location and protect loved ones.
 * 	Application name is a 'Whereabout'. The key functions is that making a friend list automatically,
 * 	sharing own location, and notifying to other users using Service component and Notification.
 * 	The goal of our application is that share location with friend, family, and sweetheart 
 * 	for safety, fun, and convenience.
 * 
 *  GroupListFragment.java -- Fragment for displaying list of groups within the FragmentPager
 * 
 * 	@author		Team6 :: Blaise, Ra Hyungjin, Kim Kideok
 * 	@version	1.0
 * 	@since		10. June. 2015
 * 	@email		doubleknd26@gmail.com
 *
 */

public class GroupListFragment extends Fragment {

	private final int mode = Activity.MODE_PRIVATE;
	private int Position;
	final String MYPREFS = "MyPreferences";
	// create a reference to the shared preferences object
	private SharedPreferences mySharedPreferences;
	// obtain an editor to add data to my SharedPreferences object
	SharedPreferences.Editor myEditor;

	public static ListView chatGroupsListView;
	int numFriends;

	public static GroupListFragment newInstance(int page, String name) {
		
		return new GroupListFragment();
	}

	// Inflate the listView layout
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Log.d("<<ChatGroupListFragment>>",
		// 		"OnCreateView -- Inflating activity_friend_list_fragment");

		// create a reference & editor for the shared preferences object
		mySharedPreferences = getActivity().getSharedPreferences(MYPREFS, 0);
		myEditor = mySharedPreferences.edit();

		// has a Preferences file been already created?
		if (mySharedPreferences != null
				&& mySharedPreferences.contains("length")) {
			// object and key found, show all saved values
			applySharedPreferences();
		} else {
			Toast.makeText(getActivity(), "No Preferences found", 1).show();
		}

		return inflater.inflate(R.layout.activity_group_list_fragment,
				container, false);
	}

	public void sharedPreferences() {
		// create the shared preferences object
		SharedPreferences mySharedPreferences = getActivity()
				.getSharedPreferences(MYPREFS, mode);
		// obtain an editor to add data to (my)SharedPreferences object
		SharedPreferences.Editor myEditor = mySharedPreferences.edit();

		// Log.i("wa",
		// 		"sharedPreferences ::"
		// 				+ Integer
		// 						.toString(((MainPageActivity) getActivity()).chatGroupList
		// 								.size()));

		for (int i = 0; i < ((MainPageActivity) getActivity()).chatGroupList
				.size(); i++) {
			myEditor.putString(Integer.toString(i),
					((MainPageActivity) getActivity()).chatGroupList.get(i));
		}

		myEditor.putString("length", Integer
				.toString(((MainPageActivity) getActivity()).chatGroupList
						.size()));
		myEditor.commit();
	}

	public void applySharedPreferences() {
		// retrieve the SharedPreferences object
		SharedPreferences mySharedPreferences = getActivity()
				.getSharedPreferences(MYPREFS, mode);
		// setChatGroup()

		int length = Integer.parseInt(mySharedPreferences.getString("length",
				"0"));

		for (int i = 0; i < length; i++) {
			MainPageActivity.setChatGroup(mySharedPreferences.getString(
					Integer.toString(i), "none"));
		}
	}

	// Save SharedPreferences when the fragment pauses
	public void onPause() {
		super.onPause();
		sharedPreferences();
	}

	@Override
	public void onStart() {

		super.onStart();

		// Associate ListAdapter with list of shared map groups
		((MainPageActivity) getActivity()).chatGroupListAdapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_list_item_1,
				((MainPageActivity) getActivity()).chatGroupList);
		((MainPageActivity) getActivity()).chatGroupListAdapter
				.setNotifyOnChange(true);

		// Get the ListView and register onclick listener
		chatGroupsListView = (ListView) getActivity().findViewById(
				R.id.chatGroupList);
		chatGroupsListView.setOnCreateContextMenuListener(this);
		chatGroupsListView
				.setAdapter(((MainPageActivity) getActivity()).chatGroupListAdapter);
		chatGroupsListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						// Empty showFriendList
						((MainPageActivity) getActivity()).showFriendList
								.clear();

						// Split the chosen group into its users
						// Iterate through this array, adding users to
						// showFriendList by userId
						for (String user : ((MainPageActivity) getActivity()).chatGroupList
								.get(position).split("/")) {
							// Iterate through allFriendList looking for the
							// user by userId
							for (int i = 0; i < ((MainPageActivity) getActivity()).allFriendList
									.size(); i++) {
								// If there's a match, add that user to
								// showFriendList
								if (((MainPageActivity) getActivity()).allFriendList
										.get(i).getID().equals(user))
									((MainPageActivity) getActivity()).showFriendList
											.add(((MainPageActivity) getActivity()).allFriendList
													.get(i));
							}
						}
						// Switch to Map page
						((MainPageActivity) getActivity()).viewPager
								.setCurrentItem(2);
					}
				});
		chatGroupsListView
				.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						// TODO Auto-generated method stub
						Position = position;
						AlertDialog dialBox = createDialogBox();
						dialBox.show();
						return true;
					}

				});
	}

	private AlertDialog createDialogBox() {
		AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getActivity())
				// set message, title, and icon
				.setTitle("Delete Room")
				.setMessage("Are you sure that you want to delete?")
				.setIcon(R.drawable.logo)
				// set three option buttons
				.setPositiveButton("No",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// whatever should be done when answering "No"
								// goes here
							}
						})// setPositiveButton
				.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// whatever should be done when answering "Yes" goes here
						ArrayList<String> groupList = new ArrayList<String>();
						// for(int i=0;i<((MainPageActivity) getActivity()).chatGroupList.size();i++)
						// {
						// 	Log.d("list " + i,((MainPageActivity) getActivity()).chatGroupList.get(i).toString());
						// }
						((MainPageActivity) getActivity()).chatGroupList.remove(Position);
						((MainPageActivity) getActivity()).chatGroupListAdapter.notifyDataSetChanged();
						
					}
				})// setNegativeButton
				.create();
		return myQuittingDialogBox;
	}// createDialogBox
}