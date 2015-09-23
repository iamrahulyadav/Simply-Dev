package com.builder.ibalance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.builder.ibalance.adapters.TutorialAdapter;
import com.builder.ibalance.util.CircleIndicator;

public class Tutorial extends FragmentActivity implements OnClickListener {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_tutorial);
        
        // DEFAULT
        ViewPager defaultViewpager = (ViewPager) findViewById(R.id.viewpager_default);
        CircleIndicator defaultIndicator = (CircleIndicator) findViewById(R.id.indicator_default);
        TutorialAdapter defaultPagerAdapter = new TutorialAdapter(this);
        defaultViewpager.setAdapter(defaultPagerAdapter);
        defaultIndicator.setViewPager(defaultViewpager,this);//hack to show only 5 circles
        TextView skipTutorial = (TextView) findViewById(R.id.skip_tutorial);
        skipTutorial.setOnClickListener(this);

        
    }

	@Override
	public void onClick(View v) {
		SharedPreferences mSharedPreferences = this.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
		Editor mEditor = mSharedPreferences.edit();
		mEditor.putBoolean("TUTORIAL", true);
/*		mEditor.putBoolean("TUTORIAL", false);
		mEditor.putBoolean("WIZARD", false);*/
		mEditor.commit();
		startActivity(new Intent(this,Wizard.class));
		
		
		
	}

	
}
