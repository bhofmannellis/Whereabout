package kr.ac.gachon.whereabout;

import java.util.ArrayList;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * 	This is an application that offer service based on location-sharing.
 *	Content is a Life style application and people can share their location and protect loved ones.
 * 	Application name is a 'Whereabout'. The key functions is that making a friend list automatically,
 * 	sharing own location, and notifying to other users using Service component and Notification.
 * 	The goal of our application is that share location with friend, family, and sweetheart 
 * 	for safety, fun, and convenience.
 * 
 *  FriendMapFragment.java - Fragment for displaying the Map in a fragment which will be displayed in 
 *  the FragmentPager. Updates the map on a loop associated with the Location services update cycle
 * 
 * 	@author		Team6 :: Blaise, Ra Hyungjin, Kim Kideok
 * 	@version	1.0
 * 	@since		10. June. 2015
 * 	@email		doubleknd26@gmail.com
 *
 */

public class FriendMapFragment extends Fragment implements OnMapReadyCallback, OnMyLocationChangeListener {

	// Instance variables for Fragment
	private static View view;                 // Keep a reference to the map view to avoid attempts at re-inflating it
	private GoogleMap map;                    // GoogleMap object
	LatLng myLocation;                        // Latitude/Longitude object for the current user
	double currentLatitude, currentLongitude; // Double representations of latitude and longitude
	
	private ArrayList<Marker> friendMarkers;  // ArrayList of friends' Markers on the map
	private Marker myMarker;                  // Marker for the user's position on the Map
	
	private double friendLat, friendLon;      // Doubles for holding friends' locations
	private String friendName;                // String for holding friends' names
		
	// Method for getting a new FriendMapFragment that plays nice with the tabbed view
	// Returns a static FriendMapFragment which will be used in the FragmentPager
	public static FriendMapFragment newInstance( int page, String name ) {

		return new FriendMapFragment();
	}

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		super.onCreateView(	inflater, container, savedInstanceState);

		// If view is null, fragment hasn't been inflated yet. Otherwise, it doesn't need to be inflated
		if (view == null)
			view = inflater.inflate(R.layout.activity_map_fragment, container, false);
		return view;

	}

	@Override
	public void onActivityCreated( Bundle savedInstanceState ) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();

		// Get instance of Map via ASyncTask. After it gets the Map, onMapReady is called
		SupportMapFragment mapFrag = (SupportMapFragment) this.getChildFragmentManager()
																.findFragmentById( R.id.map );
		mapFrag.getMapAsync(this);

		// Initialize the ArrayList which hold the user's friends' Markers on the Map
		friendMarkers = new ArrayList<Marker>();
		
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	// There may only be one instance of the GoogleMap, so we need to make sure to remove the fragment when the View is destroyed
	@Override
	public void onDestroyView() {
		// Log.d("<<FriendMapFragment>>", "onDestroyView");
		SupportMapFragment f = (SupportMapFragment) getFragmentManager()
	            .findFragmentById(R.id.map);

		// If current instance of the GoogleMap fragment isn't null, we need to destroy it
	    if (f != null) {
	        try {
	            getFragmentManager().beginTransaction().remove(f).commit();
	        } catch (Exception e) {
	        	Log.e("FriendMapFragment","Exception while removing current map fragment.");
	            e.printStackTrace();
	        }
	    }

	    super.onDestroyView();
	}
	
	public GoogleMap getMap(){
		return map;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	// onMapReady() -- Callback method for getMapAsync -- Passes the instance of
	// the GoogleMap
	@Override
	public void onMapReady( GoogleMap gMap ) {

		// Set Map to Fragment-scoped instance variable
		map = gMap;
		
		// Set map to automatically draw user's location and register the OnMyLocationChangedListener
		map.setMyLocationEnabled(true);
		map.setOnMyLocationChangeListener(this);
		
		// Disable map's buttons for external apps
		map.getUiSettings().setMapToolbarEnabled(false);
		
		// Update/draw points on the map
		updateMap();
		
		// Zoom to user's location
		adjustCamera();
	}

	// Updates map with markers for each user in the group
	public synchronized void updateMap() {
		
		// Log.d("FriendMapFragment","Updating Map");
		
		// Clear existing markers on the map, starting with self
		if (myMarker != null){
			myMarker.remove();
		}
		// Next, clear out list of users during last update (if any)
		for (Marker m : friendMarkers){
			m.remove();
		}
		// Clear off the map
		map.clear();
		
		// Update user's location from MainPageActivity's location services
		myLocation = new LatLng(((MainPageActivity) getActivity()).getLatitude(),
								((MainPageActivity) getActivity()).getLongitude());
		
		// Draw a HUE_BLUE Marker for the user's location
		myMarker = map.addMarker(new MarkerOptions().position(	myLocation)
									.title("You are here, " + ((MainPageActivity) getActivity()).getUsername())
									.visible(true)
									.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
				
		// Debug message
		// Log.d("FriendMapActivity", "Drawing " + ((MainPageActivity)getActivity()).showFriendList.size() + " people on the map");
		
		// Loop through currently selected Friends list (showFriendList)
		for (Friend friend : ((MainPageActivity) getActivity()).showFriendList) {

			// The user is also in the showFriendList, but we don't need to draw them as they will be drawn with a uniquely-colored Marker
			if (!friend.getID().equals(((MainPageActivity)getActivity()).userId)){
			
				friendLat = ((MainPageActivity)getActivity()).getFriendLatitudeFromAll(friend);
				friendLon = ((MainPageActivity)getActivity()).getFriendLongitudeFromAll(friend);
				friendName = friend.getName();		
			
				// Make a LatLng item with the appropriate location
				// Log.d("Drawing Friends","Drawing Friend : "+friend.getName()+" at Lat: " + friendLat + " Lon: " + friendLon);
				
				MarkerOptions toAdd = new MarkerOptions()
					.position(new LatLng(friendLat, friendLon))
				    .title(friendName)
				    .visible(true).draggable(false)
				    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
				   
				map.addMarker(toAdd);
				
				// Log.d("Drawing Friend on Map", "Friend: " + friendName + " Location: Lat: " + friendLat + " Lon: " + friendLon);
				}
		}
				
	}

	// Update the map when user's location changes
	@Override
	public void onMyLocationChange(Location lastKnownLocation) {
	   updateMap();
	   // adjustCamera();
	}
	
	// Adjust camera location and zoom
	private void adjustCamera() {
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.latitude, myLocation.longitude), 14));
		CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation)// Sets the center of the map to location user
																	.zoom(	14)        // Sets the zoom
																	.build();          // Creates a CameraPosition from the builder
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));	
	}
}