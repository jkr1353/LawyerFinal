package com.example.lawyerapp;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

//This Class implements the fragment for contacts placed in each case view upon flipping to the contact tab



public class ContactFrag extends ListFragment {

	
	private ArrayList<Contact> Contacts = new ArrayList<Contact>();     //a list that gets generated containing Contact objects for all the contacts on the phone
	private ArrayList<Contact> CaseContacts = new ArrayList<Contact>();  //a list that gets generated containing Contact objects for only the contacts associated with this case
	private ArrayList<Long> contactIDs = new ArrayList<Long>();          // a list generated of contact ID's of this case's associated contacts, this is what gets stored in GreenDao for contacts
	
	// bool check to see if the list Contacts has been populated yet
	private boolean created=false;
	//buttons at the bottom of the screen
	private Button newButton, pickCurrent, addNewMileage, deleteContact;
	//used by GreenDao to interact with our database
	private DaoInstance daoinstance;
	private CaseContactsDao caseContactsDao;
	
	private Long parentID;// stores the current case ID
	
	//initialize the custom made list adapter, created in the function ContactAdapter below
	private ContactAdapter adapter;        
	
	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		created=false;
		Contacts.clear();
		
		newButton = (Button) getActivity().findViewById(R.id.buttonNewHours);
		pickCurrent = (Button) getActivity().findViewById(R.id.buttonNewExpense);
		addNewMileage = (Button) getActivity().findViewById(R.id.buttonNewMileage);
		deleteContact = (Button) getActivity().findViewById(R.id.buttonDelete);
		
		parentID = getActivity().getIntent().getExtras().getLong("id");
		daoinstance = DaoInstance.getInstance(getActivity());
		caseContactsDao= daoinstance.getCaseContactsDao();
		//Only need one of the buttons at the bottom of the screen outside of this fragment, set the others to gone
		pickCurrent.setVisibility(View.VISIBLE);
		newButton.setVisibility(View.GONE);
		deleteContact.setVisibility(View.GONE);
		addNewMileage.setVisibility(View.GONE);
		// pickCurrent is the button used to pick a contact from the phone's list of contacts to be associate with the current case
		pickCurrent.setText("Import Existing Contact");
		//These buttons may be utilized at a later date
		newButton.setText("New Contact");
		deleteContact.setText("Delete Contact");
		
		//currently this button is invisible so this code is never reached, here for possible later implementation
		deleteContact.setOnClickListener(new View.OnClickListener() {
			
			@Override
			  public void onClick(View view) 
			{
				if(deleteContact.getText()=="Delete Contact")
				{
					deleteContact.setText("Done");
				}
				else
				{
					deleteContact.setText("Delete Contact");
				}
			}		 
		});
		
