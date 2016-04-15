 
package com.shoes.activity; 

import java.util.HashMap;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoes.R;
import com.shoes.constants.VirtualCallContants;

public class VirtualCallActivity extends Activity {

	private ImageButton btnBack, btnToggleBoth, btnToggleKnock;
	private TextView setting0, setting1, setting2, setting3, setting4;
	private RelativeLayout layoutCallOnTime, layoutNotifyStyle, layoutContacter, layoutWallpaper, layoutVoice; 
	private boolean isBothOpen, isKnockOpen, isTimeOpen;
	private int time, tempTime;
	private String hour, minute, second;
	private boolean isAlarmSet;
	private int notifyStyle, tempNotifyStyle;
	private Uri audioUri;
	private int contacter, tempContacter;
	private String customContacterName, customContacterPhone;
	private String chooseContacterName, chooseContacterPhone;
	private String tempName, tempPhone;
	private int wallpaper;
	private int voice, tempVoice;	
	
	private AlarmManager alarmManager;	
	
	private MyBroadcastReceiver receiver;
		
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_virtual_call);				
		
		//获取系统闹钟服务
		alarmManager=(AlarmManager) getSystemService(Service.ALARM_SERVICE);
				
		preferences=getSharedPreferences("virtual_call_setting", Context.MODE_PRIVATE);
		editor=preferences.edit();
		
		receiver=new MyBroadcastReceiver();
		IntentFilter filter=new IntentFilter("virtual_call_setting_time_toggle_change");
		registerReceiver(receiver, filter);
		
		isBothOpen=preferences.getBoolean("isBothOpen", false);
		isKnockOpen=preferences.getBoolean("isKnockOpen", false);
		isTimeOpen=preferences.getBoolean("isTimeOpen", false);
		
		
		//开始时间
		time=preferences.getInt("call_on_time", VirtualCallContants.CALL_ON_TIME_30_SECONDS);
		tempTime=time;
		hour=preferences.getString("custom_time_hour", "0");
		minute=preferences.getString("custom_time_minute", "0");
		second=preferences.getString("custom_time_second", "0");
		isAlarmSet=preferences.getBoolean("isAlarmSet", false);
		
		//提醒方式
		notifyStyle=preferences.getInt("notify_style", VirtualCallContants.NOTIFY_STYLE_VOICE);
		tempNotifyStyle=notifyStyle; //防止没修改内容，却按确定，导致 目前==临时==0，下同；
		if(preferences.getString("audioUri", null)!=null){
			audioUri=Uri.parse(preferences.getString("audioUri", null));
		}else{
			audioUri=null;
		}
		
		//联系人
		contacter=preferences.getInt("contacter", VirtualCallContants.CONTACTER_RANDOM);
		tempContacter=contacter;
		customContacterName=preferences.getString("custom_contacter_name", "未知来电");
		customContacterPhone=preferences.getString("custom_contacter_phone", "");
		chooseContacterName=preferences.getString("choose_contacter_name", "未知来电");
		chooseContacterPhone=preferences.getString("choose_contacter_phone", "");
		
		//壁纸选择
		wallpaper=preferences.getInt("wallpaper", VirtualCallContants.WALLPAPER_ANDROID);
		
		//来电声音
		voice=preferences.getInt("voice", VirtualCallContants.VOICE_MAN);
		tempVoice=voice;
		
		btnBack=(ImageButton)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
//总开关		
//////////////////////////////////////////////////////////////////////////////////////////////////////		

		btnToggleBoth=(ImageButton)findViewById(R.id.btn_toggle_all);		
		btnToggleBoth.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(isBothOpen){
					
					btnToggleBoth.setImageResource(R.drawable.toggle_btn_close);
					if(isKnockOpen){
						btnToggleKnock.setImageResource(R.drawable.toggle_btn_close);
						sendBroadcast(new Intent().setAction("close_virtual_call_when_knocked"));
					}
					isBothOpen=false;
					editor.putBoolean("isBothOpen", false).commit();
					if(isAlarmSet){
						Intent intentForCalling=new Intent(VirtualCallActivity.this, CallingActivity.class);
						PendingIntent pendingIntent=PendingIntent.getActivity(VirtualCallActivity.this, 0, intentForCalling, 0);
						alarmManager.cancel(pendingIntent);
						isAlarmSet=false;
						editor.putBoolean("isAlarmSet", isAlarmSet).commit();
						setting0.setText("关闭");
						Toast.makeText(VirtualCallActivity.this, "已取消定时来电", Toast.LENGTH_SHORT).show();
					}
				}else{
					btnToggleBoth.setImageResource(R.drawable.toggle_btn_open);
					if(isKnockOpen){
						btnToggleKnock.setImageResource(R.drawable.toggle_btn_open);
						sendBroadcast(new Intent().setAction("open_virtual_call_when_knocked"));
					}
					isBothOpen=true;
					editor.putBoolean("isBothOpen", true).commit();
				}
			}
		});
		
