package com.builder.ibalance;

/*
public class RechargesHistoryFragment extends  Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

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
			FlurryAgent.logEvent("RechargeHistoryScreen", true);
			AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"RechargeHistoryScreen","");
		super.onResume();
	}

	@Override
	public void onPause() {
		FlurryAgent.endTimedEvent("RechargeHistoryScreen");
		super.onPause();
	}

	//private ListView mListView;
	private RecyclerView mListView;
	private RecycleRechargesAdapter mRechargesAdapter;

	public RechargesHistoryFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_recharges_history, container, false);
		//mListView = (ListView) view.findViewById(R.id.listview_recharges);
		((AppCompatActivity)getActivity()).getSupportLoaderManager().initLoader(2,null,this);
		mListView = (RecyclerView) view.findViewById(R.id.recharge_history_recycler);
		LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getActivity());
		mListView.setLayoutManager(linearLayoutManager1);
		mListView.setHasFixedSize(true);
		return view;
	}

	*/
/*@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		*//*
*/
/*final Cursor cursor = new RechargeHelper().getData();
		cursor.moveToFirst();
		mRechargesAdapter = new RechargesAdapter(getActivity(), cursor, false);
		//mListView.setAdapter(mRechargesAdapter);*//*
*/
/*

	}*//*



	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity().getApplicationContext()){
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = new RechargeHelper().getData();
				return cursor;
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mRechargesAdapter = new RecycleRechargesAdapter(getActivity(), data, false);
		mListView.setAdapter(mRechargesAdapter);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}
}
*/
