//
//package com.shoes.activity; 
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.text.DecimalFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import org.achartengine.ChartFactory;
//import org.achartengine.GraphicalView;
//import org.achartengine.chart.PointStyle;
//import org.achartengine.model.XYMultipleSeriesDataset;
//import org.achartengine.model.XYSeries;
//import org.achartengine.renderer.XYMultipleSeriesRenderer;
//import org.achartengine.renderer.XYSeriesRenderer;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.BroadcastReceiver;
//import android.content.ContentValues;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.graphics.Bitmap;
//import android.graphics.Color;
//import android.graphics.Bitmap.CompressFormat;
//import android.graphics.Bitmap.Config;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.os.Handler;
//import android.text.format.DateFormat;
//import android.util.Log;
//import android.view.Display;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.Window;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.CompoundButton;
//import android.widget.CompoundButton.OnCheckedChangeListener;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.NumberPicker;
//import android.widget.Toast;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import com.example.shoes.R;
//import com.shoes.customview.RoundProgressBar;
//import com.shoes.database.DatabaseHelper;
//import com.shoes.server.HttpServer;
//import com.shoes.service.BleService;
//
//public class CountActivity extends Activity {
//
//	private LinearLayout btnTargetSteps, btnTargetCost, btnTargetDistance;
//	private RelativeLayout chartLayout;
//	private RelativeLayout todayStepsLayout, intervalStepsLayout;
//	private ImageView btnShared1, btnShared2;
//	private RoundProgressBar btnToggleToday, btnToggleInterval;
//	private Button btnStartInterval;
//	private ImageButton btnBack, btnMessage, btnRefresh;
//	private TextView tvTargetSteps, tvTargetStepsCenter, tvTargetCost, tvIntervalSteps, tvIntervalTime,
//						tvTargetDistance, tvTodaySteps, tvComplete, tvStopShow, tvCost, tvTime, tvDistance;
//	private CheckBox btnStartToday;	
//
//	private ExecutorService executorService;
//	private Handler handler;
//	
//	private MyOnClickListener listener;
//	
//	private MyBroadcastReceiver receiver;
//	
//	private SQLiteDatabase db;
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		// TODO Auto-generated method stub
//		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setContentView(R.layout.activity_count);
//		
//		DatabaseHelper helper=null;
//		
//		if(LoginActivity.isLogin){
//			helper=new DatabaseHelper(this, LoginActivity.userId);
//		}else{
//			helper=new DatabaseHelper(this, "default");
//		}
//		
//		db=helper.getWritableDatabase();
//		
//		//根据今天的日期查询数据库是否有今天的数据，没有的话就插入今天数据，并删除第30条数据，保持30天数据量
//		long nowTime=System.currentTimeMillis();
//		int numOfLostDay=0;//缺失数据的天数
//		while(true){
//			Date date=new Date(nowTime-numOfLostDay*24*60*60*1000L);
//			String dateString=(String) DateFormat.format("MM-dd", date);
//			if(db.rawQuery("SELECT * FROM todaySteps WHERE date=?", new String[]{dateString}).moveToNext()){
//				break;
//			}else{
//				numOfLostDay++;		
//				if(numOfLostDay==30) break;
//			}
//		}
//		for(int i=numOfLostDay-1; i>=0; i--){
//			Date date=new Date(nowTime-i*24*60*60*1000L);
//			String dateString=(String) DateFormat.format("MM-dd", date);
//			db.execSQL("INSERT INTO todaySteps VALUES(null, ?, ?)", new Object[]{0, dateString});
//			Date lastDate=new Date(nowTime-(30-i)*24*60*60*1000);
//			String lastDateString=(String)DateFormat.format("MM-dd", lastDate);
//			db.execSQL("DELETE FROM todaySteps WHERE date=?", new String[]{lastDateString});
//		}
//		
//		//广播注册
//		receiver=new MyBroadcastReceiver();
//		IntentFilter filter=new IntentFilter();
//		filter.addAction("update_todaySteps");
//		filter.addAction("update_sport_time");
//		filter.addAction("update_intervalSteps");
//		filter.addAction("update_intervalTime");
//		filter.addAction("update_history_steps");
//		registerReceiver(receiver, filter);
//		
//		//添加折线图
//		chartLayout=(RelativeLayout)findViewById(R.id.chart_layout);	
//		chartLayout.addView(getGraphicalView(), 0);
//
//		listener=new MyOnClickListener();
//		
//		executorService=Executors.newFixedThreadPool(1);
//		
//		handler=new Handler();
//		
//		btnBack=(ImageButton)findViewById(R.id.btn_back);
//		btnBack.setOnClickListener(listener);
//		
//		todayStepsLayout=(RelativeLayout)findViewById(R.id.count_long_layout);
//		btnToggleToday=(RoundProgressBar)findViewById(R.id.count_round_scale);
//		btnToggleToday.setOnClickListener(listener);
//		
//		intervalStepsLayout=(RelativeLayout)findViewById(R.id.count_short_layout);
//		btnToggleInterval=(RoundProgressBar)findViewById(R.id.count_round_scale_alpha);
//		btnToggleInterval.setOnClickListener(listener);
//		
//		btnStartToday=(CheckBox)findViewById(R.id.count_btn_start);
//		btnStartToday.setChecked(BleService.isCounting);
//		btnStartToday.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			
//			@Override
//			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
//				// TODO Auto-generated method stub
//				if(isChecked){
//					Log.d("zz", "开始计步");
//					Intent intent=new Intent();
//					intent.setAction("start_count");
//					sendBroadcast(intent);
//					tvStopShow.setVisibility(View.GONE);
//					tvTodaySteps.setAlpha(1f);
//				}else{
//					Log.d("zz", "暂停计步");
//					Intent intent=new Intent();
//					intent.setAction("stop_count");
//					sendBroadcast(intent);
//					tvStopShow.setVisibility(View.VISIBLE);
//					tvTodaySteps.setAlpha(0.1f);
//				}
//			}
//		});
//		
//		btnStartInterval=(Button)findViewById(R.id.count_btn_start_short);
//		if(BleService.isIntervalCounting){
//			btnStartInterval.setText("停止");
//		}else{
//			btnStartInterval.setText("开始");
//		}		
//		btnStartInterval.setOnClickListener(listener);
//		
//		btnRefresh=(ImageButton)findViewById(R.id.count_refresh);
//		btnRefresh.setOnClickListener(listener);
//		
//		btnMessage=(ImageButton)findViewById(R.id.count_info);
//		btnMessage.setOnClickListener(listener);
//		
//		btnShared1=(ImageView)findViewById(R.id.count_shared);
//		btnShared1.setOnClickListener(listener);
//		
//		btnShared2=(ImageView)findViewById(R.id.count_shared_short);
//		btnShared2.setOnClickListener(listener);
//		
//		btnTargetSteps=(LinearLayout)findViewById(R.id.btn_target_steps);
//		btnTargetSteps.setOnClickListener(listener);
//		
//		btnTargetCost=(LinearLayout)findViewById(R.id.btn_target_cost);
//		btnTargetCost.setOnClickListener(listener);
//		
//		btnTargetDistance=(LinearLayout)findViewById(R.id.btn_target_distance);
//		btnTargetDistance.setOnClickListener(listener);
//		
//		tvTargetSteps=(TextView)findViewById(R.id.count_target_steps);
//		tvTargetSteps.setText(LoginActivity.stepNumber);
//		
//		tvTargetStepsCenter=(TextView)findViewById(R.id.count_target_steps_center);
//		tvTargetStepsCenter.setText(LoginActivity.stepNumber);
//		
//		tvTargetCost=(TextView)findViewById(R.id.count_target_cost);
//		tvTargetCost.setText(LoginActivity.consume);
//		
//		tvTargetDistance=(TextView)findViewById(R.id.count_target_distance);
//		tvTargetDistance.setText(LoginActivity.distance);
//		
//		tvTodaySteps=(TextView)findViewById(R.id.count_total_steps);
//		tvTodaySteps.setText(BleService.todaySteps+"");
//		
//		tvIntervalSteps=(TextView)findViewById(R.id.count_interval_steps);
//		tvIntervalSteps.setText(BleService.intervalSteps+"");		
//		
//		tvIntervalTime=(TextView)findViewById(R.id.count_interval_time);
//		
//		tvComplete=(TextView)findViewById(R.id.count_complete_percent);
//		if(BleService.todaySteps>Integer.valueOf(LoginActivity.stepNumber)){
//			tvComplete.setText("100.0%");
//		}else if(BleService.todaySteps<0){
//			tvComplete.setText("0.0%");				
//		}else{
//			tvComplete.setText((new DecimalFormat("##0.0")
//			.format((double)BleService.todaySteps/Integer.valueOf(LoginActivity.stepNumber)*100))+"%");
//		}
//		
//		tvStopShow=(TextView)findViewById(R.id.count_stop_show);
//		if(BleService.isCounting){
//			tvStopShow.setVisibility(View.GONE);
//		}else{
//			tvStopShow.setVisibility(View.VISIBLE);
//			tvTodaySteps.setAlpha(0.1f);
//		}
//		
//		tvCost=(TextView)findViewById(R.id.count_cost);
//		if(BleService.todaySteps<0){
//			tvCost.setText("0 k cal");
//		}else{
//			tvCost.setText(new DecimalFormat("####0.0")
//			.format((float)BleService.todaySteps
//					*(float)Integer.valueOf(LoginActivity.stepLength)/100000
//					*(float)Integer.valueOf(LoginActivity.weight)
//					*1.036f)+"k cal");
//		}
//				
//		tvTime=(TextView)findViewById(R.id.count_time);
//		int hour=BleService.sportTime/3600;
//		int minute=(BleService.sportTime-hour*3600)/60;
//		int second=BleService.sportTime%60;
//		DecimalFormat df=new DecimalFormat("00");
//		tvTime.setText(df.format(hour)+":"+df.format(minute)+":"+df.format(second));
//		
//		
//		tvDistance=(TextView)findViewById(R.id.count_distance);
//		if(BleService.todaySteps<0){
//			tvDistance.setText("0m");
//		}else{
//			tvDistance.setText(BleService.todaySteps*Integer.valueOf(LoginActivity.stepLength)/100+"m");
//		}
//
//		if(BleService.todaySteps>Integer.valueOf(LoginActivity.stepNumber)){
//			btnToggleToday.setCurrentCount(100);
//		}else if(BleService.todaySteps<0){
//			btnToggleToday.setCurrentCount(0);
//		}else{
//			btnToggleToday.setCurrentCount(100*BleService.todaySteps/Integer.valueOf(LoginActivity.stepNumber));
//		}
//		
//		sendBroadcast(new Intent().setAction("get_history_steps"));
//		
//		if(LoginActivity.isLogin){
//			new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					
//					HttpServer.updateSteps(LoginActivity.userId, Integer.toString(BleService.todaySteps));
//				}
//			}).start();
//		}		
//
//	}
//	
//	private class MyOnClickListener implements OnClickListener{
//
//		@SuppressWarnings("deprecation")
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			switch (v.getId()) {
//			case R.id.btn_back:
//				finish();
//				break;
//				
//			case R.id.count_shared:
//			case R.id.count_shared_short:
//				
//				Display display=getWindowManager().getDefaultDisplay();
//				int width=display.getWidth();
//				int height=display.getHeight();
//				Bitmap bitmap=Bitmap.createBitmap(width, height, Config.ARGB_8888);
//				
//				View decorView=CountActivity.this.getWindow().getDecorView();
//				decorView.setDrawingCacheEnabled(true);
//				decorView.buildDrawingCache();
//				bitmap=decorView.getDrawingCache();
//				File f=null;
//				try{
//					File folder=new File(Environment.getExternalStorageDirectory()+"/shoes");
//					if(!folder.exists()){
//						folder.mkdirs();
//					}
//					f=new File(Environment.getExternalStorageDirectory()+"/shoes", "share.png");
//				
//					
//					FileOutputStream fos=new FileOutputStream(f);
//					bitmap.compress(CompressFormat.PNG, 80, fos);
//					fos.flush();
//					fos.close();
//					Toast.makeText(CountActivity.this, "截图成功", Toast.LENGTH_SHORT).show();
//				}catch(Exception e){						
//					Log.d("zz", e.toString());
//					return;
//				}
//				
//				Intent intent=new Intent(Intent.ACTION_SEND);
//				
//				File file=new File(Environment.getExternalStorageDirectory()+"/shoes/share.png");
//
//				Uri uri=Uri.fromFile(file);
//				if(uri!=null){
//					intent.setType("image/*");
//					intent.putExtra(Intent.EXTRA_STREAM, uri);
//				}else{
//					intent.setType("text/plain");
//				}								
//				intent.putExtra(Intent.EXTRA_SUBJECT, "share");
//				intent.putExtra(Intent.EXTRA_TEXT, "计步记录");
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				startActivity(Intent.createChooser(intent, "分享" ));
//				break;
//
//			case R.id.count_info:
//				startActivity(new Intent(CountActivity.this, CountMessageActivity.class));
//				break;
//						
//			case R.id.btn_target_steps:
//				LinearLayout dialog1=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_target_steps, null);
//				final NumberPicker np1=(NumberPicker)dialog1.findViewById(R.id.dialog_target_steps);
//				final String[] values1=new String[40];
//				for(int i=0; i<values1.length; i++){
//					values1[i]=String.valueOf((i+1)*500);
//				}
//				np1.setDisplayedValues(values1);	
//				np1.setMinValue(0);
//				np1.setMaxValue(values1.length-1);
//				np1.setValue(Integer.valueOf(LoginActivity.stepNumber)/500-1);		
//				new AlertDialog.Builder(CountActivity.this)
//				.setView(dialog1)
//				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						// TODO Auto-generated method stub
//						if(!values1[np1.getValue()].equals(LoginActivity.stepNumber)){
//							if(LoginActivity.isLogin){
//								executorService.submit(new Runnable() {
//									
//									@Override
//									public void run() {
//										// TODO Auto-generated method stub
//										HashMap<String, String> texts=new HashMap<String, String>();
//										texts.put("users.userId", LoginActivity.userId);
//										texts.put("users.stepNumber", values1[np1.getValue()]);
//										String result=HttpServer.updateUserInfo(texts, null);
//										if(result.equals("0")){
//											handler.post(new Runnable() {
//												
//												@Override
//												public void run() {
//													// TODO Auto-generated method stub
//													LoginActivity.stepNumber=values1[np1.getValue()];
//													tvTargetSteps.setText(LoginActivity.stepNumber);
//													tvTargetStepsCenter.setText(LoginActivity.stepNumber);
//													if(BleService.todaySteps>Integer.valueOf(LoginActivity.stepNumber)){
//														tvComplete.setText("100.0%");
//														btnToggleToday.setCurrentCount(100);
//													}else{
//														tvComplete.setText((new DecimalFormat("##0.0")
//														.format((float)BleService.todaySteps/Integer.valueOf(LoginActivity.stepNumber)*100))+"%");
//														btnToggleToday.setCurrentCount(100*BleService.todaySteps/Integer.valueOf(LoginActivity.stepNumber));
//													}
//												}
//											});
//										}else{
//											handler.post(new Runnable() {
//												
//												@Override
//												public void run() {
//													// TODO Auto-generated method stub
//													Toast.makeText(CountActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
//												}
//											});
//										}
//										
//										
//									}
//								});
//							}else{
//								LoginActivity.stepNumber=values1[np1.getValue()];
//								tvTargetSteps.setText(LoginActivity.stepNumber);
//								tvTargetStepsCenter.setText(LoginActivity.stepNumber);
//								if(BleService.todaySteps>Integer.valueOf(LoginActivity.stepNumber)){
//									tvComplete.setText("100.0%");
//									btnToggleToday.setCurrentCount(100);
//								}else{
//									tvComplete.setText((new DecimalFormat("##0.0")
//									.format((float)BleService.todaySteps/Integer.valueOf(LoginActivity.stepNumber)*100))+"%");
//									btnToggleToday.setCurrentCount(100*BleService.todaySteps/Integer.valueOf(LoginActivity.stepNumber));
//								}
//								getSharedPreferences("default", Context.MODE_PRIVATE).edit()
//										.putString("stepNumber", LoginActivity.stepNumber).commit();
//							}
//							
//						}
//					}
//				})
//				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						// TODO Auto-generated method stub
//						
//					}
//				})
//				.create()
//				.show();
//				break;
//				
//			case R.id.btn_target_cost:
//				LinearLayout dialog2=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_target_cost, null);
//				final NumberPicker np2=(NumberPicker)dialog2.findViewById(R.id.dialog_target_cost);
//				final String[] values2=new String[40];
//				for(int i=0; i<values2.length; i++){
//					values2[i]=String.valueOf((i+1)*500);
//				}
//				np2.setDisplayedValues(values2);	
//				np2.setMinValue(0);
//				np2.setMaxValue(values2.length-1);
//				np2.setValue(Integer.valueOf(LoginActivity.consume)/500-1);				
//				new AlertDialog.Builder(CountActivity.this)
//				.setView(dialog2)
//				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						// TODO Auto-generated method stub
//						if(!values2[np2.getValue()].equals(LoginActivity.consume)){
//							if(LoginActivity.isLogin){
//								executorService.submit(new Runnable() {
//									
//									@Override
//									public void run() {
//										// TODO Auto-generated method stub
//										HashMap<String, String> texts=new HashMap<String, String>();
//										texts.put("users.userId", LoginActivity.userId);
//										texts.put("users.consume", values2[np2.getValue()]);
//										String result=HttpServer.updateUserInfo(texts, null);
//										if(result.equals("0")){
//											handler.post(new Runnable() {
//												
//												@Override
//												public void run() {
//													// TODO Auto-generated method stub
//													LoginActivity.consume=values2[np2.getValue()];
//													tvTargetCost.setText(LoginActivity.consume);
//												}
//											});
//										}else{
//											handler.post(new Runnable() {
//												
//												@Override
//												public void run() {
//													// TODO Auto-generated method stub
//													Toast.makeText(CountActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
//												}
//											});
//										}
//										
//									}
//								});
//							}else{
//								LoginActivity.consume=values2[np2.getValue()];
//								tvTargetCost.setText(LoginActivity.consume);
//								getSharedPreferences("default", Context.MODE_PRIVATE).edit()
//									.putString("consume", LoginActivity.consume).commit();
//							}
//						}
//					}
//				})
//				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						// TODO Auto-generated method stub
//						
//					}
//				})
//				.create()
//				.show();
//				break;
//				
//			case R.id.btn_target_distance:
//				LinearLayout dialog3=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_target_distance, null);
//				final NumberPicker np3=(NumberPicker)dialog3.findViewById(R.id.dialog_target_distance);
//				final String[] values3=new String[40];
//				for(int i=0; i<values3.length; i++){
//					values3[i]=String.valueOf((i+1)*500);
//				}
//				np3.setDisplayedValues(values3);	
//				np3.setMinValue(0);
//				np3.setMaxValue(values3.length-1);
//				np3.setValue(Integer.valueOf(LoginActivity.distance)/500-1);
//				new AlertDialog.Builder(CountActivity.this)
//				.setView(dialog3)
//				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						// TODO Auto-generated method stub
//						if(!values3[np3.getValue()].equals(LoginActivity.distance)){
//							if(LoginActivity.isLogin){
//								executorService.submit(new Runnable() {
//									
//									@Override
//									public void run() {
//										// TODO Auto-generated method stub
//										HashMap<String, String> texts=new HashMap<String, String>();
//										texts.put("users.userId", LoginActivity.userId);
//										texts.put("users.journey", values3[np3.getValue()]);
//										String result=HttpServer.updateUserInfo(texts, null);
//										if(result.equals("0")){
//											handler.post(new Runnable() {
//												
//												@Override
//												public void run() {
//													// TODO Auto-generated method stub
//													LoginActivity.distance=values3[np3.getValue()];
//													tvTargetDistance.setText(LoginActivity.distance);
//												}
//											});
//										}else{
//											handler.post(new Runnable() {
//												
//												@Override
//												public void run() {
//													// TODO Auto-generated method stub
//													Toast.makeText(CountActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
//												}
//											});
//										}
//										
//									}
//								});
//							}else{
//								LoginActivity.distance=values3[np3.getValue()];
//								tvTargetDistance.setText(LoginActivity.distance);
//								getSharedPreferences("default", Context.MODE_PRIVATE).edit()
//									.putString("distance", LoginActivity.distance).commit();
//							}
//							
//						}
//					}
//				})
//				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						// TODO Auto-generated method stub
//						
//					}
//				})
//				.create()
//				.show();
//				break;
//				
//			case R.id.count_btn_start_short:
//				if(BleService.isIntervalCounting){
//					btnStartInterval.setText("开始");
//					sendBroadcast(new Intent().setAction("stop_interval_count"));
//				}else{
//					btnStartInterval.setText("停止");
//					sendBroadcast(new Intent().setAction("start_interval_count"));
//				}								
//				
//				break;
//				
//			case R.id.count_refresh:
//				BleService.intervalTime=0;
//				BleService.intervalSteps=0;
//				BleService.isIntervalCounting=false;
//				tvIntervalTime.setText("00:00:00");
//				tvIntervalSteps.setText("0");
//				btnStartInterval.setText("开始");
//				break;
//				
//			case R.id.count_round_scale:
//				todayStepsLayout.setVisibility(View.GONE);
//				intervalStepsLayout.setVisibility(View.VISIBLE);
//				break;
//				
//			case R.id.count_round_scale_alpha:
//				todayStepsLayout.setVisibility(View.VISIBLE);
//				intervalStepsLayout.setVisibility(View.GONE);
//				break;
//			}
//		}
//		
//	}
//	
//	private class MyBroadcastReceiver extends BroadcastReceiver{
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			// TODO Auto-generated method stub
//			String action=intent.getAction();
//			if(action.equals("update_todaySteps")){
//				if(BleService.todaySteps<=Integer.valueOf(LoginActivity.stepNumber)){
//					btnToggleToday.setCurrentCount(100*BleService.todaySteps/Integer.valueOf(LoginActivity.stepNumber));
//					tvComplete.setText(new DecimalFormat("##0.0")
//					.format((double)BleService.todaySteps/Integer.valueOf(LoginActivity.stepNumber)*100)+"%");
//				}else{
//					tvComplete.setText("100%");
//				}
//				if(BleService.todaySteps>=0){
//					tvTodaySteps.setText(BleService.todaySteps+"");
//					tvCost.setText(new DecimalFormat("####0.0")
//					.format((float)BleService.todaySteps
//							*(float)Integer.valueOf(LoginActivity.stepLength)/100000
//							*(float)Integer.valueOf(LoginActivity.weight)
//							*1.036f)+"k cal");
//					tvDistance.setText(BleService.todaySteps*Integer.valueOf(LoginActivity.stepLength)/100+"m");
//				}else{
//					tvTodaySteps.setText("0");
//					tvCost.setText("0k");
//					tvDistance.setText("0m");
//				}
//			}
//			if(action.equals("update_sport_time")){
//				int hour=BleService.sportTime/3600;
//				int minute=(BleService.sportTime-hour*3600)/60;
//				int second=BleService.sportTime%60;
//				DecimalFormat df=new DecimalFormat("00");
//				tvTime.setText(df.format(hour)+":"+df.format(minute)+":"+df.format(second));
//			}
//			if(action.equals("update_intervalSteps")){
//				tvIntervalSteps.setText(BleService.intervalSteps+"");
//			}
//			if(action.equals("update_intervalTime")){
//				int hour=BleService.intervalTime/3600;
//				int minute=(BleService.intervalTime-hour*3600)/60;
//				int second=BleService.intervalTime%60;
//				DecimalFormat df=new DecimalFormat("00");
//				tvIntervalTime.setText(df.format(hour)+":"+df.format(minute)+":"+df.format(second));
//			}
//			if(action.equals("update_history_steps")){
//				int[] historySteps=intent.getIntArrayExtra("history_steps");				
//				long nowTime=System.currentTimeMillis();				
//				for(int i=0; i<historySteps.length; i++){
//					Date date=new Date(nowTime-(historySteps.length-i-1)*24*60*60*1000);
//					String dateString=(String) DateFormat.format("MM-dd", date);
//					Cursor c=db.rawQuery("SELECT * FROM todaySteps WHERE date=?", new String[]{dateString});
//					if(c.moveToNext()){
//						ContentValues cv=new ContentValues();
//						cv.put("steps", historySteps[i]);
//						db.update("todaySteps", cv, "date=?", new String[]{dateString});
//					}
//				}
//				handler.post(new Runnable() {
//					
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						chartLayout.removeViewAt(0);
//						chartLayout.addView(getGraphicalView(), 0);
//					}
//				});
//			}
//		}
//		
//	}
//
//	//获取折线图
//	private GraphicalView getGraphicalView(){
//		
//		//X轴标签
//		ArrayList<String> labels=new ArrayList<String>();
//		//数据
//		ArrayList<Integer> datas=new ArrayList<Integer>();
//		
//		//从数据库取出数据
//		int max=0;
//		Cursor c=db.rawQuery("SELECT * FROM todaySteps", null);
//		while(c.moveToNext()){
//			labels.add(c.getString(c.getColumnIndex("date")));
//			int temp=c.getInt(c.getColumnIndex("steps"));
//			if(temp>max) max=temp;
//			datas.add(temp);
//		}
//		//将前两个标签改为特定名称
//		if(labels.size()>2){
//			labels.set(labels.size()-1, "当天");
//			labels.set(labels.size()-2, "前一天");
//		}
//
//		XYMultipleSeriesDataset dataset=new XYMultipleSeriesDataset();
//		XYSeries series=new XYSeries("步数");
//				
//		//数据, 只能递加添加数据，递减添加数据会报错，原因不知，应该是源码bug
//		for(int i=7-labels.size(); i<7; i++){
//			series.add(i+1, datas.get(i-7+datas.size()));
//		}
//		dataset.addSeries(series);
//		
//		//轴渲染器
//		XYMultipleSeriesRenderer renderer=new XYMultipleSeriesRenderer();
//		//X、Y的最大最小值
//		renderer.setYAxisMin(0);
//		renderer.setYAxisMax(max+max*0.25f);//加上0.25倍的空间以显示数字，避免出界
//		renderer.setXAxisMin(0.5);
//		renderer.setXAxisMax(7.5);
//		//不显示标示
//		renderer.setShowLegend(false);
//		//设置边距，参数上，左，下，右
//		renderer.setMargins(new int[]{10, -5, 5, -5});
//		//设置边距颜色为透明
//		renderer.setMarginsColor(0x00ffffff);
//		//设置XY轴是否可以延伸
//		renderer.setPanEnabled(true, false);
//		//设置点的大小
//		renderer.setPointSize(15f);
//		//显示表格
//		renderer.setShowGrid(true);
//		//设置折线图是否可以伸缩
//		renderer.setZoomEnabled(false, false);
//		//设置自定义X轴标签
//		for(int i=7-labels.size(); i<7; i++){
//			renderer.addXTextLabel(i+1, labels.get(labels.size()+i-7));
//		}
//		//标签文字大小
//		renderer.setLabelsTextSize(30);
//		renderer.setXLabels(0);
//		renderer.setYLabels(0);
//		
//		//折线渲染器
//		XYSeriesRenderer r=new XYSeriesRenderer();
//		//折线颜色
//		r.setColor(Color.WHITE);
//		//点的类型
//		r.setPointStyle(PointStyle.CIRCLE);
//		r.setFillPoints(true);
//		//显示值
//		r.setDisplayChartValues(true);
//		//值与点的距离
//		r.setChartValuesSpacing(30);
//		//值的文字大小
//		r.setChartValuesTextSize(35);
//				
//		renderer.addSeriesRenderer(r);
//		
//		return ChartFactory.getLineChartView(this, dataset, renderer);
//	}
//		
//}