//跺脚开关		
//////////////////////////////////////////////////////////////////////////////////////////////////////	
		btnToggleKnock=(ImageButton)findViewById(R.id.btn_toggle_knock);
		btnToggleKnock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(isBothOpen){
					if(isKnockOpen){					
						btnToggleKnock.setImageResource(R.drawable.toggle_btn_close);
						isKnockOpen=false;
						editor.putBoolean("isKnockOpen", false).commit();
						sendBroadcast(new Intent().setAction("close_virtual_call_when_knocked"));
					}else{
						btnToggleKnock.setImageResource(R.drawable.toggle_btn_open);
						isKnockOpen=true;
						editor.putBoolean("isKnockOpen", true).commit();
						sendBroadcast(new Intent().setAction("open_virtual_call_when_knocked"));
					}
				}else{
					Toast.makeText(VirtualCallActivity.this, "先打开总开关", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		if(isBothOpen){
			btnToggleBoth.setImageResource(R.drawable.toggle_btn_open);
			if(isKnockOpen){
				btnToggleKnock.setImageResource(R.drawable.toggle_btn_open);
				sendBroadcast(new Intent().setAction("open_virtual_call_when_knocked"));
			}else{
				btnToggleKnock.setImageResource(R.drawable.toggle_btn_close);
				sendBroadcast(new Intent().setAction("close_virtual_call_when_knocked"));
			}
		}else{
			btnToggleBoth.setImageResource(R.drawable.toggle_btn_close);
		}
		
		
//定时来电		
//////////////////////////////////////////////////////////////////////////////////////////////////////		
		
		layoutCallOnTime=(RelativeLayout)findViewById(R.id.layout_call_on_time);
		layoutCallOnTime.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub								
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_call_on_time, null);
				final LinearLayout timeInput=(LinearLayout)dialog.findViewById(R.id.dialog_time_input);
				RadioGroup radioGroup=(RadioGroup)dialog.findViewById(R.id.call_on_time_radiogroup);
				RadioButton btn1=(RadioButton)dialog.findViewById(R.id.call_on_time_item1);
				RadioButton btn2=(RadioButton)dialog.findViewById(R.id.call_on_time_item2);
				RadioButton btn3=(RadioButton)dialog.findViewById(R.id.call_on_time_item3);
				RadioButton btn4=(RadioButton)dialog.findViewById(R.id.call_on_time_item4);
				RadioButton btn5=(RadioButton)dialog.findViewById(R.id.call_on_time_item5);
				//设置数字超过60则设置为60
				final EditText timeInput1=(EditText)dialog.findViewById(R.id.time_input_1);				
				final EditText timeInput2=(EditText)dialog.findViewById(R.id.time_input_2);
				final EditText timeInput3=(EditText)dialog.findViewById(R.id.time_input_3);
				addEditTextWhatcher(timeInput1);
				addEditTextWhatcher(timeInput2);
				addEditTextWhatcher(timeInput3);
				
				switch (time) {
				case VirtualCallContants.CALL_ON_TIME_30_SECONDS:
					btn1.setChecked(true);
					break;

				case VirtualCallContants.CALL_ON_TIME_1_MINUTE:
					btn2.setChecked(true);
					break;
					
				case VirtualCallContants.CALL_ON_TIME_5_MINUTES:
					btn3.setChecked(true);
					break;
					
				case VirtualCallContants.CALL_ON_TIME_10_MINUTES:
					btn4.setChecked(true);
					break;
					
				case VirtualCallContants.CALL_ON_TIME_CUSTOM:
					btn5.setChecked(true);
					timeInput.setVisibility(View.VISIBLE);
					timeInput1.setText(hour);
					Selection.setSelection(timeInput1.getText(), timeInput1.getText().length());
					timeInput2.setText(minute);
					timeInput3.setText(second);
					break;
				}
				
				final ImageButton btnToggleTime =(ImageButton)dialog.findViewById(R.id.btn_toggle_time);
				if(isBothOpen){
					if(isTimeOpen){
						btnToggleTime.setImageResource(R.drawable.toggle_btn_open_small);
					}else{
						btnToggleTime.setImageResource(R.drawable.toggle_btn_close_small);
					}
				}
				btnToggleTime.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if(isBothOpen){
							if(isTimeOpen){
								btnToggleTime.setImageResource(R.drawable.toggle_btn_close_small);
								isTimeOpen=false;
								editor.putBoolean("isTimeOpen", false).commit();
								if(isAlarmSet){
									Intent intentForCalling=new Intent(VirtualCallActivity.this, CallingActivity.class);
									PendingIntent pendingIntent=PendingIntent.getActivity(VirtualCallActivity.this, 0, intentForCalling, 0);
									alarmManager.cancel(pendingIntent);
									isAlarmSet=false;
									editor.putBoolean("isAlarmSet", isAlarmSet).commit();
									setting0.setText("关闭");
									Toast.makeText(VirtualCallActivity.this, "已取消定时来电", Toast.LENGTH_SHORT).show();
								}
							}else{
								btnToggleTime.setImageResource(R.drawable.toggle_btn_open_small);
								isTimeOpen=true;
								editor.putBoolean("isTimeOpen", true).commit();
							}
						}else{
							Toast.makeText(VirtualCallActivity.this, "先打开总开关", Toast.LENGTH_SHORT).show();
						}
					}
				});
				
															
				radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						timeInput.setVisibility(View.GONE);
						timeInput1.setText(hour);
						timeInput2.setText(minute);
						timeInput3.setText(second);
						switch (checkedId) {						
													
						case R.id.call_on_time_item1:
							tempTime=VirtualCallContants.CALL_ON_TIME_30_SECONDS;
							break;
						case R.id.call_on_time_item2:
							tempTime=VirtualCallContants.CALL_ON_TIME_1_MINUTE;
							break;
						case R.id.call_on_time_item3:
							tempTime=VirtualCallContants.CALL_ON_TIME_5_MINUTES;
							break;
						case R.id.call_on_time_item4:
							tempTime=VirtualCallContants.CALL_ON_TIME_10_MINUTES;
							break;
						case R.id.call_on_time_item5:
							tempTime=VirtualCallContants.CALL_ON_TIME_CUSTOM;
							timeInput.setVisibility(View.VISIBLE);
							break;
						
						}
					}
				});
				
				//以对话框容器显示布局
				new AlertDialog.Builder(VirtualCallActivity.this)
						.setView(dialog)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								time=tempTime;
								hour=timeInput1.getText().toString();
								if(hour.equals("")){
									hour="0";
								}
								minute=timeInput2.getText().toString();
								if(minute.equals("")){
									minute="0";
								}
								second=timeInput3.getText().toString();
								if(second.equals("")){
									second="0";
								}
								editor.putInt("call_on_time", time);
								editor.putString("custom_time_hour", hour);								
								editor.putString("custom_time_minute", minute);
								editor.putString("custom_time_second", second);
								editor.commit();
								if(isBothOpen && isTimeOpen){
									if(time==VirtualCallContants.CALL_ON_TIME_CUSTOM){	
										if(hour.equals("0") && minute.equals("0")){
											setting0.setText(second+"秒来电");											
										}else if(hour.equals("0") ){
											setting0.setText(minute+"分钟"+second+"秒来电");
										}else{
											setting0.setText(hour+"小时"+minute+"分钟"+second+"后秒来电");
										}										
									}else{
										setting0.setText(VirtualCallContants.getCallOnTimeString(time));
									}
								}else{
									setting0.setText("关闭");
								}	
								
								//开始来电提醒
								setAlarm();
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
	
