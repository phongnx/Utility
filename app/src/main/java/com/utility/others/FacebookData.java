package com.utility.others;

public class FacebookData {
	String facebook_id = "";
	String name = "";
	String user_name = "";
	String first_name = "";
	String middle_name = "";
	String last_name = "";
	String birthday = "";
	String email = "";
	String gender = "";

	public FacebookData() {
	}

	public FacebookData(String facebook_id, String name, String user_name, String first_name, String middle_name, String last_name, String birthday, String email, String gender) {
		this.facebook_id = facebook_id;
		this.name = name;
		this.user_name = user_name;
		this.first_name = first_name;
		this.middle_name = middle_name;
		this.last_name = last_name;
		this.birthday = birthday;
		this.email = email;
		this.gender = gender;
	}

	public String getFacebook_id() {
		return facebook_id;
	}

	public void setFacebook_id(String facebook_id) {
		this.facebook_id = facebook_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserName() {
		return user_name;
	}

	public void setUserName(String user_name) {
		this.user_name = user_name;
	}

	public String getFirstName() {
		return first_name;
	}

	public void setFirstName(String first_name) {
		this.first_name = first_name;
	}

	public String getMiddleName() {
		return middle_name;
	}

	public void setMiddleName(String middle_name) {
		this.middle_name = middle_name;
	}

	public String getLastName() {
		return last_name;
	}

	public void setLastName(String last_name) {
		this.last_name = last_name;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

}