		//currently this button is invisible so this code is never reached, here for possible later implementation
		newButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			  public void onClick(View view) 
			{
				Intent intent = new Intent(Intent.ACTION_INSERT);
				intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
				startActivity(intent);
			}		 
		});
		
		//when you click this button it creates a pop up window with a new intent to all the contacts on the phone, it lets you pick on and returns and associates it with the current case
		// It actually gets associated with the case in onActivityResult below
		pickCurrent.setOnClickListener(new View.OnClickListener() {
			
			@Override
			  public void onClick(View view) 
			{
				final int CONTACT_PICKER_RESULT = 1001; 
				Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,  
			            ContactsContract.Contacts.CONTENT_URI);  
			    startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
			}		 
		});
		
		
		//Creates a cursor and content resolver, used to query data about the contacts stored in the Android OS
		ContentResolver cr = getActivity().getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		
		//checks to see if contact list is already populated and if any contacts are stored in the phone
		if (cur.getCount() > 0 && created==false) {
		while (cur.moveToNext()) {
			created=true;
			
			//Create a new contact object and query the contact list to populate it with all needed information, contact id, name , phone number, picture
			//It then adds the new Contact to the Contacts list
			
			Contact c=new Contact();
			c.setID(cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID)));
			c.setPicture(loadContactPhoto(cr,Long.valueOf(c.getID())));
			c.setName(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
		    if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
		      // This inner cursor is for contacts that have multiple numbers.
		      Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { c.getID() }, null);
		      int PhoneIdx = pCur.getColumnIndex(Phone.DATA);
		      while (pCur.moveToNext()) {
		    	  c.getNumbers().add(pCur.getString(PhoneIdx));
		    	  Contacts.add(c);		        
		      }
		      pCur.close();
		    }
		  }
		}
		cur.close();
	}
	
	//A function used to get the contact's picture and convert it into a bitmap that can be stored/displayed
	public static Bitmap loadContactPhoto(ContentResolver cr, long  id) {
	    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
	    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
	    if (input == null) {
	        return null;
	    }
	    return BitmapFactory.decodeStream(input);
	}
	
	
	/*
	 * This is a custom made adapter for contacts. An adpater is used to populate a listview.
	 * This adapter is used to inflate a listview in this fragment
	 * Each row of the list that gets generated has it's own custom xml layout file that gets inflated
	 * The layout file displays the information for each contact in each space of the listview
	 * It displays their current picture, name, phone number, etc.
	 * Also contains buttons used to delete the contact's association with this case as well as editing or calling the contact
	 */
	private class ContactAdapter extends ArrayAdapter<Contact> {
		public ContactAdapter(ArrayList<Contact> contacts) {
			super(getActivity(), 0, contacts); 
		}
		
		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.contact_item,  null); 
				
			}
			
			// configure the view of each list item
			final Contact c = getItem(pos); 
			
			ImageView pic= (ImageView)convertView.findViewById(R.id.contact_pic);
			pic.setImageBitmap(c.getPicture());
			
			TextView name = (TextView) convertView.findViewById(R.id.contact_display);
			name.setText(c.getName());
			
			TextView number = (TextView) convertView.findViewById(R.id.contact_number);
			number.setText(c.getNumbers().get(0));
			
			//set listeners for the three buttons on the end of each list item
			Button deleteB=(Button)convertView.findViewById(R.id.delete_button);
			final int innerpos=pos;
			deleteB.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					Long id =Long.parseLong(CaseContacts.get(innerpos).getID());
					CaseContacts.remove(innerpos);
					caseContactsDao.deleteByKey(id);
					adapter.notifyDataSetChanged();
				}
			});
			
			
			Button B =(Button)convertView.findViewById(R.id.call_button);
			B.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					String number = "tel:" + c.getNumbers().get(0);
			        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number)); 
			        startActivity(callIntent);
				}
			});
			
			Button B2 =(Button)convertView.findViewById(R.id.edit_button);
			B2.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					 Intent editintent = new Intent(Intent.ACTION_VIEW);
					    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(c.getID()));
					    editintent.setData(uri);
					startActivity(editintent);
				}
			});
			
			
			return convertView; 
		}
	}
	
	//Used to get the contact that was selected when associating a contact with a case
	//Then creates that contact's ID in the GreenDao database to be loaded in onResume
	public void onActivityResult(int requestCode, int resultCode, Intent data) {  
	    if (resultCode == Activity.RESULT_OK) {  
	    	Bundle extras = data.getExtras();  
	    	
	    	   if (resultCode == Activity.RESULT_OK) {

	    	     Uri contactData = data.getData();
	    	     Cursor c =  getActivity().managedQuery(contactData, null, null, null, null);
	    	     if (c.moveToFirst()) {


	    	         String id =c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
	    	         
	    	         Long IDnum=Long.parseLong(id);
	    	         CaseContacts CC=new CaseContacts(IDnum, parentID, IDnum);
	    	         caseContactsDao.insertOrReplace(CC);
	    	     }
	    	     }
	    }  
	} 
	
	//On resume is overridden to refresh the listview after you return to this page. 
	//This is needed after you retrieve a contact to associate with this case so it is displayed
	
	@Override
	public void onResume()
	{
		super.onResume();
		pickCurrent.setVisibility(View.VISIBLE);
		newButton.setVisibility(View.GONE);
		deleteContact.setVisibility(View.GONE);
		addNewMileage.setVisibility(View.GONE);
		
		CaseContacts.clear();
		contactIDs.clear();
		
		ArrayList<CaseContacts> caseContactsList = (ArrayList<CaseContacts>)caseContactsDao.queryBuilder().where(CaseContactsDao.Properties.CaseID.eq(parentID)).list();
		for(CaseContacts cc: caseContactsList)
		{
			if(cc.getCaseID()==parentID)
			{
				contactIDs.add(cc.getContactID());
			}
		}
		for(Contact c: Contacts)
		{
			for(Long l : contactIDs)
			{
			if(Long.parseLong(c.getID())==l)
			{
				CaseContacts.add(c);
			}
			}
		}
		
		
		adapter = new ContactAdapter(CaseContacts); 
		setListAdapter(adapter);
		
	}
}