//提醒方式		
//////////////////////////////////////////////////////////////////////////////////////////////////////	
		
		layoutNotifyStyle=(RelativeLayout)findViewById(R.id.layout_notify_style);
		layoutNotifyStyle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub																
				
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_notify_style, null);
				RadioGroup radioGroup=(RadioGroup)dialog.findViewById(R.id.notify_style_radiogroup);
				RadioButton btn1=(RadioButton)dialog.findViewById(R.id.notify_style_item1);
				RadioButton btn2=(RadioButton)dialog.findViewById(R.id.notify_style_item2);
				RadioButton btn3=(RadioButton)dialog.findViewById(R.id.notify_style_item3);
				
				switch (notifyStyle) {
				case VirtualCallContants.NOTIFY_STYLE_VOICE:
					btn1.setChecked(true);
					break;
					
				case VirtualCallContants.NOTIFY_STYLE_VIBRATION:
					btn2.setChecked(true);
					break;
					
				case VirtualCallContants.NOTIFY_STYLE_VOICE_AND_VIBRATION:
					btn3.setChecked(true);
					break;
				}
				
				radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						switch (checkedId) {
						case R.id.notify_style_item1:
							tempNotifyStyle=VirtualCallContants.NOTIFY_STYLE_VOICE;
							break;

						case R.id.notify_style_item2:
							tempNotifyStyle=VirtualCallContants.NOTIFY_STYLE_VIBRATION;
							break;
							
						case R.id.notify_style_item3:
							tempNotifyStyle=VirtualCallContants.NOTIFY_STYLE_VOICE_AND_VIBRATION;
							break;
						}
							
							
					}
				});
				
				Button btnCustomVoice=(Button)dialog.findViewById(R.id.notify_style_custom_voice);
				btnCustomVoice.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(intent, 3);
					}
				});
				
				//以对话框容器显示布局
				new AlertDialog.Builder(VirtualCallActivity.this)
				.setView(dialog)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						notifyStyle=tempNotifyStyle;
						editor.putInt("notify_style", notifyStyle).commit();
						setting1.setText(VirtualCallContants.getNotifyString(notifyStyle));
						
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
		
		
//来电联系人		
//////////////////////////////////////////////////////////////////////////////////////////////////////
		
		layoutContacter=(RelativeLayout)findViewById(R.id.layout_contacter);
		layoutContacter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_contacter, null);
				RadioGroup radioGroup=(RadioGroup)dialog.findViewById(R.id.contacter_radiogroup);
				RadioButton btn1=(RadioButton)dialog.findViewById(R.id.contacter_item1);
				RadioButton btn2=(RadioButton)dialog.findViewById(R.id.contacter_item2);
				RadioButton btn3=(RadioButton)dialog.findViewById(R.id.contacter_item3);
				final LinearLayout ll=(LinearLayout)dialog.findViewById(R.id.dialog_contacter_input);
				final EditText name=(EditText)dialog.findViewById(R.id.custom_input_name);
				final EditText phone=(EditText)dialog.findViewById(R.id.custom_input_phone);
				switch (contacter) {
				case VirtualCallContants.CONTACTER_RANDOM:
					btn1.setChecked(true);
					break;
					
				case VirtualCallContants.CONTACTER_APPOINT:
					btn2.setChecked(true);
					
					break;
					
				case VirtualCallContants.CONTACTER_CUSTOM:
					btn3.setChecked(true);
					ll.setVisibility(View.VISIBLE);
					name.setText(customContacterName);
					phone.setText(customContacterPhone);
					break;
				}
								
				
				btn2.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent=new Intent(VirtualCallActivity.this, ContactersActivity.class);
						startActivityForResult(intent, 1);
					}
				});
				
				radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						name.setText(customContacterName);
						phone.setText(customContacterPhone);
						ll.setVisibility(View.GONE);
						switch (checkedId) {
						case R.id.contacter_item1:
							tempContacter=VirtualCallContants.CONTACTER_RANDOM;
							break;

						case R.id.contacter_item2:
							tempContacter=VirtualCallContants.CONTACTER_APPOINT;							
							break;
							
						case R.id.contacter_item3:
							tempContacter=VirtualCallContants.CONTACTER_CUSTOM;
							ll.setVisibility(View.VISIBLE);
							
							break;
						}
					}
				});
				//以对话框容器显示布局
				new AlertDialog.Builder(VirtualCallActivity.this)
				.setView(dialog)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						contacter=tempContacter;
						customContacterName=name.getText().toString();
						customContacterPhone=phone.getText().toString();
						chooseContacterName=tempName;
						chooseContacterPhone=tempPhone;
						editor.putInt("contacter", contacter);
						editor.putString("custom_contacter_name", name.getText().toString());
						editor.putString("custom_contacter_phone", phone.getText().toString());
						editor.putString("choose_contacter_name", chooseContacterName);
						editor.putString("choose_contacter_phone", chooseContacterPhone);
						editor.commit();
						
						if(contacter==VirtualCallContants.CONTACTER_APPOINT){
							
							//可以尝试产生默认联系人
							if(chooseContacterName==null || chooseContacterPhone==null){
								setting2.setText(VirtualCallContants.getContacterString(contacter)+"  未知来电");
							}else{
								setting2.setText(VirtualCallContants.getContacterString(contacter)+"  "
										+chooseContacterName+"  "+chooseContacterPhone);
							}
							
						}else if(contacter==VirtualCallContants.CONTACTER_RANDOM){
							setting2.setText(VirtualCallContants.getContacterString(contacter));
						}else{
							if(customContacterName.equals("") && customContacterPhone.equals("")){
								setting2.setText(VirtualCallContants.getContacterString(contacter)+"  未知来电");
							}else{
								setting2.setText(VirtualCallContants.getContacterString(contacter)+"  "+customContacterName
										+"  "+customContacterPhone);
							}
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
		
//选择壁纸		
//////////////////////////////////////////////////////////////////////////////////////////////////////	
		layoutWallpaper=(RelativeLayout)findViewById(R.id.layout_wallpaper);
		layoutWallpaper.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(VirtualCallActivity.this, WallpaperActivity.class);
				intent.putExtra("wallpaper", wallpaper);
				startActivityForResult(intent, 2);
			}
		});
		
