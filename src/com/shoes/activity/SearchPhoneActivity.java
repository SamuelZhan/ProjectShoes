
package com.shoes.activity; 

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import com.example.shoes.R;
import com.shoes.service.BleService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

public class SearchPhoneActivity extends Activity {

	private Button btnOpen;
	private ImageButton btnBack;
	private ImageView circle;
	private Animation anim;
	private Timer timer;
	private AnimationTask animationTask;
	private MyHandler handler;
	private MyBroadcastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_phone);
		
		receiver=new MyBroadcastReceiver();
		IntentFilter filter=new IntentFilter("update_btn_status");
		registerReceiver(receiver, filter);
		
		handler=new MyHandler(this);
		
		btnBack=(ImageButton)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		circle=(ImageView)findViewById(R.id.search_circle_image);
		anim=AnimationUtils.loadAnimation(this, R.anim.animation_search_phone);
		
		timer=new Timer();
		animationTask=new AnimationTask();
		timer.schedule(animationTask, 0, 2000);
		
		btnOpen=(Button)findViewById(R.id.search_btn_open);
		if(BleService.isSearching){
			btnOpen.setText("关闭");			
		}else{
			btnOpen.setText("开启");
			sendBroadcast(new Intent("stop_link_lost"));
		}
		btnOpen.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(BleService.isSearching){
					btnOpen.setText("开启");
					BleService.isSearching=false;
					if(animationTask!=null){
						animationTask.cancel();
						animationTask=null;
					}
					sendBroadcast(new Intent().setAction("stop_getting_rssi"));
					sendBroadcast(new Intent().setAction("stop_link_lost"));
				}else{
					btnOpen.setText("关闭");
					BleService.isSearching=true;
					if(animationTask!=null){
						animationTask.cancel();
						animationTask=null;
					}
					animationTask=new AnimationTask();
					timer.schedule(animationTask, 0, 2000);
					sendBroadcast(new Intent().setAction("start_getting_rssi"));
					sendBroadcast(new Intent().setAction("start_link_lost"));
				}				
			}
		});
	} 
	
	private void handleMessageOutside(Message msg){
		if(msg.what==1){
			circle.startAnimation(anim);			
		}
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		unregisterReceiver(receiver);
		
		if(animationTask!=null){
			animationTask.cancel();
			animationTask=null;
		}
		if(timer!=null){
			timer.cancel();
			timer=null;
		}
	}
	
	private static class MyHandler extends Handler{
		
		private WeakReference<SearchPhoneActivity> weakReference;
		
		public MyHandler(SearchPhoneActivity activity){
			weakReference=new WeakReference<SearchPhoneActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			SearchPhoneActivity activity=weakReference.get();
			if(activity!=null){
				activity.handleMessageOutside(msg);
			}
		}
		
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();
			if(action.equals("update_btn_status")){
				if(BleService.isSearching){
					btnOpen.setText("关闭");
				}else{
					btnOpen.setText("开启");
				}
			}
		}
		
	}
	
	private class AnimationTask extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(BleService.isSearching){
				handler.sendEmptyMessage(1);				
			}
		}		
		
	}

}
