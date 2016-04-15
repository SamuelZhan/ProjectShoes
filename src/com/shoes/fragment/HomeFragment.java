
package com.shoes.fragment; 

import java.util.HashMap;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.shoes.R;
import com.shoes.activity.ConnectionActivity;
import com.shoes.activity.HeatActivity;
import com.shoes.activity.RemindActivity;
import com.shoes.activity.SearchPhoneActivity;
import com.shoes.activity.StepsActivity;
import com.shoes.activity.VirtualCallActivity;
import com.shoes.customview.MarqueeTextView;
import com.shoes.server.HttpServer;
import com.shoes.service.BleService;
import com.squareup.picasso.Picasso;

public class HomeFragment extends Fragment {
	
	private RelativeLayout virtualCall, count, heat, searchPhone, remind, led, btnConnectionLeft, btnConnectionRight;	
	private ImageView statusLeft, statusRight, notifyTemperature, notifyCharge, notifyAlarm, ivAdvertisement;
	private TextView tvEletricityLeft, tvEletricityRight, tvTemperatureLeft, tvTemperatureRight;
	private MarqueeTextView tvAdvertisement;
	private MyOnClickListener listener;
	private MyBroadcastReceiver receiver;
	private Handler handler;

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		getActivity().unregisterReceiver(receiver);
		handler.removeCallbacksAndMessages(null);
	}

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		receiver=new MyBroadcastReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("update_electricity");
		filter.addAction("update_temperature");
		filter.addAction("connection_status_changed");
		filter.addAction("update_alarm_status");
		filter.addAction("update_heating_status");
		filter.addAction("update_charging_status");
		getActivity().registerReceiver(receiver, filter);
		
		handler=new Handler();
		
		View rootView=inflater.inflate(R.layout.fragment_home, container, false);
		listener=new MyOnClickListener();
				
		tvEletricityLeft=(TextView)rootView.findViewById(R.id.home_electricity_left);
		tvEletricityLeft.setText(BleService.electricityLeft+"%");
		
		tvEletricityRight=(TextView)rootView.findViewById(R.id.home_electricity_right);
		tvEletricityRight.setText(BleService.electricityRight+"%");
		
		tvTemperatureLeft=(TextView)rootView.findViewById(R.id.home_temperature_left);
		tvTemperatureLeft.setText(BleService.temperatureLeft+"℃");
		
		tvTemperatureRight=(TextView)rootView.findViewById(R.id.home_temperature_right);
		tvTemperatureRight.setText(BleService.temperatureRight+"℃");
		
		ivAdvertisement=(ImageView)rootView.findViewById(R.id.home_advertisement);
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				loadAdvertisement();
			}
		});
		
		tvAdvertisement=(MarqueeTextView)rootView.findViewById(R.id.home_advertisement_text);
		
		statusLeft=(ImageView)rootView.findViewById(R.id.left_connection_status);
		if(BleService.isLeftConnected){
			statusLeft.setImageResource(R.drawable.left_connected);
		}else{
			statusLeft.setImageResource(R.drawable.left_disconnected);
		}
		
		statusRight=(ImageView)rootView.findViewById(R.id.right_connection_status);
		if(BleService.isRightConnected){
			statusRight.setImageResource(R.drawable.right_connected);
		}else{
			statusRight.setImageResource(R.drawable.right_disconnected);
		}
		
		notifyTemperature=(ImageView)rootView.findViewById(R.id.notify_temperature);
		if(BleService.isHeating){
			notifyTemperature.setAlpha(255);
		}else{
			notifyTemperature.setAlpha(30);
		}
		
		notifyCharge=(ImageView)rootView.findViewById(R.id.notify_charge);
		if(BleService.isCharging){
			notifyCharge.setAlpha(255);
		}else{
			notifyCharge.setAlpha(30);
		}
		
		notifyAlarm=(ImageView)rootView.findViewById(R.id.notify_alarm);
		if(BleService.isOverHeating){
			notifyAlarm.setAlpha(255);
		}else{
			notifyAlarm.setAlpha(30);
		}
		
		btnConnectionLeft=(RelativeLayout)rootView.findViewById(R.id.left_connect_status_layout);
		btnConnectionLeft.setOnClickListener(listener);
		
		btnConnectionRight=(RelativeLayout)rootView.findViewById(R.id.right_connect_status_layout);
		btnConnectionRight.setOnClickListener(listener);
		
		count=(RelativeLayout)rootView.findViewById(R.id.count);
		count.setOnClickListener(listener);
		
		heat=(RelativeLayout)rootView.findViewById(R.id.heat);
		heat.setOnClickListener(listener);
		
		virtualCall=(RelativeLayout)rootView.findViewById(R.id.virtual_call);
		virtualCall.setOnClickListener(listener);
		
		searchPhone=(RelativeLayout)rootView.findViewById(R.id.search_phone);
		searchPhone.setOnClickListener(listener);
		
		remind=(RelativeLayout)rootView.findViewById(R.id.remind);
		remind.setOnClickListener(listener);
		
		led=(RelativeLayout)rootView.findViewById(R.id.led);
		led.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				// TODO Auto-generated method stub
				BleService.ledMode=0;
				getActivity().sendBroadcast(new Intent().setAction("led_change_mode"));
				return true;
			}
		});
		led.setOnClickListener(listener);
		
		return rootView;
	}
	
	private void loadAdvertisement(){
		final HashMap<String, Object> advertisement=HttpServer.getAdvertisement();
		if(advertisement!=null){
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Picasso.with(getActivity()).load((String)advertisement.get("pictureUrl"))
					.error(R.drawable.advertisement).into(ivAdvertisement);
					
					tvAdvertisement.setText((String)advertisement.get("announcement")); 
					
				}
			});
		}else{
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					loadAdvertisement();
				}
			}, 30000);
		}
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver{

		@SuppressWarnings("deprecation")
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();
			if(action.equals("update_electricity")){
				tvEletricityLeft.setText(BleService.electricityLeft+"%");
				tvEletricityRight.setText(BleService.electricityRight+"%");
			}
			if(action.equals("update_temperature")){
				tvTemperatureLeft.setText(BleService.temperatureLeft+"℃");
				tvTemperatureRight.setText(BleService.temperatureRight+"℃");
			}
			if(action.equals("connection_status_changed")){
				if(BleService.isLeftConnected){
					statusLeft.setImageResource(R.drawable.left_connected);
				}else{
					statusLeft.setImageResource(R.drawable.left_disconnected);
				}
				if(BleService.isRightConnected){
					statusRight.setImageResource(R.drawable.right_connected);
				}else{
					statusRight.setImageResource(R.drawable.right_disconnected);
				}
				tvEletricityLeft.setText(BleService.electricityLeft+"%");
				tvEletricityRight.setText(BleService.electricityRight+"%");
				tvTemperatureLeft.setText(BleService.temperatureLeft+"℃");
				tvTemperatureRight.setText(BleService.temperatureRight+"℃");	
			}
			if(action.equals("update_alarm_status")){
				if(BleService.isOverHeating){
					notifyAlarm.setAlpha(255);
				}else{
					notifyAlarm.setAlpha(30);
				}
			}
			if(action.equals("update_heating_status")){
				if(BleService.isHeating){
					notifyTemperature.setAlpha(255);
				}else{
					notifyTemperature.setAlpha(30);
				}
			}
			if(action.equals("update_charging_status")){
				if(BleService.isCharging){
					notifyCharge.setAlpha(255);
				}else{
					notifyCharge.setAlpha(30);
				}
			}
		}
		
	}	
	
	private class MyOnClickListener implements View.OnClickListener{
		
		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			Intent intent;
			switch (view.getId()) {			
			case R.id.count:
				intent=new Intent(getActivity(), StepsActivity.class);
				startActivity(intent);
				break;
			case R.id.heat:
				intent=new Intent(getActivity(), HeatActivity.class);
				startActivity(intent);
				break;
			case R.id.virtual_call:
				intent=new Intent(getActivity(), VirtualCallActivity.class);
				startActivity(intent);
				break;
			case R.id.search_phone:
				intent=new Intent(getActivity(), SearchPhoneActivity.class);
				startActivity(intent);
				break;
			case R.id.remind:
				intent=new Intent(getActivity(), RemindActivity.class);
				startActivity(intent);
				break;
			case R.id.led:
				getActivity().sendBroadcast(new Intent().setAction("led_change_mode"));
				break;
				
			case R.id.left_connect_status_layout:
			case R.id.right_connect_status_layout:
				startActivity(new Intent(getActivity(), ConnectionActivity.class));
			}
		}
		
	}

}
