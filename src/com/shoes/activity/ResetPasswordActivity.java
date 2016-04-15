
package com.shoes.activity; 

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import com.example.shoes.R;

public class ResetPasswordActivity extends Activity {
	
	private ImageView btnBack;
	private Button btnNext;
	private TextView tvPhoneNum;
	private EditText etCode;
	private Button btnGetCode;
	
	private final String APP_KEY="b62117506765";
	private final String APP_SECRET="b523fa6ccb90942974f97dab1b7dc100";
	private boolean isVerified=false;
	
	private Handler handler;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password);
		
		handler=new Handler(){
			
			public void handleMessage(Message msg){
				if(msg.what==SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE){
					startActivityForResult(new Intent(ResetPasswordActivity.this, ResetPasswordNextActivity.class), 1);
					isVerified=true;
				}else{
					Toast.makeText(ResetPasswordActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
				}
			}			
		};
		
		SMSSDK.initSDK(this, APP_KEY, APP_SECRET);
		//验证码的监听回调接口，不能在此更改UI，报错
		EventHandler eh=new EventHandler(){

			@Override
			public void afterEvent(int event, int result, Object data) {
				Log.d("zz", event+"    "+result);
				if(result==SMSSDK.RESULT_COMPLETE){
					if(event==SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE){
						Log.d("zz", "成功提交验证码");
						handler.sendEmptyMessage(SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE);						
					}else if(event==SMSSDK.EVENT_GET_VERIFICATION_CODE){
						Log.d("zz", "成功获取验证码");
					}else{
						handler.sendEmptyMessage(SMSSDK.RESULT_ERROR);
					}
				}else{					
					handler.sendEmptyMessage(SMSSDK.RESULT_ERROR);
				}
			}
			
		};
		SMSSDK.registerEventHandler(eh);
		
		
		
		tvPhoneNum=(TextView)findViewById(R.id.reset_password_phone_number);
		tvPhoneNum.setText(LoginActivity.phoneNum);
		
		etCode=(EditText)findViewById(R.id.reset_password_verification_code);
		
		btnGetCode=(Button)findViewById(R.id.reset_password_btn_get_verification_code);
		btnGetCode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				SMSSDK.getVerificationCode("86", LoginActivity.phoneNum);
				new CountDownTimer(60000, 1000) {
					
					@Override
					public void onTick(long time) {
						// TODO Auto-generated method stub
						btnGetCode.setText(time/1000+"秒");
						
					}
					
					@Override
					public void onFinish() {
						// TODO Auto-generated method stub
						btnGetCode.setEnabled(true);
						btnGetCode.setText("获取验证码");
					}
				}.start();
				btnGetCode.setEnabled(false);
			}
		});
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnNext=(Button)findViewById(R.id.reset_password_btn_next);
		btnNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(isVerified){
					startActivityForResult(new Intent(ResetPasswordActivity.this, ResetPasswordNextActivity.class), 1);
				}else{
					if(!etCode.getText().toString().equals("")){
						SMSSDK.submitVerificationCode("86", LoginActivity.phoneNum, etCode.getText().toString());
					}else{
						Toast.makeText(ResetPasswordActivity.this, "请输入验证码", Toast.LENGTH_SHORT).show();
					}
										
				}								
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode==1 && resultCode==1){
			finish();
		}
	}
	
	

}
