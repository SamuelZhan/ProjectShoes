
package com.shoes.activity; 

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.shoes.R;
import com.shoes.constants.VirtualCallContants;

public class WallpaperActivity extends Activity {

	private ImageView btnBack;
	private RelativeLayout btnGetAlbum, btnGetCamera;
	private ImageView defaultWallpaper1, defaultWallpaper2, wallpaperPreview;
	private ImageView defaultWallpaperChecked1, defaultWallpaperChecked2;
	private int wallpaper;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wallpaper);
		
		Intent intent=getIntent();
		wallpaper=intent.getIntExtra("wallpaper", VirtualCallContants.WALLPAPER_ANDROID);
		
		btnBack=(ImageView)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent=getIntent();
				intent.putExtra("wallpaper", wallpaper);
				setResult(2, intent);
				finish();
			}
		});
		
		btnGetAlbum=(RelativeLayout)findViewById(R.id.get_album);
		btnGetAlbum.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(Intent.ACTION_PICK);
				intent.setType("image/*");
				startActivityForResult(intent, 1);
			}
		});
		
		btnGetCamera=(RelativeLayout)findViewById(R.id.get_camera);
		btnGetCamera.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String state=Environment.getExternalStorageState();
				if(state.equals(Environment.MEDIA_MOUNTED)){
					Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
					startActivityForResult(intent, 2);
				}
			}
		});
		
		defaultWallpaper1=(ImageView)findViewById(R.id.default_wallpaper_1);
		defaultWallpaper1.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				defaultWallpaperChecked1.setVisibility(View.VISIBLE);
				defaultWallpaperChecked2.setVisibility(View.GONE);
				wallpaperPreview.setImageResource(R.drawable.wallpaper_default1);
				wallpaper=R.drawable.wallpaper_default1;
			}
		});
		defaultWallpaperChecked1=(ImageView)findViewById(R.id.default_wallpaper_1_checked);
		
		defaultWallpaper2=(ImageView)findViewById(R.id.default_wallpaper_2);
		defaultWallpaper2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				defaultWallpaperChecked1.setVisibility(View.GONE);
				defaultWallpaperChecked2.setVisibility(View.VISIBLE);
				wallpaperPreview.setImageResource(R.drawable.wallpaper_default2);
				wallpaper=R.drawable.wallpaper_default2;
			}
		});
		defaultWallpaperChecked2=(ImageView)findViewById(R.id.default_wallpaper_2_checked);
		
		wallpaperPreview=(ImageView)findViewById(R.id.wallpaper_preview);		
		
		switch (wallpaper) {
		case VirtualCallContants.WALLPAPER_ANDROID:
			defaultWallpaperChecked1.setVisibility(View.VISIBLE);
			defaultWallpaperChecked2.setVisibility(View.GONE);
			wallpaperPreview.setImageResource(R.drawable.wallpaper_default1);
			break;

		case VirtualCallContants.WALLPAPER_IOS:
			defaultWallpaperChecked1.setVisibility(View.GONE);
			defaultWallpaperChecked2.setVisibility(View.VISIBLE);
			wallpaperPreview.setImageResource(R.drawable.wallpaper_default2);
			break;
			
		case VirtualCallContants.WALLPAPER_CUSTOM:
			defaultWallpaperChecked1.setVisibility(View.GONE);
			defaultWallpaperChecked2.setVisibility(View.GONE);
			try {
				wallpaperPreview.setImageBitmap(BitmapFactory.decodeStream(openFileInput("tempImage")));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}		
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub		
		if(requestCode==1 && resultCode==RESULT_OK){			
			Uri uri=data.getData();
			Bitmap bitmap=scaleBitmap(uri);
			wallpaperPreview.setImageBitmap(bitmap);
			compressAndStoreBitmap(bitmap);
			wallpaper=3;
			defaultWallpaperChecked1.setVisibility(View.GONE);
			defaultWallpaperChecked2.setVisibility(View.GONE);
		}else if(requestCode==2 && resultCode==RESULT_OK){
			Uri uri=data.getData();
			if(uri==null){
				Bundle bundle=data.getExtras();
				if(bundle!=null){
					Bitmap bitmap=(Bitmap)bundle.get("data");
					wallpaperPreview.setImageBitmap(bitmap);
					compressAndStoreBitmap(bitmap);
				}
			}else{
				Bitmap bitmap=scaleBitmap(uri);
				wallpaperPreview.setImageBitmap(bitmap);
				compressAndStoreBitmap(bitmap);
			}
			wallpaper=3;
			defaultWallpaperChecked1.setVisibility(View.GONE);
			defaultWallpaperChecked2.setVisibility(View.GONE);
		}
		
	}	

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			Intent intent=getIntent();
			intent.putExtra("wallpaper", wallpaper);
			setResult(2, intent);
			finish();
		}
		return true;
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
			FileOutputStream fos=openFileOutput("tempImage", MODE_PRIVATE);			
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
