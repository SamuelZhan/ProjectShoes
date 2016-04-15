
package com.shoes.activity; 

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoes.R;
import com.lee.pullrefresh.ui.PullToRefreshBase;
import com.lee.pullrefresh.ui.PullToRefreshBase.OnRefreshListener;
import com.lee.pullrefresh.ui.PullToRefreshListView;
import com.shoes.customview.RoundCornerImageView;
import com.shoes.server.HttpServer;
import com.squareup.picasso.Picasso;

public class RankingsActivity extends Activity {
	
	private ImageButton btnBack;
//	private TextView tvNoRankings;
	private ListView lvRankings;
	private ArrayList<HashMap<String, Object>> participants;
	private MyBaseAdapter adapter;
	private Handler handler;
	private PullToRefreshListView pullToRefreshListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rankings);
		
		handler=new Handler();
		
		
		btnBack=(ImageButton)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
//		tvNoRankings=(TextView)findViewById(R.id.label_no_rankings);

		participants=new ArrayList<HashMap<String,Object>>();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				ArrayList<HashMap<String, Object>> participantsTemp=HttpServer.getRankings();				
				if(participantsTemp!=null){
					if(participantsTemp.size()==0){
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(RankingsActivity.this, "暂无排行记录", Toast.LENGTH_SHORT).show();
							}
						});
					}else{
						participants.addAll(participantsTemp);
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								adapter.notifyDataSetChanged();
							}
						});
					}
					
					
				}else{
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(RankingsActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}).start();
		
		pullToRefreshListView=(PullToRefreshListView)findViewById(R.id.pull_to_refresh_listview);
		pullToRefreshListView.setPullLoadEnabled(false);
		pullToRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						ArrayList<HashMap<String, Object>> participantsTemp=HttpServer.getRankings();				
						if(participantsTemp!=null){
							participants.clear();
							participants.addAll(participantsTemp);
							handler.post(new Runnable() {
								
								@SuppressLint("SimpleDateFormat")
								@Override
								public void run() {
									// TODO Auto-generated method stub
									adapter.notifyDataSetChanged();
									pullToRefreshListView.onPullDownRefreshComplete();
									SimpleDateFormat df=new SimpleDateFormat("MM-dd HH:mm");
									String timeString=df.format(new Date(System.currentTimeMillis()));
							        pullToRefreshListView.setLastUpdatedLabel(timeString);
								}
							});
							
						}else{
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									Toast.makeText(RankingsActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
									pullToRefreshListView.onPullDownRefreshComplete();
								}
							});
						}
					}
				}).start();
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				
			}
		});

		lvRankings=pullToRefreshListView.getRefreshableView();
		lvRankings.setBackgroundColor(0x11ffffff);
		lvRankings.setDividerHeight(0);
		adapter=new MyBaseAdapter(this, participants);
		lvRankings.setAdapter(adapter);
				
	}

	private class MyBaseAdapter extends BaseAdapter{
		
		Context c;
		ArrayList<HashMap<String, Object>> datas;

		public MyBaseAdapter(Context context, ArrayList<HashMap<String, Object>> participants){
			c=context;
			datas=participants;
		}
		
		private class ViewHolder{
			boolean isFirstToThrid;
			TextView tvNumber;
			RoundCornerImageView ivHeadImage;
			TextView tvNickname;
			TextView tvSteps;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return datas.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return datas.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup group) {
			// TODO Auto-generated method stub
			ViewHolder holder=null;		
			HashMap<String, Object> participant=datas.get(position);	
			String no=(String)participant.get("No.");
			String headImageUrl=(String)participant.get("headImageUrl");
			String nickname=(String)participant.get("userName");
			String steps=(String)participant.get("dailySteps");
			if(position<3){
				if(convertView!=null && ((ViewHolder)convertView.getTag()).isFirstToThrid){
					holder=(ViewHolder) convertView.getTag();
					
				}else{
					convertView=LayoutInflater.from(c).inflate(R.layout.list_item_rankings_first_to_third, null);
					holder=new ViewHolder();
					holder.tvNumber=(TextView)convertView.findViewById(R.id.list_item_rankings_number);
					holder.ivHeadImage=(RoundCornerImageView)convertView.findViewById(R.id.list_item_rankings_head_image);
					holder.tvNickname=(TextView)convertView.findViewById(R.id.list_item_rankings_nickname);
					holder.tvSteps=(TextView)convertView.findViewById(R.id.list_item_rankings_steps);
					holder.isFirstToThrid=true;
					convertView.setTag(holder);
				}
				holder.tvNumber.setText(no);
				holder.ivHeadImage.setCrown(position);				
				Picasso.with(c).load(headImageUrl).into(holder.ivHeadImage);
				holder.tvNickname.setText(nickname);
				holder.tvSteps.setText(steps);

			}else{
				if(convertView!=null && !((ViewHolder)convertView.getTag()).isFirstToThrid){
					holder=(ViewHolder) convertView.getTag();
				}else{
					convertView=LayoutInflater.from(c).inflate(R.layout.list_item_rankings, null);
					holder=new ViewHolder();
					holder.tvNumber=(TextView)convertView.findViewById(R.id.list_item_rankings_number);
					holder.ivHeadImage=(RoundCornerImageView)convertView.findViewById(R.id.list_item_rankings_head_image);
					holder.tvNickname=(TextView)convertView.findViewById(R.id.list_item_rankings_nickname);
					holder.tvSteps=(TextView)convertView.findViewById(R.id.list_item_rankings_steps);
					holder.isFirstToThrid=false;
					convertView.setTag(holder);
				}
				holder.tvNumber.setText(no);
				Picasso.with(c).load(headImageUrl).into(holder.ivHeadImage);
				holder.tvNickname.setText(nickname);
				holder.tvSteps.setText(steps);
			}			
			return convertView;
		}
		
	}
	
}
