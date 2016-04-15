 
package com.shoes.activity; 

import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import net.simonvt.numberpicker.*;
import net.simonvt.numberpicker.NumberPicker.OnValueChangeListener;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.shoes.R;


public class RemindOnTimeSettingActivity extends Activity {
	
	private LinearLayout btnSetTime, btnSetRemark;
	private ImageButton btnBack;
	private Button btnOk;
	private TextView dayInfo, remarkInfo;
	private PopupWindow popupSetTime, popupSetTimeCustom;
	private MyOnClickListener listener;
	private net.simonvt.numberpicker.NumberPicker npHour, npMinute;
	private boolean[] day;
	@SuppressWarnings("deprecation")
	private int hour=new Date().getHours();
	@SuppressWarnings("deprecation")
	private int minute=new Date().getMinutes();
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remind_on_time_setting);
		
		listener=new MyOnClickListener();
		
		day=new boolean[7];
		for(int i=0; i<day.length; i++){
			day[i]=true;
		}
		
		//若是已有闹钟，则初始化已有信息
		Intent intent=getIntent();
		boolean isSet=intent.getBooleanExtra("isSet", false);		
		if(isSet){
			day=intent.getBooleanArrayExtra("day");
			hour=intent.getIntExtra("hour", new Date().getHours());
			minute=intent.getIntExtra("minute", new Date().getMinutes());
		}
		
		dayInfo=(TextView)findViewById(R.id.day_info);
		if(isSet){
			int numOfSelected=0;
			for(int i=0; i<day.length; i++){
				if(day[i]){
					numOfSelected++;
				}				
			}			
			if(numOfSelected==7){
				dayInfo.setText("每天");
			}else if(numOfSelected==0){
				dayInfo.setText("每天");
			}else{
				dayInfo.setText((day[0]?"周一  ":"")+(day[1]?"周二  ":"")+(day[2]?"周三  ":"")
						+(day[3]?"周四  ":"")+(day[4]?"周五  ":"")+(day[5]?"周六  ":"")+(day[6]?"周日":""));
			}
		}else{
			dayInfo.setText("每天");
		}
		
		remarkInfo=(TextView)findViewById(R.id.remark_info);
		if(isSet){
			remarkInfo.setText(intent.getStringExtra("remark"));
		}else{
			remarkInfo.setText("无");
		}
		
		btnBack=(ImageButton)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnOk=(Button)findViewById(R.id.btn_ok);
		btnOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent=getIntent();
				intent.putExtra("day", day);
				intent.putExtra("hour", hour);
				intent.putExtra("minute", minute);
				intent.putExtra("remark", remarkInfo.getText().toString());
				setResult(1, intent);
				finish();
			}
		});
		
		btnSetTime=(LinearLayout)findViewById(R.id.btn_set_time);
		btnSetTime.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub				
				View view=getLayoutInflater().inflate(R.layout.popup_set_time, null);				
				TextView tvEveryday=(TextView)view.findViewById(R.id.popup_everyday);
				tvEveryday.setOnClickListener(listener);				
				TextView tvWeekday=(TextView)view.findViewById(R.id.popup_weekday);
				tvWeekday.setOnClickListener(listener);
				TextView tvCustom=(TextView)view.findViewById(R.id.popup_custom);
				tvCustom.setOnClickListener(listener);
				popupSetTime=new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
				popupSetTime.setOutsideTouchable(true);				
				popupSetTime.setBackgroundDrawable(new BitmapDrawable());
				popupSetTime.setAnimationStyle(R.style.popupwindow_display_anim);
				popupSetTime.showAtLocation(findViewById(R.id.remind_on_time_setting_layout), 
						Gravity.BOTTOM, 0, 0);
			}
		});
		
		btnSetRemark=(LinearLayout)findViewById(R.id.btn_set_remark);
		btnSetRemark.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LinearLayout dialog=(LinearLayout) getLayoutInflater().inflate(R.layout.dialog_remark, null);
				final EditText etRemark=(EditText)dialog.findViewById(R.id.dialog_remark_input);		
				if(!remarkInfo.getText().toString().equals("无")){
					etRemark.setText(remarkInfo.getText().toString());
				}
				new AlertDialog.Builder(RemindOnTimeSettingActivity.this, R.style.MyDialogTheme)
				.setView(dialog)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						if(!etRemark.getText().toString().equals("")){
							remarkInfo.setText(etRemark.getText().toString());
						}else{
							remarkInfo.setText("无");
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				})
				.create()
				.show();
			}
		});
		
		npHour=(net.simonvt.numberpicker.NumberPicker)findViewById(R.id.number_picker_hour);
		npHour.setMinValue(0);
		npHour.setMaxValue(23);	
		npHour.setValue(hour);
		npHour.setLable(" 时");
		npHour.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker arg0, int arg1, int current) {
				// TODO Auto-generated method stub
				hour=current;
			}
		});
		
		npMinute=(net.simonvt.numberpicker.NumberPicker)findViewById(R.id.number_picker_minute);
		npMinute.setMinValue(0);
		npMinute.setMaxValue(59);	
		npMinute.setValue(minute);
		npMinute.setLable(" 分");
		npMinute.setOnValueChangedListener(new OnValueChangeListener() {
			
			@Override
			public void onValueChange(NumberPicker arg0, int last, int current) {
				// TODO Auto-generated method stub
				minute=current;
			}
		});	
				
	}
	
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		npHour.invalidate();
	}



	private class MyOnClickListener implements OnClickListener{

		@SuppressLint("InlinedApi")
		@SuppressWarnings("deprecation")
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub	
			if(popupSetTime.isShowing()){
				popupSetTime.dismiss();
			}
			switch (v.getId()) {			
			case R.id.popup_everyday:
				for(int i=0; i<day.length; i++){
					day[i]=true;
				}
				dayInfo.setText("每天");
				break;
			case R.id.popup_weekday:
				for(int i=0; i<day.length-2; i++){
					day[i]=true;
				}
				dayInfo.setText("周一到周五");
				break;
			case R.id.popup_custom:
				View view=getLayoutInflater().inflate(R.layout.popup_set_time_custom, null);
				
				final CheckBox cbMonday=(CheckBox)view.findViewById(R.id.popup_Monday);
				final CheckBox cbTuesday=(CheckBox)view.findViewById(R.id.popup_Tuesday);
				final CheckBox cbWednesday=(CheckBox)view.findViewById(R.id.popup_Wednesday);
				final CheckBox cbThursday=(CheckBox)view.findViewById(R.id.popup_Thursday);;
				final CheckBox cbFriday=(CheckBox)view.findViewById(R.id.popup_Friday);
				final CheckBox cbSaturday=(CheckBox)view.findViewById(R.id.popup_Saturday);
				final CheckBox cbSunday=(CheckBox)view.findViewById(R.id.popup_Sunday);
				
				
				Button btnCancel=(Button)view.findViewById(R.id.btn_cancel);
				btnCancel.setOnClickListener(this);
				
				Button btnSure=(Button)view.findViewById(R.id.btn_sure);
				btnSure.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if(cbMonday.isChecked()||cbTuesday.isChecked()||cbWednesday.isChecked()||
								cbThursday.isChecked()||cbFriday.isChecked()||cbSaturday.isChecked()||cbSunday.isChecked()){
							day=new boolean[]{cbMonday.isChecked(), cbTuesday.isChecked(), cbWednesday.isChecked(),
									cbThursday.isChecked(), cbFriday.isChecked(), cbSaturday.isChecked(), cbSunday.isChecked()};
							int numOfSelected=0;
							for(int i=0; i<day.length; i++){							
								if(day[i]){
									numOfSelected++;
								}
								if(numOfSelected==7){
									dayInfo.setText("每天");
								}else{
									dayInfo.setText((cbMonday.isChecked()?"周一  ":"")+(cbTuesday.isChecked()?"周二  ":"")
											+(cbWednesday.isChecked()?"周三  ":"")+(cbThursday.isChecked()?"周四  ":"")
											+(cbFriday.isChecked()?"周五  ":"")+(cbSaturday.isChecked()?"周六  ":"")
											+(cbSunday.isChecked()?"周日":""));
								}
							}
						}																
						popupSetTimeCustom.dismiss();
					}
				});
				
				popupSetTimeCustom=new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
				popupSetTimeCustom.setOutsideTouchable(true);				
				popupSetTimeCustom.setBackgroundDrawable(new BitmapDrawable());
				popupSetTimeCustom.setAnimationStyle(R.style.popupwindow_display_anim);
				popupSetTimeCustom.showAtLocation(findViewById(R.id.remind_on_time_setting_layout), 
						Gravity.BOTTOM, 0, 0);
				break;
			case R.id.btn_cancel:
				popupSetTimeCustom.dismiss();
				break;		
			
			}
		}
		
	}
	
}
