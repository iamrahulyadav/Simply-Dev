package com.builder.ibalance;

/**
 * 
 * Fragment to handle the deductions.
 *
 */
/*
public class DeductionsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
	
	@Override
	public void onResume() {

			// End the timed event, when the user navigates away from article

			super.onResume();
		}

		@Override
		public void onPause() {

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


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity().getApplicationContext()){
			@Override
			public Cursor loadInBackground() {
				Cursor cursor = new BalanceHelper().getData();
				//Log.e("Length",""+cursor.getCount());
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
*/
