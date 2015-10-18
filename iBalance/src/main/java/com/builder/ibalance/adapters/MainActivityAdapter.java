package com.builder.ibalance.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;

import com.builder.ibalance.BalanceFragment;
import com.builder.ibalance.CallPatternFragment;
import com.builder.ibalance.ContactsFragment;
import com.builder.ibalance.R;
import com.builder.ibalance.RechargeFragment;
import com.builder.ibalance.util.MyApplication;

import java.util.Locale;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class MainActivityAdapter extends FragmentPagerAdapter  {
	private Fragment mRechargeFragment;

	public Fragment getmRechargeFragment() {
		return mRechargeFragment;
	}




	int position = 0;
	final String TAG = FragmentStatePagerAdapter.class.getSimpleName();

	public MainActivityAdapter(FragmentManager fm) {
		super(fm);
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
			return new BalanceFragment();
		case 1:
			this.position=1;
				return  new CallPatternFragment();

			//return new CallPatternFragment();	
		case 2:
			this.position=2;
			return new ContactsFragment();
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
            default: return null;
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
			return MyApplication.context.getResources().getString(R.string.title_section1)
					.toUpperCase(l);
		case 1:
			return MyApplication.context.getString(R.string.title_section2).toUpperCase(l);
		case 2:
			return MyApplication.context.getString(R.string.title_section3).toUpperCase(l);
		case 3:
			return MyApplication.context.getString(R.string.title_section4).toUpperCase(l);
		}
		return null;
	}
	
}