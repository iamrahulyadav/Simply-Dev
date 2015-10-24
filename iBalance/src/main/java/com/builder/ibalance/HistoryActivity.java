package com.builder.ibalance;


import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

/**
 * 
 * Activity to show history.
 *
 */
public class HistoryActivity extends FragmentActivity implements ActionBar.TabListener {
	
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
		
		mAdapter = new RechargeDeductionsPagerAdapter(getFragmentManager());
		
		final ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle("Transactions");
		// Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);/*
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setCustomView(R.layout.custom_history_title);*/
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });
        
        actionBar.addTab(actionBar.newTab().setText("Deductions").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("Recharges").setTabListener(this));
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
	}
	
	/**
	 * 
	 * Pager adapter to load the fragments.
	 *
	 */
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
		
	}

}
