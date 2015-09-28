package com.builder.ibalance;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.appsflyer.AppsFlyerLib;
import com.apptentive.android.sdk.Apptentive;
import com.builder.ibalance.adapters.ContactsModel;
import com.builder.ibalance.adapters.CustomContactsAdapter;
import com.builder.ibalance.adapters.MainActivityAdapter;
import com.builder.ibalance.database.MySQLiteHelper;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

public class ContactsFragment extends Fragment implements OnItemClickListener {
	final String TAG = "ContactsFragment";
	private View view;
	private ProgressDialog pDialog;
	private ListView listView;
	private CustomContactsAdapter adapter;
	List<ContactsModel> contactsList;
	// ContactsFragment ctx;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_custom_contacts, container,
				false);
		//Log.d(TAG, "Contact Fragment In");
		listView = (ListView) view.findViewById(R.id.list);

		// ctx = this;
		// MainActivity.dateSelector.setVisible(false);
		/*
		 * pDialog = new ProgressDialog(this.getActivity()); // Showing progress
		 * dialog before initializing pDialog.setMessage("Loading...");
		 * if(isVisible()) pDialog.show();
		 */
		if (DataInitializer.done == true) {
			loadData();
		}

		// notifying list adapter about data changes
		// so that it renders the list view with updated data
		listView.setOnItemClickListener(this);

		return view;

	}

	private void loadData() {
		
		
			
			new ContactsLoader().execute(0);
		

	}

	public void loadDataAsync(MainActivityAdapter mMainActivityAdapter) {
		loadData();
		mMainActivityAdapter.notifyDataSetChanged();
	}

	private void hidePDialog() {
		if (pDialog != null) {
			pDialog.dismiss();
			pDialog = null;
		}
	}

	@Override
	public void onDestroy() {

		hidePDialog();
		super.onDestroy();
	}

	@Override
	public void onResume() {
		Tracker t = ((MyApplication) getActivity().getApplication()).getTracker(
			    TrackerName.APP_TRACKER);

			// Set screen name.
			t.setScreenName("Contact Screen");

			// Send a screen view.
			t.send(new HitBuilders.ScreenViewBuilder().build());

		// Log the timed event when the user starts reading the article
		// setting the third param to true creates a timed event
		Apptentive.engage(this.getActivity(), "Contact Screen");
		FlurryAgent.logEvent("ContactScreen", true);
		AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"Contact Screen","");
		// End the timed event, when the user navigates away from article

		super.onResume();
	}

	@Override
	public void onStop() {
		
		super.onStop();
	}

	@Override
	public void onPause() {

		FlurryAgent.endTimedEvent("ContactScreen");
		super.onPause();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Intent mIntent = new Intent(this.getActivity(),
				ContactDetailActivity.class);
		mIntent.putExtra("phnumber", arg1.getTag().toString());
		//Log.d(TAG, "Phnumber = " + arg1.getTag().toString());
		startActivity(mIntent);
		// Toast.makeText(getActivity().getApplicationContext(), ,
		// Toast.LENGTH_SHORT).show();

	}

	public class ContactsLoader extends
			AsyncTask<Integer, Integer, List<ContactsModel>>  {


		public class customComparator implements Comparator<ContactsModel> {
			public int compare(ContactsModel object1, ContactsModel object2) {
				if (object1.total_secs == object2.total_secs)
					return 0;
				if (object1.total_secs > object2.total_secs)
					return -1;
				else
					return 1;
			}
		}

		@Override
		protected List<ContactsModel> doInBackground(Integer... a) {
			Log.d(TAG, "Contact Async Task Started");
			if(contactsList==null )
			{
				Log.d(TAG, "Contact List is null Initializing");
			}
			else
			{
				Log.d(TAG, "Contact List is already exists");
			}
			if(contactsList!=null)
				return contactsList;
			contactsList = new ArrayList<ContactsModel>();
			MySQLiteHelper mSQLiteHelper = new MySQLiteHelper(MyApplication.context);
			SQLiteDatabase readableDB = mSQLiteHelper.getReadableDatabase();

			Object[] values;
			Cursor c = null;
			String query = "";
			// while(!DataInitializer.done);
			// key = number values =
			// name,InCount,InDur,OutCount,OutDur,MissCount,Provider,State,image_uri
			for (Entry<String, Object[]> entry : DataInitializer.mainmap
					.entrySet()) {
				values = (Object[]) entry.getValue();
				if (values[0] == null)
					values[0] = entry.getKey().toString();
				String[] st = new String[values.length + 1];
				st[0] = (entry.getKey().toString());
				int len = values.length;
				// //Log.d("Contacts Loader","Len = "+len);
				for (int i = 0; i < len; i++) {
					try {
						st[i + 1] = (values[i].toString());
					} catch (Exception e) {
						st[i + 1] = null;
					}
				}
				query = "select sum(COST), sum(DURATION) from CALL where NUMBER = \'+91"
						+ st[0] + "\'" + " OR NUMBER =\'" + st[0] + "\'";
				// //Log.d("Contacts Loader", "Query = "+ query);

				c = readableDB.rawQuery(query, null);
				c.moveToFirst();
				float callCost = (float) 0.0;
				int duration = 0;
				try {
					callCost = c.getFloat(0);
					duration = c.getInt(1);
				} catch (NullPointerException e) {
					e.printStackTrace();
				}

				// //Log.d("Contacts Loader","CallCost = "+callCost);
				int totalOutsecs = Integer.parseInt(st[5]);
				String total_duration_formatted = getTotalDurationFormatted(totalOutsecs);
				// if((callCost - 0.0)<0.01)
				if (st[0].length() < 10 || st[0].startsWith("1800")) {
					callCost = 0;
				} else {
					SharedPreferences mSharedPreferences = MyApplication.context
							.getSharedPreferences("USER_DATA",
									Context.MODE_PRIVATE);
					float call_rate = mSharedPreferences.getFloat("CALL_RATE",
							1.7f);
					callCost = (float) ((float) ((Integer.parseInt(st[5].toString()) - duration) * call_rate) / 100)
							+ callCost;
				}

				// //Log.d("Contacts Loader","TotalDuration = "+total_duration_formatted);
				contactsList
						.add(new ContactsModel(st[1], st[0], st[7], st[8],
								total_duration_formatted, totalOutsecs,
								callCost, st[9]));
				// s//Log.d("PAI", "ST[9] :: "+st[9]+" :: ST :: "+st);
				c.close();
			}
			readableDB.close();
			Collections.sort(contactsList, new customComparator());
			return contactsList;
		}

		protected void onPostExecute(List<ContactsModel> contactsList1) {
			Log.d(TAG, "Contact Async Task Finished");
			//Log.d(TAG, "Done Initializing data");
			if(adapter==null)
			adapter = new CustomContactsAdapter(contactsList1);
			listView.setAdapter(adapter);
			ProgressBar mProgressBar = (ProgressBar) view
					.findViewById(R.id.contact_data_Loading);
			mProgressBar.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			adapter.notifyDataSetChanged();
		}

		private String getTotalDurationFormatted(int totalSecs) {
			String min, sec, hr;
			Integer hrs, mins, secs;
			secs = totalSecs % 60;
			if (secs < 10)
				sec = "0" + secs;
			else
				sec = "" + secs;
			totalSecs = totalSecs / 60;
			mins = totalSecs % 60;
			if (mins < 10)
				min = "0" + mins;
			else
				min = "" + mins;
			totalSecs = totalSecs / 60;
			hrs = totalSecs;
			if (hrs < 10)
				hr = "0" + hrs;
			else
				hr = "" + hrs;
			return hr + ":" + min + ":" + sec;
		}

	}

}
