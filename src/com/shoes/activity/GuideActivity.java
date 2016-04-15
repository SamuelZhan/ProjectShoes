package com.shoes.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.example.shoes.R;


public class GuideActivity extends Activity{

	private ViewPager pager;
	private MyPagerAdapter pagerAdapter;
	private ArrayList<View> views;
	private ImageView dot1, dot2, dot3;
	private Button btnStartNow;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		
		SharedPreferences preferences=getSharedPreferences("isFirstLogin", MODE_PRIVATE);
		if(!preferences.getBoolean("isFirstLogin", true)){
			startActivity(new Intent(this, LoginActivity.class));
			finish();
		}		
		LayoutInflater inflater=LayoutInflater.from(this);
		views=new ArrayList<View>();
		views.add(inflater.inflate(R.layout.guide_activity_one, null));
		views.add(inflater.inflate(R.layout.guide_activity_two, null));
		views.add(inflater.inflate(R.layout.guide_activity_three, null));
		
		pager=(ViewPager)findViewById(R.id.guide_pager);
		pagerAdapter=new MyPagerAdapter(views);
		pager.setAdapter(pagerAdapter);
		pager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				switch(position){
				case 0:
					dot1.setImageResource(R.drawable.shape_circle_fill);
					dot2.setImageResource(R.drawable.shape_circle_stroke);
					dot3.setImageResource(R.drawable.shape_circle_stroke);
					break;
				case 1:
					dot1.setImageResource(R.drawable.shape_circle_stroke);
					dot2.setImageResource(R.drawable.shape_circle_fill);
					dot3.setImageResource(R.drawable.shape_circle_stroke);
					break;
				case 2:
					dot1.setImageResource(R.drawable.shape_circle_stroke);
					dot2.setImageResource(R.drawable.shape_circle_stroke);
					dot3.setImageResource(R.drawable.shape_circle_fill);
					btnStartNow.setVisibility(View.VISIBLE);
					break;
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		dot1=(ImageView)findViewById(R.id.dot1);
		dot2=(ImageView)findViewById(R.id.dot2);
		dot3=(ImageView)findViewById(R.id.dot3);	
		dot1.setImageResource(R.drawable.shape_circle_fill);
		btnStartNow=(Button)findViewById(R.id.start_now);
		btnStartNow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				SharedPreferences.Editor editor=getSharedPreferences("isFirstLogin", MODE_PRIVATE).edit();
				editor.putBoolean("isFirstLogin", false).commit();
				Intent intent = new Intent(GuideActivity.this, LoginActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
	}
	
	private class MyPagerAdapter extends PagerAdapter{

		List<View> views;
		
		public MyPagerAdapter(List<View> views){
			this.views=views;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return views.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0==arg1;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			// TODO Auto-generated method stub
			((ViewPager)container).removeView(views.get(position));
		}

		@Override
		public Object instantiateItem(View container, int position) {
			// TODO Auto-generated method stub
			((ViewPager)container).addView(views.get(position),0);
			return views.get(position);
		}						
	}
	
}