//来电内容	
//////////////////////////////////////////////////////////////////////////////////////////////////////
		layoutVoice=(RelativeLayout)findViewById(R.id.layout_voice);
		layoutVoice.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_voice, null);								
				RadioGroup radioGroup=(RadioGroup)dialog.findViewById(R.id.voice_radiogroup);
				RadioButton btn1=(RadioButton)dialog.findViewById(R.id.voice_item1);
				RadioButton btn2=(RadioButton)dialog.findViewById(R.id.voice_item2);
				switch (voice) {
				case VirtualCallContants.VOICE_MAN:
					btn1.setChecked(true);
					break;

				case VirtualCallContants.VOICE_WOMAN:
					btn2.setChecked(true);
					break;
				}
				radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						switch (checkedId) {
						case R.id.voice_item1:
							tempVoice=VirtualCallContants.VOICE_MAN;
							break;

						case R.id.voice_item2:
							tempVoice=VirtualCallContants.VOICE_WOMAN;
							break;
						}
						
					}
				});
				//以对话框容器显示布局
				new AlertDialog.Builder(VirtualCallActivity.this)
				.setView(dialog)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						voice=tempVoice;
						editor.putInt("voice", voice).commit();
						setting4.setText(VirtualCallContants.getVoiceString(voice));
						
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
		
		
//当前设置内容	
//////////////////////////////////////////////////////////////////////////////////////////////////////
		setting0=(TextView)findViewById(R.id.virtual_call_current_setting_0);
		if(isBothOpen && isTimeOpen && isAlarmSet){
			if(time==VirtualCallContants.CALL_ON_TIME_30_SECONDS){
				setting0.setText("30秒后来电");
			}else if(time==VirtualCallContants.CALL_ON_TIME_1_MINUTE){
				setting0.setText("1分钟后来电");
			}else if(time==VirtualCallContants.CALL_ON_TIME_5_MINUTES){
				setting0.setText("5分钟后来电");
			}else if(time==VirtualCallContants.CALL_ON_TIME_10_MINUTES){
				setting0.setText("10分钟后来电");
			}else{
				if(hour.equals("0") && minute.equals("0")){
					setting0.setText(second+"秒来电");											
				}else if(hour.equals("0") ){
					setting0.setText(minute+"分钟"+second+"秒来电");
				}else{
					setting0.setText(hour+"小时"+minute+"分钟"+second+"秒来电");
				}				
			}			
		}else{
			setting0.setText("关闭");
		}
		
		setting1=(TextView)findViewById(R.id.virtual_call_current_setting_1);
		setting1.setText(VirtualCallContants.getNotifyString(notifyStyle));
		
		setting2=(TextView)findViewById(R.id.virtual_call_current_setting_2);
		if(contacter==VirtualCallContants.CONTACTER_APPOINT){
			if(chooseContacterName==null || chooseContacterPhone==null 
					|| chooseContacterName.equals("") || chooseContacterPhone.equals("")){
				setting2.setText(VirtualCallContants.getContacterString(contacter)+"  未知来电");
			}else{
				setting2.setText(VirtualCallContants.getContacterString(contacter)+"  "
						+chooseContacterName+"  "+chooseContacterPhone);
			}
		}else if(contacter==VirtualCallContants.CONTACTER_RANDOM){
			setting2.setText(VirtualCallContants.getContacterString(contacter));
		}else{
			setting2.setText(VirtualCallContants.getContacterString(contacter)+"  "+customContacterName
					+"  "+customContacterPhone);
		}
		
		
		setting3=(TextView)findViewById(R.id.virtual_call_current_setting_3);
		setting3.setText(VirtualCallContants.getWallpaperString(wallpaper));
		
		setting4=(TextView)findViewById(R.id.virtual_call_current_setting_4);
		setting4.setText(VirtualCallContants.getVoiceString(voice));
		
		
	}
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
		if(requestCode==1 && resultCode==1){
			HashMap<String, Object> contacter=(HashMap<String, Object>) data.getSerializableExtra("contacter");
			tempName=(String) contacter.get("name");
			tempPhone=(String) contacter.get("phone");
		}else if(requestCode==2 && resultCode==2){
			wallpaper=data.getIntExtra("wallpaper", VirtualCallContants.WALLPAPER_ANDROID);			
			setting3.setText(VirtualCallContants.getWallpaperString(wallpaper));			
			editor.putInt("wallpaper", wallpaper).commit();			
		}else if(requestCode==3 && resultCode==-1){
			audioUri=data.getData();
			editor.putString("audioUri", audioUri.toString()).commit();
		}
	}

	//监听数字输入是否超过60
	private void addEditTextWhatcher(final EditText editText){
		editText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub							
				if(s.toString().equals("0") || s.toString().equals("00")){
					editText.setText("");
				}else if(!s.toString().equals("")){
					int num=Integer.parseInt(s.toString());	
					if(num>60){
						editText.setText("60");
						Selection.setSelection(editText.getText(), editText.getText().length());
					}else if(num<0){
						editText.setText("");
					}					
				}
				if(s.toString().length()==3){
					editText.setText(s.toString().substring(0, 2));
					Selection.setSelection(editText.getText(), editText.getText().length());
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	//设置来电提醒
	private void setAlarm(){
		//要在双开关打开状态下方可设置定时来电
		if(isBothOpen){
			if(isTimeOpen){
				
				Intent intentForCalling=new Intent(VirtualCallActivity.this, CallingActivity.class);
				intentForCalling.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				
				//设置来电时间
				int timeInMillis = 0;
				switch (time) {
				case VirtualCallContants.CALL_ON_TIME_30_SECONDS:
					timeInMillis=30*1000;
					Toast.makeText(this, "将在30秒后来电", Toast.LENGTH_SHORT).show();
					break;
				case VirtualCallContants.CALL_ON_TIME_1_MINUTE:
					timeInMillis=60*1000;
					Toast.makeText(this, "将在60秒后来电", Toast.LENGTH_SHORT).show();
					break;
				case VirtualCallContants.CALL_ON_TIME_5_MINUTES:
					timeInMillis=5*60*1000;
					Toast.makeText(this, "将在5分钟后来电", Toast.LENGTH_SHORT).show();
					break;
				case VirtualCallContants.CALL_ON_TIME_10_MINUTES:
					timeInMillis=10*60*1000;
					Toast.makeText(this, "将在10分钟后来电", Toast.LENGTH_SHORT).show();
					break;
				case VirtualCallContants.CALL_ON_TIME_CUSTOM:
					timeInMillis=Integer.parseInt(hour)*60*60*1000
									+Integer.parseInt(minute)*60*1000
									+Integer.parseInt(second)*1000;
					if(Integer.parseInt(hour)>0){
						Toast.makeText(this, "将在"+hour+"小时"+minute+"分钟"+second+"秒后来电", Toast.LENGTH_SHORT).show();
					}else if(Integer.parseInt(minute)>0){
						Toast.makeText(this, "将在"+minute+"分钟"+second+"秒后来电", Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(this, "将在"+second+"秒后来电", Toast.LENGTH_SHORT).show();
					}
					break;
				}
				
				PendingIntent pendingIntent=PendingIntent.getActivity(this, 0, intentForCalling, PendingIntent.FLAG_CANCEL_CURRENT);
				alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+timeInMillis, pendingIntent);
				
				isAlarmSet=true;
				editor.putBoolean("isAlarmSet", isAlarmSet).commit();
			}
		}
		
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();
			if(action.equals("virtual_call_setting_time_toggle_change")){
				isTimeOpen=false;
				isAlarmSet=false;
			}
		}
		
	}

}
