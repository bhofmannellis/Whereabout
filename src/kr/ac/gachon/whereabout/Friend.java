package kr.ac.gachon.whereabout;

// Object for encapsulating User information -- 
// Name, UserID/Username, location data, phone number and Email.
public class Friend {
	//Instance variables for friend object.
	private String friendName, friendID, friendPhoneNum, Email;
	private double longitude, latitude;
	//constructor to make friend object.
	public Friend(String name, String id, double lat, double lon, String phoneNum,String Email){

		friendName = name;
		friendID = id;
		latitude = lat;
		longitude = lon;
		friendPhoneNum = phoneNum;
		this.Email = Email;
	}
	
	// Getters
	public double getLongitude(){
		return longitude;
	}
	
	public double getLatitude () {
		return latitude;
	}
	
	public String getName(){
		return friendName;
	}

	public String getPhoneNum(){
		return friendPhoneNum;
	}
	public String getEmail(){
		return Email;
	}
	public String getID() {
		return friendID;
	}

	// Setters

	public void setEmail(String Email) {
		this.Email = Email;
	}

	public void setLatitude( double latitude ) {
		this.latitude = latitude;
	}

	public void setLongitude( double longitude ) {
		this.longitude = longitude;
	}

	public void setName(String name){
		this.friendName = name;
	}

	public void setPhoneNum(String phoneNum ){
		this.friendPhoneNum = phoneNum;
	}

	public void setID( String id ) {
		this.friendID = id;
	}
	
	// toString returns the Friend's name
	public String toString() {
		return friendName;
	}

	// Return a String containing all Friend data
	public String getAllData() {
		return friendName + ":" + friendID + ":" + latitude + ":" + longitude + ":" + friendPhoneNum;
	}
}
