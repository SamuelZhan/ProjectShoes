
package com.shoes.activity; 

import java.io.FileNotFoundException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.shoes.R;
import com.shoes.constants.VirtualCallContants;

public class AnswerActivity extends Activity {

	private RelativeLayout layout;
	private ImageView refuse;
	private TextView tvName, tvPhone;
	private Chronometer timer;
	private MediaPlayer player;
	private SharedPreferences preferences;
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_answer);
		
		Intent intent=getIntent();
		preferences=getSharedPreferences("virtual_call_setting", MODE_PRIVATE);
		
		layout=(RelativeLayout)findViewById(R.id.answer_layout);
		int wallpaper=preferences.getInt("wallpaper", VirtualCallContants.WALLPAPER_ANDROID);
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
		
		AudioManager audioManager=(AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setMode(AudioManager.MODE_IN_CALL);
		
		//设置来电声音
		int voice=preferences.getInt("voice", VirtualCallContants.VOICE_MAN);
		switch (voice) {
		case VirtualCallContants.VOICE_MAN:
			player=MediaPlayer.create(this, R.raw.demo);
			player.setLooping(true);
			player.start();
			break;

		case VirtualCallContants.VOICE_WOMAN:
			player=MediaPlayer.create(this, R.raw.demo);
			player.setLooping(true);
			player.start();
			break;
		}
		
		tvName=(TextView)findViewById(R.id.answer_name);
		tvName.setText(intent.getStringExtra("contacterName"));
		
		tvPhone=(TextView)findViewById(R.id.answer_phone);
		tvPhone.setText(intent.getStringExtra("contacterPhone"));
		
		timer=(Chronometer)findViewById(R.id.answer_time);
		timer.start();
		
		refuse=(ImageView)findViewById(R.id.answer_refuse);
		refuse.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(player!=null){
					player.stop();
					player.release();
				}
				AudioManager audioManager=(AudioManager) getSystemService(Context.AUDIO_SERVICE);
				audioManager.setMode(AudioManager.MODE_NORMAL);
				finish();
			}
		});
		
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(keyCode==KeyEvent.KEYCODE_BACK){
			if(player!=null){
				player.stop();
				player.release();
			}
			AudioManager audioManager=(AudioManager) getSystemService(Context.AUDIO_SERVICE);
			audioManager.setMode(AudioManager.MODE_NORMAL);
			finish();
		}
		return true;
	}

	
	
	

}
