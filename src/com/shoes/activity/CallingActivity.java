
package com.shoes.activity; 

import java.io.FileNotFoundException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.shoes.R;
import com.shoes.constants.VirtualCallContants;

public class CallingActivity extends Activity {
	
	private TextView tvName, tvPhone;
	private String name, phone;
	private ImageView refuse, answer;
	private Vibrator vibrator;
	private int notifyStyle;
	private MediaPlayer mediaPlayer;
	private int wallpaper;
	private RelativeLayout layout;	
	private SharedPreferences preferences;
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				|WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_calling);
		
		preferences=getSharedPreferences("virtual_call_setting", MODE_PRIVATE);Log.d("zz", "111");
		String type=getIntent().getStringExtra("type");Log.d("zz", "222");
		if(type!=null && type.equals("knock")){
			Log.d("zz", "333");
		}else{
			preferences.edit().putBoolean("isAlarmSet", false).commit();
			preferences.edit().putBoolean("isTimeOpen", false).commit();
			sendBroadcast(new Intent("virtual_call_setting_time_toggle_change"));
		}
		
		
		
		//设置来电模式：震动和铃声
		notifyStyle=preferences.getInt("notify_style", VirtualCallContants.NOTIFY_STYLE_VOICE);
		String uriString=preferences.getString("audioUri", null);
		if(uriString!=null){
			mediaPlayer=MediaPlayer.create(this, Uri.parse(uriString));
		}else{
			mediaPlayer=MediaPlayer.create(this, RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE));
		}	
		if(mediaPlayer!=null){
			mediaPlayer.setLooping(true);
		}		
		vibrator=(Vibrator)getSystemService(Service.VIBRATOR_SERVICE);		
		switch (notifyStyle) {
		case VirtualCallContants.NOTIFY_STYLE_VOICE:
			if(mediaPlayer!=null){
				mediaPlayer.start();
			}
			break;

		case VirtualCallContants.NOTIFY_STYLE_VIBRATION:
			vibrator.vibrate(new long[]{1000, 500, 1000, 500}, 0);
			break;
			
		case VirtualCallContants.NOTIFY_STYLE_VOICE_AND_VIBRATION:
			if(mediaPlayer!=null){
				mediaPlayer.start();
			}
			vibrator.vibrate(new long[]{1000, 500, 1000, 500}, 0);
			break;
		}
		
		//设置来电人信息
		int contacter=preferences.getInt("contacter", VirtualCallContants.CONTACTER_RANDOM);
		switch (contacter) {
		case VirtualCallContants.CONTACTER_RANDOM:
			name=(String)ContactersActivity.getRandomContacter(this).get("name");			
			phone=(String)ContactersActivity.getRandomContacter(this).get("phone");
			if(name==null) name="";
			if(phone==null) phone="";
			break;

		case VirtualCallContants.CONTACTER_APPOINT:
			name=preferences.getString("choose_contacter_name", "");
			phone=preferences.getString("choose_contacter_phone", "");
			break;
			
		case VirtualCallContants.CONTACTER_CUSTOM:
			name=preferences.getString("custom_contacter_name", "");
			phone=preferences.getString("custom_contacter_phone", "");
			break;
		}
		if(name.equals("") && phone.equals("")){
			name="未知来电";
		}
		tvName=(TextView)findViewById(R.id.calling_name);
		tvName.setText(name);
		tvPhone=(TextView)findViewById(R.id.calling_phone);		
		tvPhone.setText(phone);
		
		//设置来电壁纸		
		wallpaper=preferences.getInt("wallpaper", VirtualCallContants.WALLPAPER_ANDROID);
		layout=(RelativeLayout)findViewById(R.id.calling_layout);
		switch (wallpaper) {
		case VirtualCallContants.WALLPAPER_ANDROID:
			layout.setBackgroundResource(R.drawable.wallpaper_default1);
			break;

		case VirtualCallContants.WALLPAPER_IOS:
			layout.setBackgroundResource(R.drawable.wallpaper_default2);
			break;
			
		case VirtualCallContants.WALLPAPER_CUSTOM:			
			try {				
				layout.setBackground(new BitmapDrawable(BitmapFactory.decodeStream(openFileInput("tempImage"))));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				layout.setBackgroundResource(R.drawable.wallpaper_default1);
				e.printStackTrace();
			}
			
		}
	
		refuse=(ImageView)findViewById(R.id.calling_refuse);
		refuse.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		answer=(ImageView)findViewById(R.id.calling_answer);
		answer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(CallingActivity.this, AnswerActivity.class);				
				intent.putExtra("contacterName", name);
				intent.putExtra("contacterPhone", phone);
				startActivity(intent);
				finish();
			}
		});
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(vibrator!=null){
			vibrator.cancel();
		}		
		if(mediaPlayer!=null){
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		
	}
	
	
	
}
