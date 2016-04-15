
package com.shoes.activity; 

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.shoes.R;
import com.shoes.server.HttpServer;

public class ResetPasswordNextActivity extends Activity {

	private ImageView btnBack;
	private Button btnEnsure;
	private ImageButton btnDelete1, btnDelete2;
	private EditText etPassword1, etPassword2;
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password_next);
		
		handler=new Handler(){
			
			public void handleMessage(Message msg){
				if(msg.what==1){
					Toast.makeText(ResetPasswordNextActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
					setResult(1);
					finish();
				}
			}
		};
		
		etPassword1=(EditText)findViewById(R.id.reset_password_input_1);
		etPassword2=(EditText)findViewById(R.id.reset_password_input_2);
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnEnsure=(Button)findViewById(R.id.reset_password_btn_ensure);
		btnEnsure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String password1=etPassword1.getText().toString();
				String password2=etPassword2.getText().toString();
				if(password1.equals("")){
					Toast.makeText(ResetPasswordNextActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
					return;
				}
				if(password2.equals("")){
					Toast.makeText(ResetPasswordNextActivity.this, "请再次输入密码", Toast.LENGTH_SHORT).show();
					return;
				}
				if(!password1.equals(password2)){
					Toast.makeText(ResetPasswordNextActivity.this, "再次输入密码错误", Toast.LENGTH_SHORT).show();
					return;
				}
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						String result=HttpServer.resetPassword(LoginActivity.phoneNum, etPassword1.getText().toString());
						Message message=handler.obtainMessage();
						message.what=1;
						message.obj=result;
						handler.sendMessage(message);
					}
				}).start();
			}
		});
		
		btnDelete1=(ImageButton)findViewById(R.id.reset_password_delete_1);
		btnDelete1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				etPassword1.setText("");
			}
		});
		
		btnDelete2=(ImageButton)findViewById(R.id.reset_password_delete_2);
		btnDelete2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				etPassword2.setText("");
			}
		});
	}

}
