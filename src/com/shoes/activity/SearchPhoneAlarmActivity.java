
package com.shoes.activity; 

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.example.shoes.R;
import com.shoes.service.BleService;

public class SearchPhoneAlarmActivity extends Activity {

	private ImageView circle;
	private Animation anim;
	private Button btnClose;
	private Handler handler;
	private Timer timer;
	private AnimationTask animationTask;
	private VibrateTask vibrateTask;
	private MediaPlayer player;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				|WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_search_phone_alarm);
		
		circle=(ImageView)findViewById(R.id.search_circle_image);
		anim=AnimationUtils.loadAnimation(this, R.anim.animation_search_phone);
		
		handler=new MyHandler(this, circle, anim);
		
		timer=new Timer();
		animationTask=new AnimationTask();
		vibrateTask=new VibrateTask();
		timer.schedule(animationTask, 0, 2000);
		timer.schedule(vibrateTask, 0, 1000);
//		
//		try {
//			Uri uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
//			player=new MediaPlayer();
//			player.setDataSource(this, uri);
//			player.setAudioStreamType(AudioManager.STREAM_ALARM);
//			player.setLooping(true);
//			player.prepare();
//			player.start();
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
		
		btnClose=(Button)findViewById(R.id.search_btn_close_alarm);
		btnClose.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				BleService.isSearching=false;
				BleService.isAlarming=false;
				sendBroadcast(new Intent().setAction("stop_vibrating"));
				sendBroadcast(new Intent().setAction("stop_link_lost"));
				//通知SearchPhone更改按钮状态
				sendBroadcast(new Intent().setAction("update_btn_status"));
				finish();
			}
		});
		
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		if(animationTask!=null){
			animationTask.cancel();
			animationTask=null;
		}
		if(vibrateTask!=null){
			vibrateTask.cancel();
			vibrateTask=null;
		}
		if(timer!=null){
			timer.cancel();
			timer=null;
		}
		if(player!=null){
			player.stop();
			player.release();
		}
	}
	
	//弱引用，防止内存泄露
	private static class MyHandler extends Handler{
		
		private WeakReference<SearchPhoneAlarmActivity> weakActivity;
		private WeakReference<ImageView> weakIv;
		private WeakReference<Animation> weakAnim;
		
		public MyHandler(SearchPhoneAlarmActivity activity, ImageView iv, Animation anim){
			weakActivity=new WeakReference<SearchPhoneAlarmActivity>(activity);
			weakIv=new WeakReference<ImageView>(iv);
			weakAnim=new WeakReference<Animation>(anim);
		}
		
		@Override
		public void handleMessage(Message msg){
			SearchPhoneAlarmActivity activity=weakActivity.get();
			ImageView iv=weakIv.get();
			Animation anim=weakAnim.get();
			if(activity!=null){
				if(msg.what==1){
					iv.startAnimation(anim);
				}
			}
		}
	}

	private class VibrateTask extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(BleService.isAlarming){
				sendBroadcast(new Intent().setAction("start_vibrating"));
			}
		}
		
	}
	
	private class AnimationTask extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(BleService.isAlarming){
				handler.sendEmptyMessage(1);				
			}
		}		
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			//不退出界面，必须按按钮才退出
		}
		
		return true;
	}
	
	
	
}
