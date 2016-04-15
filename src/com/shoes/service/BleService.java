 
package com.shoes.service; 

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import com.shoes.activity.CallingActivity;
import com.shoes.activity.SearchPhoneAlarmActivity;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class BleService extends Service {

	private UUID serviceUUID=UUID.fromString("00001523-1212-efde-1523-785feabcd123");
	private UUID characteristicWriteUUID=UUID.fromString("00001524-1212-efde-1523-785feabcd123");
	private UUID characteristicReadUUID=UUID.fromString("00001525-1212-efde-1523-785feabcd123");
	private UUID descriptorUUID=UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	
	private MyBroadcastReceiver receiver;	
	private BluetoothManager btManager;
	private BluetoothAdapter btAdapter;
	private BluetoothGatt btGattLeft, btGattRight;
	private BluetoothGattService btGattServiceLeft, btGattServiceRight;
	private BluetoothGattCharacteristic characteristicLeft, characteristicRight;
	private Handler handler;
	private Timer timer;
	
	//连接状态
	private boolean isLeftLastConnected, isRightLastConnected; //标注是否为已连接状态退出
	public static boolean isLeftConnected=false; //标注当前左右连接状态
	public static boolean isRightConnected=false;  
	public static String leftAddress, rightAddress;
	
	//计步
	public static int totalSteps;
	public static float totalCalorie;
	public static int totalDistance;
	public static int totalSportTime;
	public static int intervalSteps;
	public static float intervalCalorie;
	public static int intervalDistance;
	public static int intervalSportTime;
	public static boolean isTotalCounting;
	public static boolean isIntervalCounting;
	private int lastStepsBle;
	private int lastTotalSteps;
	private boolean hasInitLastStep=false;
	private boolean isNoDataComing;
	private int lastDataCameTime;
	private StringBuffer dataString;
	private SportTimeTask sportTimeTask;
	
	//防丢
	private RssiTask rssiTask;
	public static boolean isSearching=false;
	public static boolean isAlarming=false;
	
	//电量
	public static int electricityLeft=0;
	public static int electricityRight=0;
	
	//加热状态
	private HeatTask heatTask;
	public static boolean isHeating=false;
	public static boolean isOverHeating=false;
	public static int timeLeft=0;
	public static int strength=0;
	
	//温度
	public static int temperatureLeft=0;
	public static int temperatureRight=0;
	
	//led状态
	public static int ledMode=1;
	
	//充电状态
	public static boolean isCharging=false;
	
	
	//来电提醒开关
	public static boolean isRemindWhenCalled=false;
	
	//虚拟来电开关
	public static boolean isVirtualCallWhenKnocked=false;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		//设置一个第二天的闹钟，使APP运行期间跨过0点时能重置数据，避免叠加
		Calendar calendar=Calendar.getInstance();
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.add(Calendar.DATE, 1);
		Intent intent=new Intent("time_to_24_hour");
		PendingIntent pendingIntent=PendingIntent.getBroadcast(this, 0, intent, 0);
		AlarmManager am=(AlarmManager) getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000L, pendingIntent);
		
		SharedPreferences preferences=getSharedPreferences("sport_data", Context.MODE_PRIVATE);
		totalSteps=preferences.getInt("totalSteps", 0);
		totalSportTime=preferences.getInt("totalSportTime", 0);
		totalCalorie=preferences.getFloat("totalCalorie", 0f);
		totalDistance=preferences.getInt("totalDistance", 0);
		isTotalCounting=preferences.getBoolean("isTotalCounting", true);
		intervalSteps=preferences.getInt("intervalSteps", 0);
		intervalSportTime=preferences.getInt("intervalSportTime", 0);
		intervalCalorie=preferences.getFloat("intervalCalorie", 0f);
		intervalDistance=preferences.getInt("intervalDistance", 0);
		isIntervalCounting=preferences.getBoolean("isIntervalCounting", false);
		int day=preferences.getInt("day", 0);
		
		//这里判断是否到了第二天，若到了第二天，重置todaySteps和time,并把昨天的步数录入本地数据库
		if(new Date().getDay()!=day){
			totalSteps=0;
			totalSportTime=0;
			totalCalorie=0f;
			totalDistance=0;	
			isIntervalCounting=false;
			intervalSteps=0;
			intervalSportTime=0;
			intervalCalorie=0f;
			intervalDistance=0;
		}
		
		//注册广播
		receiver=new MyBroadcastReceiver();
		IntentFilter intentFilter=new IntentFilter();
		intentFilter.addAction("request_connect");
		intentFilter.addAction("request_disconnect");
		intentFilter.addAction("get_history_steps");
		intentFilter.addAction("start_count");
		intentFilter.addAction("stop_count");
		intentFilter.addAction("time_to_24_hour");
		intentFilter.addAction("start_getting_rssi");
		intentFilter.addAction("stop_getting_rssi");
		intentFilter.addAction("start_link_lost");
		intentFilter.addAction("stop_link_lost");
		intentFilter.addAction("start_vibrating");
		intentFilter.addAction("stop_vibrating");
		intentFilter.addAction("query_electricity");
		intentFilter.addAction("query_temperature");
		intentFilter.addAction("led_change_mode");
		intentFilter.addAction("query_led_mode");
		intentFilter.addAction("query_charging_status");
		intentFilter.addAction("start_heating");
		intentFilter.addAction("stop_heating");
		intentFilter.addAction("query_heating_status");
		intentFilter.addAction("set_alarms");
		intentFilter.addAction("synchronize_time");
		intentFilter.addAction("open_remind_when_call");
		intentFilter.addAction("close_remind_when_call");
		intentFilter.addAction("open_virtual_call_when_knocked");
		intentFilter.addAction("close_virtual_call_when_knocked");
		intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		registerReceiver(receiver, intentFilter);	
		
		handler=new Handler();
		
		timer=new Timer();
		
		//开启设备连接状态检测
		timer.schedule(new ConnectionDetector(), 5000, 10000);
		
		preferences=getSharedPreferences("ble_last_connection", MODE_PRIVATE);
		isLeftLastConnected=preferences.getBoolean("isLeftLastConnected", false);
		isRightLastConnected=preferences.getBoolean("isRightLastConnected", false);
		leftAddress=preferences.getString("leftAddress", "unbound");
		rightAddress=preferences.getString("rightAddress", "unbound");
				
		btManager=(BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
		btAdapter=btManager.getAdapter();
		
	}	

	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		
		//若上次的退出状态为已连接，则再次打开时重新连接上次的设备
		Intent connectIntent=new Intent();	
		connectIntent.setAction("request_connect");
		if(isLeftLastConnected){			
			connectIntent.putExtra("address", leftAddress);
			sendBroadcast(connectIntent);
		}
		if(isRightLastConnected){
			connectIntent.putExtra("address", rightAddress);
			sendBroadcast(connectIntent);
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(timer!=null){
			timer.cancel();
		}

		//退出时保存上一次连接状态和设备地址
		SharedPreferences.Editor editor=getSharedPreferences("ble_last_connection", MODE_PRIVATE).edit();
		editor.putBoolean("isLeftLastConnected", isLeftLastConnected);
		editor.putBoolean("isRightLastConnected", isRightLastConnected);
		editor.putString("leftAddress", leftAddress);
		editor.putString("rightAddress", rightAddress);		
		editor.commit();
		
		//退出时断掉连接并释放资源
		if(btGattLeft!=null){
			btGattLeft.disconnect();
			btGattLeft.close();
		}
		if(btGattRight!=null){
			btGattRight.disconnect();
			btGattRight.close();
		}
		
		//对于全局变量来说，貌似即使停止service，进程不会马上结束，变量仍然保持不变，需默认化
		isLeftConnected=false;
		isRightConnected=false;
		electricityLeft=0;
		electricityRight=0;
		temperatureLeft=0;
		temperatureRight=0;
		timeLeft=0;
		strength=0;
		isHeating=false;
		isCharging=false;
		isRemindWhenCalled=false;
		isVirtualCallWhenKnocked=false;
		ledMode=1;
		
		unregisterReceiver(receiver);
		
		Log.d("zz", "service已停止");
	}

	//接收广播
	private class MyBroadcastReceiver extends BroadcastReceiver{
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();
			//连接请求
			if(action.equals("request_connect")){ 
				final BluetoothDevice device=btAdapter.getRemoteDevice(intent.getStringExtra("address"));
				if(device.getName()!=null){
					if(device.getName().endsWith("L")){
						if(btGattLeft!=null){
							btGattLeft.disconnect();
							btGattLeft.close();
							btGattLeft=null;
						}
						handler.postDelayed(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								btGattLeft=device.connectGatt(BleService.this, true, mBluetoothGattCallback);
							}
						}, 1000);
						
					}else if(device.getName().endsWith("R")){
						if(btGattRight!=null){
							btGattRight.disconnect();
							btGattRight.close();
							btGattRight=null;
						}			
						handler.postDelayed(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								btGattRight=device.connectGatt(BleService.this, true, mBluetoothGattCallback);
							}
						}, 1000);						
						
					}
				}else{
					Toast.makeText(BleService.this, "设备不在范围内", Toast.LENGTH_SHORT).show();
				}
			}
			//断开连接请求
			if(action.equals("request_disconnect")){
				String address=intent.getStringExtra("address");
				if(btGattLeft!=null){
					if(btGattLeft.getDevice().getAddress().equals(address)){
						btGattLeft.disconnect();
						btGattLeft.close();
						return;
					}
				}
				if(btGattRight!=null){
					if(btGattRight.getDevice().getAddress().equals(address)){
						btGattRight.disconnect();
						btGattRight.close();
						return;
					}
				}

			}
			//接收开始计步的广播，往characteristic写值并发送，即往设备写命令
			if(action.equals("start_count")){	
				if(sportTimeTask!=null){
					sportTimeTask.cancel();
					sportTimeTask=null;
				}
				sportTimeTask=new SportTimeTask();
				timer.schedule(sportTimeTask, 0, 1000);
				byte[] bytes=new byte[4];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x51;
				bytes[2]=(byte)0x01;
				bytes[3]=(byte)0x00;
				writeCommand(bytes);				
			}
			//暂停计步，不再往todayStep上累加
			if(action.equals("stop_count")){				
				byte[] bytes=new byte[4];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x51;
				bytes[2]=(byte)0x00;
				bytes[3]=(byte)0x00;
				writeCommand(bytes);
			}
			//到第二天0点重置
			if(action.equals("time_to_24_hour")){
				totalSteps=0;
				totalSportTime=0;
				totalCalorie=0f;
				totalDistance=0;	
				intervalSteps=0;
				intervalSportTime=0;
				intervalCalorie=0f;
				intervalDistance=0;
				sendBroadcast(new Intent("update_sportTime_calorie_distance"));
				sendBroadcast(new Intent("update_steps"));
			}
			//获取设备历史步数
			if(action.equals("get_history_steps")){
				//若之前的同步命令还没执行完，则不会执行下一个同步命令
//				if(isSynchronizingHistory){
//					return;
//				}
				if(dataString.length()!=0){
					dataString.delete(0, dataString.length()-1);
				}
//				isSynchronizingHistory=true;
				byte[] bytes=new byte[4];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x52;
				bytes[2]=(byte)0x01;
				bytes[3]=(byte)0x00;
				writeCommand(bytes);
			}
			//开始手机防丢断掉连接震动模式
			if(action.equals("start_link_lost")){
				byte[] bytes=new byte[3];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x92;
				bytes[2]=(byte)0x00;				
				writeCommand(bytes);
			}
			//停止手机防丢断掉连接震动模式
			if(action.equals("stop_link_lost")){
				byte[] bytes=new byte[3];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x92;
				bytes[2]=(byte)0x01;				
				writeCommand(bytes);
			}
			//开始监听rssi
			if(action.equals("start_getting_rssi")){				
				isSearching=true;
				Log.d("zz","监听rssi");			
				if(rssiTask!=null){
					rssiTask.cancel();
					rssiTask=null;
				}
				rssiTask=new RssiTask();
				timer.schedule(rssiTask, 0, 3000);
			}
			//停止监听rssi
			if(action.equals("stop_getting_rssi")){
				isSearching=false;
				sendBroadcast(new Intent().setAction("stop_vibrating"));
				if(rssiTask!=null){
					rssiTask.cancel();
					rssiTask=null;
				}
			}			
			//开始震动
			if(action.equals("start_vibrating")){
				byte[] bytes=new byte[3];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x90;
				bytes[2]=(byte)0x00;				
				writeCommand(bytes);
			}
			//停止震动
			if(action.equals("stop_vibrating")){				
				byte[] bytes=new byte[3];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x90;
				bytes[2]=(byte)0x02;
				writeCommand(bytes);
			}
			//电量查询，因为连接上后立即发送不会响应，所以需延时300毫秒发送
			if(action.equals("query_electricity")){
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {Log.d("zz", "电量查询");
						// TODO Auto-generated method stub
						byte[] bytes=new byte[4];
						bytes[0]=(byte)0x00;
						bytes[1]=(byte)0x13;
						bytes[2]=(byte)0x00;
						bytes[3]=(byte)0x00;
						writeCommand(bytes);
					}
				}, 800);
				
			}
			//温度查询
			if(action.equals("query_temperature")){
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {Log.d("zz", "温度查询");
						// TODO Auto-generated method stub
						byte[] bytes=new byte[4];
						bytes[0]=(byte)0x00;
						bytes[1]=(byte)0x86;
						bytes[2]=(byte)0x00;
						bytes[3]=(byte)0x00;
						writeCommand(bytes);
					}
				}, 800);
				
			}
			//改变led工作模式
			if(action.equals("led_change_mode")){
				
				if(ledMode==0){
					byte[] bytes=new byte[4];
					bytes[0]=(byte)0x00;
					bytes[1]=(byte)0x62;
					bytes[2]=(byte)0x01;
					bytes[3]=(byte)0xc0;
					writeCommand(bytes);
				}else if(ledMode==1){//常亮
					byte[] bytes=new byte[7];
					bytes[0]=(byte)0x00;
					bytes[1]=(byte)0x62;
					bytes[2]=(byte)0xc0;
					bytes[3]=(byte)0x03;
					bytes[4]=(byte)0x1e;
					bytes[5]=(byte)0x50;
					bytes[6]=(byte)0x01;
					writeCommand(bytes);
				}else if(ledMode==2){//呼吸
					byte[] bytes=new byte[7];
					bytes[0]=(byte)0x00;
					bytes[1]=(byte)0x62;
					bytes[2]=(byte)0xc0;
					bytes[3]=(byte)0x03;
					bytes[4]=(byte)0x1e;
					bytes[5]=(byte)0x50;
					bytes[6]=(byte)0x02;
					writeCommand(bytes);
				}else if(ledMode==3){//爆闪
					byte[] bytes=new byte[7];
					bytes[0]=(byte)0x00;
					bytes[1]=(byte)0x62;
					bytes[2]=(byte)0xc0;
					bytes[3]=(byte)0x03;
					bytes[4]=(byte)0x1e;
					bytes[5]=(byte)0x50;
					bytes[6]=(byte)0x04;
					writeCommand(bytes);
				}
				ledMode++;				
				if(ledMode>3)
					ledMode=0;
			}
			//查询led工作模式
			if(action.equals("query_led_mode")){
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {Log.d("zz", "led模式查询");
						// TODO Auto-generated method stub
						byte[] bytes=new byte[4];
						bytes[0]=(byte)0x00;
						bytes[1]=(byte)0x61;
						bytes[2]=(byte)0x00;
						bytes[3]=(byte)0x00;				
						writeCommand(bytes);
					}
				}, 1000);				
			}
			//开始加热
			if(action.equals("start_heating")){  
				int time=intent.getIntExtra("heat_time", 0);
				int strength=intent.getIntExtra("heat_strength", 0);
				byte[] bytes=new byte[6];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x82;
				bytes[2]=(byte)0x00;
				bytes[3]=(byte)0x02;
				bytes[4]=(byte)time;
				bytes[5]=(byte)strength;
				writeCommand(bytes);
			}
			//停止加热
			if(action.equals("stop_heating")){
				byte[] bytes=new byte[4];
				bytes[0]=(byte)0x00;
				bytes[1]=(byte)0x82;
				bytes[2]=(byte)0x01;
				bytes[3]=(byte)0x00;
				writeCommand(bytes);
			}
			//查询加热状态
			if(action.equals("query_heating_status")){
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						byte[] bytes=new byte[4];
						bytes[0]=(byte)0x00;
						bytes[1]=(byte)0x81;
						bytes[2]=(byte)0x00;
						bytes[3]=(byte)0x00;
						writeCommand(bytes);
					}
				}, 1000);
				
			}
			//查询充电状态
			if(action.equals("query_charging_status")){
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {Log.d("zz", "充电状态查询");
						// TODO Auto-generated method stub
						byte[] bytes=new byte[4];
						bytes[0]=(byte)0x00;
						bytes[1]=(byte)0x14;
						bytes[2]=(byte)0x00;
						bytes[3]=(byte)0x00;
						writeCommand(bytes);
					}
				}, 900);
			}
			//设置闹钟，需传10条，真麻烦
			if(action.equals("set_alarms")){		
				@SuppressWarnings("unchecked")
				ArrayList<HashMap<String, Object>> alarms=(ArrayList<HashMap<String, Object>>)intent.getSerializableExtra("alarms_data");
				//遍历闹钟，除去关闭状态的闹钟
				Iterator<HashMap<String, Object>> iterator=alarms.iterator();
				while (iterator.hasNext()) {
					HashMap<String, Object> clock = (HashMap<String, Object>) iterator.next();
					if(!(Boolean)clock.get("isOpen")){
						iterator.remove();
					}
				}
				//设置开启的闹钟
				for(int i=0; i<alarms.size(); i++){
					boolean[] day=(boolean[]) alarms.get(i).get("day");
					byte dayBit=0x00;//初始星期位，8个bit为“0”表示周一到周日都为false状态
					byte bit=0x01;//用于位运算，通过对day的遍历，左移后作 与运算 达到初始化dayBit的效果;
					for(int j=0; j<day.length; j++){
						if(day[j]){
							dayBit=(byte)(dayBit|(bit<<j));
						}
					}
					byte[] bytes=new byte[9];
					bytes[0]=(byte)0x00;
					bytes[1]=(byte)0x98;
					bytes[2]=(byte)0x00;
					bytes[3]=(byte)0x05;
					bytes[4]=(byte)i;   //闹钟编号
					bytes[5]=(byte)(Integer.parseInt(alarms.get(i).get("hour").toString()));//小时
					bytes[6]=(byte)(Integer.parseInt(alarms.get(i).get("minute").toString())+1);//分钟
					bytes[7]=(byte)0x1e;//震动30秒
					bytes[8]=(byte)dayBit;//重复的星期几
					writeCommand(bytes);
				}
				//因为要发10条，但list里并没有10个item，所以补上数据位置0的闹钟
				for(int i=alarms.size(); i<10; i++){
					byte[] bytes=new byte[9];
					bytes[0]=(byte)0x00;
					bytes[1]=(byte)0x98;
					bytes[2]=(byte)0x00;
					bytes[3]=(byte)0x05;
					bytes[4]=(byte)i;   //闹钟编号
					bytes[5]=(byte)0x00;//小时
					bytes[6]=(byte)0x00;//分钟
					bytes[7]=(byte)0x00;//无震动
					bytes[8]=(byte)0x00;//无周期
					writeCommand(bytes);
				}
			}
			//将手机时间同步到设备上，以便设置闹钟
			if(action.equals("synchronize_time")){
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {Log.d("zz", "同步时间");
						// TODO Auto-generated method stub
						SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
						long timeBefore=0;
						try {
							timeBefore=format.parse("2000-1-1").getTime();	//1970-1-1到2000-1-1的毫秒数				
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						int time=(int)((new Date().getTime()-timeBefore)/1000); //2000-1-1到现在的秒数
						byte[] bytes=new byte[8];
						bytes[0]=(byte)0x00;
						bytes[1]=(byte)0x21;
						bytes[2]=(byte)0x00;
						bytes[3]=(byte)0x04;
						bytes[4]=(byte)((time>>24)&0xff);
						bytes[5]=(byte)((time>>16)&0xff);
						bytes[6]=(byte)((time>>8)&0xff);
						bytes[7]=(byte)(time&0xff);
						writeCommand(bytes);
					}
				}, 600);
				
			}
			//开启来电提醒
			if(action.equals("open_remind_when_call")){
				isRemindWhenCalled=true;
			}
			//关闭来电提醒
			if(action.equals("close_remind_when_call")){
				isRemindWhenCalled=false;
			}
			//监听来电
			if(action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)){
				TelephonyManager telephonyManager=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				int state=telephonyManager.getCallState();
				if(state==TelephonyManager.CALL_STATE_RINGING){
					if(isRemindWhenCalled){
						byte[] bytes=new byte[4];
						bytes[0]=(byte)0x00;
						bytes[1]=(byte)0x97;
						bytes[2]=(byte)0x00;
						bytes[3]=(byte)0x14;
						writeCommand(bytes);
					}
				}				
			}
			//开启跺脚虚拟来电
			if(action.equals("open_virtual_call_when_knocked")){Log.d("zz","开启虚拟");
				if(isLeftConnected || isRightConnected){
					isVirtualCallWhenKnocked=true;
				}else{
					Toast.makeText(BleService.this, "没有连接设备", Toast.LENGTH_SHORT).show();
				}
			}
			//关闭跺脚虚拟来电
			if(action.equals("close_virtual_call_when_knocked")){Log.d("zz","关闭虚拟");
				if(isLeftConnected || isRightConnected){
					isVirtualCallWhenKnocked=false;
				}else{
					Toast.makeText(BleService.this, "没有连接设备", Toast.LENGTH_SHORT).show();
				}
			}
		}		
	}	
	
	//往设备写命令
	private void writeCommand(byte[] bytes){
		Log.d("zz", "发送命令=========>>"+bytes2HexString(bytes));
		if(btGattLeft!=null && characteristicLeft!=null){							
			characteristicLeft.setValue(bytes);					
			btGattLeft.writeCharacteristic(characteristicLeft);
		}
		if(btGattRight!=null && characteristicRight!=null){
			characteristicRight.setValue(bytes);					
			btGattRight.writeCharacteristic(characteristicRight);
		}
		if(!isLeftConnected && !isRightConnected){
			Toast.makeText(BleService.this, "没有连接设备", Toast.LENGTH_SHORT).show();
		}
	}
	
	//连接ble的回调接口
	private BluetoothGattCallback mBluetoothGattCallback=new BluetoothGattCallback() {

		//连接或断开设备时回调
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			// TODO Auto-generated method stub
			if(newState==BluetoothProfile.STATE_CONNECTED){
				Log.d("zz", "已连上GATT");
				if(gatt.getDevice().getName().endsWith("L")){
					isLeftLastConnected=true;
					isLeftConnected=true;
					leftAddress=gatt.getDevice().getAddress();
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(BleService.this, "已连上左脚设备", Toast.LENGTH_SHORT).show();
						}
					});
				
				}else{
					isRightLastConnected=true;
					isRightConnected=true;
					rightAddress=gatt.getDevice().getAddress();	
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(BleService.this, "已连上右脚设备", Toast.LENGTH_SHORT).show();
						}
					});
				}
				SharedPreferences.Editor editor=getSharedPreferences("ble_last_connection", Context.MODE_PRIVATE).edit();
				editor.putBoolean("isLeftLastConnected", isLeftLastConnected);
				editor.putBoolean("isRightLastConnected", isRightLastConnected);
				editor.putString("leftAddress", leftAddress);
				editor.putString("rightAddress", rightAddress);
				editor.commit();
				
				//发送广播，告知ConnectionActivity UI已连上
				Intent intent=new Intent();
				intent.setAction("connection_status_changed");
				intent.putExtra("address", gatt.getDevice().getAddress());
				intent.putExtra("name", gatt.getDevice().getName());
				sendBroadcast(intent); 
				//搜索服务
				gatt.discoverServices();
				
			}else if(newState==BluetoothProfile.STATE_DISCONNECTED){
				Log.d("zz", "已断掉GATT");				
				if(gatt.getDevice().getName().endsWith("L")){
					isLeftLastConnected=false;
					leftAddress="unbound";
					isLeftConnected=false;
					electricityLeft=0;
					temperatureLeft=0;					
				}else{
					isRightLastConnected=false;
					rightAddress="unbound";
					isRightConnected=false;
					electricityRight=0;
					temperatureRight=0;	
					hasInitLastStep=false;
				}	
				
				SharedPreferences.Editor editor=getSharedPreferences("ble_last_connection", Context.MODE_PRIVATE).edit();
				editor.putBoolean("isLeftLastConnected", isLeftLastConnected);
				editor.putBoolean("isRightLastConnected", isRightLastConnected);
				editor.putString("leftAddress", leftAddress);
				editor.putString("rightAddress", rightAddress);
				editor.commit();
				
				if(rssiTask!=null){
					rssiTask.cancel();
					isSearching=false;
				}
				//发送广播，告知ConnectionActivity UI已断开
				Intent intent=new Intent();
				intent.setAction("connection_status_changed");
				intent.putExtra("address", gatt.getDevice().getAddress()); 
				sendBroadcast(intent);
			}
		}

		//发现服务时回调
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			if(status==BluetoothGatt.GATT_SUCCESS){
				if(gatt.getDevice().getName().endsWith("L")){
					btGattServiceLeft=gatt.getService(serviceUUID);
					if(btGattServiceLeft!=null){
						characteristicLeft=btGattServiceLeft.getCharacteristic(characteristicWriteUUID);
						gatt.setCharacteristicNotification(characteristicLeft, true);
												
						BluetoothGattCharacteristic characteristicRead=btGattServiceLeft.getCharacteristic(characteristicReadUUID);		
						gatt.setCharacteristicNotification(characteristicRead, true);
						BluetoothGattDescriptor descriptor=characteristicRead.getDescriptor(descriptorUUID);
						descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
						gatt.writeDescriptor(descriptor);
						
					}
				}else{
					btGattServiceRight=gatt.getService(serviceUUID); 
					if(btGattServiceRight!=null){
						characteristicRight=btGattServiceRight.getCharacteristic(characteristicWriteUUID);
						
						//必须获取读模式的characteristic，并获取其descriptor,设置descriptor,
						//并往gatt里写入才能回调characteristic函数
						BluetoothGattCharacteristic characteristicRead=btGattServiceRight.getCharacteristic(characteristicReadUUID);		
						gatt.setCharacteristicNotification(characteristicRead, true);
						BluetoothGattDescriptor descriptor=characteristicRead.getDescriptor(descriptorUUID);
						descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
						gatt.writeDescriptor(descriptor);						
					}
					
					//到此基本上已连上设备，立即发送需要初始化数据的命令，但需要延时300到500ms					
					sendBroadcast(new Intent().setAction("query_electricity"));
					sendBroadcast(new Intent().setAction("query_temperature"));
					sendBroadcast(new Intent().setAction("query_heating_status"));
					sendBroadcast(new Intent().setAction("query_charging_status"));
					sendBroadcast(new Intent().setAction("query_led_mode"));
					sendBroadcast(new Intent().setAction("synchronize_time"));	
					
					if(isTotalCounting){
						handler.postDelayed(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								sendBroadcast(new Intent().setAction("start_count"));	
							}
						}, 500);
					}
					
				}
				
			}
		}

		//手机或设备往characteristic写value时回调，多用于接收设备data
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			byte[] bytes=characteristic.getValue();
			
			if(bytes==null) return;
			
			String response=bytes2HexString(bytes);			
