package com.shoes.activity;

import com.example.shoes.R;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.VideoView;

public class TutorialActivity extends Activity {
	
	private PopupWindow popupController;
	private RelativeLayout playCenter;
	private VideoView vv;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);
		
		handler=new Handler();
		
		playCenter=(RelativeLayout)findViewById(R.id.play_center);
		playCenter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(!popupController.isShowing()){
					popupController.showAtLocation(findViewById(R.id.layout_all), 
							Gravity.BOTTOM, 0, 0);
				}				
				handler.removeCallbacksAndMessages(null);
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(popupController.isShowing()){
							popupController.dismiss();
						}
					}
				}, 4000);
				
			}
		});
		
//		playCenter.setOnTouchListener(new OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View arg0, MotionEvent arg1) {
//				// TODO Auto-generated method stub
//				return false;
//			}
//		});
		
		vv=(VideoView)findViewById(R.id.video);	
		Uri uri= Uri.parse("android.resource://" + getPackageName() + "/"+ R.raw.tutorial);		
		vv.setVideoURI(uri);
		
		vv.start();
		
		View popupView=getLayoutInflater().inflate(R.layout.popup_video_controller, null);
		
		SeekBar sbTime=(SeekBar)popupView.findViewById(R.id.seekbar);
		MediaMetadataRetriever mmr=new MediaMetadataRetriever();
		mmr.setDataSource(this, uri);
		String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		sbTime.setMax(Integer.valueOf(duration));
		sbTime.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
				// TODO Auto-generated method stub
				vv.seekTo(progress);
			}
		});
		
		popupController=new PopupWindow(popupView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
		popupController.setOutsideTouchable(false);			
		popupController.setAnimationStyle(R.style.popupwindow_display_anim);
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		handler.removeCallbacksAndMessages(null);
	}
	
	

}
