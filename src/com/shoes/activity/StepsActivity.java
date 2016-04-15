package com.shoes.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.example.shoes.R;
import com.shoes.customview.RoundProgressBar;
import com.shoes.database.DatabaseHelper;
import com.shoes.service.BleService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StepsActivity extends Activity {
	
	private ImageView btnBack, btnMessage, btnShare, btnStartOrStop, btnShareInterval, btnRefresh;
	private LinearLayout btnTargetSteps, btnTargetCalorie, btnTargetDistance;
	private RelativeLayout layoutTotalSteps, layoutIntervalSteps, layoutChart;
	private RoundProgressBar pgTotalSteps, pgIntervalSteps;
	private TextView tvCalorie, tvSportTime, tvDistance, tvTargetSteps, tvTargetCalorie, tvTargetDistance;
	private TextView tvCalorieTitle, tvSportTimeTitle, tvDistanceTitle;
	private TextView tvTotalSteps, tvIntervalSteps, tvComplete, tvTargetStepsCenter, tvStopShow;
	private Button btnStartOrStopInterval;
	private MyOnClickListener listener;
	private MyBroadcastReceiver receiver;
	private SQLiteDatabase db;
	
	private int targetSteps, targetDistance;
	private float targetCalorie;
	private boolean isOnTotalSteps;
	private String dateString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_count);
		
		DatabaseHelper dateHelper=new DatabaseHelper(this, "sport_data");
		db=dateHelper.getWritableDatabase();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		dateString=sdf.format(Calendar.getInstance().getTime());
		Cursor c=db.rawQuery("SELECT * FROM sportData WHERE date=?", new String[]{dateString});
		if(!c.moveToNext()){
			db.execSQL("INSERT INTO sportData VALUES(null, ?, ?)", new Object[]{BleService.totalSteps, dateString});
		}
		
		receiver=new MyBroadcastReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("update_sportTime_calorie_distance");
		filter.addAction("update_steps");
		registerReceiver(receiver, filter);
		
		targetSteps=getSharedPreferences("sport_data", Context.MODE_PRIVATE).getInt("targetSteps", 10000);
		targetCalorie=getSharedPreferences("sport_data", Context.MODE_PRIVATE).getFloat("targetCalorie", 100f);
		targetDistance=getSharedPreferences("sport_data", Context.MODE_PRIVATE).getInt("targetDistance", 20000);
		isOnTotalSteps=getSharedPreferences("sport_data", Context.MODE_PRIVATE).getBoolean("isOnTotalSteps", true);
		
		listener=new MyOnClickListener();
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(listener);
		
		btnMessage=(ImageView)findViewById(R.id.btn_message);
		btnMessage.setOnClickListener(listener);
		
		btnShare=(ImageView)findViewById(R.id.btn_shared);
		btnShare.setOnClickListener(listener);
		
		btnShareInterval=(ImageView)findViewById(R.id.btn_shared_short);
		btnShareInterval.setOnClickListener(listener);
		
		btnStartOrStop=(ImageView)findViewById(R.id.btn_start);
		btnStartOrStop.setOnClickListener(listener);
		if(BleService.isTotalCounting){
			btnStartOrStop.setImageResource(R.drawable.count_stop_icon);
		}else{
			btnStartOrStop.setImageResource(R.drawable.count_start_icon);
		}
		
		btnRefresh=(ImageView)findViewById(R.id.btn_refresh);
		btnRefresh.setOnClickListener(listener);
		
		btnStartOrStopInterval=(Button)findViewById(R.id.btn_start_short);
		btnStartOrStopInterval.setOnClickListener(listener);
		if(BleService.isIntervalCounting){
			btnStartOrStopInterval.setText("停止");
		}else{
			btnStartOrStopInterval.setText("开始");
		}		
		
		tvCalorieTitle=(TextView)findViewById(R.id.tv_calorie_title);
		
		tvCalorie=(TextView)findViewById(R.id.tv_calorie);
		
		tvSportTimeTitle=(TextView)findViewById(R.id.tv_sport_time_title);
		
		tvSportTime=(TextView)findViewById(R.id.tv_sport_time);
		
		tvDistanceTitle=(TextView)findViewById(R.id.tv_distance_title);
		
		tvDistance=(TextView)findViewById(R.id.tv_distance);
		
		tvTotalSteps=(TextView)findViewById(R.id.tv_total_steps);
		tvTotalSteps.setText(BleService.totalSteps+"");
		
		tvIntervalSteps=(TextView)findViewById(R.id.tv_interval_steps);
		tvIntervalSteps.setText(BleService.intervalSteps+"");
		
		tvComplete=(TextView)findViewById(R.id.tv_complete);
		float percent=BleService.totalSteps*100f/targetSteps;
		if(percent>100){
			tvComplete.setText("100%");
		}else if(percent<0){
			tvComplete.setText("0%");
		}else{
			tvComplete.setText(percent2String(percent)+"%");
		}		
		
		tvStopShow=(TextView)findViewById(R.id.tv_stop_show);
		if(BleService.isTotalCounting){
			tvStopShow.setVisibility(View.GONE);
			tvTotalSteps.setAlpha(1.0f);
		}else{
			tvStopShow.setVisibility(View.VISIBLE);
			tvTotalSteps.setAlpha(0.1f);
			
		}
		
		tvTargetStepsCenter=(TextView)findViewById(R.id.tv_target_steps_center);
		tvTargetStepsCenter.setText(targetSteps+"");
		
		layoutTotalSteps=(RelativeLayout)findViewById(R.id.layout_total_steps);
		
		layoutIntervalSteps=(RelativeLayout)findViewById(R.id.layout_interval_steps);
		
		if(isOnTotalSteps){
			layoutTotalSteps.setVisibility(View.VISIBLE);
			layoutIntervalSteps.setVisibility(View.GONE);
			tvCalorieTitle.setText("卡路里");
			tvSportTimeTitle.setText("运动时间");
			tvDistanceTitle.setText("距离");
			tvCalorie.setText(calorie2String(BleService.totalCalorie)+"k cal");
			tvSportTime.setText(time2String(BleService.totalSportTime));
			tvDistance.setText(BleService.totalDistance+"m");
		}else{
			layoutTotalSteps.setVisibility(View.GONE);
			layoutIntervalSteps.setVisibility(View.VISIBLE);
			tvCalorieTitle.setText("区间卡路里");
			tvSportTimeTitle.setText("区间运动时间");
			tvDistanceTitle.setText("区间距离");
			tvCalorie.setText(calorie2String(BleService.intervalCalorie)+"k cal");
			tvSportTime.setText(time2String(BleService.intervalSportTime));
			tvDistance.setText(BleService.intervalDistance+"m");
		}
				
		pgTotalSteps=(RoundProgressBar)findViewById(R.id.pg_total_steps);
		pgTotalSteps.setOnClickListener(listener);
		int currentCount=BleService.totalSteps*100/targetSteps;
		if(currentCount>100){
			pgTotalSteps.setCurrentCount(100);
		}else if(currentCount<0){
			pgTotalSteps.setCurrentCount(0);
		}else{
			pgTotalSteps.setCurrentCount(currentCount);
		}
		
		pgIntervalSteps=(RoundProgressBar)findViewById(R.id.pg_interval_steps);
		pgIntervalSteps.setOnClickListener(listener);
		
		btnTargetSteps=(LinearLayout)findViewById(R.id.btn_target_steps);
		btnTargetSteps.setOnClickListener(listener);
		
		btnTargetCalorie=(LinearLayout)findViewById(R.id.btn_target_calorie);
		btnTargetCalorie.setOnClickListener(listener);
		
		btnTargetDistance=(LinearLayout)findViewById(R.id.btn_target_distance);
		btnTargetDistance.setOnClickListener(listener);
		
		tvTargetSteps=(TextView)findViewById(R.id.tv_target_steps);
		tvTargetSteps.setText(targetSteps+"");
		
		tvTargetDistance=(TextView)findViewById(R.id.tv_target_distance);
		tvTargetDistance.setText(targetDistance+"m");
		
		tvTargetCalorie=(TextView)findViewById(R.id.tv_target_calorie);
		tvTargetCalorie.setText(targetCalorie+"k cal");
		
		layoutChart=(RelativeLayout)findViewById(R.id.chart_layout);
		layoutChart.addView(getGraphicalView(), 0);
		
	}
	
	private class MyOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id=v.getId();
			if(id==R.id.btn_back){
				finish();
			}else if(id==R.id.btn_message){
				startActivity(new Intent(StepsActivity.this, CountMessageActivity.class));
			}else if(id==R.id.btn_target_steps){
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_target, null);
				TextView tvTitle=(TextView)dialog.findViewById(R.id.tv);
				tvTitle.setText("目标步数");
				final NumberPicker np=(NumberPicker)dialog.findViewById(R.id.np);
				String[] values=new String[40];
				for(int i=0; i<values.length; i++){
					values[i]=String.valueOf((i+1)*500);
				}
				np.setDisplayedValues(values);	
				np.setMinValue(0);
				np.setMaxValue(values.length-1);
				np.setValue(Integer.valueOf(targetSteps)/500-1);		
				new AlertDialog.Builder(StepsActivity.this)
				.setView(dialog)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						targetSteps=(np.getValue()+1)*500;
						tvTargetSteps.setText(targetSteps+"");
						tvTargetStepsCenter.setText(targetSteps+"");
						float percent=BleService.totalSteps*100f/targetSteps;
						if(percent>100){
							tvComplete.setText("100%");
						}else if(percent<0){
							tvComplete.setText("0%");
						}else{
							tvComplete.setText(percent2String(percent)+"%");
						}
						int currentCount=BleService.totalSteps*100/targetSteps;
						if(currentCount>100){
							pgTotalSteps.setCurrentCount(100);
						}else if(currentCount<0){
							pgTotalSteps.setCurrentCount(0);
						}else{
							pgTotalSteps.setCurrentCount(currentCount);
						}
						getSharedPreferences("sport_data", Context.MODE_PRIVATE).edit().putInt("targetSteps", targetSteps).commit();
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
			}else if(id==R.id.btn_target_distance){
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_target, null);
				TextView tvTitle=(TextView)dialog.findViewById(R.id.tv);
				tvTitle.setText("目标路程");
				final NumberPicker np=(NumberPicker)dialog.findViewById(R.id.np);
				String[] values=new String[40];
				for(int i=0; i<values.length; i++){
					values[i]=String.valueOf((i+1)*500);
				}
				np.setDisplayedValues(values);	
				np.setMinValue(0);
				np.setMaxValue(values.length-1);
				np.setValue(Integer.valueOf(targetDistance)/500-1);					
				new AlertDialog.Builder(StepsActivity.this)
				.setView(dialog)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						targetDistance=(np.getValue()+1)*500;
						tvTargetDistance.setText(targetDistance+"m");
						getSharedPreferences("sport_data", Context.MODE_PRIVATE).edit().putInt("targetDistance", targetDistance).commit();
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
			}else if(id==R.id.btn_target_calorie){
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_target, null);
				TextView tvTitle=(TextView)dialog.findViewById(R.id.tv);
				tvTitle.setText("目标卡路里");
				final NumberPicker np=(NumberPicker)dialog.findViewById(R.id.np);
				String[] values=new String[40];
				for(int i=0; i<values.length; i++){
					values[i]=String.valueOf((i+1)*10);
				}
				np.setDisplayedValues(values);	
				np.setMinValue(0);
				np.setMaxValue(values.length-1);
				np.setValue(Integer.valueOf(targetDistance)/10-1);					
				new AlertDialog.Builder(StepsActivity.this)
				.setView(dialog)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						targetCalorie=(np.getValue()+1)*10;
						tvTargetCalorie.setText(targetCalorie+"k cal");
						getSharedPreferences("sport_data", Context.MODE_PRIVATE).edit().putInt("targetDistance", targetDistance).commit();
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
			}else if(id==R.id.pg_total_steps){
				isOnTotalSteps=false;
				layoutTotalSteps.setVisibility(View.GONE);
				layoutIntervalSteps.setVisibility(View.VISIBLE);
				tvCalorieTitle.setText("区间卡路里");
				tvSportTimeTitle.setText("区间运动时间");
				tvDistanceTitle.setText("区间距离");
				tvCalorie.setText(calorie2String(BleService.intervalCalorie)+"k cal");
				tvSportTime.setText(time2String(BleService.intervalSportTime));
				tvDistance.setText(BleService.intervalDistance+"m");
				getSharedPreferences("sport_data", Context.MODE_PRIVATE).edit().putBoolean("isOnTotalSteps", false).commit();
			}else if(id==R.id.pg_interval_steps){
				isOnTotalSteps=true;
				layoutTotalSteps.setVisibility(View.VISIBLE);
				layoutIntervalSteps.setVisibility(View.GONE);
				tvCalorieTitle.setText("卡路里");
				tvSportTimeTitle.setText("运动时间");
				tvDistanceTitle.setText("距离");
				tvCalorie.setText(calorie2String(BleService.totalCalorie)+"k cal");
				tvSportTime.setText(time2String(BleService.totalSportTime));
				tvDistance.setText(BleService.totalDistance+"m");
				getSharedPreferences("sport_data", Context.MODE_PRIVATE).edit().putBoolean("isOnTotalSteps", true).commit();
			}else if(id==R.id.btn_start){
				if(BleService.isTotalCounting){
					btnStartOrStop.setImageResource(R.drawable.count_start_icon);
					tvStopShow.setVisibility(View.VISIBLE);
					tvTotalSteps.setAlpha(0.1f);
					BleService.isTotalCounting=false;
					getSharedPreferences("sport_data", Context.MODE_PRIVATE).edit().putBoolean("isTotalCounting", false).commit();
					sendBroadcast(new Intent("stop_count"));
				}else{
					btnStartOrStop.setImageResource(R.drawable.count_stop_icon);
					tvStopShow.setVisibility(View.GONE);
					tvTotalSteps.setAlpha(1.0f);
					BleService.isTotalCounting=true;
					getSharedPreferences("sport_data", Context.MODE_PRIVATE).edit().putBoolean("isTotalCounting", true).commit();
					sendBroadcast(new Intent("start_count"));
				}
			}else if(id==R.id.btn_start_short){
				if(BleService.isIntervalCounting){
					btnStartOrStopInterval.setText("开始");
					BleService.isIntervalCounting=false;
					getSharedPreferences("sport_data", Context.MODE_PRIVATE).edit().putBoolean("isIntervalCounting", false).commit();
				}else{
					btnStartOrStopInterval.setText("停止");
					BleService.isIntervalCounting=true;
					getSharedPreferences("sport_data", Context.MODE_PRIVATE).edit().putBoolean("isIntervalCounting", true).commit();
					
					btnStartOrStop.setImageResource(R.drawable.count_stop_icon);
					tvStopShow.setVisibility(View.GONE);
					tvTotalSteps.setAlpha(1.0f);
					BleService.isTotalCounting=true;
					getSharedPreferences("sport_data", Context.MODE_PRIVATE).edit().putBoolean("isTotalCounting", true).commit();
					sendBroadcast(new Intent("start_count"));
				}
			}else if(id==R.id.btn_refresh){
				BleService.isIntervalCounting=false;
				BleService.intervalCalorie=0f;
				BleService.intervalDistance=0;
				BleService.intervalSportTime=0;
				BleService.intervalSteps=0;				
				btnStartOrStopInterval.setText("开始");
				tvIntervalSteps.setText(BleService.intervalSteps+"");
				tvCalorie.setText(calorie2String(BleService.intervalCalorie)+"k cal");
				tvSportTime.setText(time2String(BleService.intervalSportTime));
				tvDistance.setText(BleService.intervalDistance+"m");
			}else if(id==R.id.btn_shared || id==R.id.btn_shared_short){
				LinearLayout layout=(LinearLayout)findViewById(R.id.layout_all);
				int width=layout.getWidth();
				int height=layout.getHeight();
				Bitmap bitmap=Bitmap.createBitmap(width, height, Config.ARGB_8888);
				layout.setDrawingCacheEnabled(true);
				layout.buildDrawingCache();
				bitmap=layout.getDrawingCache();
				
				File f=null;
				try{
					File folder=new File(Environment.getExternalStorageDirectory()+"/com.tokool.shoes");
					if(!folder.exists()){
						folder.mkdirs();
					}
					f=new File(Environment.getExternalStorageDirectory()+"/com.tokool.shoes", "share.png");
				
					
					FileOutputStream fos=new FileOutputStream(f);
					bitmap.compress(CompressFormat.PNG, 80, fos);
					fos.flush();
					fos.close();
					Toast.makeText(StepsActivity.this,"截图成功", Toast.LENGTH_SHORT).show();
				}catch(Exception e){						
					Log.d("zz", e.toString());
					return;
				}
				
				Intent intent=new Intent(Intent.ACTION_SEND);
				
				File file=new File(Environment.getExternalStorageDirectory()+"/com.tokool.shoes/share.png");

				Uri uri=Uri.fromFile(file);
				if(uri!=null){
					intent.setType("image/*");
					intent.putExtra(Intent.EXTRA_STREAM, uri);
				}else{
					intent.setType("text/plain");
				}								
				intent.putExtra(Intent.EXTRA_SUBJECT, "share");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(Intent.createChooser(intent, "分享" ));
				layout.setDrawingCacheEnabled(false);
				layout.destroyDrawingCache();
			}
		}
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
		if(db!=null){
			db.close();
		}
	}

	private class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();
			if(action.equals("update_sportTime_calorie_distance")){
				if(isOnTotalSteps){
					tvCalorie.setText(calorie2String(BleService.totalCalorie)+"k cal");
					tvSportTime.setText(time2String(BleService.totalSportTime));
					tvDistance.setText(BleService.totalDistance+"m");
				}else{
					tvCalorie.setText(calorie2String(BleService.intervalCalorie)+"k cal");
					tvSportTime.setText(time2String(BleService.intervalSportTime));
					tvDistance.setText(BleService.intervalDistance+"m");
				}
				db.execSQL("UPDATE sportData SET steps=? WHERE date=?", new Object[]{BleService.totalSteps, dateString});
				layoutChart.removeViewAt(0);
				layoutChart.addView(getGraphicalView(), 0);
			}
			if(action.equals("update_steps")){
				tvTotalSteps.setText(BleService.totalSteps+"");
				tvIntervalSteps.setText(BleService.intervalSteps+"");
				int currentCount=BleService.totalSteps*100/targetSteps;
				if(currentCount>100){
					pgTotalSteps.setCurrentCount(100);
				}else if(currentCount<0){
					pgTotalSteps.setCurrentCount(0);
				}else{
					pgTotalSteps.setCurrentCount(currentCount);
				}
				float percent=BleService.totalSteps*100f/targetSteps;
				if(percent>100){
					tvComplete.setText("100%");
				}else if(percent<0){
					tvComplete.setText("0%");
				}else{
					tvComplete.setText(percent2String(percent)+"%");
				}
				
			}
		}
		
	}
	
	//获取折线图,30天数据量
	private GraphicalView getGraphicalView(){
		
		//X轴标签
		ArrayList<String> labels=new ArrayList<String>();
		//数据
		ArrayList<Integer> datas=new ArrayList<Integer>();
		
		//从数据库取出数据
		int max=0;
		Calendar calendar=Calendar.getInstance();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");	
		calendar.add(Calendar.DAY_OF_MONTH, -29);
		for(int i=0; i<30; i++){			
			String dateStringTemp=sdf.format(calendar.getTime());
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			labels.add(dateStringTemp.substring(5, 10));
			Cursor c=db.rawQuery("SELECT steps FROM sportData WHERE date=?", new String[]{dateStringTemp});
			if(c.moveToNext()){
				int data=c.getInt(c.getColumnIndex("steps"));
				if(data>max) max=data;
				datas.add(data);
			}else{
				datas.add(0);
			}	
			c.close();
		}

		//将前两个标签改为特定名称
		if(labels.size()>2){
			labels.set(labels.size()-1, "当天");
			labels.set(labels.size()-2, "前一天");
		}

		XYMultipleSeriesDataset dataset=new XYMultipleSeriesDataset();
		XYSeries series=new XYSeries("步数");
				
		//数据, 只能递加添加数据，递减添加数据会报错，原因不知，应该是源码bug
		for(int i=7-labels.size(); i<7; i++){
			series.add(i+1, datas.get(i-7+datas.size()));
		}
		dataset.addSeries(series);
		
		//轴渲染器
		XYMultipleSeriesRenderer renderer=new XYMultipleSeriesRenderer();
		//X、Y的最大最小值
		renderer.setYAxisMin(0);
		renderer.setYAxisMax(max+max*0.25f);//加上0.25倍的空间以显示数字，避免出界
		renderer.setXAxisMin(0.5);
		renderer.setXAxisMax(7.5);
		//不显示标示
		renderer.setShowLegend(false);
		//设置边距，参数上，左，下，右
		renderer.setMargins(new int[]{10, -5, 5, -5});
		//设置边距颜色为透明
		renderer.setMarginsColor(0x00ffffff);
		//设置XY轴是否可以延伸
		renderer.setPanEnabled(true, false);
		//设置点的大小
		renderer.setPointSize(dp2px(4));
		//显示表格
		renderer.setShowGrid(true);
		//设置折线图是否可以伸缩
		renderer.setZoomEnabled(false, false);
		//设置自定义X轴标签
		for(int i=7-labels.size(); i<7; i++){
			renderer.addXTextLabel(i+1, labels.get(labels.size()+i-7));
		}
		//标签文字大小
		renderer.setLabelsTextSize(30);
		renderer.setXLabels(0);
		renderer.setYLabels(0);
		
		//折线渲染器
		XYSeriesRenderer r=new XYSeriesRenderer();
		//折线颜色
		r.setColor(Color.WHITE);
		//点的类型
		r.setPointStyle(PointStyle.CIRCLE);
		r.setPointStrokeWidth(dp2px(2));
		r.setFillPoints(false);
		//显示值
		r.setDisplayChartValues(true);
		//值与点的距离
		r.setChartValuesSpacing(30);
		//值的文字大小
		r.setChartValuesTextSize(35);
				
		renderer.addSeriesRenderer(r);
		
		return ChartFactory.getLineChartView(this, dataset, renderer);
	}
	
	//格式化
	private String time2String(int time){
		int hour=time/3600;
		int minute=(time-hour*3600)/60;
		int second=time%60;
		DecimalFormat df=new DecimalFormat("00");		
		return new String(df.format(hour)+":"+df.format(minute)+":"+df.format(second));
	}
	
	private String calorie2String(float calorie){
		DecimalFormat format=new DecimalFormat("0.0");
		return format.format(calorie);
	}
	
	private String percent2String(float pencent){
		DecimalFormat format=new DecimalFormat("0.0");
		return format.format(pencent);
	}
	
	//dp转px
	private int dp2px(int dp){
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
	}

}
