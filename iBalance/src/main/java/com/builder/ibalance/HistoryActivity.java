package com.builder.ibalance;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.Locale;

/**
 * 
 * Activity to show history.
 *
 */
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

		//toolbar.setLogo(R.drawable.ic_launcher);
		TabLayout tabLayout =(TabLayout)findViewById(R.id.tablayout);


		toolbar.setLogo(R.drawable.ic_launcher);
		toolbar.setContentInsetsAbsolute(0, 0);

		final ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeButtonEnabled(true);
		//ab.setDisplayShowCustomEnabled(true);
		ab.setTitle("Transactions");
		//toolbar.setTitle("Transactions");
		//ab.setC



		mAdapter = new RechargeDeductionsPagerAdapter(getFragmentManager());
		
		//final ActionBar actionBar = getActionBar();
		//actionBar.setHomeButtonEnabled(true);
		//actionBar.setTitle("Transactions");
		// Specify that we will be displaying tabs in the action bar.
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);/*
		//actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		//actionBar.setDisplayShowHomeEnabled(true);
		//actionBar.setHomeButtonEnabled(true);
		//actionBar.setCustomView(R.layout.custom_history_title);*/
        
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

	/*@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
	}*/
	
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
