
package com.shoes.activity; 

import com.example.shoes.R;
import com.shoes.service.BleService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class RemindActivity extends Activity {
	
	private ImageButton btnBack, btnToggle;
	private RelativeLayout btnRemindOnTime;
	private boolean isOpen=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remind);
		
		btnBack=(ImageButton)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnToggle=(ImageButton)findViewById(R.id.btn_open_remind_when_called);
		if(BleService.isLeftConnected || BleService.isRightConnected){
			if(BleService.isRemindWhenCalled){
				btnToggle.setImageResource(R.drawable.toggle_btn_open);
			}else{
				btnToggle.setImageResource(R.drawable.toggle_btn_close);
			}		
		}else{
			btnToggle.setImageResource(R.drawable.toggle_btn_close);
		}
		btnToggle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(BleService.isLeftConnected || BleService.isRightConnected){
					if(isOpen){
						btnToggle.setImageResource(R.drawable.toggle_btn_close);
						isOpen=false;
						sendBroadcast(new Intent().setAction("close_remind_when_call"));
					}else{
						btnToggle.setImageResource(R.drawable.toggle_btn_open);
						isOpen=true;
						sendBroadcast(new Intent().setAction("open_remind_when_call"));
					}
				}else{
					Toast.makeText(RemindActivity.this, "没有连接设备,无法开启来电提醒", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		btnRemindOnTime=(RelativeLayout)findViewById(R.id.btn_remind_on_time);
		btnRemindOnTime.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(RemindActivity.this, RemindOnTimeActivity.class);
				startActivity(intent);
			}
		});
		
	}
	

}
