package com.builder.ibalance;


/**
 * 
 * Activity to show history.
 *
 */
/*
public class HistoryActivity extends AppCompatActivity  {
	
	private ViewPager mViewPager;
	private RechargeDeductionsPagerAdapter mAdapter;
	 @Override
	    protected void onStart() {
	        super.onStart();

	    }

	    @Override
	    protected void onStop() {
	        super.onStop();
	    } 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);

		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		TabLayout tabLayout =(TabLayout)findViewById(R.id.tablayout);
		toolbar.setContentInsetsAbsolute(0, 0);

		final ActionBar ab = getSupportActionBar();

		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeButtonEnabled(true);
		ab.setTitle("Transactions");



		mAdapter = new RechargeDeductionsPagerAdapter(getFragmentManager());
		

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);
		mViewPager.setOffscreenPageLimit(1);
        //actionBar.addTab(actionBar.newTab().setText("Deductions").setTabListener(this));
        //actionBar.addTab(actionBar.newTab().setText("Recharges").setTabListener(this));
		for (int i = 0; i < mAdapter.getCount(); i++) {
			tabLayout.addTab(tabLayout.newTab().setText(mAdapter.getPageTitle(i)));
		}
		//tabLayout.addTab(tabLayout.newTab().setText("Recharges"));
		//tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

		tabLayout.setupWithViewPager(mViewPager);

	}

	*/
/*@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
	}*//*

	
	*/
/**
	 * 
	 * Pager adapter to load the fragments.
	 *
	 *//*

	public static class RechargeDeductionsPagerAdapter extends FragmentPagerAdapter {

		public RechargeDeductionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public Fragment getItem(int position) {
			
			switch (position) {
			case 0:
				return new DeductionsFragment();
				
			case 1:
				return new RechargesHistoryFragment();

			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}
		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
				case 0:
					return "DEDUCTIONS";

				case 1:
					return "RECHARGES";

			}
			return null;
		}
		
	}

}
*/