//			Log.d("zz", "返回字符串<<========="+response);
			
			//计步触发返回命令
			if(response.substring(0, 8).equals("b0510204")){
				//只初始化一次
				if(!hasInitLastStep){
					SharedPreferences preferences=getSharedPreferences("sport_data", Context.MODE_PRIVATE);
					int stepLength=preferences.getInt("stepLength", 65);
					int weight=preferences.getInt("weight", 65);					
					
					int stepsTemp=Integer.valueOf(response.substring(8), 16);
					//若硬件上的步数大于APP，则表示硬件没断电，取其新增步数；反之，硬件断电，把期间的数据全加
					if(stepsTemp>totalSteps){
						totalDistance+=stepLength*(stepsTemp-totalSteps)/100;												
						totalCalorie+=weight*((float)stepLength*(stepsTemp-totalSteps)/100f)/1000f*1.036f;						
						Log.d("zz", "distance:"+totalDistance+"  calorie:"+totalCalorie);
						if(isIntervalCounting){
							intervalDistance+=stepLength*(stepsTemp-totalSteps)/100;							
							intervalCalorie+=weight*(stepLength*(stepsTemp-totalSteps)/100)/1000f*1.036f;
							intervalSteps+=stepsTemp-totalSteps;
						}
						totalSteps+=stepsTemp-totalSteps;
						SharedPreferences.Editor editor=preferences.edit();
						editor.putInt("totalSteps", totalSteps);
						editor.putFloat("totalCalorie", totalCalorie);
						editor.putInt("totalSportTime", totalSportTime);
						editor.putInt("totalDistance", totalDistance);
						editor.putFloat("intervalCalorie", intervalCalorie);
						editor.putInt("intervalSportTime", intervalSportTime);
						editor.putInt("intervalDistance", intervalDistance);
						editor.putInt("day", new Date().getDay());
						editor.commit();
					}					
					//记录硬件的上次步数，用于下次相减后得到叠加数据
					lastStepsBle=stepsTemp;
					lastTotalSteps=totalSteps;
					hasInitLastStep=true;
					sendBroadcast(new Intent("update_sportTime_calorie_distance"));
					sendBroadcast(new Intent("update_steps"));
					return;
				}
				
				lastDataCameTime=totalSportTime;
				//使继续计时
				isNoDataComing=false;
				
				//从硬件获取步数，采用叠加的方式加到steps上
				int currentStepsBle=Integer.valueOf(response.substring(8), 16);
				if(currentStepsBle-lastStepsBle>=0){
					totalSteps+=currentStepsBle-lastStepsBle;
					if(isIntervalCounting){
						intervalSteps+=currentStepsBle-lastStepsBle;
					}
					//记录硬件的上次步数，用于下次相减后得到叠加数据
					lastStepsBle=currentStepsBle;
					sendBroadcast(new Intent("update_steps"));
				}
			
			}
			//获取计步历史数据
			if(response.substring(0, 4).equals("b052")){
				dataString.append(response.substring(8));									
			}
			//获取计步历史数据结束
			if(response.substring(0, 8).equals("b0530200")){
				int[] data=new int[dataString.length()/48];
				for(int i=0; i<dataString.length(); i+=48){
					String oneDayDataString=dataString.substring(i, i+48);
					for(int j=0; j<oneDayDataString.length(); j+=4){
						data[i/48]+=Integer.valueOf(oneDayDataString.substring(j, j+4), 16);
					}
				}
				Intent intent=new Intent();
				intent.putExtra("history_steps", data);
				intent.setAction("update_history_steps");
				sendBroadcast(intent);
				//结束本次同步
//				isSynchronizingHistory=false;
			}
			//电量查询或改变返回命令
			if(response.substring(0, 8).equals("b0130101")){
				if(gatt.getDevice().getName().endsWith("L")){
					electricityLeft=Integer.parseInt(response.substring(8, 10), 16);
				}else{
					electricityRight=Integer.parseInt(response.substring(8, 10), 16);
				}
				sendBroadcast(new Intent().setAction("update_electricity"));
			}
			//温度查询
			if(response.substring(0, 6).equals("b08602")){
				if(gatt.getDevice().getName().endsWith("L")){
					temperatureLeft=Integer.parseInt(response.substring(6, 10), 16)/10;
				}else{
					temperatureRight=Integer.parseInt(response.substring(6, 10), 16)/10;
				}
				if(temperatureLeft>40 || temperatureRight>40){
					isOverHeating=true;
				}else{
					isOverHeating=false;
				}
				sendBroadcast(new Intent().setAction("update_alarm_status"));
				sendBroadcast(new Intent().setAction("update_temperature"));
			}			
			//加热状态
			if(response.substring(0, 8).equals("b0810008")){ 
				if(response.substring(8, 10).equals("00")){
					isHeating=false;
					timeLeft=0;
					strength=0;
				}else{
					isHeating=true;
					timeLeft=Integer.parseInt(response.substring(10, 12), 16);
					strength=Integer.parseInt(response.substring(12, 14), 16);
				}
				sendBroadcast(new Intent().setAction("update_heating_status"));
				
				
				if(!isHeating){
					heatTask.cancel();
				}
				
			}		
			//加热开始确定
			if(response.substring(0, 8).equals("b0820000")){
				isHeating=true;
				if(heatTask!=null){
					heatTask.cancel();
					heatTask=null;
				}
				heatTask=new HeatTask();
				timer.schedule(heatTask, 0, 60000);
			}
			//加热停止确定
			if(response.substring(0, 8).equals("b0820100")){
				isHeating=false;
				sendBroadcast(new Intent().setAction("query_heating_status"));
			}
			//充电状态：正在充
			if(response.substring(0, 8).equals("b0140101")){
				if(response.substring(8, 10).equals("00")){
					isCharging=false;
				}else{
					isCharging=true;
				}
				sendBroadcast(new Intent().setAction("update_charging_status"));
			}
			//设备接收完10条闹钟设置命令后返回
			if(response.equals("b0980400")){
				Log.d("zz", "十条传完");
				//其实可以根据这个确认命令设置闹钟的开关同步，免得异步操作导致往设备写命令时错乱，暂未应用
			}
			//跺脚虚拟来电命令
			if(response.equals("b09a0000")){
				if(isVirtualCallWhenKnocked){
					Intent intent=new Intent(BleService.this, CallingActivity.class);
					intent.putExtra("type", "knock");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
			}
			//led工作模式
			if(response.substring(0, 4).equals("b061")){
				int mode=Integer.valueOf(response.substring(16, 18), 16);
				if(mode==0||mode==1||mode==2||mode==4){
					ledMode=mode;
				}else{
					ledMode=1;
				}
			}
		}

		//读取Rssi时回调，可以获取连接设备的Rssi
		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			// TODO Auto-generated method stub
			Log.d("zz", "rssi:"+rssi);
			
			if(rssi<-90){
				if(!isAlarming){					
					Intent intent=new Intent(BleService.this, SearchPhoneAlarmActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(intent);
					isAlarming=true;
				}
			}
			
		}			
	};
	
	//监听rssi线程
	public class RssiTask extends TimerTask{

		@Override
		public void run() {
			
			// TODO Auto-generated method stub
			if(btGattLeft!=null){
				btGattLeft.readRemoteRssi();
				
			}
			if(btGattRight!=null){
				btGattRight.readRemoteRssi();
			}
		}
		
	}
	
	//监听加热时间线程,每60秒更新一次
	public class HeatTask extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			sendBroadcast(new Intent().setAction("query_heating_status"));
		}
		
	}
	
	//byte转16进制字符串
	public String bytes2HexString(byte[] bytes){
		String hexString="";
		String temp="";
		for(int i=0; i<bytes.length; i++){
			temp=(Integer.toHexString(bytes[i] & 0xFF));
			if(temp.length() == 1){
				hexString=hexString+"0"+temp;
			}else{
				hexString=hexString+temp;
			}
		}	
		return hexString.toLowerCase();
	}
	
	//计算运动时间
	public class SportTimeTask extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub			
			if(!isNoDataComing && isRightConnected){
				totalSportTime++;	
				if(isIntervalCounting){
					intervalSportTime++;
				}
				//每5秒处理一次数据，并保存，以免硬件断电重置数据
				if(totalSportTime%5==0){
					SharedPreferences preferences=getSharedPreferences("sport_data", Context.MODE_PRIVATE);
					int stepLength=preferences.getInt("stepLength", 65);
					int weight=preferences.getInt("weight", 65);
					
					//每5秒的速度
					int alphaSteps=totalSteps-lastTotalSteps;
					if(alphaSteps<0) alphaSteps=0;
					float speed=(float)(alphaSteps*stepLength*0.01/5);
					//跑400米所需时间（分钟）
					float timeNeed=400/speed/60;
					//卡路里=体重(kg)*时间(h)*K(指数K=30/(跑400米所需的分钟数))
					totalCalorie+=(float)weight*(5f/3600)*(30/timeNeed);
					
					totalDistance+=stepLength*alphaSteps/100;
					
					if(isIntervalCounting){
						intervalCalorie+=(float)weight*(5f/3600)*(30/timeNeed);
						intervalDistance+=stepLength*alphaSteps/100;
					}
					
					lastTotalSteps=totalSteps;
					
					SharedPreferences.Editor editor=preferences.edit();
					editor.putInt("totalSteps", totalSteps);
					editor.putFloat("totalCalorie", totalCalorie);
					editor.putInt("totalSportTime", totalSportTime);
					editor.putInt("totalDistance", totalDistance);
					editor.putInt("intervalSteps", intervalSteps);
					editor.putFloat("intervalCalorie", intervalCalorie);
					editor.putInt("intervalSportTime", intervalSportTime);
					editor.putInt("intervalDistance", intervalDistance);
					editor.putInt("day", new Date().getDay());
					editor.commit();
					
				}
				//若上次数据到现在超过5秒则不再计时
				if(totalSportTime-lastDataCameTime>4){
					isNoDataComing=true;
				}
				sendBroadcast(new Intent("update_sportTime_calorie_distance"));
			}
		}
		
	}
	
	//用于检测蓝牙的实时连接状态，防止异常断开又没有触发onConnectionStateChange回调造成UI没有及时更新的情况
	public class ConnectionDetector extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			BluetoothManager btManager=(BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
			if(btGattLeft!=null){
				BluetoothDevice device=btGattLeft.getDevice();
				if(device!=null){
					int state=btManager.getConnectionState(device, BluetoothGatt.GATT);
					if(state==BluetoothGatt.STATE_DISCONNECTED){
						isLeftConnected=false;
						isLeftLastConnected=false;
						leftAddress="unbound";
						electricityLeft=0;
						temperatureLeft=0;
						sendBroadcast(new Intent("connection_status_changed"));
						SharedPreferences.Editor editor=getSharedPreferences("ble_last_connection", Context.MODE_PRIVATE).edit();
						editor.putBoolean("isLeftLastConnected", isLeftLastConnected);
						editor.putString("leftAddress", leftAddress);
						editor.commit();
					}
				}
			}
			if(btGattRight!=null){								
				BluetoothDevice device=btGattRight.getDevice();
				if(device!=null){
					int state=btManager.getConnectionState(device, BluetoothGatt.GATT);
					if(state==BluetoothGatt.STATE_DISCONNECTED){
						isRightConnected=false;
						isRightLastConnected=false;
						rightAddress="unbound";
						electricityRight=0;
						temperatureRight=0;
						sendBroadcast(new Intent("connection_status_changed"));
						SharedPreferences.Editor editor=getSharedPreferences("ble_last_connection", Context.MODE_PRIVATE).edit();
						editor.putBoolean("isRightLastConnected", isRightLastConnected);
						editor.putString("rightAddress", rightAddress);
						editor.commit();
					}
				}
			}
			
		}
		
	}
						
}
