/**  
* @Project: Shoes
* @Title: RegisterActivity.java
* @Package com.shoes.activity
* @Description: TODO
* @author lzj
* @date 2015-10-15 上午11:05:23
* @version V1.0  
*/ 
package com.shoes.activity; 

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import com.example.shoes.R;
import com.shoes.server.HttpServer;

public class RegisterActivity extends Activity {
	
	private ImageButton btnBack;
	private Button btnGetVerificationCode, btnRegister;
	private EditText etName, etPassword, etEnsurePassword, etCode;
	private CheckBox cbReadProfile;
	private MyOnClickListener listener;
	private MyHandler handler;
//	private Handler handler;
	
	private final String APP_KEY="b62117506765";
	private final String APP_SECRET="b523fa6ccb90942974f97dab1b7dc100";

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
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
		
		//线程通知更改UI
		handler=new MyHandler(this);
		
		listener=new MyOnClickListener();
		
		etName=(EditText)findViewById(R.id.register_name);
		etPassword=(EditText)findViewById(R.id.register_pw);
		etEnsurePassword=(EditText)findViewById(R.id.register_ensure_pw);
		etCode=(EditText)findViewById(R.id.register_verification_code);
		
		btnBack=(ImageButton)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(listener);
		
		btnGetVerificationCode=(Button)findViewById(R.id.btn_get_verification_code);
		btnGetVerificationCode.setOnClickListener(listener);
		
		btnRegister=(Button)findViewById(R.id.btn_register);
		btnRegister.setOnClickListener(listener);
		
		cbReadProfile=(CheckBox)findViewById(R.id.register_cb);
		
	}
	
	private void handleMessageOutside(Message msg){
		if(msg.what==SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE){
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					String result=HttpServer.register(etName.getText().toString(), etPassword.getText().toString());
					Message message=handler.obtainMessage();
					message.what=1;
					message.obj=result;
					handler.sendMessage(message);
				}
			}).start();
		}else if(msg.what==SMSSDK.RESULT_ERROR){
			Toast.makeText(RegisterActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
		}else if(msg.what==1){
			Toast.makeText(RegisterActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
		}
	}
	
	private static class MyHandler extends Handler{
		private WeakReference<RegisterActivity> weakReference;
		
		public MyHandler(RegisterActivity activity){
			weakReference=new WeakReference<RegisterActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			RegisterActivity activity=weakReference.get();
			if(activity!=null){
				activity.handleMessageOutside(msg);
			}
		}
		
		
	}

	private class MyOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			case R.id.btn_get_verification_code:				
				String patternName1= "^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$";
				String phone=etName.getText().toString();
				if(phone==null || phone.equals("")){
					Toast.makeText(RegisterActivity.this, "手机号码不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				if(phone.matches(patternName1)){
					SMSSDK.getVerificationCode("86", phone);
				}else{
					Toast.makeText(RegisterActivity.this, "电话号码不正确", Toast.LENGTH_SHORT).show();
					return;
				}
				btnGetVerificationCode.setEnabled(false);
				new CountDownTimer(60000, 1000) {
					
					@Override
					public void onTick(long time) {
						// TODO Auto-generated method stub
						btnGetVerificationCode.setText(time/1000+"秒");
						
					}
					
					@Override
					public void onFinish() {
						// TODO Auto-generated method stub
						btnGetVerificationCode.setEnabled(true);
						btnGetVerificationCode.setText("获取验证码");
					}
				}.start();
				break;
			case R.id.btn_register:
				String patternName2= "^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$";
				String patternPassword= "^(\\d|\\w|\\D){6,20}$";
				String name=etName.getText().toString().trim();
				String password=etPassword.getText().toString().trim();
				String ensurePassword=etEnsurePassword.getText().toString().trim();
				String code=etCode.getText().toString().trim();
				
				if(name==null || name.equals("")){
					Toast.makeText(RegisterActivity.this, "手机号码不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				if(password==null || password.equals("")){
					Toast.makeText(RegisterActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				if(ensurePassword==null || ensurePassword.equals("")){
					Toast.makeText(RegisterActivity.this, "再次输入密码不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				if(code==null || code.equals("")){
					Toast.makeText(RegisterActivity.this, "验证码不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				if(!cbReadProfile.isChecked()){
					Toast.makeText(RegisterActivity.this, "还没同意注册协议", Toast.LENGTH_SHORT).show();
					return;
				}
				if(!name.matches(patternName2)){
					Toast.makeText(RegisterActivity.this, "手机号码不正确", Toast.LENGTH_SHORT).show();
					return;
				}
				if(!password.matches(patternPassword)){
					Toast.makeText(RegisterActivity.this, "密码必须为6至20位", Toast.LENGTH_SHORT).show();
					return;
				}
				if(!password.equals(ensurePassword)){
					Toast.makeText(RegisterActivity.this, "再次输入密码不正确", Toast.LENGTH_SHORT).show();
					return;
				}
				
				SMSSDK.submitVerificationCode("86", name, code);//提交验证码，然后在EventHandler接口处理信息								

				break;
			}
		}
		
	}	
}
