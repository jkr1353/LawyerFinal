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

public class ContactFrag extends ListFragment {

	private ArrayList<Contact> Contacts = new ArrayList<Contact>();
	private ArrayList<Contact> CaseContacts = new ArrayList<Contact>();
	private ArrayList<Long> contactIDs = new ArrayList<Long>();
	
	private boolean created=false;
	private Button newButton, pickCurrent, addNewMileage, deleteContact;
	
	private DaoInstance daoinstance;
	private CaseContactsDao caseContactsDao;
	private Long parentID;
	private ContactAdapter adapter;
	
	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		
		newButton = (Button) getActivity().findViewById(R.id.buttonNewHours);
		pickCurrent = (Button) getActivity().findViewById(R.id.buttonNewExpense);
		addNewMileage = (Button) getActivity().findViewById(R.id.buttonNewMileage);
		deleteContact = (Button) getActivity().findViewById(R.id.buttonDelete);
		
		parentID = getActivity().getIntent().getExtras().getLong("id");
		daoinstance = DaoInstance.getInstance(getActivity());
		caseContactsDao= daoinstance.getCaseContactsDao();
		
		pickCurrent.setVisibility(View.VISIBLE);
		newButton.setVisibility(View.GONE);
		deleteContact.setVisibility(View.GONE);
		
		addNewMileage.setVisibility(View.GONE);
		
		pickCurrent.setText("Import Existing Contact");
		newButton.setText("New Contact");
		deleteContact.setText("Delete Contact");
		
		
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
		
		newButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			  public void onClick(View view) 
			{
				Intent intent = new Intent(Intent.ACTION_INSERT);
				intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

				// Just two examples of information you can send to pre-fill data
				//intent.putExtra(ContactsContract.Intents.Insert.NAME, "Dave Smith");
				startActivity(intent);
			}		 
		});
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
		
		
		
		ContentResolver cr = getActivity().getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

		if (cur.getCount() > 0 && created==false) {
		while (cur.moveToNext()) {
			created=true;
			Contact c=new Contact();
			c.setID(cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID)));
			c.setPicture(loadContactPhoto(cr,Long.valueOf(c.getID())));
		    //String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
			c.setName(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
		    //String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
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
		/*
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
		
		
		ContactAdapter adapter = new ContactAdapter(CaseContacts); 
		setListAdapter(adapter);
		*/
	}
	
	public static Bitmap loadContactPhoto(ContentResolver cr, long  id) {
	    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
	    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
	    if (input == null) {
	        return null;
	    }
	    return BitmapFactory.decodeStream(input);
	}
	
	
	
	private class ContactAdapter extends ArrayAdapter<Contact> {
		public ContactAdapter(ArrayList<Contact> contacts) {
			super(getActivity(), 0, contacts); 
		}
		
		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.contact_item,  null); 
				
			}
			
			// configure the view
			final Contact c = getItem(pos); 
			
			ImageView pic= (ImageView)convertView.findViewById(R.id.contact_pic);
			pic.setImageBitmap(c.getPicture());
			
			TextView name = (TextView) convertView.findViewById(R.id.contact_display);
			name.setText(c.getName());
			
			TextView number = (TextView) convertView.findViewById(R.id.contact_number);
			number.setText(c.getNumbers().get(0));
			
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
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {  
	    if (resultCode == Activity.RESULT_OK) {  
	    	Bundle extras = data.getExtras();  
	    	
	    	   if (resultCode == Activity.RESULT_OK) {

	    	     Uri contactData = data.getData();
	    	     Cursor c =  getActivity().managedQuery(contactData, null, null, null, null);
	    	     if (c.moveToFirst()) {


	    	         String id =c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
	    	         
	    	         Long IDnum=Long.parseLong(id);
	    	         /*
	    	         Contact contact=new Contact();
	    	         contact.setID(id);
	    	         contact.setPicture(loadContactPhoto(getActivity().getContentResolver(),Long.valueOf(contact.getID())));
	    	         contact.setName(c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)));
	    	         if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
	    			      // This inner cursor is for contacts that have multiple numbers.
	    			      Cursor pCur = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { contact.getID() }, null);
	    			      int PhoneIdx = pCur.getColumnIndex(Phone.DATA);
	    			      while (pCur.moveToNext()) {
	    			    	  contact.getNumbers().add(pCur.getString(PhoneIdx));
	    			    	  Contacts.add(contact);		        
	    			      }
	    			      pCur.close();
	    			    }
	    			    */
	    	         CaseContacts CC=new CaseContacts(IDnum, parentID, IDnum);
	    	         caseContactsDao.insertOrReplace(CC);
	    	     }
	    	     }
	    }  
	} 
	
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
