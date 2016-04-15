
package com.shoes.fragment; 

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoes.R;
import com.shoes.activity.AboutUsActivity;
import com.shoes.activity.ConnectionActivity;
import com.shoes.activity.HeroActivity;
import com.shoes.activity.LoginActivity;
import com.shoes.activity.MyMessageActivity;
import com.shoes.activity.ResetPasswordActivity;
import com.shoes.activity.TutorialActivity;
import com.shoes.database.DatabaseHelper;
import com.shoes.server.HttpServer;
import com.shoes.service.BleService;
import com.squareup.picasso.Picasso;

@SuppressLint("NewApi")
public class MineFragment extends Fragment {

	private ImageView btnMyMessage;
	private LinearLayout btnAboutUs;
	private TextView btnConnection, btnClearCache, btnHero, btnTutorial, btnResetPassword, btnLogout, tvNickName, tvUserId;
	private MyOnClickListener listener;
	private Handler handler;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		View rootView=inflater.inflate(R.layout.fragment_mine, null);
		
		listener=new MyOnClickListener();
		handler=new Handler();
		
		btnMyMessage=(ImageView)rootView.findViewById(R.id.mine_message);
		if(LoginActivity.isLogin){
			btnMyMessage.setImageResource(R.drawable.head_image_default);
			Picasso.with(getActivity()).load(LoginActivity.headImageUrl).error(R.drawable.head_image_default).into(btnMyMessage);
		}else{
			btnMyMessage.setImageResource(R.drawable.head_image_default);
		}
		btnMyMessage.setOnClickListener(listener);
		
		tvNickName=(TextView)rootView.findViewById(R.id.mine_nickname);
		if(LoginActivity.isLogin){
			tvNickName.setText(LoginActivity.userName);
		}else{
			tvNickName.setText("未登录");
		}
				
		tvUserId=(TextView)rootView.findViewById(R.id.mine_user_id);
		if(LoginActivity.isLogin){
			tvUserId.setText("会员ID: "+LoginActivity.userId);
		}else{
			tvUserId.setText("");
		}
				
		btnConnection=(TextView)rootView.findViewById(R.id.mine_connection);
		btnConnection.setOnClickListener(listener);
		
		btnAboutUs=(LinearLayout)rootView.findViewById(R.id.mine_about_us);
		btnAboutUs.setOnClickListener(listener);
		
		btnClearCache=(TextView)rootView.findViewById(R.id.mine_clear_cache);
		btnClearCache.setOnClickListener(listener);
		
		btnHero=(TextView)rootView.findViewById(R.id.mine_hero);
		btnHero.setOnClickListener(listener);
		
		btnTutorial=(TextView)rootView.findViewById(R.id.mine_tutorial);
		btnTutorial.setOnClickListener(listener);
		
		btnResetPassword=(TextView)rootView.findViewById(R.id.mine_reset_password);
		btnResetPassword.setOnClickListener(listener);
		
		btnLogout=(TextView)rootView.findViewById(R.id.mine_logout);
		btnLogout.setOnClickListener(listener);
		
		return rootView;
	}
	
	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode==1 && resultCode==1){
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					HashMap<String, Object> userCommunityInfos=HttpServer.getUserInfo(LoginActivity.userId);
					if(userCommunityInfos!=null){
						LoginActivity.headImageUrl=(String) userCommunityInfos.get("headImageUrl");
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Picasso.with(getActivity()).load(LoginActivity.headImageUrl).error(R.drawable.head_image_default).into(btnMyMessage);
								tvNickName.setText(LoginActivity.userName);
							}
						});
						CommunityFragment fragment=(CommunityFragment) getActivity().getFragmentManager().findFragmentByTag("community");
						if(fragment!=null){
							fragment.changeInfos();
						}
					}
				}
			}).start();
			
			
		}
	}

	private class MyOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.mine_message:
				if(LoginActivity.isLogin){
					startActivityForResult(new Intent(getActivity(), MyMessageActivity.class), 1);
				}else{
					LinearLayout dialog=(LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog_logout, null);
					TextView tvMessage=(TextView) dialog.findViewById(R.id.tv_dialog_message);
					tvMessage.setText("未登录，是否要登陆");
					new AlertDialog.Builder(getActivity())
					.setView(dialog)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							startActivity(new Intent(getActivity(), LoginActivity.class));
							getActivity().finish();
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
				break;
			
			case R.id.mine_connection:
				startActivity(new Intent(getActivity(), ConnectionActivity.class));
				break;

			case R.id.mine_clear_cache:
				LinearLayout dialog1=(LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog_clear_cache, null);
				new AlertDialog.Builder(getActivity())
				.setView(dialog1)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						getActivity().getSharedPreferences("sport_data", Context.MODE_PRIVATE).edit().clear().commit();
						getActivity().getSharedPreferences("virtual_call_setting", Context.MODE_PRIVATE).edit().clear().commit();
						SQLiteDatabase db=new DatabaseHelper(getActivity(), "sport_data").getWritableDatabase();
						db.execSQL("DELETE FROM sportData");
						db.close();
						BleService.totalSteps=0;
						BleService.totalSportTime=0;
						BleService.totalCalorie=0f;
						BleService.totalDistance=0;	
						BleService.intervalSteps=0;
						BleService.intervalSportTime=0;
						BleService.intervalCalorie=0f;
						BleService.intervalDistance=0;
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
				break;
				
			case R.id.mine_hero:
				if(LoginActivity.isLogin){
					startActivity(new Intent(getActivity(), HeroActivity.class));
				}else{
					Toast.makeText(getActivity(), "未登录", Toast.LENGTH_SHORT).show();
				}
				break;
				
			case R.id.mine_about_us:
				startActivity(new Intent(getActivity(), AboutUsActivity.class));
				break;
				
			case R.id.mine_reset_password:
				if(LoginActivity.isLogin){
					startActivity(new Intent(getActivity(), ResetPasswordActivity.class));
				}else{
					Toast.makeText(getActivity(), "未登录", Toast.LENGTH_SHORT).show();
				}
				break;
				
			case R.id.mine_logout:
				if(LoginActivity.isLogin){
					LinearLayout dialog2=(LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog_logout, null);
					TextView tvMessage=(TextView) dialog2.findViewById(R.id.tv_dialog_message);
					tvMessage.setText("是否退出当前账号");
					new AlertDialog.Builder(getActivity())
					.setView(dialog2)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub		
							LoginActivity.isLogin=false;
							LoginActivity.userId=null;
							LoginActivity.userName=null;
							LoginActivity.sex=null;
							LoginActivity.headImageUrl=null;
							LoginActivity.description=null;
							LoginActivity.userArea=null;
							
							SharedPreferences.Editor editor=getActivity().getSharedPreferences("loginInfo", Context.MODE_PRIVATE).edit();
							editor.putBoolean("isLogin", LoginActivity.isLogin).commit();
					
							startActivity(new Intent(getActivity(), LoginActivity.class));
							getActivity().finish();
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
					LinearLayout dialog2=(LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog_logout, null);
					TextView tvMessage=(TextView) dialog2.findViewById(R.id.tv_dialog_message);
					tvMessage.setText("未登录，是否要登陆");
					new AlertDialog.Builder(getActivity())
					.setView(dialog2)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							startActivity(new Intent(getActivity(), LoginActivity.class));
							getActivity().finish();
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
				break;
				
			case R.id.mine_tutorial:
				startActivity(new Intent(getActivity(), TutorialActivity.class));
				break;
			}
		}
		
	}
}