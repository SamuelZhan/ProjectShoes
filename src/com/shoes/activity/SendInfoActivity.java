package com.shoes.activity; 

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.shoes.R;
import com.shoes.customview.LoadingDialog;
import com.shoes.customview.RectangleWithCrossView;
import com.shoes.server.HttpServer;

public class SendInfoActivity extends Activity {
	
	private ImageButton btnBack;
	private Button btnEnsure;
	private RectangleWithCrossView btnAddImage;
	private ImageView ivAdded;
	private EditText etAdded;
	private LoadingDialog loadingDialog;
	private boolean hasImage=false;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_send_info);
		
		handler=new Handler();
		loadingDialog=new LoadingDialog(this, "");
		
		btnBack=(ImageButton)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnEnsure=(Button)findViewById(R.id.btn_ensure);
		btnEnsure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				if(etAdded.getText().toString().trim().equals("") && !hasImage){
					Toast.makeText(SendInfoActivity.this, "请添加内容", Toast.LENGTH_SHORT).show();
				}else{
					loadingDialog.show();
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							HashMap<String, String> texts=new HashMap<String, String>();
							texts.put("thought.userId", LoginActivity.userId);
							texts.put("thought.content", etAdded.getText().toString());
							HashMap<String, String> files=new HashMap<String, String>();
							if(hasImage){								
								files.put("image", "imageAdded.png");
							}
							HashMap<String, Object> info=HttpServer.sendMessage(SendInfoActivity.this, texts, files);
							if(info==null){
								handler.post(new Runnable() {
									public void run() {
										Toast.makeText(SendInfoActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
									}
								});
							}else{
								Intent intent=new Intent();
								intent.putExtra("info", info);
								setResult(1, intent);
								finish();
							}
							if(loadingDialog!=null && loadingDialog.isShowing()){
								loadingDialog.dismiss();
							}
						}
					}).start();
				}
			}
		});
		
		btnAddImage=(RectangleWithCrossView)findViewById(R.id.btn_add_image);
		btnAddImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LinearLayout dialog=(LinearLayout)getLayoutInflater().inflate(R.layout.dialog_aad_image, null);
				Button btnAlbum=(Button)dialog.findViewById(R.id.btn_album);
				Button btnCamera=(Button)dialog.findViewById(R.id.btn_camera);
				
				final AlertDialog alertDialog=new AlertDialog.Builder(SendInfoActivity.this).setView(dialog).create();
				
				btnAlbum.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent=new Intent(Intent.ACTION_PICK);
						intent.setType("image/*");
						startActivityForResult(intent, 1);
						alertDialog.dismiss();
					}
				});
				
				btnCamera.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						String state=Environment.getExternalStorageState();
						if(state.equals(Environment.MEDIA_MOUNTED)){
							Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
							startActivityForResult(intent, 2);
							alertDialog.dismiss();
						}
					}
				});
			
				alertDialog.show();
				
			}
		});
		
		ivAdded=(ImageView)findViewById(R.id.added_image);
		etAdded=(EditText)findViewById(R.id.added_text);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub		
		if(requestCode==1 && resultCode==RESULT_OK){	
			
			Uri uri=data.getData();
			Bitmap bitmap=scaleBitmap(uri);
			ivAdded.setImageBitmap(bitmap);
			btnAddImage.setVisibility(View.GONE);
			ivAdded.setVisibility(View.VISIBLE);
			compressAndStoreBitmap(bitmap);
			hasImage=true;
		}else if(requestCode==2 && resultCode==RESULT_OK){
			Uri uri=data.getData();
			if(uri==null){
				Bundle bundle=data.getExtras();
				if(bundle!=null){
					Log.d("zz", "bundle 返回");
					Bitmap bitmap=(Bitmap)bundle.get("data");
					ivAdded.setImageBitmap(bitmap);
					btnAddImage.setVisibility(View.GONE);
					ivAdded.setVisibility(View.VISIBLE);
					compressAndStoreBitmap(bitmap);
				}
			}else{
				Bitmap bitmap=scaleBitmap(uri);
				ivAdded.setImageBitmap(bitmap);
				btnAddImage.setVisibility(View.GONE);
				ivAdded.setVisibility(View.VISIBLE);
				compressAndStoreBitmap(bitmap);
				
			}
			hasImage=true;
		}
		
	}
	
	//首先通过option来对bitmap进行精度压缩，根据主流分辨率计算缩放比例，图片会失真，避免OOM
	private Bitmap scaleBitmap(Uri uri){
		ContentResolver cr=getContentResolver();
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inJustDecodeBounds=true;
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri), null, options);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		options.inJustDecodeBounds=false;
		int w=options.outWidth;
		int h=options.outHeight;
		float ww=1080f;
		float hh=1920f;
		int scale=1;
		if(w>h && w>ww){
			scale=(int)(w/ww);
		}else if(w<h && h>hh){
			scale=(int)(h/hh);
		}
		if(scale<0) scale=1;
		options.inSampleSize=scale;
		try {
			bitmap=BitmapFactory.decodeStream(cr.openInputStream(uri), null, options);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bitmap;
	}
	
	//再对图片进行质量压缩，使图片能本地存储且方便上传到服务器，图片失真较轻
	private void compressAndStoreBitmap(Bitmap bitmap){
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();						
			bitmap.compress(CompressFormat.PNG, 10, baos);
			FileOutputStream fos=openFileOutput("imageAdded.png", MODE_PRIVATE);
			
			fos.write(baos.toByteArray());
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
