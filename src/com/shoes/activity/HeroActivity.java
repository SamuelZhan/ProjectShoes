
package com.shoes.activity; 

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import com.example.shoes.R;
import com.lee.pullrefresh.ui.PullToRefreshBase;
import com.lee.pullrefresh.ui.PullToRefreshBase.OnRefreshListener;
import com.lee.pullrefresh.ui.PullToRefreshListView;
import com.shoes.customview.LoadingDialog;
import com.shoes.server.HttpServer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HeroActivity extends Activity {
	
	private ImageButton btnBack;
	private EditText etAdvice;
	private Button btnSend;
	private PullToRefreshListView pulltoRefreshListView;
	private ListView lvChat;
	private ArrayList<HashMap<String, Object>> chat;
	private MyBaseAdapter adapter;
	private Handler handler;
	private LoadingDialog loadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_hero);
		
		handler=new Handler();
		
		loadingDialog=new LoadingDialog(this, "");
		
		btnBack=(ImageButton)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnSend=(Button)findViewById(R.id.hero_btn_send);
		btnSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(!etAdvice.getText().toString().trim().equals("")){
					loadingDialog.show();
					new Thread(new Runnable() {
						public void run() {
							boolean isSuccessed=HttpServer.sendFeedback(LoginActivity.userId, etAdvice.getText().toString());
							if(isSuccessed){
								handler.post(new Runnable() {
									public void run() {
										HashMap<String, Object> oneChat=new HashMap<String, Object>();
										oneChat.put("who", "1");
										oneChat.put("content", etAdvice.getText().toString());
										chat.add(oneChat);
										adapter.notifyDataSetChanged();
										etAdvice.setText("");
									}
								});
								
							}else{
								handler.post(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										Toast.makeText(HeroActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
									}
								});
							}
							if(loadingDialog.isShowing()){
								loadingDialog.dismiss();
							}
						}
					}).start();;					
					
				}else{
					Toast.makeText(HeroActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
				}				
				
			}
		});
		
		etAdvice=(EditText)findViewById(R.id.hero_input);
		etAdvice.clearFocus();
		
		pulltoRefreshListView=(PullToRefreshListView)findViewById(R.id.pull_to_refresh_listview);
		pulltoRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				new Thread(new Runnable() {
					public void run() {
						final ArrayList<HashMap<String, Object>> chatTemp=HttpServer.getFeedback(LoginActivity.userId);
						if(chatTemp!=null){							
							handler.post(new Runnable() {
								public void run() {
									chat.clear();
									chat.addAll(chatTemp);
									adapter.notifyDataSetChanged();
									pulltoRefreshListView.onPullDownRefreshComplete();
									SimpleDateFormat df=new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
									String timeString=df.format(new Date(System.currentTimeMillis()));
									pulltoRefreshListView.setLastUpdatedLabel(timeString);
								}
							});
						}else{
							handler.post(new Runnable() {
								public void run() {
									Toast.makeText(HeroActivity.this, "网络异常", Toast.LENGTH_SHORT).show();	
									pulltoRefreshListView.onPullDownRefreshComplete();
								}
							});
						}
						
					}
				}).start();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				
			}
		});
		
		lvChat=pulltoRefreshListView.getRefreshableView();
		lvChat.setBackgroundColor(0x00000000);
		lvChat.setCacheColorHint(0x00000000);
		lvChat.setDividerHeight(0);
		chat=new ArrayList<HashMap<String,Object>>();
		adapter=new MyBaseAdapter(this, chat);
		lvChat.setAdapter(adapter);
		
		pulltoRefreshListView.doPullRefreshing(true, 0);
		
	}
	
	private class MyBaseAdapter extends BaseAdapter{
		
		private Context c;
		private ArrayList<HashMap<String, Object>> data;
		
		public MyBaseAdapter(Context context, ArrayList<HashMap<String, Object>> chat) {
			// TODO Auto-generated constructor stub
			c=context;
			data=chat;
		}
		
		private class ViewHolder{
			TextView tvLeft, tvRight;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return data.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			ViewHolder holder=null;
			if(convertView==null){
				holder=new ViewHolder();
				convertView=getLayoutInflater().inflate(R.layout.list_item_hero_chat, null);
				holder.tvLeft=(TextView)convertView.findViewById(R.id.tv_chat_left);
				holder.tvRight=(TextView)convertView.findViewById(R.id.tv_chat_right);
				convertView.setTag(holder);
			}else{
				holder=(ViewHolder) convertView.getTag();
			}
			HashMap<String, Object> oneChat=data.get(position);
			String who=(String) oneChat.get("who");
			if(who.equals("1")){
				holder.tvLeft.setText((String)oneChat.get("content"));
				holder.tvLeft.setVisibility(View.VISIBLE);
				holder.tvRight.setVisibility(View.GONE);
			}else{
				holder.tvRight.setText((String)oneChat.get("content"));
				holder.tvLeft.setVisibility(View.GONE);
				holder.tvRight.setVisibility(View.VISIBLE);
			}
			return convertView;
		}
		
	}

}
