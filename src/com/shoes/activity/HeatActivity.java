 
package com.shoes.activity; 

import com.example.shoes.R;
import com.shoes.service.BleService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class HeatActivity extends Activity {

	private ImageView btnBack, ivAnimationLeft, ivAnimationRight;
	private AnimationDrawable animationLeft, animationRight;
	private TextView temperatureLeft, temperatureRight;
	private TextView popupTime, popupTemperature;
	private SeekBar sbTime, sbTemperature;
	private MyBroadcastReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_heat);
		
		receiver=new MyBroadcastReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("update_heating_status");
		filter.addAction("update_temperature");
		registerReceiver(receiver, filter);
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		ivAnimationLeft=(ImageView)findViewById(R.id.animation_left);
		animationLeft=(AnimationDrawable)ivAnimationLeft.getDrawable();
		if(BleService.isHeating){
			animationLeft.start();
		}else{
			animationLeft.stop();
		}
				
		ivAnimationRight=(ImageView)findViewById(R.id.animation_right);
		animationRight=(AnimationDrawable)ivAnimationRight.getDrawable();
		if(BleService.isHeating){
			animationRight.start();
		}else{
			animationRight.stop();
		}
		
		temperatureLeft=(TextView)findViewById(R.id.temperature_left);
		temperatureLeft.setText(BleService.temperatureLeft+"℃");
		
		temperatureRight=(TextView)findViewById(R.id.temperature_right);
		temperatureRight.setText(BleService.temperatureRight+"℃");
		
		popupTime=(TextView)findViewById(R.id.popup_time);
		if(BleService.isHeating){
			popupTime.setText(BleService.timeLeft+"min");
		}
		
		popupTemperature=(TextView)findViewById(R.id.popup_temperature);
		if(BleService.isHeating){
			if(BleService.strength<33){
				popupTemperature.setText("低");
			}else if(BleService.strength>66){
				popupTemperature.setText("高");
			}else{
				popupTemperature.setText("中");
			}
		}
		
		sbTime=(SeekBar)findViewById(R.id.seekbar_time);
		sbTime.setMax(30);		
		if(BleService.isHeating){
			sbTime.setProgress(BleService.timeLeft);
			//onCreate中getWidth为0，需要借此监听器获取长度		
			ViewTreeObserver vto1=sbTime.getViewTreeObserver();
			vto1.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				
				@SuppressWarnings("deprecation")
				@Override
				public void onGlobalLayout() {
					// TODO Auto-generated method stub
					sbTime.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					float seekBarWidth=sbTime.getWidth()-dp2px(HeatActivity.this, 37);
					float translationLength=seekBarWidth*((float)sbTime.getProgress()/(float)sbTime.getMax());
					popupTime.setTranslationX(translationLength);
				}
			});		
		}
		sbTime.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				//发加热命令
				if(sbTime.getProgress()!=0){
					Intent intent=new Intent();
					intent.setAction("start_heating");
					intent.putExtra("heat_time", sbTime.getProgress());
					if(sbTemperature.getProgress()!=0){
						intent.putExtra("heat_strength", sbTemperature.getProgress());
					}else{
						intent.putExtra("heat_strength", 5);
					}					
					sendBroadcast(intent);	
				}else{
					
					Intent intent=new Intent();
					intent.setAction("stop_heating");
					sendBroadcast(intent);
				}

			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean arg2) {
				// TODO Auto-generated method stub
				float seekBarWidth=seekBar.getWidth()-dp2px(HeatActivity.this, 37);
				float translationLength=seekBarWidth*progress/seekBar.getMax();
				popupTime.setTranslationX(translationLength);
				popupTime.setText(progress+"min");
				if(progress==0){
					animationLeft.stop();
					animationLeft.setAlpha(0);
					animationRight.stop();
					animationRight.setAlpha(0);
				}else{
					animationLeft.start();
					animationLeft.setAlpha(255);
					animationRight.start();
					animationRight.setAlpha(255);					
				}
			}
		});
		
		sbTemperature=(SeekBar)findViewById(R.id.seekbar_temperature);
		sbTemperature.setMax(100);
		if(BleService.isHeating){
			sbTemperature.setProgress(BleService.strength);
			ViewTreeObserver vto2=sbTemperature.getViewTreeObserver();
			vto2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				
				@SuppressWarnings("deprecation")
				@Override
				public void onGlobalLayout() {
					// TODO Auto-generated method stub
					sbTemperature.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					float seekBarWidth=sbTemperature.getWidth()-dp2px(HeatActivity.this, 37);
					float translationLength=seekBarWidth*((float)sbTemperature.getProgress()/(float)sbTemperature.getMax());
					popupTemperature.setTranslationX(translationLength);
				}
			});	
		}
		sbTemperature.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				//发加热命令
				if(sbTemperature.getProgress()>4){
					Intent intent=new Intent();
					intent.setAction("start_heating");
					intent.putExtra("heat_time", sbTime.getProgress());
					intent.putExtra("heat_strength", sbTemperature.getProgress());
					sendBroadcast(intent);	
					BleService.strength=sbTemperature.getProgress();
				}else{
					Intent intent=new Intent();
					intent.setAction("start_heating");
					intent.putExtra("heat_time", sbTime.getProgress());
					intent.putExtra("heat_strength", 5);
					sendBroadcast(intent);	
					BleService.strength=0;
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean arg2) {
				// TODO Auto-generated method stub
				float seekBarWidth=seekBar.getWidth()-dp2px(HeatActivity.this, 37);
				float translationLength=seekBarWidth*progress/seekBar.getMax();
				popupTemperature.setTranslationX(translationLength);
				if(progress<33){
					popupTemperature.setText("低");						
				}else if(progress>66){
					popupTemperature.setText("高");
				}else{
					popupTemperature.setText("中");
				}				
			}
		});
		
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();
			if(action.equals("update_heating_status")){
				sbTime.setProgress(BleService.timeLeft);
				float seekBarWidth=sbTime.getWidth()-dp2px(HeatActivity.this, 37);
				float translationLength=seekBarWidth*((float)sbTime.getProgress()/(float)sbTime.getMax());
				popupTime.setTranslationX(translationLength);
				popupTime.setText(BleService.timeLeft+"min");
			}
			if(action.equals("update_temperature")){
				temperatureLeft.setText(BleService.temperatureLeft+"℃");
				temperatureRight.setText(BleService.temperatureRight+"℃");
			}
			
		}
		
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		unregisterReceiver(receiver);
	}
	
	//dp转px
	private int dp2px(Context context, float dp){
		final float scale=context.getResources().getDisplayMetrics().density;
		return (int)(dp*scale+0.5f);
	}

}
