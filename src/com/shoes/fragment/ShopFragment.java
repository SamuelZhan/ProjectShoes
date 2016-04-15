 
package com.shoes.fragment; 

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

import com.example.shoes.R;
import com.lee.pullrefresh.ui.PullToRefreshBase;
import com.lee.pullrefresh.ui.PullToRefreshBase.OnRefreshListener;
import com.lee.pullrefresh.ui.PullToRefreshScrollView;
import com.shoes.server.HttpServer;
import com.squareup.picasso.Picasso;

public class ShopFragment extends Fragment {
	
	private ArrayList<HashMap<String, Object>> recommendCommodities, displayCommodities;
	private List<View> views;
	private List<View> points;
	private PullToRefreshScrollView pullToRefreshScrollView;
	private ViewPager pager;
	private GridLayout displayCommoditiesLayout;
	private MyPagerAdapter pagerAdapter;
	private Handler handler;
	private MyOnClickListener listener;
	private ExecutorService executorService;
	private ChangeCommodityTask changeCommodityTask;
	private int position=0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		View rootView=inflater.inflate(R.layout.fragment_shop, null);
		
		pullToRefreshScrollView=(PullToRefreshScrollView)rootView.findViewById(R.id.pull_to_refresh_scrollview);		
		pullToRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ScrollView> refreshView) {
				// TODO Auto-generated method stub
				
				if(recommendCommodities.size()!=0){
					recommendCommodities.clear();
				}
				executorService.submit(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						ArrayList<HashMap<String,Object>> recommendCommoditiesTemp=HttpServer.getRecommendedCommodities();
						if(recommendCommoditiesTemp!=null){
							recommendCommodities.addAll(recommendCommoditiesTemp);
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub	
									for(int i=0; i<recommendCommodities.size(); i++){
										Picasso.with(getActivity()).load((String)recommendCommodities.get(i).get("pictureUrl")).into((ImageView)views.get(i));
										views.get(i).setTag((String)recommendCommodities.get(i).get("link"));
										views.get(i).setOnClickListener(listener);
									}
									pagerAdapter.notifyDataSetChanged();	
									if(changeCommodityTask!=null){
										changeCommodityTask.cancel();
										changeCommodityTask=null;
									}
									changeCommodityTask=new ChangeCommodityTask();
									new Timer().schedule(changeCommodityTask, 3000, 3000);
								}
							});
						}else{
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
								}
							});
						}
					}
				});
				
				
				if(displayCommodities.size()!=0){
					displayCommodities.clear();	
				}
				executorService.submit(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						ArrayList<HashMap<String,Object>> displayCommoditiesTemp=HttpServer.getDisplayCommodities();
						if(displayCommoditiesTemp!=null){
							displayCommodities.addAll(displayCommoditiesTemp);
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub	
									displayCommoditiesLayout.removeAllViews();
									for(int i=0; i<displayCommodities.size(); i++){
										LinearLayout commodity=(LinearLayout)getActivity().getLayoutInflater().inflate(R.layout.list_item_commodity, null);
										commodity.setTag((String)displayCommodities.get(i).get("link"));
										commodity.setOnClickListener(listener);
										ImageView picture=(ImageView) commodity.findViewById(R.id.commodity_picture);
										picture.setBackgroundColor(0x33ffffff);
										Picasso.with(getActivity()).load((String)displayCommodities.get(i).get("pictureUrl")).into(picture);
										
										TextView tvName=(TextView)commodity.findViewById(R.id.commodity_name);
										tvName.setText((String)displayCommodities.get(i).get("name"));
										
										TextView tvPrice=(TextView)commodity.findViewById(R.id.commodity_price);
										tvPrice.setText("¥ "+(String)displayCommodities.get(i).get("price"));
										
										LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(getActivity()
												.getWindowManager().getDefaultDisplay().getWidth()/2
												, LinearLayout.LayoutParams.WRAP_CONTENT);
										params.setMargins(10, 0, 10, 20);
										commodity.setLayoutParams(params);
										
										displayCommoditiesLayout.addView(commodity);
									}
								}
							});
						}else{
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
								}
							});
						}
					}
				});
				
				
				handler.postDelayed(new Runnable() {
					
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						pullToRefreshScrollView.onPullDownRefreshComplete();
						SimpleDateFormat df=new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
						String timeString=df.format(new Date(System.currentTimeMillis()));
				        pullToRefreshScrollView.setLastUpdatedLabel(timeString);
					}
				}, 3000);
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ScrollView> refreshView) {
				// TODO Auto-generated method stub
				
			}
		});
		
		ScrollView scrollView=pullToRefreshScrollView.getRefreshableView();
		View commoditiesLayout=getActivity().getLayoutInflater().inflate(R.layout.commodities_layout, null);
		scrollView.addView(commoditiesLayout);
		
		handler=new Handler();
		
		executorService=Executors.newFixedThreadPool(2);
				
		listener=new MyOnClickListener();
		
		recommendCommodities=new ArrayList<HashMap<String,Object>>();
		
		displayCommodities=new ArrayList<HashMap<String,Object>>();
		
		views=new ArrayList<View>();		
		for(int i=0; i<3; i++){
			ImageView view=new ImageView(getActivity());
			view.setBackgroundColor(0x33ffffff);
			view.setScaleType(ScaleType.FIT_XY);					
			views.add(view);
		}
		
		points=new ArrayList<View>();
		points.add(rootView.findViewById(R.id.point_1));
		points.add(rootView.findViewById(R.id.point_2));
		points.add(rootView.findViewById(R.id.point_3));
	
		pager=(ViewPager)commoditiesLayout.findViewById(R.id.advertisement_pager);
		pager.addOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				for(int i=0; i<points.size(); i++){
					if(position==i){
						((ImageView)points.get(i)).setImageResource(R.drawable.shape_circle_fill);
					}else{
						((ImageView)points.get(i)).setImageResource(R.drawable.shape_circle_stroke);
					}
				}
				ShopFragment.this.position=position;
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
		
		displayCommoditiesLayout=(GridLayout)commoditiesLayout.findViewById(R.id.commodities_layout);		
		
		pagerAdapter=new MyPagerAdapter();
		pager.setAdapter(pagerAdapter);
		
		pullToRefreshScrollView.doPullRefreshing(true, 300);
		
		
		return rootView;
	}
	
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d("zz", "shopfragment销毁");
	}

	private class MyPagerAdapter extends PagerAdapter{
		
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

		@Override
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
			return POSITION_NONE;
		}
		
		
	}
	
	private class MyOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {			
			// TODO Auto-generated method stub		
			WebView webView=new WebView(getActivity());
			webView.loadUrl(v.getTag().toString());
		}
		
	}
	
	//定时切换商品线程
	private class ChangeCommodityTask extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					pager.setCurrentItem(position, true);
					position++;
					if(position>2){
						position=0;
					}
				}
			});
		}
		
	}

}
