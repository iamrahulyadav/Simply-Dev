package com.builder.ibalance.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;

import com.builder.ibalance.BalanceFragment;
import com.builder.ibalance.CallPatternFragment;
import com.builder.ibalance.ContactsFragment;
import com.builder.ibalance.MainActivity.PlaceholderFragment;
import com.builder.ibalance.R;
import com.builder.ibalance.RechargeFragment;

import java.util.Locale;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class MainActivityAdapter extends FragmentPagerAdapter  {
	private Fragment mCallPatternFragment,mContFragment,mBalanceFragment,mRechargeFragment;
	
	public Fragment getmBalanceFragment() {
		return mBalanceFragment;
	}

	/**
	 * @return the mContFragment
	 */
	public Fragment getmContFragment() {
		return mContFragment;
	}


	public static int  pos = 0;
	/**
	 * @return the mCallPatternFragment
	 */
	public Fragment getmCallPatternFragment() {
		return mCallPatternFragment;
	}


	public Fragment getmRechargeFragment() {
		return mRechargeFragment;
	}




	int position = 0;
	final String TAG = FragmentStatePagerAdapter.class.getSimpleName();
	Context ctx;

	public MainActivityAdapter(FragmentManager fm, Context ctx) {
		super(fm);
		this.ctx = ctx;
	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		// Return a PlaceholderFragment (defined as a static inner class
		// below).
	
		//Log.d(TAG, "Shabazz Position = " + position);
		switch (position) {
		case 0:
			this.position = 0;
			if(mBalanceFragment==null)
				{

				//Log.d(TAG, "Creating New BalanceFragment");
				mBalanceFragment = new BalanceFragment();
				}
			return mBalanceFragment;
		case 1:
			this.position=1;
			
			if(mCallPatternFragment==null)
			{
				//Log.d(TAG, "Creating New CallPattternFragment");
				mCallPatternFragment= new CallPatternFragment();
				
			}
			return mCallPatternFragment;
				
			//return new CallPatternFragment();	
		case 2:
			this.position=2;
			if(mContFragment==null)
			{
				//Log.d(TAG, "Creating New mContFragment");
				mContFragment= new ContactsFragment();
				
			}
			return mContFragment;
			//return new ContactsFragment();	
		case 3:
			this.position=4;
			if(mRechargeFragment==null)
			{
				//Log.d(TAG, "Creating New mRechargeFragment");
				mRechargeFragment = new RechargeFragment();
			}
			return mRechargeFragment;
			//return new RechargeFragment();	

		default:
			return PlaceholderFragment.newInstance(position + 1);
		}
		
	}

	@Override
	public int getCount() {
		// Show 3 total pages.
		return 4;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case 0:
			return ctx.getResources().getString(R.string.title_section1)
					.toUpperCase(l);
		case 1:
			return ctx.getString(R.string.title_section2).toUpperCase(l);
		case 2:
			return ctx.getString(R.string.title_section3).toUpperCase(l);
		case 3:
			return ctx.getString(R.string.title_section4).toUpperCase(l);
		}
		return null;
	}
	
}