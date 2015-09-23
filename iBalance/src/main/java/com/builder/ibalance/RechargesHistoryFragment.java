package com.builder.ibalance;

import java.util.HashMap;
import java.util.Map;

import com.appsflyer.AppsFlyerLib;
import com.apptentive.android.sdk.Apptentive;
import com.builder.ibalance.adapters.RechargesAdapter;
import com.builder.ibalance.database.RechargeHelper;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class RechargesHistoryFragment extends Fragment{

	@Override
	public void onResume() {
		Tracker t = ((MyApplication) getActivity().getApplication()).getTracker(
			    TrackerName.APP_TRACKER);

			// Set screen name.
			t.setScreenName("RechargeHistoryScreen");

			// Send a screen view.
			t.send(new HitBuilders.ScreenViewBuilder().build());

			// Log the timed event when the user starts reading the article
			// setting the third param to true creates a timed event
			Apptentive.engage(this.getActivity(), "RechargeHistoryScreen");
			FlurryAgent.logEvent("RechargeHistoryScreen", true);
			AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"RechargeHistoryScreen","");
		super.onResume();
	}

	@Override
	public void onPause() {
		FlurryAgent.endTimedEvent("RechargeHistoryScreen");
		super.onPause();
	}

	private ListView mListView;
	private RechargesAdapter mRechargesAdapter;

	public RechargesHistoryFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_recharges_history, container, false);
		mListView = (ListView) view.findViewById(R.id.listview_recharges);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Cursor cursor = new RechargeHelper().getData();
		cursor.moveToFirst();
		mRechargesAdapter = new RechargesAdapter(getActivity(), cursor, false);
		mListView.setAdapter(mRechargesAdapter);

	}
	


}
