
package com.shoes.activity; 

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoes.R;
import com.shoes.customview.GifView;
import com.shoes.customview.LoadingDialog;
import com.shoes.customview.RoundCornerListView;
import com.shoes.service.BleService;
import com.shoes.util.ClassParseUtil;


public class ConnectionActivity extends Activity {

	private ImageButton btnBack;
	private RoundCornerListView lvDevicesNew, lvDevicesOld;
	private GifView gifView;
	private ImageView btnScan;
	private ArrayList<HashMap<String, Object>> devicesNewInfo, devicesOldInfo ;
	private BluetoothManager btManager;
	private BluetoothAdapter btAdapter;
	private NewDevicesBaseAdapter newDevicesBaseAdapter;
	private OldDevicesBaseAdapter oldDevicesBaseAdapter;
	private Handler handler=new Handler();
	private MyBroadcastReceiver receiver;
	private SharedPreferences preference;
	private SharedPreferences.Editor editor;
	private LoadingDialog loadingDialog;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_connection);		
		
		preference=getSharedPreferences("connection", MODE_PRIVATE);
		editor=preference.edit();
		
		receiver=new MyBroadcastReceiver();
		IntentFilter intentFilter=new IntentFilter();
		intentFilter.addAction("connection_status_changed");
		registerReceiver(receiver, intentFilter);
				
		
		btnBack=(ImageButton)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		loadingDialog=new LoadingDialog(this, "正在连接……");
					
		//初始化gif		
		gifView=(GifView)findViewById(R.id.ble_scan_gif);
		gifView.setVisibility(View.GONE);
		
		btnScan=(ImageView)findViewById(R.id.ble_scan);
		btnScan.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("ShowToast")
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//扫描5秒停止扫描，并把扫描结果显示
				
				handler.postDelayed(new Runnable() {					
					@Override
					public void run() {
						// TODO Auto-generated method stub						
						btAdapter.stopLeScan(mLeScanCallback);
						gifView.setVisibility(View.GONE);		
						gifView.setPaused(true);
						btnScan.setVisibility(View.VISIBLE);						
						Log.d("zz", "结束扫描");
						newDevicesBaseAdapter.notifyDataSetChanged();					
					}
				}, 3000);
				btAdapter.startLeScan(mLeScanCallback);
				gifView.setVisibility(View.VISIBLE);
				gifView.setPaused(false);
				btnScan.setVisibility(View.GONE);
				Toast.makeText(ConnectionActivity.this, "正在搜索，请稍等", 1000).show();
			}
		});
		
		devicesNewInfo=new ArrayList<HashMap<String,Object>>();
		
		//从文件中取出已绑定的设备
		devicesOldInfo=new ArrayList<HashMap<String,Object>>();
		
		if(preference.getString("old_devices", null)!=null){
			try {
				devicesOldInfo=(ArrayList<HashMap<String, Object>>) ClassParseUtil.string2List(preference.getString("old_devices", null));
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
		
		//恢复连接状态
		for(HashMap<String, Object> deviceOld : devicesOldInfo){
			String address=(String) deviceOld.get("address");
			String newName=(String) deviceOld.get("newName");
			if(newName!=null){
			
			}
			if(address.equals(BleService.leftAddress)){
				if(BleService.isLeftConnected){
					deviceOld.put("status", true);
					continue;
				}
			}
			if(address.equals(BleService.rightAddress)){
				if(BleService.isRightConnected){
					deviceOld.put("status", true);
					continue;
				}
			}
			deviceOld.put("status", false);
		}
		
		lvDevicesNew=(RoundCornerListView)findViewById(R.id.connection_devices_list_new);		
		newDevicesBaseAdapter=new NewDevicesBaseAdapter(ConnectionActivity.this, devicesNewInfo, R.layout.devices_new_list_item,
				new String[]{"name", "status"}, new int[]{R.id.devices_new_list_item_name, R.id.devices_new_list_item_status});
		lvDevicesNew.setAdapter(newDevicesBaseAdapter);
		lvDevicesNew.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				if((Boolean) devicesNewInfo.get(position).get("status")){
					//弹出窗口是否要断掉
					new AlertDialog.Builder(ConnectionActivity.this)
					.setMessage("是否断掉原来的连接")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							
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
						
						
				}else{
					loadingDialog.show();
					handler.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(loadingDialog.isShowing()){
								loadingDialog.dismiss();
								Toast.makeText(ConnectionActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
							}
						}
					}, 10000);
					String address=(String) devicesNewInfo.get(position).get("address");
					Intent intent=new Intent();
					intent.setAction("request_connect");
					intent.putExtra("address", address);					
					sendBroadcast(intent);
				}
			}
		});
		
		lvDevicesOld=(RoundCornerListView)findViewById(R.id.connection_devices_list_old);
		oldDevicesBaseAdapter=new OldDevicesBaseAdapter(ConnectionActivity.this, devicesOldInfo, R.layout.devices_new_list_item,
				new String[]{"name", "status"}, new int[]{R.id.devices_new_list_item_name, R.id.devices_new_list_item_status});		
		lvDevicesOld.setAdapter(oldDevicesBaseAdapter);
		lvDevicesOld.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int position,
					long arg3) {
				// TODO Auto-generated method stub
				if((Boolean) devicesOldInfo.get(position).get("status")){
					//弹出窗口是否要断掉
					new AlertDialog.Builder(ConnectionActivity.this)
					.setMessage("是否断掉原来的连接")
					.setPositiveButton("确定", new DialogInterface.OnClickListener(){
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							devicesOldInfo.get(position).put("status", false);
							oldDevicesBaseAdapter.notifyDataSetChanged();
							try {
								editor.putString("old_devices", ClassParseUtil.list2String(devicesOldInfo)).commit();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							Intent intent=new Intent();
							intent.setAction("request_disconnect");
							intent.putExtra("address",(String)devicesOldInfo.get(position).get("address"));
							sendBroadcast(intent);							
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
						
						
				}else{
					if(BleService.isLeftConnected && devicesOldInfo.get(position).get("name").toString().endsWith("L")){
						new AlertDialog.Builder(ConnectionActivity.this)
						.setMessage("是否断掉原来左鞋连接")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								loadingDialog.show();
								handler.postDelayed(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										if(loadingDialog.isShowing()){
											loadingDialog.dismiss();
											Toast.makeText(ConnectionActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
										}
									}
								}, 10000);
								String address=(String) devicesOldInfo.get(position).get("address");
								Intent intent=new Intent();
								intent.setAction("request_connect");
								intent.putExtra("address", address);								
								sendBroadcast(intent);
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
					}else if(BleService.isRightConnected && devicesOldInfo.get(position).get("name").toString().endsWith("R")){
						new AlertDialog.Builder(ConnectionActivity.this)
						.setMessage("是否断掉原来右鞋连接")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								loadingDialog.show();
								handler.postDelayed(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										if(loadingDialog.isShowing()){
											loadingDialog.dismiss();
											Toast.makeText(ConnectionActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
										}
									}
								}, 10000);
								String address=(String) devicesOldInfo.get(position).get("address");
								Intent intent=new Intent();
								intent.setAction("request_connect");
								intent.putExtra("address", address);
								sendBroadcast(intent);
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
					}else{
						loadingDialog.show();
						handler.postDelayed(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								if(loadingDialog.isShowing()){
									loadingDialog.dismiss();
									Toast.makeText(ConnectionActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
								}
							}
						}, 10000);
						String address=(String) devicesOldInfo.get(position).get("address");
						Intent intent=new Intent();
						intent.setAction("request_connect");
						intent.putExtra("address", address);
						sendBroadcast(intent);
					}
				}
			}
		});
		
		lvDevicesOld.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int position, long arg3) {
				// TODO Auto-generated method stub
				Vibrator vibrator=(Vibrator)getSystemService(Service.VIBRATOR_SERVICE);
				vibrator.vibrate(500);
				LinearLayout dialog=(LinearLayout) getLayoutInflater().inflate(R.layout.dialog_modify_device_name, null);
				
				final EditText newName=(EditText)dialog.findViewById(R.id.dialog_new_name_input);
				final AlertDialog alertDialog=new AlertDialog.Builder(ConnectionActivity.this)
				.setView(dialog)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						if(!newName.getText().toString().trim().equals("")){
							devicesOldInfo.get(position).put("newName", newName.getText().toString());
							try {
								editor.putString("old_devices", ClassParseUtil.list2String(devicesOldInfo)).commit();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							oldDevicesBaseAdapter.notifyDataSetChanged();
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				})
				.create();	
				alertDialog.show();
				
				TextView btnDelete=(TextView)dialog.findViewById(R.id.dialog_delete_history_device);
				btnDelete.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if((Boolean) devicesOldInfo.get(position).get("status")){
							Intent intent=new Intent();
							intent.setAction("request_disconnect");
							intent.putExtra("address",(String)devicesOldInfo.get(position).get("address"));
							sendBroadcast(intent);							
						}
						alertDialog.dismiss();
						devicesOldInfo.remove(position);
						try {
							editor.putString("old_devices", ClassParseUtil.list2String(devicesOldInfo)).commit();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						oldDevicesBaseAdapter.notifyDataSetChanged();
					}
				});				
				return true;
			}
		});
		
		//获取蓝牙管理和适配器		
		btManager=(BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
		btAdapter=btManager.getAdapter();
		if(btAdapter==null || !btAdapter.isEnabled()){
			btAdapter.enable();
		}

		btAdapter.startLeScan(mLeScanCallback);
		gifView.setVisibility(View.VISIBLE);
		gifView.setPaused(false);
		btnScan.setVisibility(View.GONE);
		handler.postDelayed(new Runnable() {					
			@Override
			public void run() {
				// TODO Auto-generated method stub						
				btAdapter.stopLeScan(mLeScanCallback);
				gifView.setVisibility(View.GONE);		
				gifView.setPaused(true);
				btnScan.setVisibility(View.VISIBLE);						
				Log.d("zz", "结束扫描");
				newDevicesBaseAdapter.notifyDataSetChanged();					
			}
		}, 3000);
								
	}

	@SuppressLint("NewApi")
	private LeScanCallback mLeScanCallback=new LeScanCallback() {

		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord){
			// TODO Auto-generated method stub
			if(device.getName()==null){
				return;
			}
			if(device.getName().substring(0, device.getName().length()-1).equals("yikai_shoes_")){
				for(HashMap<String, Object> deviceNew : devicesNewInfo){
					if(device.getAddress().equals(deviceNew.get("address"))){
						return;
					}
				}
				for(HashMap<String, Object> deviceOld : devicesOldInfo){
					if(device.getAddress().equals(deviceOld.get("address"))){
						return;
					}
				}
				HashMap<String, Object> deviceInfo=new HashMap<String, Object>();
				deviceInfo.put("name", device.getName());
				deviceInfo.put("address", device.getAddress());
				deviceInfo.put("status", false);
				devicesNewInfo.add(deviceInfo);
				//该回调方法貌似不能使用notifyDataSetChanged,原因不明,同步异步问题
				Log.d("zz", "添加一个："+device.getName()+"  "+device.getAddress());
				Log.d("zz", "新设备信息："+devicesNewInfo.size());
			}
			
		}		
	};

	

	private class MyBroadcastReceiver extends BroadcastReceiver{
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();						
			if(action.equals("connection_status_changed")){
				String address=intent.getStringExtra("address");
				for(int i=0; i<devicesNewInfo.size(); i++){
					if(devicesNewInfo.get(i).get("address").equals(address)){
						devicesNewInfo.remove(i);
						HashMap<String, Object> deviceOld=new HashMap<String, Object>();
						deviceOld.put("name", intent.getStringExtra("name"));
						deviceOld.put("address", intent.getStringExtra("address"));
						deviceOld.put("status", true);
						devicesOldInfo.add(deviceOld);
						try {
							editor.putString("old_devices", ClassParseUtil.list2String(devicesOldInfo)).commit();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						newDevicesBaseAdapter.notifyDataSetChanged();
						oldDevicesBaseAdapter.notifyDataSetChanged();
						break;
					}
				}

				for(HashMap<String, Object> deviceOld : devicesOldInfo){
					String oldAddress=(String) deviceOld.get("address");
					if(oldAddress.equals(BleService.leftAddress)){
						if(BleService.isLeftConnected){
							deviceOld.put("status", true);
							continue;
						}
					}
					if(oldAddress.equals(BleService.rightAddress)){
						if(BleService.isRightConnected){
							deviceOld.put("status", true);
							continue;
						}
					}
					deviceOld.put("status", false);
				}
				oldDevicesBaseAdapter.notifyDataSetChanged();
				if(loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
				
			}						
			
		}
		
	}

	//列表适配器
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public class NewDevicesBaseAdapter extends BaseAdapter{
		
		Context context;
		ArrayList<HashMap<String, Object>> datas;
		LayoutInflater inflater;
		String[] keys;
		int[] values;
		int source;
		
		private class ViewHolder{
			TextView name;
			TextView status;
		}
		
		public NewDevicesBaseAdapter(Context context, ArrayList<HashMap<String, Object>> datas,
				int source, String[] from, int[] to){
			this.context=context;
			inflater=LayoutInflater.from(this.context);
			this.source=source;
			this.datas=datas;
			keys=new String[from.length];
			System.arraycopy(from, 0, keys, 0, from.length);
			values=new int[to.length];
			System.arraycopy(to, 0, values, 0, to.length);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return datas.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return datas.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			// TODO Auto-generated method stub
			ViewHolder holder=null;
			if(convertView==null){				
				convertView=inflater.inflate(source, null);
				holder=new ViewHolder();
				holder.name=(TextView)convertView.findViewById(values[0]);
				holder.status=(TextView)convertView.findViewById(values[1]);
				convertView.setTag(holder);
			}else{
				holder=(ViewHolder) convertView.getTag();
			}
			holder.name.setText((String)datas.get(position).get("name"));
			if((Boolean) datas.get(position).get("status")){
				holder.status.setText("已连接");
				holder.status.setTextColor(0xffffffff);
			}else{
				holder.status.setText("未连接");
				holder.status.setTextColor(0xff794861);
			}
			
			return convertView;
		}
		
	}
	
	public class OldDevicesBaseAdapter extends BaseAdapter{
		
		Context context;
		ArrayList<HashMap<String, Object>> datas;
		LayoutInflater inflater;
		String[] keys;
		int[] values;
		int source;
		
		private class ViewHolder{
			TextView name;
			TextView status;
		}
		
		public OldDevicesBaseAdapter(Context context, ArrayList<HashMap<String, Object>> datas,
				int source, String[] from, int[] to){
			this.context=context;
			inflater=LayoutInflater.from(this.context);
			this.source=source;
			this.datas=datas;
			keys=new String[from.length];
			System.arraycopy(from, 0, keys, 0, from.length);
			values=new int[to.length];
			System.arraycopy(to, 0, values, 0, to.length);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return datas.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return datas.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			// TODO Auto-generated method stub
			ViewHolder holder=null;
			if(convertView==null){				
				convertView=inflater.inflate(source, null);
				holder=new ViewHolder();
				holder.name=(TextView)convertView.findViewById(values[0]);
				holder.status=(TextView)convertView.findViewById(values[1]);
				convertView.setTag(holder);
			}else{
				holder=(ViewHolder) convertView.getTag();
			}
			if((String)datas.get(position).get("newName")!=null){
				holder.name.setText((String)datas.get(position).get("newName"));
			}else{
				holder.name.setText((String)datas.get(position).get("name"));
			}			
			if((Boolean) datas.get(position).get("status")){
				holder.status.setText("已连接");
				holder.status.setTextColor(0xffffffff);
			}else{
				holder.status.setText("未连接");
				holder.status.setTextColor(0xff794861);
			}
			
			return convertView;
		}
		
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		unregisterReceiver(receiver);
	}
	

}
