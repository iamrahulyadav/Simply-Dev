package com.builder.ibalance;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.appsflyer.AppsFlyerLib;
import com.builder.ibalance.adapters.DeductionListRecycleAdapter;

import com.builder.ibalance.database.helpers.BalanceHelper;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


/**
 * 
 * Fragment to handle the deductions.
 *
 */
public class DeductionsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
	
	@Override
	public void onResume() {
		Tracker t = ((MyApplication) getActivity().getApplication()).getTracker(
			    TrackerName.APP_TRACKER);

			// Set screen name.
			t.setScreenName("DeductionsHistoryScreen");

			// Send a screen view.
			t.send(new HitBuilders.ScreenViewBuilder().build());

			// Log the timed event when the user starts reading the article
			// setting the third param to true creates a timed event
			FlurryAgent.logEvent("DeductionsHistoryScreen", true);
			AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"DeductionsHistoryScreen","");
			// End the timed event, when the user navigates away from article

			super.onResume();
		}

		@Override
		public void onPause() {

			FlurryAgent.endTimedEvent("DeductionsHistoryScreen");
			super.onPause();
		}

	//private ListView mListView;
	RecyclerView mListView;
	//private DeductionsAdapter mDeductionsAdapter;
	private DeductionListRecycleAdapter mDeductionsAdapter;
	
	public DeductionsFragment() {
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_deductions, container, false);
		super.onActivityCreated(savedInstanceState);
		((AppCompatActivity)getActivity()).getSupportLoaderManager().initLoader(1,null,this);
		//mListView = (ListView) view.findViewById(R.id.listview_deductions);
		mListView = (RecyclerView) view.findViewById(R.id.deduction_list_recycler);
		LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getActivity());
		mListView.setLayoutManager(linearLayoutManager1);
		mListView.setHasFixedSize(true);
		//mListView.addItemDecoration();
		return view;

	}
	
	/*@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//final Cursor cursor = new BalanceHelper().getData();
		//cursor.moveToFirst();
		//mDeductionsAdapter = new DeductionsAdapter(getActivity(), cursor, false);
		//mListView.setAdapter(mDeductionsAdapter);
		
	}*/

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity().getApplicationContext()){
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = new BalanceHelper().getData();
				Log.e("Length",""+cursor.getCount());
				return cursor;
			}
		};

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		mDeductionsAdapter = new DeductionListRecycleAdapter(getActivity(), data, false);
		mListView.setAdapter(mDeductionsAdapter);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}
}
