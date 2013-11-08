package com.example.lawyerapp;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import android.graphics.Bitmap;
import android.net.Uri;

//Class that defines contact objects
//These objects are not stored in the GreenDao database, but are used to make handling contact information easier

public class Contact {
	
	//Each of the properties stored for the contact
	private String name;              //contact name
	private ArrayList<String> numbers ;          // array of all phone numbers
	private String ID;                // reference ID number for the contact used to retrieve a specific contact from the android OS
	private Bitmap picture;         // stores the display picture if one exists
	private Uri ContactUri;        //The actual link to the contact
	
	public Contact() {
		numbers=new ArrayList<String>();       //initialize the list of phone numbers
	}
	
	//Getters and setters for everything else
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public ArrayList<String> getNumbers() {
		return numbers;
	}
	public void setNumbers(ArrayList<String> numbers) {
		this.numbers = numbers;
	}

	public Bitmap getPicture() {
		return picture;
	}

	public void setPicture(Bitmap picture) {
		this.picture = picture;
	}

	public Uri getContactUri() {
		return ContactUri;
	}

	public void setContactUri(Uri contactUri) {
		ContactUri = contactUri;
	}
	

}
