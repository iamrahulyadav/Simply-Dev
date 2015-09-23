package com.builder.ibalance.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.ArrayList;

public class MappingHelper {
	final String tag = MappingHelper.class.getSimpleName();
	SQLiteDatabase myDB;
	Cursor cursor = null;
	public MappingHelper(Context ctx) {
		//Log.d(tag, "Creating Databse");
		 try {
			MySQLiteHelper.getInstance(ctx).createDataBase();
		} catch (IOException e) {
			//Log.d(tag, "There was a problem in creating database");
			e.printStackTrace();
		
		}
		 myDB = MySQLiteHelper.getInstance(ctx).getReadableDatabase();
	}
	
	public ArrayList<String> getMapping(int number) {
		
		ArrayList<String> temp = new ArrayList<String>();
		if(myDB==null)
		{
			//Log.d(tag, "cant open");
			temp.add("Unknown");
			temp.add("Unknown");
			return temp;

		}
		else
		{
			String query = "SELECT * FROM NUMBER_MAPPING WHERE _id = \""+number+"\"";

			cursor =  myDB.rawQuery(query, null);

			if(cursor.moveToFirst()==false)
			{
				temp.add("Unknown");
				temp.add("Unknown");
				return temp;

			}
			else
			{
				temp.add(cursor.getString(1));
				temp.add(cursor.getString(2));

			}
			cursor.close();
		}
		return temp;
	}

	public void close() {
		myDB.close();
		
	}
	

}
