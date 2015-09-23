package com.builder.ibalance;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.appsflyer.AppsFlyerLib;
import com.apptentive.android.sdk.Apptentive;
import com.builder.ibalance.adapters.DeductionsAdapter;
import com.builder.ibalance.database.BalanceHelper;
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
public class DeductionsFragment extends Fragment {
	
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

			Apptentive.engage(this.getActivity(), "DeductionsHistoryScreen");
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

	private ListView mListView;
	private DeductionsAdapter mDeductionsAdapter;
	
	public DeductionsFragment() {
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_deductions, container, false);
		mListView = (ListView) view.findViewById(R.id.listview_deductions);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Cursor cursor = new BalanceHelper().getData();
		cursor.moveToFirst();
		mDeductionsAdapter = new DeductionsAdapter(getActivity(), cursor, false);
		mListView.setAdapter(mDeductionsAdapter);
		
	}

}
