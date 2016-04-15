 
package com.shoes.activity; 

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoes.R;
import com.shoes.server.HttpServer;
import com.squareup.picasso.Picasso;

public class MyMessageActivity extends Activity {
	
	private ImageView btnBack;
	private LinearLayout btnHeadImage, btnName, btnArea, btnSex, btnSignature;
	private TextView tvName, tvUserId, tvArea, tvSex, tvDescription;
	private MyOnClickListener listener;
	private ImageView headImage;
	private Handler handler;
	private ExecutorService executorService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_my_message);
		
		listener=new MyOnClickListener();
		handler=new Handler();
		executorService=Executors.newFixedThreadPool(1);
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(listener);
		
		btnHeadImage=(LinearLayout)findViewById(R.id.btn_message_head_image);		
		btnHeadImage.setOnClickListener(listener);
		
		btnName=(LinearLayout)findViewById(R.id.btn_message_name);
		btnName.setOnClickListener(listener);
		
		btnArea=(LinearLayout)findViewById(R.id.btn_message_area);
		btnArea.setOnClickListener(listener);
		
		btnSex=(LinearLayout)findViewById(R.id.btn_message_sex);
		btnSex.setOnClickListener(listener);
		
		btnSignature=(LinearLayout)findViewById(R.id.btn_message_signature);
		btnSignature.setOnClickListener(listener);
		
		headImage=(ImageView)findViewById(R.id.my_message_head_image);
		Picasso.with(this).load(LoginActivity.headImageUrl).error(R.drawable.head_image_default).into(headImage);
		
		tvName=(TextView)findViewById(R.id.tv_message_name);
		tvName.setText(LoginActivity.userName);
		
		tvUserId=(TextView)findViewById(R.id.tv_message_user_id);
		tvUserId.setText(LoginActivity.userId);
		
		tvArea=(TextView)findViewById(R.id.tv_message_area);
		tvArea.setText(LoginActivity.userArea);
		
		tvSex=(TextView)findViewById(R.id.tv_message_sex);
		tvSex.setText(LoginActivity.sex);
		
		tvDescription=(TextView)findViewById(R.id.tv_message_description);
		tvDescription.setText(LoginActivity.description);
		
		
	}

	
	private class MyOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_back:
				finish();
				break;
			
			case R.id.btn_message_head_image:
				AlertDialog.Builder builder=new AlertDialog.Builder(MyMessageActivity.this);
				LinearLayout dialog1=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_head_image, null);
				builder.setView(dialog1);
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
				});
				final AlertDialog dialog=builder.create();
				dialog.show();
				
				Button btnGetAlbum=(Button)dialog1.findViewById(R.id.btn_head_image_album);
				btnGetAlbum.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						
						Intent intentAlbum=new Intent(Intent.ACTION_PICK);
						intentAlbum.setType("image/*");
						startActivityForResult(intentAlbum, 1);
						dialog.dismiss();
					}
				});
				
				Button btnGetCamera=(Button)dialog1.findViewById(R.id.btn_head_image_camera);
				btnGetCamera.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
						startActivityForResult(intent, 3);
						dialog.dismiss();
					}
				});
				
				break;

			case R.id.btn_message_name:
				LinearLayout dialog2=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_nickname, null);
				final EditText etNickname=(EditText)dialog2.findViewById(R.id.edit_nickname);
				etNickname.setText(LoginActivity.userName);
				
				new AlertDialog.Builder(MyMessageActivity.this)
				.setView(dialog2)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						if(!etNickname.getText().toString().equals(LoginActivity.userName)){
							executorService.submit(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									HashMap<String, String> texts=new HashMap<String, String>();
									texts.put("users.userId", LoginActivity.userId);
									texts.put("users.userName", etNickname.getText().toString());
									String result=HttpServer.updateUserInfo(texts, null);
									if(result.equals("0")){
										handler.post(new Runnable() {
											
											@Override
											public void run() {
												// TODO Auto-generated method stub
												tvName.setText(etNickname.getText().toString());
												LoginActivity.userName=etNickname.getText().toString();
												setResult(1);
											}
										});
									}else{
										handler.post(new Runnable() {
											
											@Override
											public void run() {
												// TODO Auto-generated method stub
												Toast.makeText(MyMessageActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
											}
										});
									}
								}
							});
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
				break;
				
			case R.id.btn_message_area:
				LinearLayout dialog3=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_area, null);
				final EditText etArea=(EditText)dialog3.findViewById(R.id.edit_area_input);
				etArea.setText(LoginActivity.userArea);
				
				new AlertDialog.Builder(MyMessageActivity.this)
				.setView(dialog3)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						if(!etArea.getText().toString().equals(LoginActivity.userArea)){
							executorService.submit(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									HashMap<String, String> texts=new HashMap<String, String>();
									texts.put("users.userId", LoginActivity.userId);
									texts.put("users.userArea", etArea.getText().toString());
									String result=HttpServer.updateUserInfo(texts, null);
									if(result.equals("0")){
										handler.post(new Runnable() {
											
											@Override
											public void run() {
												// TODO Auto-generated method stub
												tvArea.setText(etArea.getText().toString());
												LoginActivity.userArea=etArea.getText().toString();
												setResult(1);
											}
										});
									}else{
										handler.post(new Runnable() {
											
											@Override
											public void run() {
												// TODO Auto-generated method stub
												Toast.makeText(MyMessageActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
											}
										});
									}
								}
							});
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
				break;
			
			case R.id.btn_message_sex:
				LinearLayout dialog4=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_sex, null);
				RadioButton btnMale=(RadioButton)dialog4.findViewById(R.id.sex_male);
				RadioButton btnFemale=(RadioButton)dialog4.findViewById(R.id.sex_female);
				final RadioGroup radioGroup=(RadioGroup)dialog4.findViewById(R.id.sex_radiogroup);
				if(LoginActivity.sex.equals("男")){
					btnMale.setChecked(true);
				}else{
					btnFemale.setChecked(true);
				}				
				new AlertDialog.Builder(MyMessageActivity.this)
				.setView(dialog4)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						if(radioGroup.getCheckedRadioButtonId()==R.id.sex_male){
							if(!LoginActivity.sex.equals("男")){
								executorService.submit(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										HashMap<String, String> texts=new HashMap<String, String>();
										texts.put("users.userId", LoginActivity.userId);
										texts.put("users.sex", "男");
										String result=HttpServer.updateUserInfo(texts, null);
										if(result.equals("0")){
											handler.post(new Runnable() {
												
												@Override
												public void run() {
													// TODO Auto-generated method stub
													tvSex.setText("男");
													LoginActivity.sex="男";
													setResult(1);
												}
											});
										}else{
											handler.post(new Runnable() {
												
												@Override
												public void run() {
													// TODO Auto-generated method stub
													Toast.makeText(MyMessageActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
												}
											});
										}
									}
								});
							}
						}else{
							if(!LoginActivity.sex.equals("女")){
								executorService.submit(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										HashMap<String, String> texts=new HashMap<String, String>();
										texts.put("users.userId", LoginActivity.userId);
										texts.put("users.sex", "女");
										String result=HttpServer.updateUserInfo(texts, null);
										if(result.equals("0")){
											handler.post(new Runnable() {
												
												@Override
												public void run() {
													// TODO Auto-generated method stub
													tvSex.setText("女");
													LoginActivity.sex="女";
													setResult(1);
												}
											});
										}else{
											handler.post(new Runnable() {
												
												@Override
												public void run() {
													// TODO Auto-generated method stub
													Toast.makeText(MyMessageActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
												}
											});
										}
									}
								});
							}
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
				break;
			
			case R.id.btn_message_signature:
				LinearLayout dialog5=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_signature, null);
				final EditText etDescription=(EditText)dialog5.findViewById(R.id.edit_description);
				etDescription.setText(LoginActivity.description);
				new AlertDialog.Builder(MyMessageActivity.this)
				.setView(dialog5)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						if(!etDescription.getText().equals(LoginActivity.description)){
							executorService.submit(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									HashMap<String, String> texts=new HashMap<String, String>();
									texts.put("users.userId", LoginActivity.userId);
									texts.put("users.individualDescription", etDescription.getText().toString());
									String result=HttpServer.updateUserInfo(texts, null);
									if(result.equals("0")){
										handler.post(new Runnable() {
											
											@Override
											public void run() {
												// TODO Auto-generated method stub
												tvDescription.setText(etDescription.getText().toString());
												LoginActivity.description=etDescription.getText().toString();
												setResult(1);
											}
										});
									}else{
										handler.post(new Runnable() {
											
											@Override
											public void run() {
												// TODO Auto-generated method stub
												Toast.makeText(MyMessageActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
											}
										});
									}
								}
							});
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
				break;

			}
		}
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode==1 && resultCode==RESULT_OK){
			Uri uri=data.getData();
			if(uri==null){
				return;
			}
			Intent intent=new Intent();
			intent.setAction("com.android.camera.action.CROP");
			intent.setDataAndType(uri, "image/*");			
			intent.putExtra("crop", true);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 80);
			intent.putExtra("outputY", 80);
			intent.putExtra("return-data", true);
			
			startActivityForResult(intent, 2);
		}
		if(requestCode==2 && resultCode==RESULT_OK){
			final Bitmap bitmap=data.getParcelableExtra("data");	
			File f=null;
			try{
				File folder=new File(Environment.getExternalStorageDirectory()+"/shoes");
				if(!folder.exists()){
					folder.mkdirs();
				}
				f=new File(Environment.getExternalStorageDirectory()+"/shoes", "clip.png");
			
				
				FileOutputStream fos=new FileOutputStream(f);
				bitmap.compress(CompressFormat.PNG, 80, fos);
				fos.flush();
				fos.close();
			}catch(Exception e){						
				Log.d("zz", e.toString());
				return;
			}
			final HashMap<String, String> texts=new HashMap<String, String>();
			texts.put("users.userId", LoginActivity.userId);
			final HashMap<String, String> files=new HashMap<String, String>();
			files.put("head", Uri.fromFile(f).toString());
			executorService.submit(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					String result=HttpServer.updateUserInfo(texts, files);
					if(result.equals("0")){
						setResult(1);
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								headImage.setImageBitmap(bitmap);
							}
						});
					}else{
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(MyMessageActivity.this, "修改头像失败", Toast.LENGTH_SHORT).show();
							}
						});
					}
				}
			});
			
		}
		if(requestCode==3 && resultCode==RESULT_OK){
			Uri uri=data.getData();
			if(uri==null){
				Bundle bundle=data.getExtras();
				if(bundle!=null){
					Log.d("zz", "bundle 返回");
					Bitmap bitmap=(Bitmap)bundle.get("data");
					File f=new File(Environment.getExternalStorageDirectory()+"/shoes", "clipImage.png");
					if(f.exists()){
						f.delete();
					}
					try{
						FileOutputStream fos=new FileOutputStream(f);
						bitmap.compress(CompressFormat.PNG, 80, fos);
						fos.flush();
						fos.close();
					}catch(Exception e){						
						Log.d("zz", e.toString());
						return;
					}
					uri=Uri.fromFile(f);
				}
			}
			Intent intent=new Intent();
			intent.setAction("com.android.camera.action.CROP");
			intent.setDataAndType(uri, "image/*");			
			intent.putExtra("crop", true);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 80);
			intent.putExtra("outputY", 80);
			intent.putExtra("return-data", true);				
			startActivityForResult(intent, 2);
				
			
		}
	}

	
}
