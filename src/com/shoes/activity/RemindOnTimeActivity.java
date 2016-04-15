 
package com.shoes.activity; 

import java.io.IOException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.shoes.R;
import com.shoes.service.BleService;
import com.shoes.util.ClassParseUtil;


public class RemindOnTimeActivity extends Activity {
	
	private ImageButton btnBack;
	private Button btnAdd;
	private ListView listAlarm;
	private ArrayList<HashMap<String, Object>> alarms;
	private AlarmBaseAdapter adapter;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_remind_on_time);
		
		preferences=getSharedPreferences("alarms", MODE_PRIVATE);
		editor=preferences.edit();
		
		alarms=new ArrayList<HashMap<String,Object>>();
		String s=preferences.getString("alarms", null);
		if(s!=null){
			try {
				alarms=(ArrayList<HashMap<String, Object>>) ClassParseUtil.string2List(s);
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		btnBack=(ImageButton)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnAdd=(Button)findViewById(R.id.btn_add_remind);
		btnAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(RemindOnTimeActivity.this, RemindOnTimeSettingActivity.class);
				intent.putExtra("isSet", false);
				startActivityForResult(intent, 100);
			}
		});
		
		listAlarm=(ListView)findViewById(R.id.alarm_list);
		adapter=new AlarmBaseAdapter(this, alarms);
		listAlarm.setAdapter(adapter);
		
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("zz", requestCode+ "  "+ resultCode);
		//闹钟设置页面按了确定后返回时，结果码=1，请求码为100时是新建闹钟，等于其它时为已有闹钟的编号
		if(resultCode==1){
			//新建闹钟
			if(requestCode==100){
				if(BleService.isLeftConnected || BleService.isRightConnected){
					HashMap<String, Object> alarm=new HashMap<String, Object>();
					boolean[] day=data.getBooleanArrayExtra("day");
					int hour=data.getIntExtra("hour", new Date().getDay());
					int minute=data.getIntExtra("minute", new Date().getMinutes());
					String remark=data.getStringExtra("remark");
					alarm.put("day", day);
					alarm.put("hour", hour);
					alarm.put("minute", minute);
					alarm.put("isOpen", true);
					alarm.put("remark", remark);
					alarms.add(alarm);
					adapter.notifyDataSetChanged();
					try {
						editor.putString("alarms", ClassParseUtil.list2String(alarms)).commit();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Intent intent=new Intent();
					intent.setAction("set_alarms");
					intent.putExtra("alarms_data", (Serializable)alarms);
					sendBroadcast(intent);
				}else{
					Toast.makeText(this, "没有连接设备，无法添加闹钟", Toast.LENGTH_SHORT).show();
				}
			//修改已有闹钟	
			}else{
				if(BleService.isLeftConnected || BleService.isRightConnected){
					boolean[] day=data.getBooleanArrayExtra("day");
					int hour=data.getIntExtra("hour", new Date().getDay());
					int minute=data.getIntExtra("minute", new Date().getMinutes());
					String remark=data.getStringExtra("remark");
					alarms.get(requestCode).put("day", day);
					alarms.get(requestCode).put("hour", hour);
					alarms.get(requestCode).put("minute", minute);
					alarms.get(requestCode).put("isOpen", true);
					alarms.get(requestCode).put("remark", remark);
					adapter.notifyDataSetChanged();
					try {
						editor.putString("alarms", ClassParseUtil.list2String(alarms)).commit();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Intent intent=new Intent();
					intent.setAction("set_alarms");
					intent.putExtra("alarms_data", (Serializable)alarms);
					sendBroadcast(intent);
				}else{
					Toast.makeText(this, "没有连接设备，无法修改闹钟", Toast.LENGTH_SHORT).show();
				}
			}
		}
		
	}
	
	private class AlarmBaseAdapter extends BaseAdapter{
		
		Context context;
		ArrayList<HashMap<String, Object>> datas;
		
		private class ViewHolder{
			TextView tvTime;
			TextView tvDay;
			CheckBox isOpen;
			LinearLayout layout;
		}
		
		public AlarmBaseAdapter(Context context, ArrayList<HashMap<String, Object>> datas){
			this.context=context;
			this.datas=datas;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return datas.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return datas.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup group) {
			// TODO Auto-generated method stub
			ViewHolder holder=null;
			if(convertView!=null){
				holder=(ViewHolder) convertView.getTag();
			}else{
				convertView=LayoutInflater.from(context).inflate(R.layout.list_item_alarm, null);
				holder=new ViewHolder();
				holder.tvTime=(TextView)convertView.findViewById(R.id.list_item_time);
				holder.tvDay=(TextView)convertView.findViewById(R.id.list_item_day);
				holder.isOpen=(CheckBox)convertView.findViewById(R.id.list_item_isOpen);
				holder.layout=(LinearLayout)convertView.findViewById(R.id.alarm_item_layout);
				convertView.setTag(holder);
			}
			
			//设置item时间
			DecimalFormat format=new DecimalFormat("00");
			holder.tvTime.setText(format.format(datas.get(position).get("hour"))+":"
													+format.format(datas.get(position).get("minute")));
			//设置item周期
			boolean[] day=(boolean[]) datas.get(position).get("day");			
			String dayInfo="";
			int numOfSelected=0;
			for(int i=0; i<day.length; i++){
				if(day[i]){
					numOfSelected++;
				}				
			}			
			if(numOfSelected==7){
				dayInfo="每天";
			}else if(numOfSelected==0){
				dayInfo="只响一次";
			}else{
				dayInfo=(day[0]?"周一  ":"")+(day[1]?"周二  ":"")+(day[2]?"周三  ":"")
						+(day[3]?"周四  ":"")+(day[4]?"周五  ":"")+(day[5]?"周六  ":"")+(day[6]?"周日":"");
			}
			holder.tvDay.setText(dayInfo);
			
			//设置item开启状态
			if((Boolean) datas.get(position).get("isOpen")){
				holder.isOpen.setChecked(true);
			}else{
				holder.isOpen.setChecked(false);
			}
			holder.isOpen.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
					// TODO Auto-generated method stub
					if(BleService.isLeftConnected || BleService.isRightConnected){
						if(isChecked){
							datas.get(position).put("isOpen", true);
						}else{
							datas.get(position).put("isOpen", false);
						}
						//每次改变状态后保存
						try {
							editor.putString("alarms", ClassParseUtil.list2String(alarms)).commit();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Intent intent=new Intent();
						intent.setAction("set_alarms");
						intent.putExtra("alarms_data", (Serializable)alarms);
						sendBroadcast(intent);
					}else{
						Toast.makeText(RemindOnTimeActivity.this, "没有连接设备，无法开启或取消闹钟", Toast.LENGTH_SHORT).show();
					}
					
				}
			});
			
			holder.layout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Intent intent=new Intent(RemindOnTimeActivity.this, RemindOnTimeSettingActivity.class);
					HashMap<String, Object> alarm=alarms.get(position);
					intent.putExtra("day", (boolean[])alarm.get("day"));
					intent.putExtra("hour", (Integer)alarm.get("hour"));
					intent.putExtra("minute", (Integer)alarm.get("minute"));
					intent.putExtra("remark", (String)alarm.get("remark"));
					intent.putExtra("isSet", true);//标志是否是已有闹钟
					startActivityForResult(intent, position);
				}
			});
			
			holder.layout.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View arg0) {
					// TODO Auto-generated method stub
					LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_delete_alarm, null);
					new AlertDialog.Builder(RemindOnTimeActivity.this)
					.setView(dialog)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							if(BleService.isLeftConnected || BleService.isRightConnected){
								datas.remove(position);
								adapter.notifyDataSetChanged();
								try {
									editor.putString("alarms", ClassParseUtil.list2String(alarms)).commit();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								Intent intent=new Intent();
								intent.setAction("set_alarms");
								intent.putExtra("alarms_data", (Serializable)alarms);
								sendBroadcast(intent);
							}else{
								Toast.makeText(RemindOnTimeActivity.this, "没有连接设备,无法删除闹钟", Toast.LENGTH_SHORT).show();
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
					return true;
				}
			});
			
			return convertView;
		}
		
	}
}
