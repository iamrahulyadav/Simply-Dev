package com.builder.ibalance.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.builder.ibalance.R;
import com.builder.ibalance.Wizard;
import com.builder.ibalance.util.MyApplication;
import com.squareup.picasso.Picasso;


public class TutorialAdapter extends PagerAdapter {
 
    LayoutInflater mLayoutInflater;
    Context mContext;
    ImageView imageView ;
    public TutorialAdapter(Context context) {
    	mContext = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    int[] mResources = {
            R.drawable.splash_screen_1,
            R.drawable.splash_screen_2,
            R.drawable.splash_screen_3,
            R.drawable.splash_screen_4,
            R.drawable.splash_screen_5,
            R.drawable.splash_screen_5,
    };
    @Override
    public int getCount() {
        return mResources.length;
    }
 
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }
 
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.fragment_tutorial, container, false);
	
         imageView = (ImageView) itemView.findViewById(R.id.imageView);
         Picasso.with(mContext).load(mResources[position]).into(imageView);
        //imageView.setImageResource(mResources[position]);
 
        container.addView(itemView);
 
        return itemView;
    }
 
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}/*extends FragmentPagerAdapter {

    public TutorialAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	private int pagerCount = 1;


   

    @Override public Fragment getItem(int i) {
    	switch (i) {
		case 0:
			return ColorFragment.newInstance(R.drawable.t1);
		case 1:
			return ColorFragment.newInstance(R.drawable.t2);

		case 2:
			return ColorFragment.newInstance(R.drawable.t3);
		case 3:
			return ColorFragment.newInstance(R.drawable.t4);
		default:
			break;
		}
		return null;
    }

    @Override public int getCount() {
        return pagerCount;
    }
}*/