
package com.shoes.activity; 

import java.lang.ref.WeakReference;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoes.R;
import com.shoes.customview.LoadingDialog;
import com.shoes.server.HttpServer;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class LoginActivity extends Activity{
	
	private Button btnLogin, btnRegister, btnSlip;
	private ImageButton btnLoginQQ, btnLoginWeixin, btnLoginWeibo;
	private TextView tvForgetPassword;
	private CheckBox cbRememberPassword;
	private EditText etName, etPassword;
	private LoadingDialog loadingDialog;
	private MyOnClickListener listener;
	private MyHandler handler;
	private MyBroadcastReceiver receiver;
	private AuthInfo authInfo;
	private SsoHandler ssoHandler;
	
	private IWXAPI api;
	
	public static String phoneNum;
	public static String userId;
	public static String userName;
	public static String sex;	
	public static String headImageUrl;
	public static String userArea;
	public static String description;
	public static boolean isLogin=false;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);	
		
		handler=new MyHandler(this);
		
		receiver=new MyBroadcastReceiver();
		IntentFilter filter=new IntentFilter("weixin_login_successfully");
		registerReceiver(receiver, filter);

		listener=new MyOnClickListener();
		
		loadingDialog=new LoadingDialog(this, "正在登陆中……");
		
		btnSlip=(Button)findViewById(R.id.btn_slip);
		btnSlip.setOnClickListener(listener);
		
		btnRegister=(Button)findViewById(R.id.btn_register);
		btnRegister.setOnClickListener(listener);
		
		cbRememberPassword=(CheckBox)findViewById(R.id.remember_pw);		
		
		tvForgetPassword=(TextView)findViewById(R.id.forget_pw);
		tvForgetPassword.setOnClickListener(listener);
		
		btnLogin=(Button)findViewById(R.id.btn_login);
		btnLogin.setOnClickListener(listener);
		
		btnLoginQQ=(ImageButton)findViewById(R.id.btn_qq_login);
		btnLoginQQ.setOnClickListener(listener);
		
		btnLoginWeixin=(ImageButton)findViewById(R.id.btn_weixin_login);
		btnLoginWeixin.setOnClickListener(listener);
		
		btnLoginWeibo=(ImageButton)findViewById(R.id.btn_weibo_login);
		btnLoginWeibo.setOnClickListener(listener);
		
		etName=(EditText)findViewById(R.id.login_name);
		etPassword=(EditText)findViewById(R.id.login_pw);
		
		SharedPreferences preferences2=getSharedPreferences("rememberPassword", Context.MODE_PRIVATE);
		if(preferences2.getBoolean("isRemember", false)){
			cbRememberPassword.setChecked(true);
			etName.setText(preferences2.getString("phoneNum", ""));
			etPassword.setText(preferences2.getString("password", ""));
		}else{
			etName.setText(preferences2.getString("phoneNum", ""));
			cbRememberPassword.setChecked(false);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode==11101){
			Tencent.onActivityResultData(requestCode, resultCode, data, new QQListener());
		}else if(requestCode==32973){
			if (ssoHandler != null) {
		        ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		    }
		}		
		
	}
	
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_logout, null);
			TextView tvMessage=(TextView) dialog.findViewById(R.id.tv_dialog_message);
			tvMessage.setText("是否退出登陆");
			new AlertDialog.Builder(this)
			.setView(dialog)
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					finish();
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
		return true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(loadingDialog!=null && loadingDialog.isShowing()){
			loadingDialog.dismiss();
			loadingDialog=null;
			
		}
		
		unregisterReceiver(receiver);

	}
	
	private static class MyHandler extends Handler{
		
		WeakReference<LoginActivity> weakReference;
		
		public MyHandler(LoginActivity activity){
			weakReference=new WeakReference<LoginActivity>(activity);
		}
		
		public void handleMessage(Message msg){
			LoginActivity activity=weakReference.get();
			if(activity!=null){
				activity.handleMessageOutside(msg);
			}
		}
	}
	
	private void handleMessageOutside(Message msg){
		if(msg.what==1){
			isLogin=true;	
			Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
			if(!etName.getText().toString().trim().equals("")){
				phoneNum=etName.getText().toString();
			}else{
				phoneNum="无";
			}			
			Intent intent=new Intent(LoginActivity.this, MainActivity.class);	
			startActivity(intent);
			finish();					
		}else if(msg.what==2){
			Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
			if(loadingDialog!=null && loadingDialog.isShowing()){
				loadingDialog.dismiss();
			}
		}
	}

	private class MyOnClickListener implements OnClickListener{
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_slip:
				SharedPreferences preferences=getSharedPreferences("default", Context.MODE_PRIVATE);
				sex=preferences.getString("sex", "男");			
				isLogin=false;
				startActivity(new Intent(LoginActivity.this, MainActivity.class));
				finish();
				break;
			case R.id.btn_register:
				startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
				break;
			case R.id.forget_pw:
				startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
				break;
			case R.id.btn_login:
				String patternName= "^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$";
				String patternPassword= "^(\\d|\\w|\\D){6,20}$";
				String name=etName.getText().toString().trim();
				String password=etPassword.getText().toString().trim();
				
				if(name==null || name.equals("")){
					Toast.makeText(LoginActivity.this, "手机号不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				if(password==null || password.equals("")){
					Toast.makeText(LoginActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				if(!name.matches(patternName)){
					Toast.makeText(LoginActivity.this, "输入手机号码不正确", Toast.LENGTH_SHORT).show();
					return;
				}
				if(!password.matches(patternPassword)){
					Toast.makeText(LoginActivity.this, "密码必须为6至20位", Toast.LENGTH_SHORT).show();
					return;
				}
				
				loadingDialog.show();
				
				SharedPreferences.Editor editor=getSharedPreferences("rememberPassword", Context.MODE_PRIVATE).edit();
				editor.putBoolean("isRemember", cbRememberPassword.isChecked());
				editor.putString("password", etPassword.getText().toString());
				editor.putString("phoneNum", etName.getText().toString());
				editor.commit();
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						userId=HttpServer.login(etName.getText().toString(), etPassword.getText().toString());
						if(userId!=null){
							HashMap<String, Object> userInfo=HttpServer.getUserInfo(userId);
							userId=(String)userInfo.get("userId");
							userName=(String)userInfo.get("userName");
							sex=(String)userInfo.get("sex");
							headImageUrl=(String)userInfo.get("headImageUrl");
							userArea=(String)userInfo.get("userArea");
							description=(String)userInfo.get("description");
							Message message=handler.obtainMessage();
							message.what=1;
							handler.sendMessage(message);
						}else{
							Message message=handler.obtainMessage();
							message.what=2;
							handler.sendMessage(message);
						}												
					}
				}).start();
				break;
			case R.id.btn_qq_login:
				loadingDialog.show();
				Tencent tencent=Tencent.createInstance("1105042325", LoginActivity.this);
				if(!tencent.isSessionValid()){
					tencent.login(LoginActivity.this, "", new QQListener());
				}
				break;
			case R.id.btn_weixin_login:
				loadingDialog.show();
				api=WXAPIFactory.createWXAPI(LoginActivity.this, "wx1d4cc222dde1750c");
				api.registerApp("wx1d4cc222dde1750c");
				if(!api.isWXAppInstalled()){
					Toast.makeText(LoginActivity.this, "未安装微信客户端", Toast.LENGTH_SHORT).show();
					return;
				}
				final SendAuth.Req req=new SendAuth.Req();
			    req.scope = "snsapi_userinfo";
			    req.state = "ofshoes";
			    api.sendReq(req);
				break;
			case R.id.btn_weibo_login:
				loadingDialog.show();
				authInfo=new AuthInfo(LoginActivity.this, "2350187022", "https://api.weibo.com/oauth2/default.html", null);
				ssoHandler=new SsoHandler(LoginActivity.this, authInfo);
				ssoHandler.authorize(new WeiboListener());
				break;
			}
		}
		
	}
	
	//qq登录接口
	private class QQListener implements IUiListener{

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			if(loadingDialog!=null && loadingDialog.isShowing()){
				loadingDialog.dismiss();
			}
		}

		@Override
		public void onComplete(final Object response) {
			// TODO Auto-generated method stub
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						JSONObject responseJson=new JSONObject(response.toString());
						String openId=responseJson.getString("openid");
						userId=HttpServer.loginWithOtherPlatform("1", openId);
						if(userId!=null){
							HashMap<String, Object> userInfo=HttpServer.getUserInfo(userId);
							userId=(String)userInfo.get("userId");
							userName=(String)userInfo.get("userName");
							sex=(String)userInfo.get("sex");
							headImageUrl=(String)userInfo.get("headImageUrl");
							userArea=(String)userInfo.get("userArea");
							description=(String)userInfo.get("description");
							Message message=handler.obtainMessage();
							message.what=1;
							handler.sendMessage(message);
						}else{
							Message message=handler.obtainMessage();
							message.what=2;
							handler.sendMessage(message);
						}	
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						Message message=handler.obtainMessage();
						message.what=2;
						handler.sendMessage(message);
					}
				}
			}).start();

		}

		@Override
		public void onError(UiError respnse) {
			// TODO Auto-generated method stub
			if(loadingDialog!=null && loadingDialog.isShowing()){
				loadingDialog.dismiss();
			}
		}
		
	}
	
	//微博登录接口
	private class WeiboListener implements WeiboAuthListener{

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			if(loadingDialog!=null && loadingDialog.isShowing()){
				loadingDialog.dismiss();
			}
		}

		@SuppressLint("NewApi")
		@Override
		public void onComplete(Bundle bundle) {
			// TODO Auto-generated method stub	
			if(Oauth2AccessToken.parseAccessToken(bundle).isSessionValid()){
				final Oauth2AccessToken token=Oauth2AccessToken.parseAccessToken(bundle);
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						
						userId=HttpServer.loginWithOtherPlatform("3", token.getUid());
						if(userId!=null){
							HashMap<String, Object> userInfo=HttpServer.getUserInfo(userId);
							userId=(String)userInfo.get("userId");
							userName=(String)userInfo.get("userName");
							sex=(String)userInfo.get("sex");
							headImageUrl=(String)userInfo.get("headImageUrl");
							userArea=(String)userInfo.get("userArea");
							description=(String)userInfo.get("description");
							Message message=handler.obtainMessage();
							message.what=1;
							handler.sendMessage(message);
						}else{
							Message message=handler.obtainMessage();
							message.what=2;	
							handler.sendMessage(message);
						}	
					}
							
				}).start();
				
			}else{
				Message message=handler.obtainMessage();
				message.what=2;
				handler.sendMessage(message);
			}
		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO Auto-generated method stub
			if(loadingDialog!=null && loadingDialog.isShowing()){
				loadingDialog.dismiss();
			}
		}
		
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent data) {
			// TODO Auto-generated method stub
			if(data.getAction().equals("weixin_login_successfully")){
				int result=data.getIntExtra("result", -2);
				if(result==0){
					final String code=data.getStringExtra("code");
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							String openid=HttpServer.getOpenId(code);
							if(openid!=null){								
								userId=HttpServer.loginWithOtherPlatform("2", openid);
								if(userId!=null){
									HashMap<String, Object> userInfo=HttpServer.getUserInfo(userId);
									userId=(String)userInfo.get("userId");
									userName=(String)userInfo.get("userName");
									sex=(String)userInfo.get("sex");
									headImageUrl=(String)userInfo.get("headImageUrl");
									userArea=(String)userInfo.get("userArea");
									description=(String)userInfo.get("description");
									Message message=handler.obtainMessage();
									message.what=1;
									handler.sendMessage(message);
								}else{
									Message message=handler.obtainMessage();
									message.what=2;	
									handler.sendMessage(message);
								}																		
							}else{
								Message message=handler.obtainMessage();
								message.what=2;	
								handler.sendMessage(message);
							}
						}
					}).start();
					
				}
				if(loadingDialog.isShowing()){
					loadingDialog.dismiss();
				}
			}
		}
		
	}

	
}
		