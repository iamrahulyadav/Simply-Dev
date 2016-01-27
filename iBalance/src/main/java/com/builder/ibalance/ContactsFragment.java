package com.builder.ibalance;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.appsflyer.AppsFlyerLib;
import com.builder.ibalance.adapters.CustomContactsRecyclerAdapter;
import com.builder.ibalance.database.helpers.BalanceHelper;
import com.builder.ibalance.database.helpers.ContactDetailHelper;
import com.builder.ibalance.database.helpers.IbalanceContract;
import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.messages.DataLoadingDone;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class ContactsFragment extends Fragment implements OnItemClickListener {
	final String TAG = "ContactsFragment";
	private View view;
	private ProgressDialog pDialog;
	private ListView listView;
	private RecyclerView recyclerView;
	//private CustomContactsAdapter adapter;
	private CustomContactsRecyclerAdapter adapter;
	List<ContactDetailModel> contactsList;
	ContactsLoader mContactsLoader;

	// ContactsFragment ctx;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v =  inflater.inflate(R.layout.fragment_list_view, container, false);
		recyclerView = (RecyclerView)v.findViewById(R.id.recyclerview);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setHasFixedSize(true);

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
		//listView.setOnItemClickListener(this);

		return v;

	}

	private void loadData() {
		
		
			
		mContactsLoader = new ContactsLoader();
		mContactsLoader.execute(0);


	}

    @Override
    public void onStart()
    {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
		try
		{
			if (mContactsLoader != null) mContactsLoader.cancel(false);
		}
		catch (Exception e)
		{
			Crashlytics.logException(e);
		}
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
    public void onEvent(DataLoadingDone m)
    {
        loadData();
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
		FlurryAgent.logEvent("ContactScreen", true);
		AppsFlyerLib.sendTrackingWithEvent(MyApplication.context, "Contact Screen", "");
		// End the timed event, when the user navigates away from article

		super.onResume();
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
		mIntent.putExtra("DETAILS", (ContactDetailModel) arg1.getTag());
		//Log.d(TAG, "Phnumber = " + arg1.getTag().toString());
		startActivity(mIntent);
		// Toast.makeText(getActivity().getApplicationContext(), ,
		// Toast.LENGTH_SHORT).show();

	}


	public class ContactsLoader extends
			AsyncTask<Integer, Integer, List<ContactDetailModel>>  {


		public class customComparator implements Comparator<ContactDetailModel> {
			public int compare(ContactDetailModel object1, ContactDetailModel object2) {
				if (object1.total_cost == object2.total_cost)
					return 0;
				if (object1.total_cost > object2.total_cost)
					return -1;
				else
					return 1;
			}
		}

		@Override
		protected List<ContactDetailModel> doInBackground(Integer... a) {

			if(contactsList!=null)
				return contactsList;
            contactsList = new ArrayList<ContactDetailModel>();
			ContactDetailHelper mContactDetailHelper = new ContactDetailHelper();
            BalanceHelper mBalanceHelper = new BalanceHelper();
            Cursor c = mContactDetailHelper.getAllContacts();
            String number="",name="",carrier="",circle="",image_uri="";
            int in_ct=0,in_dur=0,out_c=0,out_dur=0,miss_c=0;
            float call_cost = 0;
            int num_idx = c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_NUMBER);
            int name_idx = c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_NAME);
            int carrier_idx = c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_CARRIER);
            int circle_idx = c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_CIRCLE);
            int img_idx = c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_IMAGE_URI);
            int in_ct_idx = c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_IN_COUNT);
            int in_d_idx = c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_IN_DURATION);
            int out_c_idx = c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_OUT_COUNT);
            int out_d_idx = c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_OUT_DURATION);
            int miss_idx = c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_MISS_COUNT);
            SharedPreferences mSharedPreferences = MyApplication.context
                    .getSharedPreferences("USER_DATA",
                            Context.MODE_PRIVATE);
            float call_rate = mSharedPreferences.getFloat("CALL_RATE",
                    1.7f);

            while(c.moveToNext())
            {
                number = c.getString(num_idx);
                name = c.getString(name_idx);
                carrier = c.getString(carrier_idx);
                circle = c.getString(circle_idx);
                image_uri = c.getString(img_idx);
                in_ct = c.getInt(in_ct_idx);
                in_dur = c.getInt(in_d_idx);
                out_c = c.getInt(out_c_idx);
                out_dur = c.getInt(out_d_idx);
                miss_c = c.getInt(miss_idx);
                if (number.length() >= 10 && !number.startsWith("1800"))
                {
                    call_cost = mBalanceHelper.getTotalCost(number,out_dur,call_rate);
                }
                else
                {
                    call_cost = 0f;
                }
                contactsList.add(new ContactDetailModel(
                        number,
                        name,
                        carrier,
                        circle,
                        image_uri,
                        in_ct,
                        in_dur,
                        out_c,
                        out_dur,
                        miss_c,
                        call_cost));
            }
            c.close();
           //V10Log.d(TAG, "Before Sorting" + contactsList.toString());
            Collections.sort(contactsList, new customComparator());
           //V10Log.d(TAG, "After Sorting" + contactsList.toString());
            return contactsList;
		}

		protected void onPostExecute(List<ContactDetailModel> contactsList1) {
			Log.d(TAG, "Contact Async Task Finished");
			//Log.d(TAG, "Done Initializing data");
			if(adapter==null)
			{
			//adapter = new CustomContactsAdapter(contactsList1);
				//adapter=(new CustomContactsRecyclerAdapter(contactsList1));
				adapter = (new CustomContactsRecyclerAdapter(MyApplication.context,contactsList1));
			}

			ScaleInAnimationAdapter animContactAdapter = new ScaleInAnimationAdapter(adapter);
			recyclerView.setAdapter(animContactAdapter);
			//listView.setAdapter(adapter);
			/*ProgressBar mProgressBar = (ProgressBar) view
					.findViewById(R.id.contact_data_Loading);
			mProgressBar.setVisibility(View.GONE);*/
			//listView.setVisibility(View.VISIBLE);
			recyclerView.getAdapter().notifyDataSetChanged();
			//adapter.notifyDataSetChanged();
		}



	}

}
