package com.example.lawyerapp;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

//This Class implements the fragment for files placed in each case view upon flipping to the files tab


public class FileFrag extends ListFragment {

	//buttons found in the xml layout file
	private Button photoButton;
	private Button addNewExpense, addNewMileage, deleteFile;
	
	private Cursor cursor;
	
	//used by GreenDao to interact with our database
	private DaoInstance daoinstance;
	private FilesDao filesDao;
	private SQLiteDatabase db;
	
	private Long parentID; // stores the current case ID
	//strings that get switched out on a button when deleting files
	private String deleteFileStr, doneFileStr;
	
	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
			
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle saved) {
		View v = inflater.inflate(R.layout.file_frag, parent,false); 
		
		deleteFileStr = "Delete Photo";
        doneFileStr = "Done";
		
        //Gets our specific instance of GreenDao stuff
		daoinstance = DaoInstance.getInstance(getActivity());
		
		db = daoinstance.getDb();
		
		filesDao = daoinstance.getFilesDao();
		
		parentID = getActivity().getIntent().getExtras().getLong("id");
		
		Log.i("CS499", parentID.toString());
		
		//Finds all of the specific buttons on our layout
		photoButton = (Button) getActivity().findViewById(R.id.buttonNewHours);
		addNewExpense = (Button) getActivity().findViewById(R.id.buttonNewExpense);
		addNewMileage = (Button) getActivity().findViewById(R.id.buttonNewMileage);
		deleteFile = (Button) getActivity().findViewById(R.id.buttonDelete);
		//we don't need these buttons on this fragment so set to invisible
		addNewExpense.setVisibility(View.INVISIBLE);
		addNewMileage.setVisibility(View.INVISIBLE);
		
		photoButton.setText("New Photo");
		deleteFile.setText("Delete Photo");
		
		
		//when you click this button it launches an intent to photoIntentActivity
		//PhotoIntentActivity allows you to launch the picture taking intent, then name the picture and store it into the database
		photoButton.setOnClickListener(new View.OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{
				Intent takePhoto = new Intent(getActivity(), PhotoIntentActivity.class);
				
				startActivityForResult(takePhoto, 1);
			}
			
		});
		
		
		//Swaps out the string on the delete button, 
		//It changes the delete button into a done button to click to end the deleting process
		//also sets the take photo button to invisible while deleting
		
		deleteFile.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				if (deleteFile.getText() == deleteFileStr)
				{
					deleteFile.setText(doneFileStr);
					
					photoButton.setVisibility(View.INVISIBLE);
				}
				else
				{
					deleteFile.setText(deleteFileStr);
					
					photoButton.setVisibility(View.VISIBLE);
				}
			}
		});
		
		/*
		 * The lines of code below query the database to see if there are any files currently associated with this case
		 * If there are it puts them in a list adapter
		 * Then you set the listview in the middle of the screen to use the adapter
		 * This displays a list of all files associated with this case currently
		 */
		
		
		String textColumn = FilesDao.Properties.Name.columnName;
		
		//String dateColumn = FilesDao.Properties.Date.columnName;
        String orderBy = textColumn + " COLLATE LOCALIZED DESC";
		
        cursor = db.query(filesDao.getTablename(), null, "PARENT_ID IN " +
        		"(SELECT PARENT_ID FROM FILES WHERE PARENT_ID = " + parentID.toString() + ")"
        		, null, null, null, orderBy);
        
        //cursor = db.query(filesDao.getTablename(), null, null, null, null, null, null, null);
        
        String[] from = {textColumn};
        int[] to = { R.id.textView1};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item2, cursor, from,
                to);
        setListAdapter(adapter);
		
		return v; 
	}
	
	//Set the correct buttons visible/invisible when you return to this fragment
	@Override
	public void onResume()
	{
		super.onResume();
		
		addNewExpense.setVisibility(View.INVISIBLE);
		addNewMileage.setVisibility(View.INVISIBLE);
		photoButton.setVisibility(View.VISIBLE);
		deleteFile.setVisibility(View.VISIBLE);
		
		photoButton.setText("New Photo");
		deleteFile.setText("Delete Photo");
	}
	
	//This function gets the picture that is returned but the take picture intent
	//It then stores it into the database where it gets displayed in a listview
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if (resultCode == Activity.RESULT_OK)
		{
			String name = "";
			name = data.getExtras().getString("name");
			
			String path = "";
			path = data.getExtras().getString("path");
			
			Files file = new Files(null, name, parentID, "photo", path);
	        filesDao.insertOrReplace(file);
			
	        cursor.requery();
		}
	}
	
	/*This function sets what to do when you click on an item in the listview
	 * If you have clicked the delete button then it will delete the file you clicked from the database
	 * If delete mode is not active then click on a file will cause that file to open a new intent
	 * So for example click on a photo in the listview will let you view a full screen intent of that photo
	 */
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) 
	{
		final Files newFile = filesDao.queryBuilder().where(FilesDao.Properties.Id.eq(id)).unique();
		
		if (deleteFile.getText() == deleteFileStr)
		{	
			Intent newInt = new Intent(getActivity(), PhotoView.class);
			newInt.putExtra("path", newFile.getPath());
			newInt.putExtra("name", newFile.getName());
			
			startActivity(newInt);
		}
		else
		{
			File file = new File(newFile.getPath());
			boolean deleted = file.delete();
			
			filesDao.deleteByKey(id);
		}
		
		cursor.requery();
	}
}
